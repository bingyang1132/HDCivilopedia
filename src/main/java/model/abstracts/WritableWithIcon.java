package model.abstracts;

import com.alibaba.fastjson.JSONObject;

import tools.Tools;

public abstract class WritableWithIcon extends Writable {
    
    public String icon;

    public WritableWithIcon (String tag) {
        super(tag);
    }

    public JSONObject getIconLabel (String language) {
        return Tools.getIconlabel(getLink(language), icon, "ICON_" + tag, getTitle(language));
    }

    @Override
    public JSONObject toJson(String language) {
        JSONObject object = super.toJson(language);
        object.put("portrait", Tools.getPortrait(icon, "ICON_" + tag));
        return object;
    }
}
