package jbui.controller;

import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;

class ControllerUtil
{
	static void setPanningHandScrollPaneCursors(ScrollPane scrollPane)
	{
		scrollPane.setOnMousePressed(event ->
		{
			event.setDragDetect(true);
		});

		scrollPane.setOnDragDetected(event ->
		{
			scrollPane.setCursor(Cursor.CLOSED_HAND);
		});
	}
}
