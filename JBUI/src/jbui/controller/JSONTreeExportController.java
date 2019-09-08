package jbui.controller;

import java.io.File;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import jbui.JBUI;
import jbui.persistence.DAO;

class JSONTreeExportController
{
	private int mNumPendingProtocolSaves;
	public File mProtocolSaveFile;

	/**
	 * Notifies which is the current file where to save the tree
	 */
	@FXML
	private Label mProtocolSaveFileReminder;

	private String mProtocolSaveReminderLocalizationToken = "UnspecifiedExportFile";

	@FXML
	private Label mProtocolSaveStatus;

	private String mProtocolStatusLocalizationToken = "UndetectedOrUnsaveableChanges";

	void changeLanguage(ResourceBundle resources)
	{
		JBUI.sInstance.mLocalizationResources = resources;
		mProtocolSaveStatus.setText(resources.getString(mProtocolStatusLocalizationToken));
		String text = JBUI.sInstance.mLocalizationResources.getString(mProtocolSaveReminderLocalizationToken);

		if (mProtocolSaveFile != null)
		{
			text = String.format(text, mProtocolSaveFile.getAbsolutePath());
		}

		mProtocolSaveFileReminder.setText(text);
	}

	void disableProtocolSaving()
	{
		mProtocolStatusLocalizationToken = "UndetectedOrUnsaveableChanges";
		mProtocolSaveReminderLocalizationToken = "UnspecifiedExportFile";
		mProtocolSaveStatus.setText(JBUI.sInstance.mLocalizationResources.getString(mProtocolStatusLocalizationToken));
		mProtocolSaveFileReminder
				.setText(JBUI.sInstance.mLocalizationResources.getString(mProtocolSaveReminderLocalizationToken));
	}

	void handleProtocolDataChanged()
	{
		mProtocolStatusLocalizationToken = "UnsavedChanges";
		mProtocolSaveStatus.setText(JBUI.sInstance.mLocalizationResources.getString(mProtocolStatusLocalizationToken));
	}

	public void handleProtocolSaveDone()
	{
		if (--mNumPendingProtocolSaves < 1)
		{
			mProtocolStatusLocalizationToken = "AllProtocolChangesSaved";
			mProtocolSaveStatus
					.setText(JBUI.sInstance.mLocalizationResources.getString(mProtocolStatusLocalizationToken));
		}
	}

	void handleProtocolSaveQueued(DAO dao)
	{
		dao.makeAsync();
		mNumPendingProtocolSaves++;
		mProtocolStatusLocalizationToken = "SavingProtocolChanges";
		mProtocolSaveStatus.setText(JBUI.sInstance.mLocalizationResources.getString(mProtocolStatusLocalizationToken));
	}

	public boolean handleSaveDAOReplace()
	{
		assert (mNumPendingProtocolSaves > 1);
		mNumPendingProtocolSaves--;
		return true;
	}

	void notifyNewSaveFile(File saveFile)
	{
		mProtocolSaveReminderLocalizationToken = "NewExportFileNotice";
		String text = String.format(
				JBUI.sInstance.mLocalizationResources.getString(mProtocolSaveReminderLocalizationToken),
				saveFile.getAbsolutePath());
		mProtocolSaveFileReminder.setText(text);
	}

	File showJSONSavePathDialog(File lastConfirmedDirectory)
	{
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(lastConfirmedDirectory);
		ExtensionFilter filter = new ExtensionFilter("JSON files", "*.json");
		chooser.getExtensionFilters().add(filter);
		return chooser.showSaveDialog(JBUI.sInstance.mStage);
	}
}
