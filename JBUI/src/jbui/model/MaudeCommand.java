package jbui.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

class MaudeCommand
{
	private String mCommandText;

	MaudeCommand(String commandText, Object... args)
	{
		mCommandText = String.format(commandText, args);
	}

	/**
	 * @param bufferedReader
	 * @return True if the expected answer is ready and detected, false otherwise
	 */
	boolean checkAnswer(BufferedReader bufferedReader)
	{
		return true;
	}

	boolean mustVanishOnProtocolLaunch()
	{
		return false;
	}

	void send(BufferedWriter bufferedWriter) throws IOException
	{
		bufferedWriter.write(mCommandText);
		bufferedWriter.newLine();
		bufferedWriter.flush();
	}

	AnswerableMaudeCommand toAnswerable()
	{
		return null;
	}
}
