package jbui.persistence;

import java.io.File;

import jbui.controller.ProtocolPathController;

public class JSONTreeLoadDAO extends ControllerFileLoadDAO<ProtocolPathController>
{
	public JSONTreeLoadDAO(ProtocolPathController controller, File treeFile)
	{
		super(controller, treeFile);
	}

	@Override
	void executeIO()
	{
		if (mModuleFile.exists())
		{
			super.executeIO();
			return;
		}
	}

	@Override
	void handleResults()
	{
		mController.handleTreeFileLoad(mLoadedTextInput);
	}
}
