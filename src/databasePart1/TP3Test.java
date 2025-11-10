package databasePart1;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import application.User;

/**
 * JUnit4 tests for TP3 trusted reviewers functionality.
 */
public class TP3Test {

    private DatabaseHelper dbHelper;
    private DiscussionBoardDAO dao;

    @Before
    public void setUp() throws SQLException {
        dbHelper = new DatabaseHelper();
        dbHelper.connectToDatabase();

        dao = new DiscussionBoardDAO();

        try {
            User student1 = new User("student1", "pass123", "Student");
            if (!dbHelper.doesUserExist("student1")) {
                dbHelper.register(student1);
            }

            User reviewer1 = new User("reviewer1", "pass123", "Reviewer");
            if (!dbHelper.doesUserExist("reviewer1")) {
                dbHelper.register(reviewer1);
            }

            User reviewer2 = new User("reviewer2", "pass123", "Reviewer");
            if (!dbHelper.doesUserExist("reviewer2")) {
                dbHelper.register(reviewer2);
            }
            dbHelper.removeAllTrusts("student1");
        } catch (SQLException e) {
            // Users may already exist, ignore
        }

        // Clean up existing trust relationships
        try {
            dbHelper.removeTrust("student1", "reviewer1");
            dbHelper.removeTrust("student1", "reviewer2");
        } catch (SQLException e) {
            // Ignore if they don't exist
        }
    }

    @After
    public void shutDown() throws SQLException {
        if (dbHelper != null) dbHelper.closeConnection();
        if (dao != null) dao.closeConnection();
    }

    @Test
    public void testAddTrustAndViewTrust() throws SQLException {
        assertTrue(dbHelper.addTrust("student1", "reviewer1"));
        List<String> trusted = dbHelper.viewTrust("student1");
        assertEquals(1, trusted.size());
        assertTrue(trusted.contains("reviewer1"));

        assertTrue(dbHelper.addTrust("student1", "reviewer2"));
        trusted = dbHelper.viewTrust("student1");
        assertEquals(2, trusted.size());
        assertTrue(trusted.contains("reviewer1"));
        assertTrue(trusted.contains("reviewer2"));

        // Duplicate and self-trust
        assertFalse(dbHelper.addTrust("student1", "reviewer1"));
        assertEquals(2, dbHelper.viewTrust("student1").size());
        assertFalse(dbHelper.addTrust("student1", "student1"));
    }

    @Test
    public void testRemoveTrust() throws SQLException {
        dbHelper.addTrust("student1", "reviewer1");
        dbHelper.addTrust("student1", "reviewer2");

        assertTrue(dbHelper.removeTrust("student1", "reviewer1"));
        List<String> trusted = dbHelper.viewTrust("student1");
        assertEquals(1, trusted.size());
        assertFalse(trusted.contains("reviewer1"));
        assertTrue(trusted.contains("reviewer2"));

        assertFalse(dbHelper.removeTrust("student1", "reviewer1"));
        assertFalse(dbHelper.removeTrust(null, "reviewer1"));
    }

    @Test
    public void testIsReviewerTrusted() throws SQLException {
        assertFalse(dao.isReviewerTrusted("student1", "reviewer1"));

        dbHelper.addTrust("student1", "reviewer1");
        assertTrue(dao.isReviewerTrusted("student1", "reviewer1"));

        dbHelper.removeTrust("student1", "reviewer1");
        assertFalse(dao.isReviewerTrusted("student1", "reviewer1"));

        assertFalse(dao.isReviewerTrusted(null, "reviewer1"));
    }

    @Test
    public void testGetTrustedReviewers() throws SQLException {
        List<String> trusted = dao.getTrustedReviewers("student1");
        assertNotNull(trusted);
        assertTrue(trusted.isEmpty());

        dbHelper.addTrust("student1", "reviewer1");
        dbHelper.addTrust("student1", "reviewer2");

        List<String> fromDAO = dao.getTrustedReviewers("student1");
        assertEquals(2, fromDAO.size());
        assertTrue(fromDAO.contains("reviewer1"));
        assertTrue(fromDAO.contains("reviewer2"));

        List<String> fromHelper = dbHelper.viewTrust("student1");
        assertEquals(fromHelper.size(), fromDAO.size());
        assertTrue(fromHelper.contains("reviewer1") && fromDAO.contains("reviewer1"));
        assertTrue(fromHelper.contains("reviewer2") && fromDAO.contains("reviewer2"));

        List<String> nullResult = dao.getTrustedReviewers(null);
        assertNotNull(nullResult);
        assertTrue(nullResult.isEmpty());
    }
    
    @Test
    public void testTrustedReviewersMatchStudentView() throws SQLException {
        dbHelper.removeAllTrusts("student1");

        // add trusted reviewers
        assertTrue(dbHelper.addTrust("student1", "reviewer1"));
        assertTrue(dbHelper.addTrust("student1", "reviewer2"));

        List<String> studentView = dbHelper.viewTrust("student1");
        List<String> daoView = dao.getTrustedReviewers("student1");

        // they should match exactly
        assertEquals(studentView.size(), daoView.size());
        for (String reviewer : studentView) {
            assertTrue(daoView.contains(reviewer));
        }

        // remove one trust and check again
        dbHelper.removeTrust("student1", "reviewer1");

        studentView = dbHelper.viewTrust("student1");
        daoView = dao.getTrustedReviewers("student1");

        assertEquals(studentView.size(), daoView.size());
        for (String reviewer : studentView) {
            assertTrue(daoView.contains(reviewer));
        }
    }

}
