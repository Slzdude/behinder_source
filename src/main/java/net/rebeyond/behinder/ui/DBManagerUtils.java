//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.rebeyond.behinder.ui;

import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.utils.Utils;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBManagerUtils {
    private ShellService currentShellService;
    private Label statusLabel;
    private Text sqlTxt;

    public DBManagerUtils(ShellService shellService, Label statusLabel, Text sqlTxt) {
        this.currentShellService = shellService;
        this.statusLabel = statusLabel;
        this.sqlTxt = sqlTxt;
    }

    private void loadDriver(final String type, String scheme) throws Exception {
        String driverPath = "net/rebeyond/behinder/resource/driver/";
        Display.getDefault().syncExec(() -> {
            if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                DBManagerUtils.this.statusLabel.setText("正在上传数据库驱动……");
            }
        });
        String os = this.currentShellService.shellEntity.getString("os").toLowerCase();
        String remoteDir = os.contains("windows") ? "c:/windows/temp/" : "/tmp/";
        String libName = null;
        if (type.equals("jsp")) {
            if (scheme.equals("sqlserver")) {
                libName = "sqljdbc41.jar";
            } else if (scheme.equals("mysql")) {
                libName = "mysql-connector-java-5.1.36.jar";
            } else if (scheme.equals("oracle")) {
                libName = "ojdbc5.jar";
            }
        } else if (type.equals("aspx")) {
            if (scheme.equals("mysql")) {
                libName = "mysql.data.dll";
            } else if (scheme.equals("oracle")) {
                libName = "Oracle.ManagedDataAccess.dll";
            }
        }

        byte[] driverFileContent = Utils.getResourceData(driverPath + libName);
        String remotePath = remoteDir + libName;
        this.currentShellService.uploadFile(remotePath, driverFileContent, true);
        Display.getDefault().syncExec(() -> {
            if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                DBManagerUtils.this.statusLabel.setText("驱动上传成功，正在加载驱动……");
            }
        });
        JSONObject loadRes = this.currentShellService.loadJar(remotePath);
        if (loadRes.getString("status").equals("fail")) {
            throw new Exception("驱动加载失败:" + loadRes.getString("msg"));
        } else {
            Display.getDefault().syncExec(() -> {
                if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                    if (type.equals("jsp")) {
                        DBManagerUtils.this.statusLabel.setText("驱动加载成功，请再次点击“连接”。");
                    }

                    DBManagerUtils.this.statusLabel.setText("驱动加载成功。");
                }
            });
        }
    }

    private String executeSQL(Table table, Map<String, String> connParams, final String sql) throws Exception {
        Display.getDefault().syncExec(() -> {
            if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                DBManagerUtils.this.sqlTxt.setText(sql);
                DBManagerUtils.this.statusLabel.setText("正在查询，请稍后……");
            }
        });
        String type = connParams.get("type");
        String host = connParams.get("host");
        String port = connParams.get("port");
        String user = connParams.get("user");
        String pass = connParams.get("pass");
        String database = connParams.get("database");
        JSONObject resultObj = this.currentShellService.execSQL(type, host, port, user, pass, database, sql);
        final String status = resultObj.getString("status");
        final String msg = resultObj.getString("msg");
        Display.getDefault().syncExec(() -> {
            if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                if (status.equals("success")) {
                    DBManagerUtils.this.statusLabel.setText("查询完成。");
                } else if (status.equals("fail") && !msg.equals("NoDriver")) {
                    DBManagerUtils.this.statusLabel.setText("查询失败:" + msg);
                }

            }
        });
        return msg;
    }

    public void querySQL(String url, Tree tree, final Table table, final String sql) {
        try {
            final Map<String, String> connParams = parseConnURI(url);
            (new Thread() {
                public void run() {
                    try {
                        final String resultText = DBManagerUtils.this.executeSQL(table, connParams, sql);
                        Display.getDefault().syncExec(() -> {
                            if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                                try {
                                    DBManagerUtils.this.fillTable(table, resultText);
                                } catch (Exception var2) {
                                    DBManagerUtils.this.statusLabel.setText(var2.getMessage());
                                }

                            }
                        });
                    } catch (final Exception var2) {
                        var2.printStackTrace();
                        Display.getDefault().syncExec(() -> {
                            if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                                if (var2.getMessage() != null) {
                                    DBManagerUtils.this.statusLabel.setText(var2.getMessage());
                                }

                            }
                        });
                    }

                }
            }).start();
        } catch (Exception var6) {
            this.statusLabel.setText(var6.getMessage());
        }

    }

    private static Map<String, String> parseConnURI(String url) throws Exception {
        Map<String, String> connParams = new HashMap<>();
        URI connUrl = new URI(url);
        String type = connUrl.getScheme();
        String host = connUrl.getHost();
        String port = String.valueOf(connUrl.getPort());
        String authority = connUrl.getUserInfo();
        String user = authority.substring(0, authority.indexOf(":"));
        String pass = authority.substring(authority.indexOf(":") + 1);
        String database = connUrl.getPath().replaceFirst("/", "");
        String coding = "UTF-8";
        if (connUrl.getQuery() != null && connUrl.getQuery().contains("coding=")) {
            coding = connUrl.getQuery();
            Pattern p = Pattern.compile("([a-zA-Z]*)=([a-zA-Z0-9\\-]*)");
            Matcher m = p.matcher(connUrl.getQuery());

            while (m.find()) {
                String key = m.group(1).toLowerCase();
                if (key.equals("coding")) {
                    coding = m.group(2).trim();
                }
            }
        }

        connParams.put("type", type);
        connParams.put("host", host);
        connParams.put("port", port);
        connParams.put("user", user);
        connParams.put("pass", pass);
        connParams.put("database", database);
        connParams.put("coding", coding);
        return connParams;
    }

    public void showDatabases(String url, final Tree tree, final Table table) throws Exception {
        tree.removeAll();
        final String shellType = this.currentShellService.shellEntity.getString("type");
        final Map<String, String> connParams = parseConnURI(url);
        String databaseType = connParams.get("type").toLowerCase();
        final String sql;
        switch (databaseType) {
            case "mysql":
                sql = "show databases";
                break;
            case "sqlserver":
                sql = "SELECT name FROM  master..sysdatabases";
                break;
            case "oracle":
                sql = "select sys_context('userenv','db_name') as db_name from dual";
                break;
            default:
                throw new Exception("Unsupported database type: " + databaseType);
        }

        (new Thread() {
            public void run() {
                try {
                    if (shellType.equals("aspx")) {
                        DBManagerUtils.this.loadDriver("aspx", "mysql");
                        DBManagerUtils.this.loadDriver("aspx", "oracle");
                    }

                    final String resultText = DBManagerUtils.this.executeSQL(table, connParams, sql);
                    if (resultText.equals("NoDriver")) {
                        DBManagerUtils.this.loadDriver(shellType, connParams.get("type"));
                        return;
                    }

                    Display.getDefault().syncExec(() -> {
                        if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                            try {
                                DBManagerUtils.this.fillTable(table, resultText);
                                JSONArray result = new JSONArray(resultText);
                                int databaseNums = result.length() - 1;

                                for (int i = 1; i <= databaseNums; ++i) {
                                    JSONArray row = result.getJSONArray(i);
                                    TreeItem t = new TreeItem(tree, 0);
                                    t.setData("type", "database");
                                    t.setImage(new Image(table.getDisplay(), new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/database.png"))));
                                    t.setText(row.get(0).toString());
                                }
                            } catch (Exception var6) {
                                DBManagerUtils.this.statusLabel.setText(var6.getMessage());
                            }

                        }
                    });
                } catch (final Exception var2) {
                    if (var2.getMessage() != null) {
                        Display.getDefault().syncExec(() -> {
                            if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                                DBManagerUtils.this.statusLabel.setText(var2.getMessage());
                            }
                        });
                    }
                }

            }
        }).start();
    }

    public void showTables(String url, final TreeItem currentNode, final Table table) {
        try {
            currentNode.removeAll();
            String databaseName = currentNode.getText();
            final Map<String, String> connParams = parseConnURI(url);
            final String sql;
            String databaseType = connParams.get("type");
            switch (databaseType) {
                case "mysql":
                    sql = String.format("select table_name,a.* from information_schema.tables as a where table_schema='%s' and table_type='base table'", databaseName);
                    break;
                case "sqlserver":
                    sql = String.format("select name,* from %s..sysobjects  where xtype='U'", databaseName);
                    break;
                case "oracle":
                    sql = "select table_name,num_rows from user_tables";
                    break;
                default:
                    throw new Exception("Unsupported database type: " + databaseType);
            }

            (new Thread() {
                public void run() {
                    try {
                        final String resultText = DBManagerUtils.this.executeSQL(table, connParams, sql);
                        Display.getDefault().syncExec(() -> {
                            if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                                try {
                                    DBManagerUtils.this.fillTable(table, resultText);
                                    JSONArray result = new JSONArray(resultText);
                                    int tableNums = result.length() - 1;

                                    for (int i = 1; i <= tableNums; ++i) {
                                        JSONArray row = result.getJSONArray(i);
                                        TreeItem t = new TreeItem(currentNode, 0);
                                        t.setData("type", "table");
                                        t.setImage(new Image(table.getDisplay(), new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/database_table.png"))));
                                        t.setText(row.get(0).toString());
                                    }

                                    currentNode.setExpanded(true);
                                } catch (Exception var6) {
                                    var6.printStackTrace();
                                    if (var6.getMessage() != null) {
                                        DBManagerUtils.this.statusLabel.setText(var6.getMessage());
                                    }
                                }

                            }
                        });
                    } catch (Exception var2) {
                        DBManagerUtils.this.statusLabel.setText(var2.getMessage());
                    }

                }
            }).start();
        } catch (Exception var9) {
            this.statusLabel.setText(var9.getMessage());
        }

    }

    public void showColumns(String url, TreeItem currentNode, Table table) {
        try {
            currentNode.removeAll();
            String tableName = currentNode.getText();
            String databaseName = currentNode.getParentItem().getText();
            Map<String, String> connParams = parseConnURI(url);
            String sql = null;
            String databaseType = connParams.get("type");
            if (databaseType.equals("mysql")) {
                sql = String.format("select COLUMN_NAME,a.* from information_schema.columns as a where table_schema='%s' and table_name='%s'", databaseName, tableName);
            } else if (databaseType.equals("sqlserver")) {
                sql = String.format("SELECT Name,* FROM %s..SysColumns WHERE id=Object_Id('%s')", databaseName, tableName);
            } else if (databaseType.equals("oracle")) {
                sql = String.format("select COLUMN_NAME,a.* from user_tab_columns a where Table_Name='%s' ", tableName);
            }

            String resultText = this.executeSQL(table, connParams, sql);
            this.fillTable(table, resultText);
            JSONArray result = new JSONArray(resultText);
            int tableNums = result.length() - 1;

            for (int i = 1; i <= tableNums; ++i) {
                JSONArray row = result.getJSONArray(i);
                TreeItem t = new TreeItem(currentNode, 0);
                t.setData("type", "column");
                t.setImage(new Image(table.getDisplay(), new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/database_column.png"))));
                t.setText(row.get(0).toString());
            }
        } catch (Exception var15) {
            this.statusLabel.setText(var15.getMessage());
        }

    }

    private void fillTable(Table table, String resultText) throws Exception {
        table.removeAll();
        TableColumn[] var6;
        int rows = (var6 = table.getColumns()).length;

        for (int var4 = 0; var4 < rows; ++var4) {
            TableColumn c = var6[var4];
            c.dispose();
        }

        JSONArray result;
        try {
            result = new JSONArray(resultText);
        } catch (Exception var14) {
            throw new Exception(resultText);
        }

        if (result.get(0).getClass().toString().equals("class org.json.JSONArray")) {
            JSONArray fieldArray = result.getJSONArray(0);
            rows = result.length() - 1;
            int cols = fieldArray.length();

            for (Object field : fieldArray) {
                String fieldName = ((JSONObject) field).get("name").toString();
                TableColumn tableColumn = new TableColumn(table, 131072);
                tableColumn.setText(fieldName);
                tableColumn.setMoveable(true);
            }

            table.setHeaderVisible(true);
            table.setLinesVisible(true);

            int i;
            for (i = 1; i <= rows; ++i) {
                JSONArray row = result.getJSONArray(i);
                TableItem item = new TableItem(table, 0);
                String[] rowData = new String[row.length()];
                List<String> rowList = new ArrayList<>();

                Object o;
                for (Iterator var13 = row.toList().iterator(); var13.hasNext(); rowList.add(o.toString())) {
                    o = var13.next();
                    if (o == null) {
                        o = "null";
                    }
                }

                rowList.toArray(rowData);
                item.setText(rowData);
            }

            table.setRedraw(false);

            for (i = 0; i < cols; ++i) {
                table.getColumn(i).pack();
            }

            table.setRedraw(true);
            this.showTableContextMenu(table);
        }
    }

    private void showTableContextMenu(final Table table) {
        final TableCursor cursor = new TableCursor(table, 0);
        Menu menu = new Menu(cursor);
        MenuItem item = new MenuItem(menu, 8);
        item.setText("复制单元格");
        cursor.setMenu(menu);
        item.addListener(13, event -> {
            String cellContent = cursor.getRow().getText(cursor.getColumn());
            Utils.setClipboardString(cellContent);
        });
        item = new MenuItem(menu, 8);
        item.setText("复制整行");
        cursor.setMenu(menu);
        item.addListener(13, event -> {
            String lineContent = "";

            for (int i = 0; i < table.getColumnCount(); ++i) {
                lineContent = lineContent + cursor.getRow().getText(i) + "|";
            }

            Utils.setClipboardString(lineContent);
        });
        item = new MenuItem(menu, 8);
        item.setText("导出全部查询结果");
        cursor.setMenu(menu);
        item.addListener(13, event -> {
            FileDialog filedlg = new FileDialog(table.getShell(), 4096);
            filedlg.setText("请选择保存路径");
            filedlg.setFilterPath(".");
            filedlg.setFileName("query_export.csv");
            final String selected = filedlg.open();
            if (selected != null && !selected.equals("")) {
                final StringBuilder sb = new StringBuilder();

                for (int ix = 0; ix < table.getColumnCount(); ++ix) {
                    sb.append(table.getColumn(ix).getText() + ",");
                }

                sb.append("\n");
                TableItem[] var8;
                int var7 = (var8 = table.getItems()).length;

                for (int var6 = 0; var6 < var7; ++var6) {
                    TableItem item1 = var8[var6];

                    for (int i = 0; i < table.getColumnCount(); ++i) {
                        sb.append(item1.getText(i) + ",");
                    }

                    sb.append("\n");
                }

                DBManagerUtils.this.statusLabel.setText("正在写入文件……" + selected);
                (new Thread() {
                    public void run() {
                        try {
                            FileOutputStream fso = new FileOutputStream(selected);
                            fso.write(sb.toString().getBytes());
                            fso.flush();
                            fso.close();
                            Display.getDefault().syncExec(() -> {
                                if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                                    DBManagerUtils.this.statusLabel.setText("导出完成，文件已保存至" + selected);
                                }
                            });
                        } catch (final Exception var2) {
                            var2.printStackTrace();
                            if (var2.getMessage() != null) {
                                Display.getDefault().syncExec(() -> {
                                    if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                                        DBManagerUtils.this.statusLabel.setText(var2.getMessage());
                                    }
                                });
                            }
                        }

                    }
                }).start();
            }
        });
    }

    public void showContextMenu(MouseEvent e, String connUrl, final Tree dataTree, final Table dataTable) throws Exception {
        if (e.button == 3) {
            final TreeItem currentNode = dataTree.getSelection()[0];
            if (currentNode.getData("type").equals("table")) {
                final Map<String, String> connParams = parseConnURI(connUrl);
                final String databaseType = connParams.get("type");
                Menu menu = new Menu(dataTree);
                dataTree.setMenu(menu);
                MenuItem openItem = new MenuItem(menu, 8);
                openItem.setText("查询前10条");
                openItem.addListener(13, arg0 -> {
                    final String tableName = currentNode.getText();
                    final String dataBaseName = currentNode.getParentItem().getText();
                    (new Thread() {
                        public void run() {
                            try {
                                String sql = null;
                                if (databaseType.equals("mysql")) {
                                    sql = String.format("select * from %s.%s limit 10", dataBaseName, tableName);
                                } else if (databaseType.equals("sqlserver")) {
                                    sql = String.format("select top 10 * from %s..%s", dataBaseName, tableName);
                                } else if (databaseType.equals("oracle")) {
                                    sql = String.format("select * from %s where rownum<=10", tableName);
                                }

                                final String resultText = DBManagerUtils.this.executeSQL(dataTable, connParams, sql);
                                Display.getDefault().syncExec(() -> {
                                    if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                                        try {
                                            DBManagerUtils.this.fillTable(dataTable, resultText);
                                        } catch (Exception var2) {
                                            DBManagerUtils.this.statusLabel.setText(var2.getMessage());
                                        }

                                    }
                                });
                            } catch (final Exception var3) {
                                if (var3.getMessage() != null) {
                                    Display.getDefault().syncExec(() -> {
                                        if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                                            DBManagerUtils.this.statusLabel.setText(var3.getMessage());
                                        }
                                    });
                                }
                            }

                        }
                    }).start();
                });
                MenuItem openAllItem = new MenuItem(menu, 8);
                openAllItem.setText("查询全部");
                openAllItem.addListener(13, arg0 -> {
                    final String tableName = currentNode.getText();
                    final String dataBaseName = currentNode.getParentItem().getText();
                    MessageBox dialog = new MessageBox(dataTree.getShell(), 196);
                    dialog.setText("确认");
                    dialog.setMessage("查询所有记录可能耗时较长，确定查询所有记录？");
                    if (dialog.open() != 128) {
                        (new Thread() {
                            public void run() {
                                try {
                                    String sql = null;
                                    if (databaseType.equals("mysql")) {
                                        sql = String.format("select * from %s.%s", dataBaseName, tableName);
                                    } else if (databaseType.equals("sqlserver")) {
                                        sql = String.format("select * from %s..%s", dataBaseName, tableName);
                                    } else if (databaseType.equals("oracle")) {
                                        sql = String.format("select * from %s", tableName);
                                    }

                                    final String resultText = DBManagerUtils.this.executeSQL(dataTable, connParams, sql);
                                    Display.getDefault().syncExec(() -> {
                                        if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                                            try {
                                                DBManagerUtils.this.fillTable(dataTable, resultText);
                                            } catch (Exception var2) {
                                                DBManagerUtils.this.statusLabel.setText(var2.getMessage());
                                            }

                                        }
                                    });
                                } catch (final Exception var3) {
                                    if (var3.getMessage() != null) {
                                        Display.getDefault().syncExec(() -> {
                                            if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                                                DBManagerUtils.this.statusLabel.setText(var3.getMessage());
                                            }
                                        });
                                    }
                                }

                            }
                        }).start();
                    }
                });
                MenuItem exportItem = new MenuItem(menu, 8);
                exportItem.setText("导出当前表");
                exportItem.addListener(13, arg0 -> {
                    final String tableName = currentNode.getText();
                    final String dataBaseName = currentNode.getParentItem().getText();
                    FileDialog filedlg = new FileDialog(dataTree.getShell(), 4096);
                    filedlg.setText("请选择保存路径");
                    filedlg.setFilterPath(".");
                    filedlg.setFileName("export_table.csv");
                    final String selected = filedlg.open();
                    if (selected != null && !selected.equals("")) {
                        (new Thread() {
                            public void run() {
                                try {
                                    String sql = null;
                                    if (databaseType.equals("mysql")) {
                                        sql = String.format("select * from %s.%s", dataBaseName, tableName);
                                    } else if (databaseType.equals("sqlserver")) {
                                        sql = String.format("select * from %s..%s", dataBaseName, tableName);
                                    } else if (databaseType.equals("oracle")) {
                                        sql = String.format("select * from %s", tableName);
                                    }

                                    String resultText = DBManagerUtils.this.executeSQL(dataTable, connParams, sql);
                                    StringBuilder rows = new StringBuilder();
                                    JSONArray arr = new JSONArray(resultText);
                                    String colsLine = "";
                                    JSONArray cols = arr.getJSONArray(0);

                                    int i;
                                    for (i = 0; i < cols.length(); ++i) {
                                        JSONObject colObj = cols.getJSONObject(i);
                                        colsLine = colsLine + colObj.getString("name") + ",";
                                    }

                                    rows.append(colsLine + "\n");

                                    for (i = 1; i < arr.length(); ++i) {
                                        JSONArray cells = arr.getJSONArray(i);

                                        for (int j = 0; j < cells.length(); ++j) {
                                            rows.append(cells.get(j) + ",");
                                        }

                                        rows.append("\n");
                                    }

                                    FileOutputStream fso = new FileOutputStream(selected);
                                    fso.write(rows.toString().getBytes());
                                    fso.flush();
                                    fso.close();
                                    Display.getDefault().syncExec(() -> {
                                        if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                                            DBManagerUtils.this.statusLabel.setText("导出完成，文件已保存至" + selected);
                                        }
                                    });
                                } catch (final Exception var10) {
                                    var10.printStackTrace();
                                    if (var10.getMessage() != null) {
                                        Display.getDefault().syncExec(() -> {
                                            if (!DBManagerUtils.this.statusLabel.isDisposed()) {
                                                DBManagerUtils.this.statusLabel.setText(var10.getMessage());
                                            }
                                        });
                                    }
                                }

                            }
                        }).start();
                    }
                });
            }
        }

    }
}
