package jbui.controller;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser.ExtensionFilter;
import jbui.JBUI;
import jbui.persistence.DAO;
import jbui.persistence.JSONTreeLoadDAO;
import jbui.persistence.ProtocolModuleLoadDAO;

public class ProtocolPathController extends LoadablesController
{
	private enum ModuleType
	{
		JSONTreeFile, Protocol
	}

	@FXML
	private ChoiceBox<?> mAttackIds;

	private JSONArray mJSONTreeArray;

	// Last directories upon file search dialog closes
	private File mLastProtocolModuleDirectory;
	private File mLastTreeLoadFileDirectory;

	private File mProtocolModuleFile;

	@FXML
	private TextField mProtocolModulePath;

	@FXML
	private Label mProtocolModuleStatus;

	private String mProtocolModuleTextInput;

	@FXML
	private TextField mTreeLoadFilePath;

	@FXML
	private Label mTreeLoadFileStatus;

	public ProtocolPathController()
	{
		mProtocolModuleFile = JBUI.sInstance.mProtocolModuleFile;
		mLastTreeLoadFileDirectory = JBUI.sInstance.mLastTreeSaveDirectory;

		if (mProtocolModuleFile != null)
		{
			mLastProtocolModuleDirectory = mProtocolModuleFile.getParentFile();
		}
		else if (JBUI.getMaudeBinFile() != null)
		{
			// Fallback to the Maude bin directory, as it may be near the protocol one
			mLastProtocolModuleDirectory = JBUI.getMaudeBinFile().getParentFile();
		}

		if (mLastTreeLoadFileDirectory == null)
		{
			// Fallback to the calculated last protocol directory
			mLastTreeLoadFileDirectory = mLastProtocolModuleDirectory;
		}
	}

	@Override
	void handleClose(ButtonType buttonType)
	{
		if (buttonType == ButtonType.OK)
		{
			JBUI.sInstance.mProtocolModuleFile = mProtocolModuleFile;
			String title = String.format(JBUI.sInstance.DEFAULT_APP_TITLE + " [%s]",
					mProtocolModuleFile.getAbsolutePath());
			JBUI.sInstance.mStage.setTitle(title);
			JBUI.getMainController().disableProtocolSaving();

			if (!JBUI.getMaudeThinker().tryLaunchProtocolNow(JBUI.getMaudeBinFile(), mProtocolModuleTextInput,
					mJSONTreeArray))
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

	public void handleTreeFileLoad(String jsonTreeText)
	{
		if (mDialogPane.isVisible())
		{
			try
			{
				mJSONTreeArray = new JSONArray(jsonTreeText);
				handleModuleLoadOkWhileVisible(mTreeLoadFileStatus, ModuleType.JSONTreeFile);
			}
			catch (JSONException e)
			{
				handleModuleLoadErrorWhileVisible(mTreeLoadFileStatus, ModuleType.JSONTreeFile);
			}
		}
	}

	@FXML
	private void onProtocolModuleSearchBtnClick(ActionEvent event)
	{
		File protocolModuleFile = showMaudePathLoadDialog(mLastProtocolModuleDirectory);

		if (protocolModuleFile != null)
		{
			mLastProtocolModuleDirectory = handlePathDialogResult(mProtocolModulePath, protocolModuleFile);
			mProtocolModuleFile = protocolModuleFile;
			handleProtocolModulePathChange();
		}
	}

	@FXML
	private void onTreePathPickBtnClick(ActionEvent event)
	{
		ExtensionFilter filter = new ExtensionFilter("JSON files", "*.json");
		File treeLoadFile = showPathLoadDialog(filter, mLastTreeLoadFileDirectory);

		if (treeLoadFile != null)
		{
			mLastTreeLoadFileDirectory = handlePathDialogResult(mTreeLoadFilePath, treeLoadFile);
			DAO dao = new JSONTreeLoadDAO(this, treeLoadFile);
			handleModulePathChange(mTreeLoadFileStatus, dao);
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
