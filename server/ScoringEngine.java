import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScoringEngine {
    private final Map<String, Integer> scores;
    private final Map<String, Set<String>> answered = new ConcurrentHashMap<>();

    public ScoringEngine(Map<String, Integer> scores) {
        this.scores = scores;
    }

    /**
     * Submit an answer for a question.
     *
     * @param qid           Question ID
     * @param nickname      Player nickname
     * @param answerOption  Player's chosen option index
     * @param correctOption Correct option index for the question
     */
    public void submitAnswer(String qid, String nickname, int answerOption, int correctOption) {
        answered.putIfAbsent(qid, ConcurrentHashMap.newKeySet());
        Set<String> s = answered.get(qid);

        synchronized (s) {
            // Prevent multiple submissions for the same question
            if (s.contains(nickname))
                return;
            s.add(nickname);
        }

        // Only award points if the answer is correct
        if (answerOption == correctOption) {
            scores.put(nickname, scores.getOrDefault(nickname, 0) + 1);
        }
    }

    public Map<String, Integer> getScoresSnapshot() {
        return new HashMap<>(scores);
    }
}
