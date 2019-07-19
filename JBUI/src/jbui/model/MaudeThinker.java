package jbui.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import jbui.JBUI;
import jbui.persistence.DAOThread;

public class MaudeThinker extends AnimationTimer
{
	private enum State
	{
		Initial, Processing, RefreshingCanvas
	}

	private BufferedReader mBufferedReader;
	private BufferedWriter mBufferedWriter;

	// To be used if user selected a new NPA module, but not a new process,
	// and confirms general paths alert. Also to destroy() it, to prevent leaking.
	public Process mCurMaudeProcess;

	// To be used if user selected a new process, but not a new NPA module,
	// and confirms general paths alert
	public String mCurNPAModuleTextInput;

	public DAOThread mDAOThread;
	public String mJSONModuleTextInput;

	// To be used if user selected a new process, and confirms general paths
	// alert when no protocol is launched yet or a new protocol launches
	private Process mNextMaudeProcess;

	// To be used if user selected a new NPA module, and confirms general paths
	// alert when no protocol is launched yet or a new protocol launches
	public String mNextNPAModuleTextInput;

	private IdSystemNode mRootIdSystemNode;
	private State mState;

	public MaudeThinker()
	{
		mState = State.Initial;
		mDAOThread = new DAOThread();
		Thread thread = new Thread(mDAOThread);
		thread.setDaemon(true);
		thread.start();
	}

	private void checkForMaudeIdSystemAnswer(String line) throws IOException, JSONException
	{
		System.out.println(line);
		String prefix = "result String: ";

		if (line.startsWith(prefix))
		{
			// Get raw message and unescape trailing/ending/data quotes for valid JSON input
			String jsonText = line.substring(prefix.length());
			jsonText = jsonText.replaceAll("^\"|\"$", "").replace("\\", "");

			JSONArray jsonIdSystemArray = new JSONArray(jsonText);

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

	@Override
	public void handle(long now)
	{
		mDAOThread.think();

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
				for (; mBufferedReader.ready(); checkForMaudeIdSystemAnswer(mBufferedReader.readLine()));
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

	public void setNextMaudeProcess(Process maudeProcess)
	{
		if (mNextMaudeProcess != null)
		{
			mNextMaudeProcess.destroy();
		}

		mNextMaudeProcess = maudeProcess;
	}

	private void talkToMaude(String command, Object... args) throws Exception
	{
		tryPrintMaudeAnswer();
		command = String.format(command, args);
		mBufferedWriter.write(command);
		mBufferedWriter.newLine();
		mBufferedWriter.flush();
		tryPrintMaudeAnswer();
	}

	public boolean tryLaunchProtocolNow(File maudeBinFile, String protocolModuleTextInput)
	{
		try
		{
			if (mRootIdSystemNode != null)
			{
				mRootIdSystemNode.removeChildrenFromModelAndGridPane();
			}
			else
			{
				// We show the initial node, which is not an attack state.
				// Maude-NPA shares this idea, such that no returned state can be the root.
				mRootIdSystemNode = new IdSystemNode("1", "");
			}

			updateMainComponents();
			talkToMaude(protocolModuleTextInput);
			talkToMaude("red in MAUDE-NPA-JSON : runJSON(0, 1) .");
			talkToMaude("red in MAUDE-NPA-JSON : runJSON(0, 2) .");
			mState = State.Processing;
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			mState = State.Initial;
			return false;
		}
	}

	private void tryPrintMaudeAnswer() throws Exception
	{
		while (mBufferedReader.ready())
		{
			System.out.println(mBufferedReader.readLine());
		}
	}

	public void tryUpdateMainComponents()
	{
		if (mState == State.Initial)
		{
			try
			{
				updateMainComponents();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void updateMainComponents() throws Exception
	{
		if (mNextMaudeProcess != null)
		{
			// If there is current process, destroy() it to prevent leaking
			if (mCurMaudeProcess != null)
			{
				mCurMaudeProcess.destroy();
			}

			// Update Process pointers
			mCurMaudeProcess = mNextMaudeProcess;
			mNextMaudeProcess = null;

			// Hook I/O
			InputStream inputStream = mCurMaudeProcess.getInputStream();
			InputStreamReader inputReader = new InputStreamReader(inputStream);
			mBufferedReader = new BufferedReader(inputReader);
			OutputStream outputStream = mCurMaudeProcess.getOutputStream();
			OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
			mBufferedWriter = new BufferedWriter(outputWriter);

			// Load Maude Modules on subprocess/Shell
			if (mNextNPAModuleTextInput != null)
			{
				talkToMaude(mNextNPAModuleTextInput);
				mCurNPAModuleTextInput = mNextNPAModuleTextInput;
				mNextNPAModuleTextInput = null;
			}
			else if (mCurNPAModuleTextInput != null)
			{
				talkToMaude(mCurNPAModuleTextInput);

			}

			talkToMaude(mJSONModuleTextInput);
		}
		else if (mNextNPAModuleTextInput != null)
		{
			// To avoid risks, don't reload NPA module on current process
			mCurNPAModuleTextInput = mNextNPAModuleTextInput;
		}
	}
}
