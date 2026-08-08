# 缺失图集素材清单

HD 百科（hdcivilopedia）生成条目图标时，需要读取各 mod 的图集贴图（`.dds`）。
下列图集的贴图在发布版 mod 里**只以 `.blp` 形式存在**——`.blp` 是 ModBuddy 烤出来的打包格式，
社区没有可用的解包工具（唯一一次 Java 尝试已废弃、仓库已删），我们自己逆向到 version 2
也没能解开（v2 与公开的 v1 文档差异很大）。

所以想直接向作者要**烤成 `.blp` 之前的源图**。

## 需要什么

对下表每个「图集名」，需要**尺寸最大的那一张图集贴图**（即「需要的文件」列里的名字）：

- **要整张图集（sheet），不是单个图标**——百科按「格数×格数」的网格索引进去裁剪，
  所以网格布局必须和表里一致
- 格式：`.dds` 最好（和游戏里用的一样），`.png` 也可以
- **只需要最大尺寸那一档**。小尺寸（22/32/50…）用不到，百科总是取最大的那张
- 这些就是各位 ModBuddy 工程里 `.xlp` 引用的源图，一般直接躺在工程目录里

## 规模

**88 张图集，来自 62 个 mod、20 位作者**，影响百科里 237 处条目的图标显示。
下面按作者分组，按影响条目数排序。前 3 位作者占 65%，前 5 位占 77%。

---

## XHH  —  6 张图集, 影响 65 处条目

**LOC_XHH_CIV_BAVARIA_NAME** (本地 xhh-civ-bavaria)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_XHH_BAVARIA_PRODUCTS` | `XHH_BAVARIA_PRODUCTS_256` | 8×8 格 @256px (2048×2048) | 58 | PROJECT_BAVARIA_CULTURAL_EXCHANGE, PROJECT_CREATE_PRODUCT_BAVARIA_ABSINTHE |
| `ICON_ATLAS_XHH_BAVARIA_BUILDINGS` | `XHH_BAVARIA_BUILDINGS_256` | 2×2 格 @256px (512×512) | 3 | BUILDING_XHH_CLOTHING_STALL, BUILDING_XHH_FOOD_STALL |
| `ICON_ATLAS_LEADER_XHH_LUDWIG_II` | `LEADER_XHH_LUDWIG_II_256` | 1×1 格 @256px (256×256) | 1 | LEADER_XHH_LUDWIG_II |
| `ICON_ATLAS_CIVILIZATION_XHH_BAVARIA` | `CIVILIZATION_XHH_BAVARIA_256` | 1×1 格 @256px (256×256) | 1 | CIVILIZATION_XHH_BAVARIA |
| `ICON_ATLAS_UNIT_XHH_GEBIRGSJAGER` | `UNIT_XHH_GEBIRGSJAGER_256` | 1×1 格 @256px (256×256) | 1 | UNIT_XHH_GEBIRGSJAGER |
| `ICON_ATLAS_DISTRICT_XHH_FESTIVAL_THEATER` | `DISTRICT_XHH_FESTIVAL_THEATER_256` | 1×1 格 @256px (256×256) | 1 | DISTRICT_XHH_FESTIVAL_THEATER |

## JNR  —  14 张图集, 影响 53 处条目

**[COLOR:ResProductionLabelCS]UC[ENDCOLOR] - District Expansio** (创意工坊 2337885119)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_JNR_UC_REL_WORSHIP_BUILDINGS` | `UC_REL_Worship_Buildings256.dds` | 3×3 格 @256px (768×768) | 9 | BUILDING_JNR_CANDI, BUILDING_JNR_DAOGUAN |

**[COLOR:ResProductionLabelCS]UC[ENDCOLOR] - District Expansio** (创意工坊 2056401784)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_JNR_UC_COM_BUILDINGS` | `UC_COM_Buildings256.dds` | 4×4 格 @256px (1024×1024) | 8 | BUILDING_JNR_ENTREPOT, BUILDING_JNR_FISH_MARKET |

**[COLOR:ResProductionLabelCS]UC[ENDCOLOR] - District Expansio** (创意工坊 1839114973)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_JNR_UC_MIL_BUILDINGS` | `UC_MIL_Buildings256.dds` | 4×4 格 @256px (1024×1024) | 5 | BUILDING_JNR_ARSENAL, BUILDING_JNR_CAVALIER |

**[COLOR:ResProductionLabelCS]UC[ENDCOLOR] - District Expansio** (创意工坊 2402394695)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_JNR_UC_PRD_BUILDINGS` | `UC_PRD_Buildings256.dds` | 4×4 格 @256px (1024×1024) | 5 | BUILDING_IZ_WATER_MILL, BUILDING_JNR_CHEMICAL |

**[COLOR:ResProductionLabelCS]UC[ENDCOLOR] - District Expansio** (创意工坊 2474028548)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_JNR_UC_ENT_BUILDINGS` | `UC_ENT_Buildings256.dds` | 3×2 格 @256px (768×512) | 5 | BUILDING_JNR_BOTANICAL_GARDEN, BUILDING_JNR_CASINO |

**[COLOR:ResProductionLabelCS]UC[ENDCOLOR] - District Expansio** (创意工坊 2314657561)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_JNR_UC_THR_BUILDINGS` | `UC_THR_Buildings256.dds` | 4×4 格 @256px (1024×1024) | 4 | BUILDING_JNR_ASSEMBLY, BUILDING_JNR_GRAND_HOTEL |

**[COLOR:ResProductionLabelCS]UC[ENDCOLOR] - District Expansio** (创意工坊 2333458984)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_JNR_UC_REL_BUILDINGS` | `UC_REL_Buildings256.dds` | 4×2 格 @256px (1024×512) | 4 | BUILDING_JNR_ALTAR, BUILDING_JNR_GARDEN |

**[COLOR:ResProductionLabelCS]UC[ENDCOLOR] - District Expansio** (创意工坊 2112359835)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_JNR_UC_CMP_BUILDINGS` | `UC_CMP_Buildings256.dds` | 4×4 格 @256px (1024×1024) | 3 | BUILDING_JNR_EDUCATION, BUILDING_JNR_LIBERAL_ARTS |

**[COLOR:ResProductionLabelCS]UC[ENDCOLOR] - District Expansio** (创意工坊 2362219368)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_JNR_UC_AQD_BUILDINGS` | `UC_AQD_Buildings256.dds` | 2×2 格 @256px (512×512) | 3 | BUILDING_JNR_BATHHOUSE, BUILDING_JNR_HAMMER_WORKS |

**[COLOR:ResProductionLabelCS]UC[ENDCOLOR] - District Expansio** (创意工坊 2669325299)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_JNR_UC_NBH_BUILDINGS` | `UC_NBH_Buildings256.dds` | 4×2 格 @256px (1024×512) | 3 | BUILDING_JNR_HOSPITAL, BUILDING_JNR_RECYCLING_PLANT |

**[COLOR_Green]BD[ENDCOLOR] - Wetlands** (创意工坊 2135724456)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_JNR_WETLANDS_UNITACTIONS` | `Wetlands_UnitActions256.dds` | 1×1 格 @256px (256×256) | 1 | IMPROVEMENT_JNR_OASIS_FARM |
| `ICON_ATLAS_JNR_WETLANDS_FEATURES` | `Wetlands_Features256.dds` | 2×1 格 @256px (512×256) | 1 | FEATURE_HD_SWAMP |

**[COLOR:ResGoldLabelCS]Unique Building[ENDCOLOR]: Dojo (Japan** (创意工坊 2095575379)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_JNR_DOJO_BUILDINGS` | `Dojo_Buildings256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_JNR_DOJO |

**[COLOR_Green]BD[ENDCOLOR] - Savannah** (创意工坊 3263126211)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_JNR_SAVANNAH_FEATURES` | `Savannah_Features256.dds` | 1×1 格 @256px (256×256) | 1 | FEATURE_JNR_SAVANNAH |

## Sukritact  —  14 张图集, 影响 36 处条目

**Sukritact's Resources** (创意工坊 1150492115)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_SUK_RESOURCES` | `Suk_Resource_Atlas_256.dds` | 8×8 格 @256px (2048×2048) | 6 | RESOURCE_DLV_BISON, RESOURCE_GOLD |
| `ICON_ATLAS_SUK_ALTERNATERESOURCES` | `Suk_AlternateResources_Atlas_256.dds` | 8×8 格 @256px (2048×2048) | 3 | PROJECT_CREATE_CORPORATION_PRODUCT_SUK_CHEESE, PROJECT_CREATE_CORPORATION_PRODUCT_SUK_OBSIDIAN |

**Sukritact's Oceans** (创意工坊 2542898147)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_SUK_OCEAN_RESOURCES` | `Suk_OceansResource_Atlas_256.dds` | 8×8 格 @256px (2048×2048) | 6 | RESOURCE_SUK_ABALONE, RESOURCE_SUK_CAVIAR |
| `ICON_ATLAS_SUK_OCEANS_ALT_ECON` | `Suk_OceansAlternateResource_Atlas_256` | 8×8 格 @256px (2048×2048) | 2 | PROJECT_CREATE_CORPORATION_PRODUCT_SUK_CAVIAR, PROJECT_CREATE_CORPORATION_PRODUCT_SUK_LOBSTER |
| `ICON_ATLAS_FEATURE_SUK_KELP` | `Icon_Feature_Suk_Kelp_256` | 1×1 格 @256px (256×256) | 1 | FEATURE_SUK_KELP |

**Sukritact's Al-Hasan ibn Sulaiman (Swahili)** (创意工坊 1127339491)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_SUK_SWAHILI` | `Suk_Swahili_Atlas_256` | 5×1 格 @256px (1280×256) | 4 | BUILDING_SUK_PILLAR_TOMB, CIVILIZATION_SUK_SWAHILI |

**Sukritact's Ingolfur (Iceland)** (创意工坊 1299681230)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_SUK_ICELAND` | `Suk_Iceland_Atlas_256.dds` | 5×1 格 @256px (1280×256) | 4 | CIVILIZATION_SUK_ICELAND, DISTRICT_SUK_TORFBAEIR |

**Sukritact's Ramkhamhaeng (Siam)** (创意工坊 884825964)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_SUK_SIAM_RAMKHAMHAENG` | `Suk_SiamRamkhamhaeng_Atlas_256` | 6×1 格 @256px (1536×256) | 4 | CIVILIZATION_SUK_SIAM, DISTRICT_SUK_FLOATINGMARKET |

**Sukritact's Chulalongkorn (Siam)** (创意工坊 931093180)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_SUK_CHULALONGKORN_ATLAS` | `Icon_Suk_Chulalongkorn_256` | 1×1 格 @256px (256×256) | 1 | LEADER_SUK_CHULALONGKORN |
| `ICON_ATLAS_SUK_CHULALONGKORN_GOVERNORS` | `Suk_Damrong_Governors64` | 1×1 格 @64px (64×64) | 1 | GOVERNOR_SUK_DAMRONG |

**Sukritact's Narai (Siam)** (创意工坊 1734072409)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_SUK_NARAI` | `Suk_Narai_Atlas_256` | 1×1 格 @256px (256×256) | 1 | LEADER_SUK_NARAI |

**Sukritact's Wat Arun** (创意工坊 3357874053)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_SUK_WATARUN` | `WON_Suk_WatArun_Atlas_256` | 1×1 格 @256px (256×256) | 1 | BUILDING_SUK_WAT_ARUN |

**Sukritact's Candi Borobudur** (创意工坊 3378138501)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_SUK_BOROBODUR` | `Suk_Atlas_Borobodur_256` | 1×1 格 @256px (256×256) | 1 | BUILDING_SUK_BOROBUDUR |

**Sukritact's Notre-Dame** (创意工坊 3407707470)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_SUK_NOTREDAME_DE_PARIS` | `Suk_Atlas_NotreDame_256` | 1×1 格 @256px (256×256) | 1 | BUILDING_SUK_NOTRE_DAME_DE_PARIS |

## Leugi  —  4 张图集, 影响 15 处条目

**Leugi's [COLOR:ResGoldLabelCS]Monopoly++:[ENDCOLOR] Tycoons ** (创意工坊 2479197624)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_LEU_MONPOLYPLUS_IMPROVEMENTS` | `MonopolyPlus_Improvements_256.dds` | 3×2 格 @256px (768×512) | 5 | IMPROVEMENT_LEU_CONTAINER_PORT, IMPROVEMENT_LEU_STATION |
| `ICON_ATLAS_LEU_MONPOLYPLUS_UNIT_FLAGS` | `MonpolyPlus_UnitFlags_256.dds` | 2×1 格 @256px (512×256) | 3 | UNIT_HD_OVERSEAS_INVESTOR, UNIT_LEU_INVESTOR |

**Latin American [COLOR:ResCultureLabelCS]Resources[ENDCOLOR]** (创意工坊 2155632734)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_LEU_P0K_RESOURCES` | `Leu_p0k_Resource_Atlas_256.dds` | 8×8 格 @256px (2048×2048) | 6 | RESOURCE_LEU_P0K_CAPYBARAS, RESOURCE_LEU_P0K_COCA |

**Leugi's [COLOR:ResCultureLabelCS]Unique District Icons[ENDCO** (创意工坊 882664162)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_BDI_PROMENADE` | `Copacabana_256.dds` | 1×1 格 @256px (256×256) | 1 | DISTRICT_WATER_STREET_CARNIVAL |

## Albro  —  7 张图集, 影响 13 处条目

**CIVLIZATION VI: NATIONAL WONDERS** (创意工坊 2503076363)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `iconsNAT_CL_WONDERS` | `CL_NationalWonder_256.dds` | 5×1 格 @256px (1280×256) | 5 | NAT_WONDER_CL_CITADEL, NAT_WONDER_CL_COLLEGE |

**CIVLIZATION VI: NATIONAL WONDERS PACK 1** (创意工坊 2612473657)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `iconsNAT_CL_WONDERS_EXP_1` | `CL_NationalWonder_EXP1_256.dds` | 3×1 格 @256px (768×256) | 3 | NAT_WONDER_CL_IRONWORKS, NAT_WON_CL_AIRPORT |

**CIVILIZATION VI: ARECIBO OBSERVATORY** (创意工坊 2317145428)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_ARECIBO` | `ARECIBO_WON_256.dds` | 1×1 格 @256px (256×256) | 1 | WON_CL_BUILDING_ARECIBO |

**CIVILIZATION VI: KINKAKU JI** (创意工坊 2329124484)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `iconsKINKAKUJI` | `WON_CL_KINKAKU_256.dds` | 1×1 格 @256px (256×256) | 1 | WON_CL_KINKAKU |

**CIVILIZATION VI: EMPIRE STATE BUILDING** (创意工坊 2384183359)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `iconsEMPIRE` | `CL_EMPIRE_256.dds` | 1×1 格 @256px (256×256) | 1 | WON_CL_EMPIRE_STATES |

**CIVILIZATION VI: CN Tower** (创意工坊 2442822973)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `iconsCOREXPWON` | `COREX_WONDERS_256.dds` | 4×1 格 @256px (1024×256) | 1 | CL_BUILDING_CN_TOWER |

**CIVILIZATION IV: ST PETERS BASILICA** (创意工坊 2511437298)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `WONDER_AL_PETERS` | `WONDER_STPETER_256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_AL_STPETERSBASILICA |

## Deliverator  —  8 张图集, 影响 11 处条目

**Steel and Thunder: Unit Expansion** (创意工坊 1292617460)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_UNEX_UNITS` | `MOARUnitsFlags_256.dds` | 10×10 格 @256px (2560×2560) | 4 | UNIT_DLV_COG, UNIT_DLV_GALLEASS |

**Neuschwanstein Castle (World Wonder)** (创意工坊 1770681973)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_NEUSCHWANSTEIN` | `Icon_Neuschwanstein_256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_NEUSCHWANSTEIN |

**Burj Khalifa (World Wonder)** (创意工坊 1770703400)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_BURJ_KHALIFA` | `Icon_Burj_Khalifa_256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_BURJ_KHALIFA |

**Uffizi (World Wonder)** (创意工坊 1782204294)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_UFFIZI` | `Icon_Uffizi_256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_UFFIZI |

**Buddhas of Bamyan (World Wonder)** (创意工坊 1782222841)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_BAMYAN` | `Icon_Bamyan_256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_BAMYAN |

**Leaning Tower of Pisa (World Wonder)** (创意工坊 1828854812)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_LEANING_TOWER` | `Icon_Leaning_Tower_256` | 1×1 格 @256px (256×256) | 1 | BUILDING_LEANING_TOWER |

**Abu Simbel (World Wonder)** (创意工坊 1850173521)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_ABU_SIMBEL` | `Icon_Abu_Simbel_256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_ABU_SIMBEL |

**Tower Bridge (World Wonder)** (创意工坊 2068247220)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_TOWER_BRIDGE` | `Icon_Tower_Bridge_256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_TOWER_BRIDGE |

## DeepLogic  —  3 张图集, 影响 9 处条目

**LOC_CITY_STATES_DIVERSITY_NAME** (本地 HD_CityStatesDiversity)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_CSE_CITYSTATES_ATLAS` | `CSE_CITYSTATES_ATLAS_256.dds` | 8×8 格 @256px (2048×2048) | 6 | CIVILIZATION_CSD_GUNDISHAPUR, CIVILIZATION_CSE_CATALHOYUK |
| `ICON_ATLAS_ASIAN_CITYSTATES` | `AsiaCitystates_Icon_Atlas256.dds` | 4×6 格 @256px (1024×1536) | 2 | CIVILIZATION_CSD_MANILA, CIVILIZATION_CSD_SEVASTOPOL |
| `ICON_ATLAS_CSE_CITYSTATES_ATLAS_B` | `CSE_CITYSTATES_ATLAS_B_256.dds` | 8×8 格 @256px (2048×2048) | 1 | CIVILIZATION_CSD_VIENNA |

## SeelingCat  —  6 张图集, 影响 6 处条目

**CIVITAS [ICON_GOVERNMENT] Berbers** (创意工坊 1335234936)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_CVS_BERBER_UU_FLAG` | `ICON_CVS_BERBER_UU_256.dds` | 1×1 格 @256px (256×256) | 1 | UNIT_CVS_BERBER_UU |
| `ICON_ATLAS_CVS_BERBER_UI_ICON` | `CIVILIZATION_CVS_BERBER_UI_ICON_256.dds` | 1×1 格 @256px (256×256) | 1 | IMPROVEMENT_CVS_BERBER_UI |
| `ICON_ATLAS_CVS_BERBER_ICON` | `CIVILIZATION_CVS_BERBER_ICON_256.dds` | 1×1 格 @256px (256×256) | 1 | CIVILIZATION_CVS_BERBER |

**CIVITAS [ICON_GREATPERSON] Masinissa** (创意工坊 2891706503)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_CVS_MASINISSA_UU_FLAG` | `LEADER_CVS_MASINISSA_UU_ICON_256.dds` | 1×1 格 @256px (256×256) | 1 | UNIT_CVS_MASINISSA_UU |
| `ICON_ATLAS_CVS_MASINISSA_PORTRAIT` | `LEADER_CVS_MASINISSA_PORTRAIT_256.dds` | 1×1 格 @256px (256×256) | 1 | LEADER_CVS_MASINISSA |

**CIVITAS [ICON_GREATPERSON] Dihya** (创意工坊 1765625281)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_CVS_DIHYA_PORTRAIT` | `LEADER_CVS_DIHYA_PORTRAIT_256.dds` | 1×1 格 @256px (256×256) | 1 | LEADER_CVS_DIHYA |

## ChimpanG  —  5 张图集, 影响 5 处条目

**CIVITAS [ICON_GOVERNMENT] Romania** (创意工坊 1657610409)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_CVS_ROMANIA_UU_FLAG` | `CIVILIZATION_CVS_ROMANIA_UU_ICON_256.dds` | 1×1 格 @256px (256×256) | 1 | UNIT_CVS_ROMANIA_UU |
| `ICON_ATLAS_CVS_ROMANIA_ICON` | `CIVILIZATION_CVS_ROMANIA_ICON_256.dds` | 1×1 格 @256px (256×256) | 1 | CIVILIZATION_CVS_ROMANIA |
| `ICON_ATLAS_CVS_ROMANIA_UI_ICON` | `CIVILIZATION_CVS_ROMANIA_UI_ICON_256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_CVS_ROMANIA_UI |

**CIVITAS [ICON_GREATPERSON] Vlad III** (创意工坊 1657664177)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_CVS_VLAD_III_UU_FLAG` | `LEADER_CVS_VLAD_III_UU_ICON_256.dds` | 1×1 格 @256px (256×256) | 1 | UNIT_CVS_VLAD_III_UU |
| `ICON_ATLAS_CVS_VLAD_III_PORTRAIT` | `LEADER_CVS_VLAD_III_PORTRAIT_256.dds` | 1×1 格 @256px (256×256) | 1 | LEADER_CVS_VLAD_III |

## Boom  —  5 张图集, 影响 5 处条目

**Boom's Assyria** (创意工坊 2818358455)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_BOOM_ASSYRIA_UNITS` | `siegeTower256x256.dds` | 1×1 格 @256px (256×256) | 1 | UNIT_BOOM_SIEGE_ENGINE |
| `ICON_ATLAS_BOOM_ASSYRIA_CIVILIZATIONS` | `assyriaIcon256x256.dds` | 1×1 格 @256px (256×256) | 1 | CIVILIZATION_BOOM_ASSYRIA_HD |
| `ICON_ATLAS_LEADER_BOOM_ASHURBANIPAL` | `ashurbanipal256x256` | 1×1 格 @256px (256×256) | 1 | LEADER_BOOM_ASHURBANIPAL |
| `ICON_ATLAS_BOOM_ASSYRIA_IMPROVEMENTS` | `lamassuicon256x256.dds` | 1×1 格 @256px (256×256) | 1 | IMPROVEMENT_BOOM_LAMASSU |
| `ICON_ATLAS_BOOM_ASSYRIA_BUILDINGS` | `royallibrary256x256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_BOOM_ROYAL_LIBRARY |

## C  —  1 张图集, 影响 4 处条目

**LOC_AUSTRALIA_REWORK_NAME** (本地 civ6-australia-rework)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ATLAS_Austrilian_Unit_Icons` | `Austrilian_Unit_Icons_256` | 2×2 格 @256px (512×512) | 4 | UNIT_AUS_EXPLORER, UNIT_AUS_FISHERMAN |

## Merrick  —  4 张图集, 影响 4 处条目

**Merrick's Hittites (Suppiluliuma I)** (创意工坊 2381680617)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_MER_SUPPILULIUMA` | `suppi_alpha_256.dds` | 1×1 格 @256px (256×256) | 1 | LEADER_MER_SUPPILULIUMA |
| `ICON_ATLAS_MER_ANSUKURRA` | `hittites_ansukurra_256.dds` | 1×1 格 @256px (256×256) | 1 | UNIT_MER_ANSUKURRA |
| `ICON_ATLAS_MER_HITTITES` | `hittites_civalpha_256.dds` | 1×1 格 @256px (256×256) | 1 | CIVILIZATION_MER_HITTITES |
| `ICON_ATLAS_MER_ROYAL_ARCHIVE` | `BUILDING_MER_ROYAL_ARCHIVE_256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_MER_ROYAL_ARCHIVE |

## Windfly  —  2 张图集, 影响 2 处条目

**LOC_TGD_TITLE** (创意工坊 1710103474)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_THREE_GORDES_DAM` | `TGD256` | 1×1 格 @256px (256×256) | 1 | BUILDING_THREE_GORDES_DAM |

**The Motherland Calls Statue** (创意工坊 2187202390)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_MOTHERLAND_CALLS` | `Icon_TMC_256` | 1×1 格 @256px (256×256) | 1 | BUILDING_MOTHERLAND_CALLS |

## Del  —  2 张图集, 影响 2 处条目

**Porcelain Tower (World Wonder)** (创意工坊 1889267110)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_PORCELAIN_TOWER` | `Icon_Porcelain_Tower_256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_PORCELAIN_TOWER |

**Brandenburg Gate (World Wonder)** (创意工坊 1894176600)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_BRANDENBURG_GATE` | `Icon_Brandenburg_Gate_256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_BRANDENBURG_GATE |

## Phantagonist  —  2 张图集, 影响 2 处条目

**LOC_PHANTA_BRONZE_BIRD_TERRACE_MOD_NAME** (创意工坊 2996611118)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_PHANTA_BRONZE_BIRD_TERRACE` | `ICON_BUILDING_PHANTA_BRONZE_BIRD_TERRACE_256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_PHANTA_BRONZE_BIRD_TERRACE |

**LOC_PHANTA_TEMPLE_OF_HEAVEN_MOD_NAME** (创意工坊 3530119789)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_PHANTA_TEMPLE_OF_HEAVEN` | `ICON_BUILDING_PHANTA_TEMPLE_OF_HEAVEN_256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_PHANTA_TEMPLE_OF_HEAVEN |

## LOC_SIV_STEFANBATORY_AUTHORS  —  1 张图集, 影响 1 处条目

**LOC_SIV_STEFANBATORY_NAME** (本地 stefan-batory)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ATLAS_ICON_LEADER_SIV_STEFANBATORY` | `ICON_LEADER_SIV_STEFANBATORY_256` | 1×1 格 @256px (256×256) | 1 | LEADER_SIV_STEFANBATORY |

## p0kiehl  —  1 张图集, 影响 1 处条目

**p0kiehl's Temple of Poseidon** (创意工坊 1746376988)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_P0K_TEMPLE_POSEIDON` | `TemplePoseidon_256.dds` | 8×1 格 @256px (2048×256) | 1 | P0K_BUILDING_TEMPLE_POSEIDON |

## Various  —  1 张图集, 影响 1 处条目

**Globe Theatre (World Wonder)** (创意工坊 1770688835)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_GLOBE_THEATRE` | `Icon_Globe_Theatre_256.dds` | 1×1 格 @256px (256×256) | 1 | BUILDING_GLOBE_THEATRE |

## ShiroToraRyu  —  1 张图集, 影响 1 处条目

**Itsukushima Shrine (World Wonder)** (创意工坊 1818895938)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_ITSUKUSHIMA` | `Icon_Itsukushima_256` | 1×1 格 @256px (256×256) | 1 | BUILDING_ITSUKUSHIMA |

## Sailor Cat  —  1 张图集, 影响 1 处条目

**[COLOR:ResProductionLabelCS]Sailor Cat's[ENDCOLOR] Watchtowe** (创意工坊 1897241626)

| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |
|---|---|---|---|---|
| `ICON_ATLAS_SAILOR_WATCHTOWER_ICON` | `ICON_IMPROVEMENT_SAILOR_WATCHTOWER_256.dds` | 1×1 格 @256px (256×256) | 1 | IMPROVEMENT_SAILOR_WATCHTOWER |
