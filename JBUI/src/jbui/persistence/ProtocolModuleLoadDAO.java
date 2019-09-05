package jbui.persistence;

import java.io.File;

import jbui.controller.ProtocolPathController;

public class ProtocolModuleLoadDAO extends ControllerFileLoadDAO<ProtocolPathController>
{
	public ProtocolModuleLoadDAO(ProtocolPathController controller, File npaModuleFile)
	{
		super(controller, npaModuleFile);
	}

	@Override
	void handleResults()
	{
		mController.handleProtocolModuleLoad(mLoadedTextInput);
	}

	@Override
	boolean replace(DAO other)
	{
		if (other instanceof ProtocolModuleLoadDAO)
		{
			replace((ProtocolModuleLoadDAO) other);
			return true;
		}

		return false;
	}
}
