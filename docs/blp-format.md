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

### bigData 之前有一张 61 条的记录表

在 `bigDataOffset` 之前（本例 0xDDB8..0xE7E0）有一张记录表，按
「`uint32 w, 1, 0, h, 0, h`」的形状扫描，**恰好命中 61 条，等于 `bigDataCount`**。
记录里能读出两种尺寸：59 条 112×73、2 条 96×68。

### bigData 段像是未压缩的

`bigDataOffset` 起始处的字节形如 `1D 0F 08 F8 | 1F 0F 08 FD | 20 0F 07 FF`——
四字节一组、第四字节接近 0xFF，符合 RGBA8 而不是 BC 压缩块。

## 尚未确认（下一步要做的）

1. **那 61 条记录各字段的确切含义**。已知条数对得上 `bigDataCount`，但记录步长在扫描中不完全均匀
   （表跨度 0xA28 / 60 ≈ 42.7，不是整数），说明要么记录不定长，要么我的匹配位置有偏移。
2. **我们真正要的图标 atlas 在哪。** 61 个 bigData 块的尺寸是 112×73 / 96×68，而我们要的是
   `CIVILIZATION_XHH_BAVARIA_22` 这类 22/30/32/128/256 像素的图标图集。两者对不上，
   所以图标很可能存在 PackageBlock 里，不在 bigData 段。这一条必须先搞清楚，否则方向就错了。
3. **名字与数据块的对应关系**。名字表在 0xA8BD，记录表在 0xDDB8，中间还有约 12 KB 没有解读。
4. **跨文件验证**。以上全部只基于一个样本。至少要拿 5～6 个不同作者的包复核，确认头字段含义一致、
   像素段确实未压缩。

## 验证手段

有现成的对照组：某些 atlas **既有 `.dds` 又被打进了 `.blp`**。解出来的图和 `.dds` 逐像素比对，
就能证明解析是对的，不用靠肉眼看。写完读取器后应该拿这个当回归测试。
