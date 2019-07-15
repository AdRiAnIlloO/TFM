package jbui.controller;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import jbui.JBUI;
import jbui.persistence.DAO;
import jbui.persistence.JSONModuleLoadDAO;
import jbui.persistence.MaudeExecutableLoadDAO;
import jbui.persistence.NPAModuleLoadDAO;

public class GeneralPathsController extends PathsSetupController
{
	// Last directories upon file search dialog close
	private File mLastMaudeBinDir;
	private File mLastNPAModuleDir;

	private File mMaudeBinFile;

	@FXML
	private TextField mMaudeBinPath;

	@FXML
	private Label mMaudeBinStatus;

	private File mNPAModuleFile;

	@FXML
	private TextField mNPAModulePath;

	@FXML
	private Label mNPAModuleStatus;

	public GeneralPathsController()
	{
		mMaudeBinFile = JBUI.getMaudeBinFile();
		mNPAModuleFile = JBUI.sInstance.mNPAModuleFile;

		if (mMaudeBinFile != null)
		{
			mLastMaudeBinDir = mMaudeBinFile.getParentFile();
		}

		if (mNPAModuleFile != null)
		{
			mLastNPAModuleDir = mNPAModuleFile.getParentFile();
		}
	}

	@Override
	void handleClose(ButtonType buttonType)
	{
		if (buttonType == ButtonType.OK)
		{
			handleCloseViaConfirmation();
			JBUI.sInstance.mMaudeBinFile = mMaudeBinFile;
			JBUI.sInstance.mNPAModuleFile = mNPAModuleFile;
			JBUI.getMaudeThinker().tryUpdateMainComponents();
		}
		else
		{
			JBUI.getMaudeThinker().mNextNPAModuleTextInput = null;
			JBUI.getMaudeThinker().setNextMaudeProcess(null);
		}
	}

	private void handleMaudeBinPathChange(String maudeBinPathName, File maudeBinFile)
	{
		DAO dao = new MaudeExecutableLoadDAO(this, maudeBinPathName, maudeBinFile);
		handleModulePathChange(mMaudeBinStatus, dao);
	}

	public void handleMaudeProcessLoadResult(Process maudeProcess, String maudeBinPathName)
	{
		if (mDialogPane.isVisible())
		{
			JBUI.getMaudeThinker().setNextMaudeProcess(maudeProcess);
			mMaudeBinPath.setText(maudeBinPathName);

			if (maudeProcess != null)
			{
				handleModuleLoadOk(mMaudeBinStatus);
				return;
			}

			handleModuleLoadErrorWhileVisible(mMaudeBinStatus);
		}
	}

	public void handleNPAModuleLoad(String npaModuleTextInput)
	{
		if (handleModuleLoadOk(mNPAModuleStatus))
		{
			JBUI.getMaudeThinker().mNextNPAModuleTextInput = npaModuleTextInput;
		}
	}

	private void handleNPAModulePathChange()
	{
		DAO dao = new NPAModuleLoadDAO(this, mNPAModuleFile);
		handleModulePathChange(mNPAModuleStatus, dao);
	}

	public void notifyJSONModuleLoad(String jsonModuleTextInput)
	{
		if (handleModuleLoadOk())
		{
			JBUI.getMaudeThinker().mJSONModuleTextInput = jsonModuleTextInput;
		}
	}

	@FXML
	private void onMaudeBinPathSearchBtnClick(ActionEvent event)
	{
		mMaudeBinFile = showPathDialog(mMaudeBinPath, null, mLastMaudeBinDir);
		mLastMaudeBinDir = JBUI.handlePathDialogResult(mMaudeBinPath, mMaudeBinFile, mLastMaudeBinDir);

		if (mMaudeBinFile != null)
		{
			handleMaudeBinPathChange(mMaudeBinFile.getAbsolutePath(), mMaudeBinFile);
		}
	}

	@FXML
	private void onNPAModulePathSearchBtnClick(ActionEvent event)
	{
		mNPAModuleFile = showMaudePathDialog(mNPAModulePath, mLastNPAModuleDir);
		mLastNPAModuleDir = JBUI.handlePathDialogResult(mNPAModulePath, mNPAModuleFile, mLastNPAModuleDir);

		if (mNPAModuleFile != null)
		{
			handleNPAModulePathChange();
		}
	}

	@Override
	void postInitialize()
	{
		String maudeBinPathName;

		if (mMaudeBinFile != null)
		{
			maudeBinPathName = mMaudeBinFile.getAbsolutePath();
		}
		else
		{
			// Resolve Maude bin path to a full path via PATH environment
			maudeBinPathName = "maude";
			String osName = System.getProperty("os.name");

			if (osName.startsWith("Windows") && !maudeBinPathName.endsWith(".exe"))
			{
				maudeBinPathName += ".exe";
			}
		}

		mMaudeBinPath.setText(maudeBinPathName);
		handleMaudeBinPathChange(maudeBinPathName, mMaudeBinFile);

		if (mNPAModuleFile != null)
		{
			mNPAModulePath.setText(mNPAModuleFile.getAbsolutePath());
			handleNPAModulePathChange();
		}

		// Request loading JSON module
		DAO dao = new JSONModuleLoadDAO(this, "resource/maude_npa_json.maude");
		handleModulePathChange(dao);
	}
}
