# Online Quiz & Assessment System
### Java Console Application - Core OOP + File I/O

---

## Project Overview

An interactive, terminal-based quiz platform where **admins** manage question banks and **students** take timed multiple-choice quizzes with automatic scoring and performance analysis.

---

## Project Structure

```
QuizSystem/
|-- src/
|   |-- Main.java                  <- Entry point
|   |-- model/
|   |   |-- Question.java          <- Question entity (4-option MCQ)
|   |   |-- User.java              <- User entity (Admin / Student)
|   |   |-- QuizResult.java        <- Result with scoring & grading
|   |-- service/
|   |   |-- DataStore.java         <- File-based persistence (Serialization)
|   |   |-- QuestionBank.java      <- CRUD for question repository
|   |   |-- AuthService.java       <- Login & registration
|   |   |-- QuizEngine.java        <- Quiz session, timer, answer tracking
|   |-- ui/
|   |   |-- AdminPanel.java        <- Admin console menu
|   |   |-- QuizUI.java            <- Student quiz interface
|   |-- util/
|   |   |-- ConsoleUtil.java       <- ANSI colours, prompts, progress bar
|-- data/                          <- Auto-created; stores .dat files
|-- build.sh                       <- Linux/macOS build & run script
|-- build.bat                      <- Windows build & run script
\-- README.md
```

---

## How to Run

### Prerequisites
- **JDK 11 or higher** — [Download Adoptium](https://adoptium.net/)

### Linux / macOS
```bash
chmod +x build.sh
./build.sh
```

### Windows
```
build.bat
```

### Manual (any OS)
```bash
mkdir out
javac -d out -sourcepath src src/Main.java src/model/*.java src/service/*.java src/util/*.java src/ui/*.java
java -cp out Main
```

---

## Default Login Credentials

| Role    | Username  | Password     |
|---------|-----------|--------------|
| Admin   | `admin`   | `admin123`   |
| Student | `student` | `student123` |

> New student accounts can be created from the **Register** option on the main menu.

---

## Features

### Authentication
- Login / Logout for Admin and Student roles
- Student self-registration
- Role-based menu routing

### Admin Panel
| Feature | Description |
|---|---|
| View All Questions | Lists every question with options and correct answer highlighted |
| Add Question | Interactive wizard: text → 4 options → correct answer → category → difficulty |
| Remove Question | Delete by question ID |
| View All Results | Tabular leaderboard of all student attempts |
| Question Statistics | Breakdown by category and difficulty level |

### Student Quiz Interface
| Feature | Description |
|---|---|
| Full Quiz | All questions, randomly shuffled |
| By Category | Filter by topic (Java Basics, OOP, Collections, etc.) |
| By Difficulty | Easy / Medium / Hard |
| Timed Questions | **30-second countdown** per question; auto-skips on timeout |
| Answer Review | Post-quiz detailed breakdown: correct / wrong / skipped |
| My Results | Personal history of past attempts |

### Scoring & Grading
| Grade | Percentage |
|---|---|
| A+  | >= 90% |
| A   | >= 80% |
| B   | >= 70% |
| C   | >= 60% |
| D   | >= 50% |
| F   | < 50% |

Performance labels: **Excellent / Good / Average / Needs Improvement**

---

## Data Persistence

All data is stored automatically in the `data/` directory using Java Object Serialization:

| File | Contents |
|---|---|
| `data/questions.dat` | Question bank |
| `data/users.dat` | User accounts |
| `data/results.dat` | All quiz attempt results |

> Data persists across application restarts. Delete `.dat` files to reset.

---

## OOP Concepts Used

| Concept | Where Applied |
|---|---|
| **Encapsulation** | All model classes (`Question`, `User`, `QuizResult`) use private fields with getters |
| **Abstraction** | `DataStore` hides all serialization details behind static methods |
| **Separation of Concerns** | `model` / `service` / `ui` / `util` layers are clearly separated |
| **Single Responsibility** | Each class has one focused job |
| **Serializable Interface** | All model classes implement `Serializable` for file persistence |

---

## Pre-loaded Sample Questions (10)

| # | Category | Difficulty | Topic |
|---|---|---|---|
| 1 | Java Basics | Easy | int size in Java |
| 2 | Java Basics | Medium | `final` keyword |
| 3 | Java Basics | Easy | OOP definition |
| 4 | Collections | Medium | No-duplicate collection |
| 5 | Collections | Hard | Iterable interface |
| 6 | Java Basics | Easy | Integer division |
| 7 | OOP | Easy | `private` modifier |
| 8 | OOP | Medium | Polymorphism |
| 9 | Exceptions | Medium | ArithmeticException |
| 10 | Collections | Hard | HashMap complexity |

---

## Extending the Project

- **GUI**: Replace `ui/` classes with Swing/JavaFX panels — all service classes remain unchanged
- **Database**: Swap `DataStore.java` with JDBC calls to MySQL — no other class changes needed
- **More question types**: Extend `Question.java` to support True/False or fill-in-the-blank
- **Network multiplayer**: Wrap `QuizEngine` in a socket server for real-time multi-student sessions

---

## Sample Console Output

```
  +------------------------------------------------------+
  |         ONLINE QUIZ & ASSESSMENT SYSTEM              |
  |          Java Project  |  Core OOP + File I/O        |
  +------------------------------------------------------+

-- QUIZ --------------------------------------------------
  [============------------------] 4/10

  Q4 of 10  [Collections | Medium]  Time: 30s

  Which collection does NOT allow duplicate elements?

    A. ArrayList
    B. LinkedList
    C. HashSet          <- correct
    D. Vector

  Your answer (A/B/C/D) or S to skip: C
+  Correct!
```
