# CIVBLP v2 逆向笔记

目标：把打包在 `.blp` 里的贴图取出来，补上还缺图标的 89 个 atlas（见 [known-issues.md](known-issues.md)）。

**外部资料**：[Civ6 Wiki 的 BLP 页](https://civ6.fandom.com/wiki/BLP) 有 v1 的结构定义，是目前唯一的公开文档。
社区没有可用的解包工具：[ZenHAX 的帖子](https://www.zenhax.com/viewtopic.php@t=3300.html) 推到文件头就停了；
唯一的 Java 尝试 `sgarfinkel/blpdecoder` 作者中途放弃、**仓库现已 404**；
Sukritact's Texture Extractor 是游戏内截图的变通办法，不是解析器。

**我们手上的文件是 version 2，wiki 记的是 version 1**，两者有实质差异——这大概就是当年那位作者说的
「实际二进制和拿到的编码方案有出入」。以下是对着真实文件验证过的部分。

样本：`Mods/xhh-civ-bavaria/Platforms/Windows/BLPs/UITexture.blp`（47 MB）。

## 已验证

### 文件头（与 v1 文档一致）

```c
struct BLPHeader {          // 28 bytes, little-endian
    char   magic[6];        // 'CIVBLP'
    uint16 version;         // 我们的文件是 2，wiki 文档写的是 1
    uint32 packageDataOffset;   // 0x400（v1 文档说通常 0x200）
    uint32 packageDataSize;     // 0xE600
    uint32 bigDataOffset;       // 0xEA00
    uint32 bigDataCount;        // 61
    uint32 fileSize;            // 0x2F55200
};
```

两条恒等式都成立，可以拿来当校验：

- `packageDataOffset + packageDataSize == bigDataOffset`（0x400 + 0xE600 = 0xEA00）
- `fileSize == 实际文件大小`

### v2 的第一个差异：没有 PackagePreamble，PackageHeader 挪到了 +0x80

v1 文档说 `packageDataOffset` 处是 `PackagePreamble`（16 字节）紧接 `PackageHeader`。
但在 v2 里：

- 那 16 字节的 preamble 签名（`05 00 00 00 08 00 08 00 48 00 00 00 01 00 00 00`）**在整个文件里一次都没出现**
- `packageDataOffset`（0x400）往后 0x80 字节全是 0
- **`PackageHeader` 实际在 `packageDataOffset + 0x80`**（本例 0x480）

定位依据是文档里那四个常量字段，它们原样落在 0x48 字节头的末尾，不可能是巧合：

```
0x04B8  16 (0x10)  uiPackageBlockAlignment
0x04BC  48 (0x30)  uiSizeOfTypeInfoStripe
0x04C0  40 (0x28)  uiSizeOfPackageAllocation
0x04C4  16 (0x10)  uiSizeOfResourceAllocationDesc
```

按 v1 的 `PackageHeader` 布局（5 个 `StripeInfo` + 8 个 `uint32` = 0x48）解出来：

| 字段 | 值 |
|---|---|
| ResourceLinkerData | start 0x67, size 0 |
| PackageBlock | start 0x67, size 0x3740 |
| TempData | start 0x37A7, size 0x56EE |
| TypeInfo | start 0x67, **size 0** |
| RootTypeName | start 0x58, size 0xF |
| uiLinkerDataOffset | 0x2476 |
| uiResourceListOffset | 0 |

自洽性检查通过：`PackageBlock.start + PackageBlock.size == TempData.start`（0x67 + 0x3740 = 0x37A7）。

**`TypeInfo.size == 0`** —— v2 不再内嵌类型信息。这一条影响很大：v1 文档描述的通用反序列化流程
（靠 TypeInfoStripe 做指针 fixup）在 v2 上走不通，只能按固定布局硬解。对我们反而是好事，因为
我们只要贴图，不需要通用对象图。

### 字符串就是文档里的 `String::BasicT::Storage`

```c
struct Storage { uint32 capacity; uint32 length; char str[1]; };
```

实测吻合：

```
1C 00 00 00 | 1B 00 00 00 | "CIVILIZATION_XHH_BAVARIA_22"
  capacity=28    length=27     27 个字符
20 00 00 00 | 1F 00 00 00 | "LEADER_XHH_LUDWIG_II_FOREGROUND"
  capacity=32    length=31     31 个字符
```

本例的名字表在 0xA8BD..0xAB19，是一段连续的长度前缀字符串。

### 贴图在 bigData，不在 PackageBlock（已确认）

`packageData` **总共只有 58880 字节**，物理上装不下任何图标像素——
光 `XHH_BAVARIA_PRODUCTS_256` 一张 2048×2048 就是 16 MB。
所以 `packageData` 只是元数据，像素全在 `bigDataOffset` 之后那 49.5 MB 里。

### 名字表与数据块 1:1

按 `String::BasicT::Storage` 扫描整个 `packageData`，得到 **64 个字符串**：

- 3 个结构名：`ForgeUIAtlas`、`Page_0`、`Page_1`
- **61 个贴图名，正好等于 `bigDataCount`**

所以「第 N 个名字 ↔ 第 N 个数据块」，不需要额外的名字→块映射表。

### 没有内层容器

全文件搜不到 `DDS `、`DXT1`、`DXT5`、PNG 任何魔数。`bigDataOffset` 处直接就是像素字节
（`1D 0F 08 F8 | 1F 0F 08 FD`——四字节一组、第四字节接近 0xFF，像 RGBA8），
每个块没有自己的文件头。

### 我们要的东西确实在里面

这个包里的 `XHH_BAVARIA_PRODUCTS_256` 是 **2048×2048**，正是缺口最大的那个 atlas
（58 处标签引用）。方向没错。

## 一处自我更正

之前记过「bigData 前有一张 61 条记录表，尺寸 112×73」——**那是误报**。当时用一个宽松的模式
（`w, 1, 0, h, 0, h`）在几十 KB 里步长 4 地扫，命中 61 条纯属巧合，和 `bigDataCount` 撞上了。
真正对得上 61 的是名字表（见上）。**块的偏移和大小目前仍然不知道。**

## 已排除的假设（别再试一遍）

找块描述表试过四条路，全部落空。记下来是为了下一个人不用重跑：

1. **61 个大小值，和等于 bigData 区大小**。在 packageData 里按步长 4～72、uint32/uint64
   穷举了所有起点，**0 命中**。
2. **61 个单调递增的偏移**（首个 ≤ `bigDataOffset`、末个 ≤ `fileSize`）。同样穷举，**0 命中**。
3. **按 v1 文档从 `uiLinkerDataOffset` 定位 `PackageAllocation` 数组**。
   两种基址（`packageDataOffset` 和 PackageHeader 所在的 +0x80）都试了，解出来的
   `byStripe` 是 133/117 这类不可能的值，6 条里只有 1 条勉强合理——即噪声。
4. **`Types::Vector<T>` 的 `nElements == 61`**（spec 里 Vector = ptr64 + uint32 元素数）。
   packageData 里找不到任何一处 uint32 等于 61 且前面跟着合理指针。

结论：**v2 和 v1 文档的差异不止于 preamble，指针 fixup 那一层整个对不上。**
这也解释了当年那位 Java 作者为什么放弃。

## 一条尚未追查的线索

在 0x53A 附近有规整的重复结构：

```
[0, 256, 0, 256, 0, 256, 2048, 0, 0, 1792, ...]
[0, 256, 0, 256, 0, 256, 2048, 0, 0, 2560, ...]
[0, 256, 0, 256, 0, 256, 2048, 0, 0, 2816, ...]
```

`2048` 反复出现、末位值按 256 递增。结合根类型名 `ForgeUIAtlas` 和 `Page_0` / `Page_1`，
像是**图集内的精灵矩形表**：页宽 2048、每格 256。若成立，则 bigData 里的块是「页」，
而 61 个名字是页内的精灵——那么取一个图标 = 找到页 + 按矩形裁剪。这条还没验证。

## 尚未确认（下一步要做的）

1. **每个块的偏移、大小、像素格式。** 这是唯一挡路的东西。
   按 `IconTextureAtlases` 里的宽高算，56 个能解析出尺寸的名字若全按 RGBA8 计是 **53.9 MB**，
   而实际 bigData 只有 **49.5 MB**（还有 5 个名字查不到尺寸，只会让差距更大）。
   说明**不是所有块都是 RGBA8**，一部分多半是 BC 压缩的。所以块边界必须从表里读，不能靠宽高推算。

   佐证：按「最后一块是那张 2048×2048」的假设直接渲染，出来是横向条纹而不是图，
   说明偏移或行距至少有一个是错的。**这里不能再靠猜，得把描述表解出来。**

2. **跨文件验证**。以上全部只基于一个样本。至少要拿 5～6 个不同作者的包复核。

## 验证手段

有现成的对照组：某些 atlas **既有 `.dds` 又被打进了 `.blp`**。解出来的图和 `.dds` 逐像素比对，
就能证明解析是对的，不用靠肉眼看。写完读取器后应该拿这个当回归测试。
