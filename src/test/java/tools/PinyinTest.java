package tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.junit.Test;

/**
 * Pinyin is what makes a Chinese title reachable from a latin keyboard, and its two tables are
 * generated (scripts/gen_pinyin_tables.py) — so what needs guarding is that the tables are still
 * there, still loaded, and that the word table still wins over character-by-character reading.
 */
public class PinyinTest {

    @Test
    public void readsCharacterByCharacter () {
        assertEquals("ji chang", Pinyin.of("机场"));
        assertEquals("guo ji ji chang", Pinyin.of("国际机场"));
    }

    /** The whole reason pinyin-words.tsv exists: these read wrong one character at a time. */
    @Test
    public void wordsBeatCharacters () {
        assertEquals("yin hang", Pinyin.of("银行"));          // yin xing
        assertEquals("yin yue you chuan", Pinyin.of("音乐游船"));  // yin le
        assertEquals("chang mao bing", Pinyin.of("长矛兵"));   // zhang mao
        assertEquals("bu ji zhan", Pinyin.of("补给站"));       // bu gei
    }

    @Test
    public void latinRunsStayOneToken () {
        // so the client's initials are p/z/d/j rather than one per digit
        assertEquals("p-51 zhan dou ji", Pinyin.of("P-51战斗机"));
    }

    /** ü comes out of the generator as u, and the client folds queries the same way. */
    @Test
    public void uUmlautIsFoldedToU () {
        assertEquals("lu se du shi", Pinyin.of("绿色都市"));
    }

    @Test
    public void nothingChineseMeansNoField () {
        assertNull(Pinyin.of("Airport"));
        assertNull(Pinyin.of("v1.2.1"));
        assertNull(Pinyin.of(""));
        assertNull(Pinyin.of(null));
    }

    /**
     * The character table covers the whole CJK block on purpose, so a mod introducing a new
     * character needs no action. A truncated or half-generated table would otherwise show up only
     * as entries quietly missing from pinyin search.
     */
    @Test
    public void characterTableCoversTheWholeBlock () throws Exception {
        int rows = 0;
        File file = new File("manual/pinyin-chars.tsv");
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.isEmpty() || line.charAt(0) == '#') {
                    continue;
                }
                String[] parts = line.split("\t");
                assertEquals("one character per row: " + line, 1, parts[0].length());
                assertTrue("readings are plain lowercase latin: " + line,
                        parts[1].matches("[a-z]+"));
                rows++;
            }
        }
        assertTrue("expected the whole CJK block, got " + rows + " rows", rows > 20000);
    }
}
