package jbui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jbui.controller.MainController;
import jbui.model.MaudeThinker;

public class JBUI extends Application
{
	public static JBUI sInstance;

	public static MainController getMainController()
	{
		return sInstance.mMainController;
	}

	public static File getMaudeBinFile()
	{
		return sInstance.mMaudeBinFile;
	}

	public static MaudeThinker getMaudeThinker()
	{
		return sInstance.mMaudeThinker;
	}

	public static URL getResource(String name)
	{
		return JBUI.class.getResource(name);
	}

	public static void main(String[] args)
	{
		launch(args);
	}

	public String DEFAULT_APP_TITLE = "Maude-NPA JBUI";
	public ResourceBundle mEnglishResources = ResourceBundle.getBundle("jbui.resource.strings", Locale.ENGLISH);
	public File mLastTreeSaveDirectory;
	public ResourceBundle mLocalizationResources;
	public MainController mMainController;
	public File mMaudeBinFile; // Maude bin
	private MaudeThinker mMaudeThinker;
	public File mNPAModuleFile;
	public File mProtocolModuleFile;
	public ResourceBundle mSpanishResources = ResourceBundle.getBundle("jbui.resource.strings", new Locale("es"));
	public Stage mStage;

	public JBUI()
	{
		mMaudeThinker = new MaudeThinker();
	}

	@Override
	public void start(Stage primaryStage) throws IOException
	{
		sInstance = this;
		mStage = primaryStage;
		mMaudeThinker.start();

		// Load main window
		URL url = getClass().getResource("view/MainView.fxml");
		mLocalizationResources = mEnglishResources;
		BorderPane root = FXMLLoader.load(url, mLocalizationResources);
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle(DEFAULT_APP_TITLE);
		primaryStage.show();

		// Set minimum window dimensions from computed FXML's, as a convenient UX factor
		primaryStage.setMinWidth(primaryStage.getWidth());
		primaryStage.setMinHeight(primaryStage.getHeight());
	}
}
