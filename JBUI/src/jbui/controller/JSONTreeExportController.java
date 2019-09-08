package jbui.controller;

import java.io.File;

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

	@FXML
	private Label mProtocolSaveStatus;

	void disableProtocolSaving()
	{
		mProtocolSaveFileReminder.setText("(No export file specified)");
	}

	void handleProtocolDataChanged()
	{
		mProtocolSaveStatus.setText("There are unsaved changes");
	}

	public void handleProtocolSaveDone()
	{
		if (--mNumPendingProtocolSaves < 1)
		{
			mProtocolSaveStatus.setText("All protocol changes saved");
		}
	}

	void handleProtocolSaveQueued(DAO dao)
	{
		dao.makeAsync();
		mNumPendingProtocolSaves++;
		mProtocolSaveStatus.setText("Saving protocol changes...");
	}

	public boolean handleSaveDAOReplace()
	{
		assert (mNumPendingProtocolSaves > 1);
		mNumPendingProtocolSaves--;
		return true;
	}

	void notifyNewSaveFile(File saveFile)
	{
		mProtocolSaveFileReminder.setText("Export file: [" + saveFile.getAbsolutePath() + "]");
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
