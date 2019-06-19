package jbui.controller;

import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import jbui.JBUI;

public class PathsWindowController
{
	@FXML
	private TextField mMaudeBinPath;

	@FXML
	private TextField mJSONModulePath;

	@FXML
	private TextField mNPAModulePath;

	@FXML
	private TextField mProtocolModulePath;

	@FXML
	private Label mMaudeBinStatus;

	@FXML
	private Label mNPAModuleStatus;

	@FXML
	private Label mProtocolModuleStatus;

	@FXML
	private Label mJSONModuleStatus;

	// Last directory upon file search dialog close
	private File mLastConfirmedDirectory;

	private Process mMaudeProcess;

	public PathsWindowController()
	{
		mLastConfirmedDirectory = new File(JBUI.getMaudeBinPath()).getParentFile();
		mMaudeProcess = null;
	}

	@FXML
	public void initialize()
	{
		mMaudeBinPath.textProperty().addListener((observable, oldPath, newPath) -> validateMaudeBinPath(newPath));
		mMaudeBinPath.setText(JBUI.getMaudeBinPath());
		mNPAModulePath.setText(JBUI.getNPAModulePath());
		mProtocolModulePath.setText(JBUI.getProtocolModulePath());
		mJSONModulePath.setText(JBUI.getJSONModulePath());
	}

	@FXML
	private void onMaudeBinPathSearchBtnClick(ActionEvent event)
	{
		showPathDialogAndUpdate(mMaudeBinPath, null);
	}

	@FXML
	private void onNPAModulePathSearchBtnClick(ActionEvent event)
	{
		showMaudePathDialogAndUpdate(mNPAModulePath);
	}

	@FXML
	private void onProtocolModuleSearchBtnClick(ActionEvent event)
	{
		showMaudePathDialogAndUpdate(mProtocolModulePath);
	}

	@FXML
	private void onJSONModuleSearchBtnClick(ActionEvent event)
	{
		showMaudePathDialogAndUpdate(mJSONModulePath);
	}

	private void showPathDialogAndUpdate(TextField textField, ExtensionFilter filter)
	{
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(mLastConfirmedDirectory);

		if (filter != null)
		{
			chooser.getExtensionFilters().add(filter);
		}

		File file = chooser.showOpenDialog(JBUI.sInstance.mStage);

		if (file != null)
		{
			File dir = file.getParentFile();
			mLastConfirmedDirectory = dir;
			textField.setText(file.toString());
		}
	}

	private void showMaudePathDialogAndUpdate(TextField textField)
	{
		ExtensionFilter filter = new ExtensionFilter("Maude files", "*.maude");
		showPathDialogAndUpdate(textField, filter);
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
			mMaudeBinStatus.setStyle("font-color: red;");
		}
	}

	public void handleClosed(ButtonType buttonType)
	{
		if (buttonType == ButtonType.OK)
		{
			JBUI.sInstance.mMaudeBinPath = mMaudeBinPath.getText();
			JBUI.sInstance.mNPAModulePath = mNPAModulePath.getText();
			JBUI.sInstance.mProtocolModulePath = mProtocolModulePath.getText();
			JBUI.sInstance.mJSONModulePath = mJSONModulePath.getText();

			if (mMaudeProcess != null)
			{
				if (JBUI.getMaudeThinker().mMaudeProcess != null)
				{
					JBUI.getMaudeThinker().mMaudeProcess.destroy();
				}

				JBUI.getMaudeThinker().mMaudeProcess = mMaudeProcess;
			}
		}
		else if (mMaudeProcess != null)
		{
			mMaudeProcess.destroy();
			mMaudeProcess = null;
		}
	}
}
