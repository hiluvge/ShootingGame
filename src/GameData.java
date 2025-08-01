import java.io.Serializable;
import java.util.ArrayList;

public class GameData implements Serializable {
    public static final int WIDTH = 900;
    public static final int HEIGHT = 800;
    public int p1X, p1Y;
    public int p2X, p2Y;
    public ArrayList<Bullet> p1Bullets = new ArrayList<>();
    public ArrayList<Bullet> p2Bullets = new ArrayList<>();
    public int p1Score = 0;
    public int p2Score = 0;
    public GameData() {}
    public static class Bullet implements Serializable {
        public int x, y;
        public Bullet(int x, int y) { this.x = x; this.y = y; }
    }
}
