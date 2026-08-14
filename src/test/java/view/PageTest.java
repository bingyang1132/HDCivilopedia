package view;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PageTest {

    /** Titles carry [ICON_*]/[COLOR] markup that must not reach the search index or the dropdown. */
    @Test
    public void markupIsStrippedFromSearchTitles () {
        assertEquals("银行", Page.searchTitle("[ICON_BUILDING_BANK]银行"));
        assertEquals("国际机场：进口新的 食品",
                Page.searchTitle("[ICON_PROJECT] 国际机场：进口新的  [ICON_FOOD] 食品"));
    }

    /** An entry whose whole name is one token would otherwise end up with an empty title. */
    @Test
    public void aNameThatIsAllMarkupIsKept () {
        assertEquals("[ICON_BUILDING_BANK]", Page.searchTitle("[ICON_BUILDING_BANK]"));
    }

    /**
     * search-data.js and the sidebar are written as .js without a charset declaration, so every
     * non-ASCII character has to leave as an escape.
     */
    @Test
    public void jsStringsAreAsciiOnly () {
        assertEquals("\\u673a\\u573a", Page.toJsString("机场"));
        assertEquals("it\\'s a \\\\ thing", Page.toJsString("it's a \\ thing"));
    }
}
