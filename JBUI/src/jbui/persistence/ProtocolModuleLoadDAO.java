package jbui.persistence;

import java.io.File;

import jbui.controller.ProtocolPathController;

public class ProtocolModuleLoadDAO extends ModuleLoadDAO<ProtocolPathController>
{
	public ProtocolModuleLoadDAO(ProtocolPathController controller, File npaModuleFile)
	{
		super(controller, npaModuleFile);
	}

	@Override
	void handleResults()
	{
		mController.handleProtocolModuleLoad(mModuleTextInput);
	}

	@Override
	boolean shouldReplaceThisInList(DAO other)
	{
		return (other instanceof ProtocolModuleLoadDAO);
	}
}
