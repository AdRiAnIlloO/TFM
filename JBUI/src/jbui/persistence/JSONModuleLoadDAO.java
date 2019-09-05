package jbui.persistence;

import jbui.controller.GeneralPathsController;

public class JSONModuleLoadDAO extends ControllerStreamLoadDAO<GeneralPathsController>
{
	public JSONModuleLoadDAO(GeneralPathsController controller, String jsonModulePathName)
	{
		super(controller, jsonModulePathName);
	}

	@Override
	void handleResults()
	{
		mController.notifyJSONModuleLoad(mLoadedTextInput);
	}

	@Override
	boolean replace(DAO other)
	{
		if (other instanceof JSONModuleLoadDAO)
		{
			replace((JSONModuleLoadDAO) other);
			return true;
		}

		return false;
	}
}
