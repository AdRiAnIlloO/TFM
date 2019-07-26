package jbui.controller;

import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import jbui.JBUI;
import jbui.model.IdSystemNode;

public class CanvasNodeController
{
	@FXML
	private Ellipse mEllipse;

	@FXML
	private Label mIdLabel;

	private IdSystemNode mModelNode;

	@FXML
	private StackPane mWrappingStackPane;

	public void addToGridPane(int columnIndex, int rowIndex, int columnSpan)
	{
		GridPane nodesGridPane = JBUI.getNodesGridPane();
		Integer auxColumnIndex = GridPane.getColumnIndex(mWrappingStackPane);

		if (auxColumnIndex != null)
		{
			// Check for already correct position & span
			if (auxColumnIndex.compareTo(columnIndex) == 0
					&& GridPane.getRowIndex(mWrappingStackPane).compareTo(rowIndex) == 0
					&& GridPane.getColumnSpan(mWrappingStackPane).compareTo(columnSpan) == 0)
			{
				return;
			}

			nodesGridPane.getChildren().remove(mWrappingStackPane);
		}

		nodesGridPane.add(mWrappingStackPane, columnIndex, rowIndex, columnSpan, 1);
	}

	private void createAndShowContextMenu(MouseEvent event)
	{
		MenuItem singleDepthSearchItem = new MenuItem("Search from this node (single step)");
		MenuItem inputDepthSearchItem = new MenuItem("Search from this node...");

		singleDepthSearchItem.setOnAction(actionEvent ->
		{
			performGuidedSearch(1);
		});

		inputDepthSearchItem.setOnAction(this::promptSearchDepthDialog);
		ContextMenu contextMenu = new ContextMenu(singleDepthSearchItem, inputDepthSearchItem);
		contextMenu.show(mEllipse.getScene().getWindow(), event.getScreenX(), event.getScreenY());
	}

	public void drawArcToChild(CanvasNodeController child, GraphicsContext ctx)
	{
		double canvasOffsetX = ctx.getCanvas().getLocalToSceneTransform().getTx();
		double canvasOffsetY = ctx.getCanvas().getLocalToSceneTransform().getTy();
		double x0 = mEllipse.getLocalToSceneTransform().getTx() - canvasOffsetX;
		double y0 = mEllipse.getLocalToSceneTransform().getTy() - canvasOffsetY;
		double x1 = child.mEllipse.getLocalToSceneTransform().getTx() - canvasOffsetX;
		double y1 = child.mEllipse.getLocalToSceneTransform().getTy() - child.mEllipse.getRadiusY() - canvasOffsetY;
		ctx.strokeLine(x0, y0, x1, y1);
	}

	public void hideFromGridPane()
	{
		JBUI.getNodesGridPane().getChildren().remove(mWrappingStackPane);
	}

	@FXML
	private void initialize()
	{
		mIdLabel.widthProperty().addListener((observable, oldWidth, newWidth) ->
		{
			// Ideally, the width of the id label should only grow - FXML placeholder is "1"
			double radius = Math.max(newWidth.doubleValue() / 2, mEllipse.getRadiusX());
			mEllipse.setRadiusX(radius);
		});

		mEllipse.setOnMousePressed(event ->
		{
			switch (event.getButton())
			{
			case SECONDARY:
			{
				createAndShowContextMenu(event);
			}
			default:
			{
				JBUI.getMainController().selectNodeController(this);
			}
			}
		});
	}

	void performGuidedSearch(int depth)
	{
		JBUI.getMaudeThinker().performGuidedSearch(depth, mModelNode);
	}

	void promptSearchDepthDialog(ActionEvent event)
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
		mEllipse.setFill(Color.BLUE);
	}
	
	public void setModelData(String idText, IdSystemNode modelNode)
	{
		mIdLabel.setText(idText);
		mModelNode = modelNode;
	}

	void unselect()
	{
		mEllipse.setFill(Color.CORNFLOWERBLUE);
	}
}
