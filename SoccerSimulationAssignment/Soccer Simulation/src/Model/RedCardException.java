package Model;

public class RedCardException extends Exception {

    private final int jerseyNumber;

    public RedCardException(int jerseyNumber, String reason) {
        super("Red Card! Player #" + jerseyNumber + " sent off: " + reason);
        this.jerseyNumber = jerseyNumber;
    }

    public int getJerseyNumber() {
        return jerseyNumber;
    }
}