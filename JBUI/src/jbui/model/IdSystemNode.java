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

		private MsgElement(JSONObject jsonMsgElem) throws JSONException
		{
			mIsSend = jsonMsgElem.getBoolean("isSend");
			mMsg = jsonMsgElem.getString("msg");
		}
	}

	// Used to advance from the last global tree depth
	static int sMaxTreeDepth;

	private List<IdSystemNode> mChildren;
	private IdElem mIdElem;
	public List<MsgElement> mMsgElemSequences;
	private IdSystemNode mParent;
	IdSystemNodeUIController mUIController;

	IdSystemNode(IdElem idElem, JSONObject jsonMsg) throws JSONException
	{
		mIdElem = idElem;
		mChildren = new ArrayList<>();
		JSONArray jsonMsgElemSeq = jsonMsg.getJSONArray("msgSeqList");
		mMsgElemSequences = new ArrayList<>();

		for (int i = 0; i < jsonMsgElemSeq.length(); i++)
		{
			MsgElement msgElem = new MsgElement(jsonMsgElemSeq.getJSONObject(i));
			mMsgElemSequences.add(msgElem);
		}
	}

	private boolean equals(IdSystemNode node)
	{
		boolean localDataEqual = node.mIdElem.equals(mIdElem);

		if (mParent != null)
		{
			return (mParent.equals(node) && localDataEqual);
		}

		return localDataEqual;
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
		if (mParent != null)
		{
			return mParent.getDepth() + 1;
		}

		return 0;
	}
	
	public IdSystemNode getParent()
	{
		return mParent;
	}

	boolean insert(IdSystemNode otherChild, Queue<IdElem> idElems)
	{
		if (idElems.peek().equals(mIdElem))
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

			// I will be the parent of the child. Update if child is duplicated, or add it.
			int index = mChildren.indexOf(otherChild);

			if (index != -1)
			{
				// Update only the message, just in case.
				// Every other data from current node already invalidates child's.
				mChildren.get(index).mMsgElemSequences = otherChild.mMsgElemSequences;
			}
			else
			{
				otherChild.mParent = this;
				otherChild.mUIController = JBUI.getMainController().createChildNode(otherChild, mUIController);
				mChildren.add(otherChild);
			}

			return true;
		}

		return false;
	}

	private String unparseId(String separator)
	{
		if (mParent != null)
		{
			return (mParent.unparseId(separator) + separator + mIdElem.toString());
		}

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
