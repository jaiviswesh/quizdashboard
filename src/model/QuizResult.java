package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class QuizResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private List<Question> questions;
    private int[] userAnswers;          // -1 = unanswered (time ran out)
    private int score;
    private int totalQuestions;
    private long timeTakenSeconds;
    private LocalDateTime attemptTime;

    public QuizResult(String username, List<Question> questions,
                      int[] userAnswers, long timeTakenSeconds) {
        this.username        = username;
        this.questions       = questions;
        this.userAnswers     = userAnswers;
        this.totalQuestions  = questions.size();
        this.timeTakenSeconds = timeTakenSeconds;
        this.attemptTime     = LocalDateTime.now();
        this.score           = calculateScore();
    }

    private int calculateScore() {
        int count = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (userAnswers[i] >= 0 && questions.get(i).isCorrect(userAnswers[i]))
                count++;
        }
        return count;
    }

    public double getPercentage() {
        return totalQuestions == 0 ? 0 : (score * 100.0 / totalQuestions);
    }

    public String getGrade() {
        double pct = getPercentage();
        if (pct >= 90) return "A+";
        if (pct >= 80) return "A";
        if (pct >= 70) return "B";
        if (pct >= 60) return "C";
        if (pct >= 50) return "D";
        return "F";
    }

    public String getPerformanceLabel() {
        double pct = getPercentage();
        if (pct >= 80) return "Excellent";
        if (pct >= 60) return "Good";
        if (pct >= 40) return "Average";
        return "Needs Improvement";
    }

    public String getFormattedTime() {
        long mins = timeTakenSeconds / 60;
        long secs = timeTakenSeconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    public String getAttemptTimeFormatted() {
        return attemptTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    // ---------- Getters ----------
    public String getUsername()          { return username; }
    public List<Question> getQuestions() { return questions; }
    public int[] getUserAnswers()        { return userAnswers; }
    public int getScore()                { return score; }
    public int getTotalQuestions()       { return totalQuestions; }
    public long getTimeTakenSeconds()    { return timeTakenSeconds; }
    public LocalDateTime getAttemptTime(){ return attemptTime; }
}
