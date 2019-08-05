package jbui.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import jbui.JBUI;
import jbui.controller.IdSystemNodeUIController;

public class IdSystemNode
{
	// Used to advance from the last global tree depth
	static int sMaxTreeDepth;

	private List<IdSystemNode> mChildren;
	private IdElem mIdElem;
	private String mMsg;
	private IdSystemNode mParent;
	IdSystemNodeUIController mUIController;

	IdSystemNode(IdElem idElem, String msg)
	{
		mMsg = msg;
		mChildren = new ArrayList<>();
		mIdElem = idElem;
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

	boolean insert(IdSystemNode otherChild, Queue<IdElem> idElems)
	{
		if (idElems.peek().equals(mIdElem))
		{
			idElems.remove();

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
				mChildren.get(index).mMsg = otherChild.mMsg;
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
