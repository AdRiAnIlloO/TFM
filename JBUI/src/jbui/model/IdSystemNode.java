package jbui.model;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.GraphicsContext;
import jbui.JBUI;
import jbui.controller.CanvasNodeController;

public class IdSystemNode
{
	private static int PARENT_TO_CHILD_ARC_WIDTH = 2;

	// Used to advance from the last global tree depth
	static int sMaxTreeDepth;

	private List<IdSystemNode> mChildren;
	private CanvasNodeController mController;
	private IdElem mIdElem;
	private String mMsg;
	private IdSystemNode mParent;

	IdSystemNode(IdElem idElem, String msg)
	{
		mMsg = msg;
		mChildren = new ArrayList<>();
		mIdElem = idElem;
	}

	int addToGridPane(int columnIndex, int rowIndex, CanvasNodeController parentController)
	{
		int leafNodeAmount = 0;
		int childrenRowIndex = rowIndex + 1;

		for (IdSystemNode child : mChildren)
		{
			// Recurse with the accumulated count of columns created by leaf nodes
			leafNodeAmount += child.addToGridPane(columnIndex + leafNodeAmount, childrenRowIndex, mController);
		}

		leafNodeAmount = Math.max(leafNodeAmount, 1);
		mController.addToGridPane(columnIndex, rowIndex, leafNodeAmount);
		return leafNodeAmount;
	}

	void drawArcs(GraphicsContext ctx)
	{
		ctx.setLineWidth(PARENT_TO_CHILD_ARC_WIDTH);

		for (IdSystemNode child : mChildren)
		{
			mController.drawArcToChild(child.mController, ctx);
			child.drawArcs(ctx);
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

	/**
	 * Initializes the required FXML controller
	 * 
	 * It is decoupled from the constructor to easily avoid wasting memory on
	 * instantiated nodes which may be duplicated during the interactive search
	 * 
	 * @param idText
	 */
	void initController()
	{
		try
		{
			URL url = JBUI.getResource("view/canvas_node.fxml");
			FXMLLoader loader = new FXMLLoader(url);
			loader.load();
			mController = loader.getController();
			mController.setModelData(this);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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

			// I will be the parent of the child. Update if child is duplicated, or add it
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
				otherChild.initController();
				mChildren.add(otherChild);
			}

			return true;
		}

		return false;
	}

	void removeFromModelAndGridPane()
	{
		for (IdSystemNode child : mChildren)
		{
			child.removeFromModelAndGridPane();
		}

		mController.hideFromGridPane();
		mChildren.clear();
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
