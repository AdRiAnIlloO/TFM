package jbui.controller;

import java.io.IOException;

import fxtreelayout.FXTreeLayout;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import jbui.JBUI;
import jbui.model.IdSystemNode;

public class MainController
{
	private static int GAP_BETWEEN_LEVELS = 40;
	private static int GAP_BETWEEN_NODES = 20;

	@FXML
	private Button mAnyStepBtn;

	@FXML
	private Button mFoldToggleBtn;

	FXTreeLayout mFXTreeLayout;

	@FXML
	private MenuItem mProtocolLaunchBtn;

	@FXML
	private ScrollPane mScrollPane;

	private IdSystemNodeUIController mSelectedNodeController;

	/**
	 * These contribute to keeping the position of the selected node relative to the
	 * {@link #mScrollPane} viewport intact when the {@link #mTreePane} size
	 * changes. This feature is important to "cancel" the viewport from letting away
	 * nodes when they are expanded, which makes the user have to scroll in order to
	 * re-display them. These are only updated upon scroll bar translations. The
	 * previous listened values are needed for working since the scroll bar
	 * listeners are called before the {@link #mTreePane} size listeners.
	 */
	private double mSelectedNodeMarginX;
	private double mSelectedNodeMarginY;
	private double mSelectedNodePrevMarginX;
	private double mSelectedNodePrevMarginY;

	@FXML
	private Button mSingleStepBtn;

	@FXML
	private Pane mTreePane;

	private void centerTreeLayoutHorizontally(double scrollPaneWidth, double treePaneWidth)
	{
		double x = Math.max(0, scrollPaneWidth / 2 - treePaneWidth / 2);
		mTreePane.setTranslateX(x);
	}

	public void clearTree(IdSystemNodeUIController rootController)
	{
		mFXTreeLayout.removeNodesRecursively(rootController.mScreenNode);
	}

	public IdSystemNodeUIController createChildNode(IdSystemNode modelNode, IdSystemNodeUIController parent)
	{
		IdSystemNodeUIController child = new IdSystemNodeUIController(modelNode, parent);
		mFXTreeLayout.addChild(parent.mScreenNode, child.mScreenNode);
		return child;
	}

	public IdSystemNodeUIController createFXTreeLayout(IdSystemNode modelNode)
	{
		IdSystemNodeUIController root = new IdSystemNodeUIController(modelNode);
		mFXTreeLayout = new FXTreeLayout(mScrollPane, mTreePane, root.mScreenNode, GAP_BETWEEN_LEVELS,
				GAP_BETWEEN_NODES);
		return root;
	}

	@FXML
	private void initialize()
	{
		JBUI.sInstance.mMainController = this;

		// Automate updating of desired left margin for selected node
		mScrollPane.hvalueProperty().addListener((listener) ->
		{
			if (mSelectedNodeController != null)
			{
				mSelectedNodePrevMarginX = mSelectedNodeMarginX;
				updateSelectedNodeMarginX();
			}
		});

		// Automate updating of desired top margin for selected node
		mScrollPane.vvalueProperty().addListener((listener) ->
		{
			if (mSelectedNodeController != null)
			{
				mSelectedNodePrevMarginY = mSelectedNodeMarginY;
				updateSelectedNodeMarginY();
			}
		});

		// Automate horizontal centering for tree
		mScrollPane.widthProperty().addListener((observable, oldWidth, newWidth) ->
		{
			centerTreeLayoutHorizontally(newWidth.doubleValue(), mTreePane.getWidth());
		});

		mTreePane.widthProperty().addListener((observable, oldWidth, newWidth) ->
		{
			centerTreeLayoutHorizontally(mScrollPane.getWidth(), newWidth.doubleValue());

			if (mSelectedNodeController != null && mSelectedNodeMarginX > mSelectedNodePrevMarginX)
			{
				double hValue = (mSelectedNodeController.mScreenNode.getStackPane().getLocalToParentTransform().getTx()
						- mSelectedNodePrevMarginX) / (newWidth.doubleValue() - mScrollPane.getWidth());
				mScrollPane.setHvalue(hValue);
			}
		});

		mTreePane.heightProperty().addListener((observable, oldHeight, newHeight) ->
		{
			if (mSelectedNodeController != null && mSelectedNodeMarginY > mSelectedNodePrevMarginY)
			{
				double vValue = (mSelectedNodeController.mScreenNode.getStackPane().getLocalToParentTransform().getTy()
						- mSelectedNodePrevMarginY) / (newHeight.doubleValue() - mScrollPane.getHeight());
				mScrollPane.setVvalue(vValue);
			}
		});

		// Pick a better cursor for panning
		mScrollPane.setOnMousePressed(event ->
		{
			event.setDragDetect(true);
		});

		mScrollPane.setOnDragDetected(event ->
		{
			mScrollPane.setCursor(Cursor.CLOSED_HAND);
		});

		mProtocolLaunchBtn.setOnAction(event ->
		{
			showConfirmationAlert(ProtocolPathAlert.class);
		});

		mSingleStepBtn.setOnAction(event ->
		{
			assert (mSelectedNodeController != null);
			mSelectedNodeController.performGuidedSearch(1);
		});

		mAnyStepBtn.setOnAction(event ->
		{
			assert (mSelectedNodeController != null);
			mSelectedNodeController.promptSearchDepthDialog();
		});

		mFoldToggleBtn.setOnAction(event ->
		{
			assert (mSelectedNodeController != null);

			if (mSelectedNodeController.mScreenNode.isFolded())
			{
				mSelectedNodeController.unfold();
				mFoldToggleBtn.setText("Fold");
				return;
			}

			mSelectedNodeController.fold();
			mFoldToggleBtn.setText("Unfold");
		});

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

	void selectScreenNode(IdSystemNodeUIController node)
	{
		if (mSelectedNodeController == null)
		{
			mSingleStepBtn.setDisable(false);
			mAnyStepBtn.setDisable(false);
			mFoldToggleBtn.setDisable(false);
		}
		else if (node != mSelectedNodeController)
		{
			mSelectedNodeController.unselect();
		}
		else
		{
			return;
		}

		mSelectedNodeController = node;
		node.select();
		updateSelectedNodeMarginX();
		updateSelectedNodeMarginY();
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

	public void unblockProtocolLaunches()
	{
		mProtocolLaunchBtn.setDisable(false);
	}

	private void updateSelectedNodeMarginX()
	{
		assert (mSelectedNodeController != null);
		mSelectedNodeMarginX = mSelectedNodeController.getPath().getLocalToSceneTransform().getTx()
				- mScrollPane.getLocalToSceneTransform().getTx();
	}

	private void updateSelectedNodeMarginY()
	{
		assert (mSelectedNodeController != null);
		mSelectedNodeMarginY = mSelectedNodeController.getPath().getLocalToSceneTransform().getTy()
				- mScrollPane.getLocalToSceneTransform().getTy();
	}
}
