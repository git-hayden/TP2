package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import databasePart1.DiscussionBoardDAO;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.List;

//UI for the discussion board
public class DiscussionBoardPage {
    private Stage stage;
    private String currentUserName;
    private String currentUserRole;
    private DiscussionBoardDAO dao;

    //UI components
    private ListView<Question> questionListView;
    private TextArea questionDetailArea;
    private ListView<Answer> answerListView;
    private ListView<Reply> replyListView;
    private TextField searchField;
    private ComboBox<String> filterComboBox;
    private CheckBox trustedOnlyCheckBox;
    private DatabaseHelper dbHelper;

    //currently selected question
    private Question selectedQuestion;

    //currently selected answer
    private Answer selectedAnswer;

    private Button markCorrectBtn;
    private Button markHelpfulBtn;

    public DiscussionBoardPage(Stage stage, String currentUserName, String currentUserRole) {
        this.stage = stage;
        this.currentUserName = currentUserName;
        this.currentUserRole = currentUserRole;

        try {
            this.dao = new DiscussionBoardDAO();
            this.dbHelper = new DatabaseHelper();
            this.dbHelper.connectToDatabase();
        } catch (SQLException e) {
            showError("Failed to connect to the database");
        }
    }

    //create the scene for UI
    public Scene createScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        //top: search and filter
        mainLayout.setTop(createTopSection());

        //left: question list
        mainLayout.setLeft(createQuestionsSection());

        //center: question detail, answer list, and reply list
        mainLayout.setCenter(createDetailSection());

        //right: action buttons.
        mainLayout.setRight(createActionSection());

        return new Scene(mainLayout, 1200, 800);
    }

    //create the top layout for search and filter
    private VBox createTopSection() {
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(10));
        Label titleLabel = new Label("Discussion Board");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox searchBox = new HBox(10);
        searchField = new TextField();
        searchField.setPromptText("Search questions...");
        searchField.setPrefWidth(300);

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> performSearch());

        Button clearSearchButton = new Button("Clear");
        clearSearchButton.setOnAction(e -> clearSearch());

     // Trusted reviewers only checkbox
        trustedOnlyCheckBox = new CheckBox("Questions by Trusted Reviewers");
        trustedOnlyCheckBox.setOnAction(e -> performSearch());

        searchBox.getChildren().addAll(new Label("Search:"), searchField, searchButton, clearSearchButton, trustedOnlyCheckBox);

        //filter
        HBox filterBox = new HBox(10);
        filterComboBox = new ComboBox<>();
        filterComboBox.setItems(FXCollections.observableArrayList("All", "Answered", "Unanswered", "My Questions"));
        filterComboBox.setValue("All");
        filterComboBox.setOnAction(e -> applyFilter());

        filterBox.getChildren().addAll(new Label("Filter by:"), filterComboBox);
        topBox.getChildren().addAll(titleLabel, searchBox, filterBox);
        return topBox;
}
//Create left side with question list
    private VBox createQuestionsSection() {
        VBox questionsBox = new VBox(10);
        questionsBox.setPadding(new Insets(10));
        questionsBox.setPrefWidth(350);

        Label questionLabel = new Label("Questions");
        questionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        questionListView = new ListView<>();
        questionListView.setPrefHeight(600);

        //cell factory for question list
        questionListView.setCellFactory(lv -> new ListCell<Question>() {
            @Override
            protected void updateItem(Question question, boolean empty) {
                super.updateItem(question, empty);
                if (empty || question == null) {
                    setText(null);
                } else{
                    String status = question.getIsAnswered() ? "[✓]" : "[?]";
                    String star = "";
                    try {
                        if (dbHelper != null && dao.isReviewerTrusted(currentUserName, question.getAuthorUserName())) {
                            star = " ★";
                        }
                    } catch (SQLException e) {
                        // Ignore error, just don't show star
                    }
                    setText(status + " " + question.getTitle() + " (" + question.getAuthorUserName() + star + ")");
                }
            }
        });

        questionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> displayQuestionDetail(newVal));

        loadQuestions();
        questionsBox.getChildren().addAll(questionLabel, questionListView);
        return questionsBox;

}
    //create center section with question detail, answer list, and reply list
    private VBox createDetailSection() {
        VBox detailBox = new VBox(10);
        detailBox.setPadding(new Insets(10));

        //question detail
        Label detailLabel = new Label("Question Details");
        detailLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        questionDetailArea = new TextArea();
        questionDetailArea.setEditable(false);
        questionDetailArea.setPrefHeight(200);
        questionDetailArea.setWrapText(true);

        //answer list
        Label answerLabel = new Label("Answers");
        answerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        answerListView = new ListView<>();
        answerListView.setPrefHeight(175);

        //cell factory for answer list
        answerListView.setCellFactory(lv -> new ListCell<Answer>() {
            @Override
            protected void updateItem(Answer answer, boolean empty) {
                super.updateItem(answer, empty);
                if (empty || answer == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    java.time.format.DateTimeFormatter formatter =
                        java.time.format.DateTimeFormatter.ofPattern("hh:mm a · MMM dd, yyyy");

                    String timeInfo = (answer.getCreatedAt() != null)
                            ? answer.getCreatedAt().format(formatter)
                            : "unknown time";

                    String authorName = answer.getAuthorUserName();
                    String star = "";
                    try {
                        if (dbHelper != null && dao.isReviewerTrusted(currentUserName, authorName)) {
                            star = " ★";
                        }
                    } catch (SQLException e) {
                        // Ignore error, just don't show star
                    }
                    Label contentLabel = new Label(String.format(
                        "%s\n(by %s%s at %s)",
                        answer.getContent(),
                        authorName,
                        star,
                        timeInfo
                    ));
                    contentLabel.setWrapText(true);

                    VBox statusBox = new VBox(2);

                    if (answer.getIsAccepted()) {
                        Label verifiedLabel = new Label("          [✓] Verified By Admin");
                        verifiedLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        statusBox.getChildren().add(verifiedLabel);
                    }

                    if (answer.isCorrect()) {
                        Label helpfulLabel = new Label("          [✓] Student Found Helpful");
                        helpfulLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                        statusBox.getChildren().add(helpfulLabel);
                    }

                    HBox hBox = new HBox(10);
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(contentLabel, Priority.ALWAYS);
                    hBox.getChildren().addAll(contentLabel, statusBox);

                    setGraphic(hBox);
                    setText(null);
                }
            }
        });

        answerListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (markCorrectBtn != null) {
                if (newVal != null && newVal.getIsAccepted()) {
                    markCorrectBtn.setText("Mark as Incorrect");
                } else {
                    markCorrectBtn.setText("Mark as Correct");
                }
            }
        });
        answerListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> displayAnswerDetail(newVal));

     // Reply section with trusted filter
        HBox replyHeaderBox = new HBox(10);
        replyHeaderBox.setAlignment(Pos.CENTER_LEFT);
        
        Label replyLabel = new Label("Reviews");
        replyLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Add trusted reviews only checkbox
        CheckBox trustedReviewsOnlyCheckBox = new CheckBox("Trusted Reviews Only");
        trustedReviewsOnlyCheckBox.setOnAction(e -> {
            if (selectedAnswer != null) {
                displayAnswerDetail(selectedAnswer);
            }
        });

        replyHeaderBox.getChildren().addAll(replyLabel, trustedReviewsOnlyCheckBox);

        replyListView = new ListView<>();
        replyListView.setPrefHeight(175);

        // Update the cell factory for reply list to show trust indicator
        replyListView.setCellFactory(lv -> new ListCell<Reply>() {
            @Override
            protected void updateItem(Reply reply, boolean empty) {
                super.updateItem(reply, empty);
                if (empty || reply == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String reviewerName = reply.getAuthorUserName();
                    String star = "";
                    try {
                        if (dbHelper != null && dao.isReviewerTrusted(currentUserName, reviewerName)) {
                            star = " ★";
                        }
                    } catch (SQLException e) {
                        // Ignore error
                    }
                    setText(reply.getContent() + "\n - " + reviewerName + star + " (" + reply.getCreatedAt().toLocalDate() + ")");
                }
            }
        });

        detailBox.getChildren().addAll(detailLabel, questionDetailArea, answerLabel, answerListView, replyHeaderBox, replyListView);
        return detailBox;
    }
//create right section with action buttons
    private VBox createActionSection() {
        VBox actionBox = new VBox(10);
        actionBox.setPadding(new Insets(10));
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPrefWidth(200);

        //add question button
        Button createQuestionBtn = new Button("Create Question");
        createQuestionBtn.setPrefWidth(180);
        createQuestionBtn.setOnAction(e -> createQuestion());
        //edit question
        Button editQuestionBtn = new Button("Edit Question");
        editQuestionBtn.setPrefWidth(180);
        editQuestionBtn.setOnAction(e -> editQuestion());
        //delete question
        Button deleteQuestionBtn = new Button("Delete Question");
        deleteQuestionBtn.setPrefWidth(180);
        deleteQuestionBtn.setOnAction(e -> deleteQuestion());
        //add answer
        Button addAnswerBtn = new Button("Add Answer");
        addAnswerBtn.setPrefWidth(180);
        addAnswerBtn.setOnAction(e -> addAnswer());
        //edit answer
        Button editAnswerBtn = new Button("Edit Answer");
        editAnswerBtn.setPrefWidth(180);
        editAnswerBtn.setOnAction(e -> editAnswer());
        //delete answer
        Button deleteAnswerBtn = new Button("Delete Answer");
        deleteAnswerBtn.setPrefWidth(180);
        deleteAnswerBtn.setOnAction(e -> deleteAnswer());
        // Mark as Correct button (admin only)
        markCorrectBtn = new Button("Mark as Correct");
        markCorrectBtn.setPrefWidth(180);
        markCorrectBtn.setOnAction(e -> markAnswerAsCorrect());
        markCorrectBtn.setDisable(!"admin".equals(currentUserRole));
        // Mark as Helpful button (student only)
        markHelpfulBtn = new Button("Mark as Helpful");
        markHelpfulBtn.setPrefWidth(180);
        markHelpfulBtn.setOnAction(e -> markAnswerAsHelpful());
        markHelpfulBtn.setDisable("admin".equals(currentUserRole));
        //add reply
        Button addReplyBtn = new Button("Add Reply");
        addReplyBtn.setPrefWidth(180);
        addReplyBtn.setOnAction(e -> addReply());
        //edit reply
        Button editReplyBtn = new Button("Edit Reply");
        editReplyBtn.setPrefWidth(180);
        editReplyBtn.setOnAction(e -> editReply());
        //delete reply
        Button deleteReplyBtn = new Button("Delete Reply");
        deleteReplyBtn.setPrefWidth(180);
        deleteReplyBtn.setOnAction(e -> deleteReply());
        //refresh button
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setPrefWidth(180);
        refreshBtn.setOnAction(e -> refreshData());
        //back button
        Button backBtn = new Button("Back");
        backBtn.setPrefWidth(180);
        backBtn.setOnAction(e -> goBack());
        //manage trusted reviewers
        Button manageTrustedBtn = new Button("Manage Trusted Reviewers");
        manageTrustedBtn.setPrefWidth(180);
        manageTrustedBtn.setOnAction(e -> manageTrustedReviewers());

        // Add to appropriate section in the action box
        if ("admin".equals(currentUserRole)) {
            actionBox.getChildren().addAll(
                createQuestionBtn, editQuestionBtn, deleteQuestionBtn,
                new Separator(),
                addAnswerBtn, editAnswerBtn, deleteAnswerBtn, markCorrectBtn,
                new Separator(),
                addReplyBtn, editReplyBtn, deleteReplyBtn,
                new Separator(),
                manageTrustedBtn, // Add here for admin
                new Separator(),
                refreshBtn, backBtn
            );
        } else {
            actionBox.getChildren().addAll(
                createQuestionBtn, editQuestionBtn, deleteQuestionBtn,
                new Separator(),
                addAnswerBtn, editAnswerBtn, deleteAnswerBtn, markHelpfulBtn,
                new Separator(),
                addReplyBtn, editReplyBtn, deleteReplyBtn,
                new Separator(),
                manageTrustedBtn, // Add here for students
                new Separator(),
                refreshBtn, backBtn
            );
        }
        return actionBox;
    }
    
    private void manageTrustedReviewers() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Trusted Reviewers");
        dialog.setHeaderText("Add or remove reviewers from your trusted list");

        BorderPane dialogPane = new BorderPane();
        dialogPane.setPadding(new Insets(10));

        // reviewer section
        HBox addReviewerBox = new HBox(10);
        addReviewerBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField reviewerField = new TextField();
        reviewerField.setPromptText("Enter reviewer username");
        reviewerField.setPrefWidth(200);
        
        Button addButton = new Button("Add to Trusted");
        addButton.setOnAction(e -> {
            String reviewerName = reviewerField.getText().trim();
            if (reviewerName.isEmpty()) {
                showError("Please enter a reviewer username");
                return;
            }
            if (reviewerName.equals(currentUserName)) {
                showError("You cannot add yourself as a trusted reviewer");
                return;
            }
            try {
                if (dao.addTrustedReviewer(currentUserName, reviewerName)) {
                    showInfo("Added " + reviewerName + " to trusted reviewers");
                    reviewerField.clear();
                    refreshTrustedReviewersList(dialogPane); // refresh the list
                } else {
                    showError("Failed to add reviewer. They may not exist or already be trusted.");
                }
            } catch (SQLException ex) {
                showError("Failed to add trusted reviewer: " + ex.getMessage());
            }
        });

        addReviewerBox.getChildren().addAll(new Label("Add Reviewer:"), reviewerField, addButton);
        dialogPane.setTop(addReviewerBox);

        // list of current trusted reviewers
        VBox trustedListBox = new VBox(10);
        trustedListBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label trustedLabel = new Label("Your Trusted Reviewers:");
        trustedLabel.setStyle("-fx-font-weight: bold;");
        
        ListView<String> trustedListView = new ListView<>();
        trustedListView.setPrefHeight(300);
        
        // show remove button next to each reviewer
        trustedListView.setCellFactory(lv -> new ListCell<String>() {
            private final HBox hbox = new HBox();
            private final Label nameLabel = new Label();
            private final Button removeButton = new Button("Remove");
            private final Region spacer = new Region();
            
            {
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.setSpacing(10);
                HBox.setHgrow(spacer, Priority.ALWAYS);
                removeButton.setOnAction(e -> {
                    String reviewer = getItem();
                    if (reviewer != null) {
                        try {
                            if (dao.removeTrustedReviewer(currentUserName, reviewer)) {
                                showInfo("Removed " + reviewer + " from trusted reviewers");
                                refreshTrustedReviewersList(dialogPane);
                            } else {
                                showError("Failed to remove trusted reviewer");
                            }
                        } catch (SQLException ex) {
                            showError("Failed to remove trusted reviewer: " + ex.getMessage());
                        }
                    }
                });
                hbox.getChildren().addAll(nameLabel, spacer, removeButton);
            }
            
            @Override
            protected void updateItem(String reviewer, boolean empty) {
                super.updateItem(reviewer, empty);
                if (empty || reviewer == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    nameLabel.setText(reviewer);
                    setGraphic(hbox);
                }
            }
        });

        trustedListBox.getChildren().addAll(trustedLabel, trustedListView);
        dialogPane.setCenter(trustedListBox);

        // loadinitial data
        refreshTrustedReviewersList(dialogPane);

        dialog.getDialogPane().setContent(dialogPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // helper method to refresh the trusted reviewers list in the dialog
    private void refreshTrustedReviewersList(BorderPane dialogPane) {
        try {
            ListView<String> trustedListView = (ListView<String>) ((VBox) dialogPane.getCenter()).getChildren().get(1);
            List<String> trustedReviewers = dao.getTrustedReviewers(currentUserName);
            ObservableList<String> trustedList = FXCollections.observableArrayList(trustedReviewers);
            trustedListView.setItems(trustedList);
        } catch (SQLException e) {
            showError("Failed to load trusted reviewers: " + e.getMessage());
        }
    }

    //crud operations.

      //create a question
      private void createQuestion() {
        //dialog for creating a question
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Question");
        dialog.setHeaderText("Enter the details of the question");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField titleField = new TextField();
        titleField.setPromptText("Enter the title of the question");
        TextArea contentField = new TextArea();
        contentField.setPromptText("Enter the content of the question");
        contentField.setPrefRowCount(5);
        contentField.setWrapText(true);

        TextField categoryField = new TextField();
        categoryField.setPromptText("Enter the category of the question (optional)");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(contentField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String title = titleField.getText();
                String content = contentField.getText();
                String category = categoryField.getText();
                //validate the question
                String error = DiscussionBoardValidator.validateQuestion(title, content, category);
                if (error != null) {
                    showError(error);
                    return;
                }
                //create the question
                Question newQuestion = new Question(title.trim(), content.trim(), currentUserName);
                if (category != null && !category.trim().isEmpty()) {
                    newQuestion.setCategory(category.trim());
                }

                try {
                    dao.createQuestion(newQuestion);
                    showInfo("Question created successfully!");
                    refreshData();
                } catch (SQLException e) {
                    showError("Failed to create question: " + e.getMessage());
                }
            }
        });
    }

    //update a question
    private void editQuestion() {
        //for selecting and validating
        if (selectedQuestion == null) {
            showError("Please select a question to edit");
            return;
        }
        //check permissions (only admin or author can edit)
        if(!selectedQuestion.getAuthorUserName().equals(currentUserName) && !currentUserRole.equals("admin")) {
            showError("You are not authorized to edit this question");
            return;
        }
        //dialog for editing a question
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Question");
        dialog.setHeaderText("Modify the question details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        TextField titleField = new TextField(selectedQuestion.getTitle());
        TextArea contentField = new TextArea(selectedQuestion.getContent());
        contentField.setPrefRowCount(5);
        contentField.setWrapText(true);
        TextField categoryField = new TextField(selectedQuestion.getCategory() != null ? selectedQuestion.getCategory() : "");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(contentField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String title = titleField.getText();
                String content = contentField.getText();
                String category = categoryField.getText();
                //validate the question
                String error = DiscussionBoardValidator.validateQuestion(title, content, category);
                if (error != null) {
                    showError(error);
                    return;
                }
                //update the question
                selectedQuestion.setTitle(title.trim());
                selectedQuestion.setContent(content.trim());
                if (category != null && !category.trim().isEmpty()) {
                    selectedQuestion.setCategory(category.trim());
                }
                try {
                    dao.updateQuestion(selectedQuestion);
                    showInfo("Question updated successfully!");
                    refreshData();
                } catch (SQLException e) {
                    showError("Failed to update question: " + e.getMessage());
                }
            }
        });
}

  //delete a question
    private void deleteQuestion() {
        if (selectedQuestion == null) {
            showError("Please select a question to delete");
            return;
        }
        //check permissions (only admin or author can delete)
        if(!selectedQuestion.getAuthorUserName().equals(currentUserName) && !currentUserRole.equals("admin")) {
            showError("You are not authorized to delete this question");
            return;
        }
        //dialog for deleting a question
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Question");
        confirm.setHeaderText("Are you sure you want to delete this question?");
        confirm.setContentText("This action cannot be undone, this will delete all answers and reviews associated with this question.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // First, get all answers for this question to delete their reviews
                    Answers answers = dao.getAnswersForQuestion(selectedQuestion.getQuestionId());
                    
                    // Delete all reviews for each answer first
                    for (Answer answer : answers.getAllAnswers()) {
                        try {
                            // Delete all reviews for this answer
                            dao.deleteRepliesForAnswer(answer.getAnswerId());
                        } catch (SQLException e) {
                            // Log but continue with deletion
                            System.err.println("Failed to delete reviews for answer: " + e.getMessage());
                        }
                    }
                    
                    // Then delete all answers for this question
                    dao.deleteAnswersForQuestion(selectedQuestion.getQuestionId());
                    
                    // Finally delete the question
                    dao.deleteQuestion(selectedQuestion.getQuestionId());
                    
                    showInfo("Question deleted successfully");
                    selectedQuestion = null;
                    refreshData();
                } catch (SQLException e) {
                    showError("Failed to delete question: " + e.getMessage());
                }
            }
        });
    }

  //add an answer
    private void addAnswer() {
        if (selectedQuestion == null) {
            showError("Please select a question to add an answer");
            return;
        }
        //dialog for adding an answer
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Answer");
        dialog.setHeaderText("Add answer to: " + selectedQuestion.getTitle());
        dialog.setContentText("Enter the content of the answer");

        dialog.showAndWait().ifPresent(response -> {
            String error = DiscussionBoardValidator.validateAnswer(response);
            if (error != null) {
                showError(error);
                return;
            }
            Answer newAnswer = new Answer(selectedQuestion.getQuestionId(), response.trim(), currentUserName);
            try {
                // Save the new answer to the database
                dao.createAnswer(newAnswer);

                // Update the question's answered status
                selectedQuestion.setIsAnswered(true);
                dao.updateQuestion(selectedQuestion);

                // Refresh the question list to show the checkmark
                refreshQuestionsList();
                
                // Add the new answer to the ListView without removing existing items
                answerListView.getItems().add(newAnswer);
                answerListView.refresh();

                showInfo("Answer added successfully!");
            } catch (SQLException e) {
                showError("Failed to add answer: " + e.getMessage());
            }
        });
    }

    //edit an answer
    private void editAnswer() {
        if (selectedAnswer == null) {
            showError("Please select an answer to edit");
            return;
        }
        //check permissions (only author or admin can edit)
        if(!selectedAnswer.getAuthorUserName().equals(currentUserName) && !currentUserRole.equals("admin")) {
            showError("You are not authorized to edit this answer");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(selectedAnswer.getContent());
        dialog.setTitle("Edit Answer");
        dialog.setContentText("Answer:");

        dialog.showAndWait().ifPresent(content -> {
            String error = DiscussionBoardValidator.validateAnswer(content);
            if (error != null) {
                showError(error);
                return;
            }
            selectedAnswer.setContent(content.trim());
            try {
                dao.updateAnswer(selectedAnswer);
                showInfo("Answer updated successfully!");
                displayQuestionDetail(selectedQuestion);
            } catch (SQLException e) {
                showError("Failed to update answer: " + e.getMessage());
            }
        });
    }
    //delete an answer
    private void deleteAnswer() {
        if (selectedAnswer == null) {
        showError("Please select an answer to delete");
            return;
        }
        //check permissions (only author or admin can delete)
        if(!selectedAnswer.getAuthorUserName().equals(currentUserName) && !currentUserRole.equals("admin")) {
            showError("You are not authorized to delete this answer");
            return;
        }
        //dialog for deleting an answer
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Answer");
        confirm.setHeaderText("Are you sure you want to delete this answer?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    dao.deleteAnswer(selectedAnswer.getAnswerId());
                    showInfo("Answer deleted successfully");
                    displayQuestionDetail(selectedQuestion);
                } catch (SQLException e) {
                    showError("Failed to delete answer: " + e.getMessage());
                }
            }
        });
    }

    //add a reply
    private void addReply() {
        if (selectedAnswer == null) {
            showError("Please select an answer to add a reply");
            return;
        }
        //dialog for adding a reply
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Reply");
        dialog.setHeaderText("Add reply to: " + selectedAnswer.getContent());
        dialog.setContentText("Enter the content of the reply");

        dialog.showAndWait().ifPresent(response -> {
            String error = DiscussionBoardValidator.validateReply(response);
            if (error != null) {
                showError(error);
                return;
            }
            Reply newReply = new Reply(selectedAnswer.getAnswerId(), response.trim(), currentUserName);
            try {
                dao.createReply(newReply);
                showInfo("Reply added successfully!");
                displayAnswerDetail(selectedAnswer);
            } catch (SQLException e) {
                showError("Failed to add reply: " + e.getMessage());
            }
        });
    }
    //edit a reply
    private void editReply() {
        Reply selectedReply = replyListView.getSelectionModel().getSelectedItem();
        if (selectedReply == null) {
            showError("Please select a reply to edit");
            return;
        }
        //check permissions (only author or admin can edit)
        if(!selectedReply.getAuthorUserName().equals(currentUserName) && !currentUserRole.equals("admin")) {
            showError("You are not authorized to edit this reply");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(selectedReply.getContent());
        dialog.setTitle("Edit Reply");
        dialog.setContentText("Reply:");

        dialog.showAndWait().ifPresent(content -> {
            String error = DiscussionBoardValidator.validateReply(content);
            if (error != null) {
                showError(error);
                return;
            }
            selectedReply.setContent(content.trim());
            try {
                dao.updateReply(selectedReply);
                showInfo("Reply updated successfully!");
                displayAnswerDetail(selectedAnswer);
            } catch (SQLException e) {
                showError("Failed to update reply: " + e.getMessage());
            }
        });
    }
    //delete a reply
    private void deleteReply() {
        Reply selectedReply = replyListView.getSelectionModel().getSelectedItem();
        if (selectedReply == null) {
        showError("Please select a reply to delete");
            return;
        }
        //check permissions (only author or admin can delete)
        if(!selectedReply.getAuthorUserName().equals(currentUserName) && !currentUserRole.equals("admin")) {
            showError("You are not authorized to delete this reply");
            return;
        }
        //dialog for deleting a reply
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Reply");
        confirm.setHeaderText("Are you sure you want to delete this reply?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    dao.deleteReply(selectedReply.getReplyId());
                    showInfo("Reply deleted successfully");
                    displayAnswerDetail(selectedAnswer);
                } catch (SQLException e) {
                    showError("Failed to delete reply: " + e.getMessage());
                }
            }
        });
    }

    //helper methods

    //load questions
    private void loadQuestions() {
        try {
            Questions questions = dao.getAllQuestions();
            ObservableList<Question> questionList = FXCollections.observableArrayList(questions.getAllQuestions());
            questionListView.setItems(questionList);
        } catch (SQLException e) { showError("Failed to load questions: " + e.getMessage());}
    }
        //display question detail
        private void displayQuestionDetail(Question question) {
            selectedQuestion = question;
            if(question == null){
                questionDetailArea.clear();
                answerListView.setItems(FXCollections.observableArrayList());
                return;
            }
            String details = "Title: " + question.getTitle() + "\n\n" +
            "Author: " + question.getAuthorUserName() + "\n" +
            "Category: " + (question.getCategory() != null ? question.getCategory() : "N/A") + "\n" +
            "Created At: " + question.getCreatedAt().toLocalDate() + "\n" +
            "Status: " + (question.getIsAnswered() ? "Answered" : "Unanswered") + "\n\n" +
            "Content:\n" + question.getContent();
            questionDetailArea.setText(details);

            //load answers
            try {
                Answers answers = dao.getAnswersForQuestion(question.getQuestionId());
                ObservableList<Answer> answerList = FXCollections.observableArrayList(answers.getAllAnswers());
                answerListView.setItems(answerList);
            } catch (SQLException e) { showError("Failed to load answers: " + e.getMessage());}
        }
        //display answer's reviews
        private void displayAnswerDetail(Answer answer) {
            selectedAnswer = answer;
            if (answer == null) {
                replyListView.setItems(FXCollections.observableArrayList());
                return;
            }
            try {
                Replies replies = dao.getRepliesForAnswer(answer.getAnswerId());
                ObservableList<Reply> replyList = FXCollections.observableArrayList(replies.getAllReplies());
                
                // Check if we need to filter by trusted reviewers
                CheckBox trustedReviewsOnlyCheckBox = findTrustedReviewsCheckBox();
                if (trustedReviewsOnlyCheckBox != null && trustedReviewsOnlyCheckBox.isSelected()) {
                    try {
                        List<String> trustedReviewers = dao.getTrustedReviewers(currentUserName);
                        replyList = replyList.filtered(reply -> 
                            trustedReviewers.contains(reply.getAuthorUserName())
                        );
                    } catch (SQLException e) {
                        // If filtering fails, show all reviews
                        showError("Failed to filter trusted reviews: " + e.getMessage());
                    }
                }
                
                replyListView.setItems(replyList);
            } catch (SQLException e) { 
                showError("Failed to load reviews: " + e.getMessage());
            }
        }

        // Helper method to find the trusted reviews checkbox
        private CheckBox findTrustedReviewsCheckBox() {
            Scene scene = stage.getScene();
            if (scene != null) {
                BorderPane root = (BorderPane) scene.getRoot();
                VBox center = (VBox) root.getCenter();
                for (javafx.scene.Node node : center.getChildren()) {
                    if (node instanceof HBox) {
                        HBox hbox = (HBox) node;
                        for (javafx.scene.Node child : hbox.getChildren()) {
                            if (child instanceof CheckBox) {
                                CheckBox cb = (CheckBox) child;
                                if ("Trusted Reviews Only".equals(cb.getText())) {
                                    return cb;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }
        
      //perform search
        private void performSearch() {
            String keyword = searchField.getText();
            
            // Only validate if there's actually a search keyword
            if (keyword != null && !keyword.trim().isEmpty()) {
                String error = DiscussionBoardValidator.validateSearchQuery(keyword);
                if (error != null) {
                    showError(error);
                    return;
                }
            }
            
            try {
                Questions allQuestions = dao.getAllQuestions();
                Questions searchResults;
                
                // If there's a search keyword, use search, otherwise use all questions
                if (keyword != null && !keyword.trim().isEmpty()) {
                    searchResults = allQuestions.search(keyword.trim());
                } else {
                    searchResults = allQuestions;
                }

                // If trusted only checkbox is checked, filter questions by trusted reviewers
                if (trustedOnlyCheckBox != null && trustedOnlyCheckBox.isSelected()) {
                    try {
                        List<String> trustedReviewers = dao.getTrustedReviewers(currentUserName);
                        // Filter questions that are authored by trusted reviewers
                        Questions filteredResults = new Questions();
                        for (Question q : searchResults.getAllQuestions()) {
                            if (trustedReviewers.contains(q.getAuthorUserName())) {
                                filteredResults.addQuestion(q);
                            }
                        }
                        searchResults = filteredResults;
                    } catch (SQLException e) {
                        // If filtering fails, show all results
                    }
                }

                ObservableList<Question> resultList = FXCollections.observableArrayList(searchResults.getAllQuestions());
                questionListView.setItems(resultList);
            } catch (SQLException e) { showError("Failed to search questions: " + e.getMessage());}
        }
        
        //clear search
        private void clearSearch() {
            searchField.clear();
            filterComboBox.setValue("All");
            if (trustedOnlyCheckBox != null) {
                trustedOnlyCheckBox.setSelected(false);
            }
            loadQuestions();
        }

        //filter questions
        private void applyFilter() {
            try {
                Questions allQuestions = dao.getAllQuestions();
                Questions filtered;
                String filter = filterComboBox.getValue();

                switch (filter) {
                    case "Answered":
                        filtered = allQuestions.filterByAnsweredStatus(true);
                        break;
                    case "Unanswered":
                        filtered = allQuestions.filterByAnsweredStatus(false);
                        break;
                    case "My Questions":
                        filtered = allQuestions.filterByAuthor(currentUserName);
                        break;
                    default:
                        filtered = allQuestions;
                        break;
                }
                ObservableList<Question> resultList = FXCollections.observableArrayList(filtered.getAllQuestions());
                questionListView.setItems(resultList);
            } catch (SQLException e) { showError("Failed to filter questions: " + e.getMessage());}
        }
        //refresh data
        private void refreshData() {
            loadQuestions();
            if(selectedQuestion != null) {
                try {
                    Question refreshed = dao.getQuestionById(selectedQuestion.getQuestionId());
                    displayQuestionDetail(refreshed);
                }catch (SQLException e) {displayQuestionDetail(null);}
            }
            if(selectedAnswer != null) {
                try {
                    Answer refreshed = dao.getAnswerById(selectedAnswer.getAnswerId());
                    displayAnswerDetail(refreshed);
                }catch (SQLException e) {displayAnswerDetail(null);}
            }
        }

    //navigate to home page for role
    private void goBack() {
        if(currentUserRole.equals("admin")) {
            AdminHomePage adminHomePage = new AdminHomePage(stage,currentUserName);
            stage.setScene(adminHomePage.createScene());
        } else if(currentUserRole.equals("Student")) {
            StudentHomePage studentHomePage = new StudentHomePage(stage,currentUserName);
            stage.setScene(studentHomePage.createScene());
        } else {
            UserHomePage userHomePage = new UserHomePage(stage,currentUserName);
            stage.setScene(userHomePage.createScene());
        }
    }
    //Show error and info messages
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Mark selected answer as correct (admin only)
    private void markAnswerAsCorrect() {
        Answer selected = answerListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an answer to mark/unmark as correct.");
            return;
        }

        try {
            boolean wasAccepted = selected.getIsAccepted();

            if (wasAccepted) {
                // Unmark this answer
                selected.setIsAccepted(false);
                dao.updateAnswer(selected);

                // Update the question to "unanswered" if no accepted answers remain
                boolean anyAccepted = answerListView.getItems().stream().anyMatch(a -> a != selected && a.getIsAccepted());
                selectedQuestion.setIsAnswered(anyAccepted);
                dao.updateQuestion(selectedQuestion);

                markCorrectBtn.setText("Mark as Correct");
            } else {
                // Unmark all other answers for this question
                for (Answer ans : answerListView.getItems()) {
                    if (ans.getIsAccepted()) {
                        ans.setIsAccepted(false);
                        dao.updateAnswer(ans);
                    }
                }

                // Mark the selected answer as accepted
                selected.setIsAccepted(true);
                dao.updateAnswer(selected);

                // Update the question as answered
                selectedQuestion.setIsAnswered(true);
                dao.updateQuestion(selectedQuestion);

                markCorrectBtn.setText("Mark as Incorrect");
            }

            answerListView.refresh();
            questionListView.getItems().set(questionListView.getSelectionModel().getSelectedIndex(), selectedQuestion);
            questionListView.refresh();
            displayQuestionDetail(selectedQuestion);

        } catch (SQLException e) {
            showError("Failed to update answer status: " + e.getMessage());
        }
    }

    // Mark selected answer as helpful (student only)
    private void markAnswerAsHelpful() {
        Answer selected = answerListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an answer to mark/unmark as helpful.");
            return;
        }

        try {
            boolean isHelpful = selected.isCorrect();

            if (isHelpful) {
                // Unmark
                selected.setCorrect(false);
                dao.updateAnswer(selected);
            } else {
                // Unmark any previously helpful answers for this question
                for (Answer ans : answerListView.getItems()) {
                    if (ans.isCorrect()) {
                        ans.setCorrect(false);
                        dao.updateAnswer(ans);
                    }
                }
                // Mark selected as helpful
                selected.setCorrect(true);
                dao.updateAnswer(selected);
            }

            answerListView.refresh();
            displayQuestionDetail(selectedQuestion);

        } catch (SQLException e) {
            showError("Error updating helpful status: " + e.getMessage());
        }
    }
    
    // Helper method to refresh the questions list display
    private void refreshQuestionsList() {
        try {
            Questions questions = dao.getAllQuestions();
            ObservableList<Question> questionList = FXCollections.observableArrayList(questions.getAllQuestions());
            questionListView.setItems(questionList);
            
            // Re-select the current question to maintain selection
            if (selectedQuestion != null) {
                for (int i = 0; i < questionList.size(); i++) {
                    if (questionList.get(i).getQuestionId() == selectedQuestion.getQuestionId()) {
                        questionListView.getSelectionModel().select(i);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            showError("Failed to refresh questions: " + e.getMessage());
        }
    }
}
