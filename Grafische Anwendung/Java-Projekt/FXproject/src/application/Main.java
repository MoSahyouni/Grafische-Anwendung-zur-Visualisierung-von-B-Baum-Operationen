package application;
	
import java.util.Random;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;


public class Main extends Application {
	public static Stage PrimaryStage=null;
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Mainscene.fxml"));
			AnchorPane anchorPane = (AnchorPane) root.lookup("#mainWindow");
			anchorPane.setStyle("-fx-background-color: #FFFAF0;");
			Group particlesGroup = new Group();
			PrimaryStage =primaryStage;
			anchorPane.getChildren().add(particlesGroup);
			Scene scene = new Scene(root);
			particlesGroup.setTranslateZ(-1);particlesGroup.toBack();
			primaryStage.setOnShown(event -> {
                // Add particles to the group with random positions
                Random random = new Random();
                for (int i = 0; i < 5; i++) {
                    Circle particle = createParticle();
                    particlesGroup.getChildren().add(particle);

                    // Generate random X and Y coordinates within the size of the scene
                    double randomX = random.nextDouble() * anchorPane.getWidth();
                    double randomY = random.nextDouble() * anchorPane.getHeight();
                    
                    // Set the layout coordinates for the particle
                    particle.setLayoutX(randomX);
                    particle.setLayoutY(randomY);
                    
        	        
                }
            });
			primaryStage.maximizedProperty().addListener((obs, wasMaximized, isNowMaximized) -> {
                // Add particles to the group with random positions
				PauseTransition pause = new PauseTransition(Duration.millis(10));
            	Random random = new Random();
                particlesGroup.getChildren().clear();
                pause.setOnFinished(event -> {
		            for (int i = 0; i < 7; i++) {
		                Circle particle = createParticle();
		                particlesGroup.getChildren().add(particle);
		
		                // Generate random X and Y coordinates within the size of the scene
		                double randomX = random.nextDouble() * primaryStage.getWidth();
		                double randomY = random.nextDouble() * primaryStage.getHeight();
		                
		                // Set the layout coordinates for the particle
		                particle.setLayoutX(randomX);
		                particle.setLayoutY(randomY);
		                
		    	        
		
		            }
		        });
                pause.play();});
			primaryStage.setTitle("B-Baum Visualisierung");
			primaryStage.setScene(scene);
			primaryStage.show();
			//Scene scene = new Scene(root);
			
		} catch(Exception e) {
			e.printStackTrace();
			
		} 
	}
	 private Circle createParticle() {
	        // Create a light-colored particle (e.g., white)
	        Circle particle = new Circle(350);
	        RadialGradient gradient = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
	                new Stop(1, generateLightColor()),
	                new Stop(0.5, generateLightColor()));
	        particle.setFill(gradient);
	        particle.setStroke( generateLightColor());
	        particle.setStrokeWidth(1);
	        particle.setOpacity(0.2); // Adjust opacity as needed
	        return particle;
	    }
	 public static Color generateLightColor() {
	        // Define minimum brightness for a light color
	        final double MIN_BRIGHTNESS = 0.3;

	        double red, green, blue;

	        // Generate random RGB components within the valid range
	        do {
	            red = Math.random()*0.5 +0.5;
	            green = Math.random()*0.5;
	            blue = 0;
	        } while ((red + green ) / 2 < MIN_BRIGHTNESS); // Ensure brightness meets minimum threshold


	        return new Color(red, green, blue, 0.5); // Alpha set to 1.0 for fully opaque color
	    }
	public static void main(String[] args) {
		launch(args);
	}
}
