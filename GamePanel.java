import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Main mainFrame;
    private int level = 1;
    private int highestUnlockedLevel = 1;
    private int health = 50;
    private int collectedCount = 0;
    private int requiredCollectibles = 8;
    private int characterY = 250, characterR = 40;
    private boolean isJumping = false;
    private int jumpY = 0;
    private int jumpPeak = 180; // Higher jump
    private int jumpStep = 15;
    private boolean falling = false;
    private int levelLength = 0; // will be set dynamically
    private int playerX = 100; // player's horizontal position in the level
    private int cameraOffset = 0; // how much the world has scrolled
    private int startHealth = 50;
    private boolean blink = false;
    private int blinkTicks = 0;
    private final int BLINK_DURATION = 8; // ~160ms at 20ms per tick
    private boolean showEndUI = false;
    private boolean levelCleared = false;
    private JPanel endPanel;

    // Fields for obstacles and collectibles
    private List<Obstacle> obstacles = new ArrayList<>();
    private List<Collectible> collectibles = new ArrayList<>();
    private int obstacleSpeed = 7;

    private final int TRAIL_LENGTH = 10;
    private final int TRAIL_OFFSET = 18; // How far left each trail dot is from the character
    private final int TRAIL_SIZE = 18;   // Size of each trail dot (smaller than characterR)
    private int[] trailJumpY = new int[TRAIL_LENGTH]; // Store previous jumpY values for trail
    private java.util.LinkedList<Point> trail = new java.util.LinkedList<>();

    private BufferedImage bgImage = null;
    private int bgImageWidth = 0, bgImageHeight = 0;

    public GamePanel(Main mainFrame, int level, int highestUnlockedLevel) {
        this.mainFrame = mainFrame;
        this.level = level;
        this.highestUnlockedLevel = highestUnlockedLevel;
        setFocusable(true);
        addKeyListener(this);
        // Load background image for this level if available
        try {
            String bgPath = "images/level" + level + ".png";
            bgImage = ImageIO.read(new File(bgPath));
            bgImageWidth = bgImage.getWidth();
        } catch (Exception ex) {
            bgImage = null;
        }
        setupLevel();
        timer = new Timer(20, this);
        timer.start();
        setLayout(null);
    }

    private void setupLevel() {
        obstacles.clear();
        collectibles.clear();
        holes.clear();
        setRequiredCollectibles();
        int baseY = 250, w = 40, h = 40;
        int numObstacles, levelSeconds, speed;
        switch (level) {
            case 1: numObstacles = 14; levelSeconds = 20; speed = 7; break;
            case 2: numObstacles = 20; levelSeconds = 30; speed = 9; break;
            case 3: numObstacles = 26; levelSeconds = 40; speed = 11; break;
            case 4: numObstacles = 32; levelSeconds = 50; speed = 13; break;
            case 5: numObstacles = 38; levelSeconds = 60; speed = 15; break;
            default: numObstacles = 14; levelSeconds = 20; speed = 7; break;
        }
        obstacleSpeed = speed;
        int ticksPerSecond = 1000 / 20;
        int levelTicks = levelSeconds * ticksPerSecond;
        int levelDistance = speed * levelTicks;
        // --- Spread obstacles evenly throughout the level ---
        int obsGap = (levelDistance - 800) / (numObstacles - 1);
        int[] obsX = new int[numObstacles];
        int[] obsW = new int[numObstacles];
        int[] obsH = new int[numObstacles];
        int[] obsY = new int[numObstacles];
        for (int i = 0; i < numObstacles; i++) {
            obsX[i] = 600 + i * obsGap;
        }
        // --- Add holes for each level (only level 3 and above) ---
        holes.clear();
        int numHoles = 0;
        int holeW = 90;
        if (level >= 3) {
            switch (level) {
                case 3: numHoles = 4; break;
                case 4: numHoles = 5; break;
                case 5: numHoles = 6; break;
                default: numHoles = 0; break;
            }
        }
        int holeGap = (numHoles > 0) ? (levelLength - 1600) / (numHoles + 1) : 0;
        int holeX = 1400;
        java.util.List<Hole> tempHoles = new java.util.ArrayList<>();
        for (int i = 0; i < numHoles; i++) {
            tempHoles.add(new Hole(holeX, holeW));
            holeX += holeGap;
        }
        // --- Adjust obstacles so none overlap with holes ---
        for (int i = 0; i < numObstacles; i++) {
            int ox = obsX[i];
            boolean overlapsHole = false;
            int moveTo = ox;
            for (Hole holeObj : tempHoles) {
                if (ox + w > holeObj.x && ox < holeObj.x + holeW) {
                    overlapsHole = true;
                    moveTo = Math.max(moveTo, holeObj.x + holeW + 20);
                }
            }
            // If overlap, move obstacle to just after the hole
            if (overlapsHole) {
                obsX[i] = moveTo;
            }
        }
        // Now add obstacles
        for (int i = 0; i < numObstacles; i++) {
            // --- Unique patterns per level, all obstacles on the platform ---
            if (level == 1) {
                obsW[i] = w;
                obsH[i] = h;
                obsY[i] = baseY;
            } else if (level == 2) {
                if (i % 5 == 2) {
                    obsW[i] = w + 20;
                    obsH[i] = h + 10;
                } else {
                    obsW[i] = w;
                    obsH[i] = h;
                }
                obsY[i] = baseY;
            } else if (level == 3) {
                if (i % 6 == 3) {
                    obsW[i] = w + 40;
                    obsH[i] = h + 20;
                } else if (i % 4 == 1) {
                    obsW[i] = w + 20;
                    obsH[i] = h + 10;
                } else {
                    obsW[i] = w;
                    obsH[i] = h;
                }
                obsY[i] = baseY;
            } else if (level == 4) {
                if (i % 3 == 0) {
                    obsW[i] = w + 40;
                    obsH[i] = h + 20;
                } else if (i % 4 == 2) {
                    obsW[i] = w + 20;
                    obsH[i] = h + 10;
                } else {
                    obsW[i] = w;
                    obsH[i] = h;
                }
                obsY[i] = baseY;
            } else if (level == 5) {
                if (i % 2 == 0) {
                    obsW[i] = w + 40;
                    obsH[i] = h + 20;
                } else if (i % 3 == 0) {
                    obsW[i] = w + 20;
                    obsH[i] = h + 10;
                } else {
                    obsW[i] = w;
                    obsH[i] = h;
                }
                obsY[i] = baseY;
            }
        }
        // Always place the bottom of the obstacle at the platform (y=290)
        for (int i = 0; i < numObstacles; i++) {
            // Always place the bottom of the obstacle at the platform (y=290)
            int y = 290 - obsH[i];
            obstacles.add(new Obstacle(obsX[i], y, obsW[i], obsH[i]));
        }
        // Now add holes (after obstacle adjustment)
        holes.addAll(tempHoles);
        // --- Place collectibles: alternate high/low, never above an obstacle ---
        int cyHigh = 120, cyLow = 180, cw = 30, ch = 30;
        int colGap = (levelDistance - 1200) / (requiredCollectibles + 1);
        int[] colX = new int[requiredCollectibles];
        for (int i = 0; i < requiredCollectibles; i++) {
            colX[i] = 1200 + i * colGap;
        }
        int safeLandingGap = 180;
        for (int i = 0; i < requiredCollectibles; i++) {
            for (int j = 0; j < numObstacles; j++) {
                if (obsX[j] > colX[i] && obsX[j] - colX[i] < safeLandingGap) {
                    obsX[j] = colX[i] + safeLandingGap;
                }
            }
        }
        for (int i = 0; i < requiredCollectibles; i++) {
            int cy = (i % 2 == 0) ? cyHigh : cyLow;
            collectibles.add(new Collectible(colX[i], cy, cw, ch));
        }
        levelLength = speed * levelTicks;
        cameraOffset = 0;
        startHealth = health;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            // Loop the background image horizontally
            int y = 0;
            int scrollX = cameraOffset % bgImageWidth;
            for (int x = -scrollX; x < getWidth(); x += bgImageWidth) {
                g.drawImage(bgImage, x, y, null);
            }
        } else {
            g.setColor(Color.YELLOW); // Set background to yellow
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        // Draw ground/terrain in front of obstacles, with holes
        g.setColor(new Color(200, 200, 200));
        int lastX = 0;
        for (Hole hole : holes) {
            int screenX = hole.x - cameraOffset;
            if (screenX > getWidth()) continue;
            if (screenX > lastX) {
                g.fillRect(lastX, 290, screenX - lastX, 30);
            }
            lastX = screenX + hole.w;
        }
        if (lastX < getWidth()) {
            g.fillRect(lastX, 290, getWidth() - lastX, 30);
        }
        // Draw a dark area under the platform to separate ground from sky
        g.setColor(new Color(60, 60, 60)); // dark grey/blackish
        g.fillRect(0, 320, getWidth(), getHeight() - 320);
        // Draw vertical lines on the platform to show movement
        g.setColor(new Color(180, 180, 180));
        int lineSpacing = 60;
        for (int x = -((cameraOffset) % lineSpacing); x < getWidth(); x += lineSpacing) {
            boolean inHole = false;
            for (Hole hole : holes) {
                int hx = hole.x - cameraOffset;
                if (x >= hx && x < hx + hole.w) { inHole = true; break; }
            }
            if (!inHole) g.drawLine(x, 290, x, 320);
        }
        // Draw finish line at the end of the level
        int finishScreenX = levelLength - cameraOffset;
        if (finishScreenX > 0 && finishScreenX < getWidth()) {
            g.setColor(Color.ORANGE);
            g.fillRect(finishScreenX, 0, 10, getHeight());
        }
        // Draw obstacles (with variable size)
        for (Obstacle obs : obstacles) {
            int screenX = obs.x - cameraOffset;
            Polygon obstaclePoly = new Polygon(
                new int[]{screenX, screenX + obs.w / 2, screenX + obs.w},
                new int[]{obs.y + obs.h, obs.y, obs.y + obs.h},
                3
            );
            g.setColor(Color.RED);
            g.fillPolygon(obstaclePoly);
        }
        // Draw all collectibles at once, erase if collected
        for (Collectible c : collectibles) {
            if (!c.collected) {
                int screenX = c.x - cameraOffset;
                if (screenX > -c.w && screenX < getWidth()) {
                    g.setColor(Color.GREEN);
                    g.fillRect(screenX, c.y, c.w, c.h);
                }
            }
        }
        // Draw trail always behind the player (to the left), with vertical movement
        Graphics2D g2d = (Graphics2D) g;
        int cx = playerX;
        int cy = characterY;
        for (int i = 0; i < TRAIL_LENGTH; i++) {
            int tx = cx - (i + 1) * TRAIL_OFFSET;
            int ty = cy - trailJumpY[i] + characterR / 2 - TRAIL_SIZE / 2;
            int alpha = 120 - i * (100 / TRAIL_LENGTH);
            if (alpha < 0) alpha = 0;
            g2d.setColor(new Color(0, 0, 180, alpha));
            g2d.fillOval(tx, ty, TRAIL_SIZE, TRAIL_SIZE);
        }
        // Draw player (blink white if hit)
        if (blink) {
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.BLUE);
        }
        g.fillOval(playerX, characterY - jumpY, characterR, characterR);
        // HUD
        g.setColor(Color.BLACK);
        g.drawString("Level: " + level, 10, 20);
        g.drawString("Health: " + health, 10, 40);
        g.drawString("Collected: " + collectedCount + "/" + requiredCollectibles, 10, 60);
        if (showEndUI) {
            g.setColor(new Color(0,0,0,180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.setColor(levelCleared ? Color.GREEN : Color.RED);
            String msg = levelCleared ? "Level Cleared!" : "Level Failed";
            double percent = requiredCollectibles == 0 ? 0 : (double)collectedCount / requiredCollectibles * 100.0;
            String percentStr = String.format(" (%.0f%%)", percent);
            g.drawString(msg + percentStr, 220, 120);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.setColor(Color.WHITE);
            g.drawString("Collected: " + collectedCount + "/" + requiredCollectibles, 320, 170);
            if (!levelCleared && percent < 70.0) {
                g.setColor(Color.ORANGE);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.drawString("uh oh you collected less than the minimum (70%) of collectibles", 170, 200);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (showEndUI) return;
        // Move world (camera) forward
        cameraOffset += obstacleSpeed;
        // Handle jump
        if (isJumping) {
            if (jumpY < jumpPeak && !falling) {
                jumpY += jumpStep;
                if (jumpY >= jumpPeak) falling = true;
            } else if (falling) {
                jumpY -= jumpStep;
                if (jumpY <= 0) {
                    jumpY = 0;
                    isJumping = false;
                    falling = false;
                }
            }
        }
        Rectangle charRect = new Rectangle(playerX, characterY - jumpY, characterR, characterR);
        // Collectible collision (all at once, erase after collected)
        for (Collectible c : collectibles) {
            int screenX = c.x - cameraOffset;
            Rectangle colRect = new Rectangle(playerX, c.y, c.w, c.h);
            if (!c.collected && Math.abs(screenX - playerX) < c.w && new Rectangle(playerX, characterY - jumpY, characterR, characterR).intersects(colRect)) {
                c.collected = true;
                collectedCount++;
            }
        }
        // Obstacle collision (thinner hitbox, variable size)
        boolean hitThisFrame = false;
        for (Obstacle obs : obstacles) {
            int screenX = obs.x - cameraOffset;
            int hitboxX = screenX + obs.w / 3;
            int hitboxW = obs.w / 3;
            Rectangle thinHitbox = new Rectangle(hitboxX, obs.y, hitboxW, obs.h);
            if (charRect.intersects(thinHitbox)) {
                if (!blink) { // Only trigger blink if not already blinking
                    health -= 10;
                    blink = true;
                    blinkTicks = BLINK_DURATION;
                }
                hitThisFrame = true;
            }
        }
        // Handle blink duration
        if (blink) {
            blinkTicks--;
            if (blinkTicks <= 0) {
                blink = false;
            }
        }
        // Check for player death
        if (health <= 0) {
            health = 0;
            showEndUI = true;
            levelCleared = false;
            showEndButtons();
            repaint();
            timer.stop();
            return;
        }
        // Check for falling into a hole
        int playerFeetX = playerX;
        boolean inHole = false;
        for (Hole hole : holes) {
            int hx = hole.x - cameraOffset;
            // 25px safe zone on each side of the hole
            int hitboxStart = hx + 25;
            int hitboxEnd = hx + hole.w - 25;
            if (playerFeetX >= hitboxStart && playerFeetX < hitboxEnd) {
                inHole = true;
                break;
            }
        }
        if (inHole && jumpY == 0) { // Only if on the ground
            timer.stop();
            showEndUI = true;
            levelCleared = false;
            showEndButtons();
            repaint();
            return;
        }
        // Check for finish line
        if (cameraOffset + playerX >= levelLength) {
            timer.stop();
            double percent = (double)collectedCount / requiredCollectibles * 100.0;
            levelCleared = percent >= 70.0;
            showEndUI = true;
            if (levelCleared && level < 5 && highestUnlockedLevel < level + 1) {
                mainFrame.unlockLevel(level + 1);
            }
            showEndButtons();
            repaint();
            return;
        }
        // Update trailJumpY: shift values and add current jumpY to the front
        for (int i = TRAIL_LENGTH - 1; i > 0; i--) {
            trailJumpY[i] = trailJumpY[i - 1];
        }
        trailJumpY[0] = jumpY;
        repaint();
    }

    private void showEndButtons() {
        if (endPanel != null) remove(endPanel);
        endPanel = new JPanel(null);
        endPanel.setOpaque(false);
        endPanel.setBounds(0, 0, getWidth(), getHeight());
        JButton retryBtn = new JButton("Retry");
        retryBtn.setBounds(250, 220, 100, 40);
        retryBtn.addActionListener(_ -> restartLevel());
        endPanel.add(retryBtn);
        JButton menuBtn = new JButton("Main Menu");
        menuBtn.setBounds(370, 220, 120, 40);
        menuBtn.addActionListener(_ -> mainFrame.showMainMenu());
        endPanel.add(menuBtn);
        if (levelCleared) {
            JButton contBtn = new JButton("Continue");
            contBtn.setBounds(510, 220, 120, 40);
            contBtn.addActionListener(_ -> {
                if (level < 5) {
                    mainFrame.startGame(level + 1);
                } else {
                    mainFrame.showMainMenu();
                }
            });
            endPanel.add(contBtn);
        }
        add(endPanel);
        endPanel.repaint();
        endPanel.revalidate();
    }

    private void restartLevel() {
        showEndUI = false;
        levelCleared = false;
        if (endPanel != null) remove(endPanel);
        collectedCount = 0;
        health = startHealth;
        setupLevel();
        timer.start();
        requestFocusInWindow();
        repaint();
    }

    private void setRequiredCollectibles() {
        switch (level) {
            case 1: requiredCollectibles = 8; break;
            case 2: requiredCollectibles = 12; break;
            case 3: requiredCollectibles = 16; break;
            case 4: requiredCollectibles = 22; break;
            case 5: requiredCollectibles = 30; break;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if ((key == KeyEvent.VK_SPACE || key == KeyEvent.VK_UP) && !isJumping) {
            isJumping = true;
            falling = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            playerX += 20;
        }
        if (key == KeyEvent.VK_LEFT) {
            playerX -= 20;
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // Obstacle and Collectible inner classes
    private static class Obstacle {
        int x, y, w, h;
        Obstacle(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
    // Update Collectible class to track collection
    private static class Collectible {
        int x, y, w, h;
        boolean collected = false;
        Collectible(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
    // Add a class for holes
    private static class Hole {
        int x, w;
        Hole(int x, int w) { this.x = x; this.w = w; }
    }
    private java.util.List<Hole> holes = new java.util.ArrayList<>();
}
