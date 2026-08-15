# 运行教程

HD Civilopedia 生成工具的构建与运行说明。入口类 `model.abstracts.Main`。

## 1. 构建

需要 JDK（项目目标 Java 8，用 JDK 17/22 经 Maven 编译亦可）。

```
mvn compile          # 仅编译（命令行）
```

或在 IDE 里编译。目标字节码是 Java 8，用 JDK 8 / 17 / 22 都能编（CI 跑 8 和 17）。

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

## 5. 打包 APK

安卓版是一个 WebView 壳，把 `output_android/` 整个塞进 `assets/`。壳工程在
`E:\MyApplication`（不在本仓库里）。跑完 `after_init` 后：

```
powershell -ExecutionPolicy Bypass -File scripts\build_apk.ps1
```

脚本做四件事：`robocopy /MIR` 把 `output_android/` 镜像到
`app\src\main\assets`（`/MIR` 会删掉上一版留下的孤儿页面），`gradlew assembleRelease`，
用 `apksigner` 验一遍签名，然后把包复制进本次运行的归档快照。归档副本是
`<archive.folder>\HDCivilopedia_<yyyyMMdd>\HDCivilopedia_<yyyyMMdd>.apk`，沿用手工归档
一直以来的命名（apk 与所在文件夹同名）。`Archive` 重跑同一天时只删自己写的 `json/` 和两个
zip，不会碰这个 apk。壳工程路径不同时用 `-AndroidProject <路径>` 覆盖。

**版本号**取本次归档快照的日期：脚本把它作为 `-PhdVersion=20260815` 传给 Gradle，
`versionCode` 和 `versionName` 都用它，产物叫 `release-20260815.apk`。于是版本号、
tag、快照文件夹、apk 文件名全是同一个日期，拿到一个包就能反查是哪次运行打的。

**签名**配置在 `app/build.gradle`，从 `E:\keys\keystore.properties` 读
`storeFile`/`storePassword`/`keyAlias`/`keyPassword`。该文件在所有 checkout 之外，不进
版本库；**丢了就再也发不出能原地升级的新版本**，务必离线备份 `.jks` 和口令。没有这个文件
时 release 变体保持未签名而不是配置失败（这样没有密钥的机器也能编译），但脚本的
`apksigner` 检查会拦下来——未签名的包装不到任何设备上，不能靠日志绿灯当数。

`applicationId` 是 `com.civ6hd.pedia`。`namespace` 仍是模板留下的
`com.example.myapplication`，那只是源码和 R 类所在的 Java 包，不构成对外身份，改它要连带
移动源码目录，没必要。

验证包里确实是新产物，最省事的是按路径比字节：把 APK 当 zip 打开，
`assets/<lang>/...` 与 `output_android/<lang>/...` 的 sha1 应当一致。

## 6. 正确性验证

最省事的方式是 `Main audit`（约 1 秒）：它对比 `manual/audit-baseline.json`，
只在指标往坏的方向动时标 `<== WORSE`。改动前后各跑一次；确认改好了就 `Main audit save`
更新基线。各指标含义见 [known-issues.md](known-issues.md)。


性能优化不应改变产物。验证方式：
- DB 层抽查：`sqlite3 database/DebugGameplay.sqlite "select count(*) from Buildings;"` 等关键表行数是否合理（建筑数百、本地化数万）。
- 产物层：跑 `changelog`+`page` 后抽查 `json/` / `output/` 几个条目（标题、图标、链接、含 `[]`/`{}` 的动态文本是否正常）；如有上一版产物备份，`diff -r` 比对应无差异。
