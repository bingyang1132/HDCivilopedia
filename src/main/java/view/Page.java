package view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tools.Tools;

public class Page {

    public static final String[] LANGUAGES = new String[] {
            "zh_Hans_CN", "en_US"
    };

    public static final String[] HEADERS = new String[] {
            "concepts", "civilizations", "citystates", "districts",
            "buildings", "wonders", "units", "unitpromotions",
            "greatpeople", "technologies", "civics", "governments",
            "religions", "features", "resources", "improvements",
            "governors", "historic_moments"
    };

    public static Object analyzeBrackets(Object object) {
        if (object instanceof JSONObject) {
            JSONObject result = new JSONObject();
            for (Entry<String, Object> entry : ((JSONObject) object).entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                result.put(key, analyzeBrackets(value));
            }
            return result;
        } else if (object instanceof JSONArray) {
            JSONArray result = new JSONArray();
            for (Object o : (JSONArray) object) {
                result.add(analyzeBrackets(o));
            }
            return result;
        } else if (object instanceof String) {
            String s = (String) object;
            try {
                s = Tools.analyzeBrackets((String) object);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(object);
            }
            return s;
        } else {
            return object;
        }
    }

    public static Element getPromotionElement(JSONObject promotion, Document document) {
        Element buttonCompleted = document.createElement("div");
        buttonCompleted.setAttribute("class", "buttonCompleted");

        Element div1 = document.createElement("div");
        div1.setAttribute("style", "display:inline-block;position:absolute;margin:-15px 0 0 -14px;width:32px;height:32px");

        Element img = document.createElement("img");
        img.setAttribute("src", promotion.getString("icon"));
        img.setAttribute("alt", promotion.getString("alt"));
        div1.appendChild(img);
        buttonCompleted.appendChild(div1);

        Element div2 = document.createElement("div");
        div2.setAttribute("style", "margin:-8px -22px 0 22px;font-size:16px;color:white;");
        div2.setTextContent(promotion.getString("title"));
        buttonCompleted.appendChild(div2);

        Element div3 = document.createElement("div");
        div3.setAttribute("style", "margin:8px -9px -22px -9px;font-size:14px;color:white;");
        div3.setTextContent(promotion.getString("text"));
        buttonCompleted.appendChild(div3);

        return buttonCompleted;
    }

    public static Element getGovernorElement(JSONObject governor, Document document) {
        Element governorsPromotionButton = document.createElement("div");
        String icon = governor.getString("icon");
        if (icon != null) {
            governorsPromotionButton.setAttribute("class", "baseAbility");

            Element div1 = document.createElement("div");
            div1.setAttribute("style", "display:inline-block;position:absolute;margin:-11px 0 0 -14px; width:48px;height:48px");

            Element img = document.createElement("img");
            img.setAttribute("src", icon);
            img.setAttribute("alt", governor.getString("alt"));
            img.setAttribute("width", "48px");
            img.setAttribute("height", "48px");
            div1.appendChild(img);
            governorsPromotionButton.appendChild(div1);
    
            Element div2 = document.createElement("div");
            div2.setAttribute("style", "margin:-12px -22px 0 40px;font-size: 14px;color: white;");
            div2.setTextContent(governor.getString("text"));
            governorsPromotionButton.appendChild(div2);
        } else {
            governorsPromotionButton.setAttribute("class", "governorsPromotionButton");
            
            Element div1 = document.createElement("div");
            div1.setAttribute("style", "margin:14px 0 0 16px;font-size: 16px;color:white;");
            div1.setTextContent(governor.getString("title"));
            governorsPromotionButton.appendChild(div1);
    
            Element div2 = document.createElement("div");
            div2.setAttribute("style", "margin:20px 16px 0 16px;font-size: 14px;color:white;");
            div2.setTextContent(governor.getString("text"));
            governorsPromotionButton.appendChild(div2);
        }
        return governorsPromotionButton;
    }

    public static Element getHeaderElement (Document document, String thisLink) {
        Element appHeader = document.createElement("div");
        appHeader.setAttribute("class", "appHeader");

        Element tabLedge = document.createElement("div");
        appHeader.appendChild(tabLedge);
        tabLedge.setAttribute("class", "tabLedge");

        Element list = document.createElement("div");
        appHeader.appendChild(list);
        list.setAttribute("class", "list");

        for (String h : HEADERS) {
            Element a = document.createElement("a");
            list.appendChild(a);
            a.setAttribute("style", "text-decoration:none");
            a.setAttribute("href", "../../" + h + "/index/index.html");

            Element item = document.createElement("div");
            a.appendChild(item);
            if (thisLink != null && thisLink.endsWith(h)) {
                item.setAttribute("class", "itemSelected");

                Element selectedArrow = document.createElement("div");
                item.appendChild(selectedArrow);
                selectedArrow.setAttribute("class", "selectedArrow");

                Element div = document.createElement("div");
                selectedArrow.appendChild(div);
                div.setAttribute("style", "overflow:hidden;box-sizing:border-box;display:inline-block;position:relative;width:68px;height:68px");

                Element img = document.createElement("img");
                img.setAttribute("src", Tools.LINK_URL + "/css/selectArrow.png");
                img.setAttribute("style", "position:absolute;top:0;left:0;bottom:0;right:0;box-sizing:border-box;padding:0;border:none;margin:auto;display:block;width:0;height:0;min-width:100%;max-width:100%;min-height:100%;max-height:100%");
                div.appendChild(img);
            } else {
                item.setAttribute("class", "itemNormal");
            }

            Element iconOffset = document.createElement("div");
            item.appendChild(iconOffset);
            iconOffset.setAttribute("class", "iconOffset");
            {
                Element div = document.createElement("div");
                div.setAttribute("style", "overflow:hidden;box-sizing:border-box;display:inline-block;position:relative;width:32px;height:32px");
                Element img = document.createElement("img");
                img.setAttribute("src", Tools.IMAGE_URL + "/ICON_CIVILOPEDIA_" + h.toUpperCase() + ".png");
                div.appendChild(img);
                iconOffset.appendChild(div);
            }
        }
        return appHeader;
    }

    public static Element getPageListElement (Document document, JSONObject index, File thisFile) {
        Element pageList = document.createElement("div");
        pageList.setAttribute("class", "pageList");

        int d = 0;

        JSONArray folders = index.getJSONArray("folders");
        for (Object f : Tools.sort(folders)) {
            JSONObject folder = (JSONObject) f;
            JSONArray files = folder.getJSONArray("files");
            String tag = "d" + (d++);
            String title = folder.getString("name");

            boolean hidden = false;
            if (folder.containsKey("hidden")) {
                hidden = folder.getBoolean("hidden");
            }
            boolean selected = false;
            if (hidden) {
                selected = true;
            } else {
                for (Object fi : Tools.sort(files)) {
                    JSONObject file = (JSONObject) fi;
                    if (thisFile.getName().equals(file.getString("path"))) {
                        selected = true;
                        break;
                    }
                }
            }

            if (!hidden) {
                Element script = document.createElement("script");
                String scr = "";
                scr += "function " + tag + "_expanded_click() {";
                scr += "document.getElementById(\"" + tag + "_expanded\").style.display = \"none\";";
                scr += "document.getElementById(\"" + tag + "_folded\").style.display = \"\";";
                scr += "}";
                scr += "function " + tag + "_folded_click() {";
                scr += "document.getElementById(\"" + tag + "_expanded\").style.display = \"\";";
                scr += "document.getElementById(\"" + tag + "_folded\").style.display = \"none\";";
                scr += "}";
                script.setTextContent(scr);
                pageList.appendChild(script);
            }

            Element expanded = document.createElement("div");
            expanded.setAttribute("id", tag + "_expanded");
            if (!selected) {
                expanded.setAttribute("style", "display:none;");
            }

            if (!hidden) {
                Element pageTab = document.createElement("dev");
                pageTab.setAttribute("class", "pageTab");
                pageTab.setAttribute("onclick", tag + "_expanded_click()");

                Element pageTabGroup = document.createElement("dev");
                pageTabGroup.setAttribute("class", "pageTabGroup");

                Element pageTabGroupText = document.createElement("dev");
                pageTabGroupText.setAttribute("class", "pageTabGroupText");
                pageTabGroupText.setTextContent(title);
                pageTabGroup.appendChild(pageTabGroupText);

                Element pageTabExpandButton = document.createElement("dev");
                pageTabExpandButton.setAttribute("class", "pageTabExpandButtonSelected");
                pageTabGroup.appendChild(pageTabExpandButton);

                pageTab.appendChild(pageTabGroup);

                expanded.appendChild(pageTab);
            }

            for (Object fi : Tools.sort(files)) {
                JSONObject file = (JSONObject) fi;
                String name = file.getString("name");
                String path = file.getString("path");

                Element a = document.createElement("a");
                expanded.appendChild(a);
                a.setAttribute("style", "text-decoration:none;");
                // reference cross-listings carry an explicit link to the canonical page
                String link = file.getString("link");
                if (link == null) {
                    link = Tools.LINK_URL + "/" + index.getString("path") + "/" + folder.getString("path") + "/" + path.replace(".json", ".html");
                }
                a.setAttribute("href", link);
                
                Element tab = document.createElement("div");
                a.appendChild(tab);
                if (thisFile.getName().equals(path)) {
                    tab.setAttribute("class", "pageTabSelected");
                } else {
                    tab.setAttribute("class", "pageTab");
                }
                
                Element pageTabText = document.createElement("div");
                tab.appendChild(pageTabText);
                pageTabText.setAttribute("class", "pageTabText");
                pageTabText.setTextContent(name);
            }

            pageList.appendChild(expanded);
            
            Element folded = document.createElement("div");
            pageList.appendChild(folded);
            folded.setAttribute("id", tag + "_folded");
            if (selected) {
                folded.setAttribute("style", "display:none;");
            }
            
            Element pageTab = document.createElement("dev");
            folded.appendChild(pageTab);
            pageTab.setAttribute("class", "pageTab");
            pageTab.setAttribute("onclick", tag + "_folded_click()");

            Element pageTabGroup = document.createElement("dev");
            pageTab.appendChild(pageTabGroup);
            pageTabGroup.setAttribute("class", "pageTabGroup");

            Element pageTabGroupText = document.createElement("dev");
            pageTabGroupText.setAttribute("class", "pageTabGroupText");
            pageTabGroupText.setTextContent(title);
            pageTabGroup.appendChild(pageTabGroupText);

            Element pageTabExpandButton = document.createElement("dev");
            pageTabGroup.appendChild(pageTabExpandButton);
            pageTabExpandButton.setAttribute("class", "pageTabExpandButton");
        }

        return pageList;
    }

    public static Element getLeftColumnItem (Document document, JSONObject item) {
        Element leftColumnItem = document.createElement("div");
        leftColumnItem.setAttribute("class", "leftColumnItem");
        switch (item.getString("type")) {
            case "header": {
                Element chapterHeaderContainer = document.createElement("div");
                chapterHeaderContainer.setAttribute("class", "chapterHeaderContainer");

                Element chapterHeaderTitle = document.createElement("div");
                chapterHeaderTitle.setAttribute("class", "chapterHeaderTitle");
                chapterHeaderTitle.setTextContent(item.getString("text"));
                chapterHeaderContainer.appendChild(chapterHeaderTitle);
                
                leftColumnItem.appendChild(chapterHeaderContainer);
                break;
            }
            case "body": {
                String tit = item.getString("title");
                if (tit != null) {
                    Element headerBodyHeaderText = document.createElement("div");
                    headerBodyHeaderText.setAttribute("class", "headerBodyHeaderText");
                    headerBodyHeaderText.setTextContent(item.getString("title"));
                    leftColumnItem.appendChild(headerBodyHeaderText);
                }
                String content = item.getString("text");
                if (content != null) {
                    Element headerBodyHeaderBody = document.createElement("div");
                    headerBodyHeaderBody.setAttribute("class", "headerBodyHeaderBody");
                    headerBodyHeaderBody.setTextContent(content);
                    leftColumnItem.appendChild(headerBodyHeaderBody);
                }
                break;
            }
            case "promotionbox": {
                Element center = document.createElement("div");
                center.setAttribute("style", "display:flex;justify-content:space-around;");

                Element enhancedToolTip = document.createElement("div");
                enhancedToolTip.setAttribute("class", "enhancedToolTip");

                for (int i = 1; i <= 4; i++) {
                    // 0, 1, 2: promotions at left, middle or right
                    JSONObject[] promotions = new JSONObject[3];

                    boolean horizontal = false;

                    // 0 : no line
                    // 1 : m_lr
                    // 2 : lr_m
                    // 3 : vertical
                    // 4 : lr_lr
                    int mid = 0;

                    for (Object o : item.getJSONArray("contents")) {
                        JSONObject object = (JSONObject) o;
                        String type = object.getString("type");
                        if (type != null && type.equals("promotion") && object.getInteger("row") == i) {
                            int column = object.getInteger("column") - 1;
                            // 添加调试日志
                            // System.out.println("Row: " + i + ", Column: " + column);
                            if (column >= 0 && column < promotions.length) {
                                promotions[column] = object;
                            } else {
                                // System.err.println("Invalid column index: " + column);
                            }
                            // promotions[object.getInteger("column") - 1] = object;
                        }
                        if (type != null && type.equals("line") && object.getInteger("row1") == i) {
                            if (object.getInteger("row2") == i) {
                                horizontal = true;
                            } else if (object.getInteger("column1") == 2) {
                                mid = 1;
                            } else if (object.getInteger("column2") == 2) {
                                mid = 2;
                            } else if (object.getInteger("column1") == object.getInteger("column2")
                                    && mid != 4) {
                                mid = 3;
                            } else if (object.getInteger("column1") != object.getInteger("column2")) {
                                mid = 4;
                            }
                        }
                    }

                    if (promotions[0] != null && promotions[2] != null) {
                        Element doubleItem = document.createElement("div");
                        doubleItem.setAttribute("class", "doubleItem");
                        doubleItem.appendChild(getPromotionElement(promotions[0], document));
                        if (horizontal) {
                            Element img = document.createElement("img");
                            img.setAttribute("src", Tools.LINK_URL + "/css/" + "horizontal.png");
                            doubleItem.appendChild(img);
                        }
                        doubleItem.appendChild(getPromotionElement(promotions[2], document));
                        enhancedToolTip.appendChild(doubleItem);
                    } else if (promotions[1] != null) {
                        Element singleItem = document.createElement("div");
                        singleItem.setAttribute("class", "singleItem");
                        singleItem.appendChild(getPromotionElement(promotions[1], document));
                        enhancedToolTip.appendChild(singleItem);
                    }
                    
                    Element div = document.createElement("div");
                    div.setAttribute("style", "display:flex;");

                    Element img = document.createElement("img");
                    String imgName = null;
                    if (mid == 0) {
                        imgName = "empty";
                    } else if (mid == 1) {
                        imgName = "m_lr";
                    } else if (mid == 2) {
                        imgName = "lr_m";
                    } else if (mid == 3) {
                        imgName = "verticle2";
                    } else if (mid == 4) {
                        imgName = "lr_lr";
                    }
                    img.setAttribute("src", Tools.LINK_URL + "/css/" + imgName + ".png");
                    div.appendChild(img);

                    enhancedToolTip.appendChild(div);
                }

                center.appendChild(enhancedToolTip);

                leftColumnItem.appendChild(center);
                break;
            }
            case "governorbox": {
                Element center = document.createElement("div");
                center.setAttribute("style", "display:flex;justify-content:space-around;");

                Element governorsPromotionBox = document.createElement("div");
                governorsPromotionBox.setAttribute("class", "governorsPromotionBox");

                for (int i = 1; i <= 4; i++) {
                    // 0, 1, 2: promotions at left, middle or right
                    JSONObject[] promotions = new JSONObject[3];

                    // 0 : no line
                    // 1 : m_lr
                    // 2 : lr_m
                    // 3 : vertical
                    // 4 : lr_lr
                    int mid = 0;

                    for (Object o : item.getJSONArray("contents")) {
                        JSONObject object = (JSONObject) o;
                        String type = object.getString("type");
                        if (type != null && type.equals("promotion") && object.getInteger("row") == i) {
                            promotions[object.getInteger("column") - 1] = object;
                        }
                        if (type != null && type.equals("line") && object.getInteger("row1") == i) {
                            if (object.getInteger("row2") == i) {
                            } else if (object.getInteger("column1") == 2) {
                                mid = 1;
                            } else if (object.getInteger("column2") == 2) {
                                mid = 2;
                            } else if (object.getInteger("column1") == object.getInteger("column2")
                                    && mid != 4) {
                                mid = 3;
                            } else if (object.getInteger("column1") != object.getInteger("column2")) {
                                mid = 4;
                            }
                        }
                    }

                    if (promotions[0] != null && promotions[2] != null) {
                        Element doubleItem = document.createElement("div");
                        doubleItem.setAttribute("class", "doubleItem");
                        doubleItem.appendChild(getGovernorElement(promotions[0], document));
                        doubleItem.appendChild(getGovernorElement(promotions[2], document));
                        governorsPromotionBox.appendChild(doubleItem);
                    } else if (promotions[1] != null) {
                        Element singleItem = document.createElement("div");
                        singleItem.setAttribute("class", "singleItem");
                        singleItem.appendChild(getGovernorElement(promotions[1], document));
                        governorsPromotionBox.appendChild(singleItem);
                    }
                    
                    Element div = document.createElement("div");
                    div.setAttribute("style", "display:flex;");

                    Element img = document.createElement("img");
                    String imgName = null;
                    if (mid == 0) {
                        imgName = "empty";
                    } else if (mid == 1) {
                        imgName = "m_lr";
                    } else if (mid == 2) {
                        imgName = "lr_m";
                    } else if (mid == 3) {
                        imgName = "verticle2";
                    } else if (mid == 4) {
                        imgName = "lr_lr";
                    }
                    img.setAttribute("src", Tools.LINK_URL + "/css/" + imgName + ".png");
                    div.appendChild(img);

                    governorsPromotionBox.appendChild(div);
                }

                center.appendChild(governorsPromotionBox);

                leftColumnItem.appendChild(center);
                break;
            }
            case "quote": {
                Element quoteBox = document.createElement("div");
                quoteBox.setAttribute("class", "pediaQuote");

                Element quoteText = document.createElement("div");
                quoteText.setAttribute("class", "pediaQuoteText");
                quoteText.setTextContent(item.getString("text"));
                quoteBox.appendChild(quoteText);

                leftColumnItem.appendChild(quoteBox);
                break;
            }
        }
        return leftColumnItem;

    }

    public static Element getPortrait (Document document, JSONObject item) {
        Element portrait = document.createElement("div");
        portrait.setAttribute("class", "rightColumnItem");

        Element portraitSquareMargin = document.createElement("div");
        portrait.appendChild(portraitSquareMargin);
        portraitSquareMargin.setAttribute("class", "portraitSquareMargin");
        
        Element div1 = document.createElement("div");
        portraitSquareMargin.appendChild(div1);
        div1.setAttribute("style", "display:block;overflow:hidden;position:relative;box-sizing:border-box;margin:0");
        
        Element div2 = document.createElement("div");
        div1.appendChild(div2);
        div2.setAttribute("style", "display:block;box-sizing:border-box;padding-top:96.92307692307692%");

        Element img1 = document.createElement("img");
        div1.appendChild(img1);
        img1.setAttribute("src", Tools.LINK_URL + "/css/portraitsquare.png");
        img1.setAttribute("style", "visibility: inherit; position: absolute; inset: 0px; box-sizing: border-box; padding: 0px; border: none; margin: auto; display: block; width: 0px; height: 0px; min-width: 100%; max-width: 100%; min-height: 100%; max-height: 100%;");
        
        Element portraitContent = document.createElement("div");
        portraitSquareMargin.appendChild(portraitContent);
        portraitContent.setAttribute("class", "portraitContent");
        
        Element div3 = document.createElement("div");
        portraitContent.appendChild(div3);
        div3.setAttribute("style", "display:inline-block;max-width:100%;overflow:hidden;position:relative;box-sizing:border-box;margin:0");
        
        Element div4 = document.createElement("div");
        div3.appendChild(div4);
        div4.setAttribute("style", "box-sizing:border-box;display:block;max-width:100%");
        
        Element img2 = document.createElement("img");
        div4.appendChild(img2);
        img2.setAttribute("style", "max-width:100%;display:block;margin:0;border:none;padding:0");
        img2.setAttribute("aria-hidden", "true");
        img2.setAttribute("role", "presentation");
        img2.setAttribute("src", Tools.LINK_URL + "/css/blank.png");
                            
        String src = item.getString("src");
        if (src != null && !src.isEmpty()) {
            Element img = document.createElement("img");
            div4.appendChild(img);
            String alt = item.getString("alt");
            if (alt != null) {
                img.setAttribute("alt", alt);
            }
            img.setAttribute("src", src);
            img.setAttribute("style", "visibility: inherit; position: absolute; inset: 0px; box-sizing: border-box; padding: 0px; border: none; margin: auto; display: block; width: 0px; height: 0px; min-width: 100%; max-width: 100%; min-height: 100%; max-height: 100%; object-fit: contain;");
        }

        return portrait;
    }

    public static Element getRightColumnItem (Document document, JSONObject item) {
        Element rightColumnItem = document.createElement("div");
        
        rightColumnItem.setAttribute("class", "rightColumnItem");
        switch (item.getString("type")) {
            case "statbox": {
                Element statBoxFrame = document.createElement("div");
                statBoxFrame.setAttribute("class", "statBoxFrame");
                
                String title = item.getString("title");
                if (title != null) {
                    Element statBoxTitle = document.createElement("div");
                    statBoxTitle.setAttribute("class", "statBoxTitle");
                    statBoxTitle.setTextContent(title);
                    statBoxFrame.appendChild(statBoxTitle);
                }
                JSONArray contents = item.getJSONArray("contents");
                for (Object c : contents) {
                    JSONObject content = (JSONObject) c;
                    Element statBoxComponent = document.createElement("div");
                    statBoxComponent.setAttribute("class", "statBoxComponent");
                    switch (content.getString("type")) {
                        case "separator": {
                            Element separator = document.createElement("div");
                            separator.setAttribute("class", "separator");
                            statBoxComponent.appendChild(separator);
                            break;
                        }
                        case "header": {
                            Element statBoxHeaderText = document.createElement("div");
                            statBoxHeaderText.setAttribute("class", "statBoxHeaderText");
                            statBoxHeaderText.setTextContent(content.getString("text"));
                            statBoxComponent.appendChild(statBoxHeaderText);
                            break;
                        }
                        case "iconlabel": {
                            Element iconLabelContainer = document.createElement("div");
                            iconLabelContainer.setAttribute("class", "iconLabelContainer");
                            
                            Element iconLabelImage = document.createElement("dev");
                            iconLabelImage.setAttribute("style", "overflow:hidden;box-sizing:border-box;display:inline-block;position:relative;width:50px;height:50px");
                            
                            Element div = document.createElement("div");
                            div.setAttribute("style", "overflow:hidden;box-sizing:border-box;display:inline-block;position:relative;width:50px;height:50px");
                            
                            String src = content.getString("src");
                            if (src != null && !src.isEmpty()) {
                                Element img = document.createElement("img");
                                String alt = content.getString("alt");
                                    if (alt != null) {
                                        img.setAttribute("alt", alt);
                                    }
                                    img.setAttribute("src", content.getString("src"));
                                    img.setAttribute("style", "visibility: inherit; position: absolute; inset: 0px; box-sizing: border-box; padding: 0px; border: none; margin: auto; display: block; width: 0px; height: 0px; min-width: 100%; max-width: 100%; min-height: 100%; max-height: 100%;");
                                    div.appendChild(img);
                            }
                            
                            iconLabelImage.appendChild(div);
                    
                            iconLabelContainer.appendChild(iconLabelImage);

                            String number = content.getString("num");
                            if (number != null) {
                                Element iconLabelNumber = document.createElement("div");
                                iconLabelNumber.setAttribute("class", "iconLabelNumber");
                                iconLabelNumber.setTextContent(number);
                                iconLabelContainer.appendChild(iconLabelNumber);
                            }

                            Element iconLabelCaption = document.createElement("div");
                            iconLabelCaption.setAttribute("class", "iconLabelCaption");
                            iconLabelCaption.setTextContent(content.getString("text"));
                            iconLabelContainer.appendChild(iconLabelCaption);
                            
                            String lk = content.getString("link");
                            if (lk != null && !lk.isEmpty()) {
                                Element a = document.createElement("a");
                                a.setAttribute("href", lk);
                                a.setAttribute("style", "text-decoration: none;");
                                a.appendChild(iconLabelContainer);
                                statBoxComponent.appendChild(a);
                            } else {
                                statBoxComponent.appendChild(iconLabelContainer);
                            }
                            break;
                        }
                        case "label": {
                            Element statBoxLabel = document.createElement("dev");
                            statBoxLabel.setAttribute("class", "statBoxLabel");
                            statBoxLabel.setTextContent(content.getString("text"));
                            statBoxComponent.appendChild(statBoxLabel);
                            break;
                        }
                    }
                    statBoxFrame.appendChild(statBoxComponent);
                }
                
                rightColumnItem.appendChild(statBoxFrame);
                break;
            }
        }
        return rightColumnItem;      
    }

    public static Document convertSingleHtml(JSONObject page, File source, JSONObject index) throws Exception {
        // create document
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element root = document.createElement("html");
        document.appendChild(root);
        
        Element head = document.createElement("head");
        root.appendChild(head);

        Element meta = document.createElement("meta");
        meta.setAttribute("charset", "UTF-8");

        Element title = document.createElement("title");
        head.appendChild(title);
        title.setTextContent(page.getString("title"));

        Element link = document.createElement("link");
        head.appendChild(link);
        link.setAttribute("rel", "stylesheet");
        link.setAttribute("type", "text/css");
        link.setAttribute("href", Tools.LINK_URL + "/css/styles.css");

        Element body = document.createElement("body");
        root.appendChild(body);
        body.setAttribute("style", "margin:0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;");

        Element appBackground = document.createElement("div");
        body.appendChild(appBackground);
        appBackground.setAttribute("class", "appBackground");

        Element contentWithSidebar = document.createElement("div");
        appBackground.appendChild(contentWithSidebar);
        contentWithSidebar.setAttribute("class", "contentWithSideBar");

        Element app = document.createElement("div");
        contentWithSidebar.appendChild(app);
        app.setAttribute("class", "app");

        Element appbody = document.createElement("div");
        app.appendChild(appbody);
        appbody.setAttribute("class", "body");

        Element appHeader = getHeaderElement(document, index.getString("path"));
        appbody.appendChild(appHeader);
        appHeader.appendChild(getSearchBox(document, page.getString("language")));

        Element appContent = document.createElement("div");
        appbody.appendChild(appContent);
        appContent.setAttribute("class", "appContent");

        Element pageList = getPageListElement(document, index, source);
        appContent.appendChild(pageList);

        Element pageMain = document.createElement("div");
        appContent.appendChild(pageMain);
        pageMain.setAttribute("class", "page");

        Element pageFrame = document.createElement("div");
        pageMain.appendChild(pageFrame);
        pageFrame.setAttribute("class", "pageFrame");
        
        Element pageHeaderContainer = document.createElement("div");
        pageFrame.appendChild(pageHeaderContainer);
        pageHeaderContainer.setAttribute("class", "pageHeaderContainer");

        {
            Element div = document.createElement("div");
            pageHeaderContainer.appendChild(div);
            div.setAttribute("style", "display:block;overflow:hidden;position:absolute;top:0;left:0;bottom:0;right:0;box-sizing:border-box;margin:0");

            Element img = document.createElement("img");
            div.appendChild(img);
            img.setAttribute("src", Tools.LINK_URL + "/css/civilopedia_pageheader.png");
            img.setAttribute("style", "visibility: inherit; position: absolute; inset: 0px; box-sizing: border-box; padding: 0px; border: none; margin: auto; display: block; width: 0px; height: 0px; min-width: 100%; max-width: 100%; min-height: 100%; max-height: 100%; object-fit: fill;");
        }

        Element pageHeaderText = document.createElement("div");
        pageHeaderContainer.appendChild(pageHeaderText);
        pageHeaderText.setAttribute("class", "pageHeaderText");
        pageHeaderText.setTextContent(page.getString("title"));

        Element pageContent = document.createElement("div");
        pageFrame.appendChild(pageContent);
        pageContent.setAttribute("class", "pageContent");
        
        JSONArray leftColumnItems = page.getJSONArray("leftColumnItems");
        if (leftColumnItems != null) {
            Element pageLeftColumn = document.createElement("div");
            pageContent.appendChild(pageLeftColumn);
            pageLeftColumn.setAttribute("class", "pageLeftColumn");
            for (Object l : leftColumnItems) {
                JSONObject item = (JSONObject) l;
                Element leftColumnItem = getLeftColumnItem(document, item);
                pageLeftColumn.appendChild(leftColumnItem);
            }
        }

        JSONArray rightColumnItems = page.getJSONArray("rightColumnItems");
        JSONObject portrait = page.getJSONObject("portrait");
        if (portrait != null || rightColumnItems != null) {
            Element pageRightColumn = document.createElement("div");
            pageContent.appendChild(pageRightColumn);
            pageRightColumn.setAttribute("class", "pageRightColumn");
            if (portrait != null) {
                pageRightColumn.appendChild(getPortrait(document, portrait));
            }
            if (rightColumnItems != null) {
                for (Object r : rightColumnItems) {
                    JSONObject item = (JSONObject) r;
                    Element rightColumnItem = getRightColumnItem(document, item);
                    pageRightColumn.appendChild(rightColumnItem);
                }
            }
        }


        Element pageFooter = document.createElement("div");
        pageFrame.appendChild(pageFooter);
        pageFooter.setAttribute("class", "pageFooter");

        Element pageFooterImage = document.createElement("div");
        pageFooter.appendChild(pageFooterImage);
        pageFooterImage.setAttribute("class", "pageFooterImage");
        {
            Element div = document.createElement("div");
            div.setAttribute("style", "overflow:hidden;box-sizing:border-box;display:inline-block;position:relative;width:193px;height:122px");
            
            Element img = document.createElement("img");
            img.setAttribute("src", Tools.LINK_URL + "/css/compass.png");
            img.setAttribute("style", "visibility: inherit; position: absolute; inset: 0px; box-sizing: border-box; padding: 0px; border: none; margin: auto; display: block; width: 0px; height: 0px; min-width: 100%; max-width: 100%; min-height: 100%; max-height: 100%;");
            div.appendChild(img);
            
            pageFooterImage.appendChild(div);
        }

        Element pageFooterDivider = document.createElement("div");
        pageFooter.appendChild(pageFooterDivider);
        pageFooterDivider.setAttribute("class", "pageFooterDivider");

        Element sidebarContainer = document.createElement("div");
        contentWithSidebar.appendChild(sidebarContainer);
        sidebarContainer.setAttribute("class", "sidebarContainer");

        Element sidebarText = document.createElement("sidebarText");
        sidebarContainer.appendChild(sidebarText);
        sidebarText.setAttribute("class", "sidebarText");
        sidebarText.setTextContent(Tools.getControlText("Language", page.getString("language")));

        Element script = document.createElement("script");
        sidebarContainer.appendChild(script);
        String s = "";
        s += "function change() {";
        s += "var link = window.location.href;";
        s += "var select = document.getElementById(\"select\");";
        s += "var lan = \"\";";
        s += "for (i = 0; i < select.length; i++) {";
        s += "if (select.options[i].selected) {";
        s += "lan = select.options[i].value;";
        s += "}";
        s += "}";
        s += "var newlink = link.replace(/^(.*\\/)(.*)((\\/.*){3})$/, \"$1\" + lan + \"$3\");";
        s += "window.location.href = newlink;";
        s += "}";
        script.setTextContent(s);

        Element select = document.createElement("select");
        sidebarContainer.appendChild(select);
        select.setAttribute("onchange", "change()");
        select.setAttribute("id", "select");

        for (String language : LANGUAGES) {
            Element option = document.createElement("option");
            select.appendChild(option);
            option.setAttribute("value", language);
            option.setTextContent(Tools.getControlText("Self", language));
            if (language.equals(page.getString("language"))) {
                option.setAttribute("selected", "true");
            }
        }

        appendSearchScripts(document, body);

        return document;
    }

    public static void convertSingleHtml(File source, File target, JSONObject index) throws Exception {
        // read json from file
        InputStream in = new FileInputStream(source);
        byte[] buf = new byte[in.available()];
        in.read(buf);
        in.close();
        String text = new String(buf, "UTF-8");
        JSONObject page = JSON.parseObject(text);
        page = (JSONObject) analyzeBrackets(page);

        Document document = convertSingleHtml(page, source, index);
        writeDocumentToFile(document, target);
    }

    public static Document convertAndroidSingleHTML(JSONObject page) throws Exception {
        // create document
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        
        Element root = document.createElement("html");
        document.appendChild(root);
        
        Element head = document.createElement("head");
        root.appendChild(head);
        
        Element meta = document.createElement("meta");
        meta.setAttribute("charset", "UTF-8");
        
        Element title = document.createElement("title");
        head.appendChild(title);
        title.setTextContent(page.getString("title"));
        
        Element link = document.createElement("link");
        head.appendChild(link);
        link.setAttribute("rel", "stylesheet");
        link.setAttribute("type", "text/css");
        link.setAttribute("href", Tools.LINK_URL + "/css/styles.css");
        
        Element body = document.createElement("body");
        root.appendChild(body);
        body.setAttribute("style", "margin:0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;");
        
        Element appBackground = document.createElement("div");
        body.appendChild(appBackground);
        appBackground.setAttribute("class", "appBackground_android");
        
        Element appbody = document.createElement("div");
        //app.appendChild(appbody);
        appBackground.appendChild(appbody);
        appbody.setAttribute("class", "body_android");
        
        appbody.appendChild(getSearchBox(document, page.getString("language")));

        Element appContent = document.createElement("div");
        appbody.appendChild(appContent);
        appContent.setAttribute("class", "appContent");

        Element pageMain = document.createElement("div");
        appContent.appendChild(pageMain);
        pageMain.setAttribute("class", "page");

        Element pageFrame = document.createElement("div");
        pageMain.appendChild(pageFrame);
        pageFrame.setAttribute("class", "pageFrame");

        Element pageHeaderContainer = document.createElement("div");
        pageFrame.appendChild(pageHeaderContainer);
        pageHeaderContainer.setAttribute("class", "pageHeaderContainer");
        
        {
            Element div = document.createElement("div");
            pageHeaderContainer.appendChild(div);
            div.setAttribute("style", "display:block;overflow:hidden;position:absolute;top:0;left:0;bottom:0;right:0;box-sizing:border-box;margin:0");
        
            Element img = document.createElement("img");
            div.appendChild(img);
            img.setAttribute("src", Tools.LINK_URL + "/css/civilopedia_pageheader.png");
            img.setAttribute("style", "visibility: inherit; position: absolute; inset: 0px; box-sizing: border-box; padding: 0px; border: none; margin: auto; display: block; width: 0px; height: 0px; min-width: 100%; max-width: 100%; min-height: 100%; max-height: 100%; object-fit: fill;");
        }
        
        Element pageHeaderText = document.createElement("div");
        pageHeaderContainer.appendChild(pageHeaderText);
        pageHeaderText.setAttribute("class", "pageHeaderText");
        pageHeaderText.setTextContent(page.getString("title"));
        
        Element pageContent = document.createElement("div");
        pageFrame.appendChild(pageContent);
        pageContent.setAttribute("class", "pageContent");
        
        Element pageLeftColumn = document.createElement("div");
        pageContent.appendChild(pageLeftColumn);
        pageLeftColumn.setAttribute("class", "pageLeftColumn");
        
        JSONObject portrait = page.getJSONObject("portrait");
        if (portrait != null) {
            pageLeftColumn.appendChild(getPortrait(document, portrait));
        }
        
        JSONArray leftColumnItems = page.getJSONArray("leftColumnItems");
        if (leftColumnItems != null) {
            for (Object l : leftColumnItems) {
                JSONObject item = (JSONObject) l;
                Element leftColumnItem = getLeftColumnItem(document, item);
                pageLeftColumn.appendChild(leftColumnItem);
            }
        }
        
        Element tab = document.createElement("div");
        pageLeftColumn.appendChild(tab);
        tab.setAttribute("style", "min-height:20px");
        
        JSONArray rightColumnItems = page.getJSONArray("rightColumnItems");
        if (rightColumnItems != null) {
            for (Object r : rightColumnItems) {
                JSONObject item = (JSONObject) r;
                Element rightColumnItem = getRightColumnItem(document, item);
                pageLeftColumn.appendChild(rightColumnItem);
            }
        }
        
        Element pageFooter = document.createElement("div");
        pageFrame.appendChild(pageFooter);
        pageFooter.setAttribute("class", "pageFooter");
        
        Element pageFooterImage = document.createElement("div");
        pageFooter.appendChild(pageFooterImage);
        pageFooterImage.setAttribute("class", "pageFooterImage");
        {
            Element div = document.createElement("div");
            div.setAttribute("style", "overflow:hidden;box-sizing:border-box;display:inline-block;position:relative;width:193px;height:122px");
            
            Element img = document.createElement("img");
            img.setAttribute("src", Tools.LINK_URL + "/css/compass.png");
            img.setAttribute("style", "visibility: inherit; position: absolute; inset: 0px; box-sizing: border-box; padding: 0px; border: none; margin: auto; display: block; width: 0px; height: 0px; min-width: 100%; max-width: 100%; min-height: 100%; max-height: 100%;");
            div.appendChild(img);
            
            pageFooterImage.appendChild(div);
        }
        
        Element pageFooterDivider = document.createElement("div");
        pageFooter.appendChild(pageFooterDivider);
        pageFooterDivider.setAttribute("class", "pageFooterDivider");

        appendSidebar(document, body);
        appendSearchScripts(document, body);

        return document;
    }

    /**
     * Reused across the ~9750 pages of a run: building the factory is the expensive part, and it
     * was being rebuilt for every single page. The Transformer itself is still per call, since one
     * is not safe to reuse.
     */
    private static final TransformerFactory TRANSFORMERS = TransformerFactory.newInstance();

    public static void writeDocumentToFile (Document document, File target) throws Exception {
        Transformer transformer = TRANSFORMERS.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        String xml = writer.toString();
        String html = "<!DOCTYPE html>\n" + xml;
        // literal replace, not replaceAll: neither of these is a pattern, and this ran twice per
        // page over the whole document
        html = html.replace("&lt;", "<");
        html = html.replace("&gt;", ">");

        // the parent, not the file itself -- this used to mkdirs() the html path, creating a
        // directory where the page goes and then deleting it again
        target.getParentFile().mkdirs();
        if (target.exists()) {
            target.delete();
        }
        OutputStream out = new FileOutputStream(target);
        out.write(html.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    public static void convertAndroidSingleHTML(File source, File target) throws Exception {
        // read json from file
        InputStream in = new FileInputStream(source);
        byte[] buf = new byte[in.available()];
        in.read(buf);
        in.close();
        String text = new String(buf, "UTF-8");
        JSONObject page = JSON.parseObject(text);
        page = (JSONObject) analyzeBrackets(page);

        Document document = convertAndroidSingleHTML(page);
        writeDocumentToFile(document, target);
    }

    public static void convertChapter(File sourceFolder, File targetFolder) throws Exception {
        if (!sourceFolder.exists()) {
            return;
        }
        JSONObject index = new JSONObject();
        index.put("folders", new JSONArray());
        for (File indexFile : sourceFolder.listFiles()) {
            if (indexFile.isFile() && indexFile.getName().endsWith(".json")) {
                InputStream in = new FileInputStream(indexFile);
                byte[] buf = new byte[in.available()];
                in.read(buf);
                in.close();
                String text = new String(buf, "UTF-8");
                JSONObject extra = JSON.parseObject(text);
                addJson(index, extra, true);
            }
        }
        JSONArray folders = index.getJSONArray("folders");
        for (Object f : Tools.sort(folders)) {
            JSONObject folder = (JSONObject) f;
            String path = folder.getString("path");
            JSONArray files = folder.getJSONArray("files");
            for (Object fi : Tools.sort(files)) {
                JSONObject file = (JSONObject) fi;
                // reference entries are cross-listings of a page that lives in another
                // folder; the single canonical page is rendered from its own folder
                if (file.getBooleanValue("reference")) {
                    continue;
                }
                String subPath = file.getString("path");
                File source = new File(sourceFolder, path + "/" + subPath);
                File target = new File(targetFolder, path + "/" + subPath.replace(".json", ".html"));
                convertSingleHtml(source, target, index);
            }
        }
    }

    public static void convertAndroidChapter(File sourceFolder, File targetFolder) throws Exception {
        if (!sourceFolder.exists()) {
            return;
        }
        JSONObject index = new JSONObject();
        index.put("folders", new JSONArray());
        for (File indexFile : sourceFolder.listFiles()) {
            if (indexFile.isFile() && indexFile.getName().endsWith(".json")) {
                InputStream in = new FileInputStream(indexFile);
                byte[] buf = new byte[in.available()];
                in.read(buf);
                in.close();
                String text = new String(buf, "UTF-8");
                JSONObject extra = JSON.parseObject(text);
                addJson(index, extra, true);
            }
        }

        JSONObject tocPage = new JSONObject();
        tocPage.put("title", index.getString("name"));
        // the chapter's index path is "{lang}/{chapter}"; carry the language so the
        // sidebar / search box localise correctly on this TOC page
        String tocIndexPath = index.getString("path");
        if (tocIndexPath != null && tocIndexPath.contains("/")) {
            tocPage.put("language", tocIndexPath.substring(0, tocIndexPath.indexOf("/")));
        }
        JSONArray rightColumnItems = new JSONArray();
        tocPage.put("rightColumnItems", rightColumnItems);

        JSONArray folders = index.getJSONArray("folders");
        for (Object f : Tools.sort(folders)) {
            JSONObject folder = (JSONObject) f;
            String path = folder.getString("path");
            JSONArray files = folder.getJSONArray("files");

            JSONArray contents = new JSONArray();
            String folderName = folder.getString("name");
            rightColumnItems.add(Tools.getStatbox(folderName, contents));
            if (folderName != null && !folderName.isEmpty()) {
                contents.add(Tools.getSeparator());
            }

            for (Object fi : Tools.sort(files)) {
                JSONObject file = (JSONObject) fi;
                String name = file.getString("name");
                String subPath = file.getString("path");
                // reference entries link to the canonical page rendered under its own folder
                if (!file.getBooleanValue("reference")) {
                    File source = new File(sourceFolder, path + "/" + subPath);
                    File target = new File(targetFolder, path + "/" + subPath.replace(".json", ".html"));
                    convertAndroidSingleHTML(source, target);
                }

                JSONObject iconlabel = file.getJSONObject("iconlabel");
                if (iconlabel != null) {
                    contents.add(iconlabel);
                } else {
                    contents.add(Tools.getIconlabel(Tools.LINK_URL + "/" + index.getString("path") + "/" + path + "/" + subPath.replaceAll(".json", ".html"), "", "", name));
                }
            }
        }

        Document document = convertAndroidSingleHTML(tocPage);
        File target = new File(targetFolder, "index/toc.html");
        writeDocumentToFile(document, target);
    }

    // ---- client-side search -------------------------------------------------

    /** Search box UI (input + results dropdown). Shared by web and android pages. */
    public static Element getSearchBox (Document document, String language) {
        Element container = document.createElement("div");
        container.setAttribute("class", "pediaSearch");

        Element input = document.createElement("input");
        container.appendChild(input);
        input.setAttribute("id", "pediaSearchInput");
        input.setAttribute("type", "text");
        input.setAttribute("autocomplete", "off");
        // some generated pages (android chapter TOCs) carry no language field
        String lang = (language == null || language.isEmpty()) ? "en_US" : language;
        input.setAttribute("placeholder", Tools.getControlText("Search", lang));

        Element results = document.createElement("div");
        container.appendChild(results);
        results.setAttribute("id", "pediaSearchResults");
        results.setAttribute("class", "pediaSearchResults");
        // a text node keeps the XML transformer from self-closing the empty div
        results.setTextContent(" ");

        return container;
    }

    // floating hamburger button for the android pages (see 需求7)
    public static Element getSidebarToggle (Document document) {
        Element btn = document.createElement("div");
        btn.setAttribute("id", "pediaSidebarToggle");
        btn.setAttribute("class", "pediaSidebarToggle");
        btn.setAttribute("onclick", "pediaToggleSidebar()");
        for (int i = 0; i < 3; i++) {
            Element bar = document.createElement("div");
            btn.appendChild(bar);
            bar.setAttribute("class", "pediaSidebarBar");
        }
        return btn;
    }

    /**
     * Writes output_android/{language}/sidebar.js: the slide-in chapter list plus its open/close
     * behaviour. Both used to be inlined into every page, which repeated the same ~4 KB into all
     * 6000+ android pages — about a fifth of the mobile build, and a second copy of the script to
     * keep in sync. Hrefs and icon srcs resolve against the page that loads this file, not against
     * the file itself, so the "../../" depths are the same as when the markup was inlined.
     */
    public static void buildSidebarScript (String language) throws Exception {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"pediaSidebar\" id=\"pediaSidebar\">");

        // "index" is not one of HEADERS, so without this row the sidebar can reach every
        // chapter but never the pedia's own start page
        html.append("<a class=\"pediaSidebarItem pediaSidebarHome\" href=\"../../index/index/toc.html\">")
                .append(Tools.getControlText("PediaHome", language))
                .append("</a>");

        for (String chapter : HEADERS) {
            html.append("<a class=\"pediaSidebarItem\" href=\"../../").append(chapter).append("/index/toc.html\">")
                    .append("<img class=\"pediaSidebarIcon\" alt=\"\" src=\"")
                    .append(Tools.IMAGE_URL).append("/ICON_CIVILOPEDIA_").append(chapter.toUpperCase()).append(".png\">")
                    .append("<span>").append(Tools.getControlText(chapter, language)).append("</span>")
                    .append("</a>");
        }
        html.append("</div>");
        html.append("<div class=\"pediaSidebarOverlay\" id=\"pediaSidebarOverlay\"></div>");

        // The overlay's click handler closes the sidebar on a normal browser, but a WebView that
        // does not synthesise a click from a tap would strand the user with no way back, so bind
        // touchstart too. Opening also pushes a history entry, letting the phone's back button
        // close the sidebar instead of leaving the page.
        String js = "(function(){"
                + "var wrap=document.createElement('div');"
                + "wrap.innerHTML='" + toJsString(html.toString()) + "';"
                + "while(wrap.firstChild){document.body.appendChild(wrap.firstChild);}"
                + "var s=document.getElementById('pediaSidebar');"
                + "var o=document.getElementById('pediaSidebarOverlay');"
                + "function set(open){"
                + "if(open===s.classList.contains('open')){return;}"
                + "s.classList.toggle('open',open);"
                + "o.style.display=open?'block':'none';}"
                + "window.pediaToggleSidebar=function(){"
                + "var open=!s.classList.contains('open');"
                + "set(open);"
                + "try{if(open){history.pushState({pediaSidebar:1},'');}"
                + "else if(history.state&&history.state.pediaSidebar){history.back();}}catch(e){}};"
                + "o.addEventListener('click',function(){window.pediaToggleSidebar();});"
                + "o.addEventListener('touchstart',function(e){e.preventDefault();window.pediaToggleSidebar();},false);"
                + "window.addEventListener('popstate',function(){set(false);});"
                + "})();";
        writeTextToFile(js, new File("output_android/" + language + "/sidebar.js"));
    }

    // escapes a string for a single-quoted JS literal; non-ASCII becomes \\uXXXX so the
    // generated .js is charset-independent (search-data.js relies on the same property)
    static String toJsString (String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '\'') {
                out.append('\\').append(c);
            } else if (c < 128) {
                out.append(c);
            } else {
                out.append(String.format("\\u%04x", (int) c));
            }
        }
        return out.toString();
    }

    // appends the hamburger and a reference to the per-language sidebar script; the sidebar
    // and its overlay are injected by that script (see buildSidebarScript). The button stays
    // in the markup so the affordance is there before the script runs.
    public static void appendSidebar (Document document, Element body) {
        body.appendChild(getSidebarToggle(document));

        Element script = document.createElement("script");
        body.appendChild(script);
        // "../../" reaches the language dir (output_android/{lang}/) from every page
        script.setAttribute("src", "../../sidebar.js");
        script.setTextContent(" "); // force a closing tag; empty <script src/> breaks HTML parsing
    }

    /** Appends the per-language data file and shared behaviour script to a page body. */
    public static void appendSearchScripts (Document document, Element body) {
        // "../../" reaches the language dir (output/{lang}/) from every page (all are 3 dirs deep)
        Element data = document.createElement("script");
        body.appendChild(data);
        data.setAttribute("src", "../../search-data.js");
        data.setTextContent(" "); // force a closing tag; empty <script src/> breaks HTML parsing

        Element behaviour = document.createElement("script");
        body.appendChild(behaviour);
        behaviour.setAttribute("src", Tools.LINK_URL + "/search.js");
        behaviour.setTextContent(" ");
    }

    /**
     * Builds output/{language}/search-data.js (and the android copy) from the staged
     * temp/{language}/{chapter} index files. Mirrors convertChapter's index merge so the
     * index lists exactly the pages that get generated. Each entry: t=title, u=link,
     * i=icon (optional), c=localized chapter name, p=pinyin (optional).
     */
    public static void buildSearchIndex (String language) throws Exception {
        JSONArray searchIndex = new JSONArray();
        for (String chapter : HEADERS) {
            File chapterFolder = new File("temp/" + language + "/" + chapter);
            if (!chapterFolder.exists()) {
                continue;
            }
            JSONObject index = new JSONObject();
            index.put("folders", new JSONArray());
            File[] indexFiles = chapterFolder.listFiles();
            if (indexFiles == null) {
                continue;
            }
            for (File indexFile : indexFiles) {
                if (indexFile.isFile() && indexFile.getName().endsWith(".json")) {
                    InputStream in = new FileInputStream(indexFile);
                    byte[] buf = new byte[in.available()];
                    in.read(buf);
                    in.close();
                    JSONObject extra = JSON.parseObject(new String(buf, "UTF-8"));
                    addJson(index, extra, true);
                }
            }
            String indexPath = index.getString("path");
            if (indexPath == null) {
                indexPath = language + "/" + chapter;
            }
            String category = Tools.getControlText(chapter, language);
            JSONArray folders = index.getJSONArray("folders");
            if (folders == null) {
                continue;
            }
            for (Object f : folders) {
                JSONObject folder = (JSONObject) f;
                String folderPath = folder.getString("path");
                JSONArray files = folder.getJSONArray("files");
                if (files == null) {
                    continue;
                }
                for (Object fi : files) {
                    JSONObject file = (JSONObject) fi;
                    // skip reference cross-listings so each page is indexed once
                    if (file.getBooleanValue("reference")) {
                        continue;
                    }
                    String name = file.getString("name");
                    if (name == null || name.isEmpty()) {
                        continue;
                    }
                    String title = searchTitle(name);
                    JSONObject entry = new JSONObject(true);
                    entry.put("t", title);
                    entry.put("c", category);
                    JSONObject iconlabel = file.getJSONObject("iconlabel");
                    if (iconlabel != null) {
                        entry.put("u", iconlabel.getString("link"));
                        entry.put("i", iconlabel.getString("src"));
                    } else {
                        String subPath = file.getString("path");
                        entry.put("u", Tools.LINK_URL + "/" + indexPath + "/" + folderPath + "/" + subPath.replace(".json", ".html"));
                    }
                    // p=pinyin, syllables space separated; absent for titles with nothing Chinese
                    // in them, so the en index is unaffected. See tools/Pinyin.java.
                    String pinyin = tools.Pinyin.of(title);
                    if (pinyin != null) {
                        entry.put("p", pinyin);
                    }
                    searchIndex.add(entry);
                }
            }
        }
        // BrowserCompatible escapes non-ASCII to ASCII unicode escapes, so the file is charset-independent
        String js = "window.SEARCH_INDEX=" + JSON.toJSONString(searchIndex, SerializerFeature.BrowserCompatible) + ";";
        writeTextToFile(js, new File("output/" + language + "/search-data.js"));
        writeTextToFile(js, new File("output_android/" + language + "/search-data.js"));
    }

    /**
     * Strips [ICON_*]/[COLOR]/... markup tokens so a title reads cleanly and matching runs
     * against visible text only. Falls back to the raw name when nothing is left, which happens
     * for entries whose whole name is an icon token.
     */
    static String searchTitle (String name) {
        String title = name.replaceAll("\\[[^\\]]*\\]", "").replaceAll("\\s+", " ").trim();
        return title.isEmpty() ? name : title;
    }

    public static void writeTextToFile (String text, File target) throws Exception {
        target.getParentFile().mkdirs();
        if (target.exists()) {
            target.delete();
        }
        OutputStream out = new FileOutputStream(target);
        out.write(text.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    /** Runs a step and prints how long it took, matching Main's [TIME] lines. */
    interface Step {
        void run () throws Exception;
    }

    static void timed (String name, Step step) throws Exception {
        long start = System.nanoTime();
        step.run();
        System.out.printf("[TIME] %-22s %8.2fs%n", name, (System.nanoTime() - start) / 1e9);
    }

    public static void convertAll() throws Exception {
        timed("stage temp", () -> {
            copyFiles(new File("json"), new File("temp"));
            copyFiles(new File("manual/json"), new File("temp"));
        });
        timed("clear output", () -> {
            deleteFilesExcept(new File("output"), "icons");
            // output_android was never cleared, so pages for content the mod has since renamed or
            // removed stayed behind for good; this first sweep dropped 1570 of them
            deleteFilesExcept(new File("output_android"), "icons");
        });
        timed("seed android", () -> copyFiles(new File("output"), new File("output_android")));
        long pages = System.nanoTime();
        long desktop = 0, android = 0;
        for (String language : LANGUAGES) {

            JSONObject tocPage = new JSONObject();
            tocPage.put("title", Tools.getControlText("Civ6 HD Official Wiki", language));
            tocPage.put("language", language);
            JSONArray rightColumnItems = new JSONArray();
            tocPage.put("rightColumnItems", rightColumnItems);
            JSONArray contents = new JSONArray();
            rightColumnItems.add(Tools.getStatbox("", contents));

            for (String chapter : HEADERS) {
                long start = System.nanoTime();
                convertChapter(new File("temp/" + language + "/" + chapter), new File("output/" + language + "/" + chapter));
                long middle = System.nanoTime();
                convertAndroidChapter(new File("temp/" + language + "/" + chapter), new File("output_android/" + language + "/" + chapter));
                desktop += middle - start;
                android += System.nanoTime() - middle;

                contents.add(Tools.getIconlabel(Tools.LINK_URL + "/" + language + "/" + chapter + "/index/toc.html", Tools.IMAGE_URL + "/ICON_CIVILOPEDIA_" + chapter.toUpperCase() + ".png", "", Tools.getControlText(chapter, language)));
            }

            Document document = convertAndroidSingleHTML(tocPage);
            File target = new File("output_android/" + language + "/index/index/toc.html");
            writeDocumentToFile(document, target);

            buildSearchIndex(language);
            buildSidebarScript(language);
        }
        System.out.printf("[TIME] %-22s %8.2fs  (desktop %.2fs, android %.2fs)%n", "render pages",
                (System.nanoTime() - pages) / 1e9, desktop / 1e9, android / 1e9);
        timed("copy manual", () -> {
            deleteFiles(new File("temp"));
            copyFiles(new File("manual/output"), new File("output"));
            copyFiles(new File("manual/output_android"), new File("output_android"));
        });
    }

    public static void addJson(JSONObject original, JSONObject extra, boolean overwrite) {
        for (Entry<String, Object> entry : extra.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Object o = original.get(key);
            if (o == null) {
                original.put(key, value);
            } else {
                if (value instanceof JSONObject) {
                    if (o instanceof JSONObject) {
                        addJson((JSONObject) o, (JSONObject) value, overwrite);
                    } else if (overwrite) {
                        original.remove(key);
                        original.put(key, value);
                    }
                } else if (value instanceof JSONArray) {
                    if (o instanceof JSONArray) {
                        for (Object obj : (JSONArray) value) {
                            ((JSONArray) o).add(obj);
                        }
                    } else if (overwrite) {
                        original.remove(key);
                        original.put(key, value);
                    }
                } else {
                    if (overwrite) {
                        original.remove(key);
                        original.put(key, value);
                    }
                }
            }
        }
    }

    public static void copyFiles(File source, File target) throws IOException {
        if (!source.exists()) {
            return;
        }
        if (source.isFile()) {
            target.getParentFile().mkdirs();   // was mkdirs() on the file path, then deleted again
            if (target.exists()) {
                target.delete();
            }
            target.createNewFile();
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(target);
            // 64 KB, not the 64 bytes this used to use. A run copies ~190 MB through here -- json
            // into temp, the icons into output_android, the manual images into both -- which cost
            // as long as rendering every page did, almost all of it syscall overhead.
            byte[] buf = new byte[1 << 16];
            int len;
            while ((len = in.read(buf)) >= 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.flush();
            out.close();
        } else {
            for (String child : source.list()) {
                copyFiles(new File(source, child), new File(target, child));
            }
        }
    }

    public static void deleteFiles(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else {
            for (File child : file.listFiles()) {
                deleteFiles(child);
            }
            file.delete();
        }
    }

    public static void deleteFilesExcept(File file, String exception) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else {
            for (File child : file.listFiles()) {
                if (!child.getName().equals(exception)) {
                    deleteFiles(child);
                }
            }
            file.delete();
        }
    }

    public static void main(String[] args) throws Exception {
        convertAll();
    }

    // add
    
}
