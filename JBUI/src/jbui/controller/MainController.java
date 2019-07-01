package jbui.controller;

import java.io.IOException;
import java.net.URL;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import jbui.JBUI;

public class MainController
{
	@FXML
	// Fixes auto resizing & positioning of the Canvas upon window changes
	private Pane mCanvasFixesPane;

	@FXML
	public Canvas mDrawingCanvas;

	@FXML
	public GridPane mNodesGridPane;

	@FXML
	private StackPane mTest;

	@FXML
	protected void initialize()
	{
		mDrawingCanvas.widthProperty().bind(mCanvasFixesPane.widthProperty());
	}

	@FXML
	private void onExitBtnClick(ActionEvent event)
	{
		Platform.exit();
	}

	@FXML
	private void onMaudePathsBtnClick(ActionEvent event) throws IOException
	{
		showGeneralPathsWindow();
	}

	@FXML
	private void onNewAttackMenuClick(ActionEvent event) throws IOException
	{
		showCloseHandleableDialogController("view/protocol_setup_window.fxml", "New attack setup",
				"Here you can load a protocol file and set execution options, and trigger an execution");
	}

	private void showCloseHandleableDialogController(String resourcePathName, String title, String headerText)
			throws IOException
	{
		URL url = JBUI.class.getResource(resourcePathName);
		FXMLLoader loader = new FXMLLoader(url);
		Parent contentNode = loader.load();
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.getDialogPane().setContent(contentNode);
		ICloseHandleableDialogController controller = loader.getController();
		alert.showAndWait().ifPresent((buttonType) -> controller.handleClosed(buttonType));
	}

	public void showGeneralPathsWindow() throws IOException
	{
		showCloseHandleableDialogController("view/general_paths_window.fxml", "Maude paths setup",
				"Here you can configure the basic required Maude files that any execution uses");
	}
}
