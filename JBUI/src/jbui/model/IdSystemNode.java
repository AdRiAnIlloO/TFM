package jbui.model;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

	// Parsed version of the academic, dot-based term position representation
	// (e.g. 1.2.<...>.N)
	private List<Integer> mIdPositions;

	private String mMsg;

	IdSystemNode(String idText, String msg)
	{
		mMsg = msg;
		mChildren = new ArrayList<>();
		mIdPositions = new ArrayList<>();
		String[] idTextPositions = idText.split("\\.");

		if (idTextPositions.length < 1)
		{
			Integer idPosition = Integer.parseInt(idText);
			mIdPositions.add(idPosition);
			return;
		}

		for (String idTextPosition : idTextPositions)
		{
			Integer idPosition = Integer.parseInt(idTextPosition);
			mIdPositions.add(idPosition);
		}

		sMaxTreeDepth = Math.max(sMaxTreeDepth, idTextPositions.length);
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

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof IdSystemNode)
		{
			IdSystemNode otherNode = (IdSystemNode) other;
			return (otherNode.mIdPositions.equals(mIdPositions));
		}

		return false;
	}

	int getDepth()
	{
		return (mIdPositions.size() - 1);
	}

	/**
	 * Initializes the required FXML controller
	 * 
	 * It is decoupled from the constructor to easily avoid wasting memory on
	 * instantiated nodes which may be duplicated during the interactive search
	 * 
	 * @param idText
	 */
	void initController(String idText)
	{
		try
		{
			URL url = JBUI.getResource("view/canvas_node.fxml");
			FXMLLoader loader = new FXMLLoader(url);
			loader.load();
			mController = loader.getController();
			mController.setModelData(idText, this);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	boolean insert(IdSystemNode otherChild, String idText)
	{
		// Search first mismatching breadth position among the max common depth level
		for (int i = 0, maxDepth = Math.min(mIdPositions.size(), otherChild.mIdPositions.size()); i < maxDepth; i++)
		{
			if (!otherChild.mIdPositions.get(i).equals(mIdPositions.get(i)))
			{
				return false;
			}
		}

		// I will be direct/indirect parent of the child.
		// Check if it must be a direct child.
		if (mIdPositions.size() + 1 == otherChild.mIdPositions.size())
		{
			int index = mChildren.indexOf(otherChild);

			if (index != -1)
			{
				// Update only the message, just in case.
				// Every other data from current node already invalidates child's.
				mChildren.get(index).mMsg = otherChild.mMsg;
			}
			else
			{
				mChildren.add(otherChild);
				otherChild.initController(idText);
			}

			return true;
		}

		// I will be an indirect parent of the child.
		// Delegate insertion to my direct children.
		for (IdSystemNode myChild : mChildren)
		{
			if (myChild.insert(otherChild, idText))
			{
				return true;
			}
		}

		return false;
	}

	boolean isRoot()
	{
		return (mIdPositions.size() == 1);
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

	// Computes the default Maude-NPA textual id of this node, which has spaces
	String unparseIdToNPAFormat()
	{
		String spacedIdText = String.valueOf(mIdPositions.get(0));

		for (int i = 1; i < mIdPositions.size(); i++)
		{
			spacedIdText += " . " + String.valueOf(mIdPositions.get(i));
		}

		return spacedIdText;
	}
}
