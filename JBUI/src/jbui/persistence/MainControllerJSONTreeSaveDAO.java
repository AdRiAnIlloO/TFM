package jbui.persistence;

import org.json.JSONException;

import jbui.JBUI;

public class MainControllerJSONTreeSaveDAO extends JSONTreeSaveDAO
{
	public MainControllerJSONTreeSaveDAO() throws JSONException
	{
		super(JBUI.getMaudeThinker().mRootIdSystemNode, JBUI.getMainController().mProtocolSaveFile);
	}

	@Override
	void handleResults()
	{
		JBUI.getMainController().handleProtocolSaveDone();
	}

	@Override
	boolean replace(DAO other)
	{
		if ((other instanceof MainControllerJSONTreeSaveDAO) && replace((JSONTreeSaveDAO) other))
		{
			return JBUI.getMainController().handleSaveDAOReplace();
		}

		return false;
	}
}
