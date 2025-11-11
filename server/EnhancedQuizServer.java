import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.Base64;

/**
 * Enhanced QuizServer with WebSocket support for web clients
 * Supports both traditional socket clients and WebSocket clients
 */
public class EnhancedQuizServer {
    private final int port;
    private Selector selector;
    private ServerSocketChannel serverChannel;

    // Client state management
    private final Map<SocketChannel, ClientInfo> clients = new ConcurrentHashMap<>();
    private final Map<String, Integer> scores = new ConcurrentHashMap<>();
    private final Set<SocketChannel> webSocketClients = ConcurrentHashMap.newKeySet();

    private final EnhancedQuestionManager questionManager;
    private final ScoringEngine scoringEngine;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // Game state
    private volatile boolean quizInProgress = false;

    public EnhancedQuizServer(int port, String questionsFile) throws IOException {
        this.port = port;
        questionManager = new EnhancedQuestionManager(questionsFile);
        scoringEngine = new ScoringEngine(scores);
        init();
    }

    private void init() throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Enhanced QuizServer started on port " + port);
        System.out.println("Supports both traditional sockets and WebSocket connections");
    }

    public void start() throws IOException {
        // Setup question callbacks
        questionManager.setOnQuestionBroadcast((questionLine) -> {
            broadcast(questionLine);
        });
        
        questionManager.setOnQuestionEnd((qid) -> {
            broadcast("RESULT|" + qid + "|" + questionManager.getCorrectOption(qid));
            broadcastLeaderboard(scoringEngine.getScoresSnapshot());
        });

        // Main event loop
        while (true) {
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                
                if (!key.isValid()) continue;
                
                if (key.isAcceptable()) {
                    handleAccept(key);
                } else if (key.isReadable()) {
                    handleRead(key);
                }
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel client = ssc.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(8192));
        
        ClientInfo clientInfo = new ClientInfo();
        clients.put(client, clientInfo);
        
        // Don't send initial message yet - wait to see if it's WebSocket or regular socket
        System.out.println("New connection from: " + client.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        ClientInfo clientInfo = clients.get(client);
        
        try {
            int bytes = client.read(buffer);
            
            if (bytes == -1) {
                System.out.println("Client closed connection");
                disconnectClient(client);
                return;
            }
            
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            buffer.clear();
            
            String dataStr = new String(data, StandardCharsets.UTF_8);
            
            // Check for WebSocket handshake
            if (!clientInfo.isWebSocket && !clientInfo.handshakeComplete && dataStr.contains("Upgrade: websocket")) {
                System.out.println("Received WebSocket handshake request");
                
                // Set WebSocket flag BEFORE calling handleWebSocketHandshake
                clientInfo.isWebSocket = true;
                clientInfo.handshakeComplete = true;
                webSocketClients.add(client);
                
                handleWebSocketHandshake(client, dataStr);
                System.out.println("WebSocket client connected");
                return;
            }
            
            // If this is the first message and not WebSocket, it's a regular socket client
            if (!clientInfo.isWebSocket && !clientInfo.handshakeComplete) {
                System.out.println("Regular socket client detected");
                clientInfo.handshakeComplete = true;
                write(client, "INFO|Welcome to QuizNet! Send JOIN|nickname to enter.\n");
            }
            
            // Handle WebSocket frames
            if (clientInfo.isWebSocket) {
                System.out.println("Handling WebSocket frame, length: " + data.length);
                handleWebSocketFrame(client, data);
            } else {
                // Handle regular socket messages
                String[] lines = dataStr.split("\\r?\\n");
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    handleClientMessage(client, line.trim());
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error in handleRead: " + e.getMessage());
            e.printStackTrace();
            disconnectClient(client);
        }
    }

    private void handleWebSocketHandshake(SocketChannel client, String request) throws IOException {
        // Extract WebSocket key
        String key = null;
        String[] lines = request.split("\\r?\\n");
        
        for (String line : lines) {
            if (line.startsWith("Sec-WebSocket-Key:")) {
                key = line.substring("Sec-WebSocket-Key:".length()).trim();
                break;
            }
        }
        
        if (key == null) {
            System.err.println("No WebSocket key found in request");
            client.close();
            return;
        }
        
        // Generate accept key
        String acceptKey = generateWebSocketAccept(key);
        
        // Send handshake response
        String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                         "Upgrade: websocket\r\n" +
                         "Connection: Upgrade\r\n" +
                         "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";
        
        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
        client.write(buffer);
        
        System.out.println("WebSocket handshake completed, sending welcome message");
        
        // Send welcome message as WebSocket frame
        try {
            write(client, "INFO|Welcome to QuizNet! Send JOIN|nickname to enter.");
            System.out.println("Welcome message sent to WebSocket client");
        } catch (Exception e) {
            System.err.println("Error sending welcome message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateWebSocketAccept(String key) {
        try {
            String magic = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            String combined = key + magic;
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void handleWebSocketFrame(SocketChannel client, byte[] frame) {
        try {
            // Parse WebSocket frame
            if (frame.length < 2) return;
            
            int opcode = frame[0] & 0x0F;
            
            // Handle close frame
            if (opcode == 0x8) {
                disconnectClient(client);
                return;
            }
            
            // Handle text frame
            if (opcode == 0x1 || opcode == 0x0) {
                boolean masked = (frame[1] & 0x80) != 0;
                int payloadLength = frame[1] & 0x7F;
                int offset = 2;
                
                if (payloadLength == 126) {
                    payloadLength = ((frame[2] & 0xFF) << 8) | (frame[3] & 0xFF);
                    offset = 4;
                } else if (payloadLength == 127) {
                    offset = 10; // Skip extended payload length
                }
                
                byte[] mask = new byte[4];
                if (masked) {
                    System.arraycopy(frame, offset, mask, 0, 4);
                    offset += 4;
                }
                
                byte[] payload = new byte[payloadLength];
                if (offset + payloadLength <= frame.length) {
                    System.arraycopy(frame, offset, payload, 0, payloadLength);
                    
                    if (masked) {
                        for (int i = 0; i < payload.length; i++) {
                            payload[i] = (byte) (payload[i] ^ mask[i % 4]);
                        }
                    }
                    
                    String message = new String(payload, StandardCharsets.UTF_8);
                    handleClientMessage(client, message.trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling WebSocket frame: " + e.getMessage());
        }
    }

    private void handleClientMessage(SocketChannel client, String msg) {
        if (msg.isEmpty()) return;
        
        System.out.println("Received: " + msg);
        String[] parts = msg.split("\\|");
        String cmd = parts[0];
        ClientInfo clientInfo = clients.get(client);
        
        try {
            switch (cmd) {
                case "JOIN":
                    if (parts.length < 2) {
                        write(client, "INFO|Invalid JOIN\n");
                        break;
                    }
                    String nickname = parts[1].trim();
                    clientInfo.nickname = nickname;
                    scores.putIfAbsent(nickname, 0);
                    
                    write(client, "WELCOME|session|" + clients.size() + "\n");
                    broadcast("INFO|" + nickname + " joined. Players: " + getPlayerCount() + "\n");
                    break;
                    
                case "ANSWER":
                    if (parts.length < 3) {
                        write(client, "INFO|Invalid ANSWER\n");
                        break;
                    }
                    String qid = parts[1];
                    String option = parts[2];
                    String nick = clientInfo.nickname;
                    
                    if (nick == null) {
                        write(client, "INFO|You must JOIN first\n");
                        break;
                    }
                    
                    scoringEngine.submitAnswer(qid, nick, Integer.parseInt(option.trim()));
                    break;
                    
                case "CHAT":
                    String name = clientInfo.nickname != null ? clientInfo.nickname : "Anonymous";
                    String text = parts.length >= 2 ? parts[1] : "";
                    broadcast("CHAT|" + name + "|" + text + "\n");
                    break;
                    
                case "START":
                    if (!quizInProgress) {
                        quizInProgress = true;
                        
                        // Parse parameters: START|questionCount|category|timePerQuestion
                        int questionCount = 10;  // default
                        String category = "all";  // default
                        int timePerQuestion = 15;  // default
                        
                        System.out.println("DEBUG: Received START command with " + parts.length + " parts");
                        for (int i = 0; i < parts.length; i++) {
                            System.out.println("DEBUG: parts[" + i + "] = " + parts[i]);
                        }
                        
                        if (parts.length >= 4) {
                            try {
                                questionCount = Integer.parseInt(parts[1]);
                                category = parts[2].toLowerCase();
                                timePerQuestion = Integer.parseInt(parts[3]);
                                System.out.println("DEBUG: Parsed - questionCount=" + questionCount + ", category=" + category + ", time=" + timePerQuestion);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid START parameters, using defaults");
                            }
                        }
                        
                        final int finalQuestionCount = questionCount;
                        final String finalCategory = category;
                        final int finalTime = timePerQuestion;
                        
                        broadcast("INFO|Quiz will start in 3 seconds...\n");
                        scheduler.schedule(() -> {
                            questionManager.start(finalTime, finalCategory, "all", finalQuestionCount);
                        }, 3, TimeUnit.SECONDS);
                    }
                    break;
                    
                case "QUIT":
                    disconnectClient(client);
                    break;
                    
                default:
                    write(client, "INFO|Unknown command: " + cmd + "\n");
            }
        } catch (Exception e) {
            try {
                write(client, "INFO|Server error: " + e.getMessage() + "\n");
            } catch (Exception ignored) {}
        }
    }

    private void broadcast(String message) {
        for (Map.Entry<SocketChannel, ClientInfo> entry : clients.entrySet()) {
            try {
                write(entry.getKey(), message + "\n");
            } catch (IOException e) {
                disconnectClient(entry.getKey());
            }
        }
    }

    private void broadcastLeaderboard(Map<String, Integer> leaderboard) {
        StringBuilder sb = new StringBuilder();
        leaderboard.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .forEach(e -> sb.append(e.getKey()).append(",").append(e.getValue()).append(";"));
        
        broadcast("LEADERBOARD|" + sb.toString());
    }

    private void write(SocketChannel client, String msg) throws IOException {
        ClientInfo clientInfo = clients.get(client);
        
        if (clientInfo != null && clientInfo.isWebSocket) {
            // Send as WebSocket frame
            System.out.println("Sending WebSocket frame: " + msg);
            byte[] payload = msg.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.allocate(payload.length + 10);
            
            // FIN + text frame
            buffer.put((byte) 0x81);
            
            // Payload length
            if (payload.length <= 125) {
                buffer.put((byte) payload.length);
            } else if (payload.length <= 65535) {
                buffer.put((byte) 126);
                buffer.put((byte) (payload.length >> 8));
                buffer.put((byte) payload.length);
            }
            
            buffer.put(payload);
            buffer.flip();
            int written = client.write(buffer);
            System.out.println("Wrote " + written + " bytes to WebSocket client");
        } else {
            // Send as regular socket message
            System.out.println("Sending regular socket message: " + msg);
            ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
            client.write(buffer);
        }
    }

    private void disconnectClient(SocketChannel client) {
        ClientInfo clientInfo = clients.remove(client);
        webSocketClients.remove(client);
        
        try {
            client.close();
        } catch (IOException ignored) {}
        
        if (clientInfo != null && clientInfo.nickname != null) {
            scores.remove(clientInfo.nickname);
            broadcast("INFO|" + clientInfo.nickname + " disconnected\n");
        }
    }

    private int getPlayerCount() {
        return (int) clients.values().stream()
                .filter(info -> info.nickname != null)
                .count();
    }

    // Inner class to store client information
    private static class ClientInfo {
        String nickname;
        boolean isWebSocket = false;
        boolean handshakeComplete = false;
    }

    public static void main(String[] args) throws IOException {
        int port = 9000;
        String questionsFile = "questions.txt";
        
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        if (args.length >= 2) {
            questionsFile = args[1];
        }
        
        EnhancedQuizServer server = new EnhancedQuizServer(port, questionsFile);
        server.start();
    }
}
