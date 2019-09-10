package org.eclipse.jdt.internal.jarinjarloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

public class RsrcURLConnection extends URLConnection {
    private ClassLoader classLoader;

    public RsrcURLConnection(URL url, ClassLoader classLoader2) {
        super(url);
        this.classLoader = classLoader2;
    }

    public void connect() throws IOException {
    }

    public InputStream getInputStream() throws IOException {
        InputStream result = this.classLoader.getResourceAsStream(URLDecoder.decode(this.url.getFile(), "UTF-8"));
        if (result != null) {
            return result;
        }
        throw new MalformedURLException(new StringBuffer("Could not open InputStream for URL '").append(this.url).append("'").toString());
    }
}
