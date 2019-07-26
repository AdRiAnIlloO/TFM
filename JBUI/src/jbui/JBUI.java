package jbui;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import jbui.controller.MainController;
import jbui.model.MaudeThinker;

public class JBUI extends Application
{
	public static JBUI sInstance;

	public static Canvas getDrawingCanvas()
	{
		return sInstance.mMainController.mDrawingCanvas;
	}
	
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

	public static GridPane getNodesGridPane()
	{
		return sInstance.mMainController.mNodesGridPane;
	}

	public static URL getResource(String name)
	{
		return JBUI.class.getResource(name);
	}

	public static void main(String[] args)
	{
		launch(args);
	}

	public MainController mMainController;
	public File mMaudeBinFile; // Maude bin
	private MaudeThinker mMaudeThinker;
	public File mNPAModuleFile;
	public File mProtocolModuleFile;
	public Stage mStage;

	public JBUI() throws IOException
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
		URL url = getClass().getResource("view/main_view.fxml");
		BorderPane root = FXMLLoader.load(url);
		Scene scene = new Scene(root);
		String formStr = getClass().getResource("resource/application.css").toExternalForm();
		scene.getStylesheets().add(formStr);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Maude-NPA JBUI");
		primaryStage.show();

		// Set minimum window dimensions from computed FXML's, as a convenient UX factor
		primaryStage.setMinWidth(primaryStage.getWidth());
		primaryStage.setMinHeight(primaryStage.getHeight());
	}
}
