import java.awt.image.BufferedImage;

public class Character extends GameObject {
    public Character(int x, int y, BufferedImage image) {
        super(x, y, image);
    }
    public void jump() {
        y -= 50;
    }
    public void resetY() {
        y = 250;
    }
    public void moveLeft() {
        x -= 20;
    }
    public void moveRight() {
        x += 20;
    }
}
