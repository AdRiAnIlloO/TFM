package jbui.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.Queue;

import javafx.animation.AnimationTimer;
import jbui.JBUI;
import jbui.persistence.DAOThread;

public class MaudeThinker extends AnimationTimer
{
	private enum State
	{
		Initial, Processing
	}

	private int mAttackStateId;
	private BufferedReader mBufferedReader;
	private BufferedWriter mBufferedWriter;

	// To be used if user selected a new NPA module, but not a new process,
	// and confirms general paths alert. Also to destroy() it, to prevent leaking.
	private Process mCurMaudeProcess;

	// To be used if user selected a new process, but not a new NPA module,
	// and confirms general paths alert
	private String mCurNPAModuleTextInput;

	public DAOThread mDAOThread;
	public String mJSONModuleTextInput;
	public Queue<MaudeCommand> mMaudeInCommands;
	private AnswerableMaudeCommand mMaudeOutCommand;

	// To be used if user selected a new process, and confirms general paths
	// alert when no protocol is launched yet or a new protocol launches
	private Process mNextMaudeProcess;

	// To be used if user selected a new NPA module, and confirms general paths
	// alert when no protocol is launched yet or a new protocol launches
	public String mNextNPAModuleTextInput;

	IdSystemNode mRootIdSystemNode;
	private State mState;

	public MaudeThinker()
	{
		mState = State.Initial;
		mMaudeInCommands = new LinkedList<>();
		mDAOThread = new DAOThread();
		Thread thread = new Thread(mDAOThread);
		thread.setDaemon(true);
		thread.start();
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
			// Process pending Maude commands.
			// For security, we won't process a command until we fully process current.
			for (;;)
			{
				// Check cached pending command response
				if (mMaudeOutCommand != null)
				{
					if (!mMaudeOutCommand.checkAnswer(mBufferedReader))
					{
						// Since computations can take long we stop listening until next tick
						break;
					}

					mMaudeOutCommand = null;
				}

				// Check pending commands to send
				if (mMaudeInCommands.isEmpty())
				{
					break;
				}

				try
				{
					mMaudeInCommands.peek().send(mBufferedWriter);
					mMaudeOutCommand = mMaudeInCommands.remove().toAnswerable();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			break;
		}
		}
	}

	public void performGuidedSearch(int depth, IdSystemNode node)
	{
		for (int auxDepth = 1; auxDepth <= depth; auxDepth++)
		{
			MaudeCommand command = new JSONRunCommand(mAttackStateId, node.getDepth() + auxDepth,
					node.unparseIdSpaced());
			mMaudeInCommands.add(command);
		}
	}

	public void setInitialProtocolInfo(int attackStateId, int depth)
	{
		mAttackStateId = attackStateId;

		for (int i = 0; i <= depth; i++)
		{
			MaudeCommand command = new JSONRunCommand(attackStateId, i);
			mMaudeInCommands.add(command);
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

	public boolean tryLaunchProtocolNow(File maudeBinFile, String protocolModuleTextInput)
	{
		try
		{
			if (mRootIdSystemNode != null)
			{
				JBUI.getMainController().clearTree(mRootIdSystemNode.mUIController);
				mRootIdSystemNode = null;
			}

			// Try mark answerable command as aborted
			if (mMaudeOutCommand != null)
			{
				mMaudeOutCommand.abortOnProtocolLaunch();
			}

			// Remove no longer desired commands
			mMaudeInCommands.removeIf(command -> command.mustVanishOnProtocolLaunch());

			updateMainComponents();
			MaudeCommand command = new MaudeCommand(protocolModuleTextInput);
			mMaudeInCommands.add(command);
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

	public void tryUpdateMainComponents()
	{
		if (mState == State.Initial)
		{
			try
			{
				updateMainComponents();

				if (mCurNPAModuleTextInput != null)
				{
					JBUI.getMainController().unblockProtocolLaunches();
				}
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

			// Request loading Maude Modules on subprocess/Shell, if suitable
			if (mNextNPAModuleTextInput != null)
			{
				mCurNPAModuleTextInput = mNextNPAModuleTextInput;
				mNextNPAModuleTextInput = null;
			}

			if (mCurNPAModuleTextInput != null)
			{
				MaudeCommand command = new MaudeCommand(mCurNPAModuleTextInput);
				mMaudeInCommands.add(command);
				command = new MaudeCommand(mJSONModuleTextInput);
				mMaudeInCommands.add(command);
			}
		}
		else if (mNextNPAModuleTextInput != null)
		{
			// To avoid risks, don't reload NPA module on current process
			mCurNPAModuleTextInput = mNextNPAModuleTextInput;
		}
	}
}
