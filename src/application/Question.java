package application;

import java.time.LocalDateTime;


//create a question class
public class Question {
    private int questionId;
    private String title;
    private String content;
    private String authorUserName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isAnswered;
    private String category;

    //constructor getter and setter
    public Question(String title, String content, String authorUserName) {
        this.title = title;
        this.content = content;
        this.authorUserName = authorUserName;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isAnswered = false;
    }
    //getters and setters
    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }
    public String getAuthorUserName() {
        return authorUserName;
    }
    public int getQuestionId() {
        return questionId;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public boolean getIsAnswered() {
        return isAnswered;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setIsAnswered(boolean isAnswered) {
        this.isAnswered = isAnswered;
        this.updatedAt = LocalDateTime.now(); //update the updatedAt time
    }   
    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now(); //update the updatedAt time
    }
    public void setContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now(); //update the updatedAt time
    }
    public void setAuthorUserName(String authorUserName) {
        this.authorUserName = authorUserName;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }  

    //display the question
    @Override
    public String toString() {
        return "Question{" +
                "questionId=" + questionId +
                ", title='" + title + '\'' +
                ", authorUserName='" + authorUserName + '\'' +
                ", isAnswered=" + isAnswered +
                ", category='" + category + '\'' +
                '}';
    }
}