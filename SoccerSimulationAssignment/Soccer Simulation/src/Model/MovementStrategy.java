
package Model;

// Handles how players move around the pitch
// Different strategies can be used for different movement behaviours
public interface MovementStrategy {
    void move(Player player, int targetX, int targetY);
}
