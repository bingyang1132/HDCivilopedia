package tools;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Tools implements Constants {

    public static String getControlText(String tag, String language) {
        switch (tag) {
            case "Civilizations": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "文明";
                    }
                    case "en_US": {
                        return "Civilizations";
                    }
                }
                break;
            }
            case "Traits": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "特点";
                    }
                    case "en_US": {
                        return "Traits";
                    }
                }
                break;
            }
            case "Leaders": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "领袖";
                    }
                    case "en_US": {
                        return "Leaders";
                    }
                }
                break;
            }
            case "UA": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "特色能力";
                    }
                    case "en_US": {
                        return "Unique Ability";
                    }
                }
                break;
            }
            case "LA": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "领袖能力";
                    }
                    case "en_US": {
                        return "Leader Ability";
                    }
                }
                break;
            }
            case "UU": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "特色单位";
                    }
                    case "en_US": {
                        return "Unique Units";
                    }
                }
                break;
            }
            case "UB": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "特色建筑";
                    }
                    case "en_US": {
                        return "Unique Buildings";
                    }
                }
                break;
            }
            case "UD": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "特色区域";
                    }
                    case "en_US": {
                        return "Unique Districts";
                    }
                }
                break;
            }
            case "UI": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "特色改良";
                    }
                    case "en_US": {
                        return "Unique Inprovements";
                    }
                }
                break;
            }
            case "UP": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "特色项目";
                    }
                    case "en_US": {
                        return "Unique Projects";
                    }
                }
                break;
            }
            case "CityStateType": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "城邦类型";
                    }
                    case "en_US": {
                        return "City-State Type";
                    }
                }
                break;
            }
            case "LEADER_MINOR_CIV_RELIGIOUS": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "宗教";
                    }
                    case "en_US": {
                        return "Religious";
                    }
                }
                break;
            }
            case "LEADER_MINOR_CIV_INDUSTRIAL": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "工业";
                    }
                    case "en_US": {
                        return "Industrial";
                    }
                }
                break;
            }
            case "LEADER_MINOR_CIV_MILITARISTIC": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "军事";
                    }
                    case "en_US": {
                        return "Militaristic";
                    }
                }
                break;
            }
            case "LEADER_MINOR_CIV_CULTURAL": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "文化";
                    }
                    case "en_US": {
                        return "Cultural";
                    }
                }
                break;
            }
            case "LEADER_MINOR_CIV_CSE_AGRICULTURAL": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "农业";
                    }
                    case "en_US": {
                        return "Agricultural";
                    }
                }
                break;
            }
            case "LEADER_MINOR_CIV_CSE_MARITIME": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "航海";
                    }
                    case "en_US": {
                        return "Maritime";
                    }
                }
                break;
            }
            case "LEADER_MINOR_CIV_TRADE": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "贸易";
                    }
                    case "en_US": {
                        return "Trade";
                    }
                }
                break;
            }
            case "LEADER_MINOR_CIV_SCIENTIFIC": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "科技";
                    }
                    case "en_US": {
                        return "Scientific";
                    }
                }
                break;
            }
            case "Description": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "描述";
                    }
                    case "en_US": {
                        return "Description";
                    }
                }
                break;
            }
            case "Quotes": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "引言";
                    }
                    case "en_US": {
                        return "Quotes";
                    }
                }
                break;
            }
            case "Requirements": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "要求";
                    }
                    case "en_US": {
                        return "Requirements";
                    }
                }
                break;
            }
            case "Buildings": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "建筑";
                    }
                    case "en_US": {
                        return "Buildings";
                    }
                }
                break;
            }
            case "Unique Districts": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "特色区域";
                    }
                    case "en_US": {
                        return "Unique Districts";
                    }
                }
                break;
            }
            case "Common Districts": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "通用区域";
                    }
                    case "en_US": {
                        return "Common Districts";
                    }
                }
                break;
            }
            case "Replaces": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "取代";
                    }
                    case "en_US": {
                        return "Replaces";
                    }
                }
                break;
            }
            case "Replaced by": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "取代者";
                    }
                    case "en_US": {
                        return "Replaced by";
                    }
                }
                break;
            }
            case "Unique To": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "特属于";
                    }
                    case "en_US": {
                        return "Unique To";
                    }
                }
                break;
            }
            case "Basic Yields": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "基本产出";
                    }
                    case "en_US": {
                        return "Basic Yields";
                    }
                }
                break;
            }
            case "points": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "点数";
                    }
                    case "en_US": {
                        return " points";
                    }
                }
                break;
            }
            case "housing": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "住房";
                    }
                    case "en_US": {
                        return "housing";
                    }
                }
                break;
            }
            case "amenity": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "宜居度";
                    }
                    case "en_US": {
                        return "amenity";
                    }
                }
                break;
            }
            case "citizen slot": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "专家槽位";
                    }
                    case "en_US": {
                        return "citizen slot";
                    }
                }
                break;
            }
            case "appeal 1": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "相邻单元格 ";
                    }
                    case "en_US": {
                        return "";
                    }
                }
                break;
            }
            case "appeal 2": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return " 魅力";
                    }
                    case "en_US": {
                        return " appeal to adjacent tiles";
                    }
                }
                break;
            }
            case "plunder yields": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "掠夺产出：";
                    }
                    case "en_US": {
                        return "plunder yields: ";
                    }
                }
                break;
            }
            case "Adacency Bonus": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "相邻加成";
                    }
                    case "en_US": {
                        return "Adacency Bonus";
                    }
                }
                break;
            }
            case "Citizen Yields": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "专家产出";
                    }
                    case "en_US": {
                        return "Citizen Yields";
                    }
                }
                break;
            }
            case "Trade Yields": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "贸易产出";
                    }
                    case "en_US": {
                        return "Trade Yields";
                    }
                }
                break;
            }
            case "Domestic Destination": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "国内目的地";
                    }
                    case "en_US": {
                        return "Domestic Destination";
                    }
                }
                break;
            }
            case "International Destination": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "国际目的地";
                    }
                    case "en_US": {
                        return "International Destination";
                    }
                }
                break;
            }
            case "Technology": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "科技";
                    }
                    case "en_US": {
                        return "Technology";
                    }
                }
                break;
            }
            case "Civic": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "市政";
                    }
                    case "en_US": {
                        return "Civic";
                    }
                }
                break;
            }
            case "Production Cost": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "生产力消耗";
                    }
                    case "en_US": {
                        return "Production Cost";
                    }
                }
                break;
            }
            case "Basic Cost": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "基准花费：";
                    }
                    case "en_US": {
                        return "Basic Cost: ";
                    }
                }
                break;
            }
            case "production": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "生产力";
                    }
                    case "en_US": {
                        return "production";
                    }
                }
                break;
            }
            case "Extra Cost 1": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "此前每个";
                    }
                    case "en_US": {
                        return "Extra cost for each previous ";
                    }
                }
                break;
            }
            case "Extra Cost 2": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "额外花费：";
                    }
                    case "en_US": {
                        return ": ";
                    }
                }
                break;
            }
            case "Maintenance Cost": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "维护费用";
                    }
                    case "en_US": {
                        return "Maintenance Cost";
                    }
                }
                break;
            }
            case "gold": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "金币";
                    }
                    case "en_US": {
                        return "gold";
                    }
                }
                break;
            }
            case "Common Buildings": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "通用建筑";
                    }
                    case "en_US": {
                        return "Common Buildings";
                    }
                }
                break;
            }
            case "Unique Buildings": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "特色建筑";
                    }
                    case "en_US": {
                        return "Unique Buildings";
                    }
                }
                break;
            }
            case "Worship Buildings": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "祭祀建筑";
                    }
                    case "en_US": {
                        return "Worship Buildings";
                    }
                }
                break;
            }
            case "health": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "生命值";
                    }
                    case "en_US": {
                        return "health";
                    }
                }
                break;
            }
            case "powered 1": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "[ICON_POWER] 供电充足时，额外";
                    }
                    case "en_US": {
                        return "";
                    }
                }
                break;
            }
            case "powered 2": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "";
                    }
                    case "en_US": {
                        return " additionally when [ICON_POWER] Powered";
                    }
                }
                break;
            }
            case "district copy 1": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "加成等于该区域相邻 ";
                    }
                    case "en_US": {
                        return " bonus equal to the adjacency ";
                    }
                }
                break;
            }
            case "district copy 2": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "加成";
                    }
                    case "en_US": {
                        return " bonus of its district";
                    }
                }
                break;
            }
            case "per era 1": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "每时代额外 ";
                    }
                    case "en_US": {
                        return "";
                    }
                }
                break;
            }
            case "per era 2": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "";
                    }
                    case "en_US": {
                        return " additionally per era";
                    }
                }
                break;
            }
            case "GREATWORKSLOT_PALACE": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return " 巨作槽位（可保存任意类型）";
                    }
                    case "en_US": {
                        return " Great Work slot (holds any type)";
                    }
                }
                break;
            }
            case "GREATWORKSLOT_ART": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return " [ICON_GREATWORK_LANDSCAPE] 艺术巨作槽位";
                    }
                    case "en_US": {
                        return " [ICON_GREATWORK_LANDSCAPE]  Great Work of Art slot";
                    }
                }
                break;
            }
            case "GREATWORKSLOT_ARTIFACT": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return " [ICON_GREATWORK_ARTIFACT]  文物槽位";
                    }
                    case "en_US": {
                        return " [ICON_GREATWORK_ARTIFACT] Artifact slot";
                    }
                }
                break;
            }
            case "GREATWORKSLOT_CATHEDRAL": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return " [ICON_GREATWORK_RELIGIOUS] 宗教艺术巨作槽位";
                    }
                    case "en_US": {
                        return " [ICON_GREATWORK_RELIGIOUS] Great Work of Religious Art slot";
                    }
                }
                break;
            }
            case "GREATWORKSLOT_MUSIC": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return " [ICON_GREATWORK_MUSIC] 音乐巨作槽位";
                    }
                    case "en_US": {
                        return " [ICON_GREATWORK_MUSIC] Great Work of Music slot";
                    }
                }
                break;
            }
            case "GREATWORKSLOT_RELIC": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return " [ICON_GREATWORK_RELIC] 遗物槽位";
                    }
                    case "en_US": {
                        return " [ICON_GREATWORK_RELIC] Relic slot";
                    }
                }
                break;
            }
            case "GREATWORKSLOT_WRITING": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return " [ICON_GREATWORK_WRITING] 著作巨作槽位";
                    }
                    case "en_US": {
                        return " [ICON_GREATWORK_WRITING] Great Works of Writing slot";
                    }
                }
                break;
            }
            // case "GREATWORKSLOT_PRODUCT": {
            //     switch (language) {
            //         case "zh_Hans":
            //         case "zh_Hans_CN": {
            //             return " [ICON_GREATWORK_PRODUCT] 产品槽位";
            //         }
            //         case "en_US": {
            //             return " [ICON_GREATWORK_PRODUCT] Product slot";
            //         }
            //     }
            //     break;
            // }
            case "GREATWORKSLOT_PRODUCT": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "产品槽位";
                    }
                    case "en_US": {
                        return "Product slot";
                    }
                }
                break;
            }
            case "District": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "区域";
                    }
                    case "en_US": {
                        return "District";
                    }
                }
                break;
            }
            case "Building": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "建筑";
                    }
                    case "en_US": {
                        return "Building";
                    }
                }
                break;
            }
            case "Building any": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "（任意）建筑";
                    }
                    case "en_US": {
                        return "Buildings (any)";
                    }
                }
                break;
            }
            case "Purchase Cost": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "购买成本";
                    }
                    case "en_US": {
                        return "Purchase Cost";
                    }
                }
                break;
            }
            case "Power Cost": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "电力成本";
                    }
                    case "en_US": {
                        return "Power Cost";
                    }
                }
                break;
            }
            case "Power": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "电力";
                    }
                    case "en_US": {
                        return "Power";
                    }
                }
                break;
            }
            case "Tourism from Rock Concerts 1": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "摇滚乐队提供的 [ICON_TOURISM] 旅游业绩 ";
                    }
                    case "en_US": {
                        return "";
                    }
                }
                break;
            }
            case "Tourism from Rock Concerts 2": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "";
                    }
                    case "en_US": {
                        return " [ICON_TOURISM] Tourism from Rock Concerts";
                    }
                }
                break;
            }
            case "regional 1": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "其基础产出延伸到";
                    }
                    case "en_US": {
                        return "Its basic yield is extended to all City Centers within ";
                    }
                }
                break;
            }
            case "regional 2": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "单元格内的所有市中心（前提是其尚未从其他的此建筑获得加成）。";
                    }
                    case "en_US": {
                        return " tiles that do not already have a bonus from this building type.";
                    }
                }
                break;
            }
            case "regional 1M": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "以下产出延伸到市中心或社区在";
                    }
                    case "en_US": {
                        return "Its basic yield is extended to all cities with City Center or Neighborhood within ";
                    }
                }
                break;
            }
            case "regional 2M": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "单元格内的所有城市（前提是其尚未从其他的此建筑获得加成）。";
                    }
                    case "en_US": {
                        return " tiles that do not already have a bonus from this building type.";
                    }
                }
                break;
            }
            case "Exclusive": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "互相排斥";
                    }
                    case "en_US": {
                        return "Mutually Exclusive With";
                    }
                }
                break;
            }
            case "Government": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "政体";
                    }
                    case "en_US": {
                        return "Government";
                    }
                }
                break;
            }
            case "Tier1": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "一级政体";
                    }
                    case "en_US": {
                        return "Tier 1 Government";
                    }
                }
                break;
            }
            case "Tier2": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "二级政体";
                    }
                    case "en_US": {
                        return "Tier 2 Government";
                    }
                }
                break;
            }
            case "Tier3": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "三级政体";
                    }
                    case "en_US": {
                        return "Tier 3 Government";
                    }
                }
                break;
            }
            case "Tier4": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "四级政体";
                    }
                    case "en_US": {
                        return "Tier 4 Government";
                    }
                }
                break;
            }
            case "Wonders": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "奇观";
                    }
                    case "en_US": {
                        return "Wonders";
                    }
                }
                break;
            }
            case "ObsoleteEra": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "移除，若游戏开始晚于";
                    }
                    case "en_US": {
                        return "Removed if game started after";
                    }
                }
                break;
            }
            case "Upgrades To": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "可升级为";
                    }
                    case "en_US": {
                        return "Upgrades To";
                    }
                }
                break;
            }
            case "Upgraded From": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "升级自";
                    }
                    case "en_US": {
                        return "Upgraded From";
                    }
                }
                break;
            }
            case "Promotion Class": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "单位类型：";
                    }
                    case "en_US": {
                        return "Promotion Class: ";
                    }
                }
                break;
            }
            case "SightRange": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "视野范围";
                    }
                    case "en_US": {
                        return "Sight Range";
                    }
                }
                break;
            }
            case "MovementPoints": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "移动力";
                    }
                    case "en_US": {
                        return "Movement Points";
                    }
                }
                break;
            }
            case "MeleeStrength": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "近战攻击力";
                    }
                    case "en_US": {
                        return "Melee Strength";
                    }
                }
                break;
            }
            case "RangedStrength": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "远程攻击力";
                    }
                    case "en_US": {
                        return "Ranged Strength";
                    }
                }
                break;
            }
            case "BombardStrength": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "轰炸攻击力";
                    }
                    case "en_US": {
                        return "Bombard Strength";
                    }
                }
                break;
            }
            case "Anti-airStrength": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "防空力量";
                    }
                    case "en_US": {
                        return "Anti-air Strength";
                    }
                }
                break;
            }
            case "Range": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "射程";
                    }
                    case "en_US": {
                        return "Range";
                    }
                }
                break;
            }
            case "BuildCharges": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "劳动力";
                    }
                    case "en_US": {
                        return "Build Charges";
                    }
                }
                break;
            }
            case "ReligiousStrength": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "宗教战斗力";
                    }
                    case "en_US": {
                        return "Religious Strength";
                    }
                }
                break;
            }
            case "SpreadCharges": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "传教次数";
                    }
                    case "en_US": {
                        return "Spread Charges";
                    }
                }
                break;
            }
            case "HealCharges": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "治疗次数";
                    }
                    case "en_US": {
                        return "Heal Charges";
                    }
                }
                break;
            }
            case "PressureEliminated": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "压力消除";
                    }
                    case "en_US": {
                        return "Pressure Eliminated";
                    }
                }
                break;
            }
            case "Strategic Resource": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "战略资源";
                    }
                    case "en_US": {
                        return "Strategic Resource";
                    }
                }
                break;
            }
            case "UnitPromotions": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "单位强化";
                    }
                    case "en_US": {
                        return "Unit Promotions";
                    }
                }
                break;
            }
            case "Self": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "中文（简体）";
                    }
                    case "en_US": {
                        return "English (United States)";
                    }
                }
                break;
            }
            case "Language": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "语言";
                    }
                    case "en_US": {
                        return "Language";
                    }
                }
                break;
            }
            case "ActivatedEffect": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "激活效果";
                    }
                    case "en_US": {
                        return "Activated Effect";
                    }
                }
                break;
            }
            case "charge": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "次数";
                    }
                    case "en_US": {
                        return "charge";
                    }
                }
                break;
            }
            case ": ": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "：";
                    }
                    case "en_US": {
                        return ": ";
                    }
                }
                break;
            }
            case " (": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "（";
                    }
                    case "en_US": {
                        return " (";
                    }
                }
                break;
            }
            case ") ": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "）";
                    }
                    case "en_US": {
                        return ") ";
                    }
                }
                break;
            }
            case "Governments": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "政体";
                    }
                    case "en_US": {
                        return "Governments";
                    }
                }
                break;
            }
            case "Inherent": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "传承效果";
                    }
                    case "en_US": {
                        return "Legacy Effect";
                    }
                }
                break;
            }
            case "Accumulate": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "固定效果";
                    }
                    case "en_US": {
                        return "Inherent Effect";
                    }
                }
                break;
            }
            case "Slot": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "政策槽位";
                    }
                    case "en_US": {
                        return " Slot";
                    }
                }
                break;
            }
            case "Favor 1": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "每 [ICON_TURN] 回合 ";
                    }
                    case "en_US": {
                        return "";
                    }
                }
                break;
            }
            case "Favor 2": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return " 点 [ICON_FAVOR] 外交支持。";
                    }
                    case "en_US": {
                        return " [ICON_FAVOR] Diplomatic Favor per [ICON_TURN] turn.";
                    }
                }
                break;
            }
            case "Influence 1": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "每 [ICON_TURN] 回合 ";
                    }
                    case "en_US": {
                        return "";
                    }
                }
                break;
            }
            case "Influence 2": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return " 点影响力。";
                    }
                    case "en_US": {
                        return " Influence point per [ICON_TURN] turn.";
                    }
                }
                break;
            }
            case "Envoy 1": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "每 ";
                    }
                    case "en_US": {
                        return "At ";
                    }
                }
                break;
            }
            case "Envoy 2": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return " 点影响力获得 ";
                    }
                    case "en_US": {
                        return " Influence points, gain ";
                    }
                }
                break;
            }
            case "Envoy 3": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return " [ICON_ENVOY] 使者。";
                    }
                    case "en_US": {
                        return " [ICON_ENVOY] envoy.";
                    }
                }
                break;
            }
            case "Tolerance 1": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "对其他政体的文明";
                    }
                    case "en_US": {
                        return "";
                    }
                }
                break;
            }
            case "Tolerance 2": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "点外交修正值。";
                    }
                    case "en_US": {
                        return " diplomatic modifier towards civilizations in other governments.";
                    }
                }
                break;
            }
            case "Policies": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "政策";
                    }
                    case "en_US": {
                        return " Policies";
                    }
                }
                break;
            }
            case "Dark Age Policies": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "黑暗时代政策";
                    }
                    case "en_US": {
                        return "Dark Age Policies";
                    }
                }
                break;
            }
            case "Legacy Policies": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "传承政策";
                    }
                    case "en_US": {
                        return "Legacy Policies";
                    }
                }
                break;
            }
            case "Legacy From": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "传承自";
                    }
                    case "en_US": {
                        return "Legacy from";
                    }
                }
                break;
            }
            case "Legacy Policy": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "传承政策";
                    }
                    case "en_US": {
                        return "Legacy Policy";
                    }
                }
                break;
            }
            case "Exclusive Policy": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "专属政策";
                    }
                    case "en_US": {
                        return "Exclusive Policy";
                    }
                }
                break;
            }
            case "Dark Age": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "黑暗时代";
                    }
                    case "en_US": {
                        return "Dark Age";
                    }
                }
                break;
            }
            case "Min Era": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "自";
                    }
                    case "en_US": {
                        return "From ";
                    }
                }
                break;
            }
            case "Max Era": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "至";
                    }
                    case "en_US": {
                        return " Until ";
                    }
                }
                break;
            }
            case "Obsolete With": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "解锁以下政策卡后失效";
                    }
                    case "en_US": {
                        return "Obsolete With";
                    }
                }
                break;
            }
            case "Belief Class": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "信仰类型";
                    }
                    case "en_US": {
                        return "Belief Class";
                    }
                }
                break;
            }
            case "Belief": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "信仰";
                    }
                    case "en_US": {
                        return " Belief";
                    }
                }
                break;
            }
            case "Terrains": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "地形";
                    }
                    case "en_US": {
                        return "Terrains";
                    }
                }
                break;
            }
            case "Movement Cost: ": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "移动力消耗：";
                    }
                    case "en_US": {
                        return "Movement Cost: ";
                    }
                }
                break;
            }
            case "Sight Height: ": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "视野高度：";
                    }
                    case "en_US": {
                        return "Sight Height: ";
                    }
                }
                break;
            }
            case "Defense Modifier: ": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "防御力修正：";
                    }
                    case "en_US": {
                        return "Defense Modifier: ";
                    }
                }
                break;
            }
            case "Common Improvements": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "通用改良";
                    }
                    case "en_US": {
                        return "Common Improvements";
                    }
                }
                break;
            }
            case "Unique Improvements": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "特色改良";
                    }
                    case "en_US": {
                        return "Unique Improvements";
                    }
                }
                break;
            }
            case ", requires ": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "，需求";
                    }
                    case "en_US": {
                        return ", requires ";
                    }
                }
                break;
            }
            case "Built By": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "可被以下单位建造";
                    }
                    case "en_US": {
                        return "Built By";
                    }
                }
                break;
            }
            case "Resouces": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "资源";
                    }
                    case "en_US": {
                        return "Resouces";
                    }
                }
                break;
            }
            case "National Wonders": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "国家奇观";
                    }
                    case "en_US": {
                        return "National Wonders";
                    }
                }
                break;
            }
            case "Governors": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "总督";
                    }
                    case "en_US": {
                        return "Governors";
                    }
                }
                break;
            }
            case "Features": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "地貌";
                    }
                    case "en_US": {
                        return "Features";
                    }
                }
                break;
            }
            case "Harvest": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "收获";
                    }
                    case "en_US": {
                        return "Harvest";
                    }
                }
                break;
            }
            case "Natural Wonders": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "自然奇观";
                    }
                    case "en_US": {
                        return "Natural Wonders";
                    }
                }
                break;
            }
            case "Resources": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "资源";
                    }
                    case "en_US": {
                        return " Resources";
                    }
                }
                break;
            }
            case "Improved By": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "被以下设施改良";
                    }
                    case "en_US": {
                        return "Improved By";
                    }
                }
                break;
            }
            case "Requires": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "需求";
                    }
                    case "en_US": {
                        return "Requires";
                    }
                }
                break;
            }
            case "Placement": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "生成位置";
                    }
                    case "en_US": {
                        return "Placement";
                    }
                }
                break;
            }
            case "Luxuries": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "奢侈";
                    }
                    case "en_US": {
                        return " Luxuries";
                    }
                }
                break;
            }
            case "Era": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "时代";
                    }
                    case "en_US": {
                        return "Era";
                    }
                }
                break;
            }
            case "Science Cost": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "科技消耗";
                    }
                    case "en_US": {
                        return "Science Cost";
                    }
                }
                break;
            }
            case " (Random)": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "（随机）";
                    }
                    case "en_US": {
                        return " (Random)";
                    }
                }
                break;
            }
            case "Culture Cost": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "文化消耗";
                    }
                    case "en_US": {
                        return "Culture Cost";
                    }
                }
                break;
            }
            case "Eureka": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "尤里卡";
                    }
                    case "en_US": {
                        return "Eureka";
                    }
                }
                break;
            }
            case "Inspiration": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "鼓舞";
                    }
                    case "en_US": {
                        return "Inspiration";
                    }
                }
                break;
            }
            case "Unlocks": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "解锁";
                    }
                    case "en_US": {
                        return "Unlocks";
                    }
                }
                break;
            }
            case "Tech Tree": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "科技树";
                    }
                    case "en_US": {
                        return "Tech Tree";
                    }
                }
                break;
            }
            case "Civic Tree": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "市政树";
                    }
                    case "en_US": {
                        return "Civic Tree";
                    }
                }
                break;
            }
            case "Prerequires": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "前置";
                    }
                    case "en_US": {
                        return "Prerequires";
                    }
                }
                break;
            }
            case "Leads To Tech": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "作为以下科技的前置";
                    }
                    case "en_US": {
                        return "Leads To";
                    }
                }
                break;
            }
            
            case "Leads To Civic": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "作为以下市政的前置";
                    }
                    case "en_US": {
                        return "Leads To";
                    }
                }
                break;
            }
            case "Conversion 1": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "该项目 [ICON_PRODUCTION] 生产力的 ";
                    }
                    case "en_US": {
                        return "";
                    }
                }
                break;
            }
            case "Conversion 2": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "% 被转换成";
                    }
                    case "en_US": {
                        return "% [ICON_PRODUCTION] Production towards this project is converted to";
                    }
                }
                break;
            }
            case "Provides Upon Complection": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "完成后提供";
                    }
                    case "en_US": {
                        return "Provides Upon Complection";
                    }
                }
                break;
            }
            case "Removes Upon Complection": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "完成后移除";
                    }
                    case "en_US": {
                        return "Removes Upon Complection";
                    }
                }
                break;
            }
            case "Project": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "项目";
                    }
                    case "en_US": {
                        return "Project";
                    }
                }
                break;
            }
            case "Corporation Product": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "公司产品项目";
                    }
                    case "en_US": {
                        return "Corporation Product Projects";
                    }
                }
                break;
            }
            case "Enhance": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "区域项目";
                    }
                    case "en_US": {
                        return "District Projects";
                    }
                }
                break;
            }
            case "City Policy": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "城市政策项目";
                    }
                    case "en_US": {
                        return "City Policy Projects";
                    }
                }
                break;
            }
            case "Other Projects": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "其它项目";
                    }
                    case "en_US": {
                        return "Other Projects";
                    }
                }
                break;
            }
            case "Grant Resource": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "资源生成项目";
                    }
                    case "en_US": {
                        return "Resource Generation Projects";
                    }
                }
                break;
            }
            case "District Removement": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "区域移除项目";
                    }
                    case "en_US": {
                        return "District Removement Project";
                    }
                }
                break;
            }
            case "Building Conversion": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "建筑转换项目";
                    }
                    case "en_US": {
                        return "Building Conversion Project";
                    }
                }
                break;
            }
            case "Unique": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "特色项目";
                    }
                    case "en_US": {
                        return "Unique Project";
                    }
                }
                break;
            }
            case "Space": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "宇航项目";
                    }
                    case "en_US": {
                        return "Space Project";
                    }
                }
                break;
            }
            case "concepts": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "概念";
                    }
                    case "en_US": {
                        return "Concepts";
                    }
                }
                break;
            }
            case "civilizations": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "文明和领袖";
                    }
                    case "en_US": {
                        return "Civilizations and Leaders";
                    }
                }
                break;
            }
            case "citystates": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "城邦";
                    }
                    case "en_US": {
                        return "City-States";
                    }
                }
                break;
            }
            case "districts": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "区域";
                    }
                    case "en_US": {
                        return "Districts";
                    }
                }
                break;
            }
            case "buildings": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "建筑";
                    }
                    case "en_US": {
                        return "Buildings";
                    }
                }
                break;
            }
            case "wonders": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "奇观";
                    }
                    case "en_US": {
                        return "Wonders";
                    }
                }
                break;
            }
            case "units": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "单位";
                    }
                    case "en_US": {
                        return "Units";
                    }
                }
                break;
            }
            case "unitpromotions": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "单位强化";
                    }
                    case "en_US": {
                        return "Unit Promotions";
                    }
                }
                break;
            }
            case "greatpeople": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "伟人";
                    }
                    case "en_US": {
                        return "Great People";
                    }
                }
                break;
            }
            case "technologies": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "科技";
                    }
                    case "en_US": {
                        return "Technologies";
                    }
                }
                break;
            }
            case "civics": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "市政";
                    }
                    case "en_US": {
                        return "Civics";
                    }
                }
                break;
            }
            case "governments": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "政体、政策和着力点";
                    }
                    case "en_US": {
                        return "Governments, Policies and Commemorations";
                    }
                }
                break;
            }
            case "religions": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "宗教";
                    }
                    case "en_US": {
                        return "Religions";
                    }
                }
                break;
            }
            case "features": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "地形, 地貌和自然奇观";
                    }
                    case "en_US": {
                        return "Terrains, Features and Natural Wonders";
                    }
                }
                break;
            }
            case "resources": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "资源";
                    }
                    case "en_US": {
                        return "Resources";
                    }
                }
                break;
            }
            case "improvements": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "改良";
                    }
                    case "en_US": {
                        return "Improvements";
                    }
                }
                break;
            }
            case "governors": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "总督";
                    }
                    case "en_US": {
                        return "Governors";
                    }
                }
                break;
            }
            case "historic_moments": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "项目";
                    }
                    case "en_US": {
                        return "Projects";
                    }
                }
                break;
            }
            case "Civ6 HD Official Wiki": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "文明6 和而不同 官方百科";
                    }
                    case "en_US": {
                        return "Civ6 HD Official Wiki";
                    }
                }
                break;
            }
            case "Commemorations": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "着力点";
                    }
                    case "en_US": {
                        return "Commemorations";
                    }
                }
                break;
            }
            case "Commemorations_CN": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "中国“天命”着力点";
                    }
                    case "en_US": {
                        return "China \"Mandate of Heaven\" Commemorations";
                    }
                }
                break;
            }
            case "Golden Age": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "黄金时代";
                    }
                    case "en_US": {
                        return "Golden Age";
                    }
                }
                break;
            }
            case "Normal Age": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "普通时代";
                    }
                    case "en_US": {
                        return "Normal Age";
                    }
                }
                break;
            }
            case "River": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "河流";
                    }
                    case "en_US": {
                        return "River";
                    }
                }
                break;
            }
            case "Start Bias": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "出生点关联";
                    }
                    case "en_US": {
                        return "Start Bias";
                    }
                }
                break;
            }
            case "ResourceCategory": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "行业与公司";
                    }
                    case "en_US": {
                        return "Industry and Corporation";
                    }
                }
                break;
            }
            case "IndustryEffect": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "行业效果";
                    }
                    case "en_US": {
                        return "Industry Effect";
                    }
                }
                break;
            }
            case "CorporationEffect": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "公司效果";
                    }
                    case "en_US": {
                        return "Corporation Effect";
                    }
                }
                break;
            }
            case "ProductEffect": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "产品效果";
                    }
                    case "en_US": {
                        return "Product Effect";
                    }
                }
                break;
            }
            case "": {
                switch (language) {
                    case "zh_Hans":
                    case "zh_Hans_CN": {
                        return "";
                    }
                    case "en_US": {
                        return "";
                    }
                }
                break;
            }
        }
        return tag;
    }

    public static String signed (int x) {
        if (x >= 0) {
            return "+" + x;
        } else {
            return "" + x;
        }
    }

    public static JSONObject getLine(int row1, int row2, int column1, int column2) {
        JSONObject object = new JSONObject();
        object.put("type", "line");
        object.put("row1", row1);
        object.put("row2", row2);
        object.put("column1", column1);
        object.put("column2", column2);
        return object;
    }

    public static JSONObject getPromotion(int row, int column, String icon, String alt, String title, String text) {
        JSONObject object = new JSONObject();
        object.put("type", "promotion");
        object.put("row", row);
        object.put("column", column);
        object.put("icon", icon);
        object.put("alt", alt);
        object.put("title", title);
        object.put("text", text);
        return object;
    }

    public static JSONObject getGovernorBox(JSONArray contents) {
        JSONObject object = new JSONObject();
        object.put("type", "governorbox");
        object.put("contents", contents);
        return object;
    }
    public static JSONObject getPromotionBox(JSONArray contents) {
        JSONObject object = new JSONObject();
        object.put("type", "promotionbox");
        object.put("contents", contents);
        return object;
    }

    public static JSONObject getSeparator() {
        JSONObject object = new JSONObject();
        object.put("type", "separator");
        return object;
    }

    public static JSONObject getHeader(String text) {
        JSONObject object = new JSONObject();
        object.put("type", "header");
        object.put("text", text);
        return object;
    }

    public static JSONObject getIconlabel(String link, String src, String alt, String text) {
        JSONObject object = new JSONObject();
        object.put("type", "iconlabel");
        object.put("link", link);
        object.put("src", src);
        object.put("alt", alt);
        object.put("text", text);
        return object;
    }

    public static JSONObject getIconlabel(String link, String src, String alt, String num, String text) {
        JSONObject object = new JSONObject();
        object.put("type", "iconlabel");
        object.put("link", link);
        object.put("src", src);
        object.put("alt", alt);
        object.put("num", num);
        object.put("text", text);
        return object;
    }

    public static JSONObject getLabel(String text) {
        JSONObject object = new JSONObject();
        object.put("type", "label");
        object.put("text", text);
        return object;
    }

    public static JSONObject getPortrait(String src, String alt) {
        JSONObject object = new JSONObject();
        object.put("type", "portrait");
        object.put("src", src);
        object.put("alt", alt);
        return object;
    }

    public static JSONObject getBody(String title, String text) {
        JSONObject object = new JSONObject();
        object.put("type", "body");
        object.put("title", title);
        object.put("text", text);
        return object;
    }

    public static JSONObject getStatbox(String title, JSONArray contents) {
        JSONObject object = new JSONObject();
        object.put("type", "statbox");
        object.put("title", title);
        object.put("contents", contents);
        return object;
    }

    public static String getYield(String tag, String language) {
        String type = tag.replace("YIELD_", "").replace("PLUNDER_", "");
        if (type.equals("HEAL")) {
            return " " + Tools.getControlText("health", language);
        }
		if (type.equals("AMENITY")) {
            return " " + Tools.getControlText(" [ICON_AMENITIES] ", language) + getControlText("amenity", language);
        }
        return " [ICON_" + type + "] " + getTextWithAlt("LOC_YIELD_" + type + "_NAME", language);
    }

    public static String getResource(String tag, String language) {
        return " [ICON_" + tag + "] " + getTextWithAlt("LOC_" + tag + "_NAME", language);
    }

    public static String getTextWithAlt(String tag, String language) {
        String text = getText(tag, language);
        if (text == null) {
            return "[" + language + ":" + tag + "]";
        }
        return text;
    }

    public static JSONObject readJSON(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        InputStream in = new FileInputStream(file);
        byte[] buf = new byte[in.available()];
        in.read(buf);
        in.close();
        String text = new String(buf, "UTF-8");
        JSONObject object = JSON.parseObject(text);
        return object;
    }

    public static void writeJson(JSONObject object, File target) throws IOException {
        if (target.exists()) {
            target.delete();
        }
        target.getParentFile().mkdirs();
        target.createNewFile();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target), "UTF-8"));
        writer.write(object.toString());
        writer.flush();
        writer.close();
    }

    public static JSONArray sort(JSONArray array) {
        Collections.sort(array, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof JSONObject && o2 instanceof JSONObject) {
                    JSONObject object1 = (JSONObject) o1;
                    JSONObject object2 = (JSONObject) o2;
                    int i1 = 0;
                    int i2 = 0;
                    if (object1.containsKey("order")) {
                        i1 = object1.getIntValue("order");
                    }
                    if (object2.containsKey("order")) {
                        i2 = object2.getIntValue("order");
                    }
                    return Integer.compare(i1, i2);
                }
                return 0;
            }
        });
        return array;
    }

    public static List<File> sort(File[] files) {
        List<File> list = Arrays.asList(files);
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Collator.getInstance(java.util.Locale.CHINA).compare(o1.getName(), o2.getName());
            }
        });
        return list;
    }

    public static final List<String> TAGS = new ArrayList<>();

    public static String getText(String tag, String language) {
        return getText(tag, language, null);
    }

    public static void editExcel () throws IOException {
        File ori = new File("4-7 A1.xlsx");
        Workbook workbook = new XSSFWorkbook(new FileInputStream(ori));
        Sheet sheet = workbook.getSheetAt(0);
        Sheet copy = workbook.createSheet("compare");
        Row row0 = copy.createRow(0);
        row0.createCell(0).setCellValue("Tag");
        row0.createCell(1).setCellValue("原文本");
        row0.createCell(2).setCellValue("当前文本");
        row0.createCell(3).setCellValue("校对文本");
        int i = 1;
        for (Row row : sheet) {
            Cell c = row.getCell(11);
            if (c != null && c.getStringCellValue() != null && !c.getStringCellValue().isEmpty()) {
                String tag = row.getCell(0).getStringCellValue();
                if (tag.equals("Tag")) {
                    continue;
                } else {
                    Row r = copy.createRow(i);
                    i ++;
                    r.createCell(0).setCellValue(tag);
                    r.createCell(1).setCellValue(row.getCell(4) == null ? "NULL" : row.getCell(4).getStringCellValue());
                    r.createCell(2).setCellValue(Tools.getText(tag, "en_US"));
                    r.createCell(3).setCellValue(c.getStringCellValue());
                }
            }
        }
        File xls = new File("4-7 A1 cmp.xlsx");
        if (xls.exists()) {
            xls.delete();
        }
        xls.createNewFile();
        FileOutputStream out = new FileOutputStream(xls);
        workbook.write(out);
        out.flush();
        out.close();
    }

    public static void main(String[] args) throws Exception {
        editExcel();
    }

    public static void writeExcel () {
        Statement localization = null;
        Statement nohdLocalization = null;
        try {
            localization = DriverManager.getConnection(TEXT_DATABASE).createStatement();
            nohdLocalization = DriverManager.getConnection(NOHD_TEXT_DATABASE).createStatement();
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Texts");
            CellStyle yellow = workbook.createCellStyle();
            yellow.setFillForegroundColor((short)5);
            yellow.setFillPattern(CellStyle.SOLID_FOREGROUND);
            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("Tag");
            row0.createCell(1).setCellValue("F版中文");
            row0.createCell(2).setCellValue("D版中文");
            row0.createCell(3).setCellValue("F版英文");
            row0.createCell(4).setCellValue("D版英文");
            row0.createCell(5).setCellValue("F版日文");
            row0.createCell(6).setCellValue("D版日文");
            Set<String> written = new HashSet<>();
            written.add("");
            written.add(null);
            int i = 1;
            for (String tag : TAGS) {
                if (!written.contains(tag)) {
                    written.add(tag);
                    Row row = sheet.createRow(i);
                    i++;
                    row.createCell(0).setCellValue(tag);
                    ResultSet r1 = localization.executeQuery("select * from LocalizedText where Tag = \"" + tag + "\";");
                    while (r1.next()) {
                        String lan = r1.getString("Language");
                        if (lan.equals("zh_Hans_CN")) {
                            row.createCell(2).setCellValue(r1.getString("Text"));
                        }
                        if (lan.equals("en_US")) {
                            row.createCell(4).setCellValue(r1.getString("Text"));
                        }
                    }
                    ResultSet r2 = nohdLocalization.executeQuery("select * from LocalizedText where Tag = \"" + tag + "\";");
                    while (r2.next()) {
                        String lan = r2.getString("Language");
                        if (lan.equals("zh_Hans_CN")) {
                            row.createCell(1).setCellValue(r2.getString("Text"));
                        }
                        if (lan.equals("en_US")) {
                            row.createCell(3).setCellValue(r2.getString("Text"));
                        }
                        if (lan.equals("ja_JP")) {
                            row.createCell(5).setCellValue(r2.getString("Text"));
                        }
                    }
                    try {
                        if (!row.getCell(1).getStringCellValue().equals(row.getCell(2).getStringCellValue())) {
                            row.getCell(1).setCellStyle(yellow);
                            row.getCell(2).setCellStyle(yellow);
                        }
                    } catch (Exception e) {
                    }
                    try {
                        if (!row.getCell(3).getStringCellValue().equals(row.getCell(4).getStringCellValue())) {
                            row.getCell(3).setCellStyle(yellow);
                            row.getCell(4).setCellStyle(yellow);
                        }
                    } catch (Exception e) {
                    }
                }
            }
            File xls = new File(XLS);
            if (xls.exists()) {
                xls.delete();
            }
            xls.createNewFile();
            FileOutputStream out = new FileOutputStream(xls);
            workbook.write(out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (localization != null) {
                try {
                    localization.close();
                } catch (Exception e) {
                }
            }
            if (nohdLocalization != null) {
                try {
                    nohdLocalization.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static String getText(String tag, String language, String modifierID) {
        TAGS.add(tag);
        if (tag != null && tag.contains("{")) {
            return analyzeBraces(tag, language, modifierID);
        }
        String text = tryGetText(tag, language, modifierID);
        if (text == null && language.equals("zh_Hans_CN")) {
            text = tryGetText(tag, "zh_Hans", modifierID);
        }
        if (text == null && language.equals("zh_Hans")) {
            text = tryGetText(tag, "zh_Hans_CN", modifierID);
        }
        if (text == null) {
            String raw = tryGetRawText(tag, "en_US");
            if (raw != null && raw.contains("{")) {
                text = analyzeBraces(raw, language, modifierID);
            }
        }
        return text;
    }

    public static String getIcon(String icon) {
        //if (icon.startsWith("ICON_")) {
        //    return "<img src=\"" + Tools.IMAGE_URL + "/" + icon.substring("ICON_".length(), icon.length()).toUpperCase() + ".png\" alt=\"[" + icon.toUpperCase() + "]\" class=\"fontIcon\"/>";
        //}
        return "<img src=\"" + Tools.IMAGE_URL + "/" + icon.toUpperCase() + ".png\" alt=\"[" + icon.toUpperCase() + "]\" class=\"fontIcon\"/>";
        //return "[" + icon + "]";
    }

    // analyze {} inside s
    public static String analyzeBraces(String s, String language, String modifierID) {
        int l = 0;
        int r = -1;
        String s1 = "";
        while ((l = s.indexOf("{", r)) != -1) {
            s1 += s.substring(r + 1, l);
            int c = 1;
            for (r = l + 1; r < s.length(); r++) {
                if (s.charAt(r) == '{') {
                    c++;
                }
                if (s.charAt(r) == '}') {
                    c--;
                }
                if (c == 0) {
                    break;
                }
            }
            if (r != s.length()) {
                String sub = s.substring(l + 1, r);
                String text = getText(sub, language, modifierID);
                if (text == null) {
                    text = tryGetRawText(sub, "en_US");
                    if (text != null) {
                        text = analyzeBraces(text, language, modifierID);
                    }
                }
                if (text == null) {
                    Statement gameplay = null;
                    try {
                        String var = null;
                        String format = null;
                        int colon = sub.indexOf(":");
                        if (colon != -1) {
                            var = sub.substring(0, colon);
                            var = var.replaceAll("[\\s]*$", "");
                            format = sub.substring(colon + 1, sub.length());
                            format = format.replaceAll("^[\\s]", "");
                        } else {
                            var = sub;
                        }
                        String value = null;
                        gameplay = DriverManager.getConnection(GAMEPLAY_DATABASE).createStatement();
                        ResultSet r1 = gameplay.executeQuery("select * from ModifierArguments where ModifierId = \"" + modifierID + "\" and lower(Name) = lower(\"" + sub.split("\\W")[0] + "\");");
                        if (r1.next()) {
                            value = r1.getString("Value");
                            if (value != null && value.startsWith("LOC_")) {
                                value = Tools.getText(value, language);
                            }
                        }
                        if (format != null && format.startsWith("plural ")) {
                            String[] cases = format.substring("plural ".length(), format.length()).split(";");
                            String other = null;
                            for (String ca : cases) {
                                int question = ca.indexOf("?");
                                if (question != -1) {
                                    String condition = ca.substring(0, question).replaceAll("\\s", "");
                                    String txt = ca.substring(question + 1, ca.length());
                                    if (condition.equals(value)) {
                                        text = txt;
                                    } else if (condition.equals("other")) {
                                        other = txt;
                                    }
                                }
                            }
                            if (text == null) {
                                text = other;
                            }
                        } else {
                            text = value;
                        }
                    } catch (Exception e) {
                    } finally {
                        if (gameplay != null) {
                            try {
                                gameplay.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
                if (text != null) {
                    s1 += text;
                }
            } else {
                s1 += s.substring(l, r);
                r = s.length() - 1;
            }
        }
        s1 += s.substring(r + 1, s.length());
        return s1;
    }

    // analyze [] inside s
    public static String analyzeBrackets(String s) {
        int l = 0;
        int r = -1;
        String s1 = "";
        while ((l = s.indexOf("[", r)) != -1) {
            s1 += s.substring(r + 1, l);
            r = s.indexOf("]", l);
            String icon = s.substring(l + 1, r);
            if (icon.equals("NEWLINE")) {
                s1 += "<br/>\n";
            } else {
                s1 += getIcon(icon);
            }
        }
        s1 += s.substring(r + 1, s.length());
        return s1;
    }

    public static String tryGetRawText (String tag, String language) {
        Statement text = null;
        try {
            text = DriverManager.getConnection(TEXT_DATABASE).createStatement();
            ResultSet r1 = text.executeQuery("select * from LocalizedText where Language = \"" + language + "\" and Tag = \"" + tag + "\";");
            r1.next();
            String s = r1.getString("Text");
            text.close();
            if ((s.contains("TRANSLATION MISSING") || s.contains("MISMATCHED TRANSLATION"))) {
                return null;
            }
            if (s.isEmpty()) {
                return null;
            }
            return s;
        } catch (Exception e) {
        } finally {
            if (text != null) {
                try {
                    text.close();
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    public static String tryGetText(String tag, String language, String modifierID) {
        String raw = tryGetRawText(tag, language);
        if (raw != null) {
            return analyzeBraces(raw, language, modifierID);
        }
        return null;
    }

    public static BufferedImage getImageIgnoringCapital(String name) {
        try {
            Statement extra = DriverManager.getConnection(EXTRA_DATABASE).createStatement();
            ResultSet r1 = extra.executeQuery("select * from IconDefinitions where lower(Name) = lower(\"" + name + "\");");
            int num = 0;
            while (r1.next()) {
                num ++;
            }
            if (num > 0) {
                r1 = extra.executeQuery("select * from IconDefinitions where lower(Name) = lower(\"" + name + "\");");
                for (int i = 0; i < num; i++) {
                    r1.next();
                }
                String atlas = r1.getString("Atlas");
                int index = Integer.parseInt(r1.getString("Idx"));
                BufferedImage image = tryGetImage(atlas, index);
                if (image != null) return image;
            }
        } catch (Exception e) {
        }
        //System.out.println("can't find image " + name);
        return null;
    }

    public static BufferedImage getImage(String name) {
        try {
            Statement extra = DriverManager.getConnection(EXTRA_DATABASE).createStatement();
            ResultSet r1 = extra.executeQuery("select * from IconDefinitions where Name = \"" + name + "\";");
            int num = 0;
            while (r1.next()) {
                num ++;
            }
            if (num > 0) {
                r1 = extra.executeQuery("select * from IconDefinitions where lower(Name) = lower(\"" + name + "\");");
                for (int i = 0; i < num; i++) {
                    r1.next();
                }
                String atlas = r1.getString("Atlas");
                int index = Integer.parseInt(r1.getString("Idx"));
                BufferedImage image = tryGetImage(atlas, index);
                if (image != null) return image;
            }
        } catch (Exception e) {
        }
        //System.out.println("can't find image " + name);
        return null;
    }

    public static BufferedImage tryGetImage(String atlas, int index) {
        try {
            Statement extra = DriverManager.getConnection(EXTRA_DATABASE).createStatement();
            ResultSet r2 = extra.executeQuery("select * from IconTextureAtlases where Name = \"" + atlas + "\" order by IconSize desc;");
            r2.next();
            int perRow = r2.getInt("IconsPerRow");
            int perColumn = r2.getInt("IconsPerColumn");
            String fileName = r2.getString("FileName");
            extra.close();
            if (!fileName.endsWith(".dds")) {
                fileName += ".dds";
            }
            File file = null;
            for (String folder : DDS_FOLDERS) {
                File f = new File(folder + "/" + fileName);
                if (f.exists()) {
                    file = f;
                }
            }
            BufferedImage image = ImageEditor.cutImage(file, perRow, perColumn, index);
            return image;
        } catch (Exception e) {
        }
        return null;
    }

    public static List<Element> readAll(Element source, String tag1, String tag2) {
        List<Element> list = new ArrayList<>();
        NodeList nodes1 = source.getElementsByTagName(tag1);
        for (int i = 0; i < nodes1.getLength(); i++) {
            NodeList nodes2 = ((Element) nodes1.item(i)).getElementsByTagName(tag2);
            for (int j = 0; j < nodes2.getLength(); j++) {
                list.add((Element) nodes2.item(j));
            }
        }
        return list;
    }

    public static String readFromFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String s = "";
        String line;
        while ((line = reader.readLine()) != null) {
            s += line + "\n";
        }
        reader.close();
        return s;
    }
}
