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


