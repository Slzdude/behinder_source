package org.eclipse.jdt.internal.jarinjarloader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class JarRsrcLoader {

    private static class ManifestInfo {
        String[] rsrcClassPath;
        String rsrcMainClass;

        private ManifestInfo() {
        }

        ManifestInfo(ManifestInfo manifestInfo) {
            this();
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException, IOException {
        ManifestInfo mi = getManifestInfo();
        URL.setURLStreamHandlerFactory(new RsrcURLStreamHandlerFactory(Thread.currentThread().getContextClassLoader()));
        URL[] rsrcUrls = new URL[mi.rsrcClassPath.length];
        for (int i = 0; i < mi.rsrcClassPath.length; i++) {
            String rsrcPath = mi.rsrcClassPath[i];
            if (rsrcPath.endsWith("/")) {
                rsrcUrls[i] = new URL(new StringBuffer("rsrc:").append(rsrcPath).toString());
            } else {
                rsrcUrls[i] = new URL(new StringBuffer("jar:rsrc:").append(rsrcPath).append("!/").toString());
            }
        }
        ClassLoader jceClassLoader = new URLClassLoader(rsrcUrls, null);
        Thread.currentThread().setContextClassLoader(jceClassLoader);
        Class.forName(mi.rsrcMainClass, true, jceClassLoader).getMethod("main", new Class[]{args.getClass()}).invoke(null, new Object[]{args});
    }

    private static ManifestInfo getManifestInfo() throws IOException {
        Enumeration resEnum = Thread.currentThread().getContextClassLoader().getResources("META-INF/MANIFEST.MF");
        while (resEnum.hasMoreElements()) {
            try {
                InputStream is = ((URL) resEnum.nextElement()).openStream();
                if (is != null) {
                    ManifestInfo result = new ManifestInfo(null);
                    Attributes mainAttribs = new Manifest(is).getMainAttributes();
                    result.rsrcMainClass = mainAttribs.getValue("Rsrc-Main-Class");
                    String rsrcCP = mainAttribs.getValue("Rsrc-Class-Path");
                    if (rsrcCP == null) {
                        rsrcCP = "";
                    }
                    result.rsrcClassPath = splitSpaces(rsrcCP);
                    if (result.rsrcMainClass != null && !result.rsrcMainClass.trim().equals("")) {
                        return result;
                    }
                } else {
                    continue;
                }
            } catch (Exception e) {
            }
        }
        System.err.println("Missing attributes for JarRsrcLoader in Manifest (Rsrc-Main-Class, Rsrc-Class-Path)");
        return null;
    }

    private static String[] splitSpaces(String line) {
        if (line == null) {
            return null;
        }
        List result = new ArrayList();
        int firstPos = 0;
        while (firstPos < line.length()) {
            int lastPos = line.indexOf(32, firstPos);
            if (lastPos == -1) {
                lastPos = line.length();
            }
            if (lastPos > firstPos) {
                result.add(line.substring(firstPos, lastPos));
            }
            firstPos = lastPos + 1;
        }
        return (String[]) result.toArray(new String[result.size()]);
    }
}
