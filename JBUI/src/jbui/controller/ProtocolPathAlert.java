package jbui.controller;

class ProtocolPathAlert extends LoadablesAlert<ProtocolPathController>
{
	ProtocolPathAlert()
	{
		super("New attack setup",
				"Here you can load a protocol file and set execution options, and trigger an execution",
				"view/protocol_setup_window.fxml");
	}
}
