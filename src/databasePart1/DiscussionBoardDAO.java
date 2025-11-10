package databasePart1;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import application.Question;
import application.Answer;
import application.Questions;
import application.Answers;
import application.Reply;
import application.Replies;

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
        "isCorrect BOOLEAN DEFAULT FALSE," +
        "FOREIGN KEY (questionId) REFERENCES questions(questionId))";
        statement.execute(answersTable);

        //replies table.
        String repliesTable = "CREATE TABLE IF NOT EXISTS replies(" +
        "replyId INT AUTO_INCREMENT PRIMARY KEY," +
        "answerId INT NOT NULL," +
        "content TEXT NOT NULL," +
        "authorUserName VARCHAR(255) NOT NULL," +
        "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "FOREIGN KEY (answerId) REFERENCES answers(answerId))";
        statement.execute(repliesTable);

        // Add trusted_reviewers table
        String trustedReviewersTable = "CREATE TABLE IF NOT EXISTS trusted_reviewers(" +
        "id INT AUTO_INCREMENT PRIMARY KEY," +
        "studentUserName VARCHAR(255) NOT NULL," +
        "reviewerUserName VARCHAR(255) NOT NULL," +
        "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "UNIQUE(studentUserName, reviewerUserName))";
        statement.execute(trustedReviewersTable);
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
            String sql = "UPDATE answers SET content = ?, updatedAt = ?, isAccepted = ?, isCorrect = ? WHERE answerId = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, answer.getContent());
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setBoolean(3, answer.getIsAccepted());
                pstmt.setBoolean(4, answer.isCorrect());
                pstmt.setInt(5, answer.getAnswerId());

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
        //get answer by id
        public Answer getAnswerById(int answerId) throws SQLException {
            String sql = "SELECT * FROM answers WHERE answerId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, answerId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return extractAnswerFromResultSet(rs);
                }
            }
            return null;
        }

        //REPLY CRUD OPERATIONS

        //insert a reply
        public int createReply(Reply reply) throws SQLException {
            String sql = "INSERT INTO replies (answerId, content, authorUserName, createdAt, updatedAt) "
                    + "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, reply.getAnswerId());
                pstmt.setString(2, reply.getContent());
                pstmt.setString(3, reply.getAuthorUserName());
                pstmt.setTimestamp(4, Timestamp.valueOf(reply.getCreatedAt()));
                pstmt.setTimestamp(5, Timestamp.valueOf(reply.getUpdatedAt()));

                pstmt.executeUpdate();

                // generate replyId
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    reply.setReplyId(generatedId);
                    return generatedId;
                }
            }
            return -1;
        }
        //get all replies for an answer
        public Replies getRepliesForAnswer(int answerId) throws SQLException {
            Replies replies = new Replies();
            String sql = "SELECT * FROM replies WHERE answerId = ? ORDER BY createdAt ASC";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, answerId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Reply r = extractReplyFromResultSet(rs);
                    replies.addReply(r);
                }
            }
            return replies;
        }
      //get all replies
        public Replies getAllReplies() throws SQLException {
            Replies replies = new Replies();
            String sql = "SELECT * FROM replies ORDER BY createdAt DESC"; // FIXED: was "answers" instead of "replies"
            try (PreparedStatement pstmt = connection.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reply r = extractReplyFromResultSet(rs);
                    replies.addReply(r);
                }
            }
            return replies;
        }
        //update a reply
        public boolean updateReply(Reply reply) throws SQLException {
            String sql = "UPDATE replies SET content = ?, updatedAt = ? WHERE replyId = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, reply.getContent());
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(3, reply.getReplyId());

                return pstmt.executeUpdate() > 0;
            }
        }
        //delete a reply
        public boolean deleteReply(int replyId) throws SQLException {
            String sql = "DELETE FROM replies WHERE replyId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, replyId);
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
        // extract an answer from the result set
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
            a.setCorrect(rs.getBoolean("isCorrect"));
            return a;
        }
        // extract a reply from the result set
        private Reply extractReplyFromResultSet(ResultSet rs) throws SQLException {
            Reply r = new Reply(
                rs.getInt("replyId"),
                rs.getInt("answerId"),
                rs.getString("content"),
                rs.getString("authorUserName"),
                rs.getTimestamp("createdAt").toLocalDateTime(),
                rs.getTimestamp("updatedAt").toLocalDateTime()
            );
            return r;
        }
        
        /**
         * Deletes all replies for a specific answer
         */
        public boolean deleteRepliesForAnswer(int answerId) throws SQLException {
            String sql = "DELETE FROM replies WHERE answerId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, answerId);
                return pstmt.executeUpdate() >= 0; // Could be 0 if no replies exist
            }
        }

        /**
         * Deletes all answers for a specific question
         */
        public boolean deleteAnswersForQuestion(int questionId) throws SQLException {
            String sql = "DELETE FROM answers WHERE questionId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, questionId);
                return pstmt.executeUpdate() >= 0; // Could be 0 if no answers exist
            }
        }
        
        /**
         * Adds a reviewer to a student's trusted list.
         * @param studentUserName The username of the student
         * @param reviewerUserName The username of the reviewer to add
         * @return true if successful, false if already exists or error
         */
        public boolean addTrustedReviewer(String studentUserName, String reviewerUserName) throws SQLException {
            if (studentUserName == null || reviewerUserName == null || studentUserName.equals(reviewerUserName)) {
                return false;
            }

            // First check if the reviewer exists in the system (has created questions, answers, or replies)
            if (!userExists(reviewerUserName)) {
                return false;
            }

            String sql = "INSERT INTO trusted_reviewers (studentUserName, reviewerUserName) VALUES (?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, studentUserName);
                pstmt.setString(2, reviewerUserName);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                if (e.getErrorCode() == 23505) { // Unique constraint violation
                    return false;
                }
                throw e;
            }
        }

        /**
         * Removes a reviewer from a student's trusted list.
         * @param studentUserName The username of the student
         * @param reviewerUserName The username of the reviewer to remove
         * @return true if successful, false otherwise
         */
        public boolean removeTrustedReviewer(String studentUserName, String reviewerUserName) throws SQLException {
            if (studentUserName == null || reviewerUserName == null) {
                return false;
            }

            String sql = "DELETE FROM trusted_reviewers WHERE studentUserName = ? AND reviewerUserName = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, studentUserName);
                pstmt.setString(2, reviewerUserName);
                return pstmt.executeUpdate() > 0;
            }
        }
        
        /**
         * Checks if a reviewer is in a student's trusted reviewers list.
         * @param studentUserName The username of the student
         * @param reviewerUserName The username of the reviewer to check
         * @return true if the reviewer is trusted, false otherwise
         */
        public boolean isReviewerTrusted(String studentUserName, String reviewerUserName) throws SQLException {
            if (studentUserName == null || reviewerUserName == null) {
                return false;
            }

            String sql = "SELECT COUNT(*) FROM trusted_reviewers WHERE studentUserName = ? AND reviewerUserName = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, studentUserName);
                pstmt.setString(2, reviewerUserName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
            return false;
        }

        /**
         * Gets the list of trusted reviewers for a student.
         * @param studentUserName The username of the student
         * @return List of trusted reviewer usernames
         */
        public List<String> getTrustedReviewers(String studentUserName) throws SQLException {
            List<String> trustedReviewers = new ArrayList<>();
            if (studentUserName == null) {
                return trustedReviewers;
            }

            String sql = "SELECT reviewerUserName FROM trusted_reviewers WHERE studentUserName = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, studentUserName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        trustedReviewers.add(rs.getString("reviewerUserName"));
                    }
                }
            }
            return trustedReviewers;
        }

        /**
         * Checks if a user exists in the system (has created content)
         */
        private boolean userExists(String userName) throws SQLException {
            if (userName == null) {
                return false;
            }

            // Check if user has created questions, answers, or replies
            String sql = "SELECT COUNT(*) FROM (" +
                         "SELECT authorUserName FROM questions WHERE authorUserName = ? " +
                         "UNION " +
                         "SELECT authorUserName FROM answers WHERE authorUserName = ? " +
                         "UNION " +
                         "SELECT authorUserName FROM replies WHERE authorUserName = ?" +
                         ") AS user_activity";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, userName);
                pstmt.setString(2, userName);
                pstmt.setString(3, userName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
            return false;
        }

        /**
         * Gets the count of how many students trust a particular reviewer
         */
        public int getTrustedReviewerCount(String reviewerUserName) throws SQLException {
            if (reviewerUserName == null) {
                return 0;
            }

            String sql = "SELECT COUNT(*) FROM trusted_reviewers WHERE reviewerUserName = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, reviewerUserName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return 0;
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
