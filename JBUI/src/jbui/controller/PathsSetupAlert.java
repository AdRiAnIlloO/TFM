package jbui.controller;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import jbui.JBUI;

class PathsSetupAlert<T extends PathsSetupController> extends Alert
{
	private T mController;

	PathsSetupAlert(String title, String headerText, String contentFXMLPathName)
	{
		super(AlertType.CONFIRMATION);
		setTitle(title);
		setHeaderText(headerText);

		// Disable the capability to confirm the dialog (we'll enable it once validated)
		getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

		try
		{
			URL url = JBUI.getResource(contentFXMLPathName);
			FXMLLoader loader = new FXMLLoader(url);
			Parent contentNode = loader.load();
			getDialogPane().setContent(contentNode);
			mController = loader.getController();
			mController.mDialogPane = getDialogPane();
			mController.postInitialize();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	void showAndWaitAndHandle()
	{
		showAndWait().ifPresent((buttonType) -> mController.handleClose(buttonType));
	}
}
