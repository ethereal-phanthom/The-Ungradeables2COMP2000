
package Model;

// Handles player movement when returning to formation
public class FormationMovementStrategy implements MovementStrategy {

    @Override
    public void move(Player player, int targetX, int targetY) {
        player.idleDriftToward(targetX, targetY);
    }
}
