import java.awt.image.BufferedImage;

public class GameObject {
    protected int x, y;
    protected BufferedImage image;

    public GameObject(int x, int y, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.image = image;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public BufferedImage getImage() { return image; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}
