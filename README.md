# QuizNet - Real-Time Multiplayer Quiz System

## Overview
QuizNet is an advanced real-time multiplayer quiz application featuring both a modern web interface and traditional Java client. It demonstrates advanced Java network programming concepts including:
- Java NIO (Selector/Channels)
- WebSocket protocol implementation
- TCP sockets
- Multi-threading and concurrent programming
- Real-time communication

## ✨ Features

### Core Features
- 🎮 **Real-time multiplayer gameplay** - Multiple players can join and compete simultaneously
- 🌐 **Web-based GUI** - Modern, responsive HTML/CSS/JavaScript interface
- 🔌 **Dual Protocol Support** - WebSocket for web clients, traditional sockets for Java clients
- 📊 **Live Leaderboard** - Real-time score tracking and rankings
- 💬 **In-game Chat** - Communicate with other players
- ⏱️ **Timed Questions** - Configurable time limits per question
- 🎯 **Multiple Categories** - Questions organized by topic (Science, History, Geography, etc.)
- 📈 **Difficulty Levels** - Easy, Medium, and Hard questions
- 🏆 **Game Statistics** - Track correct answers, streaks, and accuracy

### Advanced Features
- Real-time timer synchronization
- Animated score updates
- Answer feedback with visual effects
- Responsive design for all screen sizes
- Session management
- Automatic game state management

## 📁 Structure
```
QuizNet/
├── README.md
├── questions.txt                    # Question database with categories
├── server/
│   ├── QuizServer.java             # Original NIO server
│   ├── EnhancedQuizServer.java     # WebSocket-enabled server
│   ├── QuestionManager.java        # Basic question handler
│   ├── EnhancedQuestionManager.java # Advanced question handler
│   └── ScoringEngine.java          # Score calculation
├── client/
│   ├── QuizClient.java             # Console client
│   └── ClientListener.java         # Client message handler
└── web/
    ├── index.html                  # Web client interface
    ├── css/
    │   └── styles.css              # Modern styling
    └── js/
        └── app.js                  # Client-side logic
```

## 🚀 Quick Start

### Prerequisites
- JDK 11 or higher
- Modern web browser (Chrome, Firefox, Edge, Safari)
- `javac` and `java` in your PATH

### Option 1: Web Client (Recommended)

1. **Compile the enhanced server:**
   ```powershell
   javac -d out/server server/*.java
   ```

2. **Start the enhanced server:**
   ```powershell
   java -cp out/server EnhancedQuizServer
   ```

3. **Open the web client:**
   - Simply open `web/index.html` in your browser
   - Or serve it with a local web server:
     ```powershell
     # Using Python 3
     cd web
     python -m http.server 8080
     ```
   - Navigate to `http://localhost:8080`

4. **Play the game:**
   - Enter your nickname
   - Server address: `localhost:9000`
   - Click "Join Game"
   - Wait for other players or click "Start Quiz"

### Option 2: Console Client (Original)

1. **Compile server:**
   ```powershell
   javac -d out/server server/*.java
   ```

2. **Run server:**
   ```powershell
   java -cp out/server QuizServer
   ```

3. **Compile client:**
   ```powershell
   javac -d out/client client/*.java
   ```

4. **Run client(s):**
   ```powershell
   java -cp out/client QuizClient
   ```

## 🎮 How to Play

### Web Client Instructions

1. **Login Screen**
   - Enter a unique nickname
   - Specify server address (default: localhost:9000)
   - Click "Join Game"

2. **Lobby**
   - See all connected players
   - Chat with other players
   - Configure game settings (difficulty, time per question)
   - Anyone can start the quiz by clicking "Start Quiz"

3. **During the Quiz**
   - Read the question carefully
   - Click on your answer before time runs out
   - See live rankings on the sidebar
   - Track your statistics (correct, wrong, streak)

4. **Results**
   - View your final score and accuracy
   - See the complete leaderboard
   - Play again or return to lobby

### Console Client Commands
- **Join:** Automatic on start (enter nickname)
- **Answer:** Type when question appears
- **Chat:** `chat your message`
- **Start:** Type `start` to begin quiz
- **Quit:** Type `quit` to exit

## 🔧 Configuration

### Server Configuration
Edit the server startup to customize:
```java
// In EnhancedQuizServer.java main method
int port = 9000;                    // Server port
String questionsFile = "questions.txt";  // Question database
```

### Question Database Format
Add questions to `questions.txt`:
```
Question text?|Option1|Option2|Option3|Option4|CorrectIndex|Category|Difficulty
```

Example:
```
What is 2+2?|3|4|5|6|2|mathematics|easy
```

Categories: `general`, `science`, `history`, `geography`, `technology`, `sports`, `mathematics`, `literature`

Difficulty: `easy`, `medium`, `hard`

### Game Settings (Web Client)
- **Difficulty**: Easy, Medium, or Hard
- **Time per Question**: 10, 15, 20, or 30 seconds

## 🏗️ Architecture

### Server Architecture
- **NIO Selector Pattern**: Efficient handling of multiple concurrent connections
- **WebSocket Implementation**: Full handshake and frame parsing
- **Dual Protocol Support**: Seamlessly handles both WebSocket and traditional socket clients
- **Thread Pool**: Scheduled executors for timer management
- **Concurrent Collections**: Thread-safe client and score management

### Client Architecture (Web)
- **Single Page Application**: Dynamic screen management
- **WebSocket Client**: Real-time bidirectional communication
- **Event-Driven**: Responsive UI updates
- **State Management**: Track game progress and player statistics

### Communication Protocol
```
Client -> Server:
  JOIN|nickname
  ANSWER|questionId|optionIndex
  CHAT|message
  START
  QUIT

Server -> Client:
  WELCOME|session|playerCount
  INFO|message
  CHAT|sender|message
  QUESTION|id|text|opt1|opt2|opt3|opt4|timeLimit
  RESULT|questionId|correctOption
  LEADERBOARD|name1,score1;name2,score2;...
  END|message
```

## 🎨 Web Interface Features

### Responsive Design
- Desktop-optimized layout
- Tablet-friendly interface
- Mobile-responsive (single column on small screens)

### Visual Effects
- Smooth animations and transitions
- Color-coded answer feedback
- Progress bars and timers
- Gradient backgrounds
- Hover effects and micro-interactions

### Accessibility
- High contrast colors
- Clear typography
- Keyboard navigation support
- Screen reader friendly

## 🛠️ Development

### Building from Source
```powershell
# Clean previous builds
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue

# Compile all server files
javac -d out/server server/*.java

# Compile all client files
javac -d out/client client/*.java
```

### Running Tests
```powershell
# Start server
java -cp out/server EnhancedQuizServer

# In separate terminals, start multiple clients
java -cp out/client QuizClient
```

### Adding New Features
1. **New Question Categories**: Add to `questions.txt` with category tag
2. **Custom Scoring**: Modify `ScoringEngine.java`
3. **UI Themes**: Edit `web/css/styles.css` CSS variables
4. **Client Features**: Extend `web/js/app.js`

## 📊 Technical Specifications

### Server
- **Language**: Java 11+
- **Concurrency**: NIO Selector, ScheduledExecutorService
- **Data Structures**: ConcurrentHashMap, CopyOnWriteArrayList
- **Protocols**: TCP, WebSocket (RFC 6455)

### Web Client
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Communication**: WebSocket API
- **Styling**: Modern CSS with CSS Grid and Flexbox
- **Fonts**: Google Fonts (Poppins)

## 🔐 Security Considerations
- Input validation on all client messages
- WebSocket handshake verification
- Nickname sanitization
- Message length limits
- Connection timeout handling

## 🚀 Future Enhancements
- [ ] User authentication and profiles
- [ ] Persistent score database
- [ ] Multiple game rooms
- [ ] Power-ups and bonuses
- [ ] Question editor interface
- [ ] Mobile app (React Native)
- [ ] Voice chat integration
- [ ] Tournament mode
- [ ] AI opponents

## 📝 License
This project is for educational purposes demonstrating network programming concepts.

## 👥 Contributing
Feel free to fork, modify, and submit pull requests!

## 📧 Support
For issues or questions, please create an issue in the repository.

---

**Built with ❤️ using Java NIO and WebSockets**
- To answer type: `answer Q0 2` (example) to send answer for question Q0 option 2.
- To chat: `chat Hello everyone`
- To quit: type `quit`

## Notes
- This project intentionally uses a single quiz room (all connected clients participate).
- Scores reset each server run.
- Questions are loaded from `questions.txt` in the root project directory.
