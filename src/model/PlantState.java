package src.model;

/**
 * Enumération représentant les différents états d'une plante.
 */
public enum PlantState {
    GRAINE,     // Step 1
    POUSSE,     // Step 2
    CROISSANCE, // Step 3 (Nouvel état intermédiaire)
    MATURE,     // Step 4
    MORT        // fstep (état d'échec)
}
