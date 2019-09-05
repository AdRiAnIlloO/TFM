package jbui.persistence;

import java.io.File;
import java.io.FileWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jbui.JBUI;
import jbui.model.IdSystemNode;

abstract class JSONTreeSaveDAO extends ResultsHandlingDAO
{
	private final String mJSONTreeText;
	private final File mTreeSaveFile;

	JSONTreeSaveDAO(IdSystemNode rootNode, File treeSaveFile) throws JSONException
	{
		mTreeSaveFile = treeSaveFile;
		JSONArray jsonTreeLevels = new JSONArray();
		recursivelyFillJSONTreeArray(rootNode, jsonTreeLevels, 0);
		mJSONTreeText = jsonTreeLevels.toString(2);
	}

	@Override
	void executeIO() throws Exception
	{
		try (FileWriter writer = new FileWriter(mTreeSaveFile))
		{
			writer.write(mJSONTreeText);
		}
	}

	private void recursivelyFillJSONTreeArray(IdSystemNode current, JSONArray jsonTreeLevels, int level)
			throws JSONException
	{
		if (level >= jsonTreeLevels.length())
		{
			JSONArray levelNodesJSONArray = new JSONArray();
			jsonTreeLevels.put(levelNodesJSONArray);
		}

		JSONObject jsonIdSystem = new JSONObject();
		jsonIdSystem.put("notes", current.mNotes);
		jsonIdSystem.put("isFolded", current.mUIController.mScreenNode.isFolded());
		jsonIdSystem.put("isInitial", current.mStateType == IdSystemNode.StateType.Initial);
		JSONArray jsonFullId = new JSONArray();
		current.outputIdAsJSONArray(jsonFullId);
		jsonIdSystem.put("id", jsonFullId);
		JSONObject jsonSystem = new JSONObject();
		JSONArray jsonStrands = new JSONArray();
		jsonSystem.put("intruderKnowledge", current.mIntruderKnowledge);
		jsonSystem.put("ghostList", current.mGhosts);
		jsonSystem.put("props", current.mPropertiesText);
		JSONArray jsonMsgElemSequences = new JSONArray();
		jsonSystem.put("msgSeqList", jsonMsgElemSequences);
		jsonSystem.put("strandSet", jsonStrands);
		jsonIdSystem.put("system", jsonSystem);
		jsonTreeLevels.getJSONArray(level).put(jsonIdSystem);

		if (JBUI.getMainController().mSelectedNodeController == current.mUIController)
		{
			jsonIdSystem.put("isSelected", true);
		}

		for (IdSystemNode.Strand strand : current.mStrands)
		{
			JSONObject jsonStrand = new JSONObject();
			jsonStrand.put("signature", strand.mSignature);
			jsonStrand.put("unknownSMsgs", strand.mUnknownSMsgs);
			jsonStrand.put("knownSMsgs", strand.mKnownSMsgs);
			jsonStrands.put(jsonStrand);
		}

		for (IdSystemNode.MsgElement msgElem : current.mMsgElemSequences)
		{
			JSONObject jsonMsgElem = new JSONObject();
			jsonMsgElem.put("isSend", msgElem.mIsSend);
			jsonMsgElem.put("msg", msgElem.mMsg);
			jsonMsgElem.put("signature", msgElem.mSignature);
			jsonMsgElem.put("isInIntruderK", msgElem.mIsInSelfIntruderKnowledge);
			jsonMsgElem.put("isChannelMsg", msgElem.mIsChannelMsg);
			jsonMsgElemSequences.put(jsonMsgElem);
		}

		for (IdSystemNode child : current.mChildren)
		{
			recursivelyFillJSONTreeArray(child, jsonTreeLevels, level + 1);
		}
	}

	boolean replace(JSONTreeSaveDAO other)
	{
		return other.mTreeSaveFile.equals(mTreeSaveFile);
	}
}
