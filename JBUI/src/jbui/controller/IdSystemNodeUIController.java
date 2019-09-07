package jbui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import fxtreelayout.NestedOvalTextNode;
import fxtreelayout.OvalTextNode;
import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import jbui.JBUI;
import jbui.model.IdSystemNode;

public class IdSystemNodeUIController
{
	private static final String FOLD_LOCALIZATION_TOKEN = "Fold";
	private static final PseudoClass FOLDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("folded");
	private static final Font PREFERED_FONT = new Font(16);
	private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
	private static final String UNFOLD_LOCALIZATION_TOKEN = "Unfold";

	private IdSystemNode mModelNode;
	public final OvalTextNode mScreenNode;

	IdSystemNodeUIController(IdSystemNode modelNode)
	{
		mScreenNode = new OvalTextNode(modelNode.unparseIdUnspaced(), PREFERED_FONT);
		init(modelNode);
	}

	IdSystemNodeUIController(IdSystemNode modelNode, IdSystemNodeUIController parent)
	{
		mScreenNode = new NestedOvalTextNode(modelNode.unparseIdUnspaced(), PREFERED_FONT, parent.mScreenNode);
		init(modelNode);
	}

	private void createAndShowContextMenu(MouseEvent event)
	{
		MenuItem item = new MenuItem("View state content...");
		ContextMenu contextMenu = new ContextMenu(item);

		item.setOnAction(actionEvent ->
		{
			showDetailWindow();
		});

		if (mModelNode.mStateType == IdSystemNode.StateType.Default)
		{
			MenuItem singleDepthSearchItem = new MenuItem("Search from this node (single step)");
			MenuItem inputDepthSearchItem = new MenuItem("Search from this node...");
			MenuItem foldToggleItem = new MenuItem(mScreenNode.isFolded() ? "Unfold" : "Fold");

			singleDepthSearchItem.setOnAction(actionEvent ->
			{
				performGuidedSearch(1);
			});

			foldToggleItem.setOnAction(actionEvent ->
			{
				if (mScreenNode.isFolded())
				{
					unfold();
				}
				else
				{
					fold();
				}

				JBUI.getMainController().tryAutoSaveCurrentProtocol();
			});

			inputDepthSearchItem.setOnAction(actionEvent -> promptSearchDepthDialog());
			contextMenu.getItems().addAll(singleDepthSearchItem, inputDepthSearchItem, foldToggleItem);
		}

		contextMenu.show(getPath().getScene().getWindow(), event.getScreenX(), event.getScreenY());
	}

	public void fold()
	{
		JBUI.getMainController().mFXTreeLayout.fold(mScreenNode);
		JBUI.getMainController().mFoldToggleBtn.setText(UNFOLD_LOCALIZATION_TOKEN);
		getPath().pseudoClassStateChanged(FOLDED_PSEUDO_CLASS, true);
		getIdText().pseudoClassStateChanged(FOLDED_PSEUDO_CLASS, true);
	}

	private Text getIdText()
	{
		return mScreenNode.getIdText();
	}

	Path getPath()
	{
		return mScreenNode.getPath();
	}

	private void init(IdSystemNode modelNode)
	{
		getPath().getStyleClass().add("state_node");
		mScreenNode.getIdText().getStyleClass().addAll("state_node_label");
		mModelNode = modelNode;

		if (modelNode.mStateType == IdSystemNode.StateType.Initial)
		{
			getPath().getStyleClass().add("initial_state_node");
		}

		getPath().setOnMousePressed(event ->
		{
			JBUI.getMainController().selectScreenNode(this);

			switch (event.getButton())
			{
			case PRIMARY:
			{
				if (event.getClickCount() > 1)
				{
					showDetailWindow();
				}

				break;
			}
			case SECONDARY:
			{
				createAndShowContextMenu(event);
				break;
			}
			default:
			}
		});
	}

	void performGuidedSearch(int depth)
	{
		JBUI.getMaudeThinker().performGuidedSearch(depth, mModelNode);
	}

	void promptSearchDepthDialog()
	{
		TextInputDialog dialog = new TextInputDialog("0");
		dialog.setHeaderText("Choose the search depth");
		Validation.makeNumeric(dialog.getEditor());
		Optional<String> result = dialog.showAndWait();

		result.ifPresent(inputText ->
		{
			if (!inputText.isEmpty())
			{
				performGuidedSearch(Integer.parseInt(inputText));
			}
		});
	}

	void select()
	{
		JBUI.getMainController().mFoldToggleBtn
				.setText(mScreenNode.isFolded() ? UNFOLD_LOCALIZATION_TOKEN : FOLD_LOCALIZATION_TOKEN);
		boolean disableButtons = (mModelNode.mStateType != IdSystemNode.StateType.Default);
		JBUI.getMainController().mSingleStepBtn.setDisable(disableButtons);
		JBUI.getMainController().mAnyStepBtn.setDisable(disableButtons);
		JBUI.getMainController().mFoldToggleBtn.setDisable(disableButtons);
		getPath().pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
	}

	void showDetailWindow()
	{
		try
		{
			URL url = JBUI.getResource("view/NodeDetailWindow.fxml");
			FXMLLoader loader = new FXMLLoader(url);
			Region region = loader.load();
			((NodeDetailController) loader.getController()).init(mModelNode);
			Stage stage = new Stage();
			Scene scene = new Scene(region);
			stage.setScene(scene);
			stage.show();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	void unfold()
	{
		JBUI.getMainController().mFXTreeLayout.unfold(mScreenNode);
		JBUI.getMainController().mFoldToggleBtn.setText(FOLD_LOCALIZATION_TOKEN);
		getPath().pseudoClassStateChanged(FOLDED_PSEUDO_CLASS, false);
		getIdText().pseudoClassStateChanged(FOLDED_PSEUDO_CLASS, false);
	}

	void unselect()
	{
		getPath().pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
	}
}
