package load;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tools.Tools;

public class DataBaseLoader {
    // -Dhd.trace=<TAG> prints every SQL/XML write that mentions TAG, in execution order
    // (the last one printed is the value that ends up in the DB). For debugging overrides.
    public static final String TRACE = System.getProperty("hd.trace", System.getenv("HD_TRACE"));

    private static void trace(String where, String src, String stmt) {
        if (TRACE != null && stmt != null && stmt.contains(TRACE)) {
            String s = stmt.replaceAll("\\s+", " ").trim();
            if (s.length() > 160) s = s.substring(0, 160) + "...";
            System.out.println("[TRACE] " + where + " <= " + src + " :: " + s);
        }
    }

    private static final Set<String> FILTER_KEYWORDS = new HashSet<>(Arrays.asList(
        "HEROES",
        "Heroes",
        "SecretSocieties",
        "SECRETSOCIETIES",
        "TESTING_OPTION",
        "TestingOption"
    ));


    public static Connection civi6;

    public static void createDatabases() throws SQLException {
        File file = new File("civi6.db");
        if (file.exists())
            file.delete();
        civi6 = DriverManager.getConnection("jdbc:sqlite:civi6.db");
    }

    public static int numberOf (String s, char c) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                n ++;
            }
        }
        return n;
    }

    // loadSQL 按 ";\n" 切句，而注释里的分号会把一条完整语句劈成两半：/* ... */ 注释掉的语句会照常
    // 执行、残留的 "*/" 报 near "*"；-- 行注释里的 ";" 则让多行 values 断在中间（incomplete input
    // + near "("）。所以切句前先去掉两种注释。扫描跳过字符串字面量，避免误删其中的 -- 和 /*；
    // 保留换行，切句位置不受影响。
    static String stripComments(String s) {
        StringBuilder out = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\'' || c == '"') {
                int end = s.indexOf(c, i + 1);
                if (end < 0) {
                    out.append(s, i, s.length());
                    break;
                }
                out.append(s, i, end + 1);
                i = end + 1;
            } else if (c == '-' && i + 1 < s.length() && s.charAt(i + 1) == '-') {
                int end = s.indexOf('\n', i);
                if (end < 0) {
                    break;
                }
                i = end;
            } else if (c == '/' && i + 1 < s.length() && s.charAt(i + 1) == '*') {
                int end = s.indexOf("*/", i + 2);
                if (end < 0) {
                    break;
                }
                for (int j = i; j < end; j++) {
                    if (s.charAt(j) == '\n') {
                        out.append('\n');
                    }
                }
                i = end + 2;
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    public static void loadSQL(File file, Statement statement) throws Exception {
        if (Init.VERBOSE) System.out.println("Loading SQL file: " + file.getAbsolutePath());

        String text = stripComments(Tools.readFromFile(file));

        String[] lines = (text + " ").split("(;\\s*\\n|;\\s*(?=--))");
        //String[] lines = (text + " ").split(";");
        for (int i = 0; i < lines.length - 1; i++) {
            if (lines[i] == null || lines[i].trim().isEmpty()) {
                continue;
            }

            // 合并 CREATE TRIGGER 的多行内容
            if (lines[i].contains("CREATE TRIGGER") && lines[i].contains("BEGIN") && !lines[i].contains("END")) {
                lines[i + 1] = lines[i] + ";" + lines[i + 1];
                continue;
            }

            // //if (numberOf(lines[i], '"') % 2 == 1) {
            // //    lines[i + 1] = lines[i] + ";" + lines[i + 1];
            // //    continue;
            // //}
            // if (lines[i].contains("CREATE TRIGGER") && lines[i].contains("BEGIN") && !lines[i].contains("END")) {
            //     lines[i + 1] = lines[i] + ";" + lines[i + 1];
            //     continue;
            // }
            // if (lines[i].matches("(?s).*\\bMake_Hash\\b.*")) {
            //     continue;
            // }
            // // if (lines[i].contains("Make_Hash")) {
            // //     continue;
            // // }

            // Filters:

            // 将 "INSERT INTO" 替换为 "INSERT OR IGNORE INTO"
            String originalLine = lines[i];
            lines[i] = lines[i].replaceFirst("(?i)\\bINSERT\\s+INTO\\b", "INSERT OR IGNORE INTO");

            // 如果有修改，打印日志
            if (!originalLine.equals(lines[i])) {
                // System.out.println("Modified SQL: ");
                // System.out.println("Original: " + originalLine);
                // System.out.println("Modified: " + lines[i]);
                if (Init.VERBOSE) System.out.println("Modified SQL in file: " + file.getName());
            }

            
            // Skip heroes related SQL
            // final String currentLine = lines[i];
            // String matchedKeyword = null; 
            // for (String keyword : FILTER_KEYWORDS) {
            //     if (currentLine.contains(keyword)) {
            //         matchedKeyword = keyword; 
            //         break;
            //     }
            // }
            // if (matchedKeyword != null) {
            //     // 豁免
            //     if (!(currentLine.contains("SPY_HERO") 
            //     || currentLine.contains("PROMOTION_HEROES")
            //     || currentLine.contains("NKISI_HEROES")
            //     )
            //     ) {  // in case of skipping HD policy and promotion
            //         System.out.println("Skipping filtered SQL due to keyword '" + matchedKeyword + "': " + currentLine);
            //         logSkippedSQL(file, currentLine, matchedKeyword); 
            //         continue;
            //     }
            // }
            
            // "Index" 是 SQLite 关键字，建表时统一改名 Idx（读取侧 Tools.getIcon 也按 Idx 取）。
            // mod 的 IconDefinitions insert 用的是双引号/方括号写法，一并归一，否则整条插入被拒、图标丢失。
            lines[i] = lines[i].replaceAll("'Index'", "'Idx'")
                    .replaceAll("\"Index\"", "\"Idx\"")
                    .replaceAll("\\[Index\\]", "[Idx]");

            trace("SQL", file.getName(), lines[i]);
            try {
                statement.execute(lines[i]);
            } catch (Exception e) {
                if (!e.getMessage().contains("The prepared statement has been finalized")) {
                    System.out.println();
                    e.printStackTrace();
                    System.err.println(e.getClass().getName() + " " + e.getMessage());
                    if (Init.loading != null) {
                        System.err.println(Init.loading.getAbsolutePath());
                        Init.errorFiles.add(Init.loading);
                    }
                }
            }
        }
    }

    public static void loadXMLAsTables(File file, Statement statement) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        Element root = doc.getDocumentElement();
        NodeList list = root.getElementsByTagName("Table");
        for (int i = 0; i < list.getLength(); i++) {
            Element table = (Element) list.item(i);
            String name = table.getAttribute("name");
            NodeList columns = table.getElementsByTagName("Column");
            String s = "";
            for (int j = 0; j < columns.getLength(); j++) {
                Element column = (Element) columns.item(j);
                String term = "\"" + column.getAttribute("name") + "\" " + column.getAttribute("type");
                if ("true".equals(column.getAttribute("notnull"))) {
                    term += " not null";
                }
                if ("unique".equals(column.getAttribute("notnull"))) {
                    term += " unique";
                }
                if ("primarykey".equals(column.getAttribute("notnull"))) {
                    term += " primary key";
                }
                if (column.getAttribute("default") != null) {
                    term += " default \"" + column.getAttribute("default") + "\"";
                }
                s += term + ", ";
            }
            s = s.substring(0, s.length() - 2);
            String command = "create table \"" + name + "\" (" + s + ");";
            command = command.replaceAll("'Index'", "'Idx'");
            try {
                statement.execute(command);
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + " " + e.getMessage());
                if (Init.loading != null) {
                    System.err.println(Init.loading.getAbsolutePath());
                    Init.errorFiles.add(Init.loading);
                }
            }
        }
    }

    public static void excuteRow(Node node, Statement statement, String tableName) throws Exception {
        Element row = (Element) node;
        String keys = "";
        String values = "";

        NamedNodeMap map = row.getAttributes();
        for (int k = 0; k < map.getLength(); k++) {
            String key = map.item(k).getNodeName();
            if (key.equals("Index")) {
                key = "Idx";
            }
            keys += key;
            String context = map.item(k).getTextContent();
            if (context.toLowerCase().equals("true") || context.toLowerCase().equals("false")) {
                values += context;
            } else {
                values += "'" + context + "'";
            }
            keys += ", ";
            values += ", ";
        }

        NodeList list = row.getChildNodes();
        for (int k = 0; k < list.getLength(); k++) {
            if (list.item(k) instanceof Element) {
                String key = list.item(k).getNodeName();
                if (key.equals("Index")) {
                    key = "Idx";
                }
                keys += key;
                String context = list.item(k).getTextContent().replaceAll("'", "''");
                if (context.toLowerCase().equals("true") || context.toLowerCase().equals("false")) {
                    values += context;
                } else {
                    values += "'" + context + "'";
                }
                keys += ", ";
                values += ", ";
            }
        }

        keys = keys.substring(0, keys.length() - 2);
        values = values.substring(0, values.length() - 2);

        String command = "insert or replace into " + tableName + " (" + keys + ") values (" + values + ");";

        trace("XML", String.valueOf(Init.loading), command);
        try {
            statement.execute(command);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + " " + e.getMessage());
            if (Init.loading != null) {
                System.err.println(Init.loading.getAbsolutePath());
                Init.errorFiles.add(Init.loading);
            }
        }

    }

    public static void loadXMLAsData(File file, Statement statement) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        Element root = doc.getDocumentElement();
        NodeList tables = root.getChildNodes();
        for (int i = 0; i < tables.getLength(); i++) {
            Node node = tables.item(i);
            if (node instanceof Element) {
                Element table = (Element) node;
                String tableName = table.getTagName();
                // print which table
                if (Init.VERBOSE) System.out.println("Loading table: " + tableName);

                NodeList rows = table.getElementsByTagName("Row");
                for (int j = 0; j < rows.getLength(); j++) {
                    Node row = rows.item(j);
                    if (shouldFilter(row)) {
                        if (Init.VERBOSE) System.out.println("Skipping filtered row: " + nodeToString(row));
                        continue;
                    }
                    
                    excuteRow(rows.item(j), statement, tableName);
                }
                // for what?
                NodeList replaces = table.getElementsByTagName("Replace");
                for (int j = 0; j < replaces.getLength(); j++) {
                    Node replace = replaces.item(j);
                    if (shouldFilter(replace)) {
                        if (Init.VERBOSE) System.out.println("Skipping filtered replace: " + nodeToString(replace) );
                        continue;
                    }
                    excuteRow(replaces.item(j), statement, tableName);
                }
            }
        }
    }

    public static void loadAsData(File file, Statement statement) throws Exception {
        if (file.getName().endsWith(".xml")) {
            loadXMLAsData(file, statement);
        } else if (file.getName().endsWith(".sql")) {
            loadSQL(file, statement);
        }
    }

    public static void loadAllXMLAsData(File file, Statement statement) throws Exception {
        if (file.isFile() && file.getName().endsWith(".xml")) {
            loadXMLAsData(file, statement);
        } else if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                loadAllXMLAsData(child, statement);
            }
        }
    }

    // 将 Node 转换为字符串，便于日志输出
    private static String nodeToString(Node node) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            return node.getNodeName();
        }
    }

    private static boolean shouldFilter(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null) {
            return false;
        }
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            String value = attr.getNodeValue();
            for (String keyword : FILTER_KEYWORDS) {
                if (value != null && value.contains(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void logSkippedSQL(File file, String sql, String keyword) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("skipped_sql.log", true))) {
            writer.write("File: " + file.getAbsolutePath());
            writer.newLine();
            writer.write("Skipped SQL: " + sql);
            writer.newLine();
            writer.write("Matched Keyword: " + keyword);
            writer.newLine();
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
}
