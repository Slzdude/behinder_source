package net.rebeyond.behinder.utils.jc;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CustomClassloaderJavaFileManager implements JavaFileManager {
    public static Map<String, JavaFileObject> fileObjects = new ConcurrentHashMap();
    private final ClassLoader classLoader;
    private final PackageInternalsFinder finder;
    private final StandardJavaFileManager standardFileManager;

    public CustomClassloaderJavaFileManager(ClassLoader classLoader2, StandardJavaFileManager standardFileManager2) {
        this.classLoader = classLoader2;
        this.standardFileManager = standardFileManager2;
        this.finder = new PackageInternalsFinder(classLoader2);
    }

    public ClassLoader getClassLoader(Location location) {
        return this.standardFileManager.getClassLoader(location);
    }

    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof CustomJavaFileObject) {
            return ((CustomJavaFileObject) file).binaryName();
        }
        return this.standardFileManager.inferBinaryName(location, file);
    }

    public boolean isSameFile(FileObject a, FileObject b) {
        return this.standardFileManager.isSameFile(a, b);
    }

    public boolean handleOption(String current, Iterator<String> remaining) {
        return this.standardFileManager.handleOption(current, remaining);
    }

    public boolean hasLocation(Location location) {
        return location == StandardLocation.CLASS_PATH || location == StandardLocation.PLATFORM_CLASS_PATH;
    }

    public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
        JavaFileObject javaFileObject = fileObjects.get(className);
        if (javaFileObject == null) {
            this.standardFileManager.getJavaFileForInput(location, className, kind);
        }
        return javaFileObject;
    }

    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
        JavaFileObject javaFileObject = new MyJavaFileObject(className, kind);
        fileObjects.put(className, javaFileObject);
        return javaFileObject;
    }

    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        return this.standardFileManager.getFileForInput(location, packageName, relativeName);
    }

    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        return this.standardFileManager.getFileForOutput(location, packageName, relativeName, sibling);
    }

    public void flush() throws IOException {
        this.standardFileManager.flush();
    }

    public void close() throws IOException {
        this.standardFileManager.close();
    }

    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
        if (location == StandardLocation.PLATFORM_CLASS_PATH) {
            return this.standardFileManager.list(location, packageName, kinds, recurse);
        }
        if (location != StandardLocation.CLASS_PATH || !kinds.contains(Kind.CLASS)) {
            return Collections.emptyList();
        }
        if (packageName.startsWith("java.")) {
            return this.standardFileManager.list(location, packageName, kinds, recurse);
        }
        return this.finder.find(packageName);
    }

    public int isSupportedOption(String option) {
        return -1;
    }
}
