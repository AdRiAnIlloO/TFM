package jbui.controller;

import java.util.Optional;

import fxtreelayout.NestedOvalTextNode;
import fxtreelayout.OvalTextNode;
import javafx.css.PseudoClass;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import jbui.JBUI;
import jbui.model.IdSystemNode;

public class IdSystemNodeUIController
{
	private static final PseudoClass FOLDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("folded");
	private static final Font PREFERED_FONT = new Font(16);
	private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

	private IdSystemNode mModelNode;
	final OvalTextNode mScreenNode;

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
				return;
			}

			fold();
		});

		inputDepthSearchItem.setOnAction(actionEvent -> promptSearchDepthDialog());
		ContextMenu contextMenu = new ContextMenu(singleDepthSearchItem, inputDepthSearchItem, foldToggleItem);
		contextMenu.show(getPath().getScene().getWindow(), event.getScreenX(), event.getScreenY());
	}

	void fold()
	{
		JBUI.getMainController().mFXTreeLayout.fold(mScreenNode);
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

		getPath().setOnMousePressed(event ->
		{
			switch (event.getButton())
			{
			case SECONDARY:
			{
				createAndShowContextMenu(event);
			}
			default:
			{
				JBUI.getMainController().selectScreenNode(this);
			}
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
		getPath().pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
	}

	void unfold()
	{
		JBUI.getMainController().mFXTreeLayout.unfold(mScreenNode);
		getPath().pseudoClassStateChanged(FOLDED_PSEUDO_CLASS, false);
		getIdText().pseudoClassStateChanged(FOLDED_PSEUDO_CLASS, false);
	}

	void unselect()
	{
		getPath().pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
	}
}
