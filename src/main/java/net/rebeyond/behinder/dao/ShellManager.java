package net.rebeyond.behinder.dao;

import net.rebeyond.behinder.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sqlite.JDBC;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.*;

public class ShellManager {
    private static final String Class_Name = "org.sqlite.JDBC";
    private static String DB_PATH = "data.db";
    private static String DB_URL = (JDBC.PREFIX + DB_PATH);
    private static Connection connection = null;

    public ShellManager() throws Exception {
        DB_PATH = URLDecoder.decode(Utils.getSelfPath(), "UTF-8") + File.separator + DB_PATH;
        DB_URL = JDBC.PREFIX + DB_PATH;
        if (!new File(DB_PATH).exists()) {
            throw new Exception("数据库文件丢失，无法启动。" + DB_PATH);
        }
        Class.forName(Class_Name);
        connection = DriverManager.getConnection(DB_URL);
        connection.setAutoCommit(true);
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONArray listShell() throws Exception {
        JSONArray result = new JSONArray();
        ResultSet rs = connection.createStatement().executeQuery("select * from shells");
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put(obj);
        }
        return result;
    }

    public JSONArray findShellByCatagory(String catagoryName) throws Exception {
        JSONArray result = new JSONArray();
        PreparedStatement statement = connection.prepareStatement("select * from shells where catagory=?");
        statement.setString(1, catagoryName);
        ResultSet rs = statement.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put(obj);
        }
        return result;
    }

    public JSONArray listCatagory() throws Exception {
        JSONArray result = new JSONArray();
        ResultSet rs = connection.createStatement().executeQuery("select * from catagory");
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put(obj);
        }
        return result;
    }

    public JSONObject findShell(int shellID) throws Exception {
        JSONArray result = new JSONArray();
        PreparedStatement statement = connection.prepareStatement("select * from shells where id=?");
        statement.setInt(1, shellID);
        ResultSet rs = statement.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put(obj);
        }
        if (result.length() == 0) {
            return null;
        }
        return result.getJSONObject(0);
    }

    public JSONObject findHostByIP(int shellID, String ip) throws Exception {
        JSONArray result = new JSONArray();
        PreparedStatement statement = connection.prepareStatement("select * from hosts where shellid=? and ip=?");
        statement.setInt(1, shellID);
        statement.setString(2, ip);
        ResultSet rs = statement.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put(obj);
        }
        if (result.length() == 0) {
            return null;
        }
        return result.getJSONObject(0);
    }

    public int addShell(String url, String password, String type, String catagory, String comment, String headers) throws Exception {
        PreparedStatement statement = connection.prepareStatement("select count(*) from shells where url=?");
        statement.setString(1, url);
        if (statement.executeQuery().getInt(1) > 0) {
            throw new Exception("该URL已存在");
        }
        PreparedStatement statement2 = connection.prepareStatement("insert into shells(url,ip,password,type,catagory,os,comment,headers,addtime,updatetime,accesstime) values (?,?,?,?,?,?,?,?,?,?,?)");
        statement2.setString(1, url);
        statement2.setString(2, InetAddress.getByName(new URL(url).getHost()).getHostAddress());
        statement2.setString(3, password);
        statement2.setString(4, type);
        statement2.setString(5, catagory);
        statement2.setString(6, "");
        statement2.setString(7, comment);
        statement2.setString(8, headers);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        statement2.setTimestamp(9, now);
        statement2.setTimestamp(10, now);
        statement2.setTimestamp(11, now);
        return statement2.executeUpdate();
    }

    public int addCatagory(String name, String comment) throws Exception {
        PreparedStatement statement = connection.prepareStatement("select count(*) from catagory where name=?");
        statement.setString(1, name);
        if (statement.executeQuery().getInt(1) > 0) {
            throw new Exception("该分类已存在");
        }
        PreparedStatement statement2 = connection.prepareStatement("insert into catagory(name,comment) values (?,?)");
        statement2.setString(1, name);
        return statement2.executeUpdate();
    }

    public int addHost(int shellID, String ip, String os, String comment) throws Exception {
        PreparedStatement statement = connection.prepareStatement("select count(*) from hosts where shellid=? and  ip=?");
        statement.setInt(1, shellID);
        statement.setString(2, ip);
        if (statement.executeQuery().getInt(1) > 0) {
            throw new Exception("该资产已存在");
        }
        PreparedStatement statement2 = connection.prepareStatement("insert into hosts(shellID,ip,os,comment) values (?,?,?,?)");
        statement2.setInt(1, shellID);
        statement2.setString(2, ip);
        statement2.setString(3, os);
        statement2.setString(4, comment);
        return statement2.executeUpdate();
    }

    public int addService(int hostID, String port, String name, String banner, String comment) throws Exception {
        PreparedStatement statement = connection.prepareStatement("select count(*) from services where hostid=? and  port=?");
        statement.setInt(1, hostID);
        statement.setString(2, port);
        if (statement.executeQuery().getInt(1) > 0) {
            throw new Exception("该端口已存在");
        }
        PreparedStatement statement2 = connection.prepareStatement("insert into services(hostid,name,port,banner,comment) values (?,?,?,?)");
        statement2.setInt(1, hostID);
        statement2.setString(2, name);
        statement2.setString(3, port);
        statement2.setString(4, banner);
        statement2.setString(5, comment);
        return statement2.executeUpdate();
    }

    public int addPlugin(String name, String version, String entryFile, String scriptType, String type, int isGetShell, String icon, String author, String link, String qrcode, String comment) throws Exception {
        PreparedStatement statement = connection.prepareStatement("select count(*) from plugins where name=? and scripttype=?");
        statement.setString(1, name);
        statement.setString(2, scriptType);
        if (statement.executeQuery().getInt(1) > 0) {
            throw new Exception("该插件已存在");
        }
        PreparedStatement statement2 = connection.prepareStatement("insert into plugins(name,version,entryFile,scriptType,type,isGetShell,icon,author,link,qrcode,comment) values (?,?,?,?,?,?,?,?,?,?,?)");
        statement2.setString(1, name);
        statement2.setString(2, version);
        statement2.setString(3, entryFile);
        statement2.setString(4, scriptType);
        statement2.setString(5, type);
        statement2.setInt(6, isGetShell);
        statement2.setString(7, icon);
        statement2.setString(8, author);
        statement2.setString(9, link);
        statement2.setString(10, qrcode);
        statement2.setString(11, comment);
        return statement2.executeUpdate();
    }

    public int updateShell(int shellID, String url, String password, String type, String catagory, String comment, String headers) throws Exception {
        PreparedStatement statement = connection.prepareStatement("update shells set url=?,ip=?,password=?,type=?,catagory=?,comment=?,headers=?,updatetime=? where id=?");
        statement.setString(1, url);
        statement.setString(2, InetAddress.getByName(new URL(url).getHost()).getHostAddress());
        statement.setString(3, password);
        statement.setString(4, type);
        statement.setString(5, catagory);
        statement.setString(6, comment);
        statement.setString(7, headers);
        statement.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
        statement.setInt(9, shellID);
        return statement.executeUpdate();
    }

    public int deleteShell(int shellId) throws Exception {
        PreparedStatement statement = connection.prepareStatement("delete from shells where id=?");
        statement.setInt(1, shellId);
        return statement.executeUpdate();
    }

    public int deleteCatagory(String cataGoryName) throws Exception {
        PreparedStatement statement = connection.prepareStatement("delete from catagory where name=?");
        statement.setString(1, cataGoryName);
        return statement.executeUpdate();
    }

    public int addPlugin(String name, String type, String code) throws Exception {
        PreparedStatement statement = connection.prepareStatement("insert into plugins(name,type,code) values (?,?,?)");
        statement.setString(0, name);
        statement.setString(1, type);
        statement.setString(2, code);
        return statement.executeUpdate();
    }

    public int addProxy(String name, String type, String ip, int port, String username, String password, int status) throws Exception {
        PreparedStatement statement = connection.prepareStatement("insert into proxys(name,type,ip,port,username,password,status) values (?,?,?,?,?,?,?)");
        statement.setString(1, name);
        statement.setString(2, type);
        statement.setString(3, ip);
        statement.setInt(4, port);
        statement.setString(5, username);
        statement.setString(6, password);
        statement.setInt(7, status);
        return statement.executeUpdate();
    }

    public int updateProxy(String name, String type, String ip, String port, String username, String password, int status) throws Exception {
        PreparedStatement statement = connection.prepareStatement("update proxys set type=?,ip=?,port=?,username=?,password=?,status=? where name=?");
        statement.setString(1, type);
        statement.setString(2, ip);
        statement.setString(3, port);
        statement.setString(4, username);
        statement.setString(5, password);
        statement.setInt(6, status);
        statement.setString(7, name);
        return statement.executeUpdate();
    }

    public JSONObject findProxy(String name) throws Exception {
        JSONArray result = new JSONArray();
        PreparedStatement statement = connection.prepareStatement("select * from  proxys  where name=?");
        statement.setString(1, name);
        ResultSet rs = statement.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put(obj);
        }
        if (result.length() == 0) {
            return null;
        }
        return result.getJSONObject(0);
    }

    public JSONObject findPluginByName(String scriptType, String name) throws Exception {
        JSONArray result = new JSONArray();
        PreparedStatement statement = connection.prepareStatement("select * from  plugins  where scripttype=? and name=?");
        statement.setString(1, scriptType);
        statement.setString(2, name);
        ResultSet rs = statement.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put(obj);
        }
        if (result.length() == 0) {
            return null;
        }
        return result.getJSONObject(0);
    }

    public int updatePlugin(int pluginID, String name, String type, String code) throws Exception {
        PreparedStatement statement = connection.prepareStatement("update plugins set name=?,type=?,code=? where id=?");
        statement.setString(0, name);
        statement.setString(1, type);
        statement.setString(2, code);
        statement.setInt(3, pluginID);
        return statement.executeUpdate();
    }

    public int delPlugin(int pluginID) throws Exception {
        PreparedStatement statement = connection.prepareStatement("delete from plugins where id=?");
        statement.setInt(1, pluginID);
        return statement.executeUpdate();
    }

    public int delHost(int hostID) throws Exception {
        PreparedStatement statement = connection.prepareStatement("delete from hosts where id=?");
        statement.setInt(1, hostID);
        return statement.executeUpdate();
    }

    public int delService(int serviceID) throws Exception {
        PreparedStatement statement = connection.prepareStatement("delete from service where id=?");
        statement.setInt(1, serviceID);
        return statement.executeUpdate();
    }

    public JSONArray listPlugin(String scriptType) throws Exception {
        JSONArray result = new JSONArray();
        PreparedStatement statement = connection.prepareStatement("select * from plugins where scripttype=? or scripttype='all'");
        statement.setString(1, scriptType);
        ResultSet rs = statement.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put(obj);
        }
        return result;
    }

    public JSONArray listPlugin() throws Exception {
        JSONArray result = new JSONArray();
        ResultSet rs = connection.createStatement().executeQuery("select * from plugins");
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put(obj);
        }
        return result;
    }

    public JSONArray listHost(int shellID) throws Exception {
        JSONArray result = new JSONArray();
        PreparedStatement statement = connection.prepareStatement("select * from hosts where shellid=?");
        statement.setInt(1, shellID);
        ResultSet rs = statement.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put(obj);
        }
        return result;
    }

    public JSONArray listService(int hostID) throws Exception {
        JSONArray result = new JSONArray();
        PreparedStatement statement = connection.prepareStatement("select * from service where hostid=?");
        statement.setInt(1, hostID);
        ResultSet rs = statement.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put(obj);
        }
        return result;
    }

    public int updateOsInfo(int shellID, String osInfo) throws Exception {
        PreparedStatement statement = connection.prepareStatement("update shells set os=? where id=?");
        statement.setString(1, osInfo);
        statement.setInt(2, shellID);
        return statement.executeUpdate();
    }

    public int updateMemo(int shellID, String memo) throws Exception {
        PreparedStatement statement = connection.prepareStatement("update shells set memo=? where id=?");
        statement.setString(1, memo);
        statement.setInt(2, shellID);
        return statement.executeUpdate();
    }
}
