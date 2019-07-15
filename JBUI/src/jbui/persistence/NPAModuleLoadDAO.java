package jbui.persistence;

import java.io.File;

import jbui.controller.GeneralPathsController;

public class NPAModuleLoadDAO extends ModuleLoadDAO<GeneralPathsController>
{
	public NPAModuleLoadDAO(GeneralPathsController controller, File npaModuleFile)
	{
		super(controller, npaModuleFile);
	}

	@Override
	void handleResults()
	{
		mController.handleNPAModuleLoad(mModuleTextInput);
	}

	@Override
	boolean shouldReplaceThisInList(DAO other)
	{
		return (other instanceof NPAModuleLoadDAO);
	}
}
