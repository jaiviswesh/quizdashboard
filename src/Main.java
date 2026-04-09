public class Main {
    public static void main(String[] args) {
        try {
            server.QuizServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
