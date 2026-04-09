package service;

import model.Question;
import model.QuizResult;
import model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple file-based persistence layer using Java serialization.
 * Each entity type is stored in its own .dat file under the data/ directory.
 */
public class DataStore {

    private static final String DATA_DIR      = "data/";
    private static final String QUESTIONS_FILE = DATA_DIR + "questions.dat";
    private static final String USERS_FILE     = DATA_DIR + "users.dat";
    private static final String RESULTS_FILE   = DATA_DIR + "results.dat";

    // ========================  Questions  ========================

    @SuppressWarnings("unchecked")
    public static List<Question> loadQuestions() {
        return (List<Question>) load(QUESTIONS_FILE, new ArrayList<>());
    }

    public static void saveQuestions(List<Question> questions) {
        save(QUESTIONS_FILE, questions);
    }

    // ========================  Users  ============================

    @SuppressWarnings("unchecked")
    public static List<User> loadUsers() {
        List<User> users = (List<User>) load(USERS_FILE, null);
        if (users == null) {
            // Seed default accounts on first run
            users = new ArrayList<>();
            users.add(new User("admin", "admin123", true));
            users.add(new User("student", "student123", false));
            saveUsers(users);
        }
        return users;
    }

    public static void saveUsers(List<User> users) {
        save(USERS_FILE, users);
    }

    // ========================  Results  ==========================

    @SuppressWarnings("unchecked")
    public static List<QuizResult> loadResults() {
        return (List<QuizResult>) load(RESULTS_FILE, new ArrayList<>());
    }

    public static void saveResults(List<QuizResult> results) {
        save(RESULTS_FILE, results);
    }

    public static void appendResult(QuizResult result) {
        List<QuizResult> results = loadResults();
        results.add(result);
        saveResults(results);
    }

    // ========================  Internal Helpers  =================

    private static Object load(String path, Object defaultValue) {
        File f = new File(path);
        if (!f.exists()) return defaultValue;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return ois.readObject();
        } catch (Exception e) {
            System.err.println("[DataStore] Failed to load " + path + ": " + e.getMessage());
            return defaultValue;
        }
    }

    private static void save(String path, Object data) {
        new File(DATA_DIR).mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("[DataStore] Failed to save " + path + ": " + e.getMessage());
        }
    }
}
