# 后续计划 / Roadmap

已发现但未修的缺陷记在 [known-issues.md](known-issues.md)，那里同时记了**怎么检测**每一类静默失败
——这个生成器不会因为出错而构建失败，只会少东西。改动前后跑 `Main audit` 对比基线。

一轮耗时参考：`page` ~130s，`after_init` ~190s（其中 `load()` 74s）。

---

## 建议优先做

### 1. 搜索：目前只是「标题子串匹配」

`search.js` 用的是 `indexOf`，不是模糊匹配；索引条目只有 `{t 标题, c 分类, u 链接, i 图标}`。
所以现状是：

- **没有拼音**。中文标题里只有 20/2400 条含拉丁字母，中文用户打 `jichang` 搜不到「机场」。
  主要受众是中文用户，这条的性价比最高。
- **只搜标题**。描述、`LOC_` 标签、分类名都不参与匹配——搜「航空」找不到机场。
- **不容错**。打错一个字、词序颠倒就没结果。
- **不排序**。取索引顺序的前 30 条，前缀命中不会排在中间命中前面。
- **每页都加载 462 KB 索引**（zh，en 是 406 KB）。近万个页面各拉一次，手机端尤其值得算笔账。

改法建议按性价比：先加拼音（构建期给每条中文标题预生成全拼+首字母，塞进索引）→ 再做前缀优先的排序
→ 再考虑扩到标签/描述。真要做容错再上 Fuse.js/lunr.js，不必一步到位。

### 2. 基础单元测试 + CI

编译 + 冒烟（`-Dhd.limit`）+ `Main audit`。audit 已经能给出「产物没变」的判据，接上 CI 就是回归保护。

---

## 以后再说

### 图标缺口（**已冻结**）

还有约 507 个标签没有图标，素材只存在于 `.blp` 包里。三条路都堵住了：

1. **逆向 `.blp`** —— 卡住，四条假设全排除，详见 [blp-format.md](blp-format.md)（别重试那四条）。
2. **向作者要源图** —— 2026-08-08 XHH 反馈：**美术资源作者一般不会给**。清单
   [missing-atlas-art.md](missing-atlas-art.md) 留着，但不指望走通。
3. **游戏内截图** —— XHH 提的思路，也是目前唯一还成立的：写一个**程序化截图工具**，
   在游戏内百科页面里逐条截图，理想情况用 **Lua 控制百科翻页**。这是社区实际在用的路子
   （Sukritact's Texture Extractor 同源），但工作量大，暂不启动。

素材若哪天到位：丢进 `mods.folder` 或 Steam 库任意位置（现在会自动扫到）→ 重跑 `after_init`
→ 看 `searchEntriesNoIcon` 有没有降。

### 性能

**当前不是瓶颈，建议排在功能之后**：日常迭代只需要 `page`（130s），整轮 190s 也不算难受。
而且这些改动动的正是 `Model.load()`，和功能开发撞车；`Main audit` 现在能自动验收「产物不变」，
真要做的时候有工具了。

- **批量查询消除 N+1**（中风险、收益大）：`Building.load()` 对每个建筑发 14 条子查询，
  Unit/Technology/Civilization 同类。改为每张子表一次性 `select *`，内存里按外键分组装配。
  以 Building 为样板验证产物一致后再推广。
- **多线程**：`linkData` 之前的各 `Model.load()` 相互独立，`write()`、`initIcons()` 同理。
  注意 SQLite `Connection` 非线程安全 → 每线程独立连接，`Db` 改 ThreadLocal。
- **`PreparedStatement`** 替换字符串拼接（性能 + 防注入）。
- **写 JSON 聚合**：`Writable.writeJSON()` 每个对象都重读+重写 `contents.json`，可改内存聚合最后落盘。
- **HTML 渲染**：`Page` 用 DOM+Transformer 序列化上千文件，可评估改 `StringBuilder` 或模板。

### 其它工程化

- 升级 `maven-shade-plugin`（1.2.1 → 3.x），让 `mvn package` 在现代 JDK 可打 fat jar。
- **增量构建**：按内容 hash 跳过未变对象。
- `manual/output` 的科技/市政大图目前是外部预渲染手工放入，可评估在代码内生成。

---

## 已完成

- **性能**：连接复用 + 缓存、`init` 事务化 + PRAGMA + 日志改造。整轮从 ~3 小时降到分钟级，
  详见 [performance.md](performance.md)。
- **历史背景**：走 Wikipedia 缓存（`Main wiki` → `manual/wiki/`），详见 memory 里的说明。
- **搜索（基础版）**：构建期产 `output/{lang}/search-data.js`，前端 `search.js` 做标题子串匹配。
  ——见上面「建议优先做」第 1 条，还有较大改进空间。
- **配置外部化**：四个机器相关路径进 `config.properties`（模板 `config.example.properties`），
  83 条硬编码贴图目录换成扫描根目录自动发现——不会再因 mod 改名而失效。产物逐项不变，
  且 `load()` 从 122s 降到 74s（每次图标查找少 stat 几十个目录）。
- **产物 audit**：`view/Audit.java`，`page`/`after_init` 后自动跑，对比 `manual/audit-baseline.json`
  并标出变差的指标。
- **手机版侧栏**：抽成 `output_android/{lang}/sidebar.js`（原本内联进每一页），加了首页入口和
  点外部/返回键关闭。顺带清掉 1570 个孤儿页面，手机版 198 MB → 163 MB。
- **图标解析修复**：mod 目录改名导致的失效路径、`Tools.getImage` 只赌一行的 bug。
  搜索结果无图标比例 41% → 26%。
