# HD Civilopedia

*[English](README.md)*

文明 VI 模组「和而不同 / Harmony in Diversity」的百科（Civilopedia）生成工具。

读取游戏的 SQLite 数据库与本地化文本，加载全部 DLC 与 mod 的改动，把每个实体渲染成
互相链接的 HTML 页面——约 4900 页、双语、桌面版与手机版各一套。产物即
[civ6hd.com](https://civ6hd.com) 上的在线百科。

```
init → icons → load → write → page
建库   切图标   读库    写 JSON  渲染 HTML
```

## 来源与分工

本项目 fork 自 **[xiaoxiaoccat/hdcivilopedia](https://gitee.com/xiaoxiaoccat/hdcivilopedia)**，
分叉点为上游 `a0d842a`（`v1.3.8`，2023-07-13）。

**生成流水线、30 余个实体模型的加载与渲染、DDS 解码、changelog 的 `.txt` DSL 与解析器、
双语与手机版 HTML 输出，均为上游作者 xiaoxiao 的工作。** 本仓库自 2024-09 起接手维护。

> 本仓库的 git 历史不含上游那 13 个提交（2026-05-29 做过一次剔除大文件的 clean import），
> 因此 `git log` 的作者信息不足以区分两者的贡献。
> **[docs/upstream-differences.md](docs/upstream-differences.md) 补上了这条溯源链**，
> 逐项列出改了什么，并给出自行复核 diff 的命令。

fork 之后的主要改动：

| | |
|---|---|
| 性能 | 一轮生成 ~3 小时 → **90 秒**，产物逐字节不变 |
| 搜索 | 全新：构建期索引 + 拼音（全拼/首字母/音节边界）+ 分档排序 |
| 历史背景 | 全新：维基百科 / 百度百科抓取，文明 59/59、领袖 95/95 |
| 产物体检 | 全新：`Main audit` 11 项指标对比基线，静默缺失变成可见数字 |
| 产物归档 | 全新：每轮按日归档，为 changelog 从产物 diff 反推做准备 |
| 图标 | 修复失效路径与选行 bug，搜索结果无图标 41% → 26% |
| 手机版 | 清掉 1570 个孤儿页（198 → 163 MB），侧栏抽离为独立脚本 |
| 工程化 | 配置外部化、单元测试 + CI、5 篇工程笔记（含逆向 `.blp` 的失败路径记录） |

## 运行需要什么

- **JDK**（目标字节码 Java 8，用 JDK 8 / 17 / 22 都能编）+ **Maven**
- **一份装好的文明 VI**，含 HD mod 与其依赖的第三方 mod——生成器读的是游戏自己的数据库和
  贴图，仓库里没有、也不会有这些数据（约 300 MB 的 `*.sqlite` 与美术资源均不入版本管理）

因此**这个仓库无法脱离游戏运行**，CI 也只跑不依赖游戏数据的那部分逻辑。

## 快速开始

```bash
cp config.example.properties config.properties   # 改成本机路径；不建也能跑，用环境推导的默认值
mvn compile
```

入口类 `model.abstracts.Main`，第一个参数是命令：

| 命令 | 作用 |
|---|---|
| `init` | 从游戏 Cache 重建数据库到 `database/` |
| `icons` | 解码 DDS、切出 `output/icons/*.png` |
| `changelog` | 加载内容并写出 `json/` |
| `page` | `json/` 渲染为 `output/` 与 `output_android/`，结束时自动跑 audit 与归档 |
| `after_init` | 串联上面几步，整轮约 90 秒 |
| `wiki [refresh\|N]` | 抓取/刷新条目的历史背景 |
| `audit [save]` | 产物体检，`save` 更新基线 |
| `archive` | 手动归档一次当前产物 |

无参数运行则一次性跑完全流程。更多细节见 [docs/run.md](docs/run.md)。

## 测试

```bash
mvn test                     # 纯逻辑单元测试（拼音表、归档、wiki 名称处理、audit 计数）
node scripts/test_search.js  # 前端搜索排序与拼音匹配
```

CI（`.github/workflows/ci.yml`）跑的就是这两条，JDK 8 / 17 两条腿。**产物层面的验收在本地**：
`Main audit` 每轮自动对比 `manual/audit-baseline.json`；重构要证明「产物没变」时，
用 `scripts/hash_artifacts.py` 对 18000 多个产物文件出 sha1 清单，改前改后比对。

## 文档

| | |
|---|---|
| [run.md](docs/run.md) | 构建、命令、流水线细节 |
| [known-issues.md](docs/known-issues.md) | 已知问题，以及**怎么检测**每一类静默失败 |
| [performance.md](docs/performance.md) | 3 小时 → 90 秒的两轮优化，及为什么先量再改 |
| [roadmap.md](docs/roadmap.md) | 后续计划，含 changelog 系统重建方案 |
| [upstream-differences.md](docs/upstream-differences.md) | 与上游的逐项差异与复核方法 |
| [blp-format.md](docs/blp-format.md) | `.blp` 纹理包逆向记录，**含四条已排除的假设** |
| [missing-atlas-art.md](docs/missing-atlas-art.md) | 缺失图集素材清单（按 mod 作者分组） |

工程上有一条贯穿的原则，值得先读 `known-issues.md`：**这个生成器每一层都 `catch` 住异常继续跑，
所以缺陷不会让构建失败，只会让产物静默地少东西。**不要靠日志判断严重程度——那次图标问题
日志里只有 4 条警告，实际影响 256 个标签。要从产物反查。

## 授权

⚠️ **上游仓库未声明许可证，因此本仓库尚未公开发布，也还不能添加自己的 LICENSE。**
按默认版权规则，上游作者保留全部权利，公开分发衍生作品需要其授权。
详见 [upstream-differences.md 的授权状态一节](docs/upstream-differences.md#授权状态开源前必须先解决)
与 [NOTICE](NOTICE)。

`manual/wiki/` 是条目首段的抓取缓存，内容来自维基百科，按 CC BY-SA 3.0 使用：每个 json 记录
原文 URL，产物页面逐条标注来源并链接回原文。唯一一条来自百度百科的缓存没有可再分发的许可，
**不入版本管理**，由使用者自行抓取——见 [manual/wiki/README.md](manual/wiki/README.md)。

游戏数据与美术资源属于 Firaxis Games / 2K Games 及各 mod 作者，从未进入本仓库，也不随之分发。
