package net.rebeyond.behinder.utils;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

public class SSLExcludeCipherConnectionHelper {
    private String[] exludedCipherSuites = {"_DHE_", "_DH_"};
    private TrustManagerFactory tmf;
    private String trustCert = null;

    private class DOSSLSocketFactory extends SSLSocketFactory {
        private String[] enabledCiphers;
        private SSLSocketFactory sf;

        private DOSSLSocketFactory(SSLSocketFactory sf2, String[] enabledCiphers2) {
            this.sf = null;
            this.enabledCiphers = null;
            this.sf = sf2;
            this.enabledCiphers = enabledCiphers2;
        }

        /* synthetic */ DOSSLSocketFactory(SSLExcludeCipherConnectionHelper sSLExcludeCipherConnectionHelper, SSLSocketFactory sSLSocketFactory, String[] strArr, DOSSLSocketFactory dOSSLSocketFactory) {
            this(sSLSocketFactory, strArr);
        }

        private Socket getSocketWithEnabledCiphers(Socket socket) {
            if (!(this.enabledCiphers == null || socket == null || !(socket instanceof SSLSocket))) {
                ((SSLSocket) socket).setEnabledCipherSuites(this.enabledCiphers);
            }
            return socket;
        }

        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            return getSocketWithEnabledCiphers(this.sf.createSocket(s, host, port, autoClose));
        }

        public String[] getDefaultCipherSuites() {
            return this.sf.getDefaultCipherSuites();
        }

        public String[] getSupportedCipherSuites() {
            if (this.enabledCiphers == null) {
                return this.sf.getSupportedCipherSuites();
            }
            return this.enabledCiphers;
        }

        public Socket createSocket(String host, int port) throws IOException {
            return getSocketWithEnabledCiphers(this.sf.createSocket(host, port));
        }

        public Socket createSocket(InetAddress address, int port) throws IOException {
            return getSocketWithEnabledCiphers(this.sf.createSocket(address, port));
        }

        public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException {
            return getSocketWithEnabledCiphers(this.sf.createSocket(host, port, localAddress, localPort));
        }

        public Socket createSocket(InetAddress address, int port, InetAddress localaddress, int localport) throws IOException {
            return getSocketWithEnabledCiphers(this.sf.createSocket(address, port, localaddress, localport));
        }
    }

    public void setExludedCipherSuites(String[] exludedCipherSuites2) {
        this.exludedCipherSuites = exludedCipherSuites2;
    }

    public SSLExcludeCipherConnectionHelper(String trustCert2) {
        this.trustCert = trustCert2;
        try {
            initTrustManager();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* JADX INFO: finally extract failed */
    private void initTrustManager() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = new BufferedInputStream(new FileInputStream(this.trustCert));
        try {
            Certificate ca = cf.generateCertificate(caInput);
            caInput.close();
            KeyStore keyStore = KeyStore.getInstance("jks");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            this.tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            this.tmf.init(keyStore);
        } catch (Throwable th) {
            caInput.close();
            throw th;
        }
    }

    public String get(URL url) throws Exception {
        String[] cipherSuites;
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, this.tmf.getTrustManagers(), null);
        SSLParameters params = context.getSupportedSSLParameters();
        List<String> enabledCiphers = new ArrayList<>();
        for (String cipher : params.getCipherSuites()) {
            boolean exclude = false;
            if (this.exludedCipherSuites != null) {
                for (int i = 0; i < this.exludedCipherSuites.length && !exclude; i++) {
                    exclude = cipher.indexOf(this.exludedCipherSuites[i]) >= 0;
                }
            }
            if (!exclude) {
                enabledCiphers.add(cipher);
            }
        }
        String[] cArray = new String[enabledCiphers.size()];
        enabledCiphers.toArray(cArray);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setSSLSocketFactory(new DOSSLSocketFactory(this, context.getSocketFactory(), cArray, null));
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuffer buffer = new StringBuffer();
        while (true) {
            String inputLine = in.readLine();
            if (inputLine == null) {
                in.close();
                return buffer.toString();
            }
            buffer.append(inputLine);
        }
    }
}
