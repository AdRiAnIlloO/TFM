package jbui.controller;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
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
	private void initialize()
	{
		JBUI.sInstance.mMainController = this;
		mDrawingCanvas.widthProperty().bind(mCanvasFixesPane.widthProperty());

		// Prompt earlier the general paths controller setup window, for convenience
		showConfirmationAlert(GeneralPathsAlert.class);
	}

	@FXML
	private void onExitBtnClick(ActionEvent event)
	{
		Platform.exit();
	}

	@FXML
	private void onMaudePathsBtnClick(ActionEvent event) throws IOException
	{
		showConfirmationAlert(GeneralPathsAlert.class);
	}

	@FXML
	private void onNewAttackMenuClick(ActionEvent event) throws IOException
	{
		showConfirmationAlert(ProtocolPathAlert.class);
	}

	private <T extends LoadablesAlert<S>, S extends LoadablesController> void showConfirmationAlert(Class<T> type)
	{
		try
		{
			type.newInstance().showAndWaitAndHandle();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
