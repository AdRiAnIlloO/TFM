package jbui.controller;

import javafx.scene.control.TextField;

class Validation
{
	static void makeNumeric(TextField inputField)
	{
		inputField.textProperty().addListener((observable, oldText, newText) ->
		{
			try
			{
				if (!newText.isEmpty() && Integer.parseInt(newText) < 0)
				{
					inputField.setText(oldText);
				}
			}
			catch (NumberFormatException e)
			{
				inputField.setText(oldText);
			}
		});
	}
}
