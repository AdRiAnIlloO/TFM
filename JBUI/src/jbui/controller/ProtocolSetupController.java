package jbui.controller;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import jbui.JBUI;

public class ProtocolSetupController implements ICloseHandleableDialogController
{
	// Last directory upon file search dialog close
	private File mLastProtocolModuleDirectory;

	private File mProtocolModuleFile;

	@FXML
	private TextField mProtocolModulePath;

	@FXML
	private Label mProtocolModuleStatus;

	public ProtocolSetupController()
	{
		mProtocolModuleFile = JBUI.getProtocolModuleFile();

		if (mProtocolModuleFile != null)
		{
			mLastProtocolModuleDirectory = mProtocolModuleFile.getParentFile();
		}
		else if (JBUI.getMaudeBinFile() != null)
		{
			// Fallback to the Maude bin directory, as it may be near the protocol one
			mLastProtocolModuleDirectory = JBUI.getMaudeBinFile().getParentFile();
		}
	}

	@Override
	public void handleClosed(ButtonType buttonType)
	{
		if (buttonType == ButtonType.OK)
		{
			JBUI.sInstance.mProtocolModuleFile = mProtocolModuleFile;

			// Validate only the user-configurable dependencies, which may be invalid
			if (JBUI.getMaudeProcess() != null && JBUI.getNPAModuleFile() != null
					&& JBUI.getJSONModuleTextContent() != null)
			{
				assert (JBUI.getMaudeBinFile() != null);

				if (!JBUI.getMaudeThinker().tryInitializeNow(JBUI.getMaudeBinFile(), JBUI.getMaudeProcess(),
						JBUI.getJSONModuleTextContent(), JBUI.getNPAModuleFile(), mProtocolModuleFile))
				{
					Alert alert = new Alert(AlertType.WARNING);
					alert.setContentText("Sorry, couldn't start the initial conversation with Maude");
					alert.showAndWait();
				}

				return;
			}

			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText(
					"There is one or more missing general Maude paths. Please, configure them via the \"File > Set Maude paths...\" menu.");
			alert.showAndWait();
		}
	}

	@FXML
	public void initialize()
	{
		mProtocolModulePath.textProperty()
				.addListener((observable, oldPath, newPath) -> validateProtocolModulePath(newPath));

		if (mProtocolModuleFile != null)
		{
			mProtocolModulePath.setText(mProtocolModuleFile.getAbsolutePath());
		}
	}

	@FXML
	private void onProtocolModuleSearchBtnClick(ActionEvent event)
	{
		mProtocolModuleFile = JBUI.showMaudePathDialog(mProtocolModulePath, mLastProtocolModuleDirectory);
		mLastProtocolModuleDirectory = JBUI.handlePathDialogResult(mProtocolModulePath, mProtocolModuleFile,
				mLastProtocolModuleDirectory);
	}

	private void validateProtocolModulePath(String path)
	{
		mProtocolModuleStatus.setText("OK");
		mProtocolModuleStatus.setStyle("-fx-text-fill: green;");
	}
}
