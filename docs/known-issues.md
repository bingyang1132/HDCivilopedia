# 静默失败与已知问题

> **先跑 `Main audit`。** 它把下面这些检测都自动化了，1 秒出结果，并和
> `manual/audit-baseline.json` 里的基线对比、标出变差的指标。改完东西跑一遍，
> 确认没有指标变红；确实改好了就 `Main audit save` 更新基线。
>
> ⚠️ 本文里 2026-08-07 之前用脚本手工统计的绝对数字**偏低**。fastjson 会把重复出现的对象
> 写成 `{"$ref": "$.folders[5].files[18].iconlabel"}` 并在解析时还原，而当时的 Python 脚本
> 不跟 `$ref`，于是漏掉了所有跨目录交叉列出的条目（光 `improvements` 一个文件就有 109 处）。
> 趋势是对的，绝对值以 `Main audit` 为准。

这个生成器每一层都 `catch` 住异常继续跑，所以缺陷不会让构建失败，只会以**产物缺失或陈旧**的形式堆积。
下面记录已发现的几类问题、当时的量级、以及**怎么检测**——检测方法比结论更重要，因为同类问题还会再出现。

> 原则：**别靠日志判断严重程度。**图标那个问题日志里只有 4 条 `can't load image` 警告，
> 实际影响 256 个标签，差了六十倍。要从产物反查。

## 已修复

### 1. `output_android` 从不清理，孤儿页面永久堆积

`Page.convertAll` 清了 `output` 却没清 `output_android`，mod 改名/删除的内容对应的页面
一直留着。首次清扫删掉 **1570 个**死页面（约 15 MB），手机版 198 MB → 163 MB。

**检测**：比较两棵树的页面数。`output_android` 合理地会多约 38 个
（每语言每章节一个 `toc.html` + 首页）。差得更多就是有孤儿。

### 2. 图标解码不出来，条目静默没有图标

`iconlabel.src` 来自模型的 `icon` 字段，而该字段只在**加载期 DDS 解码成功**时才赋值
（`Building.java` 等）；`output/icons/*.png` 却是另一条批量路径写的。两条路径不同步时，
磁盘上有好好的 PNG，索引页和搜索结果里却是纯文字。

两个成因：

- `Constants.DDS_FOLDERS` 里 83 条硬编码绝对路径有 **31 条已失效**——几个 mod 文件夹改了名
  （`civ6-harmony-in-diversity` → `Civ6HarmonyInDiversity` 等），整个 atlas 找不到。
- `Tools.getImage` 先数**精确大小写**匹配的行数，再拿这个数字当偏移去跳**忽略大小写**的
  结果集，等于随机锁定一行。一个标签常有多份定义（基础游戏 + mod 重绘），mod 那份的 `.dds`
  经常不在磁盘上；一旦选中它，图标就直接消失，而不是回退到能用的那份。

修复后：无 `src` 的 iconlabel 1720 → 1048；其中磁盘已有 PNG 的 256 → 13；
搜索结果无图标比例 41% → 27%。

**检测**：遍历 `json/{lang}/{chapter}/contents.json`，找 `iconlabel` 里有 `ICON_*` 的 `alt`
但没有 `src` 的条目，再和 `output/icons/` 对照。**有 PNG 却没有 src 的一定是 bug。**
用户可见的口径看 `output/{lang}/search-data.js` 里缺 `i` 字段的条目占比。

**检测硬编码路径**：把 `Constants.java` 里的字符串字面量抓出来逐个 `isdir()`。

### 3. mod SQL 静默不入库

`init` 曾经报 70 条语法错误照常跑完（注释里的分号把语句劈成两半、`Index` 是 SQLite 关键字、
`PlayerColors` 缺 `TextColor` 列）。已修，现在 0 错误。

## 重要：`output/icons` 是缓存，不是产物

它被 `deleteFilesExcept(..., "icons")` 豁免，所以存着 mod 早已删掉的内容的图标。
**不要**以"没有被引用"为由清扫它——实测 308 个未被引用的图标里，**256 个是本该显示却没接上的**，
删掉等于把 bug 焊死。真正的孤儿不到 2 MB，不值得冒险。

`json/` 没有这个问题：每次写入前 `Main` 都调 `Page.deleteFiles(new File("json"))`。

## 未修复 / 待确认

### 剩下的图标缺口：资源被打包进 `.blp`，不是不存在

约 507 个标签仍然没有图标。查过了，**不是"磁盘上没有"**：

| 分类 | 数量 |
|---|---|
| 有 atlas 声明，但 `.dds` 在 `DDS_FOLDERS` 里找不到 | 379 标签 / 160 个 atlas |
| 连 `IconDefinitions` 行都没有 | 145 标签 |

对那 160 个 atlas 做了全盘搜索（Mods、创意工坊、SDK Assets、游戏本体），`.dds` 文件确实不存在，
**换任何扩展名也不存在**。但它们被打包在 **`.blp`** 里——Firaxis 的纹理包格式，文件头是 `CIVBLP`。
例如 `xhh-civ-bavaria` 整个 mod 没有一个 `.dds`，只有
`Platforms/Windows/BLPs/UITexture.blp`（47 MB），里面能直接读出
`CIVILIZATION_XHH_BAVARIA_22` 这样的条目名。

扫了 Mods 和创意工坊下全部 549 个 `.blp`（共 2.6 GB）：
**160 个缺失 atlas 里有 89 个的纹理确实躺在某个 `.blp` 内。**

**这条线已冻结**（2026-08-08）。逆向卡住（[blp-format.md](blp-format.md)），
而 XHH 反馈美术资源作者一般不会给源图。唯一还成立的路子是他提的：写程序化截图工具，
在游戏内百科里逐条截，理想情况用 Lua 控制翻页——工作量太大，暂不启动。

剩下 71 个 atlas 的纹理在本机确实完全不存在——多半是 HD 的 `ModSupport/` 给未安装的第三方 mod
声明了图标，那种情况本来就出不来。

### 其它

- **6 个图标解码出来是全透明的**：`ICON_UNIT_TREBUCHET`、`ICON_UNIT_MAN_AT_ARMS`、
  `ICON_UNIT_LINE_INFANTRY`、三个 `ICON_PROJECT_*BREAD_AND_CIRCUSES*`。
- **`ICON_BUILDING_CANAL` 是 mod 数据的问题**，不是生成器的：它被声明为
  `ICON_ATLAS_EXHIBITION` 索引 0，和 `ICON_BUILDING_EXHIBITION` 同一个槽位，
  所以运河现在显示成会展中心。这一条应该反馈给 mod 作者。
- ~~`Constants.DDS_FOLDERS` 的失效路径~~ 已解决：改成扫描 `config.properties` 里的几个根目录、
  收集所有直接含 `.dds` 的目录，清单不再需要人工维护，也就不会失效。

## Audit：把上面的检测固化下来

`view/Audit.java`，每轮 `page` / `after_init` 结束后自动跑，约 1 秒。也可以
`Main audit` 单独跑、`Main audit save` 更新基线。

| 指标 | 含义 | 当前基线 |
|---|---|---|
| `configRootsMissing` | `config.properties` 里不存在的根目录数 | 0 |
| `iconlabelsNoSrc` | 渲染出来但没有图标的行数（交叉列出的按行计） | 1092 |
| `iconlabelTagsNoSrc` | 同上，去重后的标签数 | 507 |
| `iconlabelsNoSrcButPngExists` | **PNG 已在磁盘却没接上——这个永远是 bug** | 4 |
| `androidPagesUnexpected` | 两棵输出树页面数差与预期值的偏离 | 0 |
| `searchEntriesNoIcon` | 搜索结果里没有图标的条目数 | 1261 / 4855 |

只报绝对值会变成噪音——上面好几个指标非零是我们理解并接受的原因（见本文各节）。
所以它对比基线，只在**往坏的方向动**时标 `<== WORSE`。基线文件
`manual/audit-baseline.json` 进版本库，改动前后各跑一次就知道产物有没有意外变化。

这也正好是性能重构需要的验收工具：性能优化的标准是"产物不变"，跑一遍 audit 三个指标不动即可。

> 第一次跑 audit 就抓到了我自己的错误——它报 1092，我手工脚本报 1014。查下来是脚本没跟
> `$ref`，audit 是对的。
