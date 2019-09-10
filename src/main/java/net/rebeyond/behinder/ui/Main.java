package net.rebeyond.behinder.ui;

import net.rebeyond.behinder.core.ShellManager;
import net.rebeyond.behinder.utils.Constants;
import net.rebeyond.behinder.utils.Utils;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Map;

public class Main {
    public static Proxy currentProxy;
    public static Map<String, String> globalHeaders;
    public static String proxyPassword;
    public static String proxyUserName;
    public static ShellManager shellManager;
    /* access modifiers changed from: private */
    public Label proxyStatusLabel;
    protected Shell shlGemini;
    /* access modifiers changed from: private */
    public Table table;

    public static void main(String[] args) {
        try {
            new Main().open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            new Main().open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setProxy() {
        boolean enable = true;
        try {
            JSONObject proxyEntity = shellManager.findProxy("default");
            if (proxyEntity.getInt("status") != Constants.PROXY_ENABLE) {
                enable = false;
            }
            if (enable) {
                String ip = proxyEntity.getString("ip");
                int port = proxyEntity.getInt("port");
                proxyUserName = proxyEntity.getString("username");
                proxyPassword = proxyEntity.getString("password");
                currentProxy = new Proxy(Type.HTTP, new InetSocketAddress(ip, port));
                this.proxyStatusLabel.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fillShells() throws Exception {
        this.table.removeAll();
        JSONArray shellList = shellManager.listShell();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Object o : shellList) {
            JSONObject shellObj = (JSONObject) o;
            String url = shellObj.getString("url");
            String ip = shellObj.getString("ip");
            String password = shellObj.getString("password");
            String type = shellObj.getString("type");
            String os = shellObj.getString("os");
            String comment = shellObj.getString("comment");
            String addTime = df.format(new Timestamp(shellObj.getLong("addtime")));
            String format = df.format(new Timestamp(shellObj.getLong("updatetime")));
            String format2 = df.format(new Timestamp(shellObj.getLong("accesstime")));
            TableItem item = new TableItem(this.table, 0);
            item.setText(new String[]{url, ip, password, type, os, comment, addTime});
            item.setData("id", Integer.valueOf(shellObj.getInt("id")));
        }
        int n = this.table.getColumnCount();
        for (int i = 0; i < n; i++) {
            this.table.getColumn(i).pack();
        }
    }

    public void open() {
        Display display = Display.getDefault();
        try {
            shellManager = new ShellManager();
            createContents();
            this.shlGemini.open();
            this.shlGemini.layout();
            try {
                this.shlGemini.setImage(new Image(this.table.getDisplay(), new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.jpg"))));
            } catch (Exception e) {
                e.printStackTrace();
            }
            while (!this.shlGemini.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void openShellWindow(int shellID) throws Exception {
        JSONObject shellEntity = shellManager.findShell(shellID);
        MainShell m = new MainShell(Display.getDefault(), shellEntity);
        m.setText(shellEntity.getString("url") + "    冰蝎 " + Constants.VERSION);
        m.setImage(new Image(this.table.getDisplay(), new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.jpg"))));
        m.open();
    }

    /* access modifiers changed from: protected */
    public void createContents() {
        this.shlGemini = new Shell();
        this.shlGemini.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent arg0) {
                Display.getDefault().dispose();
                Main.shellManager.closeConnection();
                System.exit(0);
            }
        });
        this.shlGemini.setSize(936, 565);
        this.shlGemini.setText(String.format("冰蝎 %s 动态二进制加密Web远程管理客户端【t00ls专版 www.t00ls.net】", Constants.VERSION));
        this.shlGemini.setLayout(new GridLayout(1, false));
        ToolBar toolBar = new ToolBar(this.shlGemini, 8519680);
        ToolItem toolItem = new ToolItem(toolBar, 8);
        toolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                new ProxyForm(Display.getDefault(), Main.this.proxyStatusLabel).open();
            }
        });
        toolItem.setText("设置代理");
        this.table = new Table(this.shlGemini, 67584);
        this.table.setLayoutData(new GridData(4, 4, true, true, 1, 1));
        this.table.setHeaderVisible(true);
        this.table.setLinesVisible(true);
        this.table.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                if (e.button == 3) {
                    Menu menu = new Menu(Main.this.table);
                    Main.this.table.setMenu(menu);
                    MenuItem addItem = new MenuItem(menu, 8);
                    addItem.setText("新增");
                    addItem.addListener(13, new Listener() {
                        public void handleEvent(Event arg0) {
                            new ShellForm(Display.getDefault(), -1, Main.this).open();
                        }
                    });
                    MenuItem refreshItem = new MenuItem(menu, 8);
                    refreshItem.setText("刷新");
                    refreshItem.addListener(13, new Listener() {
                        public void handleEvent(Event arg0) {
                            try {
                                Main.this.fillShells();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    if (Main.this.table.getSelection().length != 0) {
                        MenuItem openItem = new MenuItem(menu, 8);
                        openItem.setText("打开");
                        openItem.addListener(13, new Listener() {
                            public void handleEvent(Event arg0) {
                                try {
                                    Main.this.openShellWindow(((Integer) Main.this.table.getSelection()[0].getData("id")).intValue());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        MenuItem copyItem = new MenuItem(menu, 8);
                        copyItem.setText("拷贝");
                        copyItem.addListener(13, new Listener() {
                            public void handleEvent(Event arg0) {
                                try {
                                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(Main.this.table.getSelection()[0].getText(0)), null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        MenuItem editItem = new MenuItem(menu, 8);
                        editItem.setText("编辑");
                        editItem.addListener(13, new Listener() {
                            public void handleEvent(Event arg0) {
                                new ShellForm(Display.getDefault(), ((Integer) Main.this.table.getSelection()[0].getData("id")).intValue(), Main.this).open();
                            }
                        });
                        MenuItem deleteItem = new MenuItem(menu, 8);
                        deleteItem.setText("删除");
                        deleteItem.addListener(13, new Listener() {
                            public void handleEvent(Event arg0) {
                                MessageBox dialog = new MessageBox(Main.this.shlGemini, 196);
                                dialog.setText("确认");
                                dialog.setMessage("确定删除？");
                                if (dialog.open() != 128) {
                                    try {
                                        Main.shellManager.deleteShell(((Integer) Main.this.table.getSelection()[0].getData("id")).intValue());
                                        Main.this.fillShells();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                }
            }

            public void mouseDoubleClick(MouseEvent e) {
                try {
                    Main.this.openShellWindow(((Integer) Main.this.table.getSelection()[0].getData("id")).intValue());
                } catch (Exception e2) {
                }
            }
        });
        String[] tableHeader = {"URL", "IP", "访问密码", "脚本类型", "OS类型", "备注", "添加时间"};
        for (String text : tableHeader) {
            TableColumn tableColumn = new TableColumn(this.table, 0);
            tableColumn.setText(text);
        }
        Group group = new Group(this.shlGemini, 0);
        group.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        group.setLayout(new GridLayout(7, false));
        Label label_1 = new Label(group, 0);
        label_1.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        label_1.setText("请勿用于非法用途");
        this.proxyStatusLabel = new Label(group, 0);
        this.proxyStatusLabel.setVisible(false);
        this.proxyStatusLabel.setText("代理生效中");
        Label label = new Label(group, 2);
        GridData gd_label = new GridData(16384, 16777216, false, false, 1, 1);
        gd_label.heightHint = 20;
        label.setLayoutData(gd_label);
        Label label2 = new Label(group, 0);
        label2.setText(String.format("冰蝎 %s", Constants.VERSION));
        Label label_3 = new Label(group, 2);
        GridData gd_label_3 = new GridData(16384, 16777216, false, false, 1, 1);
        gd_label_3.heightHint = 20;
        label_3.setLayoutData(gd_label_3);
        Label label3 = new Label(group, 0);
        label3.setText("By rebeyond");
        new Label(group, 0);
        try {
            fillShells();
            setProxy();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
