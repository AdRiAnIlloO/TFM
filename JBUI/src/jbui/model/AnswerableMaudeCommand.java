package jbui.model;

import java.io.BufferedReader;

abstract class AnswerableMaudeCommand extends MaudeCommand
{
	AnswerableMaudeCommand(String commandText, Object... args)
	{
		super(commandText, args);
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
