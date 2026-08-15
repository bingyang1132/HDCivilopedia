package tools;

import java.io.File;

public interface Constants {

    // ---- machine-specific roots -------------------------------------------------------------
    // Values live in config.properties (git-ignored; see config.example.properties). The
    // fallbacks below are the stock install locations, derived from the environment rather than
    // hardcoded, so a checkout with no config file still points somewhere sensible.
    // Config.report() says which of them do not exist.

    public static final String STEAM_FOLDER = Config.get("steam.folder",
            "C:/Program Files (x86)/Steam/steamapps");
    public static final String MODS_FOLDER = Config.get("mods.folder",
            Config.underHome("/Documents/My Games/Sid Meier's Civilization VI/Mods"));
    public static final String HD_MOD = MODS_FOLDER + "/" + Config.get("hd.mod.folder",
            "Civ6HarmonyInDiversity");
    /** The game's Cache directory the databases are rebuilt from. */
    public static final String DATABASES_SOURCE = Config.get("cache.source",
            Config.underLocalAppData("/Firaxis Games/Sid Meier's Civilization VI/Cache"));

    // ---- derived ----------------------------------------------------------------------------

    public static final String GAME_ASSETS = STEAM_FOLDER + "/common/Sid Meier's Civilization VI";
    public static final String SDK_ASSETS = STEAM_FOLDER + "/common/Sid Meier's Civilization VI SDK Assets";
    public static final String WORKSHOP = STEAM_FOLDER + "/workshop/content/289070";
    public static final String PLAYER_COLORS = GAME_ASSETS + "/Base/Assets/UI/Colors";
    public static final String CHANGELOG = HD_MOD + "/Changelog";

    // databases
    public static final String DATABASES = "jdbc:sqlite:database";
    public static final String GAMEPLAY_DATABASE = DATABASES + "/" + "DebugGameplay.sqlite";
    public static final String EXTRA_DATABASE = DATABASES + "/" + "extra.sqlite";
    public static final String TEXT_DATABASE = DATABASES + "/" + "DebugLocalization.sqlite";
    public static final String CONFIG_DATABASE = DATABASES + "/" + "DebugConfiguration.sqlite";
    public static final String NOHD_TEXT_DATABASE = DATABASES + "/" + "nohd_DebugLocalization.sqlite";

    public static final String EXTRA_SCH = "extra.sql";
    public static final String XLS = "Texts.xlsx";

    /**
     * The .dds behind an atlas file name, or null if it is not on disk anywhere under the roots.
     *
     * This replaced a hand-written list of 83 absolute paths. That list rotted: 31 entries were
     * dead when checked, mostly because mod folders had been renamed, and each dead entry lost a
     * whole atlas worth of icons with no error anywhere. Discovery cannot go stale.
     *
     * A method rather than a constant so the walk happens on the first icon lookup instead of the
     * first touch of this interface -- `page` and `audit` never need it.
     */
    public static File ddsFile (String fileName) {
        String path = DdsFolders.FILES.get(fileName.toLowerCase());
        return path == null ? null : new File(path);
    }

    public static final String IMAGE_URL = "../../../icons";
    public static final String LINK_URL = "../../..";

    public static final String STYLES = "\t\t<style>\n\t\t\timg{vertical-align: text-bottom;}\n\t\t\t.index{width: auto; float: left; margin: 1% 1% 1% 1%; padding: 0% 1% 0% 1%; background-color: whitesmoke; box-shadow: 5px 5px 5px whitesmoke; font-size: large; line-height:27px;}\n\t\t\t.main{width: 600px; position: absolute; left:50%; margin: 1% 0px 1% -300px; padding: 0% 1% 0% 1%; background-color: whitesmoke; box-shadow: 5px 5px 5px whitesmoke; font-size: large;}\n\t\t</style>\n";
}
