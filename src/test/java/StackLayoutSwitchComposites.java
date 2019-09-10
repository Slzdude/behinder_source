import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class StackLayoutSwitchComposites {
    static int pageNum = -1;

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setBounds(10, 10, 300, 200);
        final Composite contentPanel = new Composite(shell, 2048);
        contentPanel.setBounds(100, 10, 190, 90);
        final StackLayout layout = new StackLayout();
        contentPanel.setLayout(layout);
        final Composite page0 = new Composite(contentPanel, 0);
        page0.setLayout(new RowLayout());
        Label label = new Label(page0, 0);
        label.setText("Label on page 1");
        label.pack();
        final Composite page1 = new Composite(contentPanel, 0);
        page1.setLayout(new RowLayout());
        Button button = new Button(page1, 0);
        button.setText("Button on page 2");
        button.pack();
        Button pageButton = new Button(shell, 8);
        pageButton.setText("Push");
        pageButton.setBounds(10, 10, 80, 25);
        pageButton.addListener(13, new Listener() {
            public void handleEvent(Event event) {
                int i = StackLayoutSwitchComposites.pageNum + 1;
                StackLayoutSwitchComposites.pageNum = i;
                StackLayoutSwitchComposites.pageNum = i % 2;
                layout.topControl = StackLayoutSwitchComposites.pageNum == 0 ? page0 : page1;
                contentPanel.layout();
            }
        });
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
