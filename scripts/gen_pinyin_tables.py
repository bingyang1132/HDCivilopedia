"""Generates the two pinyin tables the build reads (manual/pinyin-chars.tsv, manual/pinyin-words.tsv).

Search needs pinyin because only 20 of ~2400 Chinese titles contain any latin letters, so a
user typing "jichang" finds nothing. The conversion itself happens in Java at build time
(tools/Pinyin.java); this script only produces its data, so neither the build nor the shipped
pages depend on Python.

Two tables, for two different failure modes:

  pinyin-chars.tsv  every CJK ideograph in U+4E00..U+9FFF that pypinyin knows a reading for,
                    not just the ones currently in use. Complete on purpose: a mod update that
                    introduces a new character then needs no action here.

  pinyin-words.tsv  the polyphone fixes. Character-by-character conversion gets 51 of the
                    current titles wrong -- 银行 as yinxing, 音乐 as yinle, 长矛 as zhangmao --
                    so this holds the multi-character words where the correct reading differs,
                    and Java prefers the longest word match before falling back to characters.
                    Only words that actually occur in a title are emitted; the full phrase
                    dictionary is ~130k entries and irrelevant here.

Re-run after a mod update if a new title reads wrong (the character table covers new
characters, but a new polyphone word needs a new row):

    pip install pypinyin
    python scripts/gen_pinyin_tables.py

It verifies that the Java algorithm -- longest word match, then per character -- reproduces
pypinyin's own phrase-aware output for every title in output/zh_Hans_CN/search-data.js, and
fails loudly if it does not, adding whole titles to the word table as a last resort.
"""

import json
import os
import re
import sys

from pypinyin import lazy_pinyin
from pypinyin.constants import PHRASES_DICT

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
INDEX = os.path.join(REPO, "output", "zh_Hans_CN", "search-data.js")
CHARS_TSV = os.path.join(REPO, "manual", "pinyin-chars.tsv")
WORDS_TSV = os.path.join(REPO, "manual", "pinyin-words.tsv")

HAN = re.compile(r"[一-鿿]")
CJK = range(0x4E00, 0xA000)

# Words where pypinyin's phrase dictionary disagrees with the reading a player would type.
# Kept here rather than by hand-editing the generated table, so regenerating keeps the fix.
EXCEPTIONS = {"大堡礁": "da bao jiao"}   # its dictionary has 堡 as pu; the reef is da bao jiao


def normalise(syllable):
    """ü is written v by pypinyin's lazy mode; fold it to u so both spellings match.

    Nothing else in pinyin uses either letter, so folding v to u is safe in both directions:
    the client folds the query the same way, and a user typing lv or lu for 绿 both land on
    "lu". It costs a few harmless collisions (路 and 绿 share "lu") and buys not having to
    guess which spelling the user reaches for.
    """
    # lowercased so a latin run inside a title ("P-51战斗机") compares equal to what the Java
    # side produces, instead of looking like a mismatch and ending up in the word table
    return syllable.lower().replace("v", "u").replace("ü", "u")


def readings(text):
    if not text:
        return []
    for word, fixed in EXCEPTIONS.items():
        if word in text:
            i = text.index(word)
            return readings(text[:i]) + fixed.split() + readings(text[i + len(word):])
    return [normalise(s) for s in lazy_pinyin(text)]


def titles():
    with open(INDEX, encoding="utf-8") as f:
        src = f.read()
    data = json.loads(src[src.index("=") + 1:src.rindex(";")])
    return [e["t"] for e in data if HAN.search(e["t"])]


def convert(text, words, chars, longest):
    """The Java algorithm, so it can be verified here: longest word match, then per character."""
    out = []
    i = 0
    while i < len(text):
        hit = None
        for size in range(min(longest, len(text) - i), 1, -1):
            hit = words.get(text[i:i + size])
            if hit:
                out.extend(hit)
                i += size
                break
        if hit:
            continue
        c = text[i]
        if HAN.match(c):
            out.append(chars.get(c, c))
            i += 1
        else:                                     # a run of latin/digits stays one token
            start = i
            while i < len(text) and not HAN.match(text[i]):
                i += 1
            out.append(text[start:i].lower())
    return out


def main():
    if not os.path.exists(INDEX):
        sys.exit("no " + INDEX + " -- run the pedia once first")

    chars = {}
    for code in CJK:
        c = chr(code)
        got = lazy_pinyin(c)
        # errors default to leaving unknown characters as-is, which comes back as the character
        if got and got[0] != c and got[0].isascii():
            chars[c] = normalise(got[0])

    # candidate words: every substring of a title that the phrase dictionary knows and that
    # character-by-character conversion would get wrong
    words = {}
    all_titles = titles()
    for title in all_titles:
        for size in range(2, 8):
            for i in range(len(title) - size + 1):
                word = title[i:i + size]
                if word in words or word not in PHRASES_DICT:
                    continue
                phrase = readings(word)
                charwise = [chars.get(c, c) for c in word]
                if phrase != charwise:
                    words[word] = phrase

    # verify the Java algorithm against pypinyin's own phrase-aware output, and patch whatever
    # is left over with a whole-title row rather than shipping a wrong reading
    longest = max((len(w) for w in words), default=0)
    for title in all_titles:
        if convert(title, words, chars, longest) != readings(title):
            words[title] = readings(title)
            longest = max(longest, len(title))
    bad = [t for t in all_titles if convert(t, words, chars, longest) != readings(t)]
    if bad:
        sys.exit("still wrong after patching: " + repr(bad[:10]))

    with open(CHARS_TSV, "w", encoding="utf-8", newline="\n") as f:
        f.write("# character\treading. Generated by scripts/gen_pinyin_tables.py, do not edit.\n")
        for c in sorted(chars):
            f.write(c + "\t" + chars[c] + "\n")
    with open(WORDS_TSV, "w", encoding="utf-8", newline="\n") as f:
        f.write("# word\tspace-separated readings. Generated by scripts/gen_pinyin_tables.py,"
                " do not edit. Only polyphone words that occur in a title.\n")
        for w in sorted(words, key=lambda w: (-len(w), w)):
            f.write(w + "\t" + " ".join(words[w]) + "\n")

    print("chars: %d  words: %d  verified against %d titles"
          % (len(chars), len(words), len(all_titles)))


if __name__ == "__main__":
    main()
