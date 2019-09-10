import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;

public class ConvertMap2Json {
    private static final String QUOTE = "\"";

    public static String buildJsonBody(Map<String, Object> body, int tabCount, boolean addComma) {
        StringBuilder sbJsonBody = new StringBuilder();
        sbJsonBody.append("{\n");
        Set<String> keySet = body.keySet();
        int count = 0;
        int size = keySet.size();
        for (String key : keySet) {
            count++;
            sbJsonBody.append(buildJsonField(key, body.get(key), tabCount + 1, count != size));
        }
        sbJsonBody.append(getTab(tabCount));
        sbJsonBody.append("}");
        return sbJsonBody.toString();
    }

    private static String buildJsonField(String key, Object value, int tabCount, boolean addComma) {
        String sbJsonField = getTab(tabCount) +
                QUOTE + key + QUOTE + ": " +
                buildJsonValue(value, tabCount, addComma);
        return sbJsonField;
    }

    private static String buildJsonValue(Object value, int tabCount, boolean addComma) {
        StringBuilder sbJsonValue = new StringBuilder();
        if (value instanceof String) {
            sbJsonValue.append(QUOTE).append(value).append(QUOTE);
        } else if ((value instanceof Integer) || (value instanceof Long) || (value instanceof Double)) {
            sbJsonValue.append(value);
        } else if (value instanceof Date) {
            sbJsonValue.append(QUOTE).append(formatDate((Date) value)).append(QUOTE);
        } else if (value.getClass().isArray() || (value instanceof Collection)) {
            sbJsonValue.append(buildJsonArray(value, tabCount, addComma));
        } else if (value instanceof Map) {
            sbJsonValue.append(buildJsonBody((Map) value, tabCount, addComma));
        }
        sbJsonValue.append(buildJsonTail(addComma));
        return sbJsonValue.toString();
    }

    private static String buildJsonArray(Object value, int tabCount, boolean addComma) {
        StringBuilder sbJsonArray = new StringBuilder();
        sbJsonArray.append("[\n");
        Object[] objArray = null;
        if (value.getClass().isArray()) {
            objArray = (Object[]) value;
        } else if (value instanceof Collection) {
            objArray = ((Collection) value).toArray();
        }
        int size = objArray.length;
        int count = 0;
        for (Object obj : objArray) {
            sbJsonArray.append(getTab(tabCount + 1));
            count++;
            sbJsonArray.append(buildJsonValue(obj, tabCount + 1, count != size));
        }
        sbJsonArray.append(getTab(tabCount));
        sbJsonArray.append("]");
        return sbJsonArray.toString();
    }

    private static String getTab(int count) {
        StringBuilder sbTab = new StringBuilder();
        while (true) {
            int count2 = count;
            count = count2 - 1;
            if (count2 <= 0) {
                return sbTab.toString();
            }
            sbTab.append("\t");
        }
    }

    private static String buildJsonTail(boolean addComma) {
        return addComma ? ",\n" : "\n";
    }

    private static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static void main(String[] args) {
        List t = new ArrayList();
        Map<String, Object> obj = new HashMap<>();
        t.add(obj);
        obj.put("aaa", "bbb\"ff");
        new ConvertMap2Json();
        String res = buildJsonArray(t, 1, true);
        System.out.println(res);
        System.out.println(new JSONArray(res));
    }
}
