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

## 🎮 Using the PowerShell Build Script (Recommended)

We've created a convenient build script for you!

### Run the Build Script:
```powershell
.\build.ps1
```

### Choose Option 9 - Full Setup
This will:
- ✅ Compile all code
- ✅ Start the server automatically
- ✅ Open the web client in your browser

---

## 📖 How to Play

### In the Web Interface:

1. **Enter Your Nickname**
   - Type a unique name (e.g., "Alex", "QuizMaster", etc.)
   - Server address is already set to `localhost:9000`
   - Click **Join Game**

2. **Wait in the Lobby**
   - See other players join
   - Chat with other players
   - Choose game settings (difficulty, time limit)
   - When ready, click **Start Quiz**

3. **Answer Questions**
   - Read each question carefully
   - Click on your answer choice (A, B, C, or D)
   - Watch the timer! ⏱️
   - See if you got it right or wrong
   - Check your score and ranking

4. **View Results**
   - See your final score
   - Check the leaderboard
   - View your accuracy percentage
   - Click **Play Again** to play another round!

---

## 🎯 Game Features

### ✨ What You Can Do:
- **Multiple Players**: Invite friends to join on the same network
- **Real-Time Chat**: Talk to other players in the lobby
- **Live Leaderboard**: See rankings update in real-time
- **Score Tracking**: Watch your score grow with correct answers
- **Streak Counter**: Build up streaks for consecutive correct answers! 🔥
- **Different Categories**: Questions from Science, History, Geography, Math, and more!
- **Difficulty Levels**: Choose Easy, Medium, or Hard
- **Timed Questions**: Race against the clock!

### 📊 Game Statistics:
- Total Score
- Correct Answers
- Wrong Answers  
- Current Streak
- Final Accuracy Percentage

---

## 🔧 Troubleshooting

### Server won't start?
**Error: Address already in use**
- Another instance is already running
- Find and stop it:
  ```powershell
  netstat -ano | findstr :9000
  taskkill /F /PID <PID_NUMBER>
  ```

### Web client won't connect?
- Make sure the server is running first
- Check the server address is `localhost:9000`
- Try refreshing the page (F5)

### Build errors?
- Make sure you have JDK 11 or higher installed
- Check that `javac` is in your PATH:
  ```powershell
  javac -version
  ```

---

## 🌐 Playing with Friends

### On the Same Computer:
1. Start the server once
2. Open multiple browser tabs/windows
3. Each person joins with a different nickname

### On the Same Network (LAN):
1. Start the server on one computer
2. Find your IP address:
   ```powershell
   ipconfig
   ```
   Look for "IPv4 Address" (e.g., 192.168.1.100)
3. Friends connect using: `192.168.1.100:9000`

---

## 📝 Adding Your Own Questions

Edit `questions.txt` and add lines in this format:
```
Question text?|Option1|Option2|Option3|Option4|CorrectIndex|Category|Difficulty
```

### Example:
```
What is the speed of light?|299,792 km/s|150,000 km/s|400,000 km/s|200,000 km/s|1|science|hard
```

### Categories:
- general
- science
- history
- geography
- technology
- sports
- mathematics
- literature

### Difficulty Levels:
- easy
- medium
- hard

---

## 🎨 Customizing the Look

### Change Colors:
Edit `web/css/styles.css` and modify the CSS variables:
```css
:root {
    --primary-color: #6366f1;  /* Main color */
    --success-color: #10b981;  /* Correct answer */
    --danger-color: #ef4444;   /* Wrong answer */
    /* ... and more! */
}
```

---

## 💡 Tips for Best Experience

1. **Use a modern browser** (Chrome, Firefox, Edge, Safari)
2. **Full screen mode** for better immersion (F11)
3. **Multiple monitors?** Put the game on one, chat with friends on the other!
4. **Keep questions balanced** - mix easy and hard questions
5. **Play in teams** - compete with friends!

---

## 🏆 Sample Game Session

```
👤 Player joins → Enters lobby
💬 Chats with other players
⚙️ Sets difficulty to "Hard"
▶️ Clicks "Start Quiz"
❓ Answers 10 questions
📊 Sees final score: 850 points
🥇 Ranks #1 on leaderboard!
🎉 Celebrates victory!
```

---

## 📚 Next Steps

- Read the full `README.md` for advanced features
- Explore the source code to learn how it works
- Add your own features!
- Share with friends and have fun! 🎮

---

**Enjoy playing QuizNet! 🎯**

Have questions? Check the main README.md or create an issue on GitHub.
