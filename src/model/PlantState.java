package src.model;

/**
 * Enumération représentant les différents états d'une plante.
 */
public enum PlantState {
    GRAINE,      // Vient d'être planté
    POUSSE,      // En cours de croissance
    MATURE,      // Prête à récolter
    POURRIE,     // Périmée (trop attendu)
    MORT         // Morte de soif
}