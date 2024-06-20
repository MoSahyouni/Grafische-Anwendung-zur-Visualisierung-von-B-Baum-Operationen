package application;

import javafx.fxml.FXML;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.beans.InvalidationListener;

public class MainsceneController {
	@FXML
	public Button start;
	@FXML
	public  Pane controllDescriptionpane;
	@FXML
	private TextField  eingabeField;
	@FXML
	private TextField  deleteInput;
	@FXML
	private AnchorPane  mainWindow;
	@FXML
	public  Pane drawingpane;
	@FXML
	public  Pane infopane;
	@FXML
	public  Pane controllpane;
	@FXML
	public  Slider animationspeed;
	@FXML
	public  Slider positionslider;
	@FXML
	public ToggleButton bbaumbtn;
	@FXML
	public Button addBtn;
	@FXML
	public Button deleteBtn;
	@FXML
	public Label titel;
	@FXML
	public Button removeBtn;
	@FXML
	public Button pauseBtn;
	
	public BooleanProperty drawingPaneAnimation = new SimpleBooleanProperty(false);
	public static Transition runningTransition = null;
	private BinSTree bSTree = new BinSTree();
	private BTree Btree = new BTree(2);
	public treeType Art = treeType.BTree;
	enum treeType{BTree, BST}
	static int offset = 120;
	// Event Listener on Button.onAction
	@FXML
	public void addClick(ActionEvent event) throws InterruptedException {
		// if method called with Enter-key, check for running Animations
		if(drawingPaneAnimation.getValue() == true) return;
		
		if(Art == treeType.BST)
		{
			try {
				String val = eingabeField.getText();
				eingabeField.deleteText(0, val.length());
				int wert = Integer.parseInt(val);
				
				if(bSTree.root==null)
				{
					bSTree.root = new BSNode(wert);
					DrawBinSearchTree.drawingpane = drawingpane;
					DrawBinSearchTree.animationspeed = animationspeed;
					DrawBinSearchTree.infopane = infopane;
					DrawBinSearchTree.drawingPaneAnimation = drawingPaneAnimation;
				}
				else {
					bSTree.add(wert);
				}
				int offset = 40;
				this.resizeHandler();
				DrawBinSearchTree.StartAnimation(drawingpane, bSTree.root, (int)drawingpane.getWidth()/2, 50, offset);
				
			}catch(NumberFormatException e) {
				System.err.println("Invalid Input");
			}
		
		}
		else {
			try {
				String val = eingabeField.getText();
				eingabeField.deleteText(0, val.length());
				int wert = Integer.parseInt(val);
				if(Btree.root==null)
				{
					
					Btree.insert(wert);
					DrawBTree.drawingpane = drawingpane;
					DrawBTree.animationspeed = animationspeed;
					DrawBTree.infopane = infopane;
					DrawBTree.drawingPaneAnimation = drawingPaneAnimation;
					
					DrawBTree.root = Btree.root; 
					DrawBTree.offsetX = 35;
					DrawBTree.CalculateOffSetWithBoundsInParent(35);
					DrawBTree.drawSubtree(drawingpane, Btree.root,(int)drawingpane.getWidth()/2, 50, 35);
					
				}
				else {
					if(Btree.search(wert)) {
						sharedMethodes.KeyalreadyInTree(wert);
						return;
					}
					Btree.insert(wert);
				}
				
				DrawBTree.positionAdjusted.clear();
				DrawBTree.StartAnimation(drawingpane, Btree.root, (int)drawingpane.getWidth()/2, 50, 50);
				DrawBTree.root = Btree.root; 
				
			}catch(NumberFormatException e) {
				System.err.println("Invalid Input");
			}
		}
	
	}
	@FXML
	public void bbaumBtn() {
		drawingpane.getChildren().clear();
		infopane.getChildren().clear();
		if(Art == treeType.BTree)
		{
			Art =  treeType.BST;
			bbaumbtn.textProperty().set("BST");
			DrawBinSearchTree.alleNodes = new ArrayList<DrawBinSearchTree>();
			if(bSTree.root !=null)DrawBinSearchTree.StartAnimation(drawingpane, bSTree.root, (int)drawingpane.getWidth()/2, 50, 40);
			
		}else {
			Art = treeType.BTree;
			DrawBTree.moved.clear();
			DrawBTree.positions.clear();
			DrawBTree.positionAdjusted.clear();
			DrawBTree.root = Btree.root;
			bbaumbtn.textProperty().set("B-Tree");
			if(Btree.root != null)
			{
				DrawBTree.drawSubtree(drawingpane, Btree.root, (int)drawingpane.getWidth()/2, 50, 35);
				int[] toAdjust = DrawBTree.checkALLPosition();
	            if(toAdjust!= null) DrawBTree.adjustPositions(toAdjust[0], toAdjust[1]);
	            DrawBTree.positionAdjusted.clear();
	            toAdjust = DrawBTree.checkALLPosition();
	            if(toAdjust!= null) DrawBTree.adjustPositions(toAdjust[0], toAdjust[1]);
	            DrawBTree.positionAdjusted.clear();
	            DrawBTree.recenterTree(Btree.root);
			}
		}
	}
	@FXML
	public void removeTree() {
		if(Art == treeType.BTree)
		{
			Btree.root= null;
			DrawBTree.moved.clear();
			DrawBTree.positionAdjusted.clear();
		}else {
			bSTree.root = null;
			DrawBinSearchTree.alleNodes = new ArrayList<DrawBinSearchTree>();
		}
		
		
		drawingpane.getChildren().clear();
		infopane.getChildren().clear();
		
	}
	@FXML
	public void deleteKey()
	{	
		// if method called with Enter-key, check for running Animations
		if(drawingPaneAnimation.getValue() == true) return;
		int wert =0;
		try {
			String val = deleteInput.getText();
			deleteInput.deleteText(0, val.length());
			wert = Integer.parseInt(val);	
			if(Art == treeType.BST)
			{	
				if(!BinSTree.search(bSTree.root, wert)) {
					sharedMethodes.KeyNotInTree(wert);
					return;
				}
				bSTree.root = bSTree.delete(wert);
				int offset = 40;
				DrawBinSearchTree.updateNodes(bSTree.root);
				DrawBinSearchTree.StartAnimation(drawingpane, bSTree.root, (int)drawingpane.getWidth()/2, 50, offset);
			}else {
				DrawBTree.positionAdjusted.clear();
				if(!Btree.search(wert)) {
					sharedMethodes.KeyNotInTree(wert);
					return;
				}
				if(Btree.root.children.size()==0&&Btree.root.keys.size()==1) {
					DrawBTree.animationRemoveKey(wert);
					Btree.root =null;
				}
				else {Btree.delete(wert);
				DrawBTree.root=Btree.root;}
				DrawBTree.StartAnimation(drawingpane, Btree.root, (int)drawingpane.getWidth()/2, 50, 50);
				DrawBTree.root = Btree.root;
			}
		}catch(NumberFormatException e) {
			System.err.println("Invalid Input");
		}
	}
	@FXML
	public void pasueAnimation()
	{
		if(runningTransition == null ||runningTransition.statusProperty().getValue() == Animation.Status.STOPPED ) return; 
		else pauseBtn.getStyleClass().remove(pauseBtn.getStyleClass().size()-1);
		
		if(runningTransition.statusProperty().getValue() == Animation.Status.RUNNING)
		{
			runningTransition.pause();
			pauseBtn.textProperty().set("Play");
			pauseBtn.getStyleClass().add("play");
		}
		else if(runningTransition.statusProperty().getValue() == Animation.Status.PAUSED)
		{
			runningTransition.play();
			pauseBtn.textProperty().set("Pause");
			pauseBtn.getStyleClass().add("pause");
		}
		
	}
	
	
	public void resizeHandler()
	{
		
		drawingpane.widthProperty().addListener(new InvalidationListener() {
			
			@Override
			public void invalidated(Observable observable) {
				double newMidX = drawingpane.getBoundsInParent().getCenterX();
				System.out.println("res");
			    controllpane.setLayoutX(newMidX-controllpane.getWidth()/2);
				titel.setLayoutX(newMidX-titel.getWidth()/2);
				
			
			}
		});
	}
	
	public void speeedhandler()
	{
		animationspeed.valueProperty().addListener((obs, oldValue, newValue) -> {
		    if(animationspeed.getValue()==0) animationspeed.setValue(1);
		});
	}
	public  void positionsliderHandler()
	{
		positionslider.valueChangingProperty().addListener((obs, oldValue, newValue) -> {
			System.out.print("vC");
			// Check if the value change is complete
	        if (!newValue) {
	            // Calculate the difference between the new value and the initial value
	            double diff = positionslider.getValue() - 50;
	            // Calculate the percentage of the difference relative to the pane width
	            double percentage = diff / 100.0; // Assuming the slider range is 0-100
	            // Calculate the translation based on the percentage and pane width
	            double translationX = percentage * drawingpane.getWidth();
	            // Set the translation
	            ParallelTransition pl = new ParallelTransition();
	            for (javafx.scene.Node node : drawingpane.getChildren()) {
	                //node.setTranslateX(translationX);
	                TranslateTransition tt = new TranslateTransition();
	                tt.setByX(translationX); tt.setDuration(Duration.millis(500));
	                tt.setNode(node);
	                pl.getChildren().add(tt);
	            }
	            positionslider.valueProperty().set(50);
	            pl.play();
	            pl.setOnFinished(event->{
	            	for (javafx.scene.Node node : drawingpane.getChildren()) {
	            		if(node instanceof Rectangle)
	            		{
	            			DrawBTree.moved.put(sharedMethodes.extractNumber(node.getId()),
	            					new double[]{node.getBoundsInParent().getCenterX(),node.getBoundsInParent().getCenterY()});
	            		}else if(node instanceof Circle ) {
	            			DrawBinSearchTree.updateNodesPositions();
	            		}
	            		
	            	}
	            	
	            });
	        }
            
        });
	}
	
	@FXML
	public void start()
	{
		speeedhandler();
		positionsliderHandler();
		animationButtonsHandler();
		resizeHandler();
		sharedMethodes.animationspeed = animationspeed;
		sharedMethodes.drawingpane = drawingpane;
		sharedMethodes.infopane = infopane;
		sharedMethodes.drawingPaneAnimation =drawingPaneAnimation;
		drawingpane.getChildren().remove(controllDescriptionpane);
		controllpane.setVisible(true);
	}
	public void animationButtonsHandler()
	{
		// disable Buttons when drawingPaneAnimation  is true
		addBtn.disableProperty().bind(drawingPaneAnimation);
		bbaumbtn.disableProperty().bind(drawingPaneAnimation);
		deleteBtn.disableProperty().bind(drawingPaneAnimation);
		removeBtn.disableProperty().bind(drawingPaneAnimation);
		positionslider.disableProperty().bind(drawingPaneAnimation);
	}
}
