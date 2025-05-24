import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    private GamePanel gamePanel;
    private MainMenuPanel mainMenuPanel;
    private LevelSelectPanel levelSelectPanel;
    private int highestUnlockedLevel = 1;

    public Main() {
        setTitle("Dino Game");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        showMainMenu();
        setVisible(true);
    }

    public void showMainMenu() {
        if (gamePanel != null) remove(gamePanel);
        if (levelSelectPanel != null) remove(levelSelectPanel);
        mainMenuPanel = new MainMenuPanel(this);
        setContentPane(mainMenuPanel);
        revalidate();
        repaint();
    }

    public void showLevelSelect() {
        if (mainMenuPanel != null) remove(mainMenuPanel);
        if (gamePanel != null) remove(gamePanel);
        levelSelectPanel = new LevelSelectPanel(this, highestUnlockedLevel);
        setContentPane(levelSelectPanel);
        revalidate();
        repaint();
    }

    public void startGame(int level) {
        if (mainMenuPanel != null) remove(mainMenuPanel);
        if (levelSelectPanel != null) remove(levelSelectPanel);
        gamePanel = new GamePanel(this, level, highestUnlockedLevel);
        setContentPane(gamePanel);
        revalidate();
        repaint();
        gamePanel.requestFocusInWindow(); // Ensure key events work
    }

    public void unlockLevel(int level) {
        if (level > highestUnlockedLevel) {
            highestUnlockedLevel = level;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}

// Main menu panel
class MainMenuPanel extends JPanel {
    public MainMenuPanel(Main mainFrame) {
        setLayout(null);
        JLabel title = new JLabel("DINO GAME", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setBounds(200, 40, 400, 60);
        add(title);
        JButton playBtn = new JButton("Play");
        playBtn.setBounds(350, 140, 100, 40);
        add(playBtn);
        JButton exitBtn = new JButton("Exit");
        exitBtn.setBounds(350, 200, 100, 40);
        add(exitBtn);
        playBtn.addActionListener(_ -> mainFrame.showLevelSelect());
        exitBtn.addActionListener(_ -> System.exit(0));
    }
}

// Level select panel
class LevelSelectPanel extends JPanel {
    public LevelSelectPanel(Main mainFrame, int highestUnlockedLevel) {
        setLayout(null);
        JLabel select = new JLabel("Select Level", SwingConstants.CENTER);
        select.setFont(new Font("Arial", Font.BOLD, 28));
        select.setBounds(250, 30, 300, 40);
        add(select);
        for (int i = 1; i <= 5; i++) {
            JButton levelBtn = new JButton("Level " + i);
            levelBtn.setBounds(180 + (i - 1) * 90, 120, 80, 40);
            levelBtn.setEnabled(i <= highestUnlockedLevel);
            int level = i;
            levelBtn.addActionListener(_ -> mainFrame.startGame(level));
            add(levelBtn);
        }
        JButton backBtn = new JButton("Main Menu");
        backBtn.setBounds(340, 220, 120, 35);
        backBtn.addActionListener(_ -> mainFrame.showMainMenu());
        add(backBtn);
    }
}
