package jbui.controller;

class GeneralPathsAlert extends PathsSetupAlert<GeneralPathsController>
{
	GeneralPathsAlert()
	{
		super("Maude paths setup", "Here you can configure the basic required Maude files that any execution uses",
				"view/general_paths_window.fxml");
	}
}
