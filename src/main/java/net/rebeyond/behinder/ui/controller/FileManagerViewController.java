package net.rebeyond.behinder.ui.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.converter.DefaultStringConverter;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.utils.Base64;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileManagerViewController {
    Map<String, String> basicInfoMap;
    private ShellManager shellManager;
    @FXML
    private TreeView<String> dirTree;
    @FXML
    private ComboBox<String> currentPathCombo;
    @FXML
    private TableView<List<SimpleStringProperty>> fileListTableView;
    @FXML
    private TableColumn<List<SimpleStringProperty>, String> fileNameCol;
    @FXML
    private StackPane fileManagerStackPane;
    @FXML
    private GridPane fileListGridPane;
    @FXML
    private GridPane fileContentGridPane;
    @FXML
    private TextField filePathText;
    @FXML
    private Button openPathBtn;
    @FXML
    private ComboBox<String> charsetCombo;
    @FXML
    private TextArea fileContentTextArea;
    @FXML
    private Button saveFileContentBtn;
    @FXML
    private Button cancelFileContentBtn;
    private ShellService currentShellService;
    private List<Thread> workList;
    private Label statusLabel;

    public FileManagerViewController() {
    }

    public void init(ShellService shellService, List<Thread> workList, Label statusLabel, Map<String, String> basicInfoMap) {
        this.currentShellService = shellService;
        this.workList = workList;
        this.statusLabel = statusLabel;
        this.basicInfoMap = basicInfoMap;

        try {
            this.initFileManagerView();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    private void initFileManagerView() throws Exception {
        this.initFileListTableColumns();
        this.initCharsetCombo();
        String driveList = this.basicInfoMap.get("driveList");
        TreeItem<String> rootItem = new TreeItem<>("文件系统", new ImageView());
        rootItem.getGraphic().setUserData("base");
        Image icon = new Image(new ByteArrayInputStream(Utils.getResourceData("res/net/rebeyond/resource/drive.png")));
        String[] var4 = driveList.split(";");
        int var5 = var4.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            String drive = var4[var6];
            TreeItem<String> driveItem = new TreeItem<>(drive, new ImageView(icon));
            driveItem.getGraphic().setUserData("root");
            driveItem.setValue(drive);
            rootItem.getChildren().add(driveItem);
            this.dirTree.setRoot(rootItem);
        }

        String currentPath = this.basicInfoMap.get("currentPath");
        ObservableList<String> pathList = FXCollections.observableArrayList(currentPath);
        this.currentPathCombo.setItems(pathList);
        this.currentPathCombo.getSelectionModel().select(0);
        this.loadContextMenu();
        this.dirTree.getSelectionModel().selectedItemProperty().addListener((ChangeListener) (observable, oldValue, newValue) -> {
            TreeItem currentTreeItem = (TreeItem) newValue;
            String pathString = FileManagerViewController.this.getFullPath(currentTreeItem);
            FileManagerViewController.this.expandByPath(pathString);
        });
        this.expandByPath(currentPath);
        this.charsetCombo.setItems(FXCollections.observableArrayList("自动", "GBK", "UTF-8"));
        this.cancelFileContentBtn.setOnAction((event) -> this.switchPane("list"));
        this.saveFileContentBtn.setOnAction((event) -> {
            String filePath = this.filePathText.getText();

            try {
                this.saveFileContent(filePath);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        });
        this.fileListTableView.setEditable(true);
        this.fileNameCol.setOnEditCommit(event -> FileManagerViewController.this.rename(event.getOldValue(), event.getNewValue()));
        this.fileNameCol.setOnEditCancel(event -> FileManagerViewController.this.expandByPath(FileManagerViewController.this.currentPathCombo.getValue()));
        this.openPathBtn.setOnAction((event) -> this.expandByPath(this.currentPathCombo.getValue()));
        this.switchPane("list");
    }

    private void initCharsetCombo() {
        this.charsetCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            String filePath = this.filePathText.getText();
            String charset = newValue.equals("自动") ? null : this.charsetCombo.getValue();
            this.showFile(filePath, charset);
        });
    }

    private void uploadFile() throws Exception {
        String currentPath = this.currentPathCombo.getValue();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("请选择需要上传的文件");
        File selectdFile = fileChooser.showOpenDialog(this.fileListGridPane.getScene().getWindow());
        if (selectdFile != null) {
            String fileName = selectdFile.getName();
            byte[] fileContent = Utils.getFileData(selectdFile.getAbsolutePath());
            int bufSize = this.currentShellService.currentType.equals("aspx") ? 524288 : '됀';
            this.statusLabel.setText("正在上传……");
            Runnable runner = () -> {
                try {
                    if (fileContent.length < bufSize) {
                        JSONObject resultObj = this.currentShellService.uploadFile(currentPath + fileName, fileContent);
                        String status = resultObj.getString("status");
                        String msg = resultObj.getString("msg");
                        if (status.equals("fail")) {
                            Platform.runLater(() -> this.statusLabel.setText("文件上传失败:" + msg));
                            return;
                        }
                    } else {
                        List<byte[]> blocks = Utils.splitBytes(fileContent, bufSize);

                        for (int i = 0; i < blocks.size(); ++i) {
                            JSONObject resultObjx;
                            String statusx;
                            String msgx;
                            if (i == 0) {
                                resultObjx = this.currentShellService.uploadFile(currentPath + fileName, blocks.get(i));
                                statusx = resultObjx.getString("status");
                                msgx = resultObjx.getString("msg");
                                if (statusx.equals("fail")) {
                                    Platform.runLater(() -> this.statusLabel.setText("文件上传失败:" + msgx));
                                    return;
                                }
                            } else {
                                resultObjx = this.currentShellService.appendFile(currentPath + fileName, blocks.get(i));
                                statusx = resultObjx.getString("status");
                                msgx = resultObjx.getString("msg");
                                int finalI = i;
                                Platform.runLater(() -> {
                                    if (statusx.equals("fail")) {
                                        this.statusLabel.setText("文件上传失败:" + msgx);
                                    } else {
                                        this.statusLabel.setText(String.format("正在上传……%skb/%skb", bufSize * finalI / 1024, fileContent.length / 1024));
                                    }
                                });
                                if (statusx.equals("fail")) {
                                    return;
                                }
                            }
                        }
                    }

                    Platform.runLater(() -> {
                        this.statusLabel.setText("上传完成");
                        this.expandByPath(currentPath);
                    });
                } catch (Exception var11) {
                    Platform.runLater(() -> this.statusLabel.setText("操作失败:" + var11.getMessage()));
                }

            };
            Thread workThread = new Thread(runner);
            this.workList.add(workThread);
            workThread.start();
        }
    }

    private void rename(String oldFileName, String newFileName) {
        String currentDir = this.currentPathCombo.getValue();
        String oldFullName = currentDir + oldFileName;
        String newFullName = currentDir + newFileName;
        Runnable runner = () -> {
            try {
                JSONObject resultObj = this.currentShellService.renameFile(oldFullName, newFullName);
                String status = resultObj.getString("status");
                String msg = resultObj.getString("msg");
                Platform.runLater(() -> {
                    this.expandByPath(this.currentPathCombo.getValue());
                    if (status.equals("fail")) {
                        this.statusLabel.setText(msg);
                    } else {
                        this.statusLabel.setText(msg);
                    }
                });
            } catch (Exception var6) {
                this.statusLabel.setText("操作失败:" + var6.getMessage());
            }

        };
        Thread workThread = new Thread(runner);
        this.workList.add(workThread);
        workThread.start();
    }

    private void saveFileContent(String pathString) throws UnsupportedEncodingException {
        String charset = null;
        if (this.charsetCombo.getSelectionModel().getSelectedIndex() > 0) {
            charset = this.charsetCombo.getValue();
        }

        byte[] fileContent = charset == null ? this.fileContentTextArea.getText().getBytes() : this.fileContentTextArea.getText().getBytes(charset);
        this.statusLabel.setText("正在保存……");
        Runnable runner = () -> {
            try {
                JSONObject resultObj = this.currentShellService.uploadFile(pathString, fileContent, true);
                String status = resultObj.getString("status");
                String msg = resultObj.getString("msg");
                Platform.runLater(() -> {
                    if (status.equals("success")) {
                        this.statusLabel.setText("保存成功。");
                    } else {
                        this.statusLabel.setText("保存失败:" + msg);
                    }

                });
            } catch (Exception var6) {
                Platform.runLater(() -> this.statusLabel.setText("操作失败:" + var6.getMessage()));
            }

        };
        Thread workThread = new Thread(runner);
        this.workList.add(workThread);
        workThread.start();
    }

    private void switchPane(String show) {
        if (show.equals("list")) {
            this.fileListGridPane.setOpacity(1.0D);
            this.fileContentGridPane.setOpacity(0.0D);
            this.fileListGridPane.toFront();
        } else if (show.equals("content")) {
            this.fileListGridPane.toBack();
            this.fileListGridPane.setOpacity(0.0D);
            this.fileContentGridPane.setOpacity(1.0D);
            this.fileContentGridPane.toFront();
        }

    }

    private String getFullPath(TreeItem currentTreeItem) {
        String fileSep = "/";
        String currentPath = currentTreeItem.getValue().toString();
        TreeItem parent = currentTreeItem;

        while (!(parent = parent.getParent()).getGraphic().getUserData().equals("base")) {
            String parentText = parent.getValue().toString();
            if (parent.getGraphic().getUserData().equals("root")) {
                currentPath = parentText + currentPath;
            } else {
                currentPath = parentText + fileSep + currentPath;
            }
        }

        if (!parent.getGraphic().getUserData().equals("directory") && !currentPath.endsWith(fileSep)) {
            currentPath = currentPath + fileSep;
        }

        return currentPath;
    }

    private void initFileListTableColumns() {
        final ObservableList<TableColumn<List<SimpleStringProperty>, ?>> tcs = this.fileListTableView.getColumns();
        ((TableColumn<List<SimpleStringProperty>, String>) tcs.get(0)).setCellValueFactory(param -> param.getValue().get(0));
        ((TableColumn<List<SimpleStringProperty>, String>) tcs.get(1)).setCellValueFactory(param -> param.getValue().get(1));
        ((TableColumn<List<SimpleStringProperty>, String>) tcs.get(2)).setCellValueFactory(param -> param.getValue().get(2));
        this.fileListTableView.setRowFactory((tv) -> {
            TableRow<List<SimpleStringProperty>> row = new TableRow<>();
            row.setOnMouseClicked((event) -> {
                event.consume();
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    String path = this.currentPathCombo.getValue();
                    String name = ((row.getItem()).get(0)).getValue();
                    String type = ((row.getItem()).get(3)).getValue();
                    if (!path.endsWith("/")) {
                        path = path + "/";
                    }

                    if (type.equals("file")) {
                        String fileName = path + name;
                        this.filePathText.setText(fileName);
                        this.showFile(fileName, null);
                        this.switchPane("content");
                    } else if (type.equals("directory")) {
                        this.expandByPath(path + name);
                    }
                }

            });
            return row;
        });
        this.fileNameCol.setCellFactory((column) -> new TextFieldTableCell<List<SimpleStringProperty>, String>(new DefaultStringConverter()) {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!(item == null | empty)) {
                    String type;

                    try {
                        type = ((StringProperty) ((List<SimpleStringProperty>) this.getTableRow().getItem()).get(3)).get();
                    } catch (Exception var7) {
                        return;
                    }

                    Image icon;
                    if (type.equals("directory")) {
                        try {
                            icon = new Image(new ByteArrayInputStream(Utils.getResourceData("res/net/rebeyond/resource/folder.png")));
                            this.setGraphic(new ImageView(icon));
                        } catch (Exception var6) {
                            var6.printStackTrace();
                        }
                    } else if (type.equals("file")) {
                        try {
                            icon = new Image(new ByteArrayInputStream(Utils.getResourceData("res/net/rebeyond/resource/file.png")));
                            this.setGraphic(new ImageView(icon));
                        } catch (Exception var5) {
                            var5.printStackTrace();
                        }
                    }

                    this.setText(item);
                }

            }
        });
    }

    private TreeItem<String> findTreeItemByPath(Path path) {
        String osInfo = this.basicInfoMap.get("osInfo");
        TreeItem<String> currentItem = null;
        List<String> pathParts = new ArrayList<String>();
        String pathString = path.toString();
        if (pathString.equals("/")) {
            pathParts.add("/");
        } else {
            pathParts.addAll(Arrays.asList(pathString.split("/|\\\\")));
            if (osInfo.contains("linux")) {
                pathParts.set(0, "/");
            } else {
                pathParts.set(0, pathParts.get(0) + "/");
            }
        }

        Image icon = null;

        try {
            icon = new Image(new ByteArrayInputStream(Utils.getResourceData("res/net/rebeyond/resource/folder.png")));
        } catch (Exception var10) {
            var10.printStackTrace();
        }

        for (Iterator<String> var7 = pathParts.iterator(); var7.hasNext(); currentItem.setExpanded(true)) {
            String childPath = var7.next();
            TreeItem<String> childItem;
            if (currentItem == null) {
                childItem = this.findTreeItem(this.dirTree.getRoot(), childPath);
                currentItem = childItem;
            } else {
                childItem = this.findTreeItem(currentItem, childPath);
                if (childItem == null) {
                    childItem = new TreeItem<>(childPath, new ImageView(icon));
                    childItem.getGraphic().setUserData("directory");
                    currentItem.getChildren().add(childItem);
                }

                currentItem = childItem;
            }
        }

        this.dirTree.getSelectionModel().select(currentItem);
        return currentItem;
    }

    private void insertTreeItems(JSONArray rows, TreeItem<String> currentTreeItem) {
        currentTreeItem.getChildren().clear();

        for (int i = 0; i < rows.length(); ++i) {
            try {
                JSONObject fileObj = rows.getJSONObject(i);
                String type = new String(Base64.decode(fileObj.getString("type")), StandardCharsets.UTF_8);
                String name = new String(Base64.decode(fileObj.getString("name")), StandardCharsets.UTF_8);
                if (!name.equals(".") && !name.equals("..") && type.equals("directory")) {
                    Image icon = new Image(new ByteArrayInputStream(Utils.getResourceData("res/net/rebeyond/resource/folder.png")));
                    TreeItem<String> treeItem = new TreeItem<>(name, new ImageView(icon));
                    treeItem.getGraphic().setUserData("directory");
                    currentTreeItem.getChildren().add(treeItem);
                }
            } catch (Exception var9) {
                var9.printStackTrace();
            }
        }

        currentTreeItem.setExpanded(true);
        this.dirTree.getSelectionModel().select(currentTreeItem);
    }

    private TreeItem<String> findTreeItem(TreeItem<String> treeItem, String text) {
        ObservableList<TreeItem<String>> childItemList = treeItem.getChildren();
        Iterator<TreeItem<String>> var4 = childItemList.iterator();

        TreeItem<String> childItem;
        do {
            if (!var4.hasNext()) {
                return null;
            }

            childItem = var4.next();
        } while (!(childItem).getValue().equals(text));

        return childItem;
    }

    private void insertFileRows(JSONArray jsonArray) {
        ObservableList<List<SimpleStringProperty>> data = FXCollections.observableArrayList();

        for (int i = 0; i < jsonArray.length(); ++i) {
            JSONObject rowObj = jsonArray.getJSONObject(i);

            try {
                String type = new String(Base64.decode(rowObj.getString("type")), StandardCharsets.UTF_8);
                String name = new String(Base64.decode(rowObj.getString("name")), StandardCharsets.UTF_8);
                String size = new String(Base64.decode(rowObj.getString("size")), StandardCharsets.UTF_8);
                String lastModified = new String(Base64.decode(rowObj.getString("lastModified")));
                List<SimpleStringProperty> row = new ArrayList<>();
                row.add(0, new SimpleStringProperty(name));
                row.add(1, new SimpleStringProperty(size));
                row.add(2, new SimpleStringProperty(lastModified));
                row.add(3, new SimpleStringProperty(type));
                data.add(row);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.fileListTableView.setItems(data);
    }

    private void expandByPath(String pathStr) {
        Path path = Paths.get(pathStr).normalize();
        String pathString = path.toString().endsWith("/") ? path.toString() : path.toString() + "/";
        TreeItem<String> currentTreeItem = this.findTreeItemByPath(path);
        this.currentPathCombo.setValue(pathString);
        this.statusLabel.setText("正在加载目录……");
        Runnable runner = () -> {
            try {
                JSONObject resultObj = this.currentShellService.listFiles(pathString);
                Platform.runLater(() -> {
                    try {
                        String status = resultObj.getString("status");
                        String msg = resultObj.getString("msg");
                        if (status.equals("fail")) {
                            this.statusLabel.setText("目录读取失败:" + msg);
                            return;
                        }

                        this.statusLabel.setText("目录加载成功");
                        msg = msg.replace("},]", "}]");
                        JSONArray objArr = new JSONArray(msg.trim());
                        this.insertFileRows(objArr);
                        this.insertTreeItems(objArr, currentTreeItem);
                    } catch (Exception var6) {
                        this.statusLabel.setText("操作失败：" + var6.getMessage());
                    }

                });
            } catch (Exception var4) {
                var4.printStackTrace();
            }

        };
        Thread workThread = new Thread(runner);
        this.workList.add(workThread);
        workThread.start();
    }

    private void showFile(String filePath, String charset) {
        Runnable runner = () -> {
            try {
                JSONObject resultObj = this.currentShellService.showFile(filePath, charset);
                String status = resultObj.getString("status");
                String msg = resultObj.getString("msg");
                Platform.runLater(() -> {
                    if (status.equals("fail")) {
                        this.statusLabel.setText("文件打开失败:" + msg);
                    } else {
                        this.fileContentTextArea.setText(msg);
                        this.switchPane("content");
                    }
                });
            } catch (Exception var6) {
                this.statusLabel.setText("操作失败:" + var6.getMessage());
                var6.printStackTrace();
            }

        };
        Thread workThread = new Thread(runner);
        this.workList.add(workThread);
        workThread.start();
    }

    private void loadContextMenu() {
        ContextMenu cm = new ContextMenu();
        MenuItem refreshBtn = new MenuItem("刷新");
        cm.getItems().add(refreshBtn);
        MenuItem openBtn = new MenuItem("打开");
        cm.getItems().add(openBtn);
        MenuItem renameBtn = new MenuItem("重命名");
        cm.getItems().add(renameBtn);
        MenuItem delBtn = new MenuItem("删除");
        cm.getItems().add(delBtn);
        cm.getItems().add(new SeparatorMenuItem());
        MenuItem downloadBtn = new MenuItem("下载");
        cm.getItems().add(downloadBtn);
        MenuItem uploadBtn = new MenuItem("上传");
        cm.getItems().add(uploadBtn);
        Menu createMenu = new Menu("新建");
        MenuItem createFileBtn = new MenuItem("文件...");
        MenuItem createDirectoryBtn = new MenuItem("文件夹");
        createMenu.getItems().add(createFileBtn);
        createMenu.getItems().add(createDirectoryBtn);
        cm.getItems().add(createMenu);
        cm.getItems().add(new SeparatorMenuItem());
        MenuItem changeTimeStampBtn = new MenuItem("修改时间戳");
        cm.getItems().add(changeTimeStampBtn);
        MenuItem cloneTimeStampBtn = new MenuItem("克隆时间戳");
        cm.getItems().add(cloneTimeStampBtn);
        this.fileListTableView.setContextMenu(cm);
        openBtn.setOnAction((event) -> {
            String type = this.fileListTableView.getSelectionModel().getSelectedItem().get(3).getValue();
            String name = this.fileListTableView.getSelectionModel().getSelectedItem().get(0).getValue();
            String pathString = this.currentPathCombo.getValue();
            pathString = Paths.get(pathString).normalize().toString();
            if (!pathString.endsWith("/")) {
                pathString = pathString + "/";
            }

            pathString = pathString + name;
            if (type.equals("directory")) {
                this.expandByPath(pathString);
            } else {
                this.filePathText.setText(pathString);
                this.showFile(pathString, null);
            }

        });
        refreshBtn.setOnAction((event) -> {
            this.statusLabel.setText("正在刷新……");
            this.expandByPath(this.currentPathCombo.getValue());
            this.statusLabel.setText("刷新完成。");
        });
        renameBtn.setOnAction((event) -> {
            int row = this.fileListTableView.getSelectionModel().getSelectedIndex();
            this.fileListTableView.edit(row, this.fileNameCol);
        });
        delBtn.setOnAction((event) -> {
            String name = this.fileListTableView.getSelectionModel().getSelectedItem().get(0).getValue();
            String fileFullPath = this.currentPathCombo.getValue() + name;
            Runnable runner = () -> {
                try {
                    JSONObject resultObj = this.currentShellService.deleteFile(fileFullPath);
                    String status = resultObj.getString("status");
                    String msg = resultObj.getString("msg");
                    Platform.runLater(() -> {
                        if (status.equals("success")) {
                            this.expandByPath(this.currentPathCombo.getValue());
                        }

                        this.statusLabel.setText(msg);
                    });
                } catch (Exception var5) {
                    Platform.runLater(() -> this.statusLabel.setText("操作失败:" + var5.getMessage()));
                }

            };
            Thread workThread = new Thread(runner);
            this.workList.add(workThread);
            workThread.start();
        });
        uploadBtn.setOnAction((event) -> {
            try {
                this.uploadFile();
            } catch (Exception var3) {
                var3.printStackTrace();
            }

        });
        downloadBtn.setOnAction((event) -> this.downloadFile());
    }

    private void downloadFile() {
        String fileName = this.fileListTableView.getSelectionModel().getSelectedItem().get(0).getValue();
        String fileFullPath = this.currentPathCombo.getValue() + fileName;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("请选择保存路径");
        fileChooser.setInitialFileName(fileName);
        File selectedFile = fileChooser.showSaveDialog(this.fileListGridPane.getScene().getWindow());
        String localFilePath = selectedFile.getAbsolutePath();
        if (selectedFile.equals("")) {
            return;
        }
        this.statusLabel.setText("正在下载" + fileFullPath + "……");
        Runnable runner = () -> {
            try {
                this.currentShellService.downloadFile(fileFullPath, localFilePath);
                String result = selectedFile.getName() + "下载完成,文件大小:" + selectedFile.length();
                Platform.runLater(() -> this.statusLabel.setText(result));
            } catch (Exception var5) {
                Platform.runLater(() -> this.statusLabel.setText("操作失败:" + var5.getMessage()));
                var5.printStackTrace();
            }

        };
        Thread workThread = new Thread(runner);
        this.workList.add(workThread);
        workThread.start();
    }
}
