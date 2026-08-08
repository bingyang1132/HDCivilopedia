# 静默失败与已知问题

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

- **约 524 个标签完全没有图标**。大部分是奇观——它们用大幅立绘而非图标，大概率是正常的，
  但没有逐个确认过。
- **6 个图标解码出来是全透明的**：`ICON_UNIT_TREBUCHET`、`ICON_UNIT_MAN_AT_ARMS`、
  `ICON_UNIT_LINE_INFANTRY`、三个 `ICON_PROJECT_*BREAD_AND_CIRCUSES*`。
- **`ICON_BUILDING_CANAL` 是 mod 数据的问题**，不是生成器的：它被声明为
  `ICON_ATLAS_EXHIBITION` 索引 0，和 `ICON_BUILDING_EXHIBITION` 同一个槽位，
  所以运河现在显示成会展中心。这一条应该反馈给 mod 作者。
- `Constants.DDS_FOLDERS` 仍有 11 条失效路径（4 条是已退订的创意工坊 mod，
  7 条是 mod 内部被删掉的子目录）。

## 建议的长期做法

`Constants.DDS_FOLDERS` 这种手工维护的绝对路径列表，本身就是这类问题的温床。
值得考虑：生成后跑一个 audit，报告「失效路径数 / 无 src 的 iconlabel 数 / 两棵树页面数差」，
数值异常就打醒目日志。这几类问题的共性都是**没人看所以一直攒着**。
