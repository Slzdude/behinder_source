package net.rebeyond.behinder.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import org.json.JSONObject;

import java.util.List;

public class ParallelViewController {
    @FXML
    private MenuItem addHostBtn;
    private ShellService currentShellService;
    @FXML
    private MenuItem doScanBtn;
    private ContextMenu hostContextMenu;
    @FXML
    private GridPane hostDetailGridPane;
    @FXML
    private FlowPane hostFlowPane;
    @FXML
    private GridPane hostListGridPane;
    @FXML
    private RadioButton hostViewRadio;
    @FXML
    private Button returnListBtn;
    private ContextMenu serviceContextMenu;
    @FXML
    private FlowPane serviceDetailFlowPane;
    @FXML
    private RadioButton serviceViewRadio;
    private JSONObject shellEntity;
    private ShellManager shellManager;
    private Label statusLabel;
    private List<Thread> workList;
}
