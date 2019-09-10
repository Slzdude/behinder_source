import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONUtils {
    private String buildJsonArray(List<Map<String, String>> list, boolean encode) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Map<String, String> entity : list) {
            sb.append(buildJson(entity, encode) + ",");
        }
        sb.setLength(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

    private String buildJson(Map<String, String> entity, boolean encode) throws Exception {
        StringBuilder sb = new StringBuilder();
        String version = System.getProperty("java.version");
        sb.append("{");
        for (String key : entity.keySet()) {
            sb.append("\"" + key + "\":\"");
            String value = ((String) entity.get(key)).toString();
            if (encode) {
                if (version.compareTo("1.9") >= 0) {
                    getClass();
                    Class Base64 = Class.forName("java.util.Base64");
                    Object Encoder = Base64.getMethod("getEncoder", null).invoke(Base64, null);
                    value = (String) Encoder.getClass().getMethod("encodeToString", new Class[]{byte[].class}).invoke(Encoder, new Object[]{value.getBytes()});
                } else {
                    getClass();
                    Object Encoder2 = Class.forName("sun.misc.BASE64Encoder").newInstance();
                    value = ((String) Encoder2.getClass().getMethod("encode", new Class[]{byte[].class}).invoke(Encoder2, new Object[]{value.getBytes()})).replace("\n", "").replace("\r", "");
                }
            }
            sb.append(value);
            sb.append("\",");
        }
        sb.setLength(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    public static void main(String[] args) throws Exception, Exception {
        System.out.println(System.getProperty("java.version"));
        List t = new ArrayList();
        Map<String, String> obj = new HashMap<>();
        t.add(obj);
        obj.put("aaa", "ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc");
        System.out.println(new JSONUtils().buildJsonArray(t, true));
        System.out.println(new JSONUtils().buildJson(obj, true));
    }
}
