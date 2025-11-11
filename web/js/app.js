// QuizNet Web Client Application
class QuizNetClient {
    constructor() {
        this.ws = null;
        this.nickname = '';
        this.currentScore = 0;
        this.correctAnswers = 0;
        this.wrongAnswers = 0;
        this.streak = 0;
        this.currentQuestion = null;
        this.questionStartTime = 0;
        this.timerInterval = null;
        this.players = new Map();
        this.currentQuestionNum = 0;
        this.totalQuestions = 0;
        
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        // Login form
        document.getElementById('login-form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.connect();
        });

        // Logout
        document.getElementById('logout-btn').addEventListener('click', () => {
            this.disconnect();
            this.showScreen('login-screen');
        });

        // Start game
        document.getElementById('start-game-btn').addEventListener('click', () => {
            this.startQuiz();
        });

        // Chat form
        document.getElementById('chat-form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.sendChatMessage();
        });

        // Results buttons
        document.getElementById('play-again-btn').addEventListener('click', () => {
            this.resetGame();
            this.showScreen('lobby-screen');
        });

        document.getElementById('back-to-lobby-btn').addEventListener('submit', () => {
            this.showScreen('lobby-screen');
        });
    }

    connect() {
        const nicknameInput = document.getElementById('nickname');
        const serverInput = document.getElementById('server');
        const statusDiv = document.getElementById('connection-status');

        this.nickname = nicknameInput.value.trim();
        const serverAddr = serverInput.value.trim();

        if (!this.nickname) {
            this.showStatus('Please enter a nickname', 'error');
            return;
        }

        statusDiv.innerHTML = '<div class="status-message info">Connecting...</div>';

        try {
            // Connect to WebSocket server
            const wsUrl = `ws://${serverAddr}`;
            this.ws = new WebSocket(wsUrl);

            this.ws.onopen = () => {
                console.log('Connected to server');
                this.sendMessage(`JOIN|${this.nickname}`);
            };

            this.ws.onmessage = (event) => {
                this.handleServerMessage(event.data);
            };

            this.ws.onerror = (error) => {
                console.error('WebSocket error:', error);
                this.showStatus('Connection failed. Make sure the server is running.', 'error');
            };

            this.ws.onclose = () => {
                console.log('Disconnected from server');
                this.showStatus('Disconnected from server', 'error');
            };

        } catch (error) {
            console.error('Connection error:', error);
            this.showStatus('Failed to connect to server', 'error');
        }
    }

    disconnect() {
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        this.stopTimer();
    }

    sendMessage(message) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(message + '\n');
        }
    }

    handleServerMessage(data) {
        console.log('Received:', data);
        
        const lines = data.split('\n').filter(line => line.trim());
        
        lines.forEach(line => {
            const parts = line.split('|');
            const command = parts[0];

            switch (command) {
                case 'WELCOME':
                    this.handleWelcome(parts);
                    break;
                case 'INFO':
                    this.handleInfo(parts);
                    break;
                case 'CHAT':
                    this.handleChat(parts);
                    break;
                case 'QUESTION':
                    this.handleQuestion(parts);
                    break;
                case 'RESULT':
                    this.handleResult(parts);
                    break;
                case 'LEADERBOARD':
                    this.handleLeaderboard(parts);
                    break;
                case 'END':
                    this.handleGameEnd();
                    break;
            }
        });
    }

    handleWelcome(parts) {
        console.log('Welcome received');
        document.getElementById('user-nickname').textContent = this.nickname;
        this.showScreen('lobby-screen');
        this.showStatus('Connected successfully!', 'success');
    }

    handleInfo(parts) {
        const message = parts.slice(1).join('|');
        this.addChatMessage('System', message, true);
        
        // Update player count if message contains player info
        if (message.includes('Players:')) {
            const match = message.match(/Players: (\d+)/);
            if (match) {
                document.getElementById('player-count').textContent = match[1];
            }
        }

        // Handle quiz start countdown
        if (message.includes('Quiz will start')) {
            this.addChatMessage('System', 'Get ready! Quiz starting soon...', true);
        }
    }

    handleChat(parts) {
        const sender = parts[1];
        const message = parts.slice(2).join('|');
        this.addChatMessage(sender, message);
    }

    handleQuestion(parts) {
        // QUESTION|Q0|What is the capital of France?|Paris|London|Rome|Berlin|15
        this.currentQuestion = {
            id: parts[1],
            text: parts[2],
            options: [parts[3], parts[4], parts[5], parts[6]],
            timeLimit: parseInt(parts[7])
        };

        this.showScreen('game-screen');
        this.displayQuestion();
        this.startTimer(this.currentQuestion.timeLimit);
    }

    handleResult(parts) {
        const questionId = parts[1];
        const correctOption = parseInt(parts[2]);
        
        this.stopTimer();
        this.showCorrectAnswer(correctOption);
        
        setTimeout(() => {
            this.clearAnswerFeedback();
        }, 3000);
    }

    handleLeaderboard(parts) {
        const leaderboardData = parts[1];
        if (!leaderboardData) return;

        const entries = leaderboardData.split(';').filter(e => e.trim());
        const leaderboard = entries.map(entry => {
            const [name, score] = entry.split(',');
            return { name, score: parseInt(score) };
        });

        this.updateLeaderboard(leaderboard);

        // Update current player's score
        const playerEntry = leaderboard.find(e => e.name === this.nickname);
        if (playerEntry) {
            this.currentScore = playerEntry.score;
            document.getElementById('current-score').textContent = this.currentScore;
        }
    }

    handleGameEnd() {
        this.stopTimer();
        this.showFinalResults();
    }

    displayQuestion() {
        const questionNum = parseInt(this.currentQuestion.id.substring(1)) + 1;
        this.currentQuestionNum = questionNum;
        
        // Display "Question X of Y"
        if (this.totalQuestions > 0) {
            document.getElementById('question-number').textContent = `Question ${questionNum} of ${this.totalQuestions}`;
        } else {
            document.getElementById('question-number').textContent = `Question ${questionNum}`;
        }
        
        document.getElementById('question-text').textContent = this.currentQuestion.text;

        const optionsContainer = document.getElementById('options-container');
        optionsContainer.innerHTML = '';

        this.currentQuestion.options.forEach((option, index) => {
            const button = document.createElement('button');
            button.className = 'option-btn';
            button.textContent = `${String.fromCharCode(65 + index)}. ${option}`;
            button.dataset.index = index + 1;
            button.addEventListener('click', () => this.selectAnswer(index + 1, button));
            optionsContainer.appendChild(button);
        });

        this.questionStartTime = Date.now();
    }

    selectAnswer(optionIndex, button) {
        // Disable all buttons
        const allButtons = document.querySelectorAll('.option-btn');
        allButtons.forEach(btn => {
            btn.classList.add('disabled');
            btn.style.pointerEvents = 'none';
        });

        // Highlight selected answer
        button.classList.add('selected');

        // Send answer to server
        this.sendMessage(`ANSWER|${this.currentQuestion.id}|${optionIndex}`);
    }

    showCorrectAnswer(correctOption) {
        const allButtons = document.querySelectorAll('.option-btn');
        allButtons.forEach((btn, index) => {
            const btnIndex = parseInt(btn.dataset.index);
            if (btnIndex === correctOption) {
                btn.classList.add('correct');
            } else if (btn.classList.contains('selected')) {
                btn.classList.add('wrong');
            }
        });

        // Show feedback
        const feedback = document.getElementById('answer-feedback');
        const selectedBtn = document.querySelector('.option-btn.selected');
        
        if (selectedBtn && selectedBtn.dataset.index == correctOption) {
            feedback.textContent = 'Correct! Well done!';
            feedback.className = 'answer-feedback correct show';
            this.correctAnswers++;
            this.streak++;
        } else {
            feedback.textContent = 'Incorrect. The correct answer was ' + String.fromCharCode(64 + correctOption);
            feedback.className = 'answer-feedback wrong show';
            this.wrongAnswers++;
            this.streak = 0;
        }

        // Update stats
        document.getElementById('correct-count').textContent = this.correctAnswers;
        document.getElementById('wrong-count').textContent = this.wrongAnswers;
        document.getElementById('streak-count').textContent = this.streak;
    }

    clearAnswerFeedback() {
        const feedback = document.getElementById('answer-feedback');
        feedback.classList.remove('show');
    }

    startTimer(seconds) {
        let timeLeft = seconds;
        document.getElementById('timer').textContent = timeLeft;
        
        const progressBar = document.getElementById('time-progress');
        progressBar.style.width = '100%';

        this.timerInterval = setInterval(() => {
            timeLeft--;
            document.getElementById('timer').textContent = timeLeft;
            
            const percentage = (timeLeft / seconds) * 100;
            progressBar.style.width = percentage + '%';

            if (timeLeft <= 0) {
                this.stopTimer();
            }
        }, 1000);
    }

    stopTimer() {
        if (this.timerInterval) {
            clearInterval(this.timerInterval);
            this.timerInterval = null;
        }
    }

    updateLeaderboard(leaderboard) {
        const miniLeaderboard = document.getElementById('mini-leaderboard');
        miniLeaderboard.innerHTML = '';

        leaderboard.forEach((entry, index) => {
            const item = document.createElement('div');
            item.className = `leaderboard-item rank-${index + 1}`;
            item.innerHTML = `
                <div>
                    <span class="player-rank">${index + 1}</span>
                    <span class="player-name">${entry.name}</span>
                </div>
                <span class="player-score">${entry.score}</span>
            `;
            miniLeaderboard.appendChild(item);
        });
    }

    showFinalResults() {
        this.showScreen('results-screen');

        document.getElementById('final-score').textContent = this.currentScore;
        document.getElementById('final-correct').textContent = this.correctAnswers;
        document.getElementById('final-wrong').textContent = this.wrongAnswers;

        const total = this.correctAnswers + this.wrongAnswers;
        const accuracy = total > 0 ? Math.round((this.correctAnswers / total) * 100) : 0;
        document.getElementById('final-accuracy').textContent = accuracy + '%';

        // Copy mini leaderboard to final leaderboard
        const miniLB = document.getElementById('mini-leaderboard').innerHTML;
        document.getElementById('final-leaderboard').innerHTML = miniLB;
    }

    startQuiz() {
        // Get user-selected settings
        const questionCount = document.getElementById('question-count-select').value;
        const category = document.getElementById('category-select').value;
        const timePerQuestion = document.getElementById('time-select').value;
        
        // Store total questions for display
        this.totalQuestions = parseInt(questionCount);
        this.currentQuestionNum = 0;
        
        // Send START command with parameters: START|questionCount|category|timePerQuestion
        this.sendMessage(`START|${questionCount}|${category}|${timePerQuestion}`);
    }

    sendChatMessage() {
        const input = document.getElementById('chat-input');
        const message = input.value.trim();

        if (message) {
            this.sendMessage(`CHAT|${message}`);
            input.value = '';
        }
    }

    addChatMessage(sender, message, isSystem = false) {
        const chatMessages = document.getElementById('chat-messages');
        const messageDiv = document.createElement('div');
        messageDiv.className = 'chat-message';
        
        if (isSystem) {
            messageDiv.innerHTML = `<span class="sender" style="color: #f59e0b;">${sender}:</span> ${message}`;
        } else {
            messageDiv.innerHTML = `<span class="sender">${sender}:</span> ${message}`;
        }
        
        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    showScreen(screenId) {
        document.querySelectorAll('.screen').forEach(screen => {
            screen.classList.remove('active');
        });
        document.getElementById(screenId).classList.add('active');
    }

    showStatus(message, type) {
        const statusDiv = document.getElementById('connection-status');
        statusDiv.innerHTML = `<div class="status-message ${type}">${message}</div>`;
    }

    resetGame() {
        this.currentScore = 0;
        this.correctAnswers = 0;
        this.wrongAnswers = 0;
        this.streak = 0;
        this.currentQuestionNum = 0;
        this.totalQuestions = 0;
        
        document.getElementById('current-score').textContent = '0';
        document.getElementById('correct-count').textContent = '0';
        document.getElementById('wrong-count').textContent = '0';
        document.getElementById('streak-count').textContent = '0';
    }
}

// Initialize the application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    const app = new QuizNetClient();
    console.log('QuizNet Client initialized');
});
