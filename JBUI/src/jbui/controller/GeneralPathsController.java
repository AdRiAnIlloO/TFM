package jbui.controller;

import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import jbui.JBUI;

public class GeneralPathsController implements ICloseHandleableDialogController
{
	@FXML
	private TextField mJSONModulePath;

	// Last directories upon file search dialog close
	private File mLastMaudeBinDir;

	private File mLastNPAModuleDir;
	private File mMaudeBinFile;

	@FXML
	private TextField mMaudeBinPath;

	@FXML
	private Label mMaudeBinStatus;

	private Process mMaudeProcess;
	private File mNPAModuleFile;

	@FXML
	private TextField mNPAModulePath;

	@FXML
	private Label mNPAModuleStatus;

	public GeneralPathsController()
	{
		mMaudeBinFile = JBUI.getMaudeBinFile();
		mNPAModuleFile = JBUI.getNPAModuleFile();

		if (mMaudeBinFile != null)
		{
			mLastMaudeBinDir = mMaudeBinFile.getParentFile();
		}

		if (mNPAModuleFile != null)
		{
			mLastNPAModuleDir = mNPAModuleFile.getParentFile();
		}
		else if (mMaudeBinFile != null)
		{
			// Fallback to the Maude bin directory, as it may be near the NPA one
			mLastNPAModuleDir = mLastMaudeBinDir;
		}
	}

	@Override
	public void handleClosed(ButtonType buttonType)
	{
		if (buttonType == ButtonType.OK)
		{
			JBUI.sInstance.mMaudeBinFile = mMaudeBinFile;
			JBUI.sInstance.mNPAModuleFile = mNPAModuleFile;

			if (mMaudeProcess != null)
			{
				if (JBUI.getMaudeProcess() != null)
				{
					JBUI.getMaudeProcess().destroy();
				}

				JBUI.sInstance.mMaudeProcess = mMaudeProcess;
			}
		}
		else if (mMaudeProcess != null)
		{
			mMaudeProcess.destroy();
			mMaudeProcess = null;
		}
	}

	@FXML
	public void initialize()
	{
		mMaudeBinPath.textProperty().addListener((observable, oldPath, newPath) -> validateMaudeBinPath(newPath));
		mNPAModulePath.textProperty().addListener((observable, oldPath, newPath) -> validateNPAModulePath(newPath));
		mJSONModulePath.setText(JBUI.DEFAULT_JSON_MODULE_PATH);

		if (mMaudeBinFile != null)
		{
			mMaudeBinPath.setText(mMaudeBinFile.getAbsolutePath());
		}

		if (mNPAModuleFile != null)
		{
			mNPAModulePath.setText(mNPAModuleFile.getAbsolutePath());
		}
	}

	@FXML
	private void onMaudeBinPathSearchBtnClick(ActionEvent event)
	{
		mMaudeBinFile = JBUI.showPathDialog(mMaudeBinPath, null, mLastMaudeBinDir);
		mLastMaudeBinDir = JBUI.handlePathDialogResult(mMaudeBinPath, mMaudeBinFile, mLastMaudeBinDir);
	}

	@FXML
	private void onNPAModulePathSearchBtnClick(ActionEvent event)
	{
		mNPAModuleFile = JBUI.showMaudePathDialog(mNPAModulePath, mLastNPAModuleDir);
		mLastNPAModuleDir = JBUI.handlePathDialogResult(mNPAModulePath, mNPAModuleFile, mLastNPAModuleDir);
	}

	private void validateMaudeBinPath(String path)
	{
		if (mMaudeProcess != null)
		{
			mMaudeProcess.destroy();
			mMaudeProcess = null;
		}

		try
		{
			ProcessBuilder builder = new ProcessBuilder(path);
			builder.redirectErrorStream(true);
			mMaudeProcess = builder.start();
			mMaudeBinStatus.setText("OK");
			mMaudeBinStatus.setStyle("-fx-text-fill: green;");
		}
		catch (IOException e)
		{
			mMaudeBinStatus.setText("Failed");
			mMaudeBinStatus.setStyle("-fx-text-fill: red;");
		}
	}

	private void validateNPAModulePath(String path)
	{
		mNPAModuleStatus.setText("OK");
		mNPAModuleStatus.setStyle("-fx-text-fill: green;");
	}
}
