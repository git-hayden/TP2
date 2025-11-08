package application;

import java.time.LocalDateTime;

// Create reply class
public class Reply {
	private int replyId;
    private int answerId;
    private String content;
    private String authorUserName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // constructor getter and setter
    public Reply(int answerId, String content, String authorUserName) {
        this.answerId = answerId;
        this.content = content;
        this.authorUserName = authorUserName;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // constructor with all fields for db retrieval
    public Reply(int replyId, int answerId, String content, String authorUserName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.replyId = replyId;
        this.answerId = answerId;
        this.content = content;
        this.authorUserName = authorUserName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // getters and setters
    public int getReplyId() {
        return replyId;
    }
    public int getAnswerId() {
        return answerId;
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
    public void setReplyId(int replyId) {
        this.replyId = replyId;
    }
    public void setAnswerId(int answerId) {
        this.answerId = answerId;
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
    @Override
    public String toString() {
        return "Reply{" +
                "replyId=" + replyId +
                ", answerId=" + answerId +
                ", content='" + content + '\'' +
                ", authorUserName='" + authorUserName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

