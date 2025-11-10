package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.List;

/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin and manages trusted reviewers.
 */

public class AdminHomePage {
    private Stage stage;
    private String userName;
    private DatabaseHelper dbHelper;
    private ListView<String> trustedReviewersList;
    private ObservableList<String> trustedReviewers;

    //constructor
    public AdminHomePage(Stage stage, String userName) {
        this.stage = stage;
        this.userName = userName;
        try {
            this.dbHelper = new DatabaseHelper();
            this.dbHelper.connectToDatabase();
        } catch (SQLException e) {
            showError("Failed to connect to database");
        }
    }

    //create the scene
    public Scene createScene() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(20));

        //label to display the welcome message for the admin
        Label adminLabel = new Label("Hello, Admin " + userName + "!");
        adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        //discussion board button
        Button discussionBoardBtn = new Button("Discussion Board");
        discussionBoardBtn.setPrefWidth(200);
        discussionBoardBtn.setOnAction(e -> {
            DiscussionBoardPage dbPage = new DiscussionBoardPage(stage, userName, "admin");
            stage.setScene(dbPage.createScene());
        });

        // Trusted Reviewers section
        VBox trustedSection = createTrustedReviewersSection();

        layout.getChildren().addAll(adminLabel, discussionBoardBtn, trustedSection);
        return new Scene(layout, 800, 600);
    }

    //create trusted reviewers management section
    private VBox createTrustedReviewersSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setAlignment(Pos.TOP_CENTER);

        Label trustedLabel = new Label("Trusted Reviewers");
        trustedLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // List view for trusted reviewers
        trustedReviewersList = new ListView<>();
        trustedReviewers = FXCollections.observableArrayList();
        trustedReviewersList.setItems(trustedReviewers);
        trustedReviewersList.setPrefHeight(200);
        trustedReviewersList.setPrefWidth(300);

        // Buttons for add/remove
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button addBtn = new Button("Add Reviewer");
        addBtn.setOnAction(e -> addTrustedReviewer());

        Button removeBtn = new Button("Remove Reviewer");
        removeBtn.setOnAction(e -> removeTrustedReviewer());

        buttonBox.getChildren().addAll(addBtn, removeBtn);

        section.getChildren().addAll(trustedLabel, trustedReviewersList, buttonBox);

        // Load existing trusted reviewers
        loadTrustedReviewers();

        return section;
    }

    //load trusted reviewers from database
    private void loadTrustedReviewers() {
        try {
            List<String> reviewers = dbHelper.viewTrust(userName);
            trustedReviewers.clear();
            trustedReviewers.addAll(reviewers);
        } catch (SQLException e) {
            showError("Failed to load trusted reviewers: " + e.getMessage());
        }
    }

    //add a trusted reviewer
    private void addTrustedReviewer() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Trusted Reviewer");
        dialog.setHeaderText("Add a reviewer to your trusted list");
        dialog.setContentText("Reviewer username:");

        dialog.showAndWait().ifPresent(reviewerName -> {
            if (reviewerName == null || reviewerName.trim().isEmpty()) {
                showError("Please enter a reviewer username");
                return;
            }
            try {
                boolean success = dbHelper.addTrust(userName, reviewerName.trim());
                if (success) {
                    showInfo("Reviewer added to trusted list");
                    loadTrustedReviewers();
                } else {
                    showError("Failed to add reviewer. They may already be trusted or the username is invalid.");
                }
            } catch (SQLException e) {
                showError("Failed to add trusted reviewer: " + e.getMessage());
            }
        });
    }

    //remove a trusted reviewer
    private void removeTrustedReviewer() {
        String selected = trustedReviewersList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a reviewer to remove");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Trusted Reviewer");
        confirm.setHeaderText("Remove " + selected + " from trusted list?");
        confirm.setContentText("Are you sure you want to remove this reviewer?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = dbHelper.removeTrust(userName, selected);
                    if (success) {
                        showInfo("Reviewer removed from trusted list");
                        loadTrustedReviewers();
                    } else {
                        showError("Failed to remove reviewer");
                    }
                } catch (SQLException e) {
                    showError("Failed to remove trusted reviewer: " + e.getMessage());
                }
            }
        });
    }

    //show error message
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    //show info message
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

	/**
     * Displays the admin page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
	    // Set the scene to primary stage
	    primaryStage.setScene(createScene());
	    primaryStage.setTitle("Admin Page");
    }
}
