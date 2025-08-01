package com.kosmo.advance.ex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class SingleShootingGame extends JPanel implements ActionListener {
    int width = 900, height = 800;
    Timer timer = new Timer(12, this);

    int playerX = width / 2 - 30;
    int playerY = height - 90;
    int playerW = 60, playerH = 60;
    int playerSpeed = 23;

    ArrayList<Bullet> bullets = new ArrayList<>();
    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<Item> items = new ArrayList<>();
    int score = 0;
    boolean running = true;

    int bulletCount = 1;
    int lives = 3;
    int stage = 1;
    int stageMsgFrame = 0;

    boolean leftPressed = false, rightPressed = false, spacePressed = false;

    public SingleShootingGame() {
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!running && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    playerX = width / 2 - 30;
                    bullets.clear();
                    enemies.clear();
                    items.clear();
                    score = 0;
                    bulletCount = 1;
                    lives = 3;
                    stage = 1;
                    stageMsgFrame = 50;
                    running = true;
                    spawnEnemy();
                    timer.start();
                    return;
                }
                if (!running) return;
                if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_SPACE) spacePressed = true;
            }
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_SPACE) spacePressed = false;
            }
        });

        timer.start();
        spawnEnemy();
    }

    void spawnEnemy() {
        Random rand = new Random();
        int x = rand.nextInt(width - 50);
        int speed = 3 + rand.nextInt(3) + stage;
        enemies.add(new Enemy(x, -50, 50, 50, speed));
    }

    void spawnItem(int x, int y) {
        items.add(new Item(x, y));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!running) return;

        if (leftPressed && playerX > 0) playerX -= playerSpeed;
        if (rightPressed && playerX < width - playerW) playerX += playerSpeed;
        if (spacePressed) {
            if (bullets.isEmpty() || bullets.get(bullets.size()-1).y < playerY - 70) {
                for (int i = 0; i < bulletCount; i++) {
                    int offset = (i - bulletCount/2) * 15;
                    bullets.add(new Bullet(playerX + playerW/2 - 3 + offset, playerY));
                }
            }
        }

        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).y -= 18;
            if (bullets.get(i).y < 0) bullets.remove(i--);
        }

        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).y += enemies.get(i).speed;
            if (enemies.get(i).y > height) {
                enemies.remove(i--);
                spawnEnemy();
            }
        }

        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            Rectangle enemyRect = new Rectangle(enemy.x, enemy.y, enemy.w, enemy.h);
            Rectangle playerRect = new Rectangle(playerX, playerY, playerW, playerH);

            if (enemyRect.intersects(playerRect)) {
                enemies.remove(i--);
                lives--;
                if (lives <= 0) {
                    running = false;
                }
                spawnEnemy();
                continue;
            }

            for (int j = 0; j < bullets.size(); j++) {
                Rectangle bulletRect = new Rectangle(bullets.get(j).x, bullets.get(j).y, 7, 18);
                if (enemyRect.intersects(bulletRect)) {
                    if (new Random().nextInt(50) == 0) { // 2% 확률
                        spawnItem(enemy.x + enemy.w / 2 - 22, enemy.y + enemy.h / 2 - 22);
                    }
                    enemies.remove(i--);
                    bullets.remove(j--);
                    score += 10;
                    spawnEnemy();
                    break;
                }
            }
        }

        for (int i = 0; i < items.size(); i++) {
            items.get(i).y += 4;
            Rectangle itemRect = new Rectangle(items.get(i).x, items.get(i).y, items.get(i).size, items.get(i).size);
            Rectangle playerRect = new Rectangle(playerX, playerY, playerW, playerH);
            if (itemRect.intersects(playerRect)) {
                items.remove(i--);
                if (bulletCount < 7) bulletCount++;
            } else if (items.get(i).y > height) {
                items.remove(i--);
            }
        }

        int nextStage = score / 20000 + 1;
        if (nextStage > stage) {
            stage = nextStage;
            stageMsgFrame = 55;
        }
        if (stageMsgFrame > 0) stageMsgFrame--;

        while (enemies.size() < stage + 2) {
            spawnEnemy();
        }
    //작업...
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.CYAN);
        g.fillRect(playerX, playerY, playerW, playerH);
        g.setColor(Color.YELLOW);
        for (Bullet b : bullets) {
            g.fillArc(b.x, b.y, 7, 18, 0, 360);
        }
        g.setColor(Color.RED);
        for (Enemy enemy : enemies) {
            g.fillRect(enemy.x, enemy.y, enemy.w, enemy.h);
        }
        g.setColor(Color.PINK);
        for (Item item : items) {
            g.fillOval(item.x, item.y, item.size, item.size);
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("굴림", Font.BOLD, 28));
        g.drawString("점수 : " + score, 18, 38);
        g.drawString("총알 수 : " + bulletCount, 180, 38);

        g.setFont(new Font("굴림", Font.BOLD, 28));
        g.setColor(Color.RED);
        for (int i = 0; i < lives; i++) {
            g.drawString("♥", 330 + i * 36, 38);
        }
        g.setColor(Color.GREEN);
        g.setFont(new Font("굴림", Font.BOLD, 36));
        if (stageMsgFrame > 0 && running) {
            g.drawString("STAGE " + stage + "!", width/2 - 90, 60);
        }
        if (!running) {
            g.setFont(new Font("굴림", Font.BOLD, 52));
            g.setColor(Color.WHITE);
            g.drawString("Game Over!", width/2-160, 340);
            g.setFont(new Font("굴림", Font.PLAIN, 22));
            g.setColor(Color.YELLOW);
            g.drawString("스페이스바로 다시 시작!", width/2-130, 410);
        }
    }

    static class Bullet {
        int x, y;
        Bullet(int x, int y) { this.x = x; this.y = y; }
    }

    static class Enemy {
        int x, y, w, h, speed;
        Enemy(int x, int y, int w, int h, int speed) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.speed = speed;
        }
    }

    static class Item {
        int x, y, size = 45;
        Item(int x, int y) { this.x = x; this.y = y; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("슈팅 게임");
            SingleShootingGame game = new SingleShootingGame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(game);
            frame.pack();
            frame.setResizable(false);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}
