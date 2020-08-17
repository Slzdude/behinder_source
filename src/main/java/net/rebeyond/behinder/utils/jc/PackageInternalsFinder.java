package net.rebeyond.behinder.utils.jc;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

public class PackageInternalsFinder {
    private static final String CLASS_FILE_EXTENSION = ".class";
    private final ClassLoader classLoader;

    public PackageInternalsFinder(ClassLoader classLoader2) {
        this.classLoader = classLoader2;
    }

    public List<JavaFileObject> find(String packageName) throws IOException {
        String javaPackageName = packageName.replaceAll("\\.", "/");
        List<JavaFileObject> result = new ArrayList<>();
        Enumeration<URL> urlEnumeration = this.classLoader.getResources(javaPackageName);
        while (urlEnumeration.hasMoreElements()) {
            URL packageFolderURL = urlEnumeration.nextElement();
            if (packageFolderURL.toString().startsWith("jar")) {
                result.addAll(listUnder(packageName, packageFolderURL));
            }
        }
        return result;
    }

    private Collection<JavaFileObject> listUnder(String packageName, URL packageFolderURL) {
        File directory = new File(packageFolderURL.getFile());
        if (directory.isDirectory()) {
            return processDir(packageName, directory);
        }
        return processJar(packageFolderURL);
    }

    private List<JavaFileObject> processJar(URL packageFolderURL) {
        List<JavaFileObject> result = new ArrayList<>();
        try {
            String jarUri = packageFolderURL.toExternalForm().split("!")[0];
            JarURLConnection jarConn = (JarURLConnection) packageFolderURL.openConnection();
            String rootEntryName = jarConn.getEntryName();
            int rootEnd = rootEntryName.length() + 1;
            Enumeration<JarEntry> entryEnum = jarConn.getJarFile().entries();
            while (entryEnum.hasMoreElements()) {
                String name = entryEnum.nextElement().getName();
                if (name.startsWith(rootEntryName) && name.indexOf(47, rootEnd) == -1 && name.endsWith(CLASS_FILE_EXTENSION)) {
                    result.add(new CustomJavaFileObject(name.replaceAll("/", ".").replaceAll(".class$", ""), URI.create(jarUri + "!/" + name)));
                }
            }
            jarConn.setDefaultUseCaches(false);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Wasn't able to open " + packageFolderURL + " as a jar file", e);
        }
    }

    private List<JavaFileObject> processRsrc(URL packageFolderURL) {
        List<JavaFileObject> result = new ArrayList<>();
        try {
            String jarUri = packageFolderURL.toExternalForm().split("!")[0];
            JarURLConnection jarConn = (JarURLConnection) packageFolderURL.openConnection();
            String rootEntryName = jarConn.getEntryName();
            int rootEnd = rootEntryName.length() + 1;
            Enumeration<JarEntry> entryEnum = jarConn.getJarFile().entries();
            while (entryEnum.hasMoreElements()) {
                String name = entryEnum.nextElement().getName();
                if (name.startsWith(rootEntryName) && name.indexOf(47, rootEnd) == -1 && name.endsWith(CLASS_FILE_EXTENSION)) {
                    result.add(new CustomJavaFileObject(name.replaceAll("/", ".").replaceAll(".class$", ""), URI.create(jarUri + "!/" + name)));
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Wasn't able to open " + packageFolderURL + " as a jar file", e);
        }
    }

    private List<JavaFileObject> processDir(String packageName, File directory) {
        List<JavaFileObject> result = new ArrayList<>();
        File[] childFiles = directory.listFiles();
        for (File childFile : childFiles) {
            if (childFile.isFile() && childFile.getName().endsWith(CLASS_FILE_EXTENSION)) {
                result.add(new CustomJavaFileObject((packageName + "." + childFile.getName()).replaceAll(".class$", ""), childFile.toURI()));
            }
        }
        return result;
    }
}
