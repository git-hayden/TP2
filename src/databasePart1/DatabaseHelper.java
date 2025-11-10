package databasePart1;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import application.User;


/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "org.h2.Driver";
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";

	//  Database credentials
	static final String USER = "sa";
	static final String PASS = "";

	private Connection connection = null;
	private Statement statement = null;
	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement();
			// You can use this command to clear the database and restart from fresh.
			// statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	private void createTables() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "role VARCHAR(20))";
		statement.execute(userTable);

		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE)";
	    statement.execute(invitationCodesTable);

		// Create the trusted reviewers table
		String trustedReviewersTable = "CREATE TABLE IF NOT EXISTS trusted_reviewers ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "studentUserName VARCHAR(255) NOT NULL, "
				+ "reviewerUserName VARCHAR(255) NOT NULL, "
				+ "UNIQUE(studentUserName, reviewerUserName))";
		statement.execute(trustedReviewersTable);
	}


	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, role) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			pstmt.executeUpdate();
		}
	}

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {

	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();

	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}

	// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();

	        if (rs.next()) {
	            return rs.getString("role"); // Return the role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}

	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode() {
	    String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    String query = "INSERT INTO InvitationCodes (code) VALUES (?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return code;
	}

	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // Mark the code as used
	            markInvitationCodeAsUsed(code);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Adds a reviewer to a student's trusted reviewers list.
	 * @param studentUserName The username of the student
	 * @param reviewerUserName The username of the reviewer to trust
	 * @return true if the reviewer was successfully added, false otherwise
	 */
	public boolean addTrust(String studentUserName, String reviewerUserName) throws SQLException {
		if (studentUserName == null || reviewerUserName == null ||
			studentUserName.equals(reviewerUserName)) {
			return false;
		}

		String sql = "INSERT INTO trusted_reviewers (studentUserName, reviewerUserName) VALUES (?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, studentUserName);
			pstmt.setString(2, reviewerUserName);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			// If the unique constraint is violated, the reviewer is already trusted
			if (e.getErrorCode() == 23505 || e.getMessage().contains("UNIQUE")) {
				return false; // Already exists
			}
			throw e;
		}
	}

	/**
	 * Removes a reviewer from a student's trusted reviewers list.
	 * @param studentUserName The username of the student
	 * @param reviewerUserName The username of the reviewer to remove from trust
	 * @return true if the reviewer was successfully removed, false otherwise
	 */
	public boolean removeTrust(String studentUserName, String reviewerUserName) throws SQLException {
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
	 * Returns a list of trusted reviewers for a given student.
	 * @param studentUserName The username of the student
	 * @return List of reviewer usernames that the student trusts
	 */
	public java.util.List<String> viewTrust(String studentUserName) throws SQLException {
		java.util.List<String> trustedReviewers = new java.util.ArrayList<>();
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
	
	// helper to clear trusts
	public void removeAllTrusts(String studentUserName) throws SQLException {
	    if (studentUserName == null) return;
	    String sql = "DELETE FROM trusted_reviewers WHERE studentUserName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	        pstmt.setString(1, studentUserName);
	        pstmt.executeUpdate();
	    }
	}

	// Closes the database connection and statement.
	public void closeConnection() {
		try{
			if(statement!=null) statement.close();
		} catch(SQLException se2) {
			se2.printStackTrace();
		}
		try {
			if(connection!=null) connection.close();
		} catch(SQLException se){
			se.printStackTrace();
		}
	}

}