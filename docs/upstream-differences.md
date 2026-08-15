# 与上游的差异

本项目是 [xiaoxiaoccat/hdcivilopedia](https://gitee.com/xiaoxiaoccat/hdcivilopedia) 的 fork。
**生成器的整体架构、数据模型层、changelog DSL、DDS 解码、双语与手机版输出，都是上游作者
xiaoxiao 的工作**，本文只说清 fork 之后改了什么，以及每条改动怎么核对。

## 基线

| | |
|---|---|
| 上游仓库 | https://gitee.com/xiaoxiaoccat/hdcivilopedia |
| 上游提交 | 13 个，2022-03-18 ~ 2023-07-13 |
| 分叉点 | `a0d842a`（提交信息 `v1.3.8`，2023-07-13），也是上游至今的最新提交 |
| 本仓库 | 2024-09-08 起，至 2026-08-14 |

⚠️ **本仓库的 git log 里看不到上游的 13 个提交**：2026-05-29 做过一次 `Initial clean import`，
把数百 MB 的游戏数据和二进制从历史里剔掉，代价是丢掉了上游的提交记录。
所以 `git log` 显示全部提交都属于本仓库作者，**这不代表全部代码都是**。
本文的作用就是补上这条断掉的溯源链。

### 自己复核的办法

```bash
git clone https://gitee.com/xiaoxiaoccat/hdcivilopedia upstream
git -C upstream log --oneline            # 应看到 13 个提交，最新为 v1.3.8
cd <本仓库>
git fetch ../upstream master:refs/upstream/master
git diff -w --ignore-blank-lines --stat refs/upstream/master HEAD
```

**必须带 `-w`。** 导入时整个代码库做过 CRLF → LF 转换，不忽略空白的话 diff 会把
没动过的文件整篇算成改动——例如 `model/District.java` 裸 diff 是 `+466 / -466`，
实际改动是 **0**。下面所有数字都是忽略空白后的。

## 数字

```
忽略空白后：1106 个文件，+30517 / -497
（裸 diff 是 +36549 / -6529，其中约 6000 行的一增一减是行尾转换，不算改动）
```

拆开看，**手写的代码与文档约 6300 行**，其余是生成的数据：

| | 行数 | 说明 |
|---|---|---|
| `src/main/java` | +3869 / -375 | 31 个文件，其中 8 个新类。上游 42 文件 13584 行 → 50 文件 17077 行 |
| `src/test/java` | +366 | 上游无测试 |
| `scripts/` | +800 | 拼音表生成、产物 sha1 清单、搜索前端测试 |
| `docs/` | +1273 | 5 篇工程笔记 |
| `manual/pinyin-chars.tsv` | +20925 | **生成的数据表**（全 CJK 区读音），不是手写 |
| `manual/wiki/` | +1376 | **抓取缓存**，972 个 json，每个一行 |
| `manual/` 其它 | ~+1700 | 手机版侧栏/搜索前端资源、audit 基线等 |

新增的 8 个类：`tools/Config`（配置外部化）、`tools/Db`（连接复用）、`tools/DdsFolders`
（贴图发现）、`tools/WikiFetcher`（历史背景抓取）、`tools/Pinyin`、`tools/Archive`（产物归档）、
`view/Audit`（产物体检）、`model/Commemoration_CN`（内容）。

## 上游已有的（不属于本 fork）

写清这部分同样重要：

- **整个生成流水线**：`init`(建库) → `icons` → `load` → `write` → `page`，以及 `Main` 的命令分发
- **数据模型层**：`model/` 下 30 余个实体类的加载与 `toJson`，这是工作量最大的一块
- **DDS 解码**（`tools/DDSReader`）与图集切图的原始实现
- **changelog 的 `.txt` DSL 与 691 行解析器**（`changelog/`），含 `[]` 实体链接语法
- **HTML 渲染**（`view/Page` 的 DOM 构建）、**双语框架**、以及**手机版 HTML 输出**
  （`convertAndroidChapter` / `convertAndroidSingleHTML` 在 v1.3.8 里就有）
- 手工维护的 `manual/json`、图片资源与 CSS

## 本 fork 的改动

### 1. 性能：一轮 ~3 小时 → 90 秒

详见 [performance.md](performance.md)。两轮：

- 第一轮（连接复用 + 缓存 + `init` 事务化 + PRAGMA）：`loadDLCs` 从近 3 小时降到 31s。
  根因是 SQLite 在 autocommit 下逐条写，每条一个事务各自 fsync
- 第二轮（2026-08）：190s → 90s。先加阶段计时，结果推翻了自己事前的优先级判断——
  `Model.load()` 里最慢的 `Civilization` 有 8.6s 是首次图标查找触发的目录遍历，和 SQL 无关；
  而没人怀疑的 64 字节拷贝缓冲区一个人占了 35.7s

验收方式：18132 个产物文件的 sha1 清单（`scripts/hash_artifacts.py`），改前改后比对，全程 0 差异。

### 2. 搜索（全新）

上游没有搜索（v1.3.8 里没有 `buildSearchIndex` / `SEARCH_INDEX` / `search-data`）。

- 构建期产 `output/{lang}/search-data.js`，前端 `search.js` 做下拉匹配
- **拼音**：`tools/Pinyin` 把中文标题转成空格分隔的音节存进索引。中文标题里只有 20/2400 条
  含拉丁字母，没有拼音等于中文用户打 `jichang` 搜不到「机场」
- 音节边界支撑三种查询：全拼、首字母 `jc`、从任一音节起匹配（`jichang` 也能找到「国际机场」，
  而 `ang` 不命中音节中段）
- 多音字词表让「银行」不读成 yinxing、「音乐」不读成 yinle
- 六档排序：标题精确 → 标题前缀 → 标题子串 → 全拼前缀 → 首字母 → 后续音节

### 3. 历史背景走百科抓取（全新）

`tools/WikiFetcher` + `manual/wiki/` 缓存。文明 59/59、领袖 en 95/95 zh 94/95 有了真正的
历史背景，而不是游戏自带的一两句话。其中：`zh.wikipedia` 要用 `Accept-Language: zh-cn`
而不是 `zh-hans`（后者只转字符，会留下「鄂图曼帝国」这类台湾用词）；中国条目用百度百科，
因为百度的 WAF 按 TLS 指纹拦 Java，只能 `PIN` 住手工维护。

### 4. 产物体检与归档（全新）

- `view/Audit`：每轮结束自动跑，11 项指标对比 `manual/audit-baseline.json`，只在变差时报警。
  动机见 [known-issues.md](known-issues.md)——这个生成器每层都 `catch` 住继续跑，
  缺陷不会让构建失败，只会让产物静默缺东西
- `tools/Archive`：每轮按日归档 `json/` + 压缩的 HTML + `manifest.json`（记生成器 commit），
  为的是 changelog 能从两个快照的 diff 反推

### 5. 图标解析修复

搜索结果无图标比例 **41% → 26%**。两个成因：`Constants` 里 83 条硬编码贴图路径有 31 条已失效
（mod 文件夹改过名），整个图集找不到；`Tools.getImage` 先数精确大小写匹配的行数、再拿这个数字
当偏移去跳忽略大小写的结果集，等于随机锁定一行。前者换成扫描配置的根目录自动发现，不会再失效。

### 6. 手机版：清孤儿页 + 侧栏抽离

手机版输出本身是上游的。本 fork 做的是：`Page.convertAll` 清了 `output` 却没清 `output_android`，
mod 改名/删除的内容对应页面永久堆积，首次清扫删掉 **1570 个**死页面（198 MB → 163 MB）；
侧栏从每页内联抽成 `sidebar.js`，加了首页入口和点外部/返回键关闭。

### 7. 配置外部化

四个机器相关路径进 `config.properties`（模板 `config.example.properties`），
83 条硬编码贴图目录换成扫描根目录自动发现。换机器不再需要改源码。

### 8. 测试与 CI（全新）

`mvn test` 20 条 + `node scripts/test_search.js` 12 条，GitHub Actions 跑 JDK 8/17 + node。
**CI 跑不了百科本身**（需要 304 MB 的游戏数据库、Steam 库、mod 目录），
所以只覆盖不依赖游戏数据的纯逻辑，产物层面的验收留在本地。

### 9. 逆向 `.blp`：把失败路径当成正式产出

约 507 个标签的图标只存在于 Firaxis 的 `.blp` 纹理包里。逆向卡住了，
[blp-format.md](blp-format.md) 记的是**四条已排除的假设**——写下来的目的是别人（包括未来的自己）
不要再重试同样四条路。

### 10. 内容与数据

`Commemoration_CN`（纪念项目）、城邦专属资源进百科（+72 页）、59 个文明与 95 个领袖的简介、
`docs/missing-atlas-art.md` 的缺失素材清单。

## 授权状态

**已取得：公开发布的许可。** 2026-08 上游作者答复「同意公开」。

**仍缺：给第三方的许可证。** 上游仓库没有 LICENSE 文件，按默认版权规则上游作者保留
其代码的全部权利。所以本仓库即使公开，别人也只能看，不能用、不能改、不能再分发——
这不是通常意义上的开源，而且本仓库**不能替上游代码声明许可证**。

要补齐，只差一步：请上游作者在其仓库加一个 LICENSE（MIT / Apache-2.0 等）。
之后本仓库照同款写，并列双方版权行：

```
Copyright (c) 2022-2023 xiaoxiao
Copyright (c) 2024-2026 <本仓库维护者>
```

> 授权目前只存在于聊天记录里，**把那条答复存档**（截图或导出），
> 这是现阶段唯一的凭据。

其余与开源相关的事实：

- `manual/wiki/` 的维基部分按 CC BY-SA 3.0 保留（每条记录原文 URL、页面逐条标注来源），
  唯一一条百度百科来源的已移出版本管理，由使用者自行抓取。见 `manual/wiki/README.md`
- 游戏数据（`*.sqlite`、`Texts.xlsx`）与美术资源从未进过版本管理，这部分没有问题
