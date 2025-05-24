import java.awt.image.BufferedImage;

public class Obstacle extends GameObject {
    public Obstacle(int x, int y, BufferedImage image) {
        super(x, y, image);
    }
    public void moveLeft() {
        x -= 5;
        if (x < -50) x = 800;
    }
}
