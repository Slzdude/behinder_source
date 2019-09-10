package net.rebeyond.behinder.ui;

import net.rebeyond.behinder.utils.Constants;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.json.JSONObject;

import java.net.URL;

public class ShellForm extends Shell {
    /* access modifiers changed from: private */
    public String ErrorMsg = "";
    /* access modifiers changed from: private */
    public Label PassErrorLabel;
    /* access modifiers changed from: private */
    public CustomStyledText PassTxt;
    /* access modifiers changed from: private */
    public CustomStyledText UrlTxt;
    /* access modifiers changed from: private */
    public CustomStyledText commentTxt;
    /* access modifiers changed from: private */
    public StyledText headerTxt;
    private Label label;
    /* access modifiers changed from: private */
    public Combo typeCombo;
    /* access modifiers changed from: private */
    public Label urlErrorLabel;

    private void loadShellInfo(int shellID) throws Exception {
        JSONObject shellEntity = Main.shellManager.findShell(shellID);
        this.UrlTxt.setText(shellEntity.getString("url"));
        this.PassTxt.setText(shellEntity.getString("password"));
        this.typeCombo.setText(shellEntity.getString("type"));
        this.commentTxt.setText(shellEntity.getString("comment"));
        this.headerTxt.setText(shellEntity.getString("headers"));
    }

    public ShellForm(Display display, int shellID, Main me) {
        super(display, 1264);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginTop = 20;
        gridLayout.marginLeft = 10;
        gridLayout.marginHeight = 0;
        setLayout(gridLayout);
        Label label2 = new Label(this, 0);
        label2.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        label2.setText("URL：");
        this.UrlTxt = new CustomStyledText(this, 2048, Constants.MENU_ALL);
        this.UrlTxt.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                try {
                    URL url = new URL(ShellForm.this.UrlTxt.getText().trim());
                    ShellForm.this.ErrorMsg = "";
                    ShellForm.this.urlErrorLabel.setText("");
                    ShellForm.this.typeCombo.setText(url.getPath().substring(url.getPath().lastIndexOf(".") + 1).toLowerCase());
                } catch (Exception e) {
                    ShellForm.this.ErrorMsg = "URL格式错误";
                    ShellForm.this.urlErrorLabel.setText(ShellForm.this.ErrorMsg);
                }
            }
        });
        this.UrlTxt.setLayoutData(new GridData(4, 16777216, false, false, 1, 1));
        new Label(this, 0);
        this.urlErrorLabel = new Label(this, 0);
        this.urlErrorLabel.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        this.urlErrorLabel.setForeground(Display.getDefault().getSystemColor(3));
        Label label3 = new Label(this, 0);
        GridData gd_lblNewLabel_2 = new GridData(131072, 16777216, false, false, 1, 1);
        gd_lblNewLabel_2.verticalIndent = 10;
        label3.setLayoutData(gd_lblNewLabel_2);
        label3.setText("密码：");
        this.PassTxt = new CustomStyledText(this, 2048, Constants.MENU_ALL);
        this.PassTxt.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                if (ShellForm.this.PassTxt.getText().length() > 255) {
                    ShellForm.this.PassErrorLabel.setText("密码长度不应大于255个字符");
                    ShellForm.this.ErrorMsg = "密码长度不应大于255个字符";
                    return;
                }
                ShellForm.this.PassErrorLabel.setText("");
                ShellForm.this.ErrorMsg = "";
            }
        });
        GridData gd_PassTxt = new GridData(16384, 16777216, true, false, 1, 1);
        gd_PassTxt.widthHint = 100;
        gd_PassTxt.verticalIndent = 10;
        this.PassTxt.setLayoutData(gd_PassTxt);
        new Label(this, 0);
        this.PassErrorLabel = new Label(this, 0);
        this.PassErrorLabel.setForeground(Display.getDefault().getSystemColor(3));
        Label label4 = new Label(this, 0);
        GridData gd_lblNewLabel_1 = new GridData(131072, 16777216, false, false, 1, 1);
        gd_lblNewLabel_1.verticalIndent = 10;
        label4.setLayoutData(gd_lblNewLabel_1);
        label4.setText("类型：");
        this.typeCombo = new Combo(this, 8);
        this.typeCombo.setItems(new String[]{"jsp", "php", "aspx", "asp"});
        GridData gd_typeCombo = new GridData(16384, 16777216, false, false, 1, 1);
        gd_typeCombo.widthHint = 50;
        gd_typeCombo.verticalIndent = 10;
        this.typeCombo.setLayoutData(gd_typeCombo);
        Label label5 = new Label(this, 0);
        GridData gd_lblNewLabel_3 = new GridData(131072, 16777216, false, true, 1, 1);
        gd_lblNewLabel_3.verticalIndent = 10;
        label5.setLayoutData(gd_lblNewLabel_3);
        label5.setText("备注：");
        this.commentTxt = new CustomStyledText(this, 2048, Constants.MENU_ALL);
        GridData gd_commentTxt = new GridData(4, 4, true, false, 1, 1);
        gd_commentTxt.verticalIndent = 10;
        this.commentTxt.setLayoutData(gd_commentTxt);
        this.label = new Label(this, 0);
        this.label.setText("请求头：");
        this.headerTxt = new CustomStyledText(this, 2048, Constants.MENU_ALL);
        final Display display2 = display;
        this.headerTxt.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                int rangeStart;
                int i;
                String[] lines = ShellForm.this.headerTxt.getText().trim().split("\r");
                int currPos = 0;
                for (int i2 = 0; i2 < lines.length; i2++) {
                    String line = lines[i2].trim();
                    StyleRange rangeKey = new StyleRange();
                    if (i2 == 0) {
                        rangeStart = 0;
                    } else {
                        rangeStart = currPos + 1;
                    }
                    int semiIndex = line.indexOf(":");
                    rangeKey.start = rangeStart;
                    if (semiIndex == -1) {
                        i = 0;
                    } else {
                        i = semiIndex;
                    }
                    rangeKey.length = i;
                    rangeKey.foreground = display2.getSystemColor(3);
                    ShellForm.this.headerTxt.setStyleRange(rangeKey);
                    if (semiIndex > -1 && line.length() > semiIndex) {
                        StyleRange rangeValue = new StyleRange();
                        rangeValue.start = rangeStart + semiIndex + 1;
                        rangeValue.length = (line.length() - semiIndex) - 1;
                        rangeValue.foreground = display2.getSystemColor(9);
                        ShellForm.this.headerTxt.setStyleRange(rangeValue);
                    }
                    currPos += lines[i2].length() + 1;
                }
            }
        });
        this.headerTxt.setLayoutData(new GridData(4, 4, true, true, 1, 1));
        new Label(this, 0);
        Button button = new Button(this, 0);
        final int i = shellID;
        final Button button2 = button;
        final Main main = me;
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (!ShellForm.this.ErrorMsg.equals("")) {
                    ShellForm.this.showErrorMessage("添加失败", ShellForm.this.ErrorMsg);
                    return;
                }
                String url = ShellForm.this.UrlTxt.getText().trim();
                String password = ShellForm.this.PassTxt.getText();
                if (password.length() < 1) {
                    ShellForm.this.showErrorMessage("错误", "密码不能为空，请输入密码");
                    return;
                }
                String type = ShellForm.this.typeCombo.getText();
                String comment = ShellForm.this.commentTxt.getText();
                String headers = ShellForm.this.headerTxt.getText();
                try {
                    if (i == -1) {
                        Main.shellManager.addShell(url, password, type, comment, headers);
                    } else {
                        Main.shellManager.updateShell(i, url, password, type, comment, headers);
                    }
                    button2.getShell().dispose();
                    main.fillShells();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    ShellForm.this.showErrorMessage("保存失败", e1.getMessage());
                }
            }
        });
        GridData gd_addShellBtn = new GridData(16777216, 16777216, false, false, 1, 1);
        gd_addShellBtn.widthHint = 80;
        gd_addShellBtn.verticalIndent = 10;
        button.setLayoutData(gd_addShellBtn);
        button.setText("保存");
        if (shellID != -1) {
            try {
                loadShellInfo(shellID);
            } catch (Exception e1) {
                e1.printStackTrace();
                showErrorMessage("加载失败", e1.getMessage());
                return;
            }
        }
        createContents();
    }

    /* access modifiers changed from: private */
    public void showErrorMessage(String title, String msg) {
        MessageBox dialog = new MessageBox(this, 33);
        dialog.setText(title);
        dialog.setMessage(msg);
        dialog.open();
    }

    /* access modifiers changed from: protected */
    public void createContents() {
        setText("新增");
        setSize(618, 355);
    }

    /* access modifiers changed from: protected */
    public void checkSubclass() {
    }
}
