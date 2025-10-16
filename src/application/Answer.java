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
    private boolean isCorrect;

    //constructor getter and setter
    public Answer(int questionId, String content, String authorUserName) {
        this.questionId = questionId;
        this.content = content;
        this.authorUserName = authorUserName;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
	    this.isAccepted = false;
	    this.isCorrect = false;
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
	    this.isCorrect = false;
    }

    //getters and setters
    public int getAnswerId() {
        return answerId;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        this.isCorrect = correct;
        this.updatedAt = LocalDateTime.now();
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

    // allow a student to mark the answer as correct
    public void markAsCorrectByStudent() {
        this.isCorrect = true;
        this.updatedAt = LocalDateTime.now();
    }

    // allow an admin to mark the answer as correct
    public void markAsCorrectByAdmin() {
        this.isCorrect = true;
        this.updatedAt = LocalDateTime.now();
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