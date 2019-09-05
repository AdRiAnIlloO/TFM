package jbui.persistence;

import org.json.JSONException;

import jbui.controller.NodeDetailController;

public class NodeDetailsJSONTreeSaveDAO extends JSONTreeSaveDAO
{
	private final NodeDetailController mController;

	public NodeDetailsJSONTreeSaveDAO(NodeDetailController controller) throws JSONException
	{
		super(controller.mProtocolSaveRootNode, controller.mProtocolSaveFile);
		mController = controller;
	}

	@Override
	void handleResults()
	{
		mController.handleProtocolSaveDone();
	}

	@Override
	boolean replace(DAO other)
	{
		if ((other instanceof NodeDetailsJSONTreeSaveDAO) && replace((JSONTreeSaveDAO) other))
		{
			return mController.handleSaveDAOReplace();
		}

		return false;
	}
}
