package astronomicalsystem.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servizio REST per interrogare il database JPL Horizons della NASA.
 * Recupera le effemeridi (posizioni e velocità) in tempo reale.
 */
public class NasaHorizonsClient {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    
    // Pattern Regex per estrarre X, Y, Z e VX, VY, VZ dal testo della NASA
    private static final Pattern POSITION_PATTERN = Pattern.compile("X =\\s*([-+0-9.E]+)\\s*Y =\\s*([-+0-9.E]+)\\s*Z =\\s*([-+0-9.E]+)");
    private static final Pattern VELOCITY_PATTERN = Pattern.compile("VX=\\s*([-+0-9.E]+)\\s*VY=\\s*([-+0-9.E]+)\\s*VZ=\\s*([-+0-9.E]+)");

    /**
     * Interroga la NASA per ottenere i dati di un corpo celeste nella data di oggi.
     *
     * @param bodyId L'identificativo NASA (es. "399" per la Terra, "499" per Marte)
     * @return un oggetto NasaState con i dati, oppure null se fallisce
     */
    public NasaState fetchTodayState(String bodyId) {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);

            // Costruiamo l'URL della NASA (Centro @10 è il Sole)
            String urlString = String.format(
                "https://ssd.jpl.nasa.gov/api/horizons.api?format=text&COMMAND='%s'&CENTER='@10'&MAKE_EPHEM='YES'&EPHEM_TYPE='VECTORS'&START_TIME='%s'&STOP_TIME='%s'&STEP_SIZE='1d'",
                bodyId, today, tomorrow
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            // Estraiamo i dati con le Regex
            Matcher posMatcher = POSITION_PATTERN.matcher(responseBody);
            Matcher velMatcher = VELOCITY_PATTERN.matcher(responseBody);

            if (posMatcher.find() && velMatcher.find()) {
                double x = Double.parseDouble(posMatcher.group(1));
                double y = Double.parseDouble(posMatcher.group(2));
                double z = Double.parseDouble(posMatcher.group(3));
                
                double vx = Double.parseDouble(velMatcher.group(1));
                double vy = Double.parseDouble(velMatcher.group(2));
                double vz = Double.parseDouble(velMatcher.group(3));

                return new NasaState(x, y, z, vx, vy, vz);
            } else {
                System.err.println("Impossibile trovare i vettori nella risposta NASA per l'ID: " + bodyId);
            }

        } catch (Exception e) {
            System.err.println("Errore di connessione alla NASA: " + e.getMessage());
        }
        return null;
    }

    // Un piccolo main integrato solo per testare la connessione al volo!
    public static void main(String[] args) {
        NasaHorizonsClient nasa = new NasaHorizonsClient();
        System.out.println("Contattando Houston...");
        
        // 399 è l'ID della Terra
        NasaState earth = nasa.fetchTodayState("399"); 
        
        if (earth != null) {
            System.out.println("Dati Terra ricevuti con successo!");
            System.out.println("Posizione (Km): X=" + earth.x() + ", Y=" + earth.y() + ", Z=" + earth.z());
            System.out.println("Velocità (Km/s): VX=" + earth.vx() + ", VY=" + earth.vy() + ", VZ=" + earth.vz());
        }
    }
}