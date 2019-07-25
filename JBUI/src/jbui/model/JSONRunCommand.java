package jbui.model;

import org.json.JSONArray;
import org.json.JSONObject;

import jbui.JBUI;

class JSONRunCommand extends AnswerableMaudeCommand
{
	JSONRunCommand(int attackId, int depth)
	{
		super("red in MAUDE-NPA-JSON : runJSON(%d, %d) .", attackId, depth);
	}

	@Override
	boolean checkAnswer(String line)
	{
		String prefix = "result String: ";

		if (line.startsWith(prefix))
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
					String idText = jsonIdSystem.getString("id");
					String msg = jsonIdSystem.getString("msg");
					IdSystemNode node = new IdSystemNode(idText, msg);

					if (node.isRoot())
					{
						JBUI.getMaudeThinker().mRootIdSystemNode = node;
						continue;
					}

					JBUI.getMaudeThinker().mRootIdSystemNode.insert(node);
				}

				JBUI.getMaudeThinker().onIdSystemNodesAdded();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return true;
		}

		return false;
	}
}
