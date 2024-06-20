package application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import javafx.animation.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class DrawBTree {
	static BTreeNode root;
	double[] position ;
	public static BooleanProperty drawingPaneAnimation=null;
	public static ArrayList<DrawBTree> alleNodes = new ArrayList<DrawBTree>();
	public static Queue<PathTransition> Animations = new LinkedList<>();
	public static List<Pair<Integer, Integer>> positionAdjusted = new ArrayList<>();
	public static Queue<ParallelTransition> ChildSplitAnimations = new LinkedList<>();
	public static Queue<ParallelTransition> RootSplitAnimations = new LinkedList<>();
	public static Queue<ParallelTransition> deleteAnimations = new LinkedList<>();
	public static Queue<ParallelTransition> mergeAnimations = new LinkedList<>();
	public static Queue<ParallelTransition> SuccessorMergeAnimations = new LinkedList<>();
	public static Queue<ParallelTransition> borrowAnimations = new LinkedList<>();
	public static Queue<ParallelTransition> moveSuccessorAnimations = new LinkedList<>();
	public static Queue<ParallelTransition> addAnimations = new LinkedList<>();
	public static Queue<PathTransition> successorAnimations = new LinkedList<>();
	public static Map<Integer, double[]> positions = new HashMap<>();
	public static Map<Integer, double[]> moved = new HashMap<>();
	public static Pane drawingpane;
	public static Pane infopane;
	public static Slider animationspeed;
	public static double offsetX =20;
	public static boolean recenterAfterLines = false;
	public static int minSpaceBetweenNodes = 45;
	
	
	public static void StartAnimation(Pane drawingpane, BTreeNode root, int x, int y, int offset)
	{	
		if(!deleteAnimations.isEmpty()|| !mergeAnimations.isEmpty())
		{
			SequentialTransition lastTransition = new SequentialTransition();
			while (!Animations.isEmpty()) {
	        	lastTransition.getChildren().add(Animations.poll());
	        }
	        while (!mergeAnimations.isEmpty()) {
	        	lastTransition.getChildren().add(mergeAnimations.poll());
	        }
	        while (!successorAnimations.isEmpty()) {
	        	lastTransition.getChildren().add(successorAnimations.poll());
	        }
	        while (!SuccessorMergeAnimations.isEmpty()) {
	        	lastTransition.getChildren().add(SuccessorMergeAnimations.poll());
	        }
	        while (!borrowAnimations.isEmpty()) {
	        	lastTransition.getChildren().add(borrowAnimations.poll());
	        }
	        while (!moveSuccessorAnimations.isEmpty()) {
	        	lastTransition.getChildren().add(moveSuccessorAnimations.poll());
	        }
	        while (!deleteAnimations.isEmpty()) {
	        	lastTransition.getChildren().add(deleteAnimations.poll());
	        }
	        lastTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.RUNNING) {
	            	MainsceneController.runningTransition = lastTransition;
	            	drawingPaneAnimation.set(true);
	            }
	        });
			lastTransition.play();
			lastTransition.setOnFinished(event->{
				if(root != null )
					recenterTree(root);
				else 
					drawingPaneAnimation.set(false);
				});
		}
		else {
		SequentialTransition sequentialTransition = new SequentialTransition();
		while (!RootSplitAnimations.isEmpty()) {
            sequentialTransition.getChildren().add(RootSplitAnimations.poll());
        }
		while (!Animations.isEmpty()) {
            sequentialTransition.getChildren().add(Animations.poll());
        }
		while (!ChildSplitAnimations.isEmpty()) {
            sequentialTransition.getChildren().add(ChildSplitAnimations.poll());
        }
        while(!addAnimations.isEmpty())
        	sequentialTransition.getChildren().add(addAnimations.poll());
        sequentialTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.STOPPED) {
            Animations = new LinkedList<>();
            recenterTree(root);
            drawingPaneAnimation.set(false);
        }});
        sequentialTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	MainsceneController.runningTransition = sequentialTransition;
            	drawingPaneAnimation.set(true);
            }
            
        });
        sequentialTransition.play();
        }
		
	}
	
	public static void drawSubtree(Pane pane, BTreeNode node, double x, double y, double offsetX) {
		positionAdjusted.clear();
		if (node != null) {
			
            // Draw the node
        	int childrenDepth =0;
        	for(BTreeNode child: node.children)
        	{  childrenDepth++;
        		while(child.children.size()!=0)
        		{
        			child=child.children.get(0);
        			childrenDepth++;
        		}
        		break;
        	}
        	offsetX = DrawBTree.offsetX *(childrenDepth+1);
        	List<javafx.scene.Node> toDrawItems = new ArrayList<>();
        	double positionToStart = x - ((node.keys.size()-1 )*16);
        	
        	for(Integer i : node.keys)
        	{
        		Rectangle rectangle = new Rectangle(positionToStart, y-15 , 30, 30);
            	rectangle.setFill(Color.WHITE);
            	rectangle.setStroke(Color.BLACK);
            	rectangle.setId("rec"+i);
            	double textX = positionToStart + 10; // Adjusted X coordinate for text
                double textY = y+5; // Adjusted Y coordinate for text
                Text tex = new Text(textX, textY, String.valueOf(i));
                tex.setId("text"+i);
                positionToStart +=32;
                toDrawItems.add(rectangle);toDrawItems.add(tex);
        	}
        	drawingpane.getChildren().addAll( toDrawItems);
        	
        	// Draw edges to children and call the drawing methode again 
            if (node.children.size() > 0 && node.children.get(0) != null) {
                Line leftLine = new Line(x-3, y-3, x - offsetX, y + 50);
                leftLine.setId("l"+node.keys.get(0));
                pane.getChildren().add(leftLine);
                drawSubtree(pane, node.children.get(0), x - offsetX, y + 50, offsetX);
            }
            if (node.children.size() > 1 && node.children.get(1) != null) {
                Line middleLine = new Line(x, y-3, x, y + 50-3);
                pane.getChildren().add(middleLine);
                middleLine.setId("l"+node.keys.get(0));
                drawSubtree(pane, node.children.get(1), x, y + 50, offsetX);
            }
            if (node.children.size() > 2 && node.children.get(2) != null) {
                Line rightLine = new Line(x+3, y, x + offsetX, y + 50-3);
                pane.getChildren().add(rightLine);
                rightLine.setId("l"+node.keys.get(1));
                drawSubtree(pane, node.children.get(2), x + offsetX, y + 50, offsetX);
            }
            if (node.children.size() > 3 && node.children.get(3) != null) {
                Line right2Line = new Line(x+6, y-3, x + offsetX*2, y + 50);
                pane.getChildren().add(right2Line);
                right2Line.setId("l"+node.keys.get(2));
                drawSubtree(pane, node.children.get(3), x + offsetX*2, y + 50, offsetX);
            }
        }
    }
	
	public static void animationToChild(Integer parent, BTreeNode child, int toadd) {
		//PathTransition animation between parent and child 
		int ChildMidK = child.keys.get(child.keys.size()/2);
		Rectangle pRec =  (Rectangle) drawingpane.lookup("#rec"+parent);
		Rectangle cRec =  (Rectangle) drawingpane.lookup("#rec"+ChildMidK);
		
		if(pRec != null && cRec != null)
		{
			double[] pRecpos = {pRec.getBoundsInParent().getCenterX(), pRec.getBoundsInParent().getCenterY()};
			double[] cRecpos = {cRec.getBoundsInParent().getCenterX(), cRec.getBoundsInParent().getCenterY()};
			if(moved.containsKey(parent))
			{
				pRecpos = Arrays.copyOf(moved.get(parent), moved.get(parent).length);
			}
			if(moved.containsKey(ChildMidK))
			{
				cRecpos = Arrays.copyOf(moved.get(ChildMidK), moved.get(ChildMidK).length);
			}
			if(parent>ChildMidK) pRecpos[0]-=16; else pRecpos[0]+=16;
			DrawBTree.animationNodeToNode(pRecpos, cRecpos,drawingpane,parent,toadd);
		}
		
	}
	public static void animationToSuccessorOrPredecessor(Integer parent, BTreeNode child, int toDelete) {
		//PathTransition animation between parent and child 
		int ChildMidK = child.keys.get(child.keys.size()/2);
		Rectangle pRec =  (Rectangle) drawingpane.lookup("#rec"+parent);
		Rectangle cRec =  (Rectangle) drawingpane.lookup("#rec"+ChildMidK);
		//get positions and call animationNodeToSuccOrPred
		if(pRec != null && cRec != null)
		{
			double[] pRecpos = {pRec.getBoundsInParent().getCenterX(), pRec.getBoundsInParent().getCenterY()};
			double[] cRecpos = {cRec.getBoundsInParent().getCenterX(), cRec.getBoundsInParent().getCenterY()};
			if(moved.containsKey(parent))
			{
				pRecpos = Arrays.copyOf(moved.get(parent), moved.get(parent).length);
			}
			if(moved.containsKey(ChildMidK))
			{
				cRecpos = Arrays.copyOf(moved.get(ChildMidK), moved.get(ChildMidK).length);
			}
			if(parent>ChildMidK) pRecpos[0]-=16; else pRecpos[0]+=16;
			
			DrawBTree.animationNodeToSuccOrPred(pRecpos, cRecpos,drawingpane,parent,toDelete);
		}
	}
	
	public static void animationNodeToSuccOrPred(double[] position1, double[] position2,Pane pane, int parent, int child)
	{	//draw a circle and move it from position1 to position2
		Circle circle = new Circle(5, Color.BLUE);
		circle.setVisible(false);

        pane.getChildren().add(circle);
        
        Path path = new Path();
        path.getElements().add(new MoveTo(position1[0], position1[1]));
        path.getElements().add(new LineTo(position2[0], position2[1]));
        Text infoText = DrawBTree.visualiseSearchSuccOrPred();
        PathTransition pathTransition = DrawBTree.makePathTransition(path, circle);
        pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
                circle.setVisible(true);
                infoText.setVisible(true);
            }
            if (newValue == Animation.Status.STOPPED) {
            pane.getChildren().remove(circle);
            infopane.getChildren().remove(infoText);
            }
        });
        DrawBTree.successorAnimations.add(pathTransition);
	}


	public static void animationNodeToNode(double[] position1, double[] position2,Pane pane, int parent, int child)
	{	//draw a circle and move it from position1 to position2
		Circle circle = new Circle(5, Color.BLUE);
		circle.setVisible(false);

        pane.getChildren().add(circle);
        
        Path path = new Path();
        path.getElements().add(new MoveTo(position1[0], position1[1]));
        path.getElements().add(new LineTo(position2[0], position2[1]));
        Text infoText = DrawBTree.visualiseStep(parent, child);
        PathTransition pathTransition = DrawBTree.makePathTransition(path, circle);
        pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
                circle.setVisible(true);
                infoText.setVisible(true);
            }
            
        });
        pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Animation.Status.STOPPED) {
            pane.getChildren().remove(circle);
            infopane.getChildren().remove(infoText);}
        });
        ParallelTransition pl = new ParallelTransition();
        pl.getChildren().add(pathTransition);
        //if a mergeAnimation has been created the add the pathtransition to 
        // mergeAnimation List so it will be animated after the merge
        if(mergeAnimations.isEmpty()) Animations.add(pathTransition);
        else mergeAnimations.add(pl);
	}
	
	public static void AnimationSplitRoot(List<Integer> rootKeys,int oldrootChildrensize,BTreeNode newroot, int toadd) {
		ParallelTransition parallelTransition = new ParallelTransition();
		Queue<Integer> tomove = new LinkedList<>();
		for(Integer n: rootKeys)
		{
			if(!newroot.keys.contains(n)) tomove.add(n);
		}
		Text infotext =visualiseSplittingRoot();
		while(!tomove.isEmpty())
		{	
			Integer val = tomove.poll();
			
			Text tex = (Text) drawingpane.lookup("#text"+val);
			Rectangle rectangle =  (Rectangle) drawingpane.lookup("#rec"+val);
			double[] oldPosition = {rectangle.getBoundsInParent().getCenterX(), rectangle.getBoundsInParent().getCenterY()};
			if(moved.containsKey(val)) oldPosition = Arrays.copyOf(moved.get(val), moved.get(val).length);
			double[] newposition = {0, rectangle.getBoundsInParent().getCenterY()+50};
			if(newroot.children.get(0).keys.contains(val))
			{
				double x = rectangle.getBoundsInParent().getCenterX()-offsetX;
				int indexVal = newroot.children.get(0).keys.indexOf(val);
				newposition[0] = (x - ((newroot.children.get(0).keys.size()-1 )*15)%2) -32 *indexVal;
			}
			else if(newroot.children.get(1).keys.contains(val))
			{
				double x = rectangle.getBoundsInParent().getCenterX();
				int indexVal = newroot.children.get(1).keys.indexOf(val);
				newposition[0] = (x - ((newroot.children.get(1).keys.size()-1 )*15)%2) -32 *indexVal;
			}
			else if(newroot.children.get(2).keys.contains(val))
			{
				double x = rectangle.getBoundsInParent().getCenterX()+offsetX;
				int indexVal = newroot.children.get(2).keys.indexOf(val);
				newposition[0] = (x - ((newroot.children.get(2).keys.size()-1 )*15)%2) -32 *indexVal;
			}
			else if(newroot.children.get(3).keys.contains(val))
			{
				double x = rectangle.getBoundsInParent().getCenterX()+offsetX*2;
				int indexVal = newroot.children.get(3).keys.indexOf(val);
				newposition[0] = (x - ((newroot.children.get(3).keys.size()-1 )*15)%2) -32 *indexVal;
			}
			
			Path path = new Path();
	        path.getElements().add(new MoveTo(oldPosition[0], oldPosition[1]));
	        path.getElements().add(new LineTo(newposition[0], newposition[1]));
	        
	        moved.put(val, newposition);
	        
	        PathTransition pathTransition = DrawBTree.makePathTransition(path, rectangle);
	        PathTransition texpathTransition = DrawBTree.makePathTransition(path, tex);
	        pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.RUNNING) {
	                infotext.setVisible(true);
	                changeRectanglesColor(rootKeys,Color.RED);
	            }   
	        });

	        parallelTransition .getChildren().add(pathTransition);
	        parallelTransition.getChildren().add(texpathTransition);
		}
		parallelTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.STOPPED) {
			infopane.getChildren().remove(infotext);
            changeRectanglesColor(rootKeys,Color.WHITE);}
		});
		RootSplitAnimations.add(parallelTransition);
	}

	public static void animationInserttoNode(List<Integer> keys, int key) 
	{	//animation for adding the key to the node
		ParallelTransition parallelTransition = new ParallelTransition();
		double[] newkeyPos = null;
		Text infotext = visualiseAddStep(key);
		//moving the old keys to make space for the new key
		for(Integer val : keys)
		{	
			if(val == key) continue;
			Text tex = (Text) drawingpane.lookup("#text"+val);
			Rectangle pc =  (Rectangle) drawingpane.lookup("#rec"+val);
			double[] oldPosition = {pc.getBoundsInParent().getCenterX(), pc.getBoundsInParent().getCenterY()};
			if(moved.containsKey(val)) {
				oldPosition = Arrays.copyOf(moved.get(val), moved.get(val).length);
			}
			double[] newposition = {oldPosition[0], oldPosition[1]};
			if(val<key)
			{
				newposition[0] -=16;
				newkeyPos = new double[]{newposition[0], newposition[1]};
				newkeyPos[0]+=16;
			}
			else {
				newposition[0] +=16;
			}
			Path path = new Path();
	        path.getElements().add(new MoveTo(oldPosition[0], oldPosition[1]));
	        path.getElements().add(new LineTo(newposition[0], newposition[1]));
	        
	        
	        PathTransition pathTransition = DrawBTree.makePathTransition(path, pc);
	        pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.RUNNING) {
	                pc.setVisible(true);
	                tex.setVisible(true);
	            }   
	        });
	        PathTransition texpathTransition = DrawBTree.makePathTransition(path, tex);
	        parallelTransition.getChildren().add(pathTransition);
	        parallelTransition.getChildren().add(texpathTransition);
	        moved.put(val, newposition);  
		}
 
		addAnimations.add(parallelTransition);
		//set the position for the new key
		if(newkeyPos == null)
		{
			Rectangle rec =  (Rectangle) drawingpane.lookup("#rec"+keys.get(1));
			newkeyPos = new double[]{rec.getBoundsInParent().getCenterX()-32, rec.getBoundsInParent().getCenterY()};
			if(moved.containsKey(keys.get(1)))
				{
					newkeyPos = Arrays.copyOf(moved.get(keys.get(1)), moved.get(keys.get(1)).length);
					newkeyPos[0]-=48;
				}
		}
		Rectangle rec =  (Rectangle) drawingpane.lookup("#rec"+key);
		newkeyPos[0]+=16;
		//if its anew key and not moved from other node, 
		//draw the rectangle and value for the key
		//make the transition to move it 
		if(rec ==null)
		{
		parallelTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	changeRectanglesColor(keys,Color.LIGHTGREEN);
            	infotext.setVisible(true);}});
		Rectangle rectangle = new Rectangle(newkeyPos[0]+105, 0 , 30, 30);
    	rectangle.setFill(Color.WHITE);
    	rectangle.setStroke(Color.BLACK);
    	rectangle.setId("rec"+key);
        Text tex = new Text(rectangle.getBoundsInParent().getCenterX()-5, rectangle.getBoundsInParent().getCenterY()+5, String.valueOf(key));
        tex.setId("text"+key);
        drawingpane.getChildren().add(rectangle); drawingpane.getChildren().add(tex);
        Path path = new Path();
        path.getElements().add(new MoveTo(rectangle.getBoundsInParent().getCenterX(), rectangle.getBoundsInParent().getCenterY()));
        path.getElements().add(new LineTo(newkeyPos[0], newkeyPos[1] ));
        
        PathTransition pathTransition = DrawBTree.makePathTransition(path, rectangle);
        PathTransition pathTransition2 = DrawBTree.makePathTransition(path, tex);
        pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	rectangle.setVisible(true);
                tex.setVisible(true);
                
            }
            if (newValue == Animation.Status.STOPPED) {
            	infopane.getChildren().remove(infotext);
            	changeRectanglesColor(keys,Color.WHITE);
                
            }
        });
        ParallelTransition plTransition = new ParallelTransition();
        plTransition.getChildren().add(pathTransition);
        plTransition.getChildren().add(pathTransition2);
        addAnimations.add(plTransition);
		}
		
	}

	public static void AnimationSplitChild(List<Integer> oldkeys, List<BTreeNode> oldChildren, List<Integer> splitedChildKeys, BTreeNode node, int key, int indexofSplit) {
		Queue<Integer> tomove = new LinkedList<>();
		Queue<Integer> moveUp = new LinkedList<>();
		Queue<Integer> movefromChildToChild = new LinkedList<>();
		//find the key that should be moved to parent
		for(Integer n: node.keys)
		{	if(oldkeys.contains(n))continue;
			moveUp.add(n);	
		}
		// keys split to children
		for(int i=0; i< oldChildren.size(); i++)
		{
			List<Integer> keys = new ArrayList<>(oldChildren.get(i).keys);
			for(Integer n: keys)
			{	
				if(i == indexofSplit+1)
					{
						if( !node.children.get(i).keys.contains(n))
							if(!tomove.contains(n) && !moveUp.contains(n))
								movefromChildToChild.add(n);
						
					}
			}
			if(i == indexofSplit+1 )break;
		}
		Text infotext = visualiseSplittingChild();
		while(!moveUp.isEmpty())
		{	
			ParallelTransition parallelTransition = new ParallelTransition();
			Integer val = moveUp.poll();
			
			if(val == key) continue;
			boolean allKeysChanged = true;
			double[] pos = {0,0};
			int remainedKey = 0;
			for(Integer k  : node.keys)
			{	
				if(oldkeys.contains(k))
				{
					allKeysChanged &= false;
					remainedKey = k;
					Rectangle rec =  (Rectangle) drawingpane.lookup("#rec"+k);
					if(moved.containsKey(k)) pos = Arrays.copyOf(moved.get(k), moved.get(k).length);
					else{
						pos[0] = rec.getBoundsInParent().getCenterX();
						pos[1] = rec.getBoundsInParent().getCenterY();
						}
					
					if(k == node.keys.get(node.keys.size()-1)|| key<k)break;
				}
				if(k == node.keys.get(node.keys.size()-1))
				{
					k = oldkeys.get(oldkeys.size()-1);
					Rectangle rec =  (Rectangle) drawingpane.lookup("#rec"+k);
					if(moved.containsKey(k)) pos = Arrays.copyOf(moved.get(k), moved.get(k).length);
					else{
						pos[0] = rec.getBoundsInParent().getCenterX();
						pos[1] = rec.getBoundsInParent().getCenterY();
						}
				}
			}
			Text tex = (Text) drawingpane.lookup("#text"+val);
			Rectangle pc =  (Rectangle) drawingpane.lookup("#rec"+val);
			double[] oldPosition = {pc.getBoundsInParent().getCenterX(), pc.getBoundsInParent().getCenterY()};
			if(moved.containsKey(val)) oldPosition = Arrays.copyOf(moved.get(val), moved.get(val).length);
			double[] newposition = {0, oldPosition[1]-50};
			int offsetInNode = (node.keys.size()-node.keys.indexOf(val)) *32; 
			if(!allKeysChanged) 
			{	
				if(val<remainedKey)
				{
					newposition[0] = pos[0]-32;
				}else {
					List<Integer> Keys = new ArrayList<>(node.keys);
					Keys.retainAll(oldkeys);
					//animate moving the key to parent 
					DrawBTree.animationInserttoNode(node.keys, val);
					newposition[0] = pos[0]+16;
				}
			}else {
				newposition[0] = pos[0] -offsetInNode;
			}
			
			Path path = new Path();
	        path.getElements().add(new MoveTo(oldPosition[0], oldPosition[1]));
	        path.getElements().add(new LineTo(newposition[0], newposition[1]));
	        DrawBTree.positions.put(val, newposition);
	        
	        PathTransition pathTransition = DrawBTree.makePathTransition(path, pc);
	        pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.RUNNING) {
	                pc.setVisible(true);
	                changeRectanglesColor(splitedChildKeys,Color.RED);
	                infotext.setVisible(true);
	            }   
	        });
	        PathTransition texpathTransition = DrawBTree.makePathTransition(path, tex);
	        texpathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.RUNNING) {
	                tex.setVisible(true);
	                
	            }});
	        parallelTransition .getChildren().add(pathTransition);
	        parallelTransition.getChildren().add(texpathTransition);
	        ChildSplitAnimations.add(parallelTransition);
	        moved.put(val, newposition);
		}
		
		while(!tomove.isEmpty())
		{	
			ParallelTransition parallelTransition = new ParallelTransition();
			Integer val = tomove.poll();
			Text tex = (Text) drawingpane.lookup("#text"+val);
			Rectangle pc =  (Rectangle) drawingpane.lookup("#rec"+val);
			double[] oldPosition = {pc.getBoundsInParent().getCenterX(), pc.getBoundsInParent().getCenterY()};
			if(moved.containsKey(val)) oldPosition = Arrays.copyOf(moved.get(val), moved.get(val).length);
			double[] newposition = {0, pc.getBoundsInParent().getCenterY()+50};
			if(node.children.get(0).keys.contains(val))
			{
				double x = oldPosition[0]-offsetX;
				int indexVal = node.children.get(0).keys.indexOf(val);
				newposition[0] = (x - ((node.children.get(0).keys.size()-1 )*15)%2) -32 *indexVal;
			}else if(node.children.get(1).keys.contains(val))
			{
				double x = oldPosition[0];
				int indexVal = node.children.get(1).keys.indexOf(val);
				newposition[0] = (x - ((node.children.get(1).keys.size()-1 )*15)%2) -32 *indexVal;
			}else if(node.children.get(2).keys.contains(val))
			{
				double x = oldPosition[0]+offsetX;
				int indexVal = node.children.get(2).keys.indexOf(val);
				newposition[0] = (x - ((node.children.get(2).keys.size()-1 )*15)%2) -32 *indexVal;
			}else if(node.children.get(3).keys.contains(val))
			{
				double x = oldPosition[0]+offsetX*2;
				int indexVal = node.children.get(3).keys.indexOf(val);
				newposition[0] = (x - ((node.children.get(3).keys.size()-1 )*15)%2) -32 *indexVal;
			}
			
			Path path = new Path();
	        path.getElements().add(new MoveTo(oldPosition[0], oldPosition[1]));
	        path.getElements().add(new LineTo(newposition[0], newposition[1]));
	        DrawBTree.positions.put(val, newposition);
	        
	        PathTransition pathTransition = DrawBTree.makePathTransition(path, pc);
	        pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.RUNNING) {
	                pc.setVisible(true);
	                
	            }   
	        });
	        PathTransition texpathTransition = DrawBTree.makePathTransition(path, tex);
	        texpathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.RUNNING) {
	                tex.setVisible(true);
	                
	            }});

	        parallelTransition .getChildren().add(pathTransition);
	        parallelTransition.getChildren().add(texpathTransition);
	        ChildSplitAnimations.add(parallelTransition);
	        moved.put(val, newposition);
		}
		//move the not splited Child to free space
		if(oldChildren.size()< node.children.size())
		{ 	indexofSplit++;
			for(int i= 0; i< node.children.size(); i++)
			{
				if( i== indexofSplit) continue;
				BTreeNode child = node.children.get(i);
				ParallelTransition parallelTransition = new ParallelTransition();
				for(int val: child.keys )
				{
					if(movefromChildToChild.contains(val)) continue;
					
					Text tex = (Text) drawingpane.lookup("#text"+val);
					Rectangle pc =  (Rectangle) drawingpane.lookup("#rec"+val);
					double[] oldPosition = {pc.getBoundsInParent().getCenterX(), pc.getBoundsInParent().getCenterY()};
					if(moved.containsKey(val)) oldPosition = Arrays.copyOf(moved.get(val), moved.get(val).length);
					double[] newposition = {0, oldPosition[1]};
					if(i < indexofSplit) newposition[0]= oldPosition[0] -offsetX;
					else newposition[0]= oldPosition[0] +offsetX;
					Path path = new Path();
			        path.getElements().add(new MoveTo(oldPosition[0], oldPosition[1]));
			        path.getElements().add(new LineTo(newposition[0], newposition[1]));
			        DrawBTree.positions.put(val, newposition);
			        
			        PathTransition pathTransition = DrawBTree.makePathTransition(path, pc);
			        pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
			            if (newValue == Animation.Status.RUNNING) {
			                pc.setVisible(true);
			                
			            }   
			        });
			        PathTransition texpathTransition = DrawBTree.makePathTransition(path, tex);
			        texpathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
			            if (newValue == Animation.Status.RUNNING) {
			                tex.setVisible(true);
			                
			            }});
			        parallelTransition .getChildren().add(pathTransition);
			        parallelTransition.getChildren().add(texpathTransition);
			        moved.put(val, newposition);
				}ChildSplitAnimations.add(parallelTransition);
			}
		}
		//move keys from the splitted child to the new positions in children 
		while(!movefromChildToChild.isEmpty())
		{	 
			ParallelTransition parallelTransition = new ParallelTransition();
			Integer val = movefromChildToChild.poll();
			Text tex = (Text) drawingpane.lookup("#text"+val);
			Rectangle rec =  (Rectangle) drawingpane.lookup("#rec"+val);
			double[] oldPosition = {rec.getBoundsInParent().getCenterX(), rec.getBoundsInParent().getCenterY()};
			if(val <key) oldPosition[0]-=32;
			else oldPosition[0]+=32;
			int indexoldChild = 0;
			for(int i=0; i< oldChildren.size(); i++)
			{	
				if(oldChildren.get(i).keys.contains(val))  
					indexoldChild = i;
			}
			if(moved.containsKey(val)) oldPosition = Arrays.copyOf(moved.get(val), moved.get(val).length);
			double[] newposition = {oldPosition[0]+ offsetX, oldPosition[1]};

			if(node.children.get(0).keys.contains(val))
			{
				double x = oldPosition[0];
				 x-= offsetX * indexoldChild; 
				newposition[0] = x;
			}else if(node.children.get(1).keys.contains(val))
			{
				double x = oldPosition[0];
				 x-= offsetX * (indexoldChild -1); 
				newposition[0] = x;
			}else if(node.children.get(2).keys.contains(val))
			{
				double x = oldPosition[0];
				x-= offsetX * (indexoldChild - 2);  
				newposition[0] = x;
			}else if(node.children.get(3).keys.contains(val))
			{
				double x = oldPosition[0];
				x-= offsetX * (indexoldChild - 3);  
				newposition[0] = x;
			}
			
			Path path = new Path();
	        path.getElements().add(new MoveTo(oldPosition[0], oldPosition[1]));
	        path.getElements().add(new LineTo(newposition[0], newposition[1]));
	        DrawBTree.positions.put(val, newposition);
	        
	        PathTransition pathTransition = DrawBTree.makePathTransition(path, rec);
	        //pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	        //    if (newValue == Animation.Status.RUNNING) {
	        //        rec.setVisible(true);
	        //        tex.setVisible(true);
	        //        
	        //    }   
	        //});
	        PathTransition texpathTransition = DrawBTree.makePathTransition(path, tex);
	        //texpathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            //if (newValue == Animation.Status.RUNNING) {
	                //tex.setVisible(true);
	                
	            //}});
	        //pathTransition.play(); // Starten Sie die Animation
	        parallelTransition .getChildren().add(pathTransition);
	        parallelTransition.getChildren().add(texpathTransition);
	        parallelTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.STOPPED) {
	        	changeRectanglesColor(splitedChildKeys,Color.WHITE);
                infopane.getChildren().remove(infotext);}
	        });
	        ChildSplitAnimations.add(parallelTransition);
	        moved.put(val, newposition);
		}
		
	}

	public static void AnimationSplitRootMoveChildren(List<Integer> rootKeys, int oldrootChildrensize,
			BTreeNode newroot, int key) {
		
		Queue<Integer> tomove = new LinkedList<>();
		
		for(Integer n: rootKeys)
			if(!newroot.keys.contains(n)) tomove.add(n);
		
		Text infotext =visualiseSplittingRoot();
		while(!tomove.isEmpty())
		{	
			ParallelTransition parallelTransition = new ParallelTransition();
			Integer val = tomove.poll();
			BTreeNode child = BTreeNode.search(newroot, val);
			
			Text tex = (Text) drawingpane.lookup("#text"+val);
			Rectangle pc =  (Rectangle) drawingpane.lookup("#rec"+val);
			double[] oldPosition = {pc.getBoundsInParent().getCenterX(), pc.getBoundsInParent().getCenterY()};
			double[] newposition = {0, pc.getBoundsInParent().getCenterY()+50};
			if(newroot.children.get(0).keys.contains(val))
			{
				double x = pc.getBoundsInParent().getCenterX()-offsetX;
				int indexVal = newroot.children.get(0).keys.indexOf(val);
				newposition[0] =(x - ((newroot.children.get(0).keys.size()-1 )*15)%2) +(32 *indexVal+1);
			}else if(newroot.children.get(1).keys.contains(val))
			{
				double x = pc.getBoundsInParent().getCenterX();
				int indexVal = newroot.children.get(1).keys.indexOf(val);
				newposition[0] = (x - ((newroot.children.get(1).keys.size()-1 )*15)%2) -(32 *indexVal);
			}else if(newroot.children.get(2).keys.contains(val))
			{
				double x = pc.getBoundsInParent().getCenterX()+offsetX;
				int indexVal = newroot.children.get(2).keys.indexOf(val);
				newposition[0] = (x - ((newroot.children.get(2).keys.size()-1 )*15)%2) -(32 *indexVal+1);
			}else if(newroot.children.get(3).keys.contains(val))
			{
				double x = pc.getBoundsInParent().getCenterX()+offsetX*2;
				int indexVal = newroot.children.get(3).keys.indexOf(val);
				newposition[0] = (x - ((newroot.children.get(3).keys.size()-1 )*15)%2) -(32 *indexVal);
			}

			Path path = new Path();
	        path.getElements().add(new MoveTo(oldPosition[0], oldPosition[1]));
	        path.getElements().add(new LineTo(newposition[0], newposition[1]));
	        DrawBTree.positions.put(val, newposition);
	        
	        PathTransition pathTransition = DrawBTree.makePathTransition(path, pc);
	        pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.RUNNING) {
	                //pc.setVisible(true);
	                changeRectanglesColor(rootKeys,Color.RED);
	                infotext.setVisible(true);
	            }   
	        });
	        PathTransition texpathTransition = DrawBTree.makePathTransition(path, tex);
	        parallelTransition .getChildren().add(pathTransition);
	        parallelTransition.getChildren().add(texpathTransition);
	        Line line = (Line) drawingpane.lookup("#l"+val);
	        if(line!=null) {
	        	PathTransition linepathTransition = DrawBTree.makePathTransition(path, line);
	        	parallelTransition.getChildren().add(linepathTransition);
	        }
	        moved.put(val, newposition);
	        
	        double xDistance = newposition[0] - oldPosition[0] ;
			double yDistance = newposition[1] - oldPosition[1] ;
			List<Integer> valChildren = child.getAllChildrenKeys();
			for(Integer childKey : valChildren)
			{
				Text text = (Text) drawingpane.lookup("#text"+childKey);
				Rectangle rec =  (Rectangle) drawingpane.lookup("#rec"+childKey);
				double[] from = {rec.getBoundsInParent().getCenterX(), rec.getBoundsInParent().getCenterY()};
				double[] to = {from[0]+xDistance, from[1]+yDistance};
				
				Path path2 = new Path();
		        path2.getElements().add(new MoveTo(from[0], from[1]));
		        path2.getElements().add(new LineTo(to[0], to[1]));
		        DrawBTree.positions.put(childKey, to);
		        moved.put(childKey, to);
		        PathTransition pathTransition2 = DrawBTree.makePathTransition(path2, rec);
		        PathTransition texpathTransition2 = DrawBTree.makePathTransition(path2, text);
		        parallelTransition .getChildren().add(pathTransition2);
		        parallelTransition.getChildren().add(texpathTransition2);
		        
			}
			parallelTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.STOPPED) {
				infopane.getChildren().remove(infotext);
	            changeRectanglesColor(rootKeys,Color.WHITE);}
			});
			RootSplitAnimations.add(parallelTransition);
	        
		}
		
	}
	
	public static void adjustPositions(int key, int key2)
	{
		BTreeNode parent = DrawBTree.root.findLowestCommonAncestor(DrawBTree.root, key, key2);
		List<Integer> childrenIdx = root.getChildIndexesContainingKeys(parent, key, key2);
		double xDis = sharedMethodes.calculateDistance(drawingpane, key, key2)[0];
		List<Integer> childrenKeys = new ArrayList<>();
		childrenKeys = BTree.getKeysAtSameLevel(root, key);
		List<Integer> childrenKeys2 = parent.children.get(childrenIdx.get(1)).getAllChildrenKeys();
		childrenKeys2.addAll(parent.children.get(childrenIdx.get(1)).keys);
		childrenKeys.addAll(childrenKeys2);
		childrenKeys.addAll(parent.children.get(childrenIdx.get(0)).keys);
	
		ParallelTransition parallelTransition = new ParallelTransition();		
		for (javafx.scene.Node obj : drawingpane.getChildren()) {
			String objId = obj.getId();
			if(!(obj instanceof Rectangle)) continue;
			int value = sharedMethodes.extractNumber(objId);
			Text tex = (Text) drawingpane.lookup("#text"+value);
		    if (!childrenKeys.contains(value)) {
		        continue;
		    }
		    int xdis = Math.abs(35-(int)xDis);
		    if(childrenIdx.get(0)> childrenIdx.get(1))
		    {
		    	if(childrenKeys2.contains(value))
		    		xdis *=-1;
		    	else {
		    		if(value < childrenKeys2.get(0))xdis *=-1;
		    		
		    	} 
		    }else
		    if(childrenIdx.get(0)< childrenIdx.get(1))
		    {
		    	if(!childrenKeys2.contains(value) && value<childrenKeys2.get(0))
		    		xdis *=-1;
		    }
		    

            // Create a path that moves horizontally
		    double[] from = {obj.getBoundsInParent().getCenterX(), obj.getBoundsInParent().getCenterY()};
		    if(moved.containsKey(value)) from = Arrays.copyOf(moved.get(value), moved.get(value).length);
		    Path path = new Path();
            path.getElements().add(new MoveTo(from[0],from[1])); // Start at current position
            double[] to = {from[0] + xdis, from[1]};
            path.getElements().add(new LineTo(to[0],to[1])); // Move 100 units to the right

            // Create a PathTransition
            PathTransition pathTransition = DrawBTree.makePathTransition(path, obj);
            PathTransition pathTransition2 = DrawBTree.makePathTransition(path, tex);
            
            if(from[0]!= to[0]) {
            	parallelTransition.getChildren().add(pathTransition);
            	parallelTransition.getChildren().add(pathTransition2);
	            moved.put(value, to);
            }else {
            	
            	pathTransition =null; pathTransition2=null;
            }
            
            
        }
		// chick if any pathtransitions were made 
		// if not the statusProperty Listener will not be activated 
		if(parallelTransition.getChildren().size()==0)
		{
			int[] tochange = DrawBTree.checkALLPosition();
        	if(tochange != null) 
        	{DrawBTree.adjustPositions(tochange[0], tochange[1]); }
        	else {
        	DrawBTree.adjustKeysInNodePositions();
        	}
		}
		parallelTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	drawingPaneAnimation.set(true);
            }
            if (newValue == Animation.Status.STOPPED) {
            	drawingPaneAnimation.set(false);
            	int[] tochange = DrawBTree.checkALLPosition();
            	if(tochange != null) 
            	{DrawBTree.adjustPositions(tochange[0], tochange[1]); }
            	else {
            	DrawBTree.adjustKeysInNodePositions();
            	}
            }
        });
		parallelTransition.play();
	}
	
	public static void adjustKeysInNodePositions()
	{
		ParallelTransition parallelTransition = new ParallelTransition();
		List<Integer> searchedKeys= new ArrayList<>();
		// get a Rectangle from the drawingpane
		for (javafx.scene.Node recn: drawingpane.getChildren()) {
			if( !(recn instanceof Rectangle)) continue;
			//get the key from the Rectangle and get the node to check node.keys
			int key = sharedMethodes.extractNumber(recn.getId());
			BTreeNode node1 = BTreeNode.search(root, key);
			if(searchedKeys.contains(key)) continue;
			//check node.keys and adjust position if needed
			for(int i = 0; i< node1.keys.size()-1;i++)
			{
				Rectangle rec = (Rectangle) drawingpane.lookup("#rec"+node1.keys.get(i+1));
				Text tex = (Text) drawingpane.lookup("#text"+node1.keys.get(i+1));
				int k = node1.keys.get(i);
				key=node1.keys.get(i+1);
				if(k == key) continue;
				Rectangle rec2 = (Rectangle) drawingpane.lookup("#rec"+k);
				Text tex2 = (Text) drawingpane.lookup("#text"+k);
				
				double rec2PosX = rec2.getBoundsInParent().getCenterX();
				double xdis = Math.abs( rec.getBoundsInParent().getCenterX() - rec2PosX);
				
				if(Math.abs(xdis-31.0)<=2) continue;
				Path path = new Path();
				PathTransition pathTransition =null;
				PathTransition pathTransitionText =null;
				double[] from = {rec2.getBoundsInParent().getCenterX(), rec2.getBoundsInParent().getCenterY()};
				double[] to = null;
				int keytomove = 0; double[] pos = null;
				if(k<key)
				{
		            path.getElements().add(new MoveTo(from[0],from[1])); // Start at current position
		            to = new double[]{rec.getBoundsInParent().getCenterX() , rec.getBoundsInParent().getCenterY()};
		            if(moved.containsKey(key)) to = Arrays.copyOf(moved.get(key), moved.get(key).length);
		            to[0]-=32;
		            path.getElements().add(new LineTo(to[0],to[1]));
		            keytomove = k;
		            pos = to;
		            pathTransition = DrawBTree.makePathTransition(path, rec2);
		            pathTransitionText = DrawBTree.makePathTransition(path, tex2);
				}else
				{
					path.getElements().add(new MoveTo(from[0], from[1])); // Start at current position
					to = new double[]{rec.getBoundsInParent().getCenterX() , rec.getBoundsInParent().getCenterY()};
					if(moved.containsKey(key)) to = Arrays.copyOf(moved.get(key), moved.get(key).length);
					to[0]+=32;
					keytomove = key;
		            pos = to;
					path.getElements().add(new LineTo(to[0],to[1])); 
					pathTransition = DrawBTree.makePathTransition(path, rec);
					pathTransitionText = DrawBTree.makePathTransition(path, tex);
				}
				
				if(pathTransition ==null) continue;
				if(to[0] != from[0])
				{
					parallelTransition.getChildren().add(pathTransition);
					parallelTransition.getChildren().add(pathTransitionText);
					moved.put(keytomove,pos);
				}else {	
	            	pathTransition =null; pathTransitionText=null;
	            }
	            // add to searchedKeys to mark that those keys were checked
	            searchedKeys.add(node1.keys.get(i));
	            searchedKeys.add(node1.keys.get(i+1));
			}
        }
		parallelTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.STOPPED) {
				drawingPaneAnimation.set(false);
				int[] tochange = DrawBTree.checkALLPosition();
				if(tochange != null) 
	            {adjustPositions(tochange[0], tochange[1]); }
	            else {
	            	adjustLines();
	            }
            }
            if (newValue == Animation.Status.RUNNING) {
            	drawingPaneAnimation.set(true);
        }});
		if(!parallelTransition.getChildren().isEmpty())
		parallelTransition.play();
		if(parallelTransition.getChildren().size()==0)adjustLines();
	}
	
	private static void adjustLines() {
		List<Line> toremoveLines = new ArrayList<>();
		//get all lines and remove them 
		for (javafx.scene.Node line: drawingpane.getChildren() ) 
			if( line instanceof Line) toremoveLines.add((Line) line);
		for(Line l : toremoveLines) drawingpane.getChildren().remove(l);
		List<Integer> searchedKeys = new ArrayList<>();
		List<Line> Lines = new ArrayList<>();
		for (javafx.scene.Node rec: drawingpane.getChildren() ) { 
			if( !(rec instanceof Rectangle)) continue;
			int key = sharedMethodes.extractNumber(rec.getId());
			
			if(searchedKeys.contains(key)) continue;
			
	        BTreeNode node = BTreeNode.search(root, key);
	        if(node.keys.get(0) != key)continue;
	        searchedKeys.addAll(node.keys);
	        if(node.children.size() <=0) continue;
	        int childIdx = 0;
	        double[] parentPos = {rec.getBoundsInParent().getCenterX(), rec.getBoundsInParent().getCenterY() };
    		if(moved.containsKey(key))parentPos = moved.get(key);
	        
	        for(BTreeNode child : node.children)
	        	{
	        		int midKey = child.keys.get((child.keys.size()-1)/2);
	        		Rectangle childRec = (Rectangle) drawingpane.lookup("#rec"+midKey);
	        		double[] childPos = {childRec.getBoundsInParent().getCenterX(), childRec.getBoundsInParent().getCenterY() };
	        		double startX = 0;
	        		if(childIdx!=0)startX = parentPos[0]+(32 * (childIdx-1));
	        		else startX = parentPos[0];
	        		if(key < midKey) startX +=16;
	        		else startX-=16;
	        		
	        		Line line = new Line(startX, parentPos[1]+15
	        						, childPos[0], childPos[1]-15);
	        		if(childIdx>0)line.setId("l"+node.keys.get(childIdx-1));
	        		else line.setId("l"+node.keys.get(0));
	        		Lines.add(line);
	        		Random random = new Random();
	        	    double red = random.nextDouble() * 0.4;
	        	    double green = random.nextDouble() * 0.7;
	        	    double blue = random.nextDouble() * 0.7;
	        		line.setStrokeWidth(1.5);
	        		line.setStroke(new Color(red, green, blue, 1.0));
	        		childIdx++;
	        	}
	    }
		
		for(Line l : Lines) {
			drawingpane.getChildren().add(l);
			l.toBack();
		}
		List<javafx.scene.Node> CrossingLines = sharedMethodes.getCrossingLines(drawingpane);
    	if(CrossingLines != null) DrawBTree.AdjustPositionsLinesCrossing(CrossingLines);
    	else if(drawingPaneAnimation.getValue()== false){
    		positionAdjusted.clear();
    		int[] tochange = DrawBTree.checkALLPosition();
			if(tochange != null) 
            {	
				adjustPositions(tochange[0], tochange[1]);
			} else { 
				if(!recenterAfterLines) 
				{
					positionAdjusted.clear();
					recenterAfterLines =true;
					recenterTree(DrawBTree.root);
				}
				else recenterAfterLines =false;
			}
		}
	}

	public static int[] checkALLPosition() {
		for(javafx.scene.Node rec1 : drawingpane.getChildren())
		{
			if(rec1 instanceof Rectangle)
			{
				int val1 = sharedMethodes.extractNumber(rec1.getId());
				List<Integer> sameLevel = BTree.getKeysAtSameLevel(root, val1);
				for (javafx.scene.Node rec2 : drawingpane.getChildren()) {
					if(rec2 instanceof Rectangle)
					{	int val2 = sharedMethodes.extractNumber(rec2.getId());
						if(!sameLevel.contains(val2)) continue;
						if(val1 == val2 || BTreeNode.keysInSameNode(DrawBTree.root,val1, val2))
						continue;
						double[] position1 = {rec1.getBoundsInParent().getCenterX(), rec1.getBoundsInParent().getCenterY()}; 
						double[] position2 = {rec2.getBoundsInParent().getCenterX(), rec2.getBoundsInParent().getCenterY()}; 
						if(moved.containsKey(val1)) position1 = Arrays.copyOf(moved.get(val1), moved.get(val1).length);
						if(moved.containsKey(val2)) position2 = Arrays.copyOf(moved.get(val2), moved.get(val2).length);
						double deltaX = position1[0] - position2[0];
			            double deltaY = position1[1] - position2[1];
			            // Ensure that the keys are located at the same level by comparing their y-coordinates.
			            // Check if the necessary adjustments have already been made for this pair of keys.
			            if (Math.abs(deltaX)<= minSpaceBetweenNodes && Math.abs(deltaY)<=5&& 
			            		!positionAdjusted.contains(new Pair<>(val1, val2))) {
			            	positionAdjusted.add(new Pair<>(val1, val2));
			            	return new int[] {val1 ,val2};
			            }
		            }
				}
			}
			
		}
		return null;
	}
	
	private static void AdjustPositionsLinesCrossing(List<javafx.scene.Node> crossingLines) {
		Line line1 = (Line) crossingLines.get(0);
		Line line2 = (Line) crossingLines.get(1);
		int value1 = sharedMethodes.extractNumber(line1.getId());
		int value2 = sharedMethodes.extractNumber(line2.getId());
		positionAdjusted.clear();
		if(BTree.areKeysInSameLevel(root, value1, value2))
			DrawBTree.adjustPositions(value1, value2);	
	}
	
	public static void changeRectanglesColor(List<Integer> keys, Color color) {
	    for (Integer key : keys) {
	        Rectangle rectangle = (Rectangle) drawingpane.lookup("#rec" + key);
	        if (rectangle != null) {
	            rectangle.setFill(color);
	        }
	    }
	}
	
	private static Text visualiseSplittingRoot() {
		double y = infopane.getHeight()/2;
		Text text = null;
		text = new Text("Splitting root due to maximum keys reached for efficient B-tree structure.");
		infopane.getChildren().add(text);
		double paneWidth = infopane.getWidth();
		double textWidth = text.getBoundsInLocal().getWidth();
		double x = (paneWidth - textWidth) / 2;
		text.setX(x);
		text.setY(y);
		text.setVisible(false); 
		text.setId("infotext");
		return text;
		
	}
	
	private static Text visualiseSplittingChild() {
		double y = infopane.getHeight()/2;
		Text text = null;
		text = new Text("Splitting Child due to maximum keys reached.\nMoving middle key to parent node.\nRedistributing remaining keys among the new child nodes.");
		infopane.getChildren().add(text);
		double paneWidth = infopane.getWidth();
		double textWidth = text.getBoundsInLocal().getWidth();
		double x = (paneWidth - textWidth) / 2;
		text.setX(x);
		text.setY(y);
		text.setVisible(false); 
		text.setId("infotext");
		return text;
		
	}
	
	private static Text visualiseStep(int parent, int toadd) {
		double y = infopane.getHeight()/2;
		Text text = null;
		if(parent> toadd) {
			text = new Text( parent+" > "+toadd+ " check left child");
		}
		else {
			text = new Text( parent+" < "+toadd+ " check right child");
		}
		infopane.getChildren().add(text);
		double paneWidth = infopane.getWidth();
		double textWidth = text.getBoundsInLocal().getWidth();
		double x = (paneWidth - textWidth) / 2;
		text.setX(x);
		text.setY(y);
		text.setVisible(false);
		text.setId("infotext");
		return text;
		
	}
	
	private static Text visualiseAddStep(int toadd) {
		double y = infopane.getHeight()/2;
		Text text = null;
		text = new Text( "Inserting new key ("+toadd+") into the node.");
		infopane.getChildren().add(text);
		double paneWidth = infopane.getWidth();
		double textWidth = text.getBoundsInLocal().getWidth();
		double x = (paneWidth - textWidth) / 2;
		text.setX(x);
		text.setY(y);
		text.setVisible(false);
		text.setId("infotext");
		return text;
		
	}
	
	
	public static void CalculateOffSetWithBoundsInParent(int offset)
	{	//calculate the offset in drawingpane. Boundsinparent makes a different
		Rectangle rectangle = new Rectangle(0, -15 , 30, 30);
		drawingpane.getChildren().add(rectangle);
		double x1 = rectangle.getBoundsInParent().getCenterX();
		Rectangle rectangle2 = new Rectangle(x1+offset, -15 , 30, 30);
		drawingpane.getChildren().add(rectangle2);
		double x2 = rectangle2.getBoundsInParent().getCenterX();
		offsetX = offset- (offset*(((x2-x1)/offset)- 1)); 
		drawingpane.getChildren().remove(rectangle);
		drawingpane.getChildren().remove(rectangle2);
	}

	public static void animationRemoveKey(Integer key) {
	ParallelTransition plt = new ParallelTransition();
		for(javafx.scene.Node node: drawingpane.getChildren())
		{	
			int val = sharedMethodes.extractNumber(node.getId());
			if(val==key && (node instanceof Rectangle || node instanceof Text ))
			{
				double[] from = {node.getBoundsInParent().getCenterX(), node.getBoundsInParent().getCenterY()};
				if(moved.containsKey(key)) from = Arrays.copyOf(moved.get(key), moved.get(key).length);
				double[] to = {from[0], from[1]-30};
				Path path = new Path();
				path.getElements().add(new MoveTo(from[0],from[1]));
				path.getElements().add(new LineTo(to[0],to[1]));
				PathTransition pt = DrawBTree.makePathTransition(path, node);
				FadeTransition ft =sharedMethodes.makeFadeTransition(node);
				plt.getChildren().add(pt);
				plt.getChildren().add(ft);
				pt.statusProperty().addListener((observable, oldValue, newValue) -> {
		            if (newValue == Animation.Status.RUNNING) {
		                if(node instanceof Rectangle) ((Rectangle) node).setFill(Color.RED);
		            }
		            if (newValue == Animation.Status.STOPPED) drawingpane.getChildren().remove(node);
				});
			}
			
		}
	DrawBTree.deleteAnimations.add(plt);
	moved.remove(key);
	}
	
	private static Text visualiseSearchSuccOrPred() {
		double y = infopane.getHeight()/2;
		Text text = null;
		text = new Text( "Searching for the successor or predecessor key in the node's children. ");
		infopane.getChildren().add(text);
		double paneWidth = infopane.getWidth();
		double textWidth = text.getBoundsInLocal().getWidth();
		double x = (paneWidth - textWidth) / 2;
		text.setX(x);
		text.setY(y);
		text.setVisible(false);
		text.setId("infotext");
		return text;
	}
	
	public static void switchWithSuccessorOrPredecessor(Integer successor, int key) {
		ParallelTransition plt = new ParallelTransition();
		Rectangle keyRec = (Rectangle) drawingpane.lookup("#rec"+key);
		Rectangle succRec = (Rectangle) drawingpane.lookup("#rec"+successor);
		Text succText = (Text) drawingpane.lookup("#text"+successor);
		Text infoText = visualiseText("switching "+key+" with successor and removing "+key);
		double[] from = {succRec.getBoundsInParent().getCenterX(), succRec.getBoundsInParent().getCenterY()};
		
		if(moved.containsKey(successor)) from = moved.get(successor);
		
		double[] to = {keyRec.getBoundsInParent().getCenterX(), keyRec.getBoundsInParent().getCenterY()};
		if(moved.containsKey(key)) to = Arrays.copyOf(moved.get(key), moved.get(key).length);
		
		Path path = new Path();
		path.getElements().add(new MoveTo(from[0],from[1]));
		path.getElements().add(new LineTo(to[0],to[1]));
		PathTransition pt = DrawBTree.makePathTransition(path, succRec);
		PathTransition pt2 = DrawBTree.makePathTransition(path, succText);
		plt.getChildren().add(pt);
		plt.getChildren().add(pt2);
		pt.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	succRec.setFill(Color.LIGHTGREEN);
            	infoText.setVisible(true);
                
            }});
		pt.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.STOPPED) {
            	infopane.getChildren().remove(infoText);
            	succRec.setFill(Color.WHITE);}});
		moved.put(successor, to);
		if(key == BTree.toRemove)animationRemoveKey(key);
		moveSuccessorAnimations.add(plt);
	}
	public static void switchWithNodes(Integer successor, int key) {
		ParallelTransition plt = new ParallelTransition();
		Rectangle keyRec = (Rectangle) drawingpane.lookup("#rec"+key);
		Text keyText = (Text) drawingpane.lookup("#text"+key);
		Rectangle succRec = (Rectangle) drawingpane.lookup("#rec"+successor);
		Text succText = (Text) drawingpane.lookup("#text"+successor);
		double[] from = {succRec.getBoundsInParent().getCenterX(), succRec.getBoundsInParent().getCenterY()};
		if(moved.containsKey(successor)) from = Arrays.copyOf(moved.get(successor), moved.get(successor).length);
		double[] to = {keyRec.getBoundsInParent().getCenterX(), keyRec.getBoundsInParent().getCenterY()};
		if(moved.containsKey(key)) to = Arrays.copyOf(moved.get(key), moved.get(key).length);
		Path path = new Path();
		path.getElements().add(new MoveTo(from[0],from[1]));
		path.getElements().add(new LineTo(to[0],to[1]));
		Path path2 = new Path();
		path2.getElements().add(new MoveTo(to[0],to[1]));
		path2.getElements().add(new LineTo(from[0],from[1]));
		PathTransition pt = DrawBTree.makePathTransition(path, succRec);
		PathTransition pt2 = DrawBTree.makePathTransition(path, succText);
		PathTransition kpt = DrawBTree.makePathTransition(path2, keyRec);
		PathTransition kpt2 = DrawBTree.makePathTransition(path2, keyText);
		plt.getChildren().add(pt);
		plt.getChildren().add(pt2);
		plt.getChildren().add(kpt);
		plt.getChildren().add(kpt2);
		pt.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	succRec.setFill(Color.LIGHTGREEN);
            	keyRec.setFill(Color.LIGHTGREEN);
                
            }
            if (newValue == Animation.Status.STOPPED) {
            		succRec.setFill(Color.WHITE);
					keyRec.setFill(Color.WHITE);}
        });
		moveSuccessorAnimations.add(plt);
		if(key == BTree.toRemove||successor == BTree.toRemove) 
		{animationRemoveKey(BTree.toRemove);
		}
		
	}

	public static void AnimationMergeChildren(BTreeNode node, int index, int key) {
		if(index == (node.children.size() -1)) index--;
		List<Integer> leftChild = node.children.get(index).keys;
		List<Integer>  rightChild = node.children.get(index + 1).keys;
		List<Integer>  allKeys = new ArrayList<>();
		allKeys.addAll(rightChild); allKeys.addAll(leftChild); allKeys.add(key);
		double[] xDis = sharedMethodes.calculateDistance(drawingpane, leftChild.get((leftChild.size()-1)/2),rightChild.get((rightChild.size()-1)/2));
		double[] yDis = sharedMethodes.calculateDistance(drawingpane, leftChild.get((leftChild.size()-1)/2),key);
		if(yDis == null) {yDis = new double[]{0,50};}
		double midx = sharedMethodes.calculateMidX(drawingpane, leftChild.get((leftChild.size()-1)/2),rightChild.get((rightChild.size()-1)/2));
		ParallelTransition plChildrenKeys = new ParallelTransition();
		ParallelTransition plMovingKey = new ParallelTransition();
		ParallelTransition plAfterRemovingKeys = new ParallelTransition();
		List<Integer>  searched = new ArrayList<>();
		//moving the Key down to merge with the children
		for(Integer k :allKeys)
		{
			if(searched.contains(k)) continue;
			searched.add(k);
			Rectangle keyRec = (Rectangle) drawingpane.lookup("#rec"+k);
			Text keyText = (Text) drawingpane.lookup("#text"+k);
			double[] from = {keyRec.getBoundsInParent().getCenterX(), keyRec.getBoundsInParent().getCenterY()};
			if(moved.containsKey(k)) from = Arrays.copyOf(moved.get(k), moved.get(k).length);
			double[] to = {from[0], from[1]};
			if(k ==key)
			{
				to[0]= midx; to[1] +=yDis[1];
		
			}
			else if(leftChild.contains(k))
			{
				to[0] += ((xDis[0]/2) -48); 
				
			}else to[0] -=((xDis[0]/2) -48);
			
			Path path = new Path();
			path.getElements().add(new MoveTo(from[0],from[1]));
			path.getElements().add(new LineTo(to[0],to[1]));
			PathTransition pt = DrawBTree.makePathTransition(path, keyRec);
			PathTransition pt2 = DrawBTree.makePathTransition(path, keyText);
			if(k!= key)
			{
				Path path2 = new Path();
				path2.getElements().add(new MoveTo(to[0],to[1]));
				if(k<key)to[0] +=16; else to[0]-=16;
				path2.getElements().add(new LineTo(to[0],to[1]));
				PathTransition ptAfter = DrawBTree.makePathTransition(path2, keyRec);
				PathTransition pt2After = DrawBTree.makePathTransition(path2, keyText);
				plAfterRemovingKeys.getChildren().add(ptAfter);
				plAfterRemovingKeys.getChildren().add(pt2After);
			}
			
			moved.put(k, to);
			if(k ==key)
			{
				plMovingKey.getChildren().add(pt);
				plMovingKey.getChildren().add(pt2);
			}else {
				plChildrenKeys.getChildren().add(pt);
				plChildrenKeys.getChildren().add(pt2);
			}
			
			
		}
		Text infoText = visualiseText("Merging initiated due to the node having the minimum key count.");
		plMovingKey.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	changeRectanglesColor(allKeys,Color.RED);
                infoText.setVisible(true);
            }});
		plMovingKey.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.STOPPED) {changeRectanglesColor(allKeys,Color.LIGHTGREEN);}});
		
		plAfterRemovingKeys.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.STOPPED) {
            	changeRectanglesColor(allKeys,Color.WHITE);
            	infopane.getChildren().remove(infoText);
            }});
		
		if(DrawBTree.successorAnimations.isEmpty()) {
			mergeAnimations.add(plMovingKey);		
			mergeAnimations.add(plChildrenKeys);
			mergeAnimations.add(plAfterRemovingKeys);
		}
		else {
			SuccessorMergeAnimations.add(plMovingKey);		
			SuccessorMergeAnimations.add(plChildrenKeys);
			SuccessorMergeAnimations.add(plAfterRemovingKeys);
		} 
	}

	public static Text visualiseText(String tex)
	{
		double y = infopane.getHeight()/2;
		Text text = null;
		text = new Text(tex);
		infopane.getChildren().add(text);
		double paneWidth = infopane.getWidth();
		text.setWrappingWidth(paneWidth/1.5);
		double textWidth = text.getBoundsInLocal().getWidth();
		double x = (paneWidth - textWidth) / 2;
		text.setX(x);
		text.setY(y);
		text.setVisible(false);
		text.setId("infotext");
		return text;
		
	}

	
	public static void AnimationMoveChild(BTreeNode parent, BTreeNode child, int borrowedKey) {
		int rightOrleft = 0;
		if(borrowedKey < child.keys.get(0)) rightOrleft = -1;else rightOrleft = 1;
		ParallelTransition plt = new ParallelTransition();
		for(Integer k: child.keys)
		{
			Rectangle Rec = (Rectangle) drawingpane.lookup("#rec"+k);
			Text Text = (Text) drawingpane.lookup("#text"+k);
			double[] from = {Rec.getBoundsInParent().getCenterX(), Rec.getBoundsInParent().getCenterY()};
			if(moved.containsKey(k)) from = Arrays.copyOf(moved.get(k), moved.get(k).length);
			double[] to = {from[0] + (32*rightOrleft), from[1]};
			Path path = new Path();
			path.getElements().add(new MoveTo(from[0],from[1]));
			path.getElements().add(new LineTo(to[0],to[1]));
			PathTransition pt = DrawBTree.makePathTransition(path, Rec);
			PathTransition pt2 = DrawBTree.makePathTransition(path, Text);
			plt.getChildren().add(pt);
			plt.getChildren().add(pt2);
			Text info = visualiseText("Moving child after borrowing the Key");
			pt.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.RUNNING) {
	            	Rec.setFill(Color.LIGHTGREEN);
	            	info.setVisible(true);
	            }
	            if (newValue == Animation.Status.STOPPED) {
	            	Rec.setFill(Color.WHITE);
	            	infopane.getChildren().remove(info); }
			});
			moved.put(k, to);
	
		}
	}

	public static void AnimationBorrowKey(BTreeNode node, int key, BTreeNode parent, int childIdx) {
		ParallelTransition plt = new ParallelTransition();
		Rectangle keyRec = (Rectangle) drawingpane.lookup("#rec"+key);
		Text keyText = (Text) drawingpane.lookup("#text"+key);
		double[] fromkey = {keyRec.getBoundsInParent().getCenterX(), keyRec.getBoundsInParent().getCenterY()};
		if(moved.containsKey(key)) fromkey = Arrays.copyOf(moved.get(key), moved.get(key).length);
		int val = node.keys.get(node.keys.size()-1);
		if(key< node.keys.get(0))	
			val = node.keys.get(0);
		int keyparent = parent.keys.get((childIdx < parent.keys.size())? childIdx: parent.keys.size()-1);
		if(parent.keys.contains(BTree.toRemove)) keyparent = BTree.toRemove;
		Rectangle rec = (Rectangle) drawingpane.lookup("#rec"+keyparent);
		Text tex = (Text) drawingpane.lookup("#text"+keyparent);
		
		Rectangle rec2 = (Rectangle) drawingpane.lookup("#rec"+val);
		double[] toremove = new double[]{rec.getBoundsInParent().getCenterX(), rec.getBoundsInParent().getCenterY()};
		if(moved.containsKey(keyparent)) toremove = Arrays.copyOf(moved.get(keyparent), moved.get(keyparent).length);
		double[] toval = new double[]{rec2.getBoundsInParent().getCenterX(), rec2.getBoundsInParent().getCenterY()};
		if(moved.containsKey(val)) toval= Arrays.copyOf(moved.get(val), moved.get(val).length);
		if(key<val) toval[0] -=32;
			else toval[0] +=32;
		Path path = new Path();
		path.getElements().add(new MoveTo(fromkey[0],fromkey[1]));
		path.getElements().add(new LineTo(toremove[0],toremove[1]));
		Path path2 = new Path();
		path2.getElements().add(new MoveTo(toremove[0],toremove[1]));
		path2.getElements().add(new LineTo(toval[0],toval[1]));
		PathTransition pt1 = DrawBTree.makePathTransition(path2, rec);
		PathTransition pt2 = DrawBTree.makePathTransition(path2, tex);
		PathTransition pt3 = DrawBTree.makePathTransition(path, keyRec);
		PathTransition pt4 = DrawBTree.makePathTransition(path, keyText);
		moved.put(key, toremove);
		moved.put(keyparent, toval);
		plt.getChildren().add(pt1);
		plt.getChildren().add(pt2);
		plt.getChildren().add(pt3);
		plt.getChildren().add(pt4);
		Text info = visualiseText("When removing a key from a non-leaf node and encountering a successor with the minimum number of keys, it's necessary to borrow a key from the right node to maintain balance.");
		pt1.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	keyRec.setFill(Color.GREEN);
            	changeRectanglesColor(node.keys,Color.LIGHTGREEN);
            	info.setVisible(true);
                
            }
            if (newValue == Animation.Status.STOPPED) {
            	keyRec.setFill(Color.WHITE);
            	changeRectanglesColor(node.keys,Color.WHITE);
            	infopane.getChildren().remove(info);}
		});
		moved.put(key, toremove);
		if(successorAnimations.isEmpty())
			mergeAnimations.add(plt);
		else SuccessorMergeAnimations.add(plt);
		if(keyparent== BTree.toRemove)animationRemoveKey(BTree.toRemove);
	
	}
	
	private static PathTransition makePathTransition(Path path, javafx.scene.Node node) {
		PathTransition pathTransition = new PathTransition(); 
		DoubleProperty durationProperty = new SimpleDoubleProperty(); 
		durationProperty.bind(animationspeed.valueProperty().multiply(100));
		pathTransition.setDuration(Duration.millis(durationProperty.get()));
		pathTransition.setPath(path);
		pathTransition.setNode(node);
		pathTransition.setCycleCount(0);
		pathTransition.setAutoReverse(false);
		return pathTransition;
	}
	
	public static void recenterTree(BTreeNode root)
	{	//move the node to center x of its children
		ParallelTransition parallelTransition = new ParallelTransition();
		recenterTree(root,parallelTransition);
		parallelTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	drawingPaneAnimation.set(true);
            }
            if (newValue == Animation.Status.STOPPED) {
            	drawingPaneAnimation.set(false);
    			int[] toAdjust = DrawBTree.checkALLPosition();
    	        if(toAdjust!= null) 
    	        	DrawBTree.adjustPositions(toAdjust[0], toAdjust[1]);
    	         else adjustKeysInNodePositions();
            }
		});
		// if parallelTransition has no children the status Listener will not be activated
		if(parallelTransition.getChildren().size()== 0)
		{
            	drawingPaneAnimation.set(false);
    			int[] toAdjust = DrawBTree.checkALLPosition();
    	        if(toAdjust!= null) 
    	        	DrawBTree.adjustPositions(toAdjust[0], toAdjust[1]);
    	         else adjustKeysInNodePositions();
        }
		parallelTransition.play();	
		
		
	}
	public static void recenterTree(BTreeNode root,ParallelTransition parallelTransition)
	{	//calls recenter Node on each node and add the returned transitions
		if(root!= null) {
			ParallelTransition ChildTransition = recenterNode(root);
			if(ChildTransition.getChildren().size()!=0) parallelTransition.getChildren().add(ChildTransition);
		}
		for(BTreeNode child: root.children)
		{
			recenterTree(child,parallelTransition);
		}
		
	}
	public static ParallelTransition recenterNode(BTreeNode parent)
	{	//create a transition that moves the node to the mid x of its children
    	ParallelTransition parallelTransition = new ParallelTransition();
		if(parent.children.size() != 0)
		{
			//children =new ArrayList<>(child.children);
			double midX = sharedMethodes.calculateMidX(drawingpane, parent.children.get(0).keys.get(0),
					parent.children.get(parent.children.size()-1).keys.get(parent.children.get(parent.children.size()-1).keys.size()-1));

			for(int val: parent.keys )
			{
				Text tex = (Text) drawingpane.lookup("#text"+val);
				Rectangle pc =  (Rectangle) drawingpane.lookup("#rec"+val);
				double[] oldPosition = {pc.getBoundsInParent().getCenterX(), pc.getBoundsInParent().getCenterY()};
				if(moved.containsKey(val)&& moved.get(val)[0]>0)
				{
					oldPosition = Arrays.copyOf(moved.get(val), moved.get(val).length);
				}
		        double[] newposition = {0, oldPosition[1]};
				newposition[0]= (int)(midX+(32*(parent.keys.indexOf(val)-((parent.keys.size()>1)?1:0))));
				
				Path path = new Path();
		        path.getElements().add(new MoveTo(oldPosition[0], oldPosition[1]));
		        path.getElements().add(new LineTo(newposition[0], newposition[1]));
		        
		        PathTransition pathTransition = DrawBTree.makePathTransition(path, pc);
		        PathTransition texpathTransition = DrawBTree.makePathTransition(path, tex);
		        if(oldPosition[0]!= newposition[0]) {
		        	parallelTransition.getChildren().add(pathTransition);
		        	parallelTransition.getChildren().add(texpathTransition);
		        	moved.put(val, newposition);
		        }
				
		        
			}

		}
		
		return parallelTransition;
		
	}

	
}
