## run
先用vscode自动编译一下

最后一个词是arg
cmd /C ""C:\Program Files\Java\jre-1.8\bin\java.exe" -cp C:\Users\1132\AppData\Local\Temp\cp_echr0d65y5xfcx4ndrg8iys0j.jar model.abstracts.Main init"

cmd /C ""C:\Program Files\Java\jre-1.8\bin\java.exe" -cp C:\Users\1132\AppData\Local\Temp\cp_echr0d65y5xfcx4ndrg8iys0j.jar model.abstracts.Main "

## 顺序
init
icons
changelog
build 貌似不必要？
page

## 性能开关 / 小规模测试

运行参数（系统属性 `-Dxxx`，或同名大写环境变量）：
- `-Dhd.verbose=true`：恢复每文件/每行的详细日志（默认关闭，关闭可大幅提速 init）。默认只打每个 request 的进度条。
- `-Dhd.limit=N`：init 的 loadDLCs 只处理前 N 个 request，用于几分钟内跑通做冒烟测试。

各阶段会打印 `[TIME] <阶段> <秒>`（init 的 copyDatabases/addTables/loadDLCs/initFix，以及 loadDLCs 内 fea/iga 两循环；load() 内每个模型）。

### 快速迭代：不要每次都跑 3h 的 init
init 把游戏数据建进 `database/` 后会持久保留。调试 load/write（已优化的部分）时**跳过 init**，直接反复跑：
```
... model.abstracts.Main changelog   # 读 database/ → 生成 json
... model.abstracts.Main page        # json → html
```
只有当游戏数据/Mod 变化、需要重建 `database/` 时才重新跑一次 `init`。

示例（冒烟，限 50 个 request + 详细日志）：
```
... -Dhd.limit=50 -Dhd.verbose=true model.abstracts.Main init
```