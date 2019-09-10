package net.rebeyond.behinder.ui;

import net.rebeyond.behinder.core.ShellService;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class EvalUtils {
    /* access modifiers changed from: private */
    public ShellService currentShellService;
    private StyledText sourceCodeTxt;
    /* access modifiers changed from: private */
    public Label statusLabel;

    public EvalUtils(ShellService shellService, final Label statusLabel2, StyledText sourceCodeTxt2) {
        this.currentShellService = shellService;
        this.statusLabel = statusLabel2;
        this.sourceCodeTxt = sourceCodeTxt2;
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                if (!statusLabel2.isDisposed()) {
                    EvalUtils.this.fillSourceCode();
                }
            }
        });
    }

    public static void main(String[] args) {
    }

    /* access modifiers changed from: private */
    public void fillSourceCode() {
        if (this.currentShellService.currentType.equals("jsp")) {
            return;
        }
        if (this.currentShellService.currentType.equals("aspx")) {
            this.sourceCodeTxt.setText("using System;\r\n\r\npublic class Eval {\r\n\r\n\tpublic void eval(Object obj) {\r\n\r\n\t/**用户自定义代码开始**/\t\r\n\r\n\tSystem.Web.UI.Page page = (System.Web.UI.Page)obj;\r\n\tpage.Response.Write(\"hello world\");\r\n\r\n   /**用户自定义代码结束**/\t\r\n\r\n\t}\r\n}");
        } else if (this.currentShellService.currentType.equals("php")) {
            this.sourceCodeTxt.setText("echo 'hello world';");
        }
    }

    public void execute(Button btn, final String sourceCode, final StyledText resultTxt) {
        this.statusLabel.setText("正在执行……");
        new Thread() {
            public void run() {
                try {
                    final String result = EvalUtils.this.currentShellService.eval(sourceCode);
                    Display display = Display.getDefault();
                    final StyledText styledText = resultTxt;
                    display.syncExec(new Runnable() {
                        public void run() {
                            if (!EvalUtils.this.statusLabel.isDisposed()) {
                                styledText.setText(result);
                                EvalUtils.this.statusLabel.setText("完成。");
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getMessage() != null) {
                        Display display2 = Display.getDefault();
                        final StyledText styledText2 = resultTxt;
                        display2.syncExec(new Runnable() {
                            public void run() {
                                if (!EvalUtils.this.statusLabel.isDisposed()) {
                                    EvalUtils.this.statusLabel.setText("运行失败");
                                    styledText2.setText(e.getMessage());
                                }
                            }
                        });
                    }
                }
            }
        }.start();
    }
}
