package jbui.model;

import jbui.controller.ProtocolInitController;

public class ProtocolInfoCommand extends AnswerableMaudeCommand
{
	private ProtocolInitController mController;

	public ProtocolInfoCommand(ProtocolInitController controller)
	{
		super("red in MAUDE-NPA-JSON : countAttackStates .");
		mController = controller;
	}

	@Override
	boolean checkAnswer(String line)
	{
		String prefix = "result NzNat: ";

		if (line.startsWith(prefix))
		{
			String attackStatesText = line.substring(prefix.length());
			int attackStatesAmount = Integer.parseInt(attackStatesText);
			mController.fillAttackStateIds(attackStatesAmount);
			return true;
		}

		return false;
	}
}
