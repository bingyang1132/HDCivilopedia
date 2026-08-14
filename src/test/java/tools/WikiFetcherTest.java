package tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * The pure parts of the wiki scraper. Each of these encodes something that took a round of wrong
 * output to find, which is exactly what is worth pinning down: the zh variant, the persona
 * epithets, and what counts as a fetchable host.
 */
public class WikiFetcherTest {

    /**
     * zh-hans converts characters only, so regional terms arrive in their Taiwan form
     * (鄂图曼帝国, 大部份). zh-cn converts those too. Everything else asks for no variant.
     */
    @Test
    public void zhAsksForTheMainlandVariant () {
        assertEquals("zh-cn", WikiFetcher.acceptLang("zh"));
        assertNull(WikiFetcher.acceptLang("en"));
    }

    /**
     * A leader's display name carries the persona epithet while the article sits under the bare
     * name — and both personas of a leader share that one article. 29 of 95 leaders per language
     * were missing before this retry existed.
     */
    @Test
    public void personaEpithetsAreDropped () {
        assertEquals("嬴政", WikiFetcher.dropEpithet("嬴政（受命于天）"));
        assertEquals("Victoria", WikiFetcher.dropEpithet("Victoria (Age of Steam)"));
        assertEquals("腓力二世", WikiFetcher.dropEpithet("腓力二世"));
    }

    @Test
    public void titlesComeOutOfWikiUrlsDecoded () {
        assertEquals("Amanitore", WikiFetcher.titleFromOverride("https://en.wikipedia.org/wiki/Amanitore"));
        assertEquals("马其顿王国", WikiFetcher.titleFromOverride(
                "https://zh.wikipedia.org/wiki/%E9%A9%AC%E5%85%B6%E9%A1%BF%E7%8E%8B%E5%9B%BD"));
        assertEquals("Amanitore", WikiFetcher.titleFromOverride(
                "https://en.wikipedia.org/wiki/Amanitore?action=raw#Life"));
        // a bare title is an override value too, and passes through untouched
        assertEquals("腓力二世 (西班牙)", WikiFetcher.titleFromOverride("腓力二世 (西班牙)"));
    }

    @Test
    public void onlyRealUrlsHaveAHost () {
        assertEquals("zh.wikipedia.org", WikiFetcher.hostOf("https://zh.wikipedia.org/wiki/机场"));
        assertEquals("baike.baidu.com", WikiFetcher.hostOf("https://baike.baidu.com/item/中国/1122445"));
        assertNull(WikiFetcher.hostOf("机场"));
        assertNull(WikiFetcher.hostOf(null));
    }
}
