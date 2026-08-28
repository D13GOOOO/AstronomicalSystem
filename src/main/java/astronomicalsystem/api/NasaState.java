package astronomicalsystem.api;

/**
 * Un contenitore di dati (Data Transfer Object) che rappresenta lo stato 
 * fisico crudo restituito dai server della NASA.
 */
public record NasaState(
    double x, double y, double z,
    double vx, double vy, double vz
) {}