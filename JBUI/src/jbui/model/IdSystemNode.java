package jbui.model;

import java.util.ArrayList;
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
		public final boolean mIsSend;
		public final String mMsg;
		public final String mSignature;

		private MsgElement(JSONObject jsonMsgElem) throws JSONException
		{
			mIsSend = jsonMsgElem.getBoolean("isSend");
			mMsg = jsonMsgElem.getString("msg");
			mSignature = jsonMsgElem.getString("signature");
		}
	}

	// For now, LastReachable is not handled
	public static enum StateType
	{
		Default, Initial
	}

	// Used to advance from the last global tree depth
	static int sMaxTreeDepth;

	private List<IdSystemNode> mChildren;
	private IdElem mIdElem;
	public List<MsgElement> mMsgElemSequences;
	private IdSystemNode mParent;
	public final StateType mStateType;
	IdSystemNodeUIController mUIController;

	IdSystemNode(IdElem idElem, JSONObject jsonIdSystem) throws JSONException
	{
		mStateType = (jsonIdSystem.getBoolean("isInitial") ? StateType.Initial : StateType.Default);
		mIdElem = idElem;
		mChildren = new ArrayList<>();
		JSONArray jsonMsgElemSeq = jsonIdSystem.getJSONObject("system").getJSONArray("msgSeqList");
		mMsgElemSequences = new ArrayList<>();

		for (int i = 0; i < jsonMsgElemSeq.length(); i++)
		{
			MsgElement msgElem = new MsgElement(jsonMsgElemSeq.getJSONObject(i));
			mMsgElemSequences.add(msgElem);
		}
	}

	int getDepth()
	{
		return 0;
	}

	public IdSystemNode getParent()
	{
		return null;
	}

	boolean insert(NonRootIdSystemNode otherChild, Queue<IdElem> idElems)
	{
		if (idElems.peek().equals(mIdElem))
		{
			if (idElems.size() < 2)
			{
				// The child is duplicated of me via full ID. Update the system info content.
				mMsgElemSequences = otherChild.mMsgElemSequences;
			}
			else
			{
				idElems.remove();

				// Remove child messages already contained in this's
				otherChild.mMsgElemSequences = otherChild.mMsgElemSequences.subList(0,
						otherChild.mMsgElemSequences.size() - mMsgElemSequences.size());

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
			}

			return true;
		}

		return false;
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
