package view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.junit.Test;

public class AuditTest {

    /**
     * fastjson writes a repeated object as {"$ref": "$.files[0].iconlabel"} and resolves it back on
     * parse. The hand-written script that first counted these did not follow refs and reported 1014
     * where the truth was 1092 — every entry cross-listed under more than one folder went missing.
     * This pins down that the audit walks the resolved tree.
     */
    @Test
    public void repeatedEntriesAreCountedThroughFastjsonRefs () {
        JSONObject iconlabel = new JSONObject();
        iconlabel.put("alt", "ICON_BUILDING_BANK");            // decoded nothing, so no src

        JSONObject first = new JSONObject();
        first.put("iconlabel", iconlabel);
        JSONObject second = new JSONObject();
        second.put("iconlabel", iconlabel);                    // the same object, cross-listed
        JSONArray files = new JSONArray();
        files.add(first);
        files.add(second);
        JSONObject root = new JSONObject();
        root.put("files", files);

        String text = JSON.toJSONString(root);
        assertTrue("fastjson is expected to write a $ref here: " + text, text.contains("$ref"));

        List<String> alts = new ArrayList<>();
        Audit.collectIconlabelsWithoutSrc(JSON.parseObject(text), alts);
        assertEquals(2, alts.size());
    }

    @Test
    public void onlyIconTagsWithNoSrcCount () {
        JSONObject wired = new JSONObject();
        wired.put("alt", "ICON_BUILDING_BANK");
        wired.put("src", "../../../icons/ICON_BUILDING_BANK.png");
        JSONObject notATag = new JSONObject();
        notATag.put("alt", "银行");
        JSONObject missing = new JSONObject();
        missing.put("alt", "ICON_UNIT_TREBUCHET");

        JSONArray files = new JSONArray();
        for (JSONObject iconlabel : new JSONObject[] { wired, notATag, missing }) {
            JSONObject file = new JSONObject();
            file.put("iconlabel", iconlabel);
            files.add(file);
        }
        JSONObject root = new JSONObject();
        root.put("files", files);

        List<String> alts = new ArrayList<>();
        Audit.collectIconlabelsWithoutSrc(root, alts);
        assertEquals(1, alts.size());
        assertEquals("ICON_UNIT_TREBUCHET", alts.get(0));
    }

    /** Drives the two pinyin metrics: a title with Chinese in it is one that needs a `p` field. */
    @Test
    public void chineseIsDetectedTheSameWayAsInPinyin () {
        assertTrue(Audit.hasHan("银行"));
        assertTrue(Audit.hasHan("P-51战斗机"));
        assertFalse(Audit.hasHan("Airport"));
        assertFalse(Audit.hasHan("ji chang"));
    }
}
