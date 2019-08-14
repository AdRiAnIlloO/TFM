package jbui.model;

import java.io.BufferedReader;
import java.io.IOException;

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
		try
		{
			if (bufferedReader.ready())
			{
				String answer = "";

				// Read all ready input, even if it is exceeds the expected substring
				do
				{
					char[] charBuf = new char[1];
					bufferedReader.read(charBuf);
					answer += charBuf[0];
				}
				while (bufferedReader.ready());

				System.out.println(answer);
				String[] lines = answer.split("\n");

				for (String line : lines)
				{
					if (checkAnswer(line))
					{
						return true;
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return false;
	}

	abstract boolean checkAnswer(String line);

	@Override
	AnswerableMaudeCommand toAnswerable()
	{
		return this;
	}
}
