package jbui.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import jbui.controller.LoadablesController;

abstract class ControllerFileLoadDAO<T extends LoadablesController> extends LoadablesControllerDAO<T>
{
	File mModuleFile;

	ControllerFileLoadDAO(T controller, File moduleFile)
	{
		super(controller);
		mModuleFile = moduleFile;
	}

	@Override
	void executeIO()
	{
		try
		{
			InputStream inputStream = new FileInputStream(mModuleFile);
			executeIO(inputStream);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}
