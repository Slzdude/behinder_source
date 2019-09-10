package org.eclipse.jdt.internal.jarinjarloader;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class RsrcURLStreamHandlerFactory implements URLStreamHandlerFactory {
    private URLStreamHandlerFactory chainFac;
    private ClassLoader classLoader;

    public RsrcURLStreamHandlerFactory(ClassLoader cl) {
        this.classLoader = cl;
    }

    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("rsrc".equals(protocol)) {
            return new RsrcURLStreamHandler(this.classLoader);
        }
        if (this.chainFac != null) {
            return this.chainFac.createURLStreamHandler(protocol);
        }
        return null;
    }

    public void setURLStreamHandlerFactory(URLStreamHandlerFactory fac) {
        this.chainFac = fac;
    }
}
