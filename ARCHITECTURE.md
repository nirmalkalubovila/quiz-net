# QuizNet Architecture Diagram

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           QuizNet System Architecture                        │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│                               CLIENT LAYER                                    │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│  ┌─────────────────────┐                    ┌──────────────────────────┐    │
│  │   Web Client        │                    │   Console Client         │    │
│  │  (HTML/CSS/JS)      │                    │   (Java)                 │    │
│  ├─────────────────────┤                    ├──────────────────────────┤    │
│  │ • index.html        │                    │ • QuizClient.java        │    │
│  │ • styles.css        │                    │ • ClientListener.java    │    │
│  │ • app.js            │                    │                          │    │
│  │                     │                    │                          │    │
│  │ Features:           │                    │ Features:                │    │
│  │ • Login Screen      │                    │ • Console I/O            │    │
│  │ • Lobby             │                    │ • Text-based interface   │    │
│  │ • Game Screen       │                    │ • Direct socket          │    │
│  │ • Results           │                    │                          │    │
│  │ • Chat System       │                    │                          │    │
│  │ • Leaderboard       │                    │                          │    │
│  └─────────────────────┘                    └──────────────────────────┘    │
│           │                                              │                   │
│           │ WebSocket                                    │ TCP Socket        │
│           │ (ws://localhost:9000)                        │ (localhost:9000)  │
└───────────┼──────────────────────────────────────────────┼───────────────────┘
            │                                              │
            ▼                                              ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                            NETWORK LAYER                                      │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│                    ┌────────────────────────────┐                            │
│                    │   Port 9000 (TCP/WebSocket)│                            │
│                    │   NIO Selector             │                            │
│                    └────────────────────────────┘                            │
│                                                                               │
└──────────────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                              SERVER LAYER                                     │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│                    ┌────────────────────────────┐                            │
│                    │  EnhancedQuizServer.java   │                            │
│                    ├────────────────────────────┤                            │
│                    │                            │                            │
│                    │ • NIO Selector/Channels    │                            │
│                    │ • WebSocket Handshake      │                            │
│                    │ • Frame Parsing            │                            │
│                    │ • Client Management        │                            │
│                    │ • Message Routing          │                            │
│                    │ • Concurrent Collections   │                            │
│                    │                            │                            │
│                    └────────────────────────────┘                            │
│                                   │                                          │
│                    ┌──────────────┴─────────────────┐                        │
│                    │                                │                        │
│                    ▼                                ▼                        │
│     ┌──────────────────────────┐    ┌────────────────────────────┐         │
│     │ EnhancedQuestionManager  │    │    ScoringEngine           │         │
│     ├──────────────────────────┤    ├────────────────────────────┤         │
│     │                          │    │                            │         │
│     │ • Load Questions         │    │ • Track Scores             │         │
│     │ • Category Management    │    │ • Validate Answers         │         │
│     │ • Difficulty Filtering   │    │ • Calculate Points         │         │
│     │ • Question Shuffling     │    │ • Time-based Scoring       │         │
│     │ • Timer Management       │    │ • Leaderboard Generation   │         │
│     │ • Broadcast Questions    │    │                            │         │
│     │                          │    │                            │         │
│     └──────────────────────────┘    └────────────────────────────┘         │
│                    │                                                         │
│                    ▼                                                         │
│          ┌─────────────────────┐                                            │
│          │  questions.txt      │                                            │
│          ├─────────────────────┤                                            │
│          │                     │                                            │
│          │ • 30+ Questions     │                                            │
│          │ • 8 Categories      │                                            │
│          │ • 3 Difficulty      │                                            │
│          │   Levels            │                                            │
│          │                     │                                            │
│          └─────────────────────┘                                            │
│                                                                               │
└──────────────────────────────────────────────────────────────────────────────┘


## Data Flow Diagram

┌─────────────┐
│ Web Client  │
└──────┬──────┘
       │ 1. JOIN|nickname
       ▼
┌────────────────────┐
│  WebSocket Handler │
└──────┬─────────────┘
       │ 2. Parse WebSocket Frame
       ▼
┌───────────────────┐
│  Message Router   │
└──────┬────────────┘
       │ 3. Route to Handler
       ▼
┌──────────────────────┐
│  Command Handlers    │
│  • JOIN → Add Client │
│  • ANSWER → Score    │
│  • CHAT → Broadcast  │
│  • START → Quiz      │
└──────┬───────────────┘
       │ 4. Process
       ▼
┌─────────────────────────┐
│   State Update          │
│   • Client Map          │
│   • Score Map           │
│   • Leaderboard         │
└──────┬──────────────────┘
       │ 5. Broadcast Response
       ▼
┌──────────────────────────┐
│  WebSocket Frame Encoder │
└──────┬───────────────────┘
       │ 6. Send to Client(s)
       ▼
┌─────────────┐
│ Web Client  │
│ • Update UI │
│ • Animate   │
└─────────────┘


## WebSocket Handshake Flow

Client                          Server
  │                               │
  │  HTTP Upgrade Request         │
  ├──────────────────────────────>│
  │  Sec-WebSocket-Key: xxx       │
  │                               │
  │                               │ Generate Accept Key
  │                               │ SHA-1(key + magic)
  │                               │
  │  HTTP 101 Switching Protocols │
  │<──────────────────────────────┤
  │  Sec-WebSocket-Accept: yyy    │
  │                               │
  │  WebSocket Connection Open    │
  │<═════════════════════════════>│
  │                               │
  │  Text Frame: JOIN|Alice       │
  ├──────────────────────────────>│
  │                               │
  │  Text Frame: WELCOME|...      │
  │<──────────────────────────────┤
  │                               │


## Game State Flow

┌──────────────┐
│  LOBBY       │ ← Players join, chat, configure
└──────┬───────┘
       │ START command
       ▼
┌──────────────┐
│  COUNTDOWN   │ ← 3 second countdown
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  QUESTION 1  │ ← Question broadcast
└──────┬───────┘
       │ Players answer
       │ Timer expires
       ▼
┌──────────────┐
│  RESULT 1    │ ← Show correct answer
└──────┬───────┘   Update scores
       │
       ▼
┌──────────────┐
│  QUESTION 2  │ ← Next question
└──────┬───────┘
       │
       ⋮  (Repeat for all questions)
       │
       ▼
┌──────────────┐
│  FINAL       │ ← Quiz complete
│  RESULTS     │   Show leaderboard
└──────────────┘


## Message Protocol Format

┌────────────────────────────────────────────────────────────┐
│                    Client → Server                         │
├────────────────────────────────────────────────────────────┤
│ JOIN|<nickname>                                            │
│ ANSWER|<questionId>|<optionIndex>                          │
│ CHAT|<message>                                             │
│ START                                                      │
│ QUIT                                                       │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                    Server → Client                         │
├────────────────────────────────────────────────────────────┤
│ WELCOME|session|<playerCount>                              │
│ INFO|<message>                                             │
│ CHAT|<sender>|<message>                                    │
│ QUESTION|<id>|<text>|<opt1>|<opt2>|<opt3>|<opt4>|<time>   │
│ RESULT|<questionId>|<correctOption>                        │
│ LEADERBOARD|<name1>,<score1>;<name2>,<score2>;...         │
│ END|<message>                                              │
└────────────────────────────────────────────────────────────┘


## Concurrency Model

┌─────────────────────────────────────────────────────┐
│               Main Thread (NIO Selector)            │
│  • Accept connections                               │
│  • Read from channels                               │
│  • Parse messages                                   │
│  • Write to channels                                │
└─────────────────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│  Scheduler   │ │  Scheduler   │ │   Thread     │
│   Thread 1   │ │   Thread 2   │ │    Pool      │
│              │ │              │ │              │
│ • Question   │ │ • Result     │ │ • Message    │
│   Broadcast  │ │   Timing     │ │   Processing │
│ • Countdown  │ │ • Cleanup    │ │              │
└──────────────┘ └──────────────┘ └──────────────┘

  All sharing:
  • ConcurrentHashMap (clients, scores)
  • CopyOnWriteArrayList (questions)
  • Thread-safe collections


## Storage & Data Structures

┌──────────────────────────────────────────────────────┐
│           Server Memory (Concurrent)                 │
├──────────────────────────────────────────────────────┤
│                                                      │
│  clients: Map<SocketChannel, ClientInfo>            │
│    ├─ SocketChannel → ClientInfo                    │
│    └─ ClientInfo { nickname, isWebSocket, ... }     │
│                                                      │
│  scores: Map<String, Integer>                       │
│    └─ nickname → score                              │
│                                                      │
│  webSocketClients: Set<SocketChannel>               │
│    └─ All WebSocket connections                     │
│                                                      │
│  questionsByCategory: Map<String, List<Question>>   │
│    ├─ "science" → [Q1, Q2, Q3, ...]                │
│    ├─ "history" → [Q1, Q2, Q3, ...]                │
│    └─ ...                                           │
│                                                      │
│  currentQuizQuestions: List<Question>               │
│    └─ Shuffled questions for current quiz           │
│                                                      │
└──────────────────────────────────────────────────────┘


## Deployment Architecture

┌────────────────────────────────────────────────────────┐
│                  Development Setup                     │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Localhost (127.0.0.1)                                │
│  ├─ Server: port 9000                                 │
│  └─ Multiple Clients: WebSocket/Socket                │
│                                                        │
└────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────┐
│                    LAN Setup                           │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Server Machine (192.168.1.100)                       │
│  ├─ Port 9000 (open in firewall)                      │
│  └─ QuizNet Server running                            │
│                                                        │
│  Client Machines (192.168.1.x)                        │
│  └─ Connect to: ws://192.168.1.100:9000              │
│                                                        │
└────────────────────────────────────────────────────────┘


## Technology Stack

┌──────────────────────────────────────────────┐
│              Frontend Stack                   │
├──────────────────────────────────────────────┤
│ • HTML5                                      │
│ • CSS3 (Grid, Flexbox, Animations)          │
│ • JavaScript ES6+ (Classes, Async)          │
│ • WebSocket API                              │
│ • No frameworks (Vanilla JS)                │
└──────────────────────────────────────────────┘

┌──────────────────────────────────────────────┐
│              Backend Stack                    │
├──────────────────────────────────────────────┤
│ • Java 11+                                   │
│ • NIO (Non-blocking I/O)                    │
│ • Selector Pattern                           │
│ • WebSocket Protocol (RFC 6455)             │
│ • Concurrent Collections                     │
│ • ScheduledExecutorService                   │
└──────────────────────────────────────────────┘

┌──────────────────────────────────────────────┐
│              DevOps Tools                     │
├──────────────────────────────────────────────┤
│ • PowerShell (build automation)              │
│ • javac/java (compilation/runtime)          │
│ • Git (version control)                      │
└──────────────────────────────────────────────┘
