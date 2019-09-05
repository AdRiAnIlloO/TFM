package jbui.persistence;

import java.io.File;

import jbui.controller.GeneralPathsController;

public class NPAModuleLoadDAO extends ControllerFileLoadDAO<GeneralPathsController>
{
	public NPAModuleLoadDAO(GeneralPathsController controller, File npaModuleFile)
	{
		super(controller, npaModuleFile);
	}

	@Override
	void handleResults()
	{
		mController.handleNPAModuleLoad(mLoadedTextInput);
	}

	@Override
	boolean replace(DAO other)
	{
		if (other instanceof NPAModuleLoadDAO)
		{
			replace((NPAModuleLoadDAO) other);
			return true;
		}

		return false;
	}
}
