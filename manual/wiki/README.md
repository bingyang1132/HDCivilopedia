# 抓取缓存 / Scrape cache

每个条目的「历史背景」正文缓存，一个实体一个 json：`{title, url, text, source?}`。
`tools/WikiFetcher.java` 写入，`Tools.getWikiHistory` 读取并在页面底部附上来源链接。
没有缓存的条目会退回游戏自带的百科文本。

Cached lead paragraphs used as the History section of each page, one json per entity.
Written by `tools/WikiFetcher.java`, read by `Tools.getWikiHistory`, which appends a link
back to the source. Entries with no cache fall back to the game's own text.

## 来源与许可 / Sources and licensing

| `source` | 出处 | 许可 |
|---|---|---|
| （无字段） | 维基百科条目首段 | CC BY-SA 3.0 |
| `wiki_translated` | 英文维基百科首段的人工中译 | CC BY-SA 3.0（译文为衍生作品） |
| `baidu` | 百度百科 | **无可再分发的许可，故不入版本管理** |

维基百科部分按 CC BY-SA 3.0 使用：每个 json 都记录了原文 URL，生成的页面逐条标注来源
并链接回原文。

Wikipedia-derived text here is used under CC BY-SA 3.0: every json records the source URL,
and every generated page attributes and links back to it.

## 缺失的条目 / What is missing here

`zh_Hans_CN/CIVILIZATION_CHINA.json` **不在仓库里**（百度百科来源，见上）。
重新生成的办法写在 `_overrides.tsv` 里 `CIVILIZATION_CHINA` 那一行上方的注释中。
缺这个文件时，`Main wiki` 会把它报成一条失败而不是悄悄退回——这是有意的，
提醒你这条需要自己补；不补的话该页面用游戏自带文本，其余一切正常。

That one file is excluded from the repository. `_overrides.tsv` documents how to recreate it;
until you do, `Main wiki` reports it as a failure rather than silently falling back, and the
page uses the game's own text.

## 重新抓取 / Re-fetching

```
Main wiki           # 补齐缺失的条目，已有文件跳过
Main wiki refresh   # 就地重抓已缓存的条目（只改措辞，不改指向哪篇文章）
```

`_failures.tsv` 是抓不到文章的条目清单，`_overrides.tsv` 用来指定确切的条目名或 URL。
