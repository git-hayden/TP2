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
import java.sql.SQLException;

//UI for the discussion board
public class DisussionBoardPage {
    private Stage stage;
    private String currentUserName;
    private String currentUserRole;
    private DiscussionBoardDAO dao;

    //UI components
    private ListView<Question> questionListView;
    private TextArea questionDetailArea;
    private ListView<Answer> answerListView;
    private TextField searchField;
    private ComboBox<String> filterComboBox;

    //currently selected question 
    private Question selectedQuestion;

    public DisussionBoardPage(Stage stage, String currentUserName, String currentUserRole) {
        this.stage = stage;
        this.currentUserName = currentUserName;
        this.currentUserRole = currentUserRole;

        try {
            this.dao = new DiscussionBoardDAO();
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

        //center: question detail and answer list
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

        searchBox.getChildren().addAll(new Label("Search:"), searchField, searchButton, clearSearchButton);

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
                    setText(status + " " + question.getTitle()+ " (" + question.getAuthorUserName() + ")");
    
                }
            }
        });

        questionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> displayQuestionDetail(newVal)); 

        loadQuestions();
        questionsBox.getChildren().addAll(questionLabel, questionListView);
        return questionsBox;

}
    //create center section with question detail and answer list
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
        answerListView.setPrefHeight(350);

        //cell factory for answer list
        answerListView.setCellFactory(lv -> new ListCell<Answer>() {
            @Override
            protected void updateItem(Answer answer, boolean empty) {
                super.updateItem(answer, empty);
                if (empty || answer == null) {
                    setText(null);
                } else {
                    String status = answer.getIsAccepted() ? "[ACCEPTED] " : "";
                    setText(status + answer.getContent() + "\n - " + answer.getAuthorUserName() + " (" + answer.getCreatedAt().toLocalDate() + ")");
                }
            }
        });

        detailBox.getChildren().addAll(detailLabel, questionDetailArea, answerLabel, answerListView);
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
        //refresh button
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setPrefWidth(180);
        refreshBtn.setOnAction(e -> refreshData());
        //back button
        Button backBtn = new Button("Back");
        backBtn.setPrefWidth(180);
        backBtn.setOnAction(e -> goBack());
        actionBox.getChildren().addAll(
            createQuestionBtn, editQuestionBtn, deleteQuestionBtn,
            new Separator(),
            addAnswerBtn, editAnswerBtn, deleteAnswerBtn,
            new Separator(),
            refreshBtn, backBtn
        );
        return actionBox;
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
        if(!selectedQuestion.getAuthorUserName().equals(currentUserName) && !currentUserRole.equals("Admin")) {
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
        if(!selectedQuestion.getAuthorUserName().equals(currentUserName) && !currentUserRole.equals("Admin")) {
            showError("You are not authorized to delete this question");
            return;
        }
        //dialog for deleting a question
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Question");
        confirm.setHeaderText("Are you sure you want to delete this question?");
        confirm.setContentText("This action cannot be undone, this will delete all answers associated with this question.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
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
                dao.createAnswer(newAnswer);
                showInfo("Answer added successfully!");
                displayQuestionDetail(selectedQuestion);
            } catch (SQLException e) {
                showError("Failed to add answer: " + e.getMessage());
            }
        });
    }
    //edit an answer
    private void editAnswer() {
        Answer selectedAnswer = answerListView.getSelectionModel().getSelectedItem();
        if (selectedAnswer == null) {
            showError("Please select an answer to edit");
            return;
        }
        //check permissions (only author or admin can edit)
        if(!selectedAnswer.getAuthorUserName().equals(currentUserName) && !currentUserRole.equals("Admin")) {
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
        Answer selectedAnswer = answerListView.getSelectionModel().getSelectedItem();
        if (selectedAnswer == null) {
        showError("Please select an answer to delete");
            return;
        }
        //check permissions (only author or admin can delete)
        if(!selectedAnswer.getAuthorUserName().equals(currentUserName) && !currentUserRole.equals("Admin")) {
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
        //perofm search
        private void performSearch() {
            String keyword = searchField.getText();
            String error = DiscussionBoardValidator.validateSearchQuery(keyword);
            if (error != null) {
                showError(error);
                return;
            }
            try {
                Questions allQuestions = dao.getAllQuestions();
                Questions searchResults = allQuestions.search(keyword);
                ObservableList<Question> resultList = FXCollections.observableArrayList(searchResults.getAllQuestions());
                questionListView.setItems(resultList);
            } catch (SQLException e) { showError("Failed to search questions: " + e.getMessage());}
        }
        //clear search
        private void clearSearch() {
            searchField.clear();
            filterComboBox.setValue("All");
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
        }

    //navigate to home page for role
    private void goBack() {
        if(currentUserRole.equals("Admin")) {
            AdminHomePage adminHomePage = new AdminHomePage(stage,currentUserName);
            stage.setScene(adminHomePage.createScene());
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
}