package application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// manages the collection of replies
public class Replies {
	private List<Reply> replyList;
    
    // constructor
    public Replies() {
        this.replyList = new ArrayList<>();
    }
    
    // constructor with initial list
    public Replies(List<Reply> replies) {
        this.replyList = new ArrayList<>(replies);
    }
    
    // add a new reply
    public void addReply(Reply reply) {
        replyList.add(reply);
    }
    
    // get all replies
    public List<Reply> getAllReplies() {
        return new ArrayList<>(replyList);
    }
    
    // get a specific reply by ID
    public Reply getReplyById(int replyId) {
        return replyList.stream()
            .filter(a -> a.getReplyId() == replyId)
            .findFirst()
            .orElse(null);
    }
    
    // get all replies for a specific answer
    public Replies getRepliesForAnswer(int answerId) {
        List<Reply> filtered = replyList.stream()
            .filter(a -> a.getAnswerId() == answerId)
            .collect(Collectors.toList());
        return new Replies(filtered);
    }
    
    // update an existing reply
    public boolean updateReply(int replyId, String newContent) {
        Reply reply = getReplyById(replyId);
        if (reply != null) {
            reply.setContent(newContent);
            return true;
        }
        return false;
    }
    
    // remove a reply
    public boolean deleteReply(int replyId) {
        return replyList.removeIf(a -> a.getReplyId() == replyId);
    }
    
    // remove all replies for a specific answer
    public boolean deleteRepliesForAnswer(int answerId) {
        return replyList.removeIf(a -> a.getAnswerId() == answerId);
    }
    
    // search by content
    public Replies searchByContent(String keyword) {
        List<Reply> filtered = replyList.stream()
            .filter(a -> a.getContent().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
        return new Replies(filtered);
    }
    
    // author filter
    public Replies filterByAuthor(String authorUserName) {
        List<Reply> filtered = replyList.stream()
            .filter(a -> authorUserName.equals(a.getAuthorUserName()))
            .collect(Collectors.toList());
        return new Replies(filtered);
    }
}
