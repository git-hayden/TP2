package application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//manages the collection of questions
public class Questions {
    private List<Question> questionList;
    
    // constructor
    public Questions() {
        this.questionList = new ArrayList<>();
    }
    
    // constructor with list of questions
    public Questions(List<Question> questions) {
        this.questionList = new ArrayList<>(questions);
    }
    
    // add a question
    public void addQuestion(Question question) {
        questionList.add(question);
    }
    
    // get all questions
    public List<Question> getAllQuestions() {
        return new ArrayList<>(questionList);
    }
    
    // search by question id
    public Question getQuestionById(int questionId) {
        return questionList.stream()
            .filter(q -> q.getQuestionId() == questionId)
            .findFirst()
            .orElse(null);
    }
    
    // update question
    public boolean updateQuestion(int questionId, String newTitle, String newContent) {
        Question question = getQuestionById(questionId);
        if (question != null) {
            question.setTitle(newTitle);
            question.setContent(newContent);
            return true;
        }
        return false;
    }
    
    // remove question
    public boolean deleteQuestion(int questionId) {
        return questionList.removeIf(q -> q.getQuestionId() == questionId);
    }
    
    // search by title
    public Questions searchByTitle(String keyword) {
        List<Question> filtered = questionList.stream()
            .filter(q -> q.getTitle().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
        return new Questions(filtered);
    }
    
    // search by content
    public Questions searchByContent(String keyword) {
        List<Question> filtered = questionList.stream()
            .filter(q -> q.getContent().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
        return new Questions(filtered);
    }
    
    // category filter
    public Questions filterByCategory(String category) {
        List<Question> filtered = questionList.stream()
            .filter(q -> category.equals(q.getCategory()))
            .collect(Collectors.toList());
        return new Questions(filtered);
    }
    
    // author filter
    public Questions filterByAuthor(String authorUserName) {
        List<Question> filtered = questionList.stream()
            .filter(q -> authorUserName.equals(q.getAuthorUserName()))
            .collect(Collectors.toList());
        return new Questions(filtered);
    }
    
    // answered status filter
    public Questions filterByAnsweredStatus(boolean isAnswered) {
        List<Question> filtered = questionList.stream()
            .filter(q -> q.getIsAnswered() == isAnswered)
            .collect(Collectors.toList());
        return new Questions(filtered);
    }
    
    // title OR content filter
    public Questions search(String keyword) {
        List<Question> filtered = questionList.stream()
            .filter(q -> q.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        q.getContent().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
        return new Questions(filtered);
    }
    
    // get count of questions
    public int size() {
        return questionList.size();
    }
    
    // check if empty
    public boolean isEmpty() {
        return questionList.isEmpty();
    }
    
    // clear all questions
    public void clear() {
        questionList.clear();
    }
}