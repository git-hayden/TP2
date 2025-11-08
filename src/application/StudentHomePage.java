package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class StudentHomePage {
    private Stage stage;
    private String userName;

    public StudentHomePage(Stage stage, String userName) {
        this.stage = stage;
        this.userName = userName;
    }

    public Scene createScene() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
    
    //welcome label
    Label welcomeLabel = new Label("Hello, Student " + userName + "!");

    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

    //Discussion Board button
    Button discussionBoardBtn = new Button("Discussion Board");
    discussionBoardBtn.setPrefWidth(200);
    discussionBoardBtn.setOnAction(e -> {
        DiscussionBoardPage dbPage = new DiscussionBoardPage(stage, userName, "Student");
        stage.setScene(dbPage.createScene());

    });

    layout.getChildren().addAll(welcomeLabel, discussionBoardBtn);
    return new Scene(layout, 800, 400);
    } 

    
    public void show(Stage primaryStage) {
        primaryStage.setScene(createScene());
        primaryStage.setTitle("Student Home Page");
    }
}
