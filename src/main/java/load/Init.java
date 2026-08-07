package load;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import org.sqlite.Function;

import tools.ImageEditor;
import tools.Tools;
import view.Page;

public class Init {
    
    public static File loading;
    public static Set<File> errorFiles = new HashSet<>();

    public static final String[] NOT_CRETIRIA = new String[] {
        "UC", 
        "Ethiopia_Mode", 
        "Ethiopia_Mode_Expansion1", 
        "Ethiopia_Mode_Expansion2", 
        "Ethiopia_Mode_Expansion2_GranColombia_Maya"
    };
    public static final Set<String> SKIP_MODINFO_KEYWORDS = new HashSet<>(Arrays.asList(
        "Cheat",
        "BetterLoadingScreen"
    ));

    // ---- performance / test knobs ----
    // -Dhd.verbose=true (or env HD_VERBOSE) re-enables the noisy per-file/per-row logging.
    public static final boolean VERBOSE = parseBool(System.getProperty("hd.verbose", System.getenv("HD_VERBOSE")));
    // -Dhd.limit=N (or env HD_LIMIT) caps how many requests loadDLCs processes, for quick smoke runs.
    public static final int LIMIT = parseInt(System.getProperty("hd.limit", System.getenv("HD_LIMIT")), -1);

    // Mods (by folder name under any scanned mods/DLC dir) to ignore entirely. These are
    // present locally but not wanted in the manual build — e.g. "Horse" bundles its own
    // copy of BetterChinese that loads after HD and clobbers HD's text overrides.
    // Matching is case-insensitive. Extend at runtime with -Dhd.skipMods=Name1,Name2 (or env HD_SKIP_MODS).
    public static final Set<String> SKIP_MODS = buildSkipMods(
        "Horse",
        "OneProduction",
        "NormalCiv2",
        "NormalCiv3"
    );

    private static Set<String> buildSkipMods(String... defaults) {
        Set<String> set = new HashSet<>();
        for (String d : defaults) set.add(d.toLowerCase());
        String extra = System.getProperty("hd.skipMods", System.getenv("HD_SKIP_MODS"));
        if (extra != null) {
            for (String s : extra.split(",")) {
                if (!s.trim().isEmpty()) set.add(s.trim().toLowerCase());
            }
        }
        return set;
    }

    private static boolean parseBool(String s) {
        return s != null && s.equalsIgnoreCase("true");
    }

    private static int parseInt(String s, int def) {
        try { return s == null ? def : Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    /** Prints how long a named init step took. */
    static void logTime(String name, long startNano) {
        System.out.printf("[TIME] %-18s %8.2fs%n", name, (System.nanoTime() - startNano) / 1e9);
    }

    /**
     * Tunes a connection for fast bulk writing: in-memory journal + no fsync (the
     * databases are throwaway copies rebuilt from the game cache), and turns off
     * autocommit so writes are batched into transactions instead of one fsync per row.
     */
    static void tuneForBulkWrite(Connection c) {
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA journal_mode=MEMORY;");
            s.execute("PRAGMA synchronous=OFF;");
        } catch (Exception e) {
            // pragmas are best-effort
        }
        try { c.setAutoCommit(false); } catch (Exception e) {}
    }

    /** Commits a non-autocommit connection, swallowing errors. */
    static void commitQuietly(Connection c) {
        try { if (c != null && !c.getAutoCommit()) c.commit(); } catch (Exception e) {}
    }

    /** Commits and closes a connection used for bulk writing. */
    static void finishBulkWrite(Connection c) {
        commitQuietly(c);
        try { if (c != null && !c.isClosed()) c.close(); } catch (Exception e) {}
    }


    // copy databases from usr file
    public static void copyDatabases() throws IOException {
        
        // DebugGameplay.sqlite
        // DebugLocalization.sqlite
        
        File databaseFolder = new File(Tools.DATABASES_SOURCE);  // "C:\\Users\\xiaoxiao\\AppData\\Local\\Firaxis Games\\Sid Meier's Civilization VI\\Cache" original database
        // print database folder
        System.out.println(databaseFolder.getAbsolutePath());
        File destFolder = new File("database");  // "./database" localized database instead of the original one
        // clear dest folder
        if(destFolder.exists()) {
            if(destFolder.isDirectory()) {
                for(File f : destFolder.listFiles()) {
                    f.delete();
                }
            }
            destFolder.delete();
        }
        destFolder.mkdir();

        for(File file : databaseFolder.listFiles()) {
			if (file.isDirectory()) {
				continue;
			}
            File dest = new File(destFolder, file.getName());
            if(dest.exists()) {
                dest.delete();
            }
            dest.createNewFile();
            // readin cache files and save at dest
            InputStream in = new FileInputStream(file);
            OutputStream out = new FileOutputStream(dest);
            byte[] buf = new byte[64];
            int len;
            while((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
            out.close();
            in.close();
            
            // deal with localization file separately
            if (file.getName().contains("Localization")) {
                File dest_nohd = new File(destFolder, "nohd_" + file.getName());
                if(dest_nohd.exists()) {
                    dest_nohd.delete();
                }
                dest_nohd.createNewFile();
                in = new FileInputStream(file);
                OutputStream out_nohd = new FileOutputStream(dest_nohd);
                while((len = in.read(buf)) != -1) {
                    out_nohd.write(buf, 0, len);
                }
                out_nohd.flush();
                out_nohd.close();
                in.close();
            }
        }
    }

    // build extra.sqlite
    public static void addTables() throws SQLException {
        /*
         * parse all the .modinfo files
         * parse all the modifications to the game database in them
         * record and store them in Request format along with the corresponding sql/xml source code files.
         */
        Connection connection = DriverManager.getConnection(Tools.EXTRA_DATABASE); // ./databser/extra.sqlite
        // // 注册自定义 Make_Hash 函数
        // Function.create(connection, "Make_Hash", new Function() {
        //     @Override
        //     protected void xFunc() throws SQLException {
        //         if (args() != 1) {
        //             throw new SQLException("Make_Hash requires exactly one argument");
        //         }
        //         String input = value_text(0); // 获取函数的第一个参数
        //         if (input == null) {
        //             result((String) null); // 如果输入为 null，返回 null
        //         } else {
        //             // 生成哈希值，这里使用 Java 的 hashCode 并转换为十六进制
        //             result(Integer.toHexString(input.hashCode()));
        //         }
        //     }
        // });

        tuneForBulkWrite(connection);
        Statement statement = connection.createStatement();

        // load extra tables in extra.sqlite
        try {
            DataBaseLoader.loadSQL(new File(Tools.STEAM_FOLDER + "/common/Sid Meier's Civilization VI/Base/Assets/Database/ColorManager.sql"), statement);
            DataBaseLoader.loadSQL(new File(Tools.STEAM_FOLDER + "/common/Sid Meier's Civilization VI/Base/Assets/Database/IconManager.sql"), statement);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // load players colors in extra.sqlite
        try {
            DataBaseLoader.loadXMLAsData(new File(Tools.STEAM_FOLDER + "/common/Sid Meier's Civilization VI/Base/Assets/UI/Colors/PlayerColors.xml"), statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            DataBaseLoader.loadXMLAsData(new File(Tools.STEAM_FOLDER + "/common/Sid Meier's Civilization VI/Base/Assets/UI/Colors/PlayerStandardColors.xml"), statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 若干 mod 的 Colors.sql 往 PlayerColors 写 TextColor，但基础游戏的 PlayerColors.xml 没这列，
        // 整条 insert 会被拒。补上该列，让这些 mod 的配色能落库。
        try {
            statement.execute("alter table PlayerColors add column TextColor TEXT;");
        } catch (Exception e) {
            // 已存在则忽略
        }


        // load icon table in extra.sqlite
        File folder = new File(Tools.STEAM_FOLDER + "/common/Sid Meier's Civilization VI/Base/Assets/UI/Icons");
        for(File file : folder.listFiles()) {
            try {
                DataBaseLoader.loadXMLAsData(file, statement);
            } catch (Exception e) {
                // e.printStackTrace();
                logError("Error processing file: " + file.getAbsolutePath() + "\n" + e.getMessage());
            }
        }
        statement.close();
        finishBulkWrite(connection);
    }

    // create a request class to store the mod information, like a package of .modinfo file
    static class Request implements Comparable<Request> {
        String type;
        int order;
        List<File> files;
        String mod;
        @Override
        public int compareTo(Request r) {
            return order - r.order; 
        }

        @Override
        public String toString() {
            return "Request{" +
                    "type='" + type + '\'' +
                    ", order=" + order +
                    ", mod='" + mod + '\'' +
                    ", files=" + files +
                    '}';
        }
    }

    // load DLCs and mods into database
    public static void loadDLCs () throws Exception {
        List<Request> feaRequests = new ArrayList<>();
        List<Request> igaRequests = new ArrayList<>();

        // all DLCs and mods for civ6
        File folder1 = new File(Tools.STEAM_FOLDER + "/common/Sid Meier's Civilization VI/DLC");  // DLCs
        File folder2 = new File(Tools.STEAM_FOLDER + "/workshop/content/289070");  // workshop mods
        File folder3 = new File("C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods");  // local mods
        // add mods into the dlcs list for further loading
        List<File> dlcs = new ArrayList<>();
        for (File f : folder1.listFiles()) {
            dlcs.add(f);
        }
        for (File f : folder2.listFiles()) {
            dlcs.add(f);
        }
        for (File f : folder3.listFiles()) {
            dlcs.add(f);
            System.out.println("local mod: " + f.getAbsolutePath());
        }

        // load requests from mods&DLCs
        for (File dlc : dlcs) {  // for each mod/dlc file
            boolean skip_flag = false;
            // directory only
            if(dlc.isFile()) {
                continue;
            }
            // skip explicitly-blacklisted mods (see SKIP_MODS), case-insensitive
            if(SKIP_MODS.contains(dlc.getName().toLowerCase())) {
                System.out.println("Skipping blacklisted mod: " + dlc.getName());
                continue;
            }
            // skip scenarios
            if(dlc.getName().endsWith("Scenario") || dlc.getName().contains("Randomizer") || dlc.getName().contains("Mode")) {
                continue;
            }

            // legal mods always have a .modinfo file(XML format), skip illegal ones
            File modInfo = null;
            for (File child : dlc.listFiles()) {
                if(child.getName().endsWith(".modinfo")) {
                    // filter
                    boolean shouldSkip = SKIP_MODINFO_KEYWORDS.stream().anyMatch(keyword -> child.getName().contains(keyword));
                    if (shouldSkip) {
                        System.out.println("Skipping modinfo file due to filter: " + child.getName());
                        logError("Skipped modinfo file: " + child.getAbsolutePath());
                        skip_flag = true;
                    }
                    modInfo = child;
                    break;
                }
            }
            if (skip_flag) {
                skip_flag = false;
                continue;
            }
            if (modInfo == null) {
                System.out.println("can't find .modinfo in " + dlc.getAbsolutePath());
                continue;
            }
            // parse modinfo file and get id
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(modInfo);  // mod doc
            System.out.println("Parsing modinfo file: " + modInfo.getAbsolutePath());
            Element root = document.getDocumentElement();

            String modID = root.getAttribute("id");

            // skip specific mods
            // Eastern Zhou and Baiyue City States
            if (modID.equals("6861b067-5bc1-4c4b-b229-725d9bbb659d") || modID.equals("56382b36-05be-46a3-831d-06df8e48c12a")) {
                continue;
            }

            // parse <frontEndActions>
            Element frontEndActions = (Element) root.getElementsByTagName("FrontEndActions").item(0);
            if (frontEndActions != null) {
                NodeList feaList = frontEndActions.getChildNodes();

                for (int i = 0; i < feaList.getLength(); i++) { // for each item in feaList

                    // filter texts for unsuitable modes
                    NamedNodeMap map = feaList.item(i).getAttributes();
                    if (map != null) {
                        String criteria = null;
                        try {
                            criteria = map.getNamedItem("criteria").getTextContent();
                        } catch (Exception e) {
                        }
                        if (criteria != null) {
                            boolean exist = false;
                            for (String not : NOT_CRETIRIA) {
                                if (criteria.equals(not)) {
                                    exist = true;
                                    break;
                                }
                            }
                            if (exist) {
                                continue;
                            }
                        }
                    }

                    // classify the request by type, only updateColors, UpdateIcons, UpdateText, UpdateDatabase are valid tags
                    String type = feaList.item(i).getNodeName();
                    if(!type.equals("UpdateColors") && !type.equals("UpdateIcons") && !type.equals("UpdateText") &&! type.equals("UpdateDatabase")) {
                        continue;
                    }
                    Element update = (Element) feaList.item(i);
                    Request request = new Request();
                    request.type = type;
                    request.mod = modID;
                    Element property = (Element) update.getElementsByTagName("Properties").item(0);
                    // get the load order of the request
                    if (property == null) {
                        request.order = 0;
                    } else {
                        request.order = Integer.parseInt(property.getElementsByTagName("LoadOrder").item(0).getTextContent());
                    }
                    // get the files of the request, save them into a list
                    List<File> files = new ArrayList<>();
                    NodeList fileNodes = update.getElementsByTagName("File");
                    for (int j = 0; j < fileNodes.getLength(); j++) {
                        File file = new File(dlc, fileNodes.item(j).getTextContent());
                        if (file.exists()) {
                            files.add(file);
                            System.out.println("Found file: " + file.getAbsolutePath());
                        } else {
                            System.out.println("Skipping missing file: " + file.getAbsolutePath());
                            logError("Missing file in request: " + file.getAbsolutePath());
                        }
                    }
                    // 如果没有有效文件，跳过该 Request
                    if (files.isEmpty()) {
                        System.out.println("Skipping request due to all files missing: " + update.getNodeName() + " in mod " + modID);
                        logError("Skipping request due to all files missing: " + update.getNodeName() + " in mod " + modID);
                        continue;
                    }
                    // for (int j = 0; j < fileNodes.getLength(); j++) {
                    //     files.add(new File(dlc, fileNodes.item(j).getTextContent()));
                    // }
                    request.files = files;
                    feaRequests.add(request);
                }
            }
            
            // parse <inGameActions>
            Element inGameActions = (Element) root.getElementsByTagName("InGameActions").item(0);
            if (inGameActions != null) {
                // igas in a modinfo file
                NodeList igaList = inGameActions.getChildNodes();
                for (int i = 0; i < igaList.getLength(); i++) {
                    // i th iga change
                    NamedNodeMap map = igaList.item(i).getAttributes();
                    if (map != null) {
                        String criteria = null;
                        try {
                            criteria = map.getNamedItem("criteria").getTextContent();
                        } catch (Exception e) {
                        }
                        if (criteria != null) {
                            boolean exist = false;
                            for (String not : NOT_CRETIRIA) {
                                if (criteria.equals(not)) {
                                    exist = true;
                                    break;
                                }
                            }
                            if (exist) {
                                continue;
                            }
                        }
                    }
                    String type = igaList.item(i).getNodeName();
                    if(!type.equals("UpdateColors") && !type.equals("UpdateIcons") && !type.equals("UpdateText") &&! type.equals("UpdateDatabase")) {
                        continue;
                    }
                    Element update = (Element) igaList.item(i);  // "update": a item in igaList
                    // sikp the following criteria of game mode
                    String criteria = update.getAttribute("criteria");
                    if (criteria.contains("BarbarianClans_Mode")
                        || criteria.contains("SecretSocieties_Mode")
                        || criteria.contains("Dramatic_Mode")
                        || criteria.contains("Dramatic_Ages_Mode")
                        || criteria.contains("Apocalypse_Mode")
                        || criteria.contains("Heroes_Mode")
                        || criteria.contains("TowerDefense_Mode")
                        || criteria.contains("TreeRandomizer_Mode")
                        || criteria.contains("AITest_Mode")
                        || criteria.contains("Testing_Mode")
                        || criteria.contains("TechTest_Mode")
                        || criteria.contains("Wild_Mode")
                        || criteria.contains("MPTest_Mode")
						|| criteria.contains("DevTest_Mode")
                        || criteria.contains("Heroes")
                        || criteria.contains("hero")
                        || criteria.contains("Test")
                        || criteria.contains("test")
                        ) {
                        // 豁免hd相关mod
                        // if (!(request.mod.equals("521b8777-0977-4859-a5ee-3e411a732e5c") // hd main
                        // || request.mod.equals("66add898-b3bb-4bd9-98a2-805d37f0da2e") // hd district
                        // || request.mod.equals("c0417322-9747-42d5-9717-b0df5a4c6e5d") // hd city-states
                        // || request.mod.equals("c086b5a6-90d2-4dea-a32f-c642639b9469") // hd I&C
                        // || request.mod.equals("7d155dc3-4a94-4923-a306-d8fd40fb0003") // hd Australia
                        // )) continue;

                        System.out.println("Skipping request" + update.getNodeName() + " with id " + update.getAttribute("id") + " due to criteria: " + criteria);
                        continue;
                    }
                    
                    Request request = new Request();
                    request.type = type;
                    request.mod = modID;
                    Element property = (Element) update.getElementsByTagName("Properties").item(0);
                    if (property == null) {
                        request.order = 0;
                    } else {
                        request.order = Integer.parseInt(property.getElementsByTagName("LoadOrder").item(0).getTextContent());
                    }
                    List<File> files = new ArrayList<>();
                    NodeList fileNodes = update.getElementsByTagName("File");
                    for (int j = 0; j < fileNodes.getLength(); j++) {
                        File file = new File(dlc, fileNodes.item(j).getTextContent());
                        if (file.exists()) {
                            files.add(file);
                        } else {
                            System.out.println("Skipping missing file: " + file.getAbsolutePath());
                            logError("Missing file in request: " + file.getAbsolutePath());
                        }
                    }
                    if (files.isEmpty()) {
                        System.out.println("Skipping request due to all files missing: " + update.getNodeName() + " in mod " + modID);
                        logError("Skipping request due to all files missing: " + update.getNodeName() + " in mod " + modID);
                        continue;
                    }
                    // for (int j = 0; j < fileNodes.getLength(); j++) {
                    //     files.add(new File(dlc, fileNodes.item(j).getTextContent()));
                    // }
                    request.files = files;
                    igaRequests.add(request);
                }
            }
        }
        
        // sort requests
        System.out.println("Sorting requests...");
        System.out.println("Total frontEndActions requests: " + feaRequests.size());
        System.out.println("Total inGameActions requests: " + igaRequests.size());
        Collections.sort(feaRequests);
        Collections.sort(igaRequests);
        System.out.println("Sorting completed.");

        Connection gameplayConn = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE);
        Connection extraConn = DriverManager.getConnection(Tools.EXTRA_DATABASE);
        Connection textConn = DriverManager.getConnection(Tools.TEXT_DATABASE);
        Connection configConn = DriverManager.getConnection(Tools.CONFIG_DATABASE); // debugConfiguration
        Connection nohdTextConn = DriverManager.getConnection(Tools.NOHD_TEXT_DATABASE);
        for (Connection c : new Connection[]{gameplayConn, extraConn, textConn, configConn, nohdTextConn}) {
            tuneForBulkWrite(c);
        }
        Statement gameplayStatement = gameplayConn.createStatement();
        Statement extraStatement = extraConn.createStatement();
        Statement textStatement = textConn.createStatement();
        Statement configStatement = configConn.createStatement();
        Statement nohdTextStatement = nohdTextConn.createStatement();

        // process all requests
        int n = 0;
        int N = feaRequests.size() + igaRequests.size();

        // process all <frontEndActions> requests
        System.out.println("processing frontEndActions");
        long feaStart = System.nanoTime();
        for (Request request : feaRequests) {
            if (LIMIT >= 0 && n >= LIMIT) break;
            // progress bar
            if (VERBOSE) System.out.println("Processing fea request: " + request);
            System.out.println(n + "/" + N + ": order=" + request.order + " " + "files=" + request.files.size());
            n++;
            // ? why comment these loadAsData lines?
            // travel all files of one request
            for (File file : request.files) {
                loading = file;
                try {
                    if (VERBOSE) System.out.println("Processing file: " + file.getAbsolutePath());
                    // skip heroes
                    if(file.getAbsolutePath().contains("Hero")){
                        if (VERBOSE) System.out.println("skipped file bacause of heroes:" + file.getAbsolutePath());
                        continue;
                    }
                    switch (request.type) {
                        case "UpdateColors":
                        case "UpdateIcons": {
                            // DataBaseLoader.loadAsData(file, extraStatement);
                            break;
                        }
                        case "UpdateText": {
                            // System.out.println("Loading text file: " + file.getAbsolutePath());
                            // DataBaseLoader.loadAsData(file, textStatement);
                            break;
                        }
                        case "UpdateDatabase": {
                            // DataBaseLoader.loadAsData(file, gameplayStatement);  // 不对 应该是config
                            // DataBaseLoader.loadAsData(file, configStatement);
                            break;
                        }
                    }
                    logProcessedFile(file);
                } catch (Exception e) {
                    // System.out.println(e.getClass().getName() + " " + e.getMessage() + " " + file.getAbsolutePath());
                    String errorMessage = "Error processing request: " + request + "\n" +
                                  "File: " + file.getAbsolutePath() + "\n" +
                                  "Exception: " + e.getClass().getName() + ": " + e.getMessage();
                    System.out.println(errorMessage);
                    logError("Error processing file: " + file.getAbsolutePath() + "\n" + e.getMessage());
                    // e.printStackTrace();
                    // logError(errorMessage);
                }
            }
            commitQuietly(extraConn);
            commitQuietly(textConn);
            commitQuietly(nohdTextConn);
        }
        logTime("loadDLCs.fea", feaStart);
        // process all <inGameActions> requests
        System.out.println("processing inGameActions");
        long igaStart = System.nanoTime();
        for (Request request : igaRequests) {
            if (LIMIT >= 0 && n >= LIMIT) break;
            if (VERBOSE) System.out.println("Processing iga request: " + request);
            System.out.println(n + "/" + N + ": order=" + request.order + " " + "files=" + request.files.size());
            n++;
            for (File file : request.files) {
                loading = file;
                try {
                    if (VERBOSE) System.out.println("Processing file: " + file.getAbsolutePath());
                    // skip heroes
                    if(file.getAbsolutePath().contains("hero")){
                        if (VERBOSE) System.out.println("skipped file bacause of heroes:" + file.getAbsolutePath());
                        continue;
                    }

                    switch (request.type) {
                        case "UpdateColors":
                        case "UpdateIcons": {
                            DataBaseLoader.loadAsData(file, extraStatement);
                            break;
                        }
                        case "UpdateText": {
                            DataBaseLoader.loadAsData(file, textStatement);
                            if (!(request.mod.equals("521b8777-0977-4859-a5ee-3e411a732e5c") // hd main
                            || request.mod.equals("66add898-b3bb-4bd9-98a2-805d37f0da2e") // hd district
                            || request.mod.equals("c0417322-9747-42d5-9717-b0df5a4c6e5d") // hd city-states
                            || request.mod.equals("c086b5a6-90d2-4dea-a32f-c642639b9469") // hd I&C
                            )) {
                                DataBaseLoader.loadAsData(file, nohdTextStatement);
                            }
                            break;
                        }
                        case "UpdateDatabase": {
                            // DataBaseLoader.loadAsData(file, gameplayStatement);
                            break;
                        }
                    }
                } catch (Exception e) {
                    // System.out.println(e.getClass().getName() + " " + e.getMessage() + " " + file.getAbsolutePath());
                    System.out.println("Error processing file for inGameActions: " + file.getAbsolutePath());
                    System.out.println("Error in request: " + request);
                    logError("Error processing file: " + file.getAbsolutePath() + "\n" + e.getMessage());
                    // e.printStackTrace();
                    // errorFiles.add(file);
                }
            }
            commitQuietly(extraConn);
            commitQuietly(textConn);
            commitQuietly(nohdTextConn);
        }
        logTime("loadDLCs.iga", igaStart);
        System.out.println("Database init completed.");

        // // Again, process all <frontEndActions> requests, only HD
        // System.out.println("[KEYINFO] Processing frontEndActions only for HD");
        // for (Request request : feaRequests) {
        //     if (!(request.mod.equals("521b8777-0977-4859-a5ee-3e411a732e5c") // hd main
        //     || request.mod.equals("66add898-b3bb-4bd9-98a2-805d37f0da2e") // hd district
        //     || request.mod.equals("c0417322-9747-42d5-9717-b0df5a4c6e5d") // hd city-states
        //     || request.mod.equals("c086b5a6-90d2-4dea-a32f-c642639b9469") // hd I&C
        //     || request.mod.equals("7d155dc3-4a94-4923-a306-d8fd40fb0003") // hd Australia
        //     )) continue;

        //     // progress bar
        //     System.out.println("Processing fea request: " + request);
        //     System.out.println(n + "/" + N + ": order=" + request.order + " " + "files=" + request.files.size());
        //     n++;
        //     // ? why comment these loadAsData lines?
        //     // travel all files of one request
        //     for (File file : request.files) {
        //         loading = file;
        //         try {
        //             System.out.println("Processing file: " + file.getAbsolutePath());
        //             // skip heroes
        //             if(file.getAbsolutePath().contains("Hero")){
        //                 System.out.println("skipped file bacause of heroes:" + file.getAbsolutePath());
        //                 continue;
        //             }
        //             switch (request.type) {
        //                 case "UpdateColors":
        //                 case "UpdateIcons": {
        //                     // DataBaseLoader.loadAsData(file, extraStatement);
        //                     break;
        //                 }
        //                 case "UpdateText": {
        //                     DataBaseLoader.loadAsData(file, textStatement);
        //                     System.out.println("Loading text file: " + file.getAbsolutePath());
        //                     // DataBaseLoader.loadAsData(file, textStatement);
        //                     break;
        //                 }
        //                 case "UpdateDatabase": {
        //                     // DataBaseLoader.loadAsData(file, gameplayStatement);  // 不对 应该是config
        //                     // DataBaseLoader.loadAsData(file, configStatement);
        //                     break;
        //                 }
        //             }
        //             logProcessedFile(file);
        //         } catch (Exception e) {
        //             // System.out.println(e.getClass().getName() + " " + e.getMessage() + " " + file.getAbsolutePath());
        //             String errorMessage = "Error processing request: " + request + "\n" +
        //                           "File: " + file.getAbsolutePath() + "\n" +
        //                           "Exception: " + e.getClass().getName() + ": " + e.getMessage();
        //             System.out.println(errorMessage);
        //             logError("Error processing file: " + file.getAbsolutePath() + "\n" + e.getMessage());
        //             // e.printStackTrace();
        //             // logError(errorMessage);
        //         }
        //     }
        // }
        // System.out.println("[KEYINFO] Processing inGameActions only for HD");
        // for (Request request : igaRequests) {
        //     if (!(request.mod.equals("521b8777-0977-4859-a5ee-3e411a732e5c") // hd main
        //     || request.mod.equals("66add898-b3bb-4bd9-98a2-805d37f0da2e") // hd district
        //     || request.mod.equals("c0417322-9747-42d5-9717-b0df5a4c6e5d") // hd city-states
        //     || request.mod.equals("c086b5a6-90d2-4dea-a32f-c642639b9469") // hd I&C
        //     || request.mod.equals("7d155dc3-4a94-4923-a306-d8fd40fb0003") // hd Australia
        //     )) continue;

        //     System.out.println("Processing iga request: " + request);
        //     System.out.println(n + "/" + N + ": order=" + request.order + " " + "files=" + request.files.size());
        //     n++;
        //     for (File file : request.files) {
        //         loading = file;
        //         try {
        //             System.out.println("Processing file: " + file.getAbsolutePath());
        //             // skip heroes
        //             if(file.getAbsolutePath().contains("hero")){
        //                 System.out.println("skipped file bacause of heroes:" + file.getAbsolutePath());
        //                 continue;
        //             }

        //             switch (request.type) {
        //                 case "UpdateColors":
        //                 case "UpdateIcons": {
        //                     DataBaseLoader.loadAsData(file, extraStatement);
        //                     break;
        //                 }
        //                 case "UpdateText": {
        //                     DataBaseLoader.loadAsData(file, textStatement);
        //                     if (!(request.mod.equals("521b8777-0977-4859-a5ee-3e411a732e5c") // hd main
        //                     || request.mod.equals("66add898-b3bb-4bd9-98a2-805d37f0da2e") // hd district
        //                     || request.mod.equals("c0417322-9747-42d5-9717-b0df5a4c6e5d") // hd city-states
        //                     || request.mod.equals("c086b5a6-90d2-4dea-a32f-c642639b9469") // hd I&C
        //                     )) {
        //                         DataBaseLoader.loadAsData(file, nohdTextStatement);
        //                     }
        //                     break;
        //                 }
        //                 case "UpdateDatabase": {
        //                     // DataBaseLoader.loadAsData(file, gameplayStatement);
        //                     break;
        //                 }
        //             }
        //         } catch (Exception e) {
        //             // System.out.println(e.getClass().getName() + " " + e.getMessage() + " " + file.getAbsolutePath());
        //             System.out.println("Error processing file for inGameActions: " + file.getAbsolutePath());
        //             System.out.println("Error in request: " + request);
        //             logError("Error processing file: " + file.getAbsolutePath() + "\n" + e.getMessage());
        //             // e.printStackTrace();
        //             // errorFiles.add(file);
        //         }
        //     }
        // }

        finishBulkWrite(gameplayConn);
        finishBulkWrite(extraConn);
        finishBulkWrite(textConn);
        finishBulkWrite(configConn);
        finishBulkWrite(nohdTextConn);
    }

    public static void initFix() {
        Connection extraConn = null;
        Connection gameplayConn = null;
        try {
            extraConn = DriverManager.getConnection(Tools.EXTRA_DATABASE);
            tuneForBulkWrite(extraConn);
            DataBaseLoader.loadSQL(new File("scripts/fix/fix_extra.sql"), extraConn.createStatement());

            gameplayConn = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE);
            tuneForBulkWrite(gameplayConn);
            DataBaseLoader.loadSQL(new File("scripts/fix/fix_gameplay.sql"), gameplayConn.createStatement());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            finishBulkWrite(extraConn);
            finishBulkWrite(gameplayConn);
        }
    }

    public static void writeErrors() throws Exception {
        File error = new File("icons/errors.txt");
        if(error.exists()) {
            error.delete();
        }
        error.createNewFile();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(error)));
        for (File f : errorFiles) {
            writer.write(f.getAbsolutePath() + "\n");
        }
        writer.flush();
        writer.close();
    }

    // manual fix before running this :
    
    // replace <?xml version="2.0" encoding="utf-8"?>
    // with <?xml version="1.0" encoding="utf-8"?> in
    // C:\Program Files (x86)\Steam\steamapps\common\Sid Meier's Civilization VI\DLC\Ethiopia\Data\Ethiopia_Icons_Buildings.xml

    // delete comments at the beginning of
    // C:\Program Files (x86)\Steam\steamapps\workshop\content\289070\1579019534\Icons\FontIconsRMI.xml
    // and
    // C:\Program Files (x86)\Steam\steamapps\workshop\content\289070\1312585482\Icons\FontIconsRMI.xml

    // add </Replace> at near end of
    // C:\Program Files (x86)\Steam\steamapps\workshop\content\289070\1770688835\Text\Globe_Theatre_Text.xml

    // fix & with &amp; in
    // C:\Program Files (x86)\Steam\steamapps\workshop\content\289070\1770703400\Text\Burj_Khalifa_Text.xml

    // replace insert into with insert or ignore into in
    // C:\Program Files (x86)\Steam\steamapps\workshop\content\289070\2542898147\Core\Suk_Oceans_Icons.sql

    // delete TEMPORARY in
    // C:\Program Files (x86)\Steam\steamapps\workshop\content\289070\2542898147\Core\Monopolies\Suk_AltEcon_Common.sql
    // and
    // C:\Program Files (x86)\Steam\steamapps\workshop\content\289070\2155632734\Core_MODE\LatResources_Monopolies_Text.sql
    // and
    // C:\Program Files (x86)\Steam\steamapps\workshop\content\289070\2616754773\HD_Monopoly_Text.sql
    
    // replace "Index" with "Idx": (会导致游戏内没有icon 可能不对 应该改Idx的而不是Index的？)
    // E:\SteamLibrary\steamapps\workshop\content\289070\1369684991\Core\Res2_icons.sql
    // and
    // E:\SteamLibrary\steamapps\workshop\content\289070\1369684991\Core\Res2_Honey_Maize_NonNFP_Icons.sql
    // and
    // E:\SteamLibrary\steamapps\workshop\content\289070\1369684991\Core\Game_Mode\Res2_Mode_Icons.sql
    // and
    // C:\Users\1132\Documents\My Games\Sid Meier's Civilization VI\Mods\civ6-harmony-in-diversity\ModSupport\Resourceful2\HD_Resourceful2_Icons.sql
    // and (2 places in this file)

    // part of main function
    public static void init() {
        // Clear logs
        new File("error_log.txt").delete();
        new File("processed_files_log.txt").delete();

        try {
            long t;
            t = System.nanoTime(); copyDatabases(); logTime("copyDatabases", t);
            t = System.nanoTime(); addTables();     logTime("addTables", t);
            t = System.nanoTime(); loadDLCs();       logTime("loadDLCs", t);
            t = System.nanoTime(); initFix();        logTime("initFix", t);
            t = System.nanoTime(); writeErrors();    logTime("writeErrors", t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // load icons
    public static void initIcons () throws Exception {
        Statement extra = DriverManager.getConnection(Tools.EXTRA_DATABASE).createStatement();
        File iconFile = new File ("icons/icons.txt");  // 找icon的总列表 需要事先手写
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(iconFile)));
        List<String> icons = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            icons.add(line);
        }
        reader.close();
        for (String civilopedia : Page.HEADERS) {
            icons.add("icon_civilopedia_" + civilopedia);  // 不区分大小写 页标题
        }
        for (String icon : icons) {
            BufferedImage image = null;
            File file = new File("icons/" + icon + ".png");
            ResultSet r1 = extra.executeQuery("select * from IconDefinitions where lower(Name) = \"" + icon + "\";");
            if (r1.next()) {
                icon = r1.getString("Name");
                image = Tools.getImage(icon);
                if (image == null) {
                    System.out.println("Found icon: " + icon + ", but can't load image");
                }
            }
            if (image == null && file.exists()) {
                System.out.println("manually replaced " + icon);
                image = ImageIO.read(file); // 手动找icon 放在icons/下面 png格式
            }
            if (image == null) {
                System.out.println("can't find " + icon);
            }
            String path;
            path = (icon.toUpperCase().startsWith("ICON_") ? "" : "ICON_") + icon.toUpperCase() + ".png";
            ImageEditor.saveImage(image, path);
        }
    }

    public static void logError(String message) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("error_log.txt", true)))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    
    public static void logProcessedFile(File file) {
        if (!VERBOSE) return; // per-file logging is the fea-loop hotspot; opt in via -Dhd.verbose
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("processed_files_log.txt", true)))) {
            writer.write(file.getAbsolutePath());
            writer.newLine();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // debug
    public static void testFindGreatWorkProduct() throws Exception {
        Statement extra = DriverManager.getConnection(Tools.EXTRA_DATABASE).createStatement();

        String icon = "GreatWork_Product"; 
        // String icon = "GreatWork_Religious";
    
        ResultSet r1 = extra.executeQuery("select * from IconDefinitions where lower(Name) = \"" + icon.toLowerCase() + "\";");
        
        if (r1.next()) {
            icon = r1.getString("Name");
            System.out.println("Found icon in DB: " + icon);
    
            BufferedImage image = Tools.getImage(icon);
            if (image != null) {
                System.out.println("Image loaded successfully.");
            } else {
                System.out.println("Image is null, can't load it.");
            }
        } else {
            System.out.println("No matching icon found in DB for: " + icon);
        }
        
        r1.close();
        extra.close();
    }
    

    // total main function
    public static void main(String[] args) throws Exception {
        init();
        initIcons();

        // debug
        // testFindGreatWorkProduct();
    }

}
