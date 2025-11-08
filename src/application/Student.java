package application;

public class Student {
    private User user;

    public Student(User user) {
            this.user = user;
        }

        public String getUserName() {
            return user.getUserName();
        }

        public String getRole() {
            return user.getRole();
        }
    public User getUser() {
        return user;
    }
}
