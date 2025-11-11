# QuizNet — Real-Time Multiplayer Quiz System

![Java](https://img.shields.io/badge/Java-11+-orange.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

## 📋 Overview

QuizNet is a real-time multiplayer quiz platform demonstrating advanced Java network programming concepts. It features:
- ⚡ **Java NIO** server using Selector/Channels for efficient concurrent connections
- 🌐 **Dual Protocol Support** - WebSocket for web clients, TCP for Java console clients
- 🎮 **Real-time gameplay** with live leaderboard and chat
- 🎯 **Customizable quizzes** - question count, categories, and time limits
- 📱 **Modern responsive web UI** built with vanilla JavaScript

---

## 🏗️ Architecture

```
┌─────────────┐         WebSocket          ┌──────────────────┐
│ Web Browser │◄────────────────────────────┤                  │
│  (HTML/JS)  │                             │   Java NIO       │
└─────────────┘                             │   Quiz Server    │
                                            │   (Selector)     │
┌─────────────┐         TCP Socket          │                  │
│ Java Console│◄────────────────────────────┤   Port 9000      │
│   Client    │                             │                  │
└─────────────┘                             └──────────────────┘
                                                     ▲
                                                     │
                                            ┌────────┴─────────┐
                                            │  questions.txt   │
                                            │  (Question DB)   │
                                            └──────────────────┘
```

### Key Components

- **`server/`** — NIO-based server with WebSocket handshake implementation
  - `EnhancedQuizServer.java` — Main server, connection handling, message routing
  - `EnhancedQuestionManager.java` — Question selection, filtering, and scheduling
  - `ScoringEngine.java` — Score calculation and leaderboard management

- **`web/`** — Single-page web application
  - `index.html` — Responsive UI with lobby, game, and results screens
  - `css/style.css` — Modern styling with animations
  - `js/app.js` — WebSocket client, game logic, DOM updates

- **`client/`** — Optional Java console client
  - `QuizClient.java` — TCP client with command-line interface
  - `ClientListener.java` — Message handler for server responses

- **`questions.txt`** — Question database (format: `question|opt1|opt2|opt3|opt4|correct|category|difficulty`)

---

## 🎯 Features

### Player Features
- ✅ Join multiplayer quiz sessions
- ✅ Customize quiz settings (question count, category, time per question)
- ✅ Real-time question delivery with countdown timers
- ✅ Live leaderboard updates
- ✅ Chat with other players
- ✅ View final results and rankings

### Technical Features
- ✅ Non-blocking I/O with Java NIO Selector
- ✅ Custom WebSocket implementation (RFC 6455)
- ✅ Concurrent client handling with thread-safe collections
- ✅ Scheduled question timers using ScheduledExecutorService
- ✅ Category and difficulty-based question filtering
- ✅ Dynamic quiz generation

---

## 🔌 Protocol Specification

QuizNet uses a simple pipe-delimited text protocol:

### Client → Server Messages

| Command | Format | Example | Description |
|---------|--------|---------|-------------|
| JOIN | `JOIN\|nickname` | `JOIN\|Alice` | Join the quiz session |
| START | `START\|count\|category\|time` | `START\|5\|geography\|10` | Start quiz with settings |
| ANSWER | `ANSWER\|questionId\|optionIndex` | `ANSWER\|Q0\|2` | Submit answer (0-3) |
| CHAT | `CHAT\|message` | `CHAT\|Good luck!` | Send chat message |
| QUIT | `QUIT` | `QUIT` | Leave the session |

### Server → Client Messages

| Message | Format | Example | Description |
|---------|--------|---------|-------------|
| WELCOME | `WELCOME\|sessionId\|playerCount` | `WELCOME\|ABC123\|3` | Connection confirmed |
| INFO | `INFO\|message` | `INFO\|Quiz starting...` | System notification |
| QUESTION | `QUESTION\|id\|text\|opt1\|opt2\|opt3\|opt4\|time` | `QUESTION\|Q0\|Capital?\|Paris\|London\|Berlin\|Rome\|10` | Quiz question |
| RESULT | `RESULT\|questionId\|correctIndex` | `RESULT\|Q0\|2` | Correct answer reveal |
| LEADERBOARD | `LEADERBOARD\|name,score;...` | `LEADERBOARD\|Alice,100;Bob,80` | Current rankings |
| END | `END\|message` | `END\|Quiz complete!` | Quiz finished |

---

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


## 🎮 How to Play

### Web Client

1. **Join the Lobby**
   - Open `web/index.html` in your browser
   - Enter your nickname and click "Join Quiz"

2. **Configure Quiz Settings**
   - Select number of questions (1-20)
   - Choose a category (All, Science, History, Geography, etc.)
   - Set time per question (5-60 seconds)

3. **Start the Quiz**
   - Click "Start Quiz" to begin
   - Questions will appear with a countdown timer

4. **Answer Questions**
   - Click your answer before time runs out
   - See if you were correct after each question
   - Watch the leaderboard update in real-time

5. **View Results**
   - See final rankings when the quiz ends
   - Chat with other players

### Console Client

```
Commands:
  start [questions] [category] [time]  - Start quiz with settings
  answer [0-3]                         - Submit answer
  chat [message]                       - Send chat message
  quit                                 - Exit quiz
```

Example:
```
> start 5 geography 10
> answer 2
> chat Good game everyone!
```

---

## 🎓 Presentation Guide

### Slide Deck Outline

#### Slide 1: Title & Overview
- **Title:** QuizNet - Real-Time Multiplayer Quiz System
- **Subtitle:** Advanced Java Network Programming with WebSocket & NIO
- **Bullet points:**
  - Real-time multiplayer quiz platform
  - Java NIO + WebSocket implementation
  - Modern web client with responsive UI

#### Slide 2: System Architecture
- Show the architecture diagram (above)
- Explain dual protocol support (WebSocket + TCP)
- Highlight Java NIO Selector for efficient concurrency

#### Slide 3: Key Technologies
- **Java NIO (Non-blocking I/O)**
  - Single-threaded server handles multiple connections
  - `Selector` multiplexes I/O operations
  - Efficient resource usage
- **WebSocket Protocol (RFC 6455)**
  - Custom handshake implementation
  - Binary frame encoding/decoding
  - Full-duplex communication
- **Concurrent Programming**
  - `ConcurrentHashMap` for thread-safe state
  - `ScheduledExecutorService` for timers

#### Slide 4: Protocol Design
- Show the protocol table (from above)
- Explain pipe-delimited message format
- Demonstrate message flow diagram:
  ```
  Client                Server
    |------- JOIN -------->|
    |<----- WELCOME -------|
    |------ START -------->|
    |<---- QUESTION -------|
    |------ ANSWER ------->|
    |<----- RESULT --------|
    |<--- LEADERBOARD -----|
  ```

#### Slide 5: Features & Capabilities
- Real-time gameplay
- Customizable quiz settings
- Live leaderboard
- Chat functionality
- Category and difficulty filtering

#### Slide 6: Live Demo
- **Pre-demo checklist:**
  - [ ] Server compiled and ready
  - [ ] Browser windows open
  - [ ] Server terminal visible
  - [ ] Network connectivity confirmed
- **Demo steps:**
  1. Start server (show console output)
  2. Join with Player 1 (show WebSocket handshake in logs)
  3. Join with Player 2 (show player count update)
  4. Configure quiz (5 questions, Geography, 10s)
  5. Start quiz (show question broadcast in logs)
  6. Answer questions (show score updates)
  7. View final results

#### Slide 7: Code Walkthrough
Show key code snippets:

**Server Message Handling:**
```java
// EnhancedQuizServer.java
private void handleClientMessage(SocketChannel channel, String message) {
    String[] parts = message.split("\\|");
    String command = parts[0];
    
    switch (command) {
        case "JOIN":
            handleJoin(channel, parts[1]);
            break;
        case "START":
            int count = Integer.parseInt(parts[1]);
            String category = parts[2];
            int time = Integer.parseInt(parts[3]);
            startQuiz(count, category, time);
            break;
        // ...
    }
}
```

**WebSocket Handshake:**
```java
private void performWebSocketHandshake(SocketChannel channel, String request) {
    String key = extractWebSocketKey(request);
    String acceptKey = generateWebSocketAccept(key);
    
    String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                     "Upgrade: websocket\r\n" +
                     "Connection: Upgrade\r\n" +
                     "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";
    
    write(channel, response);
    clientInfo.setWebSocket(true);
}
```

#### Slide 8: Challenges & Solutions
| Challenge | Solution |
|-----------|----------|
| Concurrent client handling | Java NIO Selector pattern |
| Protocol compatibility | Dual protocol support (WS + TCP) |
| Thread safety | ConcurrentHashMap, synchronized blocks |
| Question timing | ScheduledExecutorService |
| WebSocket framing | Custom RFC 6455 implementation |

#### Slide 9: Future Enhancements
- 🔐 User authentication & profiles
- 💾 Database integration (PostgreSQL/MongoDB)
- 📊 Statistics and analytics
- 🏆 Achievement system
- 🎨 Theme customization
- 📱 Mobile app (Android/iOS)
- 🔊 Audio/video questions
- 🌍 Internationalization

#### Slide 10: Conclusion & Q&A
- **Summary:**
  - Demonstrated Java NIO and WebSocket implementation
  - Built a fully functional real-time multiplayer system
  - Applied concurrent programming best practices
- **Learning Outcomes:**
  - Network protocol design
  - Non-blocking I/O patterns
  - WebSocket implementation
  - Real-time system architecture
- **Q&A**

---

## 📝 Technical Deep Dive

### Server Architecture

#### NIO Selector Pattern
```java
Selector selector = Selector.open();
ServerSocketChannel serverChannel = ServerSocketChannel.open();
serverChannel.configureBlocking(false);
serverChannel.register(selector, SelectionKey.OP_ACCEPT);

while (running) {
    selector.select();
    Set<SelectionKey> keys = selector.selectedKeys();
    
    for (SelectionKey key : keys) {
        if (key.isAcceptable()) handleAccept(key);
        if (key.isReadable()) handleRead(key);
    }
}
```

**Benefits:**
- Single thread handles thousands of connections
- Low memory footprint
- Efficient CPU utilization
- Scalable architecture

#### WebSocket Implementation

**Handshake Process:**
1. Client sends HTTP Upgrade request
2. Server extracts `Sec-WebSocket-Key`
3. Concatenate key with magic GUID: `258EAFA5-E914-47DA-95CA-C5AB0DC85B11`
4. SHA-1 hash and Base64 encode
5. Send `101 Switching Protocols` response
6. Switch to WebSocket frame mode

**Frame Format:**
```
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-------+-+-------------+-------------------------------+
|F|R|R|R| opcode|M| Payload len |    Extended payload length    |
|I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
|N|V|V|V|       |S|             |   (if payload len==126/127)   |
| |1|2|3|       |K|             |                               |
+-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
|     Extended payload length continued, if payload len == 127  |
+ - - - - - - - - - - - - - - - +-------------------------------+
|                               |Masking-key, if MASK set to 1  |
+-------------------------------+-------------------------------+
| Masking-key (continued)       |          Payload Data         |
+-------------------------------- - - - - - - - - - - - - - - - +
:                     Payload Data continued ...                :
+ - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
|                     Payload Data continued ...                |
+---------------------------------------------------------------+
```

### Question Management

**Question Loading:**
```java
// Format: question|opt1|opt2|opt3|opt4|correctIndex|category|difficulty
private void loadQuestions() {
    try (BufferedReader reader = new BufferedReader(
            new FileReader("questions.txt"))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\|");
            if (parts.length >= 8) {
                Question q = new Question(
                    parts[0],  // text
                    Arrays.copyOfRange(parts, 1, 5),  // options
                    Integer.parseInt(parts[5]),  // correct index
                    parts[6],  // category
                    parts[7]   // difficulty
                );
                allQuestions.add(q);
            }
        }
    }
}
```

**Question Selection Algorithm:**
1. Filter by category and difficulty
2. Shuffle remaining questions
3. Select requested number (or available count if fewer)
4. Schedule broadcast with timer

### Thread Safety

**Concurrent Collections:**
```java
private final ConcurrentHashMap<SocketChannel, ClientInfo> clients = 
    new ConcurrentHashMap<>();
private final ConcurrentHashMap<String, Integer> scores = 
    new ConcurrentHashMap<>();
```

**Synchronized Access:**
```java
private synchronized void broadcast(String message) {
    for (SocketChannel client : clients.keySet()) {
        write(client, message);
    }
}
```

### Scoring System

**Score Calculation:**
- Base points: 100 per correct answer
- Time bonus: Up to 50 points based on response speed
- Formula: `score = 100 + (50 * remainingTime / totalTime)`

**Leaderboard Updates:**
- Real-time score updates after each question
- Sorted by score descending
- Broadcast to all clients simultaneously

---

## 🐛 Troubleshooting

### Server Won't Start
```
Error: Address already in use
```
**Solution:** Kill process using port 9000
```powershell
netstat -ano | findstr :9000
taskkill /PID <process_id> /F
```

### WebSocket Connection Failed
```
WebSocket connection to 'ws://localhost:9000/' failed
```
**Solution:**
1. Ensure server is running
2. Check firewall settings
3. Verify browser WebSocket support
4. Check server console for handshake errors

### Questions Not Loading
```
No questions available
```
**Solution:**
1. Verify `questions.txt` exists in project root
2. Check file format (pipe-delimited)
3. Ensure at least one question matches category/difficulty

### Client Can't Connect
**Solution:**
1. Verify server is running: `netstat -an | findstr 9000`
2. Check network connectivity
3. Verify correct IP address and port
4. Check firewall/antivirus settings

---

## 📚 Resources & References

### Java Documentation
- [Java NIO Tutorial](https://docs.oracle.com/javase/tutorial/essential/io/nio.html)
- [Selector API](https://docs.oracle.com/javase/8/docs/api/java/nio/channels/Selector.html)
- [SocketChannel API](https://docs.oracle.com/javase/8/docs/api/java/nio/channels/SocketChannel.html)

### WebSocket
- [RFC 6455 - WebSocket Protocol](https://tools.ietf.org/html/rfc6455)
- [MDN WebSocket API](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket)

### Concurrency
- [Java Concurrency in Practice](https://jcip.net/)
- [ConcurrentHashMap Guide](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentHashMap.html)

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

### Development Setup
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Make your changes
4. Test thoroughly
5. Commit: `git commit -am 'Add new feature'`
6. Push: `git push origin feature/your-feature`
7. Submit a pull request

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 👥 Authors

**QuizNet Development Team**
- Network Protocol Design
- Server Implementation (Java NIO + WebSocket)
- Web Client Development
- UI/UX Design

---

## 🙏 Acknowledgments

- Java NIO framework and documentation
- WebSocket RFC 6455 specification
- University of Moratuwa - Computer Science and Engineering
- All contributors and testers

---

## 📞 Support

For questions, issues, or feedback:
- Open an issue on GitHub
- Contact the development team
- Check the troubleshooting section above

---

**Happy Quizzing! 🎉**

