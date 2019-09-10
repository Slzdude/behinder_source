package net.rebeyond.behinder.utils;

import net.rebeyond.behinder.core.ShellService;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class ProxyUtils extends Thread {
    public static int bufSize = 65535;
    private String bindAddress;
    private String bindPort;
    /* access modifiers changed from: private */
    public ShellService currentShellService;
    /* access modifiers changed from: private */
    public StyledText logContent;
    private Thread proxy;
    /* access modifiers changed from: private */
    public Thread r;
    private ServerSocket serverSocket;
    /* access modifiers changed from: private */
    public Label statusLabel;
    /* access modifiers changed from: private */
    public Thread w;

    private class Session extends Thread {
        /* access modifiers changed from: private */
        public Socket socket;

        private class Reader extends Thread {
            private Reader() {
            }

            /* synthetic */ Reader(Session session, Reader reader) {
                this();
            }

            public void run() {
                while (Session.this.socket != null) {
                    try {
                        byte[] data = ProxyUtils.this.currentShellService.readProxyData();
                        if (data == null) {
                            return;
                        }
                        if (data.length == 0) {
                            Thread.sleep(100);
                        } else {
                            Session.this.socket.getOutputStream().write(data);
                            Session.this.socket.getOutputStream().flush();
                        }
                    } catch (Exception e) {
                        ProxyUtils.this.log("ERROR", "数据读取异常:" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

        private class Writer extends Thread {
            private Writer() {
            }

            /* synthetic */ Writer(Session session, Writer writer) {
                this();
            }

            public void run() {
                while (Session.this.socket != null) {
                    try {
                        Session.this.socket.setSoTimeout(1000);
                        byte[] data = new byte[ProxyUtils.bufSize];
                        int length = Session.this.socket.getInputStream().read(data);
                        if (length != -1) {
                            ProxyUtils.this.currentShellService.writeProxyData(Arrays.copyOfRange(data, 0, length));
                        }
                    } catch (SocketTimeoutException e) {
                    } catch (Exception e2) {
                        ProxyUtils.this.log("ERROR", "数据写入异常:" + e2.getMessage());
                        e2.printStackTrace();
                    }
                }
                try {
                    ProxyUtils.this.currentShellService.closeProxy();
                    ProxyUtils.this.log("INFO", "隧道关闭成功。");
                    Session.this.socket.close();
                } catch (Exception e3) {
                    ProxyUtils.this.log("ERROR", "隧道关闭失败:" + e3.getMessage());
                    e3.printStackTrace();
                }
            }
        }

        public Session(Socket socket2) {
            this.socket = socket2;
        }

        public void run() {
            try {
                if (handleSocks(this.socket)) {
                    ProxyUtils.this.log("INFO", "正在通信...");
                    ProxyUtils.this.r = new Reader(this, null);
                    ProxyUtils.this.w = new Writer(this, null);
                    ProxyUtils.this.r.start();
                    ProxyUtils.this.w.start();
                    ProxyUtils.this.r.join();
                    ProxyUtils.this.w.join();
                }
            } catch (Exception e) {
                try {
                    ProxyUtils.this.currentShellService.closeProxy();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

        private boolean handleSocks(Socket socket2) throws Exception {
            int ver = socket2.getInputStream().read();
            if (ver == 5) {
                return parseSocks5(socket2);
            }
            if (ver == 4) {
                return parseSocks4(socket2);
            }
            return false;
        }

        private boolean parseSocks5(Socket socket2) throws Exception {
            int cmd;
            int atyp;
            DataInputStream ins = new DataInputStream(socket2.getInputStream());
            DataOutputStream os = new DataOutputStream(socket2.getOutputStream());
            int read = ins.read();
            int read2 = ins.read();
            byte[] bArr = new byte[2];
            bArr[0] = 5;
            os.write(bArr);
            if (ins.read() == 2) {
                int version = ins.read();
                cmd = ins.read();
                int read3 = ins.read();
                atyp = ins.read();
            } else {
                cmd = ins.read();
                int read4 = ins.read();
                atyp = ins.read();
            }
            byte[] targetPort = new byte[2];
            String host = "";
            if (atyp == 1) {
                byte[] target = new byte[4];
                ins.readFully(target);
                ins.readFully(targetPort);
                String[] tempArray = new String[4];
                for (int i = 0; i < target.length; i++) {
                    tempArray[i] = String.valueOf(target[i] & 255);
                }
                int length = tempArray.length;
                for (int i2 = 0; i2 < length; i2++) {
                    host = host + tempArray[i2] + ".";
                }
                host = host.substring(0, host.length() - 1);
            } else if (atyp == 3) {
                byte[] target2 = new byte[ins.read()];
                ins.readFully(target2);
                ins.readFully(targetPort);
                host = new String(target2);
            } else if (atyp == 4) {
                byte[] target3 = new byte[16];
                ins.readFully(target3);
                ins.readFully(targetPort);
                host = new String(target3);
            }
            int port = ((targetPort[0] & 255) * 256) + (targetPort[1] & 255);
            if (cmd == 2 || cmd == 3) {
                throw new Exception("not implemented");
            } else if (cmd == 1) {
                String host2 = InetAddress.getByName(host).getHostAddress();
                if (ProxyUtils.this.currentShellService.openProxy(host2, String.valueOf(port))) {
                    byte[] bArr2 = new byte[4];
                    bArr2[0] = 5;
                    bArr2[3] = 1;
                    os.write(CipherUtils.mergeByteArray(bArr2, InetAddress.getByName(host2).getAddress(), targetPort));
                    ProxyUtils.this.log("INFO", "隧道建立成功，请求远程地址" + host2 + ":" + port);
                    return true;
                }
                byte[] bArr3 = new byte[4];
                bArr3[0] = 5;
                bArr3[3] = 1;
                os.write(CipherUtils.mergeByteArray(bArr3, InetAddress.getByName(host2).getAddress(), targetPort));
                throw new Exception(String.format("[%s:%d] Remote failed", host2, Integer.valueOf(port)));
            } else {
                throw new Exception("Socks5 - Unknown CMD");
            }
        }

        private boolean parseSocks4(Socket socket2) {
            return false;
        }
    }

    public ProxyUtils(ShellService shellService, String bindAddress2, String bindPort2, StyledText proxyLogTxt, Label statusLabel2) throws Exception {
        this.currentShellService = shellService;
        this.bindAddress = bindAddress2;
        this.bindPort = bindPort2;
        this.logContent = proxyLogTxt;
        this.statusLabel = statusLabel2;
    }

    /* access modifiers changed from: private */
    public void log(String type, String log) {
        final String logLine = "[" + type + "]" + log + "\n";
        final Display display = Display.getDefault();
        final int color = type.equals("ERROR") ? 3 : 9;
        display.syncExec(new Runnable() {
            public void run() {
                if (!ProxyUtils.this.statusLabel.isDisposed()) {
                    ProxyUtils.this.logContent.append(logLine);
                    StyleRange styleRange = new StyleRange();
                    styleRange.start = ProxyUtils.this.logContent.getText().length() - logLine.length();
                    styleRange.length = logLine.length();
                    styleRange.foreground = display.getSystemColor(color);
                    ProxyUtils.this.logContent.setStyleRange(styleRange);
                    ProxyUtils.this.logContent.showSelection();
                }
            }
        });
    }

    public void shutdown() {
        log("INFO", "正在关闭代理服务");
        try {
            if (this.r != null) {
                this.r.stop();
            }
            if (this.w != null) {
                this.w.stop();
            }
            if (this.proxy != null) {
                this.proxy.stop();
            }
            this.serverSocket.close();
        } catch (IOException e) {
            log("ERROR", "代理服务关闭异常:" + e.getMessage());
        }
        log("INFO", "代理服务已停止");
    }

    public void run() {
        try {
            this.proxy = Thread.currentThread();
            this.serverSocket = new ServerSocket(Integer.parseInt(this.bindPort), 50, InetAddress.getByName(this.bindAddress));
            this.serverSocket.setReuseAddress(true);
            log("INFO", "正在监听端口" + this.bindPort);
            while (true) {
                Socket socket = this.serverSocket.accept();
                log("INFO", "收到客户端连接请求.");
                new Session(socket).start();
            }
        } catch (IOException e) {
            log("ERROR", "端口监听失败：" + e.getMessage());
        }
    }
}
