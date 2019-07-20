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

public abstract class LoadablesController
{
	DialogPane mDialogPane;

	/**
	 * Contains failed module identifiers determined after a path load result
	 * 
	 * It is used to notify the user about present failed when he confirms the alert
	 */
	private Set<Integer> mDistinctFailedModules;

	/**
	 * Contains successfully loaded module identifiers, only for latest loaded path
	 * 
	 * If a path is reloaded with error, the module identifier is removed from here.
	 * While it has elements, the confirmation button MUST be disabled. When it
	 * becomes empty, and not paths are loading, then it MUST be enabled.
	 */
	private Set<Integer> mDistinctOkModules;

	/**
	 * Represents the current amount of file paths that are loading asynchronously
	 * 
	 * While it is greater than zero, the confirmation button MUST be disabled
	 */
	private int mLoadingModulesAmount;

	LoadablesController()
	{
		mDistinctFailedModules = new HashSet<>();
		mDistinctOkModules = new HashSet<>();
	}

	abstract void handleClose(ButtonType buttonType);

	void handleCloseViaConfirmation()
	{
		if (!mDistinctFailedModules.isEmpty())
		{
			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText(mDistinctFailedModules.size() + " loaded path(s) failed and will be ignored");
			alert.showAndWait();
		}
	}

	void handleModuleLoadErrorWhileVisible(Label statusLabel, Enum<?> moduleType)
	{
		mDistinctFailedModules.add(moduleType.ordinal());
		mDistinctOkModules.remove(moduleType.ordinal());
		statusLabel.setText("Failed");
		statusLabel.setStyle("-fx-text-fill: red;");
		handleModuleLoadWhileVisible();
	}

	boolean handleModuleLoadOk(Label statusLabel, Enum<?> moduleType)
	{
		if (mDialogPane.isVisible())
		{
			handleModuleLoadOkWhileVisible(statusLabel, moduleType);
			return true;
		}

		return false;
	}

	private void handleModuleLoadOkWhileVisible(Enum<?> moduleType)
	{
		mDistinctOkModules.add(moduleType.ordinal());
		mDistinctFailedModules.remove(moduleType.ordinal());
		handleModuleLoadWhileVisible();
	}

	void handleModuleLoadOkWhileVisible(Label statusLabel, Enum<?> moduleType)
	{
		mDistinctFailedModules.remove(moduleType.ordinal());
		statusLabel.setText("OK");
		statusLabel.setStyle("-fx-text-fill: green;");
		handleModuleLoadOkWhileVisible(moduleType);
	}

	// This must be called after the module identifier is registered at our lists
	void handleModuleLoadWhileVisible()
	{
		if (--mLoadingModulesAmount < 1)
		{
			// Make the user can confirm the dialog, since we got a valid module,
			// so we don't force the user to supply valid files for the rest of paths
			mDialogPane.setCursor(Cursor.DEFAULT);
			setOkButtonDisable(mDistinctOkModules.isEmpty());
		}
	}

	private void handleModulePathChange()
	{
		if (mLoadingModulesAmount < 1)
		{
			mDialogPane.setCursor(Cursor.WAIT);
			setOkButtonDisable(true);
		}

		mLoadingModulesAmount++;
	}

	void handleModulePathChange(DAO loadDAO)
	{
		handleModulePathChange();
		loadDAO.makeAsync();
	}

	void handleModulePathChange(Label statusLabel)
	{
		handleModulePathChange();
		statusLabel.setText("Loading...");
		statusLabel.setStyle("-fx-text-fill: orange;");
	}

	void handleModulePathChange(Label statusLabel, DAO loadDAO)
	{
		handleModulePathChange(statusLabel);
		loadDAO.makeAsync();
	}

	/**
	 * @param textField
	 * @param updatedFile
	 * @param lastConfirmedDirectory
	 * @return If final updatedFiles valid, its parent directory. Otherwise,
	 *         lastConfirmedDirectory
	 */
	File handlePathDialogResult(TextField textField, File updatedFile, File lastConfirmedDirectory)
	{
		textField.setText(updatedFile.getAbsolutePath());
		lastConfirmedDirectory = updatedFile.getParentFile();
		return lastConfirmedDirectory;
	}

	abstract void postInitialize();

	void setOkButtonDisable(boolean disable)
	{
		mDialogPane.lookupButton(ButtonType.OK).setDisable(disable);
	}

	File showMaudePathDialog(TextField textField, File lastConfirmedDirectory)
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
	File showPathDialog(TextField textField, ExtensionFilter filter, File lastConfirmedDirectory)
	{
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(lastConfirmedDirectory);

		if (filter != null)
		{
			chooser.getExtensionFilters().add(filter);
		}

		return chooser.showOpenDialog(JBUI.sInstance.mStage);
	}
}
