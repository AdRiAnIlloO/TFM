package jbui.controller;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import jbui.JBUI;

class LoadablesAlert<T extends LoadablesController> extends Alert
{
	private T mController;

	LoadablesAlert(String titleToken, String headerToken, String contentFXMLPathName)
	{
		super(AlertType.CONFIRMATION);
		setTitle(JBUI.sInstance.mLocalizationResources.getString(titleToken));
		setHeaderText(JBUI.sInstance.mLocalizationResources.getString(headerToken));

		// Disable the capability to confirm the dialog (we'll enable it once validated)
		getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

		try
		{
			URL url = JBUI.getResource(contentFXMLPathName);
			FXMLLoader loader = new FXMLLoader(url, JBUI.sInstance.mLocalizationResources);
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
