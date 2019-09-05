package jbui.persistence;

import java.io.InputStream;
import java.util.Scanner;

import jbui.controller.LoadablesController;

abstract class LoadablesControllerDAO<T extends LoadablesController> extends ResultsHandlingDAO
{
	T mController;
	String mLoadedTextInput;

	LoadablesControllerDAO(T controller)
	{
		mController = controller;
	}

	void executeIO(InputStream moduleInputStream)
	{
		try (Scanner scanner = new Scanner(moduleInputStream))
		{
			// We read sanitizing the content -escaping percentage signs-, for early safety
			for (scanner.useDelimiter("\\z"), mLoadedTextInput = ""; scanner
					.hasNext(); mLoadedTextInput += scanner.next().replace("%", "%%"));
		}
	}

	void replace(LoadablesControllerDAO<T> other)
	{
		if (other.mController == mController)
		{
			mController.replaceLoadingModule();
		}
	}
}
