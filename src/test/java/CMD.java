import java.util.Scanner;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CMD extends Shell {
    private Text text;

    public static void main(String[] args) {
        try {
            System.out.println(10);
            Display display = Display.getDefault();
            CMD shell = new CMD(display);
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

    public CMD(Display display) {
        super(display, 1264);
        setLayout(new FillLayout(256));
        Composite composite = new Composite(this, 0);
        composite.setLayout(new GridLayout(1, false));
        this.text = new Text(composite, 2050);
        this.text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
            }
        });
        this.text.setLayoutData(new GridData(4, 4, true, true, 1, 1));
        Scanner textScan = new Scanner(this.text.getText());
        if (textScan.hasNextLine()) {
            System.out.println(textScan.nextLine());
        }
        createContents();
    }

    /* access modifiers changed from: protected */
    public void createContents() {
        setText("SWT Application");
        setSize(634, 469);
    }

    /* access modifiers changed from: protected */
    public void checkSubclass() {
    }
}
