package jbui.controller;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import jbui.JBUI;
import jbui.persistence.DAO;

public abstract class PathsSetupController
{
	static File showMaudePathDialog(TextField textField, File lastConfirmedDirectory)
	{
		ExtensionFilter filter = new ExtensionFilter("Maude files", "*.maude");
		return showPathDialog(textField, filter, lastConfirmedDirectory);
	}

	/**
	 * Shows a path search dialog with an associated TextField path for feedback
	 * 
	 * @param textField
	 * @param filter
	 * @param lastConfirmedDirectory Last parent directory user was located while in
	 *                               previous search dialogs
	 * @return Final choosen file
	 */
	static File showPathDialog(TextField textField, ExtensionFilter filter, File lastConfirmedDirectory)
	{
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(lastConfirmedDirectory);

		if (filter != null)
		{
			chooser.getExtensionFilters().add(filter);
		}

		return chooser.showOpenDialog(JBUI.sInstance.mStage);
	}

	DialogPane mDialogPane;

	// Set of failed modules' status labels determined after a path load result,
	// used to notify the user about this when he confirms the alert
	private Set<Label> mFailedModulesSet;

	private int mLoadingModulesAmount;

	PathsSetupController()
	{
		mFailedModulesSet = new HashSet<>();
	}

	abstract void handleClose(ButtonType buttonType);

	void handleCloseViaConfirmation()
	{
		if (!mFailedModulesSet.isEmpty())
		{
			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText(mFailedModulesSet.size() + " loaded path(s) failed and will be ignored");
			alert.showAndWait();
		}
	}

	void handleModuleLoadErrorWhileVisible(Label statusLabel)
	{
		mFailedModulesSet.add(statusLabel);
		statusLabel.setText("Failed");
		statusLabel.setStyle("-fx-text-fill: red;");
		handleModuleLoadWhileVisible();
	}

	boolean handleModuleLoadOk()
	{
		if (mDialogPane.isVisible())
		{
			handleModuleLoadWhileVisible();
			return true;
		}

		return false;
	}

	boolean handleModuleLoadOk(Label statusLabel)
	{
		mFailedModulesSet.remove(statusLabel);
		statusLabel.setText("OK");
		statusLabel.setStyle("-fx-text-fill: green;");
		return handleModuleLoadOk();
	}

	private void handleModuleLoadWhileVisible()
	{
		if (mLoadingModulesAmount < 2)
		{
			// Make the user can confirm the dialog, since we got a valid module,
			// and we don't force the user to supply valid files for the rest of paths
			mDialogPane.setCursor(Cursor.DEFAULT);
			setOkButtonDisable(false);
		}

		mLoadingModulesAmount--;
	}

	void handleModulePathChange(DAO loadDAO)
	{
		if (mLoadingModulesAmount < 1)
		{
			mDialogPane.setCursor(Cursor.WAIT);
			setOkButtonDisable(true);
		}

		mLoadingModulesAmount++;
		loadDAO.makeAsync();
	}

	void handleModulePathChange(Label statusLabel, DAO loadDAO)
	{
		statusLabel.setText("Loading...");
		statusLabel.setStyle("-fx-text-fill: orange;");
		handleModulePathChange(loadDAO);
	}

	abstract void postInitialize();

	private void setOkButtonDisable(boolean disable)
	{
		mDialogPane.lookupButton(ButtonType.OK).setDisable(disable);
	}
}
