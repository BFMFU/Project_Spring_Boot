package app.product.project.model.constants;

import java.util.*;

public class ApplicationStatusTransitions {

    // Define allowed transitions from each status
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = new HashMap<>();

    static {
        // From PENDING: can go to REVIEWING or REJECTED
        ALLOWED_TRANSITIONS.put("PENDING", new HashSet<>(Arrays.asList("REVIEWING", "REJECTED")));

        // From REVIEWING: can go to INTERVIEWING or REJECTED
        ALLOWED_TRANSITIONS.put("REVIEWING", new HashSet<>(Arrays.asList("INTERVIEWING", "REJECTED")));

        // From INTERVIEWING: can go to ACCEPTED or REJECTED
        ALLOWED_TRANSITIONS.put("INTERVIEWING", new HashSet<>(Arrays.asList("ACCEPTED", "REJECTED")));

        // From ACCEPTED: final state, cannot transition
        ALLOWED_TRANSITIONS.put("ACCEPTED", new HashSet<>());

        // From REJECTED: final state, cannot transition
        ALLOWED_TRANSITIONS.put("REJECTED", new HashSet<>());
    }

    public static boolean isValidTransition(String currentStatus, String newStatus) {
        if (!ALLOWED_TRANSITIONS.containsKey(currentStatus)) {
            return false;
        }
        return ALLOWED_TRANSITIONS.get(currentStatus).contains(newStatus);
    }

    public static Set<String> getAllowedTransitions(String status) {
        return ALLOWED_TRANSITIONS.getOrDefault(status, new HashSet<>());
    }

    public static boolean isFinalState(String status) {
        return ALLOWED_TRANSITIONS.getOrDefault(status, new HashSet<>()).isEmpty()
               && ALLOWED_TRANSITIONS.containsKey(status);
    }
}

