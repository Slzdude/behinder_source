package net.rebeyond.behinder.ui;

import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.utils.Constants;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CmdUtils {
    private List<String> cmdHistory = new ArrayList<>();
    public int currentPos;
    /* access modifiers changed from: private */
    public ShellService currentShellService;
    private List<String> realCmdHistory = new ArrayList<>();
    /* access modifiers changed from: private */
    public int running = Constants.REALCMD_STOPPED;
    private JSONObject shellEntity;
    /* access modifiers changed from: private */
    public Label statusLabel;

    public CmdUtils(ShellService shellService, Label statusLabel2, JSONObject shellEntity2) {
        this.currentShellService = shellService;
        this.statusLabel = statusLabel2;
        this.shellEntity = shellEntity2;
    }

    public void sendCommand(KeyEvent e, Text cmdView,MainShell main) {
        Map<String, String> basicInfo = main.basicInfoMap;
        if ((e.keyCode == 8 || e.keyCode == 16777219) && cmdView.getCaretPosition() <= this.currentPos) {
            e.doit = false;
        }
        if (e.keyCode == 16777217) {
            e.doit = false;
        }
        if (cmdView.getCaretPosition() < this.currentPos) {
            e.doit = false;
        }
        if (e.keyCode == 13) {
            String pwd = basicInfo.get("currentPath") + " >";
            try {
                String lastLine = cmdView.getText().split("\n")[cmdView.getText().split("\n").length - 1];
                JSONObject resultObj = this.currentShellService.runCmd(lastLine.substring(lastLine.indexOf(pwd) + pwd.length()));
                if (resultObj.getString("status").equals("success")) {
                    cmdView.insert("\n" + resultObj.getString("msg") + "\n");
                    cmdView.insert(pwd);
                    this.statusLabel.setText("√¸¡Ó÷¥––ÕÍ≥…");
                    this.currentPos = cmdView.getCaretPosition();
                } else {
                    cmdView.insert("\n" + resultObj.getString("msg") + "\n");
                    cmdView.insert(pwd);
                    this.statusLabel.setText("√¸¡Ó÷¥–– ß∞‹:" + resultObj.getString("msg"));
                }
                e.doit = false;
            } catch (Exception ex) {
                e.doit = false;
                ex.printStackTrace();
                this.statusLabel.setText(ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public void createRealCMD(final StyledText cmdView, final String imagePath) throws Exception {
        this.statusLabel.setText("’˝‘⁄∆Ù∂Ø–Èƒ‚÷’∂À°≠°≠");
        new Thread() {
            public void run() {
                try {
                    final String bashPath = imagePath;
                    new Thread() {
                        public void run() {
                            try {
                                CmdUtils.this.currentShellService.createRealCMD(bashPath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    Thread.sleep(1000);
                    JSONObject resultObj = CmdUtils.this.currentShellService.readRealCMD();
                    while (resultObj.getString("status").equals("success") && resultObj.getString("msg").equals("")) {
                        resultObj = CmdUtils.this.currentShellService.readRealCMD();
                    }
                    final String status = resultObj.getString("status");
                    final String msg = resultObj.getString("msg");
                    Display display = Display.getDefault();
                    final StyledText styledText = cmdView;
                    display.syncExec(new Runnable() {
                        public void run() {
                            if (!CmdUtils.this.statusLabel.isDisposed()) {
                                if (status.equals("success")) {
                                    styledText.setForeground(Display.getDefault().getSystemColor(5));
                                    styledText.append(msg);
                                    styledText.setCaretOffset(styledText.getCharCount());
                                    CmdUtils.this.currentPos = styledText.getCaretOffset();
                                    styledText.setFocus();
                                    CmdUtils.this.statusLabel.setText("–Èƒ‚÷’∂À∆Ù∂ØÕÍ≥…°£");
                                    CmdUtils.this.running = Constants.REALCMD_RUNNING;
                                    return;
                                }
                                CmdUtils.this.statusLabel.setText("–Èƒ‚÷’∂À∆Ù∂Ø ß∞‹:" + msg);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            if (!CmdUtils.this.statusLabel.isDisposed()) {
                                CmdUtils.this.statusLabel.setText("–Èƒ‚÷’∂À∆Ù∂Ø ß∞‹");
                            }
                        }
                    });
                }
            }
        }.start();
    }

    public void stopRealCMD(StyledText cmdView, String imagePath) throws Exception {
        this.statusLabel.setText("’˝‘⁄Õ£÷π–Èƒ‚÷’∂À°≠°≠");
        new Thread() {
            public void run() {
                try {
                    JSONObject resultObj = CmdUtils.this.currentShellService.stopRealCMD();
                    final String status = resultObj.getString("status");
                    final String msg = resultObj.getString("msg");
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            if (!CmdUtils.this.statusLabel.isDisposed()) {
                                if (status.equals("success")) {
                                    CmdUtils.this.statusLabel.setText("–Èƒ‚÷’∂À“—Õ£÷π°£");
                                    CmdUtils.this.running = Constants.REALCMD_STOPPED;
                                    return;
                                }
                                CmdUtils.this.statusLabel.setText("–Èƒ‚÷’∂À∆Ù∂Ø ß∞‹:" + msg);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            if (!CmdUtils.this.statusLabel.isDisposed()) {
                                CmdUtils.this.statusLabel.setText("–Èƒ‚Õ£÷π∆Ù∂Ø ß∞‹");
                            }
                        }
                    });
                }
            }
        }.start();
    }

    public void filterEvent(StyledText cmdView, KeyEvent key) {
        char keyValue = key.character;
        if ((keyValue == 8 || key.keyCode == 16777219) && cmdView.getCaretOffset() <= this.currentPos) {
            key.doit = false;
        }
        if (key.keyCode == 16777217) {
            key.doit = false;
        }
        if (cmdView.getCaretOffset() >= this.currentPos) {
            return;
        }
        if (keyValue != 13) {
            key.doit = false;
        } else {
            cmdView.setCaretOffset(cmdView.getCharCount());
        }
    }

    public void runRealCMD(StyledText cmdView, KeyEvent key) throws Exception {
        if (this.running != Constants.REALCMD_RUNNING) {
            this.statusLabel.setText("–Èƒ‚÷’∂À…–Œ¥∆Ù∂Ø£¨«Îœ»∆Ù∂Ø–Èƒ‚÷’∂À°£");
            return;
        }
        final char keyValue = key.character;
        if (keyValue == 9 || keyValue == 13) {
            final String cmd = cmdView.getText(this.currentPos, cmdView.getCaretOffset() - 1).trim();
            this.statusLabel.setText("«Î…‘∫Û°≠°≠");
            final KeyEvent keyEvent = key;
            final StyledText styledText = cmdView;
            new Thread() {
                public void run() {
                    String str = "";
                    try {
                        if (keyValue == 13) {
                            keyEvent.doit = false;
                            CmdUtils.this.currentShellService.writeRealCMD(cmd + "\n");
                            Thread.sleep(1000);
                            JSONObject resultObj = CmdUtils.this.currentShellService.readRealCMD();
                            String string = resultObj.getString("status");
                            String result = resultObj.getString("msg");
                            if (result.length() > 1) {
                                if (result.startsWith(cmd)) {
                                    result = result.substring(cmd.length());
                                }
                                if (!result.startsWith("\n")) {
                                    result = "\n" + result;
                                }
                                if (result.startsWith("\n")) {
                                    result = result.substring(1);
                                }
                                final String finalResult = result;
                                Display display = Display.getDefault();
                                display.syncExec(() -> {
                                    if (!CmdUtils.this.statusLabel.isDisposed()) {
                                        styledText.append(finalResult);
                                        styledText.setCaretOffset(styledText.getCharCount());
                                        styledText.setTopIndex(styledText.getLineCount() - 1);
                                        CmdUtils.this.currentPos = styledText.getCaretOffset();
                                        CmdUtils.this.statusLabel.setText("ÕÍ≥…°£");
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
}
