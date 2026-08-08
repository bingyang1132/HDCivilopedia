# 后续计划 / Roadmap

按优先级与性价比排列的候选方向。已发现但未修的缺陷记在 [known-issues.md](known-issues.md)，
那里同时记了**怎么检测**每一类静默失败——这个生成器不会因为出错而构建失败，只会少东西。

## 性能（继续）
已完成：连接复用 + 缓存（load/write）、init 事务化 + PRAGMA + 日志改造。后续：
- **批量查询消除 N+1**（中风险、收益大）：`Building.load()` 对每个建筑发 14 条子查询，Unit/Technology/Civilization 同类。改为每张子表一次性 `select *`（或 `WHERE x IN (...)`），在内存按外键 `Map<tag,List<Row>>` 分组装配。以 Building 为样板验证产物一致后推广。
- **PreparedStatement** 替换字符串拼接（性能 + 防注入）。
- **多线程**：linkData 之前的各 `Model.load()` 相互独立，可用 `ExecutorService` 并行；各语言 `write()`、`initIcons()` 同理。注意 SQLite `Connection` 非线程安全 → 每线程独立连接，`Db` 改 ThreadLocal。
- **写 JSON 聚合**：`Writable.writeJSON()` 目前每对象重读+重写 `contents.json`，可改内存聚合最后一次落盘。
- **HTML 渲染**：`Page` 用 DOM+Transformer 序列化上千文件，可评估改 `StringBuilder`/模板（FreeMarker）。

## 功能
- ~~**文明/领袖/城邦/伟人简介**~~ 已完成：走 Wikipedia 缓存（`Main wiki` → `manual/wiki/`）。
- ~~**搜索功能**~~ 已完成：构建期产 `output/{lang}/search-data.js`，前端 `search.js` 做模糊匹配。

## 工程化
- 升级 `maven-shade-plugin`（1.2.1 → 3.x），让 `mvn package` 在现代 JDK 可打 fat jar。
- **增量构建**：按内容 hash 跳过未变对象，避免每次全量重跑。
- **外部化 `tools/Constants.java` 里硬编码的游戏路径**（优先级已提高）：`DDS_FOLDERS` 那 83 条
  绝对路径里一度有 31 条因 mod 改名而失效，直接导致大批图标解不出来且无人察觉。
- ~~**产物 audit**~~ 已完成：`view/Audit.java`，`page`/`after_init` 后自动跑，对比
  `manual/audit-baseline.json` 并标出变差的指标。也是性能重构"产物不变"的验收工具。
- 基础单元测试 + GitHub Actions CI（编译 + 冒烟 `-Dhd.limit`）。
- `manual/output` 的科技/市政大图目前是外部预渲染手工放入，可评估在代码内生成、消除手工步骤。
