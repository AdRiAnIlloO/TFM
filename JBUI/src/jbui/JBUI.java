package jbui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import jbui.controller.MainController;

public class JBUI extends Application
{
	public static String DEFAULT_JSON_MODULE_PATH = "resource/maude_npa_json.maude";

	public static JBUI sInstance;

	static Canvas getDrawingCanvas()
	{
		return sInstance.mMainController.mDrawingCanvas;
	}

	public static String getJSONModuleTextContent()
	{
		return sInstance.mJSONModuleTextContent;
	}

	public static File getMaudeBinFile()
	{
		return sInstance.mMaudeBinFile;
	}

	public static Process getMaudeProcess()
	{
		return sInstance.mMaudeProcess;
	}

	public static MaudeThinker getMaudeThinker()
	{
		return sInstance.mMaudeThinker;
	}

	public static GridPane getNodesGridPane()
	{
		return sInstance.mMainController.mNodesGridPane;
	}

	public static File getNPAModuleFile()
	{
		return sInstance.mNPAModuleFile;
	}

	public static File getProtocolModuleFile()
	{
		return sInstance.mProtocolModuleFile;
	}

	protected static URL getResource(String name)
	{
		return JBUI.class.getResource(name);
	}

	/**
	 * @param textField
	 * @param updatedFile
	 * @param lastConfirmedDirectory
	 * @return If final updatedFiles valid, its parent directory. Otherwise,
	 *         lastConfirmedDirectory
	 */
	public static File handlePathDialogResult(TextField textField, File updatedFile, File lastConfirmedDirectory)
	{
		if (updatedFile != null)
		{
			textField.setText(updatedFile.getAbsolutePath());
			lastConfirmedDirectory = updatedFile.getParentFile();
		}

		return lastConfirmedDirectory;
	}

	public static void main(String[] args)
	{
		launch(args);
	}

	public static File showMaudePathDialog(TextField textField, File lastConfirmedDirectory)
	{
		ExtensionFilter filter = new ExtensionFilter("Maude files", "*.maude");
		return showPathDialog(textField, filter, lastConfirmedDirectory);
	}

	/**
	 * Shows a path search dialog with an associated TextField path for feedback
	 * 
	 * @param textField
	 * @param filter
	 * @param lastConfirmedDirectory Last parent directory user was located while in
	 *                               previous search dialogs
	 * @return Final choosen file
	 */
	public static File showPathDialog(TextField textField, ExtensionFilter filter, File lastConfirmedDirectory)
	{
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(lastConfirmedDirectory);

		if (filter != null)
		{
			chooser.getExtensionFilters().add(filter);
		}

		return chooser.showOpenDialog(sInstance.mStage);
	}

	// We store JSON module content directly to load correctly on Maude when it must
	// be read from within the JAR
	private String mJSONModuleTextContent;

	private MainController mMainController;

	public File mMaudeBinFile; // Maude bin

	// An auxiliar process. It may not be used currently until next re-launch.
	public Process mMaudeProcess;

	private MaudeThinker mMaudeThinker;

	public File mNPAModuleFile;

	public File mProtocolModuleFile;

	private Stage mStage;

	public JBUI() throws IOException
	{
		mMaudeThinker = new MaudeThinker();

		// Resolve Maude bin path to a full path via PATH environment
		mMaudeBinFile = findExecutableOnPath("maude");

		// Load JSON module
		mJSONModuleTextContent = "";
		InputStream jsonModuleStream = getClass().getResourceAsStream(DEFAULT_JSON_MODULE_PATH);
		InputStreamReader jsonModuleStreamReader = new InputStreamReader(jsonModuleStream);
		BufferedReader jsonModuleBufferedReader = new BufferedReader(jsonModuleStreamReader);
		for (String line; (line = jsonModuleBufferedReader.readLine()) != null; mJSONModuleTextContent += line
				+ System.lineSeparator());
	}

	private File findExecutableOnPath(String name)
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
				return file;
			}
		}

		return null;
	}

	@Override
	public void start(Stage primaryStage) throws IOException
	{
		sInstance = this;
		mStage = primaryStage;
		mMaudeThinker.start();

		// Load main window
		URL url = getClass().getResource("view/main_view.fxml");
		FXMLLoader loader = new FXMLLoader(url);
		BorderPane root = loader.load();
		mMainController = loader.getController();
		Scene scene = new Scene(root);
		String formStr = getClass().getResource("application.css").toExternalForm();
		scene.getStylesheets().add(formStr);
		primaryStage.setScene(scene);
		primaryStage.show();

		// Set minimum window dimensions from computed FXML's, as a convenient UX factor
		primaryStage.setMinWidth(primaryStage.getWidth());
		primaryStage.setMinHeight(primaryStage.getHeight());

		// Prompt already the general paths controller setup window, for convenience
		MainController controller = loader.getController();
		controller.showGeneralPathsWindow();
	}
}
