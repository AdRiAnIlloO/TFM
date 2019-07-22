package jbui.controller;

class ProtocolPathAlert extends LoadablesAlert<ProtocolPathController>
{
	ProtocolPathAlert()
	{
		super("New attack setup",
				"Here you can load a protocol file to prepare an execution.\nWarning: confirming will completely clear the current execution.",
				"view/protocol_setup_window.fxml");
	}
}
