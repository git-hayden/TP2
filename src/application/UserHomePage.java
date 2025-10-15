package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * This page displays a simple welcome message for the user.
 */

public class UserHomePage {
    private Stage stage;
    private String userName;
    
    //constructor
    public UserHomePage(Stage stage, String userName) {
        this.stage = stage;
        this.userName = userName;
    }
    
    //create the scene
    public Scene createScene() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        
        //label to display Hello user
        Label userLabel = new Label("Hello, User " + userName + "!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        //discussion board button
        Button discussionBoardBtn = new Button("Discussion Board");
        discussionBoardBtn.setPrefWidth(200);
        discussionBoardBtn.setOnAction(e -> {
            DiscussionBoardPage dbPage = new DiscussionBoardPage(stage, userName, "User");
            stage.setScene(dbPage.createScene());
        });
        
        layout.getChildren().addAll(userLabel, discussionBoardBtn);
        return new Scene(layout, 800, 400);
    }

    public void show(Stage primaryStage) {
	    // Set the scene to primary stage
	    primaryStage.setScene(createScene());
	    primaryStage.setTitle("User Page");
    }
}
