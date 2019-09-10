import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

public class App {
    /* access modifiers changed from: private */
    public static Map<String, JavaFileObject> fileObjects = new ConcurrentHashMap();

    public static class MyJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        protected MyJavaFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
            JavaFileObject javaFileObject = (JavaFileObject) App.fileObjects.get(className);
            if (javaFileObject == null) {
                super.getJavaFileForInput(location, className, kind);
            }
            return javaFileObject;
        }

        public JavaFileObject getJavaFileForOutput(Location location, String qualifiedClassName, Kind kind, FileObject sibling) throws IOException {
            JavaFileObject javaFileObject = new MyJavaFileObject(qualifiedClassName, kind);
            App.fileObjects.put(qualifiedClassName, javaFileObject);
            return javaFileObject;
        }
    }

    public static class MyJavaFileObject extends SimpleJavaFileObject {
        private ByteArrayOutputStream outPutStream;
        private String source;

        public MyJavaFileObject(String name, String source2) {
            super(URI.create("String:///" + name + Kind.SOURCE.extension), Kind.SOURCE);
            this.source = source2;
        }

        public MyJavaFileObject(String name, Kind kind) {
            super(URI.create("String:///" + name + kind.extension), kind);
            this.source = null;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            if (this.source != null) {
                return this.source;
            }
            throw new IllegalArgumentException("source == null");
        }

        public OutputStream openOutputStream() throws IOException {
            this.outPutStream = new ByteArrayOutputStream();
            return this.outPutStream;
        }

        public byte[] getCompiledBytes() {
            return this.outPutStream.toByteArray();
        }
    }

    public static void main(String[] args) throws IOException {
        String code = "public class Man {\n\tpublic void hello(){\n\t\tSystem.out.println(\"hello world\");\n\t}\n}";
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        JavaFileManager javaFileManager = new MyJavaFileManager(compiler.getStandardFileManager(collector, null, null));
        List<String> options = new ArrayList<>();
        options.add("-target");
        options.add("1.8");
        Matcher matcher = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s*").matcher(code);
        if (matcher.find()) {
            String cls = matcher.group(1);
            Boolean call = compiler.getTask(null, javaFileManager, collector, options, null, Arrays.asList(new JavaFileObject[]{new MyJavaFileObject(cls, code)})).call();
            JavaFileObject fileObject = (JavaFileObject) fileObjects.get(cls);
            if (fileObject != null) {
                System.out.println(((MyJavaFileObject) fileObject).getCompiledBytes().length);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("No such class name in " + code);
    }
}
