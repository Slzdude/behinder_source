import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    public static void main(String[] args) throws Exception {
        String str = "cookie-";
        System.out.println(checkPort("1080"));
    }

    public static boolean checkPort(String portTxt) {
        Matcher matcher = Pattern.compile("([1-9]{1,5})").matcher(portTxt);
        System.out.println(matcher.matches());
        if (!matcher.matches() || Integer.parseInt(portTxt) < 1 || Integer.parseInt(portTxt) > 65535) {
            return false;
        }
        return true;
    }

    private static Map<String, String> parseHeaders(String headerTxt) {
        String[] split;
        Map<String, String> headers = new HashMap<>();
        for (String line : headerTxt.split("\n")) {
            int semiIndex = line.indexOf(":");
            if (semiIndex > 0) {
                String key = formatHeaderName(line.substring(0, semiIndex));
                String value = line.substring(semiIndex + 1);
                if (!value.equals("")) {
                    headers.put(key, value);
                }
            }
        }
        return headers;
    }

    private static String formatHeaderName(String beforeName) {
        String[] split;
        String afterName = "";
        for (String element : beforeName.split("-")) {
            afterName = String.valueOf(afterName) + String.valueOf(String.valueOf(element.charAt(0)).toUpperCase()) + element.substring(1).toLowerCase() + "-";
        }
        if (afterName.length() - beforeName.length() != 1 || !afterName.endsWith("-")) {
            return afterName;
        }
        return afterName.substring(0, afterName.length() - 1);
    }
}
