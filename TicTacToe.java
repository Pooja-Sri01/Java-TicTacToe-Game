import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class TicTacToe extends JFrame implements ActionListener {
    private final JButton[][] buttons = new JButton[3][3];
    private final JLabel statusLabel;
    private final JLabel player1Label, player2Label;
    private final JButton resetButton;
    private boolean player1Turn = true;
    private boolean gameOver = false;

    private Timer bounceTimer;
    private final ArrayList<Confetti> confettis = new ArrayList<>();
    private final ConfettiPanel confettiPanel;

    public TicTacToe() {
        setTitle("🎮 Tic Tac Toe Deluxe 🎮");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(15, 15, 25));

        // ===== HEADER =====
        JPanel headerPanel = new JPanel(new GridLayout(1, 3));
        headerPanel.setBackground(new Color(10, 10, 20));

        // Player 1 Label (Neon Cyan Glow)
        player1Label = new JLabel("⭘ PLAYER 1", SwingConstants.CENTER);
        player1Label.setForeground(Color.CYAN);
        player1Label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        player1Label.setBorder(BorderFactory.createLineBorder(Color.CYAN, 3, true));

        // Status Label (Center)
        statusLabel = new JLabel("Your Turn: Player 1", SwingConstants.CENTER);
        statusLabel.setForeground(Color.YELLOW);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        // Player 2 Label (Neon Pink Glow)
        player2Label = new JLabel("✕ PLAYER 2", SwingConstants.CENTER);
        player2Label.setForeground(Color.PINK);
        player2Label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        player2Label.setBorder(BorderFactory.createLineBorder(Color.PINK, 3, true));

        headerPanel.add(player1Label);
        headerPanel.add(statusLabel);
        headerPanel.add(player2Label);
        add(headerPanel, BorderLayout.NORTH);

        // ===== GAME GRID =====
        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 8, 8));
        gridPanel.setBackground(new Color(30, 30, 50));
        Font btnFont = new Font("Comic Sans MS", Font.BOLD, 50);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                JButton b = new JButton("");
                b.setFont(btnFont);
                b.setBackground(new Color(60, 60, 90));
                b.setForeground(Color.WHITE);
                b.setFocusPainted(false);
                b.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 160), 2, true));

                // Hover glow
                b.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        if (b.getText().equals("") && !gameOver)
                            b.setBackground(new Color(100, 100, 150));
                    }

                    public void mouseExited(MouseEvent e) {
                        if (b.getText().equals("") && !gameOver)
                            b.setBackground(new Color(60, 60, 90));
                    }
                });

                b.addActionListener(this);
                buttons[i][j] = b;
                gridPanel.add(b);
            }
        }

        // ===== Layered Grid + Confetti =====
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));

        confettiPanel = new ConfettiPanel();
        confettiPanel.setOpaque(false);

        layeredPane.add(confettiPanel, Integer.valueOf(1)); // top layer
        layeredPane.add(gridPanel, Integer.valueOf(0));     // bottom layer

        add(layeredPane, BorderLayout.CENTER);

        // ===== RESET BUTTON =====
        resetButton = new JButton("🔁 Reset Game");
        resetButton.setBackground(new Color(120, 30, 180));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetGame());
        add(resetButton, BorderLayout.SOUTH);

        startBounceAnimation();
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;
        JButton clicked = (JButton) e.getSource();
        if (!clicked.getText().equals("")) return;

        clicked.setText(player1Turn ? "X" : "O");
        clicked.setForeground(player1Turn ? Color.CYAN : Color.PINK);
        playClickSound();

        if (checkWin()) {
            statusLabel.setText((player1Turn ? "Player 1" : "Player 2") + " Wins! 🎉");
            statusLabel.setForeground(Color.GREEN);
            gameOver = true;
            triggerConfetti();
            glowWinner();
            updatePlayerGlow(player1Turn);
        } else if (isBoardFull()) {
            statusLabel.setText("It's a Draw!");
            statusLabel.setForeground(Color.ORANGE);
            gameOver = true;
            player1Label.setBorder(BorderFactory.createLineBorder(Color.CYAN, 3, true));
            player2Label.setBorder(BorderFactory.createLineBorder(Color.PINK, 3, true));
        } else {
            player1Turn = !player1Turn;
            statusLabel.setText("Your Turn: " + (player1Turn ? "Player 1" : "Player 2"));
            statusLabel.setForeground(player1Turn ? Color.CYAN : Color.PINK);
            updatePlayerGlow(player1Turn);
        }
    }

    private void playClickSound() {
        try {
            File soundFile = new File("click.wav");
            if (soundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
            }
        } catch (Exception ignored) {}
    }

    private boolean checkWin() {
        for (int i = 0; i < 3; i++) {
            if (equal(buttons[i][0], buttons[i][1], buttons[i][2])) return true;
            if (equal(buttons[0][i], buttons[1][i], buttons[2][i])) return true;
        }
        return equal(buttons[0][0], buttons[1][1], buttons[2][2]) ||
               equal(buttons[0][2], buttons[1][1], buttons[2][0]);
    }

    private boolean equal(JButton a, JButton b, JButton c) {
        return !a.getText().equals("") &&
               a.getText().equals(b.getText()) &&
               b.getText().equals(c.getText());
    }

    private boolean isBoardFull() {
        for (JButton[] row : buttons)
            for (JButton b : row)
                if (b.getText().equals("")) return false;
        return true;
    }

    private void glowWinner() {
        Timer glowTimer = new Timer(150, new ActionListener() {
            boolean glow = false;
            int times = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                for (JButton[] row : buttons)
                    for (JButton b : row)
                        if (!b.getText().equals(""))
                            b.setBackground(glow ? new Color(80, 150, 80) : new Color(60, 60, 90));
                glow = !glow;
                times++;
                if (times > 10) ((Timer) e.getSource()).stop();
            }
        });
        glowTimer.start();
    }

    private void updatePlayerGlow(boolean player1Turn) {
        if (player1Turn) {
            player1Label.setBorder(BorderFactory.createLineBorder(Color.CYAN, 4, true));
            player2Label.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2, true));
        } else {
            player2Label.setBorder(BorderFactory.createLineBorder(Color.PINK, 4, true));
            player1Label.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2, true));
        }
    }

    private void triggerConfetti() {
        Random rand = new Random();
        confettis.clear();
        for (int i = 0; i < 50; i++)
            confettis.add(new Confetti(rand.nextInt(400), 0, new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat())));

        Timer confettiTimer = new Timer(30, e -> {
            confettiPanel.updateConfetti();
            confettiPanel.repaint();
        });
        confettiTimer.start();
    }

    private void resetGame() {
        for (JButton[] row : buttons)
            for (JButton b : row) {
                b.setText("");
                b.setBackground(new Color(60, 60, 90));
            }
        player1Turn = true;
        gameOver = false;
        statusLabel.setText("Your Turn: Player 1");
        statusLabel.setForeground(Color.YELLOW);
        confettis.clear();
        confettiPanel.repaint();
        updatePlayerGlow(true);
    }

    private void startBounceAnimation() {
        bounceTimer = new Timer(100, new ActionListener() {
            int size = 18;
            boolean growing = true;

            public void actionPerformed(ActionEvent e) {
                if (gameOver) return;
                if (growing) size++;
                else size--;
                if (size > 22 || size < 18) growing = !growing;
                statusLabel.setFont(new Font("Segoe UI", Font.BOLD, size));
            }
        });
        bounceTimer.start();
    }

    // ===== Confetti Inner Class =====
    class Confetti {
        int x, y, speed;
        Color color;
        Confetti(int x, int y, Color color) {
            this.x = x; this.y = y;
            this.color = color;
            this.speed = 2 + (int)(Math.random() * 4);
        }
    }

    // ===== Custom Confetti Panel =====
    class ConfettiPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (Confetti c : confettis) {
                g.setColor(c.color);
                g.fillOval(c.x, c.y, 8, 8);
            }
        }

        void updateConfetti() {
            for (Confetti c : confettis) {
                c.y += c.speed;
                if (c.y > getHeight()) c.y = 0;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TicTacToe());
    }
}

