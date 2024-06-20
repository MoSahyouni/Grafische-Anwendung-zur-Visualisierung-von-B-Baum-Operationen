package application;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javafx.animation.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class DrawBinSearchTree {
	BSNode node;
	String id;
	public double[] position = {0, 0};
	public static BooleanProperty drawingPaneAnimation=null;
	public static ArrayList<DrawBinSearchTree> alleNodes = new ArrayList<DrawBinSearchTree>();
	public static Queue<Transition> Animations = new LinkedList<>();
	public static Queue<ParallelTransition> deleteAnimations = new LinkedList<>();
	public static Queue<ParallelTransition> movingTransitions = new LinkedList<>();
	public static Queue<Transition> addAnimations = new LinkedList<>();
	public static Queue<PathTransition> successorAnimations = new LinkedList<>();
	public static Pane drawingpane;
	public static Pane infopane;
	public static Slider animationspeed;
	public static double offsetX =30;
	
	
	public static void drawSubtree(Pane pane, BSNode node, double x, double y, double offsetX) {
		if (node != null) {
            // Draw the node
        	DrawBinSearchTree nodeObj = new DrawBinSearchTree();
        	nodeObj.node = node;
        	nodeObj.id = "node"+node.value;
        	nodeObj.position[0] = x;
        	nodeObj.position[1] = y;
        	boolean PositionAdjustNeeded = false;
        	// check if the Node is already drawn. 
        	// check if an object for the node already exists 
        	if(findNodeById(nodeObj.id) == null)
            {	//check if the position for the node is taken from another node
            	if(DrawBinSearchTree.checkPosition(nodeObj.position) || null !=sharedMethodes.getCrossingLines(drawingpane) )
            	{
            		int nodeInPos = DrawBinSearchTree.positionTakenFrom(nodeObj.position);
            		
            		if(nodeInPos != nodeObj.node.value&&nodeInPos!= Integer.MIN_VALUE)
            			{PositionAdjustNeeded = true;
            		}
            	}
            }
        	// make an object for the node 
        	else {
        		DrawBinSearchTree drawNode = findNodeById(nodeObj.id);
        		x = drawNode.position[0] ;
            	y = drawNode.position[1] ;
            	nodeObj.position[0] = x;
            	nodeObj.position[1] = y;
        	}
        	
        	Circle circle = new Circle(x, y, 15);
            circle.setId(nodeObj.id);
            circle.setFill(Color.WHITE);
            circle.setStroke(Color.BLACK);
            Text tex = new Text(x - 5, y + 5, String.valueOf(node.value));
            tex.setId("text"+node.value);
            
            // find parent of the current Node 
            BSNode parent =null;
            if(!DrawBinSearchTree.alleNodes.isEmpty()) 
            	parent = nodeObj.node.findParent(alleNodes.get(0).node);

            // Draw edges
            if (parent!= null && findNodeById(nodeObj.id) == null) {
            	DrawBinSearchTree parentNode = DrawBinSearchTree.findNodeById("node"+parent.value);
            	if(parent.value > nodeObj.node.value) {
            		Line lineLeft = new Line(parentNode.position[0]-4, parentNode.position[1]+ 15, x +1, y -15);
            		lineLeft.toBack();
            		lineLeft.setId("lineL"+parent.value);
	                lineLeft.setStroke(Color.GREEN);
	                pane.getChildren().add(lineLeft);}
            	else
            	{
            		Line lineRight = new Line(parentNode.position[0]+4, parentNode.position[1]+15, x -1, y - 15);
            		lineRight.toBack();
            		lineRight.setId("lineR"+parent.value);
	                lineRight.setStroke(Color.RED);
	                pane.getChildren().add(lineRight);
            	}
            }
            
            if(findNodeById(nodeObj.id) == null)
            {
            	alleNodes.add(nodeObj);
            	pane.getChildren().add(circle);
	            pane.getChildren().add(tex);
            }
            
            if(PositionAdjustNeeded)
            {	
            	drawingPaneAnimation.set(true);
            	
        		int nodeInPosition = DrawBinSearchTree.positionTakenFrom(nodeObj.position);
        		DrawBinSearchTree nodeInpos = DrawBinSearchTree.findNodeById("node"+nodeInPosition);
        		DrawBinSearchTree.adjustPositions(node, nodeInpos.node);
        		double[] newpos = DrawBinSearchTree.getParentPosition(nodeObj);
        		BSNode parentNode = nodeObj.node.findParent(alleNodes.get(0).node);
        		if(parentNode.value> nodeObj.node.value)
        		{
        			nodeObj.position[0] = newpos[0] - 30;
        			nodeObj.position[1] = newpos[1] + 40;
        		}else {
        			nodeObj.position[0] = newpos[0] + 30;
        			nodeObj.position[1] = newpos[1] + 40;
        		}		
            }
            List<javafx.scene.Node> CrossingLines = sharedMethodes.getCrossingLines(drawingpane); 
        	if(CrossingLines != null)
        	{
        		DrawBinSearchTree.AdjustPositionsLinesCrossing(CrossingLines);
        	}
            // Draw left subtree
        	if(node.leftChild!=null)drawSubtree(pane, node.leftChild, x - offsetX, y + 40, offsetX);
            // Draw right subtree
            if(node.rightChild!=null)drawSubtree(pane, node.rightChild, x + offsetX, y + 40, offsetX);
        }
    }
	private static void AdjustPositionsLinesCrossing(List<javafx.scene.Node> crossingLines) {
		Line line1 = (Line) crossingLines.get(0);
		Line line2 = (Line) crossingLines.get(1);
		int value1 = sharedMethodes.extractNumber(line1.getId());
		int value2 = sharedMethodes.extractNumber(line2.getId());
		DrawBinSearchTree node1 = DrawBinSearchTree.findNodeById("node"+value1);
		DrawBinSearchTree node2  =DrawBinSearchTree.findNodeById("node"+value2);
		if(value1 != value2)DrawBinSearchTree.adjustPositions(node1.node, node2.node);	
		else drawingPaneAnimation.set(false);
	}
	private static double[] getParentPosition(DrawBinSearchTree nodeobg) {
		BSNode parent = nodeobg.node.findParent(alleNodes.get(0).node);
		DrawBinSearchTree p = DrawBinSearchTree.findNodeById("node"+parent.value);
		if(p != null)
			return p.position;
		return null;
		
	}

	public static void adjustPositions(BSNode node, BSNode node2)
	{
		DrawBinSearchTree parent = DrawBinSearchTree.findNodeById("node"+BinSTree.findLowestCommonAncestor(alleNodes.get(0).node, node, node2).value);
		ParallelTransition parallelTransition = new ParallelTransition();
		
		for (javafx.scene.Node obj : drawingpane.getChildren()) {
			boolean skip = false;
			if(parent.node.leftChild != null && parent.node.rightChild == null)
				if(parent.node.leftChild.value == node2.value)
					break;
			if(parent.node.rightChild != null && parent.node.leftChild == null)
				if(parent.node.rightChild.value == node2.value)
					break;
			String objId = obj.getId();
			int value = sharedMethodes.extractNumber(objId);
		    if ( !parent.node.isChild(value)) {
		        continue;
		    }
		    int xdis = 40;
		    if(value< parent.node.value) xdis *= -1;
            // Create a path that moves horizontally
		    double[] newPos = new double[] {obj.getBoundsInParent().getCenterX() + xdis, obj.getBoundsInParent().getCenterY()};
            for (DrawBinSearchTree tomove : alleNodes) {
                if (tomove.id.equals("node"+value)) {
                	if(tomove.id.equals(parent.id)){
                	skip = true;
                	break;
                	}
                if(obj instanceof Circle) {
                	tomove.position[0] = newPos[0];
                	tomove.position[1] = newPos[1];
                	break;
                	}
                }
            }
            if(skip) continue;
		    Path path = new Path();
            path.getElements().add(new MoveTo(obj.getBoundsInParent().getCenterX(), obj.getBoundsInParent().getCenterY())); // Start at current position
            path.getElements().add(new LineTo(obj.getBoundsInParent().getCenterX() + xdis, obj.getBoundsInParent().getCenterY())); // Move 100 units to the right

            // Create a PathTransition
            PathTransition pathTransition = DrawBinSearchTree.makePathTransition(path, obj);
            parallelTransition.getChildren().add(pathTransition);
        }

		parallelTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	MainsceneController.runningTransition = null;
            	MainsceneController.runningTransition = parallelTransition;
            	drawingPaneAnimation.set(true);
            }
            
        });
		
		parallelTransition.setOnFinished(event -> {
			updateNodesPositions();
			DrawBinSearchTree[] tochange = DrawBinSearchTree.checkALLPosition();
			if(tochange != null) 
            {DrawBinSearchTree.adjustPositions(tochange[0].node, tochange[1].node); }
            else {
            	DrawBinSearchTree.adjustLines();
            	List<javafx.scene.Node> CrossingLines = sharedMethodes.getCrossingLines(drawingpane); 
            	if(CrossingLines != null)
            		DrawBinSearchTree.AdjustPositionsLinesCrossing(CrossingLines);
            	else drawingPaneAnimation.set(false);
            }
			//
        });	
		parallelTransition.play();
	}
	
	public static void shrinkTree(BSNode node)
	{
		if(node.leftChild ==null || node.rightChild==null) return;
		DrawBinSearchTree parent = DrawBinSearchTree.findNodeById("node"+node.value);
		
		ParallelTransition parallelTransition = new ParallelTransition();
		int newXDistance = 40;

		
		for (javafx.scene.Node obj : drawingpane.getChildren()) {
			String objId = obj.getId();
			int value = sharedMethodes.extractNumber(objId);
		    if ( !parent.node.isChild(value) ||parent.node.value == value) {
		        continue;
		    }
		    int xDisplacement = (value < parent.node.value) ? newXDistance : -newXDistance;
		    double[] newPos = new double[]{obj.getBoundsInParent().getCenterX() + xDisplacement, obj.getBoundsInParent().getCenterY()};

		    // Update the position of the node
            for (DrawBinSearchTree toMove : alleNodes) {
                if (toMove.id.equals("node" + value)) {
                	DrawBinSearchTree.changePositionAttribute(value, newPos);
                    break;
                }
            }

            // Create a path that moves the node horizontally
            Path path = new Path();
            path.getElements().add(new MoveTo(obj.getBoundsInParent().getCenterX(), obj.getBoundsInParent().getCenterY())); // Start at current position
            path.getElements().add(new LineTo(newPos[0], newPos[1])); // Move to the new position

            // Create a PathTransition to animate the movement
            PathTransition pathTransition = DrawBinSearchTree.makePathTransition(path, obj);
            parallelTransition.getChildren().add(pathTransition);
        }

		parallelTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	drawingPaneAnimation.set(true);
            	MainsceneController.runningTransition = parallelTransition;
            }
            
        });
		
		parallelTransition.setOnFinished(event -> {
			DrawBinSearchTree.updateNodesPositions();
			DrawBinSearchTree[] tochange = DrawBinSearchTree.checkALLPosition();
			if(tochange != null) 
            {DrawBinSearchTree.adjustPositions(tochange[0].node, tochange[1].node); }
            else {
            	DrawBinSearchTree.adjustLines();
            	List<javafx.scene.Node> CrossingLines = sharedMethodes.getCrossingLines(drawingpane); 
            	if(CrossingLines != null)
            		DrawBinSearchTree.AdjustPositionsLinesCrossing(CrossingLines);
            	else drawingPaneAnimation.set(false);
            }
			
        });	
		parallelTransition.play();
	}
	
	private static void adjustLines() {
		for (DrawBinSearchTree current : alleNodes) {
	        if (current.node.leftChild != null) {
	            // Lookup the circles and line for left child
	            Line ll = (Line) drawingpane.lookup("#lineL" + current.node.value);
	            DrawBinSearchTree child = DrawBinSearchTree.findNodeById("node"+current.node.leftChild.value);
	            // Adjust the line between circles
	            DrawBinSearchTree.adjustLineBetweenCircles(current.position, child.position, ll);
	        }
	        if (current.node.rightChild != null) {
	            // Lookup the circles and line for right child
	            Line lr = (Line) drawingpane.lookup("#lineR" + current.node.value);
	            // Adjust the line between circles
	            DrawBinSearchTree child = DrawBinSearchTree.findNodeById("node"+current.node.rightChild.value);
	            // Adjust the line between circles
	            DrawBinSearchTree.adjustLineBetweenCircles(current.position, child.position, lr);
	        }
	    }
		
	}
	
	private static void adjustLineBetweenCircles(double[] start, double[] end, Line line) {
        double startX = start[0];
        double startY = start[1] + 15;

        // Adjust the line end point
        double endX = end[0];
        double endY = end[1] - 15;
        if(startX >endX) {startX -= 4; endX +=4;}else {startX +=4; endX -=4;}
        Line newLine = new Line();
        newLine.setId(line.getId());
        newLine.setStroke(line.getStroke());
        drawingpane.getChildren().remove(line);
        // Set line start and end points
        newLine.setStartX(startX);
        newLine.setStartY(startY);
        newLine.setEndX(endX);
        newLine.setEndY(endY);
        drawingpane.getChildren().add(newLine);
    }

	private static boolean checkPosition(double[] position2) {
		for (javafx.scene.Node node : drawingpane.getChildren()) {
	        if (node instanceof Circle) {
	            Circle circle = (Circle) node;
	            double deltaX = position2[0] - circle.getBoundsInParent().getCenterX();
	            double deltaY = position2[1] - circle.getBoundsInParent().getCenterY();
	            if (Math.abs(deltaX)<=30 && Math.abs(deltaY)<=30) {
	            	return true;
	            }
	        }
	    }
		return false;
	}
	private static DrawBinSearchTree[] checkALLPosition() {
		for(DrawBinSearchTree node1 : alleNodes)
		{
			double[] position = node1.position;
			for (DrawBinSearchTree node : alleNodes) {
				if(node.node.value == node1.node.value)
					continue;
				double deltaX = position[0] - node.position[0];
	            double deltaY = position[1] - node.position[1];
	            //double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	            if (Math.abs(deltaX)<=30 && Math.abs(deltaY)<=30) {
	            	return new DrawBinSearchTree[] {node1,node};
		     
	            }
			}
		}
		return null;
	}
	
	
	
	private static int positionTakenFrom(double[] position2) {
		for (javafx.scene.Node node : drawingpane.getChildren()) {
	        if (node instanceof Circle) {
	            Circle circle = (Circle) node;
	            double deltaX = position2[0] - circle.getBoundsInParent().getCenterX();
	            double deltaY = position2[1] - circle.getBoundsInParent().getCenterY();
	            if (Math.abs(deltaX)<=30 && Math.abs(deltaY)<=30) {
	            	int value = sharedMethodes.extractNumber(node.getId());
	                return value;
	            }
	        }
	    }
		return Integer.MIN_VALUE;
	}
	public static void changeColor(int id, Color color)
	{
		Circle chickingnode =  (Circle) drawingpane.lookup("#node"+id);
		if (chickingnode != null) {
	        //chickingnode.setFill(color);
	        chickingnode.fillProperty().set(color);
	        chickingnode.setStroke(color);
	        chickingnode.setStrokeWidth(5);
	    } else {
	        System.out.println("Node with ID " + id + " not found.");
	    }
	}
	
	public static void animationNodeToNode(double[] position1, double[] position2,Pane pane, int parent, int child) throws InterruptedException
	{	
		Circle circle = new Circle(4, Color.BLUE);
		circle.setVisible(false);

        pane.getChildren().add(circle);
        
        Path path = new Path();
        path.getElements().add(new MoveTo(position1[0], position1[1]));
        path.getElements().add(new LineTo(position2[0], position2[1]));
        Text infoText = DrawBinSearchTree.visualiseStep(parent, child);
        PathTransition pathTransition = DrawBinSearchTree.makePathTransition(path, circle);
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
        Animations.add(pathTransition);
	}
	
	public static void animationAddNewNode( int parent, int child) throws InterruptedException
	{	
		DrawBinSearchTree parentNode = DrawBinSearchTree.findNodeById("node"+parent);
		double[] position1 = parentNode.position;
		double[] position2 = null;
		double offset = DrawBinSearchTree.offsetX;
		if (parent < child) {
		    position2 = new double[] {position1[0] + offset, position1[1] + 40};
		} else {
		    position2 = new double[] {position1[0] - offset, position1[1] + 40};
		}
		Circle circle = new Circle(position1[0] , position1[1] ,15, Color.WHITE);
        circle.setStroke(Color.BLACK);
        Text text = new Text(position1[0] - 5, position1[1] + 5, String.valueOf(child));
        circle.setVisible(false);
        text.setVisible(false);

        drawingpane.getChildren().addAll(circle,text);
        
        Path path = new Path();
        path.getElements().add(new MoveTo(position1[0], position1[1]));
        path.getElements().add(new LineTo(position2[0], position2[1]));
        Text infoText = DrawBinSearchTree.visualiseStep(parent, child);
        PathTransition pathTransition = DrawBinSearchTree.makePathTransition(path, circle);
        PathTransition pathTransition2 = DrawBinSearchTree.makePathTransition(path, text);
        pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
                circle.setVisible(true);
                text.setVisible(true);
                infoText.setVisible(true);
            }
            if (newValue == Animation.Status.STOPPED) {
            	drawingpane.getChildren().remove(circle);
                drawingpane.getChildren().remove(text);
                infopane.getChildren().remove(infoText);
            }
        });
        
        ParallelTransition parallelTransition = new ParallelTransition(pathTransition,pathTransition2);
        addAnimations.add(parallelTransition);
	}	 
	
	private static Text visualiseStep(int parent, int toadd) {
		double x = infopane.getWidth()/2;
		double y = infopane.getHeight()/2;
		Text text = null;
		if(parent> toadd) {
			text = new Text(x, y, parent+" > "+toadd+ " check left child");
		}
		else {
			text = new Text(x, y, parent+" < "+toadd+ " check right child");
		}
		text.setVisible(false);
		text.setId("infotext");
		//text.getStyleClass().add("text-with-border");
		infopane.getChildren().add(text);
		return text;
		
	}

	public static DrawBinSearchTree findNodeById(String targetId) {
        for (DrawBinSearchTree drawTree : alleNodes) {
            if (drawTree.id.equals(targetId)) {
                return drawTree;
            }
        }
        return null; // Node with the specified ID not found
    }
	
	public static void removeNode(int value, Pane pane)
	{	
		Circle nodec =  (Circle) pane.lookup("#node"+value);
		Text text = (Text) pane.lookup("#text"+value);
		Line ll = (Line) pane.lookup("lineL"+value);
		Line lr = (Line) pane.lookup("lineR"+value);
		pane.getChildren().remove(nodec);
		pane.getChildren().remove(text);
		pane.getChildren().remove(ll);
		pane.getChildren().remove(lr);
		Iterator<DrawBinSearchTree> iterator = alleNodes.iterator();
        while (iterator.hasNext()) {
        	DrawBinSearchTree node = iterator.next();
	            if (node.id.equals("node"+value)) {
	                iterator.remove();
	            }
	        }
	}
	public static void removeNodeObjOnly(int value)
	{	Iterator<DrawBinSearchTree> iterator = alleNodes.iterator();
        while (iterator.hasNext()) {
        	DrawBinSearchTree node = iterator.next();
	            if (node.id.equals("node"+value)) {
	                iterator.remove();
	            }
	        }
	}

	public static void animationToChild(int parent, int child, int toadd) throws InterruptedException
	{
		Circle pc =  (Circle) drawingpane.lookup("#node"+parent);
		Circle cc =  (Circle) drawingpane.lookup("#node"+child);
		if(pc != null && cc != null)
		{
			double[] pcpos = {pc.getBoundsInParent().getCenterX(), pc.getBoundsInParent().getCenterY()};
			double[] ccpos = {cc.getBoundsInParent().getCenterX(), cc.getBoundsInParent().getCenterY()};
			
			animationNodeToNode(pcpos, ccpos,drawingpane,parent,toadd);
		}	
	}
	
	public static void StartAnimation(Pane drawingpane, application.BSNode root, int x, int y, int offset)
	{	drawingPaneAnimation.set(true);;
		if(!deleteAnimations.isEmpty())
		{
			SequentialTransition deletesequentialTransition = new SequentialTransition();
			SequentialTransition sequentialTransition = new SequentialTransition();
			SequentialTransition successorsequentialTransition = new SequentialTransition();
			sequentialTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.RUNNING)
	            	MainsceneController.runningTransition = sequentialTransition;
	        });
			deletesequentialTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.RUNNING)
	            	MainsceneController.runningTransition = deletesequentialTransition;
	        });
			successorsequentialTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.RUNNING)
	            	MainsceneController.runningTransition = successorsequentialTransition;
	        });
			
			
			
			boolean deleteAnimationwait = true;
	        if(!Animations.isEmpty()) deleteAnimationwait = false;
	        final boolean successorAnimationwait = !successorAnimations.isEmpty();
			while (!Animations.isEmpty()) {
	            sequentialTransition.getChildren().add(Animations.poll());
	        }
	        sequentialTransition.setOnFinished(event -> {
	            Animations = new LinkedList<>();
	            if(successorAnimationwait) successorsequentialTransition.play();
	            else deletesequentialTransition.play();
	        });
	        sequentialTransition.play();
	        
	        while (!successorAnimations.isEmpty()) {
	        	successorsequentialTransition.getChildren().add(successorAnimations.poll());
	        }
	        successorsequentialTransition.setOnFinished(event -> {
	        	successorAnimations = new LinkedList<>();
	        	infopane.getChildren().clear();
	        	deletesequentialTransition.play();
	        });
	        

	        while (!deleteAnimations.isEmpty()) {
	        	deletesequentialTransition.getChildren().add(deleteAnimations.poll());
	        }
	        deletesequentialTransition.setOnFinished(event -> {
	        	deleteAnimations = new LinkedList<>();
	        	DrawBinSearchTree.updateNodesPositions();
	        	drawingPaneAnimation.set(false);
	        });
	        if(deleteAnimationwait) {
	        	if(successorAnimationwait) successorsequentialTransition.play();
	        	else deletesequentialTransition.play();	
	        }
	        
		}
		else {
			SequentialTransition sequentialTransition = new SequentialTransition();
			while (!Animations.isEmpty()) {
	            sequentialTransition.getChildren().add(0,Animations.poll());
	        }
	        if(!addAnimations.isEmpty())
	        	sequentialTransition.getChildren().add(addAnimations.poll());
	        sequentialTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.STOPPED) {
	            	Animations = new LinkedList<>();
		            DrawBinSearchTree.updateNodesPositions();
		            drawingPaneAnimation.set(false);
		            DrawBinSearchTree.drawSubtree(drawingpane, root, x, y, offset);
	            }
	            if (newValue == Animation.Status.RUNNING)
	            	MainsceneController.runningTransition = sequentialTransition;
	        });
	        if(sequentialTransition.getChildren().size()>0)sequentialTransition.play();
	        else {	
	        	drawingPaneAnimation.set(false);
	        	DrawBinSearchTree.drawSubtree(drawingpane, root, x, y, offset);
	        }
        }
	        
	}

	public static void move(int removed, int successor) {
		DrawBinSearchTree toremove = DrawBinSearchTree.findNodeById("node"+removed);
		DrawBinSearchTree sucsr = DrawBinSearchTree.findNodeById("node"+successor);
		if(toremove.node.rightChild == null && toremove.node.leftChild != null)
			{
				DrawBinSearchTree.deleteRootMoveLeftChild(toremove);
				DrawBinSearchTree.removeNode(removed, drawingpane);
			}
		
		else if(toremove.node.rightChild !=null && toremove.node.leftChild == null )
			{
				DrawBinSearchTree.deleteRootMoveRightChild(toremove);
				DrawBinSearchTree.removeNode(removed, drawingpane);
			}
		else {
				DrawBinSearchTree.deleteTransitions(toremove, sucsr);
			}	
	}

	private static void deleteRootMoveLeftChild(DrawBinSearchTree root) {
		
		DrawBinSearchTree.deleteTransitions(root);
		//DrawTree.removeNodeMoveleftChildInfo(root.node.value);
		
		DrawBinSearchTree PParent = DrawBinSearchTree.findLowestAncestorWithChildren(root);
		
		DrawBinSearchTree leftChild = DrawBinSearchTree.findNodeById("node"+root.node.leftChild.value);
		Circle rootCircle = (Circle) drawingpane.lookup("#node"+root.node.value);
		Circle ChildCircle = (Circle) drawingpane.lookup("#node"+leftChild.node.value);
		double rootX = rootCircle.getBoundsInParent().getCenterX();
		double rootY = rootCircle.getBoundsInParent().getCenterY();
		double lcX = ChildCircle.getBoundsInParent().getCenterX();
		double lcY = ChildCircle.getBoundsInParent().getCenterY();
		double xdis = rootX - lcX;
		double ydis = rootY - lcY;
		Line ll = (Line) drawingpane.lookup("#lineL"+root.node.value);
		
		
		ParallelTransition parallelTransition = new ParallelTransition();
		//move all children from the left Child
		for (javafx.scene.Node node : drawingpane.getChildren()) {
			String nodeId = node.getId();
			int value = sharedMethodes.extractNumber(nodeId);
		    if ( !root.node.isChild(value)) {
		        continue;
		    }
		    // Create a path that moves horizontally
            Path path = new Path();
            path.getElements().add(new MoveTo(node.getBoundsInParent().getCenterX(), node.getBoundsInParent().getCenterY())); 
            path.getElements().add(new LineTo(node.getBoundsInParent().getCenterX() + xdis, node.getBoundsInParent().getCenterY() + ydis)); 
            if(node instanceof  Circle) {
            	DrawBinSearchTree.changePositionAttribute(value, new double[]{node.getBoundsInParent().getCenterX() + xdis, node.getBoundsInParent().getCenterY() + ydis});
                }
            // Create a PathTransition
            PathTransition pathTransition = DrawBinSearchTree.makePathTransition(path, node);
            parallelTransition.getChildren().add(pathTransition);
        }
		parallelTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	DrawBinSearchTree.removeNodeMoveleftChildInfo(root.node.value);
            	drawingpane.getChildren().remove(ll);
            }
            if (newValue == Animation.Status.STOPPED) {
            	if(PParent != null) DrawBinSearchTree.shrinkTree(PParent.node);
				while(infopane.lookup("#infotext")!= null)
				{
					infopane.getChildren().remove(infopane.lookup("#infotext"));
				}
            }
        });
		
		 
        deleteAnimations.add(parallelTransition);
	}
	
	private static void deleteRootMoveRightChild(DrawBinSearchTree root) {
		
		DrawBinSearchTree.deleteTransitions(root);
		DrawBinSearchTree rightChild = DrawBinSearchTree.findNodeById("node"+root.node.rightChild.value);
		Circle rootCircle = (Circle) drawingpane.lookup("#node"+root.node.value);
		Circle ChildCircle = (Circle) drawingpane.lookup("#node"+rightChild.node.value);
		double rootX = rootCircle.getBoundsInParent().getCenterX();
		double rootY = rootCircle.getBoundsInParent().getCenterY();
		double rcX = ChildCircle.getBoundsInParent().getCenterX();
		double rcY = ChildCircle.getBoundsInParent().getCenterY();
		double xdis = rootX - rcX;
		double ydis = rootY - rcY;
		Line lr = (Line) drawingpane.lookup("#lineR"+root.node.value);
		DrawBinSearchTree PParent = DrawBinSearchTree.findLowestAncestorWithChildren(root);
		ParallelTransition parallelTransition = new ParallelTransition();
		//move all children from the right Child
		for (javafx.scene.Node node : drawingpane.getChildren()) {
			String nodeId = node.getId();
			int value = sharedMethodes.extractNumber(nodeId);
		    if ( !root.node.isChild(value)) {
		        continue;
		    }
            // Create a path that moves horizontally
            Path path = new Path();
            path.getElements().add(new MoveTo(node.getBoundsInParent().getCenterX(), node.getBoundsInParent().getCenterY())); // Start at current position
            path.getElements().add(new LineTo(node.getBoundsInParent().getCenterX() + xdis, node.getBoundsInParent().getCenterY() + ydis)); // Move 100 units to the right
     
            // Create a PathTransition
            PathTransition pathTransition = DrawBinSearchTree.makePathTransition(path, node);
            if(node instanceof  Circle) {
            	DrawBinSearchTree.changePositionAttribute(value, new double[]{node.getBoundsInParent().getCenterX() + xdis, node.getBoundsInParent().getCenterY() + ydis});
            	}
            parallelTransition.getChildren().add(pathTransition);
        }
		parallelTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	DrawBinSearchTree.removeNodeMoveRightChildInfo(root.node.value);
            	drawingpane.getChildren().remove(lr);
            }
            if (newValue == Animation.Status.STOPPED) {
            	if(PParent != null) DrawBinSearchTree.shrinkTree(PParent.node);
				while(infopane.lookup("#infotext")!= null)
				{
					infopane.getChildren().remove(infopane.lookup("#infotext"));
				}
            }
        });
        deleteAnimations.add(parallelTransition);
	}
	
	public static void changePositionAttribute(int value, double[] position)
	{
		Iterator<DrawBinSearchTree> iterator = alleNodes.iterator();
	    while (iterator.hasNext()) {
	        DrawBinSearchTree node = iterator.next();
            if (node.node.value == value) {
                node.position = position;
                return; // Once the position is updated, exit the loop
            }
        }
	}
	
	private static void deleteTransitions(DrawBinSearchTree toremove, DrawBinSearchTree sucsr) {
		double[] targetpos = toremove.position;
		double[] pos = sucsr.position;
		//find the Nodes (Circle, Text, Line) and create the info text
		Text info = DrawBinSearchTree.deleteTransitionInfo(toremove.node.value, sucsr.node.value);
		Circle circleSucsr =(Circle) drawingpane.lookup("#"+sucsr.id);
        Text sucsrValue = (Text)drawingpane.lookup("#text"+sucsr.node.value);
        Circle toremoveCircle =  (Circle) drawingpane.lookup("#"+toremove.id);
        Text toremoveValue = (Text)drawingpane.lookup("#text"+toremove.node.value);
        Line toSucsrLine = DrawBinSearchTree.findLineToNode(sucsr);
        boolean shouldRemoveLine = (sucsr.node.rightChild == null);
        boolean shouldShrink = sucsr.node.leftChild == null && sucsr.node.rightChild ==null;
    	
    	
        // Create a path for the animation
        Path path = new Path();
        path.getElements().add(new MoveTo(pos[0], pos[1]));
        path.getElements().add(new LineTo(targetpos[0], targetpos[1]));
        
        Path path2 = new Path();
        path2.getElements().add(new MoveTo(targetpos[0], targetpos[1]));
        path2.getElements().add(new LineTo(targetpos[0]-60, targetpos[1]));
        
        FadeTransition fadeTransition = sharedMethodes.makeFadeTransition(toremoveCircle);
    	FadeTransition fadeTransition2 = sharedMethodes.makeFadeTransition(toremoveValue);
    	fadeTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	infopane.getChildren().clear();
            	drawingpane.getChildren().remove(toremoveValue);
            	drawingpane.getChildren().remove(toremoveCircle);
            	}
    	});
        PathTransition pathTransition = DrawBinSearchTree.makePathTransition(path2, toremoveCircle);
        pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING && oldValue !=Animation.Status.PAUSED) {
            	toremoveCircle.setFill(Color.RED);
            	infopane.getChildren().add(info);
            }
        });
        pathTransition.setOnFinished(event -> {
        	fadeTransition.play();
        	fadeTransition2.play();
        });
        
        // Create path transitions
        PathTransition pathTransition0 = DrawBinSearchTree.makePathTransition(path2, toremoveValue);
        PathTransition pathTransition1 = DrawBinSearchTree.makePathTransition(path, circleSucsr);
        pathTransition1.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	circleSucsr.setFill(Color.GREEN);
            	if(shouldRemoveLine)
            		drawingpane.getChildren().remove(toSucsrLine);
            }
            if (newValue == Animation.Status.STOPPED) {
            	circleSucsr.setFill(Color.WHITE);
	        	DrawBinSearchTree.switchObjectWithSucsrObject(toremove, sucsr);
	        	if(shouldShrink) DrawBinSearchTree.shrinkTree(sucsr.node);
            }
        });

        // Create a path transition
        PathTransition pathTransition2 = DrawBinSearchTree.makePathTransition(path, sucsrValue);

        ParallelTransition parallelTransition = new ParallelTransition(pathTransition0,pathTransition);
        ParallelTransition parallelTransition2 = new ParallelTransition(pathTransition1, pathTransition2);

        deleteAnimations.add(parallelTransition);
        deleteAnimations.add(parallelTransition2);
        
		if(sucsr.node.rightChild != null)
			DrawBinSearchTree.moveSucsrChildren(sucsr);
		
	
	}


	private static void switchObjectWithSucsrObject(DrawBinSearchTree toremove, DrawBinSearchTree sucsr) {
    	String id = sucsr.id;
    	int value = sucsr.node.value;
    	int tremoveValue = sharedMethodes.extractNumber(toremove.id);

    	Circle circle = (Circle) drawingpane.lookup("#node"+value);
    	
    	Line lrSucsr = (Line) drawingpane.lookup("#lineR"+value);
    	if(lrSucsr != null)
    		drawingpane.getChildren().remove(lrSucsr);
    	
    	Line lr = (Line) drawingpane.lookup("#lineR"+tremoveValue);
    	Line ll = (Line) drawingpane.lookup("#lineL"+tremoveValue);
    	
    	if(lr != null)
    	{
    		drawingpane.getChildren().remove(lr);
    		String newLRId = "lineR" + value;
    		lr.setId(newLRId);
    		drawingpane.getChildren().add(lr);
    	}
    	if(ll != null)
    	{
    		drawingpane.getChildren().remove(ll);
    		String newLLId = "lineL" + value;
    		ll.setId(newLLId);
    		drawingpane.getChildren().add(ll);
    	}

    	DrawBinSearchTree.removeNodeObjOnly(value);
    	toremove.id = id;
    	toremove.node.value = value;
    	toremove.position[0] = circle.getBoundsInParent().getCenterX();
    	toremove.position[1] = circle.getBoundsInParent().getCenterY();
	}
	
	public static int findIndex(String id) {
        for (int i = 0; i < alleNodes.size(); i++) {
            if (alleNodes.get(i).id.equals(id)) {
                return i; // Return the index of the object if found
            }
        }
        return -1; // Return -1 if the object is not found in the list
    }
	private static void moveSucsrChildren(DrawBinSearchTree root) {
		DrawBinSearchTree rightChild = DrawBinSearchTree.findNodeById("node"+root.node.rightChild.value);
		Circle rootCircle = (Circle) drawingpane.lookup("#node"+root.node.value);
		Circle ChildCircle = (Circle) drawingpane.lookup("#node"+rightChild.node.value);
		double rootX = rootCircle.getBoundsInParent().getCenterX();
		double rootY = rootCircle.getBoundsInParent().getCenterY();
		double rcX = ChildCircle.getBoundsInParent().getCenterX();
		double rcY = ChildCircle.getBoundsInParent().getCenterY();
		double xdis = rootX - rcX;
		double ydis = rootY - rcY;
		Line lr = (Line) drawingpane.lookup("#lineR"+root.node.value);
		
		ParallelTransition parallelTransition = new ParallelTransition();
		parallelTransition.setOnFinished(event ->{drawingpane.getChildren().remove(lr);});

		for (javafx.scene.Node node : drawingpane.getChildren()) {
			String nodeId = node.getId();
			int value = sharedMethodes.extractNumber(nodeId);
		    if ( !root.node.isChild(value) || value == root.node.value) {
		        continue;
		    }
            // Create a path that moves horizontally
            Path path = new Path();
            path.getElements().add(new MoveTo(node.getBoundsInParent().getCenterX(), node.getBoundsInParent().getCenterY())); // Start at current position
            path.getElements().add(new LineTo(node.getBoundsInParent().getCenterX() + xdis, node.getBoundsInParent().getCenterY() + ydis)); // Move 100 units to the right
            
            
            
            // Create a PathTransition
            PathTransition pathTransition = DrawBinSearchTree.makePathTransition(path, node);
            if(node instanceof  Circle) {
            	DrawBinSearchTree.changePositionAttribute(value, new double[]{node.getBoundsInParent().getCenterX() + xdis, node.getBoundsInParent().getCenterY() + ydis});
            	}
            parallelTransition.getChildren().add(pathTransition);
        }

        
        deleteAnimations.add(parallelTransition);
	}
	
	private static Line findLineToNode(DrawBinSearchTree sucsr) {
		BSNode parent = sucsr.node.findParent(alleNodes.get(0).node);
		if(parent==null)
			return null;
		if(sucsr.node.value <= parent.value)
		{
			Line ll = (Line) drawingpane.lookup("#lineL" + parent.value);
			return ll;
		}else {
			Line lr = (Line) drawingpane.lookup("#lineR" + parent.value);
			return lr;
		}
		
	}
	public static void move(int value) {
		DrawBinSearchTree toremove = DrawBinSearchTree.findNodeById("node"+value);
		DrawBinSearchTree.deleteTransitions(toremove);
		Iterator<DrawBinSearchTree> iterator = alleNodes.iterator();
        while (iterator.hasNext()) {
        	DrawBinSearchTree node = iterator.next();
	            if (node.id.equals("node"+value)) {
	            	DrawBinSearchTree.removeNode(value, drawingpane);
	            	break;
	            }
	        }
		
	}

	private static void deleteTransitions(DrawBinSearchTree toremove) {
		double[] pos = toremove.position;
		
		Circle circle = new Circle(pos[0], pos[1], 15);
        circle.setFill(Color.WHITE);
        circle.setStroke(Color.BLACK);
        
        Line lineToRemove = DrawBinSearchTree.findLineToNode(toremove);
        Text tex = new Text(pos[0] - 5, pos[1] + 5, String.valueOf(toremove.node.value));
        drawingpane.getChildren().addAll(circle, tex);
        DrawBinSearchTree parent  = DrawBinSearchTree.findLowestAncestorWithChildren(toremove);
        Path path = new Path();
        path.getElements().add(new MoveTo(pos[0], pos[1]));
        path.getElements().add(new LineTo(pos[0]-60, pos[1]));
        
        FadeTransition fadeTransition = sharedMethodes.makeFadeTransition(circle);
        FadeTransition fadeTransition2 = sharedMethodes.makeFadeTransition(tex);
        
        PathTransition pathTransition = DrawBinSearchTree.makePathTransition(path, circle);
        pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	circle.setFill(Color.RED);
            	if(lineToRemove != null && toremove.node.leftChild ==null && toremove.node.rightChild ==null)
            		drawingpane.getChildren().remove(lineToRemove);
            }
        });
        // ParallelTransition for Path- and FadeTransitions
        ParallelTransition parallelTransition_0 = new ParallelTransition(pathTransition, fadeTransition, fadeTransition2);
        PathTransition pathTransition2 = DrawBinSearchTree.makePathTransition(path, tex);    
        ParallelTransition parallelTransition = new ParallelTransition(parallelTransition_0,pathTransition2);
        parallelTransition.setOnFinished(event -> {
        	if(toremove.node.leftChild==null && toremove.node.rightChild==null)DrawBinSearchTree.shrinkTree(parent.node);});
        deleteAnimations.add(parallelTransition);
	    
	}
	
	private static PathTransition makePathTransition(Path path, Node node) {
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
	
	public static void animationTosuccessor(int parent, int child) {
		Circle pc =  (Circle) drawingpane.lookup("#node"+parent);
		Circle cc =  (Circle) drawingpane.lookup("#node"+child);
		if(pc != null && cc != null)
		{
			double[] pcpos = {pc.getBoundsInParent().getCenterX(), pc.getBoundsInParent().getCenterY()};
			double[] ccpos = {cc.getBoundsInParent().getCenterX(), cc.getBoundsInParent().getCenterY()};
			
			Circle circle = new Circle(4, Color.BLUE);
			circle.setVisible(false);

	        drawingpane.getChildren().add(circle);
	        
	        Path path = new Path();
	        path.getElements().add(new MoveTo(pcpos[0], pcpos[1]));
	        path.getElements().add(new LineTo(ccpos[0], ccpos[1]));
	        Text infoText = DrawBinSearchTree.visualiseStep(parent, child);
	        PathTransition pathTransition = DrawBinSearchTree.makePathTransition(path, circle);
	        pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.RUNNING) {
	                circle.setVisible(true);
	                infoText.setVisible(true);
	            }
	        });
	        if(successorAnimations.isEmpty()) {
	        	pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
		            if (newValue == Animation.Status.RUNNING) {
		                	DrawBinSearchTree.markToRemoveNode(parent);
		                	DrawBinSearchTree.toRemoveNodeInfo(parent);
		            }});
	        }
	        if(DrawBinSearchTree.findNodeById("node"+child).node.leftChild ==null) {
	        	pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
		            if (newValue == Animation.Status.STOPPED) {
		              drawingpane.getChildren().remove(circle);
			          infopane.getChildren().remove(infoText);
			          DrawBinSearchTree.markSuccessorNode(child);
		            }
			    });
	        }
	        else pathTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue == Animation.Status.STOPPED) {
	            drawingpane.getChildren().remove(circle);
	            infopane.getChildren().remove(infoText);}	            
	        }); 
	        successorAnimations.add(pathTransition);
		}
	}
	public static void markToRemoveNode(int value) {
		Circle pc =  (Circle) drawingpane.lookup("#node"+value);
		pc.setFill(Color.RED);
	}
	public static void markSuccessorNode(int value) {
		Circle sc =  (Circle) drawingpane.lookup("#node"+value);
		sc.setFill(Color.GREEN);
	}
	
	private static void toRemoveNodeInfo(int toremove) {
		double x = 10;
		double y = infopane.getHeight()/2;
		Text text = new Text(x, y, "Node with Value "+ toremove+ " found looking for Successor");
		text.setId("infotext");
		infopane.getChildren().add(text);
		
	}
	
	private static void removeNodeMoveleftChildInfo(int toremove) {
		double x = infopane.getWidth()/2;
		Text text = new Text(x, 0, "Node with Value "+ toremove+ " found ");
		double paneWidth = infopane.getWidth();
		text.setWrappingWidth(paneWidth/1.5);
		double textWidth = text.getBoundsInLocal().getWidth();
		x = (paneWidth - textWidth) / 2;
		Text text2 = new Text(x, 30, "Node has no Children on the right");
		Text text3 = new Text(x,60, "Node will be replaced with left Child");
		text.setId("infotext"); text2.setId("infotext"); text3.setId("infotext");
		text2.setWrappingWidth(paneWidth/1.5); text3.setWrappingWidth(paneWidth/1.5);
		text.setX(x);	text2.setX(x);	text3.setX(x);
		infopane.getChildren().add(text);
		infopane.getChildren().add(text2);
		infopane.getChildren().add(text3);
	}
	private static void removeNodeMoveRightChildInfo(int toremove) {
		double x = infopane.getWidth()/2;
		Text text = new Text(x, 0, "Node with Value "+ toremove+ " found ");
		double paneWidth = infopane.getWidth();
		text.setWrappingWidth(paneWidth/1.5);
		double textWidth = text.getBoundsInLocal().getWidth();
		x = (paneWidth - textWidth) / 2;
		Text text2 = new Text(x, 30, "Node has no Children on the left");
		Text text3 = new Text(x, 60, "Node will be replaced with right Child");
		text.setId("infotext"); text2.setId("infotext"); text3.setId("infotext");
		text2.setWrappingWidth(paneWidth/1.5); text3.setWrappingWidth(paneWidth/1.5);
		text.setX(x);	text2.setX(x);	text3.setX(x);
		infopane.getChildren().add(text);
		infopane.getChildren().add(text2);
		infopane.getChildren().add(text3);
	}
	
	private static Text deleteTransitionInfo(int toremove, int sucsr) {
		double x = 10;
		double y = infopane.getHeight()/2;
		Text text = new Text(x, y, "removing node with value "+ toremove+ ", moving Successor");
		text.setVisible(true);
		text.setId("infotext");
		double paneWidth = infopane.getWidth();
		text.setWrappingWidth(paneWidth/1.5);
		double textWidth = text.getBoundsInLocal().getWidth();
		x = (paneWidth - textWidth) / 2;
		text.setX(x);
		return text;
	}
	
	public static void updateNodesPositions()
	{
		if(alleNodes.size()==1)return;
		for (javafx.scene.Node node : drawingpane.getChildren()) {
			if(node instanceof  Circle) {	
				String nodeId = node.getId();
				int value = sharedMethodes.extractNumber(nodeId);
            	Iterator<DrawBinSearchTree> iterator = alleNodes.iterator();
    	        while (iterator.hasNext()) {
    	        	DrawBinSearchTree nodeObj = iterator.next();
    		            if (nodeObj.id.equals("node"+value)) {
    		            	nodeObj.position[0] = node.getBoundsInParent().getCenterX(); 
    		            	nodeObj.position[1] = node.getBoundsInParent().getCenterY(); 
    		            	break;
    		            }
    		        }
            	}
        }
	
	}
	
	public static void updateNodes(BSNode root)
	{
		if(root != null)
		{
			Iterator<DrawBinSearchTree> iterator = alleNodes.iterator();
	        while (iterator.hasNext()) {
	        	DrawBinSearchTree node = iterator.next();
		            if (node.id.equals("node"+root.value)) {
		            	node.node = root;
		            	break;
		            }
		        }
	        if(root.leftChild != null) updateNodes(root.leftChild);
			if(root.rightChild != null) updateNodes(root.rightChild);
		}
	}
	
	public static DrawBinSearchTree findLowestAncestorWithChildren(DrawBinSearchTree root)
	{
		DrawBinSearchTree parent =null;
		if(! root.id.equals(alleNodes.get(0).id))
			parent = DrawBinSearchTree.findNodeById("node"+  root.node.findParent(alleNodes.get(0).node).value );
		while(parent !=null  &&(parent.node.leftChild == null || parent.node.rightChild ==null) )
			{
			if(parent.node.value == alleNodes.get(0).node.value) break;
			parent = DrawBinSearchTree.findNodeById("node"+  parent.node.findParent(alleNodes.get(0).node).value );
		}
		return parent;
	}
	
}
