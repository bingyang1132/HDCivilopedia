# 性能优化记录

记录把一轮生成从 **~3 小时降到 ~180 秒** 的过程与原理。核心结论：瓶颈全是**架构性 I/O 浪费，不是算力**——没有用 GPU、没动业务逻辑、产物不变。

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

## 战绩
| 指标 | 优化前 | 优化后 |
|---|---|---|
| init `loadDLCs` | ~3 小时 | ~31s |
| 整轮 | ~3 小时+ | ~180s |

## 验证
事务化只改提交时机、不改 SQL 与顺序；缓存是纯函数。已验证：init 关键表行数正常（建筑 381 / 科技 104 / 本地化 4万+），产物 `json/` 抽样追到 DB 逐字一致、无空文件、无残留未翻译标记。**输出未改变。**

## 不做 GPU 的理由
唯一沾边的是 DDS 解码（`DDSReader` 纯 Java 逐像素），但 ~1600 张小图（~128px）+ Java 走 JNI/JCUDA 的 kernel 启动开销 > 收益。要再快应先上多线程，或用 Rust/Go 重写关键路径，而非在 Java 里硬塞 GPU。
