package jbui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import jbui.JBUI;
import jbui.model.ProtocolInfoCommand;

public class ProtocolInitController extends LoadablesController
{
	private enum ModuleType
	{
		AttackIds
	}

	@FXML
	private ChoiceBox<Integer> mAttackStateIds;

	@FXML
	private Label mAttackStateIdsStatus;

	@FXML
	private TextField mDepth;

	public void fillAttackStateIds(int count)
	{
		if (mDialogPane.isVisible())
		{
			if (count > 0)
			{
				for (int i = 1; i < count; i++)
				{
					mAttackStateIds.getItems().add(i);
				}

				handleModuleLoadOkWhileVisible(mAttackStateIdsStatus, ModuleType.AttackIds);
				return;
			}

			handleModuleLoadErrorWhileVisible(mAttackStateIdsStatus, ModuleType.AttackIds);
			setOkButtonDisable(false);
		}
	}

	@Override
	void handleClose(ButtonType buttonType)
	{
		if (buttonType == ButtonType.OK)
		{
			int depth;

			try
			{
				depth = Integer.parseInt(mDepth.getText());
			}
			catch (NumberFormatException e)
			{
				depth = 0;
			}

			JBUI.getMaudeThinker().setInitialProtocolInfo(mAttackStateIds.getSelectionModel().getSelectedItem(), depth);
			return;
		}

		JBUI.getMaudeThinker().setInitialProtocolInfo(0, 0);
	}

	@Override
	void postInitialize()
	{
		Validation.makeNumeric(mDepth);
		handleModulePathChange(mAttackStateIdsStatus);
		mAttackStateIds.getItems().add(0);
		mAttackStateIds.getSelectionModel().select(0);
		ProtocolInfoCommand command = new ProtocolInfoCommand(this);
		JBUI.getMaudeThinker().mMaudeInCommands.add(command);
	}
}
