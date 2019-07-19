package jbui.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;
import jbui.JBUI;

public class CanvasNodeController
{
	@FXML
	private Ellipse mEllipse;

	@FXML
	public Label mIdLabel;

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

	public void drawArcToChild(CanvasNodeController child, GraphicsContext ctx)
	{
		double canvasOffsetY = ctx.getCanvas().getLocalToSceneTransform().getTy();
		double x0 = mEllipse.getLocalToSceneTransform().getTx();
		double y0 = mEllipse.getLocalToSceneTransform().getTy() - canvasOffsetY;
		double x1 = child.mEllipse.getLocalToSceneTransform().getTx();
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
		mIdLabel.widthProperty().addListener((property, oldWidth, newWidth) ->
		{
			// Ideally, the width of the id label should only grow - FXML placeholder is "1"
			double radius = Math.max(newWidth.doubleValue() / 2, mEllipse.getRadiusX());
			mEllipse.setRadiusX(radius);
		});

		mEllipse.setOnMousePressed(event ->
		{
			// TODO: Mark active node selection?
		});
	}
}
