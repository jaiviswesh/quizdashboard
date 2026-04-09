package service;

import model.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the question repository.
 * Questions are persisted to disk via DataStore.
 */
public class QuestionBank {

    private List<Question> questions;
    private int nextId;

    public QuestionBank() {
        questions = DataStore.loadQuestions();
        nextId = questions.stream().mapToInt(Question::getId).max().orElse(0) + 1;
        if (questions.isEmpty()) seedDefaultQuestions();
    }

    // ========================  Admin Operations  =================

    public void addQuestion(String text, String[] options, int correctIdx,
                            String category, int difficulty) {
        questions.add(new Question(nextId++, text, options, correctIdx, category, difficulty));
        DataStore.saveQuestions(questions);
    }

    public boolean removeQuestion(int id) {
        boolean removed = questions.removeIf(q -> q.getId() == id);
        if (removed) DataStore.saveQuestions(questions);
        return removed;
    }

    // ========================  Query Operations  =================

    public List<Question> getAllQuestions() {
        return new ArrayList<>(questions);
    }

    public List<Question> getByCategory(String category) {
        return questions.stream()
                .filter(q -> q.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<Question> getByDifficulty(int level) {
        return questions.stream()
                .filter(q -> q.getDifficultyLevel() == level)
                .collect(Collectors.toList());
    }

    public List<String> getCategories() {
        return questions.stream()
                .map(Question::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public int getTotalCount() { return questions.size(); }

    // ========================  Seed Data  ========================

    private void seedDefaultQuestions() {
        addQuestion("What is the size of an int in Java?",
                new String[]{"2 bytes", "4 bytes", "8 bytes", "Depends on JVM"},
                1, "Java Basics", 1);

        addQuestion("Which keyword is used to prevent method overriding in Java?",
                new String[]{"static", "private", "final", "abstract"},
                2, "Java Basics", 2);

        addQuestion("What does OOP stand for?",
                new String[]{"Object-Oriented Programming",
                             "Open Object Protocol",
                             "Ordered Object Processing",
                             "Operational Object Program"},
                0, "Java Basics", 1);

        addQuestion("Which collection does NOT allow duplicate elements?",
                new String[]{"ArrayList", "LinkedList", "HashSet", "Vector"},
                2, "Collections", 2);

        addQuestion("Which interface must be implemented for a class to be used in a for-each loop?",
                new String[]{"Runnable", "Comparable", "Iterable", "Serializable"},
                2, "Collections", 3);

        addQuestion("What is the output of: System.out.println(10 / 3)?",
                new String[]{"3.33", "3", "3.0", "Compilation error"},
                1, "Java Basics", 1);

        addQuestion("Which access modifier makes a member visible only within its own class?",
                new String[]{"public", "protected", "default", "private"},
                3, "OOP", 1);

        addQuestion("What is polymorphism in Java?",
                new String[]{"A class having multiple constructors",
                             "One interface, many implementations",
                             "Hiding internal data",
                             "Inheriting from multiple classes"},
                1, "OOP", 2);

        addQuestion("Which exception is thrown when dividing by zero with integers?",
                new String[]{"NullPointerException",
                             "ArithmeticException",
                             "IllegalArgumentException",
                             "NumberFormatException"},
                1, "Exceptions", 2);

        addQuestion("What is the time complexity of HashMap.get() in the average case?",
                new String[]{"O(n)", "O(log n)", "O(1)", "O(n log n)"},
                2, "Collections", 3);
    }
}
