# 运行教程

HD Civilopedia 生成工具的构建与运行说明。入口类 `model.abstracts.Main`。

## 1. 构建

需要 JDK（项目目标 Java 8，用 JDK 17/22 经 Maven 编译亦可）。

```
mvn compile          # 仅编译（命令行）
```

或在 VSCode 里用 Java 扩展自动编译。运行时实际用 JRE 8 跑（见 `.vscode/settings.json` 的 `JavaSE-1.8` 运行时配置）。

> **换机器**：复制 `config.example.properties` 为 `config.properties`，改里面四个路径即可
> （Steam 库、Mods 目录、HD mod 的文件夹名、游戏 Cache 目录）。其余全部派生，贴图目录靠
> 扫描这些根发现，不需要维护路径清单。不建 `config.properties` 就用 `tools/Config.java`
> 里的默认值，即原来硬编码的那套。

## 2. 流水线与命令

`Main` 第一个参数是命令，整体顺序：

| 命令 | 作用 | 输入 → 输出 |
|---|---|---|
| `init` | 从游戏缓存重建数据库 | 游戏 Cache → `database/*.sqlite` |
| `icons` | 解码 DDS、切图标 | `database/` + 游戏贴图 → `output/icons/*.png` |
| `changelog` | 加载内容并写 JSON | `database/` → `json/` |
| `page` | JSON 渲染为 HTML | `json/` → `output/` |
| `build <version> [out]` | 生成更新日志 JSON | Changelog 文本 → `manual/json/.../updates/` |
| `audit [save]` | 产物体检（`page`/`after_init` 后自动跑） | `json/` + `output*/` → 报告 |

无参运行：执行 `icons → load → write → page` 全流程（默认**跳过 init**，复用已建好的 `database/`）。需要重建数据库时单独先跑一次 `init`。

运行示例（VSCode 编译后用 JRE 8 跑 classpath，参考你环境里的临时 classpath jar）：
```
java -cp <classpath> model.abstracts.Main init
java -cp <classpath> model.abstracts.Main          # 无参 = 全流程(跳过init)
```

## 3. 快速迭代（不要每次都跑 init）

`init` 把游戏数据建进 `database/` 后会持久保留。调试 `load`/`write`/`page` 时**跳过 init**，直接反复跑 `changelog` / `page` 即可。只有游戏数据/Mod 变化、需要重建 `database/` 时才重跑 `init`。

## 4. 性能开关 / 小规模测试

通过系统属性 `-Dxxx`（或同名大写环境变量）控制：

- `-Dhd.verbose=true`（或 `HD_VERBOSE`）：恢复每文件/每行的详细日志。**默认关闭**——关闭后只打每个 request 的进度条，可大幅提速 init。
- `-Dhd.limit=N`（或 `HD_LIMIT`）：init 的 `loadDLCs` 只处理前 N 个 request，用于几分钟内跑通做冒烟测试。

各阶段会打印 `[TIME] <阶段> <秒>`：
- init：`copyDatabases / addTables / loadDLCs / initFix / writeErrors`，以及 `loadDLCs` 内的 `fea` / `iga` 两循环。
- load：每个模型（Era / Building / Technology …）+ `== load() total`。
- 全程：`== TOTAL`。

冒烟示例（限 50 个 request + 详细日志）：
```
java -Dhd.limit=50 -Dhd.verbose=true -cp <classpath> model.abstracts.Main init
```

## 5. 正确性验证

最省事的方式是 `Main audit`（约 1 秒）：它对比 `manual/audit-baseline.json`，
只在指标往坏的方向动时标 `<== WORSE`。改动前后各跑一次；确认改好了就 `Main audit save`
更新基线。各指标含义见 [known-issues.md](known-issues.md)。


性能优化不应改变产物。验证方式：
- DB 层抽查：`sqlite3 database/DebugGameplay.sqlite "select count(*) from Buildings;"` 等关键表行数是否合理（建筑数百、本地化数万）。
- 产物层：跑 `changelog`+`page` 后抽查 `json/` / `output/` 几个条目（标题、图标、链接、含 `[]`/`{}` 的动态文本是否正常）；如有上一版产物备份，`diff -r` 比对应无差异。
