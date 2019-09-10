package org.eclipse.jdt.internal.jarinjarloader;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class RsrcURLStreamHandler extends URLStreamHandler {
    private ClassLoader classLoader;

    public RsrcURLStreamHandler(ClassLoader classLoader2) {
        this.classLoader = classLoader2;
    }

    /* access modifiers changed from: protected */
    public URLConnection openConnection(URL u) throws IOException {
        return new RsrcURLConnection(u, this.classLoader);
    }

    /* access modifiers changed from: protected */
    public void parseURL(URL url, String spec, int start, int limit) {
        String file;
        if (spec.startsWith("rsrc:")) {
            file = spec.substring(5);
        } else if (url.getFile().equals("./")) {
            file = spec;
        } else if (url.getFile().endsWith("/")) {
            file = new StringBuffer(String.valueOf(url.getFile())).append(spec).toString();
        } else {
            file = spec;
        }
        setURL(url, "rsrc", "", -1, null, null, file, null, null);
    }
}
