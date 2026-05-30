# 跑通1.3.9版本
## icon问题
先搞清楚icons load的流程
看看是哪里缺了东西
问问如何获取
跑一下试试

在SDK里面 但是没找到？ 原版和HD里的gold冲突了？（这个gold应该只用于澳大利亚场景） 忽略
can't find RESOURCE_GOLD
FontIcons.dds

database里没有 可能是其他mod的 忽略
can't find prestige

### TODO: 变成apk
### TODO:找不到对应的dds？
can't find GreatWork_Product
can't find RESOURCE_JNR_PEAT
Monopolies_Products22.dds
Wetlands_Resources256.dds Wetlands_Resources256_FOW.

### TODO:新UU的icon 没放进去
### TODO:万国之春新文明的icon缺失
### TODO:一些index是-1？

## 其他bug
- database里缺少table
    - QueryCriteria
    - PlayerItem
    - GameModePlayerItemOverrides
- Religious里的bug？(* syntax error?)
- UNIQUE constraint failed: 
    - PlayerColors.Type
    - Colors.Type
    - IconDefinitions.Name
    - IconDefinitions.Atlas
    - IconTextureAtlases.Name
    - IconTextureAtlases.IconSize
    - Icons.Name
- 2465378070\Texts\HD_Text.sql:(near "(": syntax error)



### 文件结构

- manual
  - 下面是所有手动操作的页面以及文件夹
  - 每个subdir下面的manual.json决定哪些文件被系统看到 加新东西需要修改
  - 一部分的manual内容被自动生成（changelog） 但不包括manual.json
    - 加了新版本需要重新修改这个内容
- manual/json和./json有什么区别
  - manual.json里只有index
  - 它和后续生成的页面是合并关系
- 