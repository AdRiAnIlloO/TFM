package jbui.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jbui.JBUI;
import jbui.controller.IdSystemNodeUIController;

public class IdSystemNode
{
	public class MsgElement
	{
		public final boolean mIsChannelMsg;
		public final boolean mIsInSelfIntruderKnowledge;
		public final boolean mIsSend;
		public final String mMsg;
		public final String mSignature;

		private MsgElement(JSONObject jsonMsgElem) throws JSONException
		{
			mIsSend = jsonMsgElem.getBoolean("isSend");
			mMsg = jsonMsgElem.getString("msg");
			mSignature = jsonMsgElem.getString("signature");
			mIsInSelfIntruderKnowledge = jsonMsgElem.getBoolean("isInIntruderK");
			mIsChannelMsg = jsonMsgElem.getBoolean("isChannelMsg");
		}
	}

	public static enum StateType
	{
		Default, Initial, LastReachable
	}

	public class Strand
	{
		public final List<String> mKnownSMsgs = new ArrayList<>();
		public final String mSignature;
		public final List<String> mUnknownSMsgs = new ArrayList<>();

		private Strand(JSONObject jsonStrandObj) throws JSONException
		{
			mSignature = jsonStrandObj.getString("signature");
			JSONArray knownSMsgsJSONArray = jsonStrandObj.getJSONArray("knownSMsgs");
			JSONArray unknownSMsgsJSONArray = jsonStrandObj.getJSONArray("unknownSMsgs");

			for (int i = 0; i < knownSMsgsJSONArray.length(); i++)
			{
				mKnownSMsgs.add(knownSMsgsJSONArray.getString(i));
			}

			for (int i = 0; i < unknownSMsgsJSONArray.length(); i++)
			{
				mUnknownSMsgs.add(unknownSMsgsJSONArray.getString(i));
			}
		}
	}

	static void parseJSONIdSystemArray(IdSystemNode startNode, int depth, JSONArray jsonIdSystemArray,
			boolean parseUIKeys) throws JSONException
	{
		if (depth < 1)
		{
			JSONObject jsonIdSystem = jsonIdSystemArray.getJSONObject(0);
			int idNumber = jsonIdSystem.getJSONArray("id").getJSONObject(0).getInt("elem");
			IdElem idElem = new IdElem(idNumber);
			IdSystemNode node = new IdSystemNode(idElem, jsonIdSystem);

			// This condition should be true if JSON tree loaded successfully from user file
			if (JBUI.getMaudeThinker().mRootIdSystemNode != null)
			{
				JBUI.getMaudeThinker().mRootIdSystemNode.mMsgElemSequences = node.mMsgElemSequences;
				JBUI.getMaudeThinker().mRootIdSystemNode.parseUIKeys(jsonIdSystem);
			}
			else
			{
				JBUI.getMaudeThinker().mRootIdSystemNode = node;
				node.mUIController = JBUI.getMainController().createFXTreeLayout(node);
				JBUI.getMainController().mTreeExportItem.setDisable(false);
			}
		}
		else
		{
			List<IdSystemNode> levelNodes = new ArrayList<>();

			for (int i = 0; i < jsonIdSystemArray.length(); i++)
			{
				JSONObject jsonIdSystem = jsonIdSystemArray.getJSONObject(i);
				JSONArray jsonId = jsonIdSystem.getJSONArray("id");
				LinkedList<IdElem> idElems = new LinkedList<>();

				for (int j = 0; j < jsonId.length(); j++)
				{
					JSONObject jsonIdElem = jsonId.getJSONObject(j);
					int idElemNum = jsonIdElem.getInt("elem");
					int subIdElemNum = jsonIdElem.optInt("subElem");
					IdElem idElem;

					if (subIdElemNum > 0)
					{
						idElem = new IdSubElem(idElemNum, subIdElemNum);
					}
					else
					{
						idElem = new IdElem(idElemNum);
					}

					idElems.add(idElem);
				}

				NonRootIdSystemNode node = new NonRootIdSystemNode(idElems.getLast(), jsonIdSystem);
				JBUI.getMaudeThinker().mRootIdSystemNode.insert(node, idElems);
				levelNodes.add(node);

				if (parseUIKeys)
				{
					node.parseUIKeys(jsonIdSystem);
				}
			}

			startNode.inferLastReachableNodes(levelNodes, depth, startNode.getDepth());
		}
	}

	public final List<IdSystemNode> mChildren = new ArrayList<>();
	public final List<String> mGhosts = new ArrayList<>();
	private final IdElem mIdElem;
	public final List<String> mIntruderKnowledge = new ArrayList<>();
	public List<MsgElement> mMsgElemSequences = new ArrayList<>();
	public String mNotes;
	public final String mPropertiesText;
	public StateType mStateType;
	public final List<Strand> mStrands = new ArrayList<>();
	public IdSystemNodeUIController mUIController;

	IdSystemNode(IdElem idElem, JSONObject jsonIdSystem) throws JSONException
	{
		if (jsonIdSystem.getBoolean("isInitial"))
		{
			mStateType = StateType.Initial;
		}
		else if (jsonIdSystem.optBoolean("isLastReachable"))
		{
			mStateType = StateType.LastReachable;
		}
		else
		{
			mStateType = StateType.Default;
		}

		mIdElem = idElem;
		JSONObject jsonSystem = jsonIdSystem.getJSONObject("system");
		JSONArray jsonStrandsArray = jsonSystem.getJSONArray("strandSet");
		JSONArray jsonIntruderKnowledgeArray = jsonSystem.getJSONArray("intruderKnowledge");
		JSONArray jsonMsgElemSeq = jsonSystem.getJSONArray("msgSeqList");
		JSONArray jsonGhostsArray = jsonSystem.getJSONArray("ghostList");
		mPropertiesText = jsonSystem.optString("props", null);

		for (int i = 0; i < jsonStrandsArray.length(); i++)
		{
			Strand strand = new Strand(jsonStrandsArray.getJSONObject(i));
			mStrands.add(strand);
		}

		for (int i = 0; i < jsonIntruderKnowledgeArray.length(); i++)
		{
			mIntruderKnowledge.add(jsonIntruderKnowledgeArray.getString(i));
		}

		for (int i = 0; i < jsonMsgElemSeq.length(); i++)
		{
			MsgElement msgElem = new MsgElement(jsonMsgElemSeq.getJSONObject(i));
			mMsgElemSequences.add(msgElem);
		}

		for (int i = 0; i < jsonGhostsArray.length(); i++)
		{
			mGhosts.add(jsonGhostsArray.getString(i));
		}
	}

	boolean equals(IdSystemNode node)
	{
		return mIdElem.equals(node.mIdElem);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof IdSystemNode)
		{
			return equals((IdSystemNode) obj);
		}

		return false;
	}

	int getDepth()
	{
		return 0;
	}

	public IdSystemNode getParent()
	{
		return null;
	}

	private void inferLastReachableNodes(List<IdSystemNode> levelNodes, int thresholdDepth, int curDept)
	{
		if (curDept + 1 >= thresholdDepth)
		{
			for (Iterator<IdSystemNode> iterator = mChildren.iterator(); iterator.hasNext();)
			{
				IdSystemNode child = iterator.next();

				if (!levelNodes.remove(child))
				{
					JBUI.getMainController().mFXTreeLayout.removeNodesRecursively(child.mUIController.mScreenNode);
					iterator.remove();
				}
			}

			if (mChildren.isEmpty() && mStateType == StateType.Default)
			{
				mStateType = StateType.LastReachable;
				mUIController.mScreenNode.getPath().getStyleClass().add("limit_state_node");
			}

			return;
		}

		for (IdSystemNode child : mChildren)
		{
			child.inferLastReachableNodes(levelNodes, thresholdDepth, curDept + 1);
		}
	}

	private boolean insert(NonRootIdSystemNode otherChild, Queue<IdElem> idElems)
	{
		// We may only insert children on default state parents
		if (idElems.peek().equals(mIdElem))
		{
			if (idElems.size() < 2)
			{
				// The child is duplicated of me via full ID.
				// Update the parent for correct operation of inferSearchLimitSafeNodes, and the
				// system information.
				otherChild.mParent = getParent();
				mMsgElemSequences = otherChild.mMsgElemSequences;
				return true;
			}
			else if (mStateType == StateType.Default)
			{
				idElems.remove();

				// Change pointers for unchanged parent-tracked messages in the child's list.
				// Reuse the parent nodes message string pointers to reduce memory consumption;
				// the old pointers will be garbage collected.
				List<MsgElement> updatedParentMsgElemSeqs = otherChild.mMsgElemSequences.subList(
						otherChild.mMsgElemSequences.size() - mMsgElemSequences.size(),
						otherChild.mMsgElemSequences.size());

				for (int i = 0; i < updatedParentMsgElemSeqs.size(); i++)
				{
					if (updatedParentMsgElemSeqs.get(i).equals(mMsgElemSequences.get(i)))
					{
						updatedParentMsgElemSeqs.set(i, mMsgElemSequences.get(i));
					}
				}

				// Delegate insertion to my children.
				// If it succeeds, it means I'm an older ancestor of the child - we stop.
				for (IdSystemNode myChild : mChildren)
				{
					if (myChild.insert(otherChild, idElems))
					{
						return true;
					}
				}

				// I must be the parent of the child
				otherChild.mParent = this;
				otherChild.mUIController = JBUI.getMainController().createChildNode(otherChild, mUIController);
				mChildren.add(otherChild);
				return true;
			}
		}

		return false;
	}

	public void outputIdAsJSONArray(JSONArray jsonArray) throws JSONException
	{
		JSONObject jsonId = new JSONObject();
		mIdElem.outputAsJSONObject(jsonId);
		jsonArray.put(jsonId);
	}

	void parseUIKeys(JSONObject jsonIdSystem)
	{
		if (jsonIdSystem.optBoolean("isSelected"))
		{
			JBUI.getMainController().selectScreenNode(mUIController);
		}

		if (jsonIdSystem.optBoolean("isFolded"))
		{
			mUIController.fold();
		}

		mNotes = jsonIdSystem.optString("notes");
	}

	protected String unparseId(String separator)
	{
		return mIdElem.toString();
	}

	// Computes the default Maude-NPA textual id of this node
	String unparseIdSpaced()
	{
		return unparseId(" . ");
	}

	public String unparseIdUnspaced()
	{
		return unparseId(".");
	}
}
