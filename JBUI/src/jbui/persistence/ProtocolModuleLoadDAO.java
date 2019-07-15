package jbui.persistence;

import java.io.File;

import jbui.controller.ProtocolSetupController;

public class ProtocolModuleLoadDAO extends ModuleLoadDAO<ProtocolSetupController>
{
	public ProtocolModuleLoadDAO(ProtocolSetupController controller, File npaModuleFile)
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
