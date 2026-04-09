package model;

import java.io.Serializable;

public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String questionText;
    private String[] options;         // Always 4 options: A, B, C, D
    private int correctOptionIndex;   // 0=A, 1=B, 2=C, 3=D
    private String category;
    private int difficultyLevel;      // 1=Easy, 2=Medium, 3=Hard

    public Question(int id, String questionText, String[] options,
                    int correctOptionIndex, String category, int difficultyLevel) {
        if (options == null || options.length != 4)
            throw new IllegalArgumentException("A question must have exactly 4 options.");
        if (correctOptionIndex < 0 || correctOptionIndex > 3)
            throw new IllegalArgumentException("Correct option index must be 0-3.");
        this.id = id;
        this.questionText = questionText;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.category = category;
        this.difficultyLevel = difficultyLevel;
    }

    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == correctOptionIndex;
    }

    public int getId()                  { return id; }
    public String getQuestionText()     { return questionText; }
    public String[] getOptions()        { return options; }
    public int getCorrectOptionIndex()  { return correctOptionIndex; }
    public String getCategory()         { return category; }
    public int getDifficultyLevel()     { return difficultyLevel; }

    public String getDifficultyLabel() {
        if (difficultyLevel == 1) return "Easy";
        if (difficultyLevel == 2) return "Medium";
        return "Hard";
    }

    @Override
    public String toString() {
        return String.format("Q%d [%s | %s]: %s", id, category, getDifficultyLabel(), questionText);
    }
}
