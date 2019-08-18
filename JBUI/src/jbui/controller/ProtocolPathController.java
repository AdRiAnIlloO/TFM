package jbui.controller;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import jbui.JBUI;
import jbui.persistence.DAO;
import jbui.persistence.ProtocolModuleLoadDAO;

public class ProtocolPathController extends LoadablesController
{
	private enum ModuleType
	{
		Protocol
	}

	@FXML
	private ChoiceBox<?> mAttackIds;

	// Last directory upon file search dialog close
	private File mLastProtocolModuleDirectory;

	private File mProtocolModuleFile;

	@FXML
	private TextField mProtocolModulePath;

	@FXML
	private Label mProtocolModuleStatus;

	private String mProtocolModuleTextInput;

	public ProtocolPathController()
	{
		mProtocolModuleFile = JBUI.sInstance.mProtocolModuleFile;

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
	void handleClose(ButtonType buttonType)
	{
		if (buttonType == ButtonType.OK)
		{
			JBUI.sInstance.mProtocolModuleFile = mProtocolModuleFile;

			if (!JBUI.getMaudeThinker().tryLaunchProtocolNow(JBUI.getMaudeBinFile(), mProtocolModuleTextInput))
			{
				Alert alert = new Alert(AlertType.WARNING);
				alert.setContentText("Sorry, couldn't start the initial conversation with Maude");
				alert.showAndWait();
			}
			else
			{
				LoadablesAlert<ProtocolInitController> alert = new LoadablesAlert<>("Protocol initialization",
						String.format("Initialize the protocol '%s'", mProtocolModuleFile.getAbsolutePath()),
						"view/ProtocolInitWindow.fxml");
				alert.showAndWaitAndHandle();
			}
		}
	}

	public void handleProtocolModuleLoad(String protocolModuleTextInput)
	{
		if (handleModuleLoadOk(mProtocolModuleStatus, ModuleType.Protocol))
		{
			mProtocolModuleTextInput = protocolModuleTextInput;
		}
	}

	private void handleProtocolModulePathChange()
	{
		DAO dao = new ProtocolModuleLoadDAO(this, mProtocolModuleFile);
		handleModulePathChange(mProtocolModuleStatus, dao);
	}

	@FXML
	private void onProtocolModuleSearchBtnClick(ActionEvent event)
	{
		File protocolModuleFile = showMaudePathDialog(mProtocolModulePath, mLastProtocolModuleDirectory);

		if (protocolModuleFile != null)
		{
			mLastProtocolModuleDirectory = handlePathDialogResult(mProtocolModulePath, protocolModuleFile,
					mLastProtocolModuleDirectory);
			mProtocolModuleFile = protocolModuleFile;
			handleProtocolModulePathChange();
		}
	}

	@Override
	void postInitialize()
	{
		if (mProtocolModuleFile != null)
		{
			mProtocolModulePath.setText(mProtocolModuleFile.getAbsolutePath());
			handleProtocolModulePathChange();
		}
	}
}
