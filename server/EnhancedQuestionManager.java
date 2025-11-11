import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Enhanced QuestionManager with support for multiple categories and difficulty levels
 */
public class EnhancedQuestionManager {
    private final Map<String, List<Question>> questionsByCategory = new ConcurrentHashMap<>();
    private final List<Question> currentQuizQuestions = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private Consumer<String> broadcastCallback;
    private Consumer<String> questionEndCallback;
    
    private int currentIndex = -1;
    private int questionTimeSec = 15;
    private String currentCategory = "general";
    private String currentDifficulty = "medium";

    public EnhancedQuestionManager(String questionsFile) throws IOException {
        loadQuestions(questionsFile);
    }

    private void loadQuestions(String questionsFile) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(questionsFile));
        
        for (String line : lines) {
            if (line.trim().isEmpty() || line.startsWith("#")) continue;
            
            try {
                Question q = Question.fromLine(line);
                questionsByCategory
                    .computeIfAbsent(q.category, k -> new ArrayList<>())
                    .add(q);
            } catch (Exception e) {
                System.err.println("Error parsing question: " + line);
            }
        }
        
        System.out.println("Loaded questions:");
        questionsByCategory.forEach((cat, questions) -> 
            System.out.println("  " + cat + ": " + questions.size() + " questions")
        );
    }

    public void setOnQuestionBroadcast(Consumer<String> c) {
        this.broadcastCallback = c;
    }

    public void setOnQuestionEnd(Consumer<String> c) {
        this.questionEndCallback = c;
    }

    public void start(int perQuestionTimeSec) {
        start(perQuestionTimeSec, currentCategory, currentDifficulty, 10);
    }

    public void start(int perQuestionTimeSec, String category, String difficulty) {
        start(perQuestionTimeSec, category, difficulty, 10);
    }
    
    public void start(int perQuestionTimeSec, String category, String difficulty, int questionCount) {
        this.questionTimeSec = perQuestionTimeSec;
        this.currentCategory = category;
        this.currentDifficulty = difficulty;
        this.currentIndex = -1;
        
        // Prepare questions for this quiz
        currentQuizQuestions.clear();
        
        List<Question> availableQuestions = questionsByCategory.getOrDefault(category, new ArrayList<>());
        
        if (availableQuestions.isEmpty()) {
            // Fallback to all questions
            availableQuestions = questionsByCategory.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        }
        
        // Filter by difficulty if needed
        List<Question> filteredQuestions = availableQuestions.stream()
            .filter(q -> difficulty.equals("all") || q.difficulty.equals(difficulty))
            .collect(Collectors.toList());
        
        if (filteredQuestions.isEmpty()) {
            filteredQuestions = availableQuestions;
        }
        
        // Shuffle and limit to user-specified number
        Collections.shuffle(filteredQuestions);
        int actualCount = Math.min(filteredQuestions.size(), questionCount);
        currentQuizQuestions.addAll(filteredQuestions.subList(0, actualCount));
        
        System.out.println("Starting quiz with " + currentQuizQuestions.size() + " questions (category: " + category + ", time: " + perQuestionTimeSec + "s)");
        scheduler.schedule(this::nextQuestion, 0, TimeUnit.SECONDS);
    }

    private void nextQuestion() {
        currentIndex++;
        
        if (currentIndex >= currentQuizQuestions.size()) {
            if (broadcastCallback != null) {
                broadcastCallback.accept("END|Quiz complete!");
            }
            return;
        }
        
        Question q = currentQuizQuestions.get(currentIndex);
        String qid = "Q" + currentIndex;
        
        String out = String.format("QUESTION|%s|%s|%s|%s|%s|%s|%d",
            qid, q.text, q.options[0], q.options[1], q.options[2], q.options[3], questionTimeSec);
        
        if (broadcastCallback != null) {
            broadcastCallback.accept(out);
        }
        
        scheduler.schedule(() -> {
            if (questionEndCallback != null) {
                questionEndCallback.accept(qid);
            }
            nextQuestion();
        }, questionTimeSec, TimeUnit.SECONDS);
    }

    public int getCorrectOption(String qid) {
        try {
            int idx = Integer.parseInt(qid.substring(1));
            if (idx >= 0 && idx < currentQuizQuestions.size()) {
                return currentQuizQuestions.get(idx).correctOption;
            }
        } catch (Exception e) {
            System.err.println("Error getting correct option for " + qid);
        }
        return -1;
    }

    public Set<String> getCategories() {
        return questionsByCategory.keySet();
    }

    public int getTotalQuestions() {
        return currentQuizQuestions.size();
    }

    // Question data class
    public static class Question {
        String text;
        String[] options;
        int correctOption;
        String category;
        String difficulty;

        public Question(String text, String[] options, int correctOption, String category, String difficulty) {
            this.text = text;
            this.options = options;
            this.correctOption = correctOption;
            this.category = category;
            this.difficulty = difficulty;
        }

        /**
         * Parse question from line format:
         * question|option1|option2|option3|option4|correctIndex[|category|difficulty]
         */
        public static Question fromLine(String line) {
            String[] parts = line.split("\\|");
            
            if (parts.length < 6) {
                throw new IllegalArgumentException("Invalid question format");
            }
            
            String text = parts[0].trim();
            String[] options = new String[] {
                parts[1].trim(),
                parts[2].trim(),
                parts[3].trim(),
                parts[4].trim()
            };
            int correctOption = Integer.parseInt(parts[5].trim());
            String category = parts.length > 6 ? parts[6].trim() : "general";
            String difficulty = parts.length > 7 ? parts[7].trim() : "medium";
            
            return new Question(text, options, correctOption, category, difficulty);
        }
    }
}
