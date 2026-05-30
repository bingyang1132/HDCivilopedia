# HD Civilopedia

文明 VI 模组「和而不同 / Harmony in Diversity」的文明百科（Civilopedia）数据生成工具。

读取游戏的 SQLite 数据库与本地化文本，生成各分类的手册 JSON，并渲染为 HTML / 图片。

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

其它命令：`build <version> [output]` 生成更新日志、`after_init` 串联完整流程。无参数运行则一次性跑完全流程。

更多运行细节见 [docs/run.md](docs/run.md)。
