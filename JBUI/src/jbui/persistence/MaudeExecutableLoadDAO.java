package jbui.persistence;

import java.io.File;
import java.io.IOException;

import jbui.controller.GeneralPathsController;

public class MaudeExecutableLoadDAO extends LoadablesControllerDAO<GeneralPathsController>
{
	private File mMaudeBinFile;
	private String mMaudeBinPathName;
	private Process mMaudeProcess;

	public MaudeExecutableLoadDAO(GeneralPathsController controller, String maudeBinPathName, File maudeBinFile)
	{
		super(controller);
		mController = controller;
		mMaudeBinPathName = maudeBinPathName;
		mMaudeBinFile = maudeBinFile;
	}

	@Override
	void executeIO() throws IOException
	{
		if (mMaudeBinFile == null)
		{
			for (String directory : System.getenv("PATH").split(File.pathSeparator))
			{
				File auxFile = new File(directory, mMaudeBinPathName);

				if (auxFile.isFile() && auxFile.canExecute())
				{
					mMaudeBinFile = auxFile;
					mMaudeBinPathName = auxFile.getAbsolutePath();
					break;
				}
			}

			if (mMaudeBinFile == null)
			{
				return;
			}
		}

		ProcessBuilder builder = new ProcessBuilder(mMaudeBinPathName);
		builder.redirectErrorStream(true);
		mMaudeProcess = builder.start();
	}

	@Override
	void handleResults()
	{
		mController.handleMaudeProcessLoadResult(mMaudeProcess, mMaudeBinFile);
	}

	@Override
	boolean replace(DAO other)
	{
		if (other instanceof MaudeExecutableLoadDAO)
		{
			replace((MaudeExecutableLoadDAO) other);
			return true;
		}

		return false;
	}
}
