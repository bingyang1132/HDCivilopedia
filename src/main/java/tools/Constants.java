package tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

public interface Constants {
    // public static final String STEAM_FOLDER = "C:/Program Files (x86)/Steam/steamapps";
    public static final String STEAM_FOLDER = "E:/SteamLibrary/steamapps";
    
    // public static final String DATABASES_SOURCE = "C:\\Users\\xiaoxiao\\AppData\\Local\\Firaxis Games\\Sid Meier's Civilization VI\\Cache";
    // public static final String DATABASES_SOURCE = "C:\\Users\\1132\\AppData\\Local\\Firaxis Games\\Sid Meier's Civilization VI\\Cache";
    // public static final String DATABASES_SOURCE = "C:\\Users\\1132\\Desktop\\hdciv\\backup\\Cache_1.3.9";
    public static final String DATABASES_SOURCE = "E:\\hdciv\\backup\\Cache";
    // public static final String DATABASES_SOURCE = "C:\\Users\\1132\\Desktop\\hdciv\\backup\\Cache_1.4.0_cat";
    
    // databases
    public static final String DATABASES = "jdbc:sqlite:database";
    public static final String GAMEPLAY_DATABASE = DATABASES + "/" + "DebugGameplay.sqlite";
    public static final String EXTRA_DATABASE = DATABASES + "/" + "extra.sqlite";
    public static final String TEXT_DATABASE = DATABASES + "/" + "DebugLocalization.sqlite";
    public static final String CONFIG_DATABASE = DATABASES + "/" + "DebugConfiguration.sqlite";  // 原来少了这么一个db 
    public static final String NOHD_TEXT_DATABASE = DATABASES + "/" + "nohd_DebugLocalization.sqlite";

    public static final String HD_MOD = "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity";

    // changelog
    public static final String CHANGELOG = HD_MOD + "/Changelog";
    
    public static final String PLAYER_COLORS = STEAM_FOLDER + "/" + "common/Sid Meier's Civilization VI/Base/Assets/UI/Colors";
    public static final String EXTRA_SCH = "extra.sql";
    public static final List<String> DDS_FOLDERS = getDDSFolders();

    public static final String XLS = "Texts.xlsx";
    
    // for icons
    public static List<String> getDDSFolders () {
        String[] base = new String[] {
            // civ6 SDK Assets(SDK needed)
            "E:/SteamLibrary/steamapps/common/Sid Meier's Civilization VI SDK Assets/pantry/Textures",
            "E:/SteamLibrary/steamapps/common/Sid Meier's Civilization VI SDK Assets/Civ6/pantry/Textures",
            "E:/SteamLibrary/steamapps/common/Sid Meier's Civilization VI SDK Assets/Civ6/DLC/Shared/pantry/Textures",
            "E:/SteamLibrary/steamapps/common/Sid Meier's Civilization VI SDK Assets/Civ6/DLC/Expansion2/pantry/Textures",
            "E:/SteamLibrary/steamapps/common/Sid Meier's Civilization VI SDK Assets/Civ6/DLC/Expansion1/pantry/Textures",
            "E:/SteamLibrary/steamapps/common/Sid Meier's Civilization VI SDK Assets/Civ6/DLC/CivRoyaleScenario/pantry/Textures", 

            // hd  !needs to be updated after new contents be added in the Assets folder
            // other textures
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/HD_CityStatesDiversity/Textures",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/Assets/Buildings",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/Assets/CityStates",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/Assets/GreatPeople",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/Assets/GreatWorks",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/Assets/Policies",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/Assets/Projects",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/Assets/Resourceful2",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/Assets/TechCivics",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/Assets/UnitActions",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/Assets/Units",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/Assets/Units/MedivalPirate",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/Assets/Wetlands",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/Assets/WonderResources",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/Assets/HDRESOURCE",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/GameModeSupport/MilitaryMode/ArmsComplement/Assets",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/GameModeSupport/ReligiousArmMode/Assests",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/Civ6HarmonyInDiversity/ThirdParty/EpsBarbarians",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/HD_CivilizationsDiversity/Assets",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/HD_CorporationsDiversity/Assets/Airport",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/HD_CorporationsDiversity/Assets/Khalifa",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/HD_CorporationsDiversity/Assets/Merchant",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/HD_CorporationsDiversity/Assets/Monopoly",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/HD_CorporationsDiversity/Assets/Porcelain",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/HD_CorporationsDiversity/Assets/Resourceful2",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/HD_CorporationsDiversity/Assets/Suk",
            "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/HD_DistrictsDiversity/Assets",

            // real great people
            "E:/SteamLibrary/steamapps/workshop/content/289070/2383232087/modcompatibility",
            "E:/SteamLibrary/steamapps/workshop/content/289070/2383232087/Icons/Admiral", 
            "E:/SteamLibrary/steamapps/workshop/content/289070/2383232087/Icons/Artist", 
            "E:/SteamLibrary/steamapps/workshop/content/289070/2383232087/Icons/Engineer", 
            "E:/SteamLibrary/steamapps/workshop/content/289070/2383232087/Icons/General", 
            "E:/SteamLibrary/steamapps/workshop/content/289070/2383232087/Icons/Merchant", 
            "E:/SteamLibrary/steamapps/workshop/content/289070/2383232087/Icons/Musician", 
            "E:/SteamLibrary/steamapps/workshop/content/289070/2383232087/Icons/Prophet", 
            "E:/SteamLibrary/steamapps/workshop/content/289070/2383232087/Icons/Scientist", 
            "E:/SteamLibrary/steamapps/workshop/content/289070/2383232087/Icons/Writer",

            // other mods
            "E:/SteamLibrary/steamapps/workshop/content/289070/1185630489/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/1293801965/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/1312585482/Icons",
            "E:/SteamLibrary/steamapps/workshop/content/289070/1369684991/Icons",
            "E:/SteamLibrary/steamapps/workshop/content/289070/1579019534/Icons",
            "E:/SteamLibrary/steamapps/workshop/content/289070/1580369598/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/1601259406/UI",
            "E:/SteamLibrary/steamapps/workshop/content/289070/1628605090",
            "E:/SteamLibrary/steamapps/workshop/content/289070/1647124021/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/1651487987/Art",
            "E:/SteamLibrary/steamapps/workshop/content/289070/1679150838/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/1702339134/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/1709115371/Icons",
            "E:/SteamLibrary/steamapps/workshop/content/289070/1752329484",
            "E:/SteamLibrary/steamapps/workshop/content/289070/2087866677/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/2104384400/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/2155632734/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/2186513760/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/2276345682",
            "E:/SteamLibrary/steamapps/workshop/content/289070/2402394695/Icons/ModSupport",
            "E:/SteamLibrary/steamapps/workshop/content/289070/2460661464",
            "E:/SteamLibrary/steamapps/workshop/content/289070/2494925002/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/2533189420",
            "E:/SteamLibrary/steamapps/workshop/content/289070/2553831629",
            "E:/SteamLibrary/steamapps/workshop/content/289070/2562033833/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/2794603014/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/870850597/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/882664162/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/900089445/Icons/Admiral",
            "E:/SteamLibrary/steamapps/workshop/content/289070/900089445/Icons/Artist",
            "E:/SteamLibrary/steamapps/workshop/content/289070/900089445/Icons/Engineer",
            "E:/SteamLibrary/steamapps/workshop/content/289070/900089445/Icons/General",
            "E:/SteamLibrary/steamapps/workshop/content/289070/900089445/Icons/Merchant",
            "E:/SteamLibrary/steamapps/workshop/content/289070/900089445/Icons/Musician",
            "E:/SteamLibrary/steamapps/workshop/content/289070/900089445/Icons/Prophet",
            "E:/SteamLibrary/steamapps/workshop/content/289070/900089445/Icons/Scientist",
            "E:/SteamLibrary/steamapps/workshop/content/289070/900089445/Icons/Writer",
            "E:/SteamLibrary/steamapps/workshop/content/289070/900089445/UI",
            "E:/SteamLibrary/steamapps/workshop/content/289070/933787677/Textures",
            "E:/SteamLibrary/steamapps/workshop/content/289070/970184582/Icons"


        };
        List<String> folders = new ArrayList<>();
        for (String folder : base) {
            folders.add(folder);
        }
        File mod = new File(Tools.STEAM_FOLDER + "/workshop/content/289070");
        for (File sub : mod.listFiles()) {
            File dds1 = new File(sub, "Textures");
            if (dds1.exists() && dds1.isDirectory()) {
                folders.add(dds1.getAbsolutePath());
            }
            File dds2 = new File(sub, "Icons");
            if (dds2.exists() && dds2.isDirectory()) {
                folders.add(dds2.getAbsolutePath());
            }
        }
        // The HD mod's own art folders are listed above one by one, which goes stale every time
        // the mod adds one -- Assets/Resources, Assets/Districts and six others were missing, so
        // 56 .dds were unreachable and their icons silently disappeared. Walk its tree instead.
        addFoldersWithDDS(new File(HD_MOD + "/Assets"), folders);
        return folders;
    }

    /** Adds every directory under {@code root} that directly contains a .dds. */
    // Constants is an interface, so this cannot be private on Java 8
    static void addFoldersWithDDS (File root, List<String> folders) {
        if (!root.isDirectory()) {
            return;
        }
        File[] children = root.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                addFoldersWithDDS(child, folders);
            } else if (child.getName().toLowerCase().endsWith(".dds")
                    && !folders.contains(root.getAbsolutePath())) {
                folders.add(root.getAbsolutePath());
            }
        }
    }

    public static final String IMAGE_URL = "../../../icons";
    public static final String LINK_URL = "../../..";

    public static final String STYLES = "\t\t<style>\n\t\t\timg{vertical-align: text-bottom;}\n\t\t\t.index{width: auto; float: left; margin: 1% 1% 1% 1%; padding: 0% 1% 0% 1%; background-color: whitesmoke; box-shadow: 5px 5px 5px whitesmoke; font-size: large; line-height:27px;}\n\t\t\t.main{width: 600px; position: absolute; left:50%; margin: 1% 0px 1% -300px; padding: 0% 1% 0% 1%; background-color: whitesmoke; box-shadow: 5px 5px 5px whitesmoke; font-size: large;}\n\t\t</style>\n";
}
