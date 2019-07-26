package jbui.model;

import java.io.BufferedReader;

abstract class AnswerableMaudeCommand extends MaudeCommand
{
	/**
	 * Whether the "main" operation of this command should be skipped
	 * 
	 * Supplied that the "main" operation of a command awaiting answer is not
	 * desired anymore, setting this condition instead of skipping the whole object
	 * at MaudeThinker allows this command to apply still small operations specific
	 * to this command (e.g. logging) when the expected Maude answer is ready
	 */
	boolean mIsAborted;

	AnswerableMaudeCommand(String commandText, Object... args)
	{
		super(commandText, args);
	}

	void abortOnProtocolLaunch()
	{
		return;
	}

	@Override
	boolean checkAnswer(BufferedReader bufferedReader)
	{
		boolean isExpectedAnswerDetected = false;

		try
		{
			// Read all ready lines while checking answer, even if the checking succeeds
			for (; bufferedReader.ready();)
			{
				String line = bufferedReader.readLine();
				System.out.println(line);

				if (!isExpectedAnswerDetected)
				{
					isExpectedAnswerDetected = checkAnswer(line);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return isExpectedAnswerDetected;
	}

	abstract boolean checkAnswer(String line);

	@Override
	AnswerableMaudeCommand toAnswerable()
	{
		return this;
	}
}
