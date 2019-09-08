package jbui.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONException;

import fxtreelayout.FXTreeLayout;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import jbui.JBUI;
import jbui.model.IdSystemNode;
import jbui.persistence.MainControllerJSONTreeSaveDAO;

public class MainController extends JSONTreeExportController implements Initializable
{
	private static int GAP_BETWEEN_LEVELS = 40;
	private static int GAP_BETWEEN_NODES = 20;

	@FXML
	Button mAnyStepBtn;

	@FXML
	private Tooltip mAnyStepsTooltip;

	@FXML
	private Menu mEditMenu;

	@FXML
	private CheckMenuItem mEnglishMenuItem;

	@FXML
	private MenuItem mExitMenuItem;

	@FXML
	private Menu mFileMenu;

	@FXML
	Button mFoldToggleBtn;

	@FXML
	private Tooltip mFoldToggleTooltip;

	public FXTreeLayout mFXTreeLayout;

	@FXML
	private Menu mLanguageMenu;

	@FXML
	private MenuItem mMaudePathsMenuItem;

	@FXML
	private MenuItem mProtocolLaunchBtn;

	@FXML
	private ScrollPane mScrollPane;

	public IdSystemNodeUIController mSelectedNodeController;

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
	Button mSingleStepBtn;

	@FXML
	private Tooltip mSingleStepTooltip;

	@FXML
	private CheckMenuItem mSpanishMenuItem;

	@FXML
	private Button mStateContentViewBtn;

	@FXML
	private CheckMenuItem mTreeAutoSaveCheckItem;

	@FXML
	public MenuItem mTreeExportItem;

	@FXML
	private Pane mTreePane;

	@FXML
	private MenuItem mTreeQuickSaveItem;

	private void centerTreeLayoutHorizontally(double scrollPaneWidth, double treePaneWidth)
	{
		double x = Math.max(0, scrollPaneWidth / 2 - treePaneWidth / 2);
		mTreePane.setTranslateX(x);
	}

	private void changeLanguage(ResourceBundle resources, CheckMenuItem curCheckItem, CheckMenuItem nextCheckItem)
	{
		super.changeLanguage(resources);
		nextCheckItem.setSelected(true);
		nextCheckItem.setDisable(true);
		curCheckItem.setSelected(false);
		curCheckItem.setDisable(false);
		mFileMenu.setText(resources.getString("File"));
		mMaudePathsMenuItem.setText(resources.getString("SetMaudePaths"));
		mProtocolLaunchBtn.setText(resources.getString("LaunchProtocol"));
		mTreeQuickSaveItem.setText(resources.getString("Save"));
		mTreeExportItem.setText(resources.getString("ExportProtocol"));
		mExitMenuItem.setText(resources.getString("Exit"));
		mTreeAutoSaveCheckItem.setText(resources.getString("EnableProtocolAutosave"));
		mEditMenu.setText(resources.getString("Edit"));
		mLanguageMenu.setText(resources.getString("Language"));
		mEnglishMenuItem.setText(resources.getString("English"));
		mSpanishMenuItem.setText(resources.getString("Spanish"));
		mStateContentViewBtn.setText(resources.getString("ViewStateContent"));
		mSingleStepBtn.setText(resources.getString("SingleStep"));
		mSingleStepTooltip.setText(resources.getString("SingleStepHelp"));
		mAnyStepBtn.setText(resources.getString("AnySteps"));
		mAnyStepsTooltip.setText(resources.getString("AnyStepsHelp"));
		mFoldToggleTooltip.setText(resources.getString("FoldToggleHelp"));

		if (mSelectedNodeController == null || !mSelectedNodeController.mScreenNode.isFolded())
		{
			mFoldToggleBtn.setText(resources.getString(IdSystemNodeUIController.FOLD_LOCALIZATION_TOKEN));
		}
		else
		{
			mFoldToggleBtn.setText(resources.getString(IdSystemNodeUIController.UNFOLD_LOCALIZATION_TOKEN));
		}
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

	@Override
	void disableProtocolSaving()
	{
		super.disableProtocolSaving();
		mProtocolSaveFile = null;
		mTreeQuickSaveItem.setDisable(true);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		JBUI.sInstance.mMainController = this;
		mMaudePathsMenuItem.setOnAction(actionEvent -> showConfirmationAlert(GeneralPathsAlert.class));
		mExitMenuItem.setOnAction(actionEvent -> Platform.exit());

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
		ControllerUtil.setPanningHandScrollPaneCursors(mScrollPane);

		mProtocolLaunchBtn.setOnAction(event ->
		{
			showConfirmationAlert(ProtocolPathAlert.class);
		});

		mStateContentViewBtn.setOnAction(event ->
		{
			mSelectedNodeController.showDetailWindow();
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
			}
			else
			{
				mSelectedNodeController.fold();
			}

			tryAutoSaveCurrentProtocol();
		});

		mTreeQuickSaveItem.setOnAction(actionEvent ->
		{
			if (mProtocolSaveFile != null)
			{
				saveCurrentProtocol();
			}
		});

		mTreeExportItem.setOnAction(actionEvent ->
		{
			File treeSaveFile = showJSONSavePathDialog(JBUI.sInstance.mLastTreeSaveDirectory);

			if (treeSaveFile != null)
			{
				mProtocolSaveFile = treeSaveFile;
				JBUI.sInstance.mLastTreeSaveDirectory = treeSaveFile.getParentFile();
				notifyNewSaveFile(treeSaveFile);
				saveCurrentProtocol();
			}
		});

		mEnglishMenuItem.setOnAction(actionEvent ->
		{
			changeLanguage(JBUI.sInstance.mEnglishResources, mSpanishMenuItem, mEnglishMenuItem);
		});

		mSpanishMenuItem.setOnAction(actionEvent ->
		{
			changeLanguage(JBUI.sInstance.mSpanishResources, mEnglishMenuItem, mSpanishMenuItem);
		});

		// Prompt earlier the general paths controller setup window, for convenience
		showConfirmationAlert(GeneralPathsAlert.class);
	}

	private void saveCurrentProtocol()
	{
		try
		{
			handleProtocolSaveQueued(new MainControllerJSONTreeSaveDAO());
			mTreeQuickSaveItem.setDisable(true);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public void selectScreenNode(IdSystemNodeUIController node)
	{
		if (mSelectedNodeController == null)
		{
			mStateContentViewBtn.setDisable(false);
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
		tryAutoSaveCurrentProtocol();
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

	public void tryAutoSaveCurrentProtocol()
	{
		if (mProtocolSaveFile != null)
		{
			if (mTreeAutoSaveCheckItem.isSelected())
			{
				saveCurrentProtocol();
				return;
			}

			mTreeQuickSaveItem.setDisable(false);
			handleProtocolDataChanged();
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
