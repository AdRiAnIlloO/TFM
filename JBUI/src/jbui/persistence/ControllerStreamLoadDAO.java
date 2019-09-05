package jbui.persistence;

import java.io.InputStream;

import jbui.JBUI;
import jbui.controller.LoadablesController;

abstract class ControllerStreamLoadDAO<T extends LoadablesController> extends LoadablesControllerDAO<T>
{
	private String mModulePathName;

	ControllerStreamLoadDAO(T controller, String modulePathName)
	{
		super(controller);
		mModulePathName = modulePathName;
	}

	@Override
	void executeIO()
	{
		InputStream inputStream = JBUI.class.getResourceAsStream(mModulePathName);
		executeIO(inputStream);
	}
}
