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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;

public class MaudeThinker extends AnimationTimer
{
	private enum State
	{
		Initial, Processing, RefreshingCanvas
	}

	private BufferedReader mBufferedReader;
	private BufferedWriter mBufferedWriter;
	private Process mMaudeProcess;

	// We'll work with relative paths from the Maude executable,
	// to deal with some cases where the CWD is translated to a
	// different filesystem than host's (e.g. Windows + Cygwin).
	// This causes only relative paths succeed in loading modules.
	private String mRelJSONModulePath;
	private String mRelNPAModulePath;

	private String mRelProtocolModulePath;
	private IdSystemNode mRootIdSystemNode;
	private State mState;

	protected MaudeThinker()
	{
		mState = State.Initial;
	}

	@Override
	public void handle(long now)
	{
		switch (mState)
		{
		case Initial:
		{
			break;
		}
		case Processing:
		{
			try
			{
				while (mBufferedReader.ready())
				{
					tryCheckMaudeAnswer();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			if (mRootIdSystemNode != null)
			{
				mState = State.RefreshingCanvas;
			}

			break;
		}
		case RefreshingCanvas:
		{
			GraphicsContext ctx = JBUI.getDrawingCanvas().getGraphicsContext2D();
			ctx.clearRect(0, 0, ctx.getCanvas().getWidth(), ctx.getCanvas().getHeight());
			mRootIdSystemNode.drawArcs(mRootIdSystemNode, JBUI.getDrawingCanvas().getGraphicsContext2D());
			mState = State.Processing;
			break;
		}
		}
	}

	private String relativizePathFromMaudeBin(File maudeBinFile, File interestFile)
	{
		String absPathName = interestFile.getAbsolutePath();

		try
		{
			String maudeBinDir = maudeBinFile.getParent();
			Path path = Paths.get(absPathName);
			return Paths.get(maudeBinDir).relativize(path).toString();
		}
		catch (Exception e)
		{
			return absPathName;
		}
	}

	private void talkToMaude(String command, Object... args) throws IOException
	{
		command = String.format(command, args);
		mBufferedWriter.write(command);
		mBufferedWriter.newLine();
		mBufferedWriter.flush();
	}

	private void tryCheckMaudeAnswer() throws IOException, JSONException
	{
		String line = mBufferedReader.readLine();
		System.out.println(line);
		String prefix = "result String: ";

		if (line.startsWith(prefix))
		{
			// Get raw message and unescape trailing/ending/data quotes for valid JSON input
			String jsonText = line.substring(prefix.length());
			jsonText = jsonText.replaceAll("^\"|\"$", "").replace("\\", "");

			JSONArray jsonIdSystemArray = new JSONArray(jsonText);

			if (mRootIdSystemNode == null)
			{
				mRootIdSystemNode = new IdSystemNode("1", "");
			}

			for (int i = 0; i < jsonIdSystemArray.length(); i++)
			{
				JSONObject jsonIdSystem = jsonIdSystemArray.getJSONObject(i);
				String idText = jsonIdSystem.getString("id");
				String msg = jsonIdSystem.getString("msg");
				IdSystemNode child = new IdSystemNode(idText, msg);
				mRootIdSystemNode.insert(child);
			}

			mRootIdSystemNode.addToGridPane(0, 0, null);
		}
	}

	public boolean tryInitializeNow(File maudeBinFile, Process maudeProcess, File jsonModuleFile, File npaModuleFile,
			File protocolModuleFile)
	{
		if (maudeProcess != mMaudeProcess)
		{
			if (mMaudeProcess != null)
			{
				mMaudeProcess.destroy();
			}

			mMaudeProcess = maudeProcess;
		}

		mRelJSONModulePath = relativizePathFromMaudeBin(maudeBinFile, JBUI.getJSONModuleFile());
		mRelNPAModulePath = relativizePathFromMaudeBin(maudeBinFile, JBUI.getNPAModuleFile());
		mRelProtocolModulePath = relativizePathFromMaudeBin(maudeBinFile, JBUI.getProtocolModuleFile());

		try
		{
			InputStream inputStream = mMaudeProcess.getInputStream();
			InputStreamReader inputReader = new InputStreamReader(inputStream);
			mBufferedReader = new BufferedReader(inputReader);
			OutputStream outputStream = mMaudeProcess.getOutputStream();
			OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
			mBufferedWriter = new BufferedWriter(outputWriter);
			mRootIdSystemNode = null;

			// Start initial talk with Maude
			talkToMaude("load %s .", mRelNPAModulePath);
			talkToMaude("load %s .", mRelJSONModulePath);
			talkToMaude("load %s .", mRelProtocolModulePath);
			talkToMaude("red in MAUDE-NPA-JSON : runJSON(0, 1) .");
			talkToMaude("red in MAUDE-NPA-JSON : runJSON(0, 2) .");

			mState = State.Processing;
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			mState = State.Initial;
			return false;
		}
	}
}
