package application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//manages the collection of answers and allows CRUD operations
public class Answers {
    private List<Answer> answerList;
    
    // constructor
    public Answers() {
        this.answerList = new ArrayList<>();
    }
    
    // constructor with initial list
    public Answers(List<Answer> answers) {
        this.answerList = new ArrayList<>(answers);
    }
    
    // add a new answer
    public void addAnswer(Answer answer) {
        answerList.add(answer);
    }
    
    // get all answers
    public List<Answer> getAllAnswers() {
        return new ArrayList<>(answerList);
    }
    
    // get a specific answer by ID
    public Answer getAnswerById(int answerId) {
        return answerList.stream()
            .filter(a -> a.getAnswerId() == answerId)
            .findFirst()
            .orElse(null);
    }
    
    // get all answers for a specific question
    public Answers getAnswersForQuestion(int questionId) {
        List<Answer> filtered = answerList.stream()
            .filter(a -> a.getQuestionId() == questionId)
            .collect(Collectors.toList());
        return new Answers(filtered);
    }
    
    // update an existing answer
    public boolean updateAnswer(int answerId, String newContent) {
        Answer answer = getAnswerById(answerId);
        if (answer != null) {
            answer.setContent(newContent);
            return true;
        }
        return false;
    }
    
    // remove  answer
    public boolean deleteAnswer(int answerId) {
        return answerList.removeIf(a -> a.getAnswerId() == answerId);
    }
    
    // remove all answers for a specific question
    public boolean deleteAnswersForQuestion(int questionId) {
        return answerList.removeIf(a -> a.getQuestionId() == questionId);
    }
    
    // search by content
    public Answers searchByContent(String keyword) {
        List<Answer> filtered = answerList.stream()
            .filter(a -> a.getContent().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
        return new Answers(filtered);
    }
    
    // author filter
    public Answers filterByAuthor(String authorUserName) {
        List<Answer> filtered = answerList.stream()
            .filter(a -> authorUserName.equals(a.getAuthorUserName()))
            .collect(Collectors.toList());
        return new Answers(filtered);
    }
    // accepted status filter
    public Answers filterByAcceptedStatus(boolean isAccepted) {
        List<Answer> filtered = answerList.stream()
            .filter(a -> a.getIsAccepted() == isAccepted)
            .collect(Collectors.toList());
        return new Answers(filtered);
    }
    
    // search answers for a specific question
    public Answers searchAnswersForQuestion(int questionId, String keyword) {
        List<Answer> filtered = answerList.stream()
            .filter(a -> a.getQuestionId() == questionId &&
                        a.getContent().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
        return new Answers(filtered);
    }
    
    // get count of answers
    public int size() {
        return answerList.size();
    }
    
    // check if empty
    public boolean isEmpty() {
        return answerList.isEmpty();
    }
    
    // clear all answers
    public void clear() {
        answerList.clear();
    }
    
    // get count of answers for question
    public int getAnswerCountForQuestion(int questionId) {
        return (int) answerList.stream()
            .filter(a -> a.getQuestionId() == questionId)
            .count();
    }
}