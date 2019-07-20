package jbui.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import jbui.JBUI;
import jbui.controller.LoadablesController;

abstract class ModuleLoadDAO<T extends LoadablesController> extends ResultsHandlingDAO
{
	T mController;
	private InputStream mModuleInputStream;
	String mModuleTextInput;

	ModuleLoadDAO(T controller, File moduleFile)
	{
		try
		{
			mController = controller;
			mModuleInputStream = new FileInputStream(moduleFile);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	ModuleLoadDAO(T controller, String modulePathName)
	{
		mController = controller;
		mModuleInputStream = JBUI.class.getResourceAsStream(modulePathName);
	}

	@Override
	void executeIO()
	{
		try (Scanner scanner = new Scanner(mModuleInputStream))
		{
			// We read sanitizing the content -escaping percentage signs-, for early safety
			for (scanner.useDelimiter("\\z"), mModuleTextInput = ""; scanner
					.hasNext(); mModuleTextInput += scanner.next().replace("%", "%%"));
		}
	}

	boolean shouldReplaceThisInList(ModuleLoadDAO<T> other)
	{
		return (other.mController == mController);
	}
}
