package jbui.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import jbui.model.IdSystemNode;

public class NodeDetailController // NO_UCD
{
	private static int MSGS_GROUPS_ID_COLUMN_INDEX = 4;
	private static int RECV_MSGS_COLUMN_INDEX = 2;
	private static int SEND_MSGS_COLUMN_INDEX = 0;

	@FXML
	private GridPane mMsgFlowGridPane;

	@FXML
    private StackPane mMsgFlowStackPane;

	private void addListenerForAlternatedBackgroundPainting(HBox hBox, Color color)
	{
		hBox.localToParentTransformProperty().addListener((observable, oldTransform, newTransform) ->
		{
			double startY = newTransform.getTy();

			if (startY > 0)
			{
				Insets insets = new Insets(startY, 0, 0, 0);
				BackgroundFill bgFill = new BackgroundFill(color, null, insets);
				List<BackgroundFill> bgFills = new ArrayList<>(mMsgFlowStackPane.getBackground().getFills());
				bgFills.add(bgFill);
				Background background = new Background(bgFills, null);
				mMsgFlowStackPane.setBackground(background);
			}
		});
	}

	void init(IdSystemNode modelNode)
	{
		Background background = new Background(Arrays.asList(), null);
		mMsgFlowStackPane.setBackground(background);
		LinkedList<HBox> msgHBoxList = new LinkedList<>();
		boolean isEvenMsgGrup = true;

		do
		{
			int curSeqFirstMsgIndex = msgHBoxList.size();

			// Build the message sequences that interact with the communication channel
			for (IdSystemNode.MsgElement msgElem : modelNode.mMsgElemSequences)
			{
				HBox hBox = new HBox();
				hBox.setAlignment(Pos.CENTER_LEFT);
				ImageView image;

				if (msgElem.mIsSend)
				{
					hBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
					image = new ImageView("/jbui/resource/send_arrow.png");
					mMsgFlowGridPane.add(hBox, SEND_MSGS_COLUMN_INDEX, msgHBoxList.size());
				}
				else
				{
					image = new ImageView("/jbui/resource/recv_arrow.png");
					mMsgFlowGridPane.add(hBox, RECV_MSGS_COLUMN_INDEX, msgHBoxList.size());
				}

				image.setPreserveRatio(true);
				image.setFitHeight(40);
				Label label = new Label(msgElem.mMsg);
				hBox.getChildren().addAll(image, label);
				msgHBoxList.add(hBox);
			}

			if (curSeqFirstMsgIndex > 0)
			{
				addListenerForAlternatedBackgroundPainting(msgHBoxList.get(curSeqFirstMsgIndex),
						isEvenMsgGrup ? Color.WHITE : Color.LIGHTGRAY);
			}

			// Build the related node ID indicator
			Label label = new Label(modelNode.unparseIdUnspaced());
			VBox vBox = new VBox(label);
			vBox.setAlignment(Pos.CENTER);
			mMsgFlowGridPane.add(vBox, MSGS_GROUPS_ID_COLUMN_INDEX, curSeqFirstMsgIndex, 1,
					msgHBoxList.size() - curSeqFirstMsgIndex);

			isEvenMsgGrup = !isEvenMsgGrup;
			modelNode = modelNode.getParent();
		}
		while (modelNode != null);

		msgHBoxList.getFirst().getStyleClass().add("edgeMsgHBox");
		msgHBoxList.getLast().getStyleClass().add("edgeMsgHBox");
	}
}
