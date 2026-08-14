package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Romanises a Chinese title so the search box can be driven from a latin keyboard.
 *
 * Only 20 of the ~2400 Chinese titles contain any latin letters, so before this a user typing
 * "jichang" got nothing for 机场 — the single biggest hole in search given that the audience is
 * mostly Chinese-speaking.
 *
 * The output is one token per character, space separated ("ji chang"), which is what the client
 * needs to tell syllables apart: it joins them for whole-pinyin matching, takes first letters for
 * initials matching ("jc"), and keeps the boundaries so a query can match from any syllable
 * ("jichang" finding 国际机场) without also matching mid-syllable noise.
 *
 * Readings come from two generated tables (scripts/gen_pinyin_tables.py):
 * {@code manual/pinyin-chars.tsv} covers every CJK ideograph, not just the ones in use, so a mod
 * update introducing a new character needs no action; {@code manual/pinyin-words.tsv} carries the
 * polyphone words, since character-by-character conversion reads 银行 as yinxing, 音乐 as yinle
 * and 长矛 as zhangmao. Words win over characters, longest first.
 *
 * A character with no reading is passed through as itself, which leaves a non-ASCII character in
 * the field — {@code searchPinyinUnmapped} in the audit counts those, so the gap is a number
 * rather than a silently unsearchable entry.
 */
public class Pinyin {

    private static final Map<Character, String> CHARS = new HashMap<>();
    private static final Map<String, String> WORDS = new HashMap<>();
    private static int longestWord = 0;

    static {
        load(new File("manual/pinyin-chars.tsv"), true);
        load(new File("manual/pinyin-words.tsv"), false);
    }

    /**
     * Space separated readings, or null when there is nothing Chinese to romanise — an English
     * title needs no pinyin field and paying for one on every entry of the en index is waste.
     */
    public static String of (String text) {
        if (text == null || !hasHan(text)) {
            return null;
        }
        StringBuilder out = new StringBuilder(text.length() * 4);
        int i = 0;
        while (i < text.length()) {
            String word = null;
            int size = Math.min(longestWord, text.length() - i);
            for (; size >= 2; size--) {
                word = WORDS.get(text.substring(i, i + size));
                if (word != null) {
                    break;
                }
            }
            if (word != null) {
                append(out, word);
                i += size;
                continue;
            }
            char c = text.charAt(i);
            if (isHan(c)) {
                String reading = CHARS.get(c);
                append(out, reading == null ? String.valueOf(c) : reading);
                i++;
            } else {
                // a run of latin or digits stays one token, so "P-51战斗机" gives "p-51 zhan dou ji"
                int start = i;
                while (i < text.length() && !isHan(text.charAt(i))) {
                    i++;
                }
                append(out, text.substring(start, i).toLowerCase());
            }
        }
        return out.toString();
    }

    private static void append (StringBuilder out, String token) {
        if (token.isEmpty()) {
            return;
        }
        if (out.length() > 0) {
            out.append(' ');
        }
        out.append(token);
    }

    /** Ideographs plus extension A; the table only holds the main block, the rest passes through. */
    private static boolean isHan (char c) {
        return c >= 0x3400 && c <= 0x9fff;
    }

    private static boolean hasHan (String text) {
        for (int i = 0; i < text.length(); i++) {
            if (isHan(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static void load (File file, boolean chars) {
        if (!file.exists()) {
            System.out.println("[SEARCH] " + file.getPath() + " is missing, titles get no pinyin"
                    + " (regenerate with scripts/gen_pinyin_tables.py)");
            return;
        }
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.isEmpty() || line.charAt(0) == '#') {
                    continue;
                }
                int tab = line.indexOf('\t');
                if (tab <= 0) {
                    continue;
                }
                String key = line.substring(0, tab);
                String value = line.substring(tab + 1).trim();
                if (chars) {
                    CHARS.put(key.charAt(0), value);
                } else {
                    WORDS.put(key, value);
                    longestWord = Math.max(longestWord, key.length());
                }
            }
        } catch (Exception e) {
            System.out.println("[SEARCH] could not read " + file.getPath() + ": " + e.getMessage());
        }
    }
}
