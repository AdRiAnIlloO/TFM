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

public class GeneralPathsController extends LoadablesController
{
	private enum ModuleType
	{
		MaudeBin, NPA
	}

	// Last directories upon file search dialog closes
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

		// Fallback each missing last directory to the other,
		// to make the user likely locate the each module more easily
		if (mLastMaudeBinDir == null)
		{
			mLastMaudeBinDir = mLastNPAModuleDir;
		}
		else if (mLastNPAModuleDir == null)
		{
			mLastNPAModuleDir = mLastMaudeBinDir;
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

	public void handleMaudeProcessLoadResult(Process maudeProcess, File maudeBinFile)
	{
		if (mDialogPane.isVisible())
		{
			JBUI.getMaudeThinker().setNextMaudeProcess(maudeProcess);

			if (maudeProcess != null)
			{
				handleModuleLoadOk(mMaudeBinStatus, ModuleType.MaudeBin);
				mMaudeBinFile = maudeBinFile;
				mMaudeBinPath.setText(maudeBinFile.getAbsolutePath());
				mLastMaudeBinDir = maudeBinFile.getParentFile();

				// Make the user likely locate the missing NPA module more easily
				if (mLastNPAModuleDir == null)
				{
					mLastNPAModuleDir = mLastMaudeBinDir;
				}

				return;
			}

			mMaudeBinFile = null;
			handleModuleLoadErrorWhileVisible(mMaudeBinStatus, ModuleType.MaudeBin);
		}
	}

	public void handleNPAModuleLoad(String npaModuleTextInput)
	{
		if (handleModuleLoadOk(mNPAModuleStatus, ModuleType.NPA))
		{
			JBUI.getMaudeThinker().mNextNPAModuleTextInput = npaModuleTextInput;

			// Make the user likely locate the missing Maude executable more easily
			if (mLastMaudeBinDir == null)
			{
				mLastMaudeBinDir = mLastNPAModuleDir;
			}
		}
	}

	private void handleNPAModulePathChange()
	{
		DAO dao = new NPAModuleLoadDAO(this, mNPAModuleFile);
		handleModulePathChange(mNPAModuleStatus, dao);
	}

	public void notifyJSONModuleLoad(String jsonModuleTextInput)
	{
		if (mDialogPane.isVisible())
		{
			handleModuleLoadWhileVisible();
			JBUI.getMaudeThinker().mJSONModuleTextInput = jsonModuleTextInput;
		}
	}

	@FXML
	private void onMaudeBinPathSearchBtnClick(ActionEvent event)
	{
		File maudeBinFile = showPathLoadDialog(null, mLastMaudeBinDir);

		if (maudeBinFile != null)
		{
			mLastMaudeBinDir = handlePathDialogResult(mMaudeBinPath, maudeBinFile);
			mMaudeBinFile = maudeBinFile;
			handleMaudeBinPathChange(mMaudeBinFile.getAbsolutePath(), mMaudeBinFile);
		}
	}

	@FXML
	private void onNPAModulePathSearchBtnClick(ActionEvent event)
	{
		File npaModuleFile = showMaudePathLoadDialog(mLastNPAModuleDir);

		if (npaModuleFile != null)
		{
			mLastNPAModuleDir = handlePathDialogResult(mNPAModulePath, npaModuleFile);
			mNPAModuleFile = npaModuleFile;
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
