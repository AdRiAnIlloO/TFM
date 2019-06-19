package jbui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.animation.AnimationTimer;

public class MaudeThinker extends AnimationTimer
{
	public enum State
	{
		Initial, CheckingRequirements, Processing
	}

	public Process mMaudeProcess;
	public State mState;
	private BufferedReader mBufferedReader;
	private BufferedWriter mBufferedWriter;

	// We'll work with relative paths from the Maude executable,
	// to deal with some cases where the CWD is translated to a
	// different filesystem than host's (e.g. Windows + Cygwin).
	// This causes only relative paths succeed in loading modules.
	private String mRelJSONModulePath;
	private String mRelNPAModulePath;
	private String mRelProtocolModulePath;

	protected MaudeThinker()
	{
		mMaudeProcess = null;
		mState = State.Initial;
		mBufferedReader = null;
		mBufferedWriter = null;
		mRelJSONModulePath = null;
		mRelNPAModulePath = null;
		mRelProtocolModulePath = null;
	}

	@Override
	public void handle(long now)
	{
		switch (mState)
		{
		case CheckingRequirements:
		{
			if (mMaudeProcess != null && JBUI.getJSONModulePath() != null && JBUI.getNPAModulePath() != null
					&& JBUI.getProtocolModulePath() != null)
			{
				File maudeBinFile = new File(JBUI.sInstance.mMaudeBinPath);
				mRelJSONModulePath = relativizePathFromMaudeBin(maudeBinFile, JBUI.getJSONModulePath());
				mRelNPAModulePath = relativizePathFromMaudeBin(maudeBinFile, JBUI.getNPAModulePath());
				mRelProtocolModulePath = relativizePathFromMaudeBin(maudeBinFile, JBUI.getProtocolModulePath());

				try
				{
					InputStream inputStream = mMaudeProcess.getInputStream();
					InputStreamReader inputReader = new InputStreamReader(inputStream);
					mBufferedReader = new BufferedReader(inputReader);
					OutputStream outputStream = mMaudeProcess.getOutputStream();
					OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
					mBufferedWriter = new BufferedWriter(outputWriter);

					// Start initial talk with Maude
					talkToMaude("load %s .", mRelNPAModulePath);
					talkToMaude("load %s .", mRelJSONModulePath);
					talkToMaude("load %s .", mRelProtocolModulePath);
					talkToMaude("red in MAUDE-NPA-JSON : runJSON(0, 1) .");

					mState = State.Processing;
				}
				catch (IOException e)
				{
					e.printStackTrace();
					mState = State.Initial;
				}
			}

			break;
		}
		case Processing:
		{
			try
			{
				if (mBufferedReader.ready())
				{
					String line = mBufferedReader.readLine();
					System.out.println(line);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			break;
		}
		}
	}

	private String relativizePathFromMaudeBin(File maudeBinFile, String pathName)
	{
		String maudeBinDir = maudeBinFile.getParent();
		Path path = Paths.get(pathName);
		return Paths.get(maudeBinDir).relativize(path).toString();
	}

	private void talkToMaude(String command, String... args) throws IOException
	{
		command = String.format(command, args);
		mBufferedWriter.write(command);
		mBufferedWriter.newLine();
		mBufferedWriter.flush();
	}
}
