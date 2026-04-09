package service;

import model.Question;
import model.QuizResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages quiz sessions: question selection, answer recording, timer tracking.
 */
public class QuizEngine {

    private static final int DEFAULT_TIME_PER_QUESTION_SEC = 30;

    private List<Question> sessionQuestions;
    private int[] userAnswers;
    private int currentIndex;
    private long startTimeMs;
    private boolean running;

    /**
     * Start a new quiz session with the given questions (randomly shuffled).
     */
    public void startQuiz(List<Question> questions) {
        sessionQuestions = new ArrayList<>(questions);
        Collections.shuffle(sessionQuestions);
        userAnswers  = new int[sessionQuestions.size()];
        java.util.Arrays.fill(userAnswers, -1);   // -1 = unanswered
        currentIndex = 0;
        startTimeMs  = System.currentTimeMillis();
        running      = true;
    }

    /** Record the user's answer for the current question. */
    public void submitAnswer(int optionIndex) {
        if (running && currentIndex < sessionQuestions.size()) {
            userAnswers[currentIndex] = optionIndex;
        }
    }

    /** Move to the next question. Returns false if quiz is complete. */
    public boolean nextQuestion() {
        if (!running) return false;
        currentIndex++;
        if (currentIndex >= sessionQuestions.size()) {
            running = false;
            return false;
        }
        return true;
    }

    /** Finish early (e.g. user quits or timer expired). */
    public void finishQuiz() { running = false; }

    /** Build and persist the result after the quiz ends. */
    public QuizResult buildResult(String username) {
        long elapsed = (System.currentTimeMillis() - startTimeMs) / 1000;
        QuizResult result = new QuizResult(username, sessionQuestions, userAnswers, elapsed);
        DataStore.appendResult(result);
        return result;
    }

    // ========================  Accessors  ========================

    public Question getCurrentQuestion() {
        if (currentIndex < sessionQuestions.size())
            return sessionQuestions.get(currentIndex);
        return null;
    }

    public int getCurrentIndex()         { return currentIndex; }
    public int getTotalQuestions()       { return sessionQuestions.size(); }
    public boolean isRunning()           { return running; }
    public int getTimeLimitSeconds()     { return DEFAULT_TIME_PER_QUESTION_SEC; }

    /** Seconds elapsed since the quiz started. */
    public long getElapsedSeconds() {
        return (System.currentTimeMillis() - startTimeMs) / 1000;
    }
}
