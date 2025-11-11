# QuizNet - Quick Start Guide

## 🚀 Getting Started in 3 Steps!

### Step 1: Compile the Server
Open PowerShell in the QuizNet directory and run:
```powershell
javac -d out/server server/EnhancedQuizServer.java server/EnhancedQuestionManager.java server/ScoringEngine.java
```

### Step 2: Start the Server
```powershell
java -cp out/server EnhancedQuizServer
```

You should see:
```
Loaded questions:
  literature: 2 questions
  general: 3 questions
  sports: 3 questions
  ...
Enhanced QuizServer started on port 9000
Supports both traditional sockets and WebSocket connections
```

### Step 3: Open the Web Client
Simply double-click `web/index.html` or open it in your browser!

---

