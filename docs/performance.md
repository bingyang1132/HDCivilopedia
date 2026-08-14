# 性能优化记录

记录把一轮生成从 **~3 小时降到 ~90 秒** 的过程与原理。核心结论：瓶颈全是**架构性 I/O 浪费，不是算力**——没有用 GPU、没动业务逻辑、产物不变。

## 流水线
`init`(建库) → `icons`(DDS 解码切图) → `load`(SQLite→Java 对象) → `write`(对象→JSON) → `page`(JSON→HTML)

## 优化一：load/write 阶段（连接复用 + 缓存）

**问题**
- `Tools.getText/getImage/tryGetRawText` 每次调用都 `DriverManager.getConnection(...)` 新建连接，且多数连接未关闭（泄漏）。这两个方法在"每对象 × 每字段 × 每语言"被调用成千上万次。
- 无任何缓存；`getImage` 还对每个图标查两遍表（先计数再取值）。

**改动**（`tools/Db.java` 新增、`tools/Tools.java`、`model/abstracts/Main.java`）
- 新增 `Db`：按数据库 URL 持有**单例复用连接**，结束时 `Db.shutdown()` 统一关闭。
- `getText` / `tryGetRawText` / `getImage` / `getImageIgnoringCapital` 加 `ConcurrentHashMap` 缓存（含空结果，避免重复未命中查询），并修复 statement 泄漏。
- `Main.load()` 给每个模型加 `[TIME]` 计时，便于定位热点。

> 注：各 `Model.load()` 自身的连接只有 ~29 次，非瓶颈，未改；其 N+1 子查询的优化属"后续计划"。

## 优化二：init 阶段（事务化 + PRAGMA + 日志改造）—— 大头

**问题**：`Init.loadDLCs()` 两个循环耗时近 3 小时。
- **iga 循环**：`DataBaseLoader.loadAsData()` 在 **autocommit** 下逐条 `statement.execute()` 写库。全代码库无任何事务/PRAGMA → 每条写都是独立事务、各自 fsync 落盘。这是 SQLite 经典的"不用事务批量写 → 慢上百倍"。
- **fea 循环**：真正的写操作早被注释，耗时来自**每文件一条 `System.out.println` + `logProcessedFile()` 每次新开关一个文件流**（IDE 中 println 尤其慢）。

**改动**（`load/Init.java`、`load/DataBaseLoader.java`）
- **事务化 + PRAGMA**：写连接开 `PRAGMA journal_mode=MEMORY` + `synchronous=OFF`、`setAutoCommit(false)`，**每个 request 提交一次**，结束统一提交+关闭。`addTables` / `initFix` 同样处理。（数据库是从游戏缓存拷贝的可重建临时文件，故可用激进 PRAGMA。）
- **修连接泄漏**：原 `addTables`/`initFix` 只关 statement 不关 connection——在事务模式下会因未提交连接持锁阻塞其它写，已统一 `commit + close`。
- **日志改造**：`logProcessedFile` 默认关闭；海量 per-file/row/table 的 `println`（含调用昂贵 `nodeToString` 的过滤日志）gate 到 `-Dhd.verbose` 后，仅保留 request 进度条。
- 附带修复：`initFix` 的 SQL 路径由失效的 `fix/` 改回 `scripts/fix/`（之前静默失败）。

## 优化三：又一轮，190s → 90s（2026-08-14）

这一轮的方法：**先量，再改**。给 `Page.convertAll` 的每个阶段和 `writeAll` 加 `[TIME]`，
结果和事前的猜测完全不同——roadmap 里排第一的「批量查询消除 N+1」根本不是瓶颈，
`Model.load()` 里最慢的 `Civilization` 有 8.6s 是**第一次图标查找触发的目录遍历**，
和 SQL 无关。

| 改动 | 位置 | 收益 |
|---|---|---|
| **copyFiles 的缓冲区 64 字节 → 64 KB** | `view/Page.java` | 一轮要copy ~190 MB（json 进 temp、icons 进 output_android、manual 图片进两边），几乎全是 syscall 开销。三个 copy 阶段 35.7s → 8.6s |
| **atlas 解码结果缓存**（LRU，按像素数限额 ~192 MB） | `tools/ImageEditor.java` | 切一个图标原本要把整张 `.dds` 读进来解码一遍，而几百个图标共用一张图。2379 次切图只解码 334 次，图集解码降到 1.5s |
| **切图改一次 `setRGB` 批量拷贝** | 同上 | 原本 w×h 次逐像素 `setRGB`；offset/scansize 正好能原地定位图块 |
| **PNG 编码延后 + 并行** | 同上 | `saveImage` 只入队，`flushImages()` 在 `load()` / `initIcons()` 末尾并行编码。13.1s → 2.0s。安全性前提：`decodeIcon` 只读 `.dds`，加载期没有任何东西回读 `output/icons` |
| **`.dds` 文件名索引** | `tools/DdsFolders.java` | 原本每次图标查找都问 ~110 个目录「你有这个文件吗」，几千次。遍历时顺手建 `文件名→路径` 索引，**「最后命中者胜」的覆盖优先级完全保留** |
| **遍历改 `Files.walkFileTree`** | 同上 | `File.isDirectory()` 每个条目一次 stat；walkFileTree 由目录列举直接给出类型。**8.6s → 0.7s**，遍历顺序不变所以优先级不变 |
| **章节索引内存聚合** | `model/abstracts/Writable.java` | 每个对象原本把本章 `contents.json` 读回来、加自己、再整个写出去——2400 个对象 × 几百 KB 是平方级。改成内存累积、每语言 `flushContents()` 落盘一次。23s → 15.4s |
| **每页新建 `TransformerFactory` → 静态复用**；`replaceAll` → `replace`；`mkdirs()` 建到文件路径上 → 建到父目录 | `view/Page.java` | 渲染 34.7s → 27.6s。（那个 `mkdirs()` 原本在页面路径上建目录，再删掉，每页两次多余的文件系统操作） |

**验收：产物逐字节不变。** 18132 个文件（`output/` + `output_android/` + `json/`）算 sha1
做成清单，每改一步比一次，全程 0 差异；`Main audit` 十一项指标也一项没动。
其中「`.dds` 索引」和「延后并行写 PNG」两条直接决定图标内容，是靠这个清单才敢改的。

| 阶段 | 优化前 | 优化后 |
|---|---|---|
| `load()` | 74.3s | 11.6s |
| 写 json（两种语言） | ~23s | 16.3s |
| `page` 渲染前的文件搬运 | 35.7s | 8.6s |
| `page` 渲染 | 34.7s | 28.6s |
| **`after_init` 整轮** | **~190s** | **90s**（其中 15s 是新增的产物归档） |

**没做的**：多线程渲染和多线程写 json 是剩下的大头（28.6s + 16.3s，页面之间互相独立，
8 核理论上能压到个位数）。没做是因为 `Tools` 里那些 `HashMap` 缓存、`Trait.sort` 之类的
共享可变状态需要先审一遍——并发下这些东西的错法是**产物随机少东西**，正好是本项目最怕的失效方式。
要做就单独做一轮，先审共享状态，再用上面那份 sha1 清单验收。

## 战绩
| 指标 | 优化前 | 优化后 |
|---|---|---|
| init `loadDLCs` | ~3 小时 | ~31s |
| 整轮 | ~3 小时+ | **90s**（见优化三） |

## 验证
事务化只改提交时机、不改 SQL 与顺序；缓存是纯函数。已验证：init 关键表行数正常（建筑 381 / 科技 104 / 本地化 4万+），产物 `json/` 抽样追到 DB 逐字一致、无空文件、无残留未翻译标记。**输出未改变。**

## 不做 GPU 的理由
唯一沾边的是 DDS 解码（`DDSReader` 纯 Java 逐像素），但 ~1600 张小图（~128px）+ Java 走 JNI/JCUDA 的 kernel 启动开销 > 收益。要再快应先上多线程，或用 Rust/Go 重写关键路径，而非在 Java 里硬塞 GPU。
