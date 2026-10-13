
package Model;

// Handles player movement when chasing the ball
public class ChaseMovementStrategy implements MovementStrategy {

    private final int speed;

    public ChaseMovementStrategy(int speed) {
        this.speed = speed;
    }

    @Override
    public void move(Player player, int targetX, int targetY) {
        player.stepToward(targetX, targetY, speed);
    }
}
