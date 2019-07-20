package jbui.persistence;

import jbui.controller.GeneralPathsController;

public class JSONModuleLoadDAO extends ModuleLoadDAO<GeneralPathsController>
{
	public JSONModuleLoadDAO(GeneralPathsController controller, String jsonModulePathName)
	{
		super(controller, jsonModulePathName);
	}

	@Override
	void handleResults()
	{
		mController.notifyJSONModuleLoad(mModuleTextInput);
	}

	@Override
	boolean shouldReplaceThisInList(DAO other)
	{
		if (other instanceof JSONModuleLoadDAO)
		{
			return shouldReplaceThisInList((JSONModuleLoadDAO) other);
		}

		return false;
	}
}
