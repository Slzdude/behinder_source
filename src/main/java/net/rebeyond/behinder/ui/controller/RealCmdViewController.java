package net.rebeyond.behinder.ui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class RealCmdViewController {
    Map basicInfoMap;
    private ShellManager shellManager;
    @FXML
    private TextArea realCmdTextArea;
    @FXML
    private TextField shellPathText;
    @FXML
    private Button realCmdBtn;
    private ShellService currentShellService;
    private JSONObject shellEntity;
    private List<Thread> workList;
    private Label statusLabel;
    private int running;
    private int currentPos;

    public RealCmdViewController() {
    }

    public void init(ShellService shellService, List<Thread> workList, Label statusLabel, Map basicInfoMap) {
        this.currentShellService = shellService;
        this.shellEntity = shellService.getShellEntity();
        this.basicInfoMap = basicInfoMap;
        this.workList = workList;
        this.statusLabel = statusLabel;
        this.initRealCmdView();
    }

    private void initRealCmdView() {
        String osInfo = (String) this.basicInfoMap.get("osInfo");
        if (!osInfo.contains("windows") && !osInfo.contains("winnt")) {
            this.shellPathText.setText("/bin/bash");
        } else {
            this.shellPathText.setText("cmd.exe");
        }

        this.realCmdBtn.setOnAction((event) -> {
            if (this.realCmdBtn.getText().equals("启动")) {
                this.createRealCmd();
            } else {
                this.stopRealCmd();
            }

        });
    }

    @FXML
    private void createRealCmd() {
        this.statusLabel.setText("正在启动虚拟终端……");
        Runnable runner = () -> {
            try {
                final String bashPath = this.shellPathText.getText();
                (new Thread() {
                    public void run() {
                        try {
                            RealCmdViewController.this.currentShellService.createRealCMD(bashPath);
                        } catch (Exception var2) {
                            var2.printStackTrace();
                        }

                    }
                }).start();
                Thread.sleep(1000L);
                JSONObject resultObj = this.currentShellService.readRealCMD();

                while (resultObj.getString("status").equals("success") && resultObj.getString("msg").equals("")) {
                    resultObj = this.currentShellService.readRealCMD();
                    Thread.sleep(1000L);
                }

                String status = resultObj.getString("status");
                String msg = resultObj.getString("msg");
                Platform.runLater(() -> {
                    if (status.equals("success")) {
                        this.realCmdTextArea.appendText(msg);
                        this.statusLabel.setText("虚拟终端启动完成。");
                        this.realCmdTextArea.requestFocus();
                        this.currentPos = this.realCmdTextArea.getLength();
                        this.realCmdBtn.setText("停止");
                        this.running = Constants.REALCMD_RUNNING;
                    } else {
                        this.statusLabel.setText("虚拟终端启动失败:" + msg);
                    }

                });
            } catch (Exception var5) {
                var5.printStackTrace();
                Platform.runLater(() -> {
                    this.statusLabel.setText("虚拟终端启动失败:" + var5.getMessage());
                });
            }

        };
        Thread workThread = new Thread(runner);
        this.workList.add(workThread);
        workThread.start();
    }

    private void stopRealCmd() {
        this.statusLabel.setText("正在停止虚拟终端……");
        Runnable runner = () -> {
            try {
                JSONObject resultObj = this.currentShellService.stopRealCMD();
                String status = resultObj.getString("status");
                String msg = resultObj.getString("msg");
                Platform.runLater(() -> {
                    if (status.equals("success")) {
                        this.statusLabel.setText("虚拟终端已停止。");
                        this.realCmdBtn.setText("启动");
                        this.running = Constants.REALCMD_STOPPED;
                    } else {
                        this.statusLabel.setText("虚拟终端启动失败:" + msg);
                    }

                });
            } catch (Exception var4) {
                var4.printStackTrace();
                Platform.runLater(() -> {
                    this.statusLabel.setText("操作失败:" + var4.getMessage());
                });
            }

        };
        Thread workThread = new Thread(runner);
        this.workList.add(workThread);
        workThread.start();
    }

    @FXML
    private void onRealCMDKeyPressed(KeyEvent keyEvent) {
        if (this.running != Constants.REALCMD_RUNNING) {
            this.statusLabel.setText("虚拟终端尚未启动，请先启动虚拟终端。");
        } else {
            if (this.realCmdTextArea.getCaretPosition() <= this.currentPos) {
                if (keyEvent.getCode() != KeyCode.ENTER) {
                    keyEvent.consume();
                    return;
                }

                this.realCmdTextArea.end();
            }

            if (keyEvent.getCode() == KeyCode.ENTER) {
                String cmd = this.realCmdTextArea.getText(this.currentPos, this.realCmdTextArea.getLength()).trim();
                this.statusLabel.setText("请稍后……");
                Runnable runner = () -> {
                    try {
                        String result = "";
                        if (keyEvent.getCode() == KeyCode.ENTER) {
                            keyEvent.consume();
                            this.currentShellService.writeRealCMD(cmd + "\n");
                            Thread.sleep(1000L);
                            JSONObject resultObj = this.currentShellService.readRealCMD();
                            String status = resultObj.getString("status");
                            String msg = resultObj.getString("msg");
                            result = msg;
                            if (msg.length() > 1) {
                                if (msg.startsWith(cmd)) {
                                    result = msg.substring(cmd.length());
                                }

                                result = result.startsWith("\n") ? result : "\n" + result;
                                result = result.startsWith("\n") ? result.substring(1) : result;
                                String finalResult = result;
                                Platform.runLater(() -> {
                                    this.realCmdTextArea.appendText(finalResult);
                                    this.currentPos = this.realCmdTextArea.getLength();
                                });
                                Thread.sleep(1000L);
                            }

                            Platform.runLater(() -> this.statusLabel.setText("完成。"));
                        }
                    } catch (Exception var8) {
                        var8.printStackTrace();
                    }

                };
                Thread workThread = new Thread(runner);
                this.workList.add(workThread);
                workThread.start();
                keyEvent.consume();
            }
        }
    }
}
