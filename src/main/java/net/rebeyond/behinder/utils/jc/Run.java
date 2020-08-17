package net.rebeyond.behinder.utils.jc;

import javax.tools.*;
import java.io.File;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Run {
    public static void main(String[] args) {
        new Run().test();
    }

    public void test() {
        while (true) {
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public static byte[] getClassFromSourceCode(String sourceCode) throws Exception {
        Matcher matcher = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s*").matcher(sourceCode);
        if (matcher.find()) {
            String cls = matcher.group(1);
            JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
            if (jc == null) {
                throw new Exception("本地机器上没有找到编译环境，请确认:1.是否安装了JDK环境;2." + System.getProperty("java.home") + File.separator + "lib目录下是否有tools.jar.");
            }
            JavaFileManager fileManager = new CustomClassloaderJavaFileManager(Run.class.getClassLoader(), jc.getStandardFileManager(null, null, null));
            JavaFileObject javaFileObject = new MyJavaFileObject(cls, sourceCode);
            List<String> options = new ArrayList<>();
            options.add("-source");
            options.add("1.6");
            options.add("-target");
            options.add("1.6");
            DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
            if (!jc.getTask(null, fileManager, collector, options, null, Arrays.asList(javaFileObject)).call().booleanValue()) {
                Iterator<Diagnostic<? extends JavaFileObject>> it = collector.getDiagnostics().iterator();
                if (it.hasNext()) {
                    throw new Exception(it.next().getMessage(null));
                }
            }
            JavaFileObject fileObject = CustomClassloaderJavaFileManager.fileObjects.get(cls);
            if (fileObject != null) {
                return ((MyJavaFileObject) fileObject).getCompiledBytes();
            }
            return null;
        }
        throw new IllegalArgumentException("No such class name in " + sourceCode);
    }
}
