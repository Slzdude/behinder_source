import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;

public class vvvv {
    public static void main(String[] args) {
        Display display = Display.getDefault();
        Shell shell = new Shell();
        shell.setSize(738, 574);
        shell.setText("SWT Application");
        shell.setLayout(new FillLayout());
        ViewForm viewForm = new ViewForm(shell, 0);
        viewForm.setLayout(new FillLayout());
        ToolBar toolBar = new ToolBar(viewForm, 0);
        viewForm.setContent(new Text(viewForm, 2560));
        new ToolItem(toolBar, 8).setText("È¡µÃ");
        new ToolItem(toolBar, 8).setText("Çå³ý");
        viewForm.setTopLeft(toolBar);
        CLabel lblNewLabel = new CLabel(viewForm, 0);
        lblNewLabel.setImage(SWTResourceManager.getImage(vvvv.class, "/javax/swing/plaf/basic/icons/JavaCup16.png"));
        viewForm.setTopCenter(lblNewLabel);
        lblNewLabel.setText("New Label");
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
}
