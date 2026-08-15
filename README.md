# HD Civilopedia

*[简体中文](README.zh-CN.md)*

A static-site generator for the in-game encyclopedia of **Harmony in Diversity**, a large
overhaul mod for Sid Meier's Civilization VI. It reads the game's own SQLite databases and
localization tables, applies every DLC and mod on top, and renders each entity as a
cross-linked HTML page — roughly 4,900 pages, in two languages, in a desktop and a mobile
edition. Its output is the encyclopedia published at [civ6hd.com](https://civ6hd.com).

```
init → icons → load → write → page
build   cut      read   write   render
 DBs   icons     DBs    JSON     HTML
```

> **This repository cannot run on its own.** The generator reads an installed copy of the
> game: its databases (~300 MB), its localization tables and its texture atlases, none of
> which are — or will be — in version control. CI therefore only exercises the logic that
> does not need game data.

## Relationship to upstream

This is a fork of **[xiaoxiaoccat/hdcivilopedia](https://gitee.com/xiaoxiaoccat/hdcivilopedia)**,
branched at upstream `a0d842a` (`v1.3.8`, 2023-07-13).

**The pipeline itself, the thirty-odd entity models and their rendering, the DDS decoding,
the changelog DSL and its parser, and the bilingual and mobile HTML output are the work of
the upstream author, xiaoxiao.** This repository has maintained the project since 2024-09.

> The upstream commits are not in this repository's history — a clean import in May 2026
> stripped several hundred MB of game data out and took them with it — so `git log` here
> credits one author for work that had two.
> **[docs/upstream-differences.md](docs/upstream-differences.md) is the missing trail**: what
> came from where, with the commands to reproduce the diff yourself.

What this fork changed:

| | |
|---|---|
| Performance | A full run went from ~3 hours to **90 seconds**, output identical to the byte |
| Search | New: build-time index, pinyin matching (full / initials / syllable-aligned), tiered ranking |
| History text | New: encyclopedia scraping, 59/59 civilizations and 95/95 leaders covered |
| Artifact audit | New: 11 metrics compared against a committed baseline after every run |
| Archiving | New: a dated snapshot per run, groundwork for deriving changelogs from artifact diffs |
| Icons | Fixed dead atlas paths and a row-selection bug; search hits without an icon 41% → 26% |
| Mobile edition | Removed 1570 orphaned pages (198 → 163 MB), extracted the inlined sidebar |
| Engineering | Externalized configuration, unit tests + CI, five engineering notes |

## Requirements

- A **JDK** (bytecode targets Java 8; building with 8, 17 or 22 all work) and **Maven**
- An installed copy of **Civilization VI** with the Harmony in Diversity mod and its
  third-party mod dependencies

## Getting started

```bash
cp config.example.properties config.properties   # local paths; without it, defaults are
                                                 # derived from the environment
mvn compile
```

The entry point is `model.abstracts.Main`; the first argument is a command:

| Command | What it does |
|---|---|
| `init` | Rebuild the databases from the game's cache into `database/` |
| `icons` | Decode DDS atlases, cut `output/icons/*.png` |
| `changelog` | Load content and write `json/` |
| `page` | Render `json/` into `output/` and `output_android/`; runs audit and archiving at the end |
| `after_init` | The whole chain, about 90 seconds |
| `wiki [refresh\|N]` | Fetch or refresh the History sections |
| `audit [save]` | Artifact health check; `save` records a new baseline |
| `archive` | Snapshot the current artifacts by hand |

With no arguments it runs everything end to end.

## Tests

```bash
mvn test                     # pure logic: pinyin tables, archiving, wiki name handling, audit counting
node scripts/test_search.js  # the shipped search ranking and pinyin matching
```

CI (`.github/workflows/ci.yml`) runs both, on JDK 8 and 17. **Artifact-level acceptance is
local**: `Main audit` compares against `manual/audit-baseline.json` after every run, and
`scripts/hash_artifacts.py` produces a sha1 manifest of all 18,000-odd output files, so a
refactor that is meant to change nothing can be held to exactly that.

## Documentation

The engineering notes are in Chinese. What is in them:

| | |
|---|---|
| [known-issues.md](docs/known-issues.md) | Known defects, and **how to detect** each class of silent failure |
| [performance.md](docs/performance.md) | The two optimization rounds, and why measuring first overturned the plan |
| [roadmap.md](docs/roadmap.md) | What is next, including a design for rebuilding the changelog system |
| [upstream-differences.md](docs/upstream-differences.md) | Item-by-item diff against upstream, and how to verify it |
| [blp-format.md](docs/blp-format.md) | Reverse-engineering Firaxis's `.blp` texture packs — **including the four hypotheses ruled out** |
| [missing-atlas-art.md](docs/missing-atlas-art.md) | Atlases whose source art is missing, grouped by mod author |
| [run.md](docs/run.md) | Build and command details |

One theme runs through all of them, and `known-issues.md` is the place to start: **every
layer of this generator catches and continues, so a defect never fails the build — it just
leaves the output quietly missing something.** Do not judge severity by the logs; the icon
bug printed four warnings and affected 256 tags. Work backwards from the artifacts instead.

## Licensing

⚠️ **Upstream declares no license. This repository is therefore not yet published, and
cannot declare a license of its own** — under default copyright the upstream author retains
all rights, and a derivative work needs their permission. See [NOTICE](NOTICE) and the
licensing section of [docs/upstream-differences.md](docs/upstream-differences.md).

`manual/wiki/` holds cached lead paragraphs from Wikipedia, used under CC BY-SA 3.0: every
entry records its source URL and every generated page links back to it. The single entry
sourced from Baidu Baike is excluded from the repository, since there is no license to
redistribute it under; see [manual/wiki/README.md](manual/wiki/README.md).

Game data and art assets belong to Firaxis Games / 2K Games and the individual mod authors.
They have never been in this repository and are not distributed with it.
