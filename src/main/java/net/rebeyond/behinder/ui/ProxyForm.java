package net.rebeyond.behinder.ui;

import net.rebeyond.behinder.utils.Constants;
import net.rebeyond.behinder.utils.Utils;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

public class ProxyForm extends Shell {
    /* access modifiers changed from: private */
    public Text ProxyPassTxt;
    private Label ProxyStatusLabel;
    /* access modifiers changed from: private */
    public Text ProxyUserTxt;
    private Composite btnComp;
    /* access modifiers changed from: private */
    public Button disableProxyBtn;
    /* access modifiers changed from: private */
    public Button enableProxyBtn;
    private Label errorLabel;
    private Composite proxyComp = new Composite(this, 0);
    /* access modifiers changed from: private */
    public Text proxyIPTxt;
    /* access modifiers changed from: private */
    public Text proxyPortTxt;
    private Composite switchComp;

    private void loadProxyInfo() throws Exception {
        boolean enable;
        JSONObject proxyEntity = Main.shellManager.findProxy("default");
        this.proxyIPTxt.setText(proxyEntity.getString("ip"));
        this.proxyPortTxt.setText(String.valueOf(proxyEntity.getInt("port")));
        this.ProxyUserTxt.setText(proxyEntity.getString("username"));
        this.ProxyPassTxt.setText(proxyEntity.getString("password"));
        enable = proxyEntity.getInt("status") == Constants.PROXY_ENABLE;
        if (enable) {
            this.enableProxyBtn.setSelection(true);
            return;
        }
        this.disableProxyBtn.setSelection(true);
        enableControls(false);
    }

    /* access modifiers changed from: private */
    public void enableControls(boolean enable) {
        Control[] children;
        for (Control control : this.proxyComp.getChildren()) {
            if (control == this.switchComp || control == this.btnComp) {
                control.setEnabled(true);
            } else {
                control.setEnabled(enable);
            }
        }
    }

    /* access modifiers changed from: private */
    public void showError(String errorTxt) {
        MessageBox dialog = new MessageBox(getShell(), 33);
        dialog.setText("保存失败");
        dialog.setMessage(errorTxt);
        dialog.open();
    }

    public ProxyForm(Display display, Label ProxyStatusLabel2) {
        super(display, 1264);
        this.ProxyStatusLabel = ProxyStatusLabel2;
        setLayout(new FillLayout(256));
        this.proxyComp.setLayout(new GridLayout(2, false));
        new Label(this.proxyComp, 0);
        this.switchComp = new Composite(this.proxyComp, 0);
        this.switchComp.setLayout(new GridLayout(2, false));
        this.enableProxyBtn = new Button(this.switchComp, 16);
        this.enableProxyBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                ProxyForm.this.enableControls(true);
            }
        });
        this.enableProxyBtn.setText("启用");
        this.disableProxyBtn = new Button(this.switchComp, 16);
        this.disableProxyBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                ProxyForm.this.enableControls(false);
            }
        });
        this.disableProxyBtn.setText("禁用");
        Label label = new Label(this.proxyComp, 0);
        label.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        label.setText("类型");
        Combo combo = new Combo(this.proxyComp, 0);
        combo.setText("HTTP");
        Label lblNewLabel = new Label(this.proxyComp, 0);
        lblNewLabel.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        lblNewLabel.setText("IP地址：");
        this.proxyIPTxt = new Text(this.proxyComp, 2048);
        this.proxyIPTxt.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        Label lblNewLabel_1 = new Label(this.proxyComp, 0);
        lblNewLabel_1.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        lblNewLabel_1.setText("端口：");
        this.proxyPortTxt = new Text(this.proxyComp, 2048);
        this.proxyPortTxt.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        Label label2 = new Label(this.proxyComp, 0);
        label2.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        label2.setText("用户名：");
        this.ProxyUserTxt = new Text(this.proxyComp, 2048);
        this.ProxyUserTxt.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        Label label3 = new Label(this.proxyComp, 0);
        label3.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        label3.setText("密码：");
        this.ProxyPassTxt = new Text(this.proxyComp, 2048);
        this.ProxyPassTxt.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        new Label(this.proxyComp, 0);
        this.errorLabel = new Label(this.proxyComp, 0);
        this.btnComp = new Composite(this.proxyComp, 0);
        GridLayout gl_btnComp = new GridLayout(2, true);
        gl_btnComp.verticalSpacing = 0;
        this.btnComp.setLayout(gl_btnComp);
        this.btnComp.setLayoutData(new GridData(4, 16777216, false, false, 2, 1));
        Button button = new Button(this.btnComp, 0);
        final Combo combo2 = combo;
        final Label label4 = ProxyStatusLabel2;
        final Button button2 = button;
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                try {
                    String type = combo2.getText();
                    if (!type.toUpperCase().equals("HTTP")) {
                        ProxyForm.this.showError("目前仅支持HTTP类型的代理");
                        return;
                    }
                    String ip = ProxyForm.this.proxyIPTxt.getText().trim();
                    if (!Utils.checkIP(ip)) {
                        ProxyForm.this.showError("IP格式有误");
                        return;
                    }
                    String portTxt = ProxyForm.this.proxyPortTxt.getText().trim();
                    if (!Utils.checkPort(portTxt)) {
                        ProxyForm.this.showError("端口格式有误");
                        return;
                    }
                    int port = Integer.parseInt(portTxt);
                    String username = ProxyForm.this.ProxyUserTxt.getText();
                    String password = ProxyForm.this.ProxyPassTxt.getText();
                    int status = ProxyForm.this.enableProxyBtn.getSelection() ? 0 : 1;
                    if (ProxyForm.this.disableProxyBtn.getSelection()) {
                        Main.currentProxy = null;
                        label4.setVisible(false);
                    } else {
                        ProxyForm.this.setProxy(type, ip, port, username, password);
                        label4.setVisible(true);
                    }
                    try {
                        Main.shellManager.updateProxy("default", "http", ip, port, username, password, status);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    button2.getShell().dispose();
                } catch (Exception e2) {
                    ProxyForm.this.showError(e2.getMessage());
                }
            }
        });
        button.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        button.setText("保存");
        final Button cancelProxyBtn = new Button(this.btnComp, 0);
        cancelProxyBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                cancelProxyBtn.getShell().dispose();
            }
        });
        cancelProxyBtn.setLayoutData(new GridData(16384, 16777216, true, false, 1, 1));
        cancelProxyBtn.setText("取消");
        new Label(this.proxyComp, 0);
        new Label(this.proxyComp, 0);
        createContents();
        try {
            loadProxyInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void setProxy(String type, String ip, int port, String username, String password) {
        Main.currentProxy = new Proxy(Type.HTTP, new InetSocketAddress(ip, port));
        Main.proxyUserName = username;
        Main.proxyPassword = password;
    }

    /* access modifiers changed from: protected */
    public void createContents() {
        setText("代理服务器设置");
        setSize(456, 285);
    }

    /* access modifiers changed from: protected */
    public void checkSubclass() {
    }
}
