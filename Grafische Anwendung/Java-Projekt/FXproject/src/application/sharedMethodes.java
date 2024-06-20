package application;



import java.util.List;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.shape.Line;
import javafx.util.Duration;

public class sharedMethodes {
	public static  Slider animationspeed = null;
	public static  Pane drawingpane = null;
	public static  Pane infopane = null;
	public static BooleanProperty drawingPaneAnimation = null;
	public static int extractNumber(String input) {
		StringBuilder numberStr = new StringBuilder();
		if(input == null) return -1;
		boolean isNegative = false;
		for (int i = 0; i < input.length(); i++) {
	        char c = input.charAt(i);
	        if (Character.isDigit(c)) {
	            numberStr.append(c);
	        } else if (c == '-' && numberStr.length() == 0) {
	            isNegative = true; // Mark the number as negative
	        }
	    }
	    if (numberStr.length() > 0) {
	        int number = Integer.parseInt(numberStr.toString());
	        return isNegative ? -number : number;
	    } else {
	        return -1; // or handle this case as appropriate for your application
	    }
	}
	
	public static List<javafx.scene.Node> getCrossingLines(Pane pane) {
        List<javafx.scene.Node> lines = pane.getChildren();
        List<javafx.scene.Node> crossingLines = FXCollections.observableArrayList();

        for (int i = 0; i < lines.size(); i++) {
        	if(lines.get(i ) instanceof Line)
        	{
        		Line line1 = (Line) lines.get(i);
	            for (int j = i + 1; j < lines.size(); j++) {
	            	if(!(lines.get(j) instanceof Line)) continue;
	                Line line2 = (Line) lines.get(j);
	                if (doIntersect(line1, line2)) {
	                    crossingLines.add(line1);
	                    crossingLines.add(line2);
	                    return crossingLines;
	                }
	            }
        	}
            
        }
        return null;
    }
	private static boolean doIntersect(Line line1, Line line2) {
        double x1 = line1.getStartX(), y1 = line1.getStartY();
        double x2 = line1.getEndX(), y2 = line1.getEndY();
        double x3 = line2.getStartX(), y3 = line2.getStartY();
        double x4 = line2.getEndX(), y4 = line2.getEndY();

        double denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (denominator == 0) {
            return false; // Lines are parallel
        }

        double intersectX = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / denominator;
        double intersectY = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / denominator;
        //intersection point lies within both line segments
        boolean onLine1 = isBetween(x1, x2, intersectX) && isBetween(y1, y2, intersectY);
        boolean onLine2 = isBetween(x3, x4, intersectX) && isBetween(y3, y4, intersectY);
        return onLine1 && onLine2;
    }

    private static boolean isBetween(double a, double b, double c) {
        return Math.min(a, b) <= c && c <= Math.max(a, b);
    }
    
	 public static Color generateLightColor() {
	        // Define minimum brightness for a the color
	        final double MIN_BRIGHTNESS = 0.7;

	        double red, green, blue;

	        // Generate random RGB components within the range
	        do {
	            red = Math.random();
	            green = Math.random();
	            blue = Math.random();
	        } while ((red + green + blue) / 3 < MIN_BRIGHTNESS); // Ensure brightness


	        return new Color(red, green, blue, 1.0); // Alpha set to 1.0 for fully opaque color
	    }
	 
	 public static FadeTransition makeFadeTransition(javafx.scene.Node node) {
			DoubleProperty durationProperty = new SimpleDoubleProperty(); // Create a DoubleProperty
			durationProperty.bind(animationspeed.valueProperty().multiply(100));
			FadeTransition fadeTransition = new FadeTransition(Duration.millis(durationProperty.get()), node);
	    	fadeTransition.setFromValue(1.0);
	    	fadeTransition.setToValue(0.0);
	    	fadeTransition.setOnFinished(event -> {
		        // Your code to execute after the parallel transition finishes
		    	drawingpane.getChildren().remove(node);
		    });
			return fadeTransition;
		}

	public static double[] calculateDistance(Pane drawingpane, Integer key1, Integer key2) {
		for(javafx.scene.Node node: drawingpane.getChildren())
		{	
			if((node instanceof Circle || node instanceof Rectangle)&& extractNumber(node.getId())== key1)
			{
				for(javafx.scene.Node node2: drawingpane.getChildren())
				{
					if((node2 instanceof Circle || node2 instanceof Rectangle)&&(extractNumber(node2.getId())== key2)&&extractNumber(node2.getId())!= key1)
					{
						double[] pos1 = {node.getBoundsInParent().getCenterX(), node.getBoundsInParent().getCenterY()};
						double[] pos2 = {node2.getBoundsInParent().getCenterX(), node2.getBoundsInParent().getCenterY()};
						if(node2 instanceof Rectangle && DrawBTree.moved.containsKey(key2)) 
							pos2 =DrawBTree.moved.get(key2);
						if(node instanceof Rectangle && DrawBTree.moved.containsKey(key1)) 
							pos1 =DrawBTree.moved.get(key1);
						return new double[] {Math.abs(pos2[0]-pos1[0]), Math.abs(pos2[1]-pos1[1])};
					}
				}
			}
		}
		return null;
	}

	public static double calculateMidX(Pane drawingpane, Integer key1, Integer key2) {
		for(javafx.scene.Node node: drawingpane.getChildren())
		{	
			if(extractNumber(node.getId())!= key1)continue;
			if(node instanceof Circle || node instanceof Rectangle)
			{
				for(javafx.scene.Node node2: drawingpane.getChildren())
				{	if(extractNumber(node2.getId())!= key2)continue;
					if(node2 instanceof Circle || node2 instanceof Rectangle)
					{
						double x1 = node.getBoundsInParent().getCenterX();
						double x2 = node2.getBoundsInParent().getCenterX();
						if(node2 instanceof Circle && DrawBTree.moved.containsKey(key2)) 
							x2 =DrawBTree.moved.get(key2)[0];
						if(node instanceof Circle && DrawBTree.moved.containsKey(key1)) 
							x1 =DrawBTree.moved.get(key1)[0];
						return (x1 + x2) / 2.0;
					}
				}
			}
		}
		return 0.0;
	}
	public static void KeyalreadyInTree(int wert) {
		double y = infopane.getHeight()/2;
		Text text = new Text("The key ("+wert+") is already in the tree."); 
		infopane.getChildren().add(text);
		text.setVisible(false);
		double paneWidth = infopane.getWidth();
		text.setWrappingWidth(paneWidth/1.5);
		double textWidth = text.getBoundsInLocal().getWidth();
		double x = (paneWidth - textWidth) / 2;
		text.setX(x);
		text.setY(y);
		text.setId("infotext");
		//text.getStyleClass().add("text-with-border");
		FadeTransition ft =sharedMethodes.makeFadeTransition(text);
		ft.durationProperty().set(Duration.seconds(2));
		ft.setOnFinished(event-> {infopane.getChildren().remove(text);
			drawingPaneAnimation.set(false);});
		ft.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	drawingPaneAnimation.set(true);
            	text.setVisible(true);
            }});
		//ParallelTransition pt = new ParallelTransition();
		//pt.getChildren().add(ft);
		DrawBinSearchTree.Animations.add(ft);
		
		
	}

	public static void KeyNotInTree(int wert) {
		double y = infopane.getHeight()/2;
		Text text = new Text("key ("+wert+") not found in The Tree"); 
		infopane.getChildren().add(text);
		double paneWidth = infopane.getWidth();
		text.setWrappingWidth(paneWidth/1.5);
		double textWidth = text.getBoundsInLocal().getWidth();
		double x = (paneWidth - textWidth) / 2;
		text.setX(x);
		text.setY(y);
		text.setId("infotext");
		//text.getStyleClass().add("text-with-border");
		FadeTransition ft =sharedMethodes.makeFadeTransition(text);
		ft.durationProperty().set(Duration.seconds(2));
		ft.setOnFinished(event-> {infopane.getChildren().remove(text);
			drawingPaneAnimation.set(false);});
		ft.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.RUNNING) {
            	drawingPaneAnimation.set(true);
            }});
		ft.play();
		
	}
}
