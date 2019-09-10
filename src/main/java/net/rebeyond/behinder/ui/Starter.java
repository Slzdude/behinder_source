package net.rebeyond.behinder.ui;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Starter {
    public static void main(String[] args) {
        System.setProperty("org.eclipse.swt.browser.DefaultType", "ie,webkit");
        addJarToClasspath(getArchFilename("swt"));
        new Main().start();
    }

    public static String getArchFilename(String prefix) {
        return prefix + "_" + getOSName() + "_" + getArchName() + ".jar";
    }

    private static String getOSName() {
        String osNameProperty = System.getProperty("os.name");
        if (osNameProperty == null) {
            throw new RuntimeException("os.name property is not set");
        }
        String osNameProperty2 = osNameProperty.toLowerCase();
        if (osNameProperty2.contains("win")) {
            return "win";
        }
        if (osNameProperty2.contains("mac")) {
            return "osx";
        }
        if (osNameProperty2.contains("linux") || osNameProperty2.contains("nix")) {
            return "linux";
        }
        throw new RuntimeException("Unknown OS name: " + osNameProperty2);
    }

    private static String getArchName() {
        String osArch = System.getProperty("os.arch");
        if (osArch == null || !osArch.contains("64")) {
            return "32";
        }
        return "64";
    }

    public static void addJarToClasspath(String jarFile) {
        try {
            URL url = Starter.class.getClassLoader().getResource("net/rebeyond/behinder/resource/lib/" + jarFile);
            URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(urlClassLoader, url);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
