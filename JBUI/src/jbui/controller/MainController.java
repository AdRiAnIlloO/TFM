package jbui.controller;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import jbui.JBUI;

public class MainController
{
	@FXML
	private Button mAnyStepBtn;

	@FXML
	// Fixes auto resizing & positioning of the Canvas upon window changes
	private Pane mCanvasPane;

	@FXML
	public Canvas mDrawingCanvas;

	@FXML
	public GridPane mNodesGridPane;

	@FXML
	private ScrollPane mScrollPane;

	private CanvasNodeController mSelectedNodeController;

	@FXML
	private Button mSingleStepBtn;

	@FXML
	private void initialize()
	{
		JBUI.sInstance.mMainController = this;
		mDrawingCanvas.widthProperty().bind(mCanvasPane.widthProperty());
		mDrawingCanvas.heightProperty().bind(mCanvasPane.heightProperty());

		// Pick a better cursor for panning
		mScrollPane.setOnMousePressed(event ->
		{
			event.setDragDetect(true);
		});

		mScrollPane.setOnDragDetected(event ->
		{
			mScrollPane.setCursor(Cursor.CLOSED_HAND);
		});

		// Prompt earlier the general paths controller setup window, for convenience
		showConfirmationAlert(GeneralPathsAlert.class);

		mSingleStepBtn.setOnAction(event ->
		{
			assert (mSelectedNodeController != null);
			mSelectedNodeController.performGuidedSearch(1);
		});

		mAnyStepBtn.setOnAction(event ->
		{
			assert (mSelectedNodeController != null);
			mSelectedNodeController.promptSearchDepthDialog(event);
		});
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

	void selectNodeController(CanvasNodeController controller)
	{
		if (mSelectedNodeController == null)
		{
			mSingleStepBtn.setDisable(false);
			mAnyStepBtn.setDisable(false);
		}
		else if (controller != mSelectedNodeController)
		{
			mSelectedNodeController.unselect();
		}
		else
		{
			return;
		}

		mSelectedNodeController = controller;
		controller.select();
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
