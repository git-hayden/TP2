package databasePart1;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import application.Question;
import application.Answer;
import application.Questions;
import application.Answers;

//data access object for the discussion board
public class DiscussionBoardDAO {
    private Connection connection;
    private Statement statement;

    //db credentials (from DatabaseHelper)

    // JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

    //constructor
    public DiscussionBoardDAO() throws SQLException {
        connectToDatabase();
    }
    //connect to db 
    private void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            createTables();
        } catch (ClassNotFoundException e) {
            throw new SQLException("Failed to connect to the database", e);
        }
    }
    //create the tables
    private void createTables() throws SQLException {
        //questions table.
        String questionsTable = "CREATE TABLE IF NOT EXISTS questions(" +
        "questionId INT AUTO_INCREMENT PRIMARY KEY," +
        "title VARCHAR(255) NOT NULL," +
        "content TEXT NOT NULL," +
        "authorUserName VARCHAR(255) NOT NULL," +
        "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "isAnswered BOOLEAN DEFAULT FALSE," +
        "category VARCHAR(100))";
        statement.execute(questionsTable);
    
    //answers table.
    String answersTable = "CREATE TABLE IF NOT EXISTS answers(" +
    "answerId INT AUTO_INCREMENT PRIMARY KEY," +
    "questionId INT NOT NULL," +
    "content TEXT NOT NULL," +
    "authorUserName VARCHAR(255) NOT NULL," +
    "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
    "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
    "isAccepted BOOLEAN DEFAULT FALSE," +
    "FOREIGN KEY (questionId) REFERENCES questions(questionId))";

    statement.execute(answersTable);
    }
    //insert a question 
    public int createQuestion(Question question) throws SQLException {
        String sql = "INSERT INTO questions (title, content, authorUserName, category) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getContent());
            pstmt.setString(3, question.getAuthorUserName());
            pstmt.setString(4, question.getCategory());
            pstmt.executeUpdate();
            //return the question id
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                question.setQuestionId(generatedId);
                return generatedId;
            }
        }
            return -1;
        }
        //get all questions
        public Questions getAllQuestions() throws SQLException {
            Questions questions = new Questions();
            String sql = "SELECT * FROM questions ORDER BY createdAt DESC";
            try (PreparedStatement pstmt = connection.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    Question q = extractQuestionFromResultSet(rs);
                    questions.addQuestion(q);
                }
            }
            return questions;
        }
        //get question by id
        public Question getQuestionById(int questionId) throws SQLException {
            String sql = "SELECT * FROM questions WHERE questionId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, questionId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return extractQuestionFromResultSet(rs);
                }
            }
            return null;
        }
        //update a question
        public boolean updateQuestion(Question question) throws SQLException {
            String sql = "UPDATE questions SET title = ?, content = ?, updatedAt = ?, "
                    + "isAnswered = ?, category = ? WHERE questionId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, question.getTitle());
                pstmt.setString(2, question.getContent());
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setBoolean(4, question.getIsAnswered());
                pstmt.setString(5, question.getCategory());
                pstmt.setInt(6, question.getQuestionId());
                return pstmt.executeUpdate() > 0;
            }
        }
        //delete a question
        public boolean deleteQuestion(int questionId) throws SQLException {
            String sql = "DELETE FROM questions WHERE questionId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, questionId);
                return pstmt.executeUpdate() > 0;
            }
        }

        //ANSWER CRUD OPERATIONS

        //insert an answer
        public int createAnswer(Answer answer) throws SQLException {
            String sql = "INSERT INTO answers (questionId, content, authorUserName, createdAt, updatedAt, isAccepted) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, answer.getQuestionId());
                pstmt.setString(2, answer.getContent());
                pstmt.setString(3, answer.getAuthorUserName()); 
                pstmt.setTimestamp(4, Timestamp.valueOf(answer.getCreatedAt()));
                pstmt.setTimestamp(5, Timestamp.valueOf(answer.getUpdatedAt()));
                pstmt.setBoolean(6, answer.getIsAccepted());
                
                pstmt.executeUpdate();
                
                // generate answerId
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    answer.setAnswerId(generatedId);
                    return generatedId;
                }
            }
            return -1;
        }
        //get all answers for a question
        public Answers getAnswersForQuestion(int questionId) throws SQLException {
            Answers answers = new Answers();
            String sql = "SELECT * FROM answers WHERE questionId = ? ORDER BY isAccepted DESC, createdAt ASC";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, questionId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Answer a = extractAnswerFromResultSet(rs);
                    answers.addAnswer(a);
                }
            }
            return answers;
        }
        //get all answers
        public Answers getAllAnswers() throws SQLException {
            Answers answers = new Answers();
            String sql = "SELECT * FROM answers ORDER BY createdAt DESC";
            try (PreparedStatement pstmt = connection.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Answer a = extractAnswerFromResultSet(rs);
                    answers.addAnswer(a);
                }
            }
            return answers;
        }
        //update an answer
        public boolean updateAnswer(Answer answer) throws SQLException {
            String sql = "UPDATE answers SET content = ?, updatedAt = ?, isAccepted = ? WHERE answerId = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, answer.getContent());
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setBoolean(3, answer.getIsAccepted());
                pstmt.setInt(4, answer.getAnswerId());
                
                return pstmt.executeUpdate() > 0;
            }
        }
        //delete an answer
        public boolean deleteAnswer(int answerId) throws SQLException {
            String sql = "DELETE FROM answers WHERE answerId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, answerId);
                return pstmt.executeUpdate() > 0;
            }
        }
        //helper methods for all operations
        private Question extractQuestionFromResultSet(ResultSet rs) throws SQLException {
            Question q = new Question(
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("authorUserName")
            );
            q.setQuestionId(rs.getInt("questionId"));
            q.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
            q.setUpdatedAt(rs.getTimestamp("updatedAt").toLocalDateTime());
            q.setIsAnswered(rs.getBoolean("isAnswered"));
            q.setCategory(rs.getString("category"));
            return q;
        }
        //extract an answer from the result set
        private Answer extractAnswerFromResultSet(ResultSet rs) throws SQLException {
            Answer a = new Answer(
                rs.getInt("answerId"),
                rs.getInt("questionId"),
                rs.getString("content"),
                rs.getString("authorUserName"),
                rs.getTimestamp("createdAt").toLocalDateTime(),
                rs.getTimestamp("updatedAt").toLocalDateTime(),
                rs.getBoolean("isAccepted")
            );
            return a;
        }

        //finally, close the connection
        public void closeConnection() {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
