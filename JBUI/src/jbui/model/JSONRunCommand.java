package jbui.model;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jbui.JBUI;

class JSONRunCommand extends AnswerableMaudeCommand
{
	JSONRunCommand(int attackId, int depth)
	{
		this(attackId, depth, "1");
	}

	JSONRunCommand(int attackId, int depth, String startIdText)
	{
		super("red in MAUDE-NPA-JSON : runJSON[%s](%d, %d) .", startIdText, attackId, depth);
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
			// Get raw message and unescape trailing/ending/data quotes for valid JSON input
			String jsonText = line.substring(prefix.length());
			jsonText = jsonText.replaceAll("^\"|\"$", "").replace("\\", "");

			try
			{
				JSONArray jsonIdSystemArray = new JSONArray(jsonText);

				for (int i = 0; i < jsonIdSystemArray.length(); i++)
				{
					JSONObject jsonIdSystem = jsonIdSystemArray.getJSONObject(i);
					createAndInsertNode(jsonIdSystem);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return true;
	}

	private void createAndInsertNode(JSONObject jsonIdSystem) throws JSONException
	{
		JSONArray jsonId = jsonIdSystem.getJSONArray("id");
		LinkedList<IdElem> idElems = new LinkedList<>();

		for (int i = 0; i < jsonId.length(); i++)
		{
			JSONObject jsonIdElem = jsonId.getJSONObject(i);
			int idElemNum = jsonIdElem.getInt("elem");
			int subIdElemNum = jsonIdElem.optInt("subElem");
			IdElem idElem = new IdElem(idElemNum, subIdElemNum);
			idElems.add(idElem);
		}

		IdSystemNode node = new IdSystemNode(idElems.getLast(), jsonIdSystem);

		if (idElems.size() < 2)
		{
			assert (JBUI.getMaudeThinker().mRootIdSystemNode == null);
			JBUI.getMaudeThinker().mRootIdSystemNode = node;
			node.mUIController = JBUI.getMainController().createFXTreeLayout(node);
			return;
		}

		JBUI.getMaudeThinker().mRootIdSystemNode.insert(node, idElems);
	}

	@Override
	boolean mustVanishOnProtocolLaunch()
	{
		return true;
	}
}
