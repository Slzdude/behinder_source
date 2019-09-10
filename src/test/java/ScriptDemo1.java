import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

public class ScriptDemo1 {
    public static void main(String[] args) {
        String[] params;
        for (ScriptEngineFactory factory : new ScriptEngineManager().getEngineFactories()) {
            System.out.println("Full name = " + factory.getEngineName());
            System.out.println("Version = " + factory.getEngineVersion());
            System.out.println("Extensions");
            for (String extension : factory.getExtensions()) {
                System.out.println("   " + extension);
            }
            System.out.println("Language name = " + factory.getLanguageName());
            System.out.println("Language version = " + factory.getLanguageVersion());
            System.out.println("MIME Types");
            for (String mimetype : factory.getMimeTypes()) {
                System.out.println("   " + mimetype);
            }
            System.out.println("Short Names");
            for (String shortname : factory.getNames()) {
                System.out.println("   " + shortname);
            }
            for (String param : new String[]{"javax.script.engine", "javax.script.engine_version", "javax.script.language", "javax.script.language_version", "javax.script.name", "THREADING"}) {
                System.out.printf("Parameter %s = %s", new Object[]{param, factory.getParameter(param)});
                System.out.println();
            }
            System.out.println(factory.getScriptEngine());
            System.out.println();
        }
    }
}
