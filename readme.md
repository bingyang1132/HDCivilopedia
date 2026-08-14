# HD Civilopedia

文明 VI 模组「和而不同 / Harmony in Diversity」的文明百科（Civilopedia）数据生成工具。

读取游戏的 SQLite 数据库与本地化文本，生成各分类的手册 JSON，并渲染为 HTML / 图片。
产物即 [civ6hd.com](https://civ6hd.com) 上的在线百科。

## 来源与分工

本项目 fork 自 **[xiaoxiaoccat/hdcivilopedia](https://gitee.com/xiaoxiaoccat/hdcivilopedia)**，
分叉点为上游 `a0d842a`（`v1.3.8`，2023-07-13）。

**生成流水线、30 余个实体模型的加载与渲染、DDS 解码、changelog 的 `.txt` DSL 与解析器、
双语与手机版 HTML 输出，均为上游作者 xiaoxiao 的工作。** 本仓库自 2024-09 起接手维护。

> 本仓库的 git 历史不含上游那 13 个提交（2026-05-29 做过一次剔除大文件的 clean import），
> 因此 `git log` 显示的作者信息不足以区分两者的贡献。
> **[docs/upstream-differences.md](docs/upstream-differences.md) 补上了这条溯源链**，
> 并给出自行复核 diff 的命令。

fork 之后的主要改动（详见上文那份文档）：

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

> **授权**：上游仓库未声明许可证，因此本仓库尚未公开发布。见
> [docs/upstream-differences.md 的授权状态一节](docs/upstream-differences.md#授权状态开源前必须先解决)。

## 技术栈

- Java 8 + Maven
- 依赖：`sqlite-jdbc`、`fastjson`、`Apache POI`（见 `pom.xml`）
- 入口类：`model.abstracts.Main`

## 目录结构

```
src/main/java/        核心源码（model/ 实体、load/ 数据加载、view/ 渲染、changelog/、tools/）
manual/               手册产物（json + output 图片，旧流程需手动放入，纳入版本管理）
scripts/              辅助脚本（find_icons/ 图标查找、fix/ 数据修复 SQL）
docs/                 开发笔记（run.md 运行说明、questions.md、notes.md）
pom.xml               Maven 构建配置
```

> 运行所需的游戏数据（`*.sqlite`、`*.xlsx`、`backup/`）与生成产物（`output/`、`json/`、`database/`、`icons/` 等）不纳入版本管理，详见 `.gitignore`。

## 构建

```
mvn package
```

## 运行

入口为 `model.abstracts.Main`，第一个参数为命令。典型执行顺序：

```
init        # 初始化数据
icons       # 处理图标
changelog   # 加载内容并写出 JSON
page        # 将 JSON 渲染为 HTML
```

其它命令：`build <version> [output]` 生成更新日志、`wiki [refresh|N]` 抓取历史背景、
`archive` 归档当次产物（`page` / `after_init` 结束时会自动跑）、`audit [save]` 产物体检、
`after_init` 串联完整流程。无参数运行则一次性跑完全流程。

更多运行细节见 [docs/run.md](docs/run.md)。

## 测试

```
mvn test                    # 纯逻辑单元测试（拼音表、归档、wiki 名称处理、audit 计数）
node scripts/test_search.js # 前端搜索排序与拼音匹配
```

CI（`.github/workflows/ci.yml`）跑的就是这两条。**CI 跑不了百科本身**——那需要游戏的 SQLite
数据库（304 MB，不入库）、Steam 库和 mod 目录。产物层面的验收留在本地：`Main audit`
在每轮 `page` / `after_init` 结束时自动对比 `manual/audit-baseline.json`。
