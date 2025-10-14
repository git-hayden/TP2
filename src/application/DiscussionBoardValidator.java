package application;

//validate the discussion board
public class DiscussionBoardValidator {
    //validation constants
    private static final int MIN_TITLE_LENGTH = 5;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MIN_CONTENT_LENGTH = 10;
    private static final int MAX_CONTENT_LENGTH = 5000;

    //validate the title
    public static String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Title cannot be empty";
        }
        String trimmedTitle = title.trim();
        if (trimmedTitle.length() < MIN_TITLE_LENGTH) {
            return "Title must be at least " + MIN_TITLE_LENGTH + " characters long";
        }
        if (trimmedTitle.length() > MAX_TITLE_LENGTH) {
            return "Title must be less than " + MAX_TITLE_LENGTH + " characters long";
        }
        return null; //if the title is valid, do not return anything
    }
    //validate the content of the question/answer
    public static String validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "Content cannot be empty";
        }
        String trimmedContent = content.trim();
        if (trimmedContent.length() < MIN_CONTENT_LENGTH) {
            return "Content must be at least " + MIN_CONTENT_LENGTH + " characters long";
        }
        if (trimmedContent.length() > MAX_CONTENT_LENGTH) {
            return "Content must be less than " + MAX_CONTENT_LENGTH + " characters long";
        }
        return null; //if the content is valid, do not return anything
    }
    //validate the category (optional field)
    public static String validateCategory(String category) {
        if (category != null && !category.trim().isEmpty()) {
            String trimmedCategory = category.trim();
            if(trimmedCategory.length() > 50) {
                return "Category must be less than 50 characters long";
            }
        }
        return null; //if the category is valid, do not return anything
    }
    //validate the complete question
    public static String validateQuestion(String title, String content, String category) {
        String titleError = validateTitle(title);
        if(titleError != null) return titleError;
        String contentError = validateContent(content);
        if(contentError != null) return contentError;
        String categoryError = validateCategory(category);
        if(categoryError != null) return categoryError;
        return null; //if the question is valid, do not return anything
        
    }
    //validate the complete answer
    public static String validateAnswer(String content) {
        return validateContent(content);
    }

    //validate search query
    public static String validateSearchQuery(String keyword) {
        if(keyword == null || keyword.trim().isEmpty()) {
            return "Keyword cannot be empty";
        }
        if (keyword.trim().length() < 2) {
            return "Keyword must be at least 2 characters long";
        }
        return null; //if the keyword is valid, do not return anything
    }
}


