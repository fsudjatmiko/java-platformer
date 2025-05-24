import java.util.ArrayList;
import java.util.List;

public class LevelLayout {
    public static class ObstaclePos {
        public int x, y, w, h;
        public ObstaclePos(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
    public static class CollectiblePos {
        public int x, y, w, h;
        public CollectiblePos(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    public static List<ObstaclePos> getObstaclesForLevel(int level) {
        List<ObstaclePos> list = new ArrayList<>();
        int baseY = 250;
        int w = 40, h = 40;
        int gap = 180 - level * 10;
        int startX = 600;
        int num = Math.min(3 + level, 8);
        for (int i = 0; i < num; i++) {
            list.add(new ObstaclePos(startX + i * gap, baseY, w, h));
        }
        return list;
    }

    public static List<CollectiblePos> getCollectiblesForLevel(int level, int required) {
        List<CollectiblePos> list = new ArrayList<>();
        int y = 120;
        int w = 30, h = 30;
        int gap = 120;
        int startX = 900;
        for (int i = 0; i < required; i++) {
            list.add(new CollectiblePos(startX + i * gap, y, w, h));
        }
        return list;
    }
}
