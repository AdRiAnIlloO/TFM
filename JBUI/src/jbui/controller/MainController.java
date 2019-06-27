package jbui.controller;

import java.io.IOException;
import java.net.URL;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import jbui.JBUI;
import jbui.MaudeThinker;

public class MainController
{
	@FXML
	void onMaudePathsBtnClick(ActionEvent event) throws IOException
	{
		URL url = JBUI.class.getResource("view/paths_window.fxml");
		FXMLLoader loader = new FXMLLoader(url);
		GridPane gridPane = loader.load();
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Maude paths setup");
		alert.setHeaderText("Here you can set required files for a protocol execution");
		alert.getDialogPane().setContent(gridPane);
		PathsWindowController controller = loader.getController();
		alert.showAndWait().ifPresent((buttonType) -> controller.handleClosed(buttonType));
	}

	@FXML
	void onLaunchMenuClick(ActionEvent event)
	{
		JBUI.sInstance.mMaudeThinker.mState = MaudeThinker.State.CheckingRequirements;
	}
}
