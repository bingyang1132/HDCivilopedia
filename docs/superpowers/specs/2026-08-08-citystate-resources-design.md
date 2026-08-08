# 城邦专属资源进百科 — 设计

2026-08-08

## 背景

HD 近期版本加入了「城邦专属资源」：宗主国从城邦获得的资源。数据在
`DebugGameplay.sqlite` 的 `HD_CityState_Resources`，36 行 = 9 种城邦类型 × 4 个资源，
每行带一个 `ResourceClassificationType`。

**百科里目前一个都没有。** 原因不是漏读表，是 `Resource.load()` 的加载条件：

```sql
select * from Resources where exists (Resource_ValidFeatures ...) or exists (Resource_ValidTerrains ...)
```

只收「能出现在地图上」的资源。这 36 个在 `Resources` 表里存在（均为 `RESOURCECLASS_LUXURY`），
但 ValidFeatures / ValidTerrains 各 0 行，产出、消耗、采集也都没有——它们不是地块资源，
是宗主国奖励，所以被整体滤掉。

它们的「生产项目」页面倒是已经有了（`historic_moments/corporation_product/PROJECT_CREATE_CORPORATION_PRODUCT_HD_CS_*`，
36 个齐全），即「怎么造」有，「是什么、谁给的」没有。

## 目标

1. 36 个资源各有独立页面，说明所属城邦类型与资源分类。
2. 城邦页面上列出该城邦提供的 4 个专属资源，并链到资源页。

## 设计

### 1. 加载

`Resource.load()` 的 WHERE 追加一个条件：

```sql
or exists (select 1 from HD_CityState_Resources h where h.ResourceType = Resources.ResourceType)
```

`Resource` 增加两个字段，从 `HD_CityState_Resources` 读入：

- `cityStateType` — 如 `SCIENTIFIC`
- `resourceClassification` — 如 `RESOURCE_CLASSIFICATION_HD_STATIONERY`

### 2. 分组

这些资源是 `RESOURCECLASS_LUXURY` 且没有改良，按现有 `getFolder()` 会落进 `luxury`，
显示为「奢侈资源」——名不副实。改为独立文件夹：

- `getFolder()` → `citystate`
- `getFolderName()` → 城邦资源 / City-State Resources
- `getFolderOrder()` → 1500（奢侈 1000 与战略 2000 之间）

### 3. 资源页面

现有模板每个区块都有 `isEmpty()` / `size() > 0` 守卫，所以地块、产出、采集、改良这些
无数据的区块**本来就不渲染**，不需要额外处理。页面新增两行：

- **来源**：所属城邦类型（带色文字，非链接——见「不做」）
- **资源分类**：`RESOURCE_CLASSIFICATION_HD_*` 的本地化名称

### 4. 城邦页面

每个城邦页右栏加一个「城邦专属资源」statbox，列出该类型的 4 个资源，
带图标并链到资源页。

join 键：`HD_CityState_Resources.CityStateType` ↔ `CityState.getFolder()`
（后者是 `inheritLeader` 去掉 `LEADER_MINOR_CIV_` 前缀并小写，两者一一对应）。

同类型的城邦会重复显示相同的 4 个资源。这是刻意的：读者是在看某个具体城邦时
想知道「它给我什么」。

### 5. `LIGHT_INDUSTRIAL`

`HD_CityState_Resources` 有 9 种类型，但游戏数据里只有 8 种城邦真实存在
（`CSE_ClassTypes` 8 条、城邦的 `inheritLeader` 8 种，都没有 `LIGHT_INDUSTRIAL`）。
「轻工业城邦」的 4 个资源存在，却没有任何城邦属于该类型。

**处理**：照常收录这 4 个资源页面（数据在就展示），但「来源」一栏标注为未启用，
不伪造一个不存在的城邦类型。若日后 HD 补上该类型的城邦，无需改代码即可自动接上。

## 不做

- **不新建「城邦类型」页面**。类型目前只是 citystates 章节的文件夹分组，本身没有页面，
  所以资源页的「来源」是文字而非链接。要做成链接得新增 9 个类型页，属于另一个需求。
- **不与生产项目页互链**。`PROJECT_CREATE_CORPORATION_PRODUCT_HD_CS_*` 已存在且命名可推导，
  互链是加分项，先不铺开。

## 验收

- 36 个 `RESOURCE_HD_CS_*` 页面生成，位于 `resources/citystate/`。
- 8 种类型的城邦页面各出现「城邦专属资源」区块，条目链接可达。
- `Main audit`：`iconlabelsNoSrc` 等指标不因本次改动而变差；页面数按预期增加
  （中英各 36 页，两棵输出树同步）。
