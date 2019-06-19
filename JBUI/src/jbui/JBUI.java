package jbui;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JBUI extends Application
{
	public static JBUI sInstance;

	public Stage mStage;
	public MaudeThinker mMaudeThinker;

	public String mMaudeBinPath; // Maude bin
	public String mJSONModulePath;
	public String mNPAModulePath;
	public String mProtocolModulePath;

	public static void main(String[] args)
	{
		launch(args);
	}

	public static MaudeThinker getMaudeThinker()
	{
		return sInstance.mMaudeThinker;
	}

	public static String getMaudeBinPath()
	{
		return sInstance.mMaudeBinPath;
	}

	public static String getJSONModulePath()
	{
		return sInstance.mJSONModulePath;
	}

	public static String getNPAModulePath()
	{
		return sInstance.mNPAModulePath;
	}

	public static String getProtocolModulePath()
	{
		return sInstance.mProtocolModulePath;
	}

	public JBUI()
	{
		mStage = null; // Delayed to start()
		mMaudeThinker = new MaudeThinker();
		mJSONModulePath = null;
		mNPAModulePath = null;
		mProtocolModulePath = null;

		// Resolve Maude bin path to a full path via PATH environment
		mMaudeBinPath = findExecutableOnPath("maude");
	}

	@Override
	public void start(Stage primaryStage) throws IOException
	{
		sInstance = this;
		mStage = primaryStage;
		mMaudeThinker.start();

		// Load main window
		URL url = getClass().getResource("view/main_view.fxml");
		Parent root = FXMLLoader.load(url);
		Scene scene = new Scene(root, 400, 400);
		String formStr = getClass().getResource("application.css").toExternalForm();
		scene.getStylesheets().add(formStr);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private String findExecutableOnPath(String name)
	{
		String osName = System.getProperty("os.name");

		if (osName.startsWith("Windows") && !name.endsWith(".exe"))
		{
			name += ".exe";
		}

		for (String directory : System.getenv("PATH").split(File.pathSeparator))
		{
			File file = new File(directory, name);

			if (file.isFile() && file.canExecute())
			{
				return file.getAbsolutePath();
			}
		}

		return name;
	}
}
