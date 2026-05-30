# 后续计划 / Roadmap

按优先级与性价比排列的候选方向。

## 性能（继续）
已完成：连接复用 + 缓存（load/write）、init 事务化 + PRAGMA + 日志改造。后续：
- **批量查询消除 N+1**（中风险、收益大）：`Building.load()` 对每个建筑发 14 条子查询，Unit/Technology/Civilization 同类。改为每张子表一次性 `select *`（或 `WHERE x IN (...)`），在内存按外键 `Map<tag,List<Row>>` 分组装配。以 Building 为样板验证产物一致后推广。
- **PreparedStatement** 替换字符串拼接（性能 + 防注入）。
- **多线程**：linkData 之前的各 `Model.load()` 相互独立，可用 `ExecutorService` 并行；各语言 `write()`、`initIcons()` 同理。注意 SQLite `Connection` 非线程安全 → 每线程独立连接，`Db` 改 ThreadLocal。
- **写 JSON 聚合**：`Writable.writeJSON()` 目前每对象重读+重写 `contents.json`，可改内存聚合最后一次落盘。
- **HTML 渲染**：`Page` 用 DOM+Transformer 序列化上千文件，可评估改 `StringBuilder`/模板（FreeMarker）。

## 功能
- **文明/领袖/城邦/伟人简介**：推荐做一个**独立 mod 改 database**（往 `LocalizedText`/自定义表写简介），本项目只读取——零爬虫、随游戏更新、与现有 `Tools.getText()` 管线天然兼容。需更详尽资料再用离线爬 wiki 脚本（放 `scripts/`）补充。
- **搜索功能**：产物已是静态 JSON/HTML，适合**纯前端搜索**——构建期多产一份精简 `search-index.json`（title/分类/tag/关键词），前端用 Fuse.js/lunr.js 模糊搜索，零后端。`Main.WRITABLES_ZHINDEX` 可作索引数据源。

## 工程化
- 升级 `maven-shade-plugin`（1.2.1 → 3.x），让 `mvn package` 在现代 JDK 可打 fat jar。
- **增量构建**：按内容 hash 跳过未变对象，避免每次全量重跑。
- 外部化 `tools/Constants.java` 里硬编码的游戏路径到配置文件。
- 基础单元测试 + GitHub Actions CI（编译 + 冒烟 `-Dhd.limit`）。
- `manual/output` 的科技/市政大图目前是外部预渲染手工放入，可评估在代码内生成、消除手工步骤。
