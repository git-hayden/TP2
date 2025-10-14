package application;

import java.time.LocalDateTime;

//create an answer class
public class Answer {
    private int answerId;
    private int questionId;
    private String content;
    private String authorUserName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isAccepted;

    //constructor getter and setter
    public Answer(int questionId, String content, String authorUserName) {
        this.questionId = questionId;
        this.content = content;
        this.authorUserName = authorUserName;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isAccepted = false;
    }

    //constructor with all fields for db retrieval
    public Answer(int answerId, int questionId, String content, String authorUserName, LocalDateTime createdAt, LocalDateTime updatedAt, boolean isAccepted) {
        this.answerId = answerId;
        this.questionId = questionId;
        this.content = content;
        this.authorUserName = authorUserName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isAccepted = isAccepted;
    }

    //getters and setters
    public int getAnswerId() {
        return answerId;
    }
    public int getQuestionId() {
        return questionId;
    }
    public String getContent() {
        return content;
    }
    public String getAuthorUserName() {
        return authorUserName;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public boolean getIsAccepted() {
        return isAccepted;
    }
    public void setAnswerId(int answerId) {
        this.answerId = answerId;
    }
    public void setQuestionId(int questionId) {
        this.questionId = questionId;
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
    public void setIsAccepted(boolean isAccepted) {
        this.isAccepted = isAccepted;
        this.updatedAt = LocalDateTime.now(); //update the updatedAt time
    }   
    //accept the answer
    public void markAsAccepted() {
        this.isAccepted = true;
        this.updatedAt = LocalDateTime.now(); //update the updatedAt time
    }
    //not accept the answer
    public void markAsNotAccepted() {
        this.isAccepted = false;
        this.updatedAt = LocalDateTime.now(); //update the updatedAt time
    }
    @Override
    public String toString() {
        return "Answer{" +
                "answerId=" + answerId +
                ", questionId=" + questionId +
                ", content='" + content + '\'' +
                ", authorUserName='" + authorUserName + '\'' +
                ", isAccepted=" + isAccepted +
                ", createdAt=" + createdAt +
                '}';
    }
}