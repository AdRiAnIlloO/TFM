package jbui.model;

import org.json.JSONArray;

import jbui.JBUI;

class JSONRunCommand extends AnswerableMaudeCommand
{
	// Used to infer safe limit states (those which would not produce valid messages
	// when searching further)
	private final int mDepth;
	private IdSystemNode mStartNode;

	JSONRunCommand(IdSystemNode startNode, int attackId, int depth, String startIdText)
	{
		super("red in MAUDE-NPA-JSON : runJSON[%s](%d, %d) .", startIdText, attackId, depth);
		mStartNode = startNode;
		mDepth = depth;
	}

	JSONRunCommand(int attackId, int depth)
	{
		this(JBUI.getMaudeThinker().mRootIdSystemNode, attackId, depth, "1");
		assert (JBUI.getMaudeThinker().mRootIdSystemNode != null);
	}

	@Override
	void abortOnProtocolLaunch()
	{
		mIsAborted = true;
	}

	@Override
	boolean checkAnswer(String line)
	{
		String prefix = "result String: ";

		if (!line.startsWith(prefix))
		{
			return false;
		}
		else if (!mIsAborted)
		{
			if (mStartNode == null)
			{
				mStartNode = JBUI.getMaudeThinker().mRootIdSystemNode;
			}

			// Get raw message and unescape trailing/ending/data quotes for valid JSON input
			String jsonText = line.substring(prefix.length());
			jsonText = jsonText.replaceAll("^\"|\"$", "").replace("\\", "");

			try
			{
				JSONArray jsonIdSystemArray = new JSONArray(jsonText);
				IdSystemNode.parseJSONIdSystemArray(mStartNode, mDepth, jsonIdSystemArray, false);
				JBUI.getMainController().tryAutoSaveCurrentProtocol();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return true;
	}

	@Override
	boolean mustVanishOnProtocolLaunch()
	{
		return true;
	}
}
