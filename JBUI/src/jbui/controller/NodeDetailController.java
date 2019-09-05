package jbui.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import jbui.JBUI;
import jbui.model.IdSystemNode;
import jbui.persistence.NodeDetailsJSONTreeSaveDAO;

public class NodeDetailController extends JSONTreeExportController // NO_UCD
{
	private class SectionStringTreeItem extends TreeItem<String>
	{
		private SectionStringTreeItem(String text)
		{
			super(text);
			setExpanded(true);
		}
	}

	private static final PseudoClass INTRUDER_LEARNED_STATE_PSEUDO_CLASS = PseudoClass
			.getPseudoClass("intruderLearned");
	private static int LEFT_MSG_INFO_COLUMN_INDEX = 0;

	// Workarounds observed GridPane dynamic row height reduction inability
	private static double MSG_IMAGE_TO_TEXT_HEIGHT_FIT_RATIO = 2.2;

	private static int MSGS_GROUPS_ID_COLUMN_INDEX = 4;
	private static int RIGHT_MSG_INFO_COLUMN_INDEX = 2;

	@FXML
	private TreeView<String> mFullInfoTreeView;

	@FXML
	private GridPane mMsgFlowGridPane;

	@FXML
	private ScrollPane mMsgFlowScrollPane;

	@FXML
	private StackPane mMsgFlowStackPane;

	@FXML
	private CheckBox mNotesAutoSaveCheck;

	@FXML
	private Button mNotesSaveBtn;

	@FXML
	private TextArea mNotesTextArea;

	public final IdSystemNode mProtocolSaveRootNode;

	public NodeDetailController()
	{
		mProtocolSaveRootNode = JBUI.getMaudeThinker().mRootIdSystemNode;
		mProtocolSaveFile = JBUI.getMainController().mProtocolSaveFile;
	}

	private void createCopyToClipboardMenu()
	{
		Clipboard clipboard = Clipboard.getSystemClipboard();
		ClipboardContent content = new ClipboardContent();
		String lines = "";

		for (TreeItem<String> selected : mFullInfoTreeView.getSelectionModel().getSelectedItems())
		{
			for (TreeItem<String> parent = selected; (parent = parent.getParent()) != null; lines += ' ');
			lines += selected.getValue() + System.lineSeparator();
		}

		content.putString(lines);
		clipboard.setContent(content);
	}

	@SuppressWarnings("unchecked")
	void init(IdSystemNode modelNode)
	{
		TreeItem<String> strandsItem = new SectionStringTreeItem("Participants's strands");
		TreeItem<String> intruderKItem = new SectionStringTreeItem("Intruder knowledge");
		TreeItem<String> msgSequenceItem = new SectionStringTreeItem("Message sequence");
		TreeItem<String> ghostsItem = new SectionStringTreeItem("Ghost list");
		TreeItem<String> propertiesItem = new SectionStringTreeItem("Properties");
		TreeItem<String> sectionsItem = new SectionStringTreeItem("Sections");
		sectionsItem.getChildren().addAll(strandsItem, intruderKItem, msgSequenceItem, ghostsItem, propertiesItem);
		mFullInfoTreeView.setRoot(sectionsItem);

		for (IdSystemNode.Strand strand : modelNode.mStrands)
		{
			TreeItem<String> signatureItem = new SectionStringTreeItem(strand.mSignature);
			strandsItem.getChildren().add(signatureItem);
			TreeItem<String> unknownMsgsItem = new SectionStringTreeItem("Unknown messages");
			TreeItem<String> knownMsgsItem = new SectionStringTreeItem("Known messages");
			signatureItem.getChildren().addAll(unknownMsgsItem, knownMsgsItem);

			for (String msg : strand.mUnknownSMsgs)
			{
				TreeItem<String> msgItem = new TreeItem<>(msg);
				unknownMsgsItem.getChildren().add(msgItem);
			}

			for (String msg : strand.mKnownSMsgs)
			{
				TreeItem<String> msgItem = new TreeItem<>(msg);
				knownMsgsItem.getChildren().add(msgItem);
			}
		}

		for (String intruderData : modelNode.mIntruderKnowledge)
		{
			TreeItem<String> intruderDataItem = new TreeItem<>(intruderData);
			intruderKItem.getChildren().add(intruderDataItem);
		}

		for (String ghost : modelNode.mGhosts)
		{
			TreeItem<String> ghostItem = new TreeItem<>(ghost);
			ghostsItem.getChildren().add(ghostItem);
		}

		if (modelNode.mPropertiesText != null)
		{
			TreeItem<String> propsTreeItem = new TreeItem<>(modelNode.mPropertiesText);
			propertiesItem.getChildren().add(propsTreeItem);
		}

		Background background = new Background(Arrays.asList(), null);
		mMsgFlowStackPane.setBackground(background);
		LinkedList<GridPane> msgInfoGridPaneList = new LinkedList<>();
		int msgInfoChanSide = LEFT_MSG_INFO_COLUMN_INDEX;
		boolean isEvenMsgGrup = true;
		IdSystemNode.MsgElement prevMsgElem = null;
		IdSystemNode auxModelNode = modelNode;

		do
		{
			int start = msgInfoGridPaneList.size();
			int end = start + auxModelNode.mMsgElemSequences.size();

			if (auxModelNode.getParent() != null)
			{
				end -= auxModelNode.getParent().mMsgElemSequences.size();
			}

			// Build the related node ID indicator
			Label label = new Label(auxModelNode.unparseIdUnspaced());
			label.getStyleClass().add("msgGroupIdLabel");
			VBox vBox = new VBox(label);
			vBox.setAlignment(Pos.CENTER);
			mMsgFlowGridPane.add(vBox, MSGS_GROUPS_ID_COLUMN_INDEX, start, 1, end - start);

			// Build the message sequences that interact with the communication channel
			for (int i = start, localAuxNodeIndex = 0; i < end; i++, localAuxNodeIndex++)
			{
				IdSystemNode.MsgElement msgElem = modelNode.mMsgElemSequences.get(i);
				TreeItem<String> auxTreeItem = new TreeItem<>(msgElem.mMsg);
				msgSequenceItem.getChildren().add(auxTreeItem);

				if (msgElem.mIsChannelMsg)
				{
					GridPane gridPane = new GridPane();
					Label signatureLabel = new Label(msgElem.mSignature);
					signatureLabel.getStyleClass().add("signatureLabel");
					GridPane.setHalignment(signatureLabel, HPos.CENTER);
					gridPane.add(signatureLabel, 1, 0);
					Label msgLabel = new Label(msgElem.mMsg);
					msgLabel.getStyleClass().add("msgLabel");
					GridPane.setValignment(msgLabel, VPos.CENTER);
					gridPane.add(msgLabel, 1, 1);
					ImageView image;

					// Highlight the message if learned by intruder in the current state
					if (auxModelNode.mMsgElemSequences.get(localAuxNodeIndex).mIsInSelfIntruderKnowledge)
					{
						msgLabel.pseudoClassStateChanged(INTRUDER_LEARNED_STATE_PSEUDO_CLASS, true);
					}

					// Check if we must use default sides for the message info box.
					// When the previous message has same signature than current, it means both
					// belong to the same principal, so we keep the side for good.
					if (prevMsgElem != null && !msgElem.mSignature.equals(prevMsgElem.mSignature))
					{
						if (msgInfoChanSide == LEFT_MSG_INFO_COLUMN_INDEX)
						{
							msgInfoChanSide = RIGHT_MSG_INFO_COLUMN_INDEX;
						}
						else
						{
							msgInfoChanSide = LEFT_MSG_INFO_COLUMN_INDEX;
						}
					}

					if (msgInfoChanSide == LEFT_MSG_INFO_COLUMN_INDEX)
					{
						gridPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
					}

					if (msgElem.mIsSend)
					{
						image = new ImageView("/jbui/resource/send_arrow.png");

						if (msgInfoChanSide == RIGHT_MSG_INFO_COLUMN_INDEX)
						{
							image.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
						}
					}
					else
					{
						image = new ImageView("/jbui/resource/recv_arrow.png");

						if (msgInfoChanSide == LEFT_MSG_INFO_COLUMN_INDEX)
						{
							image.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
						}
					}

					gridPane.add(image, 0, 1);
					image.setPreserveRatio(true);
					image.setFitHeight(msgLabel.getFont().getSize() * MSG_IMAGE_TO_TEXT_HEIGHT_FIT_RATIO);
					mMsgFlowGridPane.add(gridPane, msgInfoChanSide, msgInfoGridPaneList.size());
					msgInfoGridPaneList.add(gridPane);
					gridPane.getStyleClass().add("msgInfoGridPane");
					prevMsgElem = msgElem;
				}
			}

			if (start > 0)
			{
				// Add alternated background coloring
				Color color = isEvenMsgGrup ? Color.WHITE : Color.LIGHTGRAY;

				msgInfoGridPaneList.get(start).localToParentTransformProperty()
						.addListener((observable, oldTransform, newTransform) ->
						{
							double startY = newTransform.getTy();

							if (startY > 0)
							{
								Insets insets = new Insets(startY, 0, 0, 0);
								BackgroundFill bgFill = new BackgroundFill(color, null, insets);
								List<BackgroundFill> bgFills = new ArrayList<>(
										mMsgFlowStackPane.getBackground().getFills());
								bgFills.add(bgFill);
								Background newBackground = new Background(bgFills, null);
								mMsgFlowStackPane.setBackground(newBackground);
							}
						});
			}

			isEvenMsgGrup = !isEvenMsgGrup;
		}
		while ((auxModelNode = auxModelNode.getParent()) != null);
	}

	@FXML
	private void initialize()
	{
		ControllerUtil.setPanningHandScrollPaneCursors(mMsgFlowScrollPane);
		mFullInfoTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		PseudoClass leafPseudoClass = PseudoClass.getPseudoClass("leaf");

		// Set custom cells to allow basic styling
		mFullInfoTreeView.setCellFactory(treeView ->
		{
			TreeCell<String> cell = new TreeCell<String>()
			{
				@Override
				public void updateItem(String item, boolean empty)
				{
					super.updateItem(item, empty);
					setText(item);
				}
			};

			cell.treeItemProperty().addListener((obs, oldTreeItem, newTreeItem) ->
			{
				if (newTreeItem != null)
				{
					// We need to mark leafs via CSS states instead of style classes because during
					// expand/collapse events, isLeaf() may return false but will return true once
					// layout is complete, thus complicating the conditions for correct styling
					cell.pseudoClassStateChanged(leafPseudoClass, !(newTreeItem instanceof SectionStringTreeItem));
				}
			});

			return cell;
		});

		// Set the sections copy-to-clipboard functionality
		KeyCodeCombination copyKeyCode = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);

		mFullInfoTreeView.setOnKeyPressed(keyEvent ->
		{
			if (copyKeyCode.match(keyEvent))
			{
				createCopyToClipboardMenu();
			}
		});

		mFullInfoTreeView.setOnMouseClicked(mouseEvent ->
		{
			if (mouseEvent.getButton() == MouseButton.SECONDARY)
			{
				MenuItem copyMenuItem = new MenuItem("Copy");
				ContextMenu contextMenu = new ContextMenu(copyMenuItem);
				contextMenu.show(mFullInfoTreeView.getScene().getWindow(), mouseEvent.getScreenX(),
						mouseEvent.getScreenY());
				copyMenuItem.setOnAction(event -> createCopyToClipboardMenu());
			}
		});

		if (mProtocolSaveFile != null)
		{
			notifyNewSaveFile(mProtocolSaveFile);
		}

		mNotesSaveBtn.setOnAction(actionEvent ->
		{
			if (mProtocolSaveFile == null)
			{
				mProtocolSaveFile = showJSONSavePathDialog(JBUI.sInstance.mLastTreeSaveDirectory);

				if (mProtocolSaveFile == null)
				{
					return;
				}
			}

			saveCurrentProtocol();
		});

		mNotesTextArea.setText(mProtocolSaveRootNode.mNotes);

		mNotesTextArea.textProperty().addListener((observable, oldText, newText) ->
		{
			mProtocolSaveRootNode.mNotes = newText;

			if (mProtocolSaveFile != null)
			{
				if (mNotesAutoSaveCheck.isSelected())
				{
					saveCurrentProtocol();
					return;
				}

				mNotesSaveBtn.setDisable(false);
			}
		});
	}

	private void saveCurrentProtocol()
	{
		try
		{
			handleCurrentProtocolSaveQueued(new NodeDetailsJSONTreeSaveDAO(this));
			mNotesSaveBtn.setDisable(true);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
}
