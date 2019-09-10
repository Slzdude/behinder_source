package net.rebeyond.behinder.ui;

import net.rebeyond.behinder.core.ShellService;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.json.JSONObject;

public class ConnectBackUtils {
    /* access modifiers changed from: private */
    public ShellService currentShellService;
    /* access modifiers changed from: private */
    public Label statusLabel;

    public ConnectBackUtils(ShellService shellService, Label statusLabel2) {
        this.currentShellService = shellService;
        this.statusLabel = statusLabel2;
    }

    public void connectBack(Button btn, final String type, final String ip, final String port) {
        new Thread() {
            public void run() {
                try {
                    final JSONObject resultObj = ConnectBackUtils.this.currentShellService.connectBack(type, ip, port);
                    final String status = resultObj.getString("status");
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            if (!ConnectBackUtils.this.statusLabel.isDisposed() && status.equals("fail")) {
                                ConnectBackUtils.this.statusLabel.setText("²Ù×÷Ê§°Ü:" + resultObj.getString("msg"));
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getMessage() != null) {
                        Display.getDefault().syncExec(new Runnable() {
                            public void run() {
                                if (!ConnectBackUtils.this.statusLabel.isDisposed()) {
                                    ConnectBackUtils.this.statusLabel.setText(e.getMessage());
                                }
                            }
                        });
                    }
                }
            }
        }.start();
    }
}
