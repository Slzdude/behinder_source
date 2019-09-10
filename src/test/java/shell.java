import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class shell extends Shell {
    public static void main(String[] args) {
        try {
            Display display = Display.getDefault();
            shell shell = new shell(display);
            shell.open();
            shell.layout();
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public shell(Display display) {
        throw new Error("Unresolved compilation problem: \n\tThe constructor CustomStyledText(shell, int) is undefined\n");
    }

    /* access modifiers changed from: protected */
    public void createContents() {
        setText("SWT Application");
        setSize(450, 300);
    }

    /* access modifiers changed from: protected */
    public void checkSubclass() {
    }
}
