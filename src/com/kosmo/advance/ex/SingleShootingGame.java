package com.kosmo.advance.ex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class SingleShootingGame extends JPanel implements ActionListener {
    int width = 1100, height = 850;
    Timer timer = new Timer(12, this);

    int playerX = width / 2 - 30;
    int playerY = height - 90;
    int playerW = 60, playerH = 60;
    int playerSpeed = 11;
    ArrayList<Bullet> bullets = new ArrayList<>();
    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<Item> items = new ArrayList<>();
    ArrayList<BossBullet> bossBullets = new ArrayList<>();
    Boss boss = null;
    int score = 0;
    boolean running = true;

    int bulletCount = 1;
    int lives = 3;
    int stage = 1;
    int stageMsgFrame = 0;
    boolean bossStage = false;
    int bossShootTick = 0;
    int bossShootInterval = 60;
    int shootCooldown = 0;

    boolean leftPressed = false, rightPressed = false, spacePressed = false;
    boolean upPressed = false, downPressed = false;

    public SingleShootingGame() {
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!running && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    playerX = width / 2 - 30;
                    playerY = height - 90;
                    bullets.clear();
                    enemies.clear();
                    items.clear();
                    bossBullets.clear();
                    boss = null;
                    score = 0;
                    bulletCount = 1;
                    lives = 3;
                    stage = 1;
                    stageMsgFrame = 50;
                    bossStage = false;
                    bossShootTick = 0;
                    bossShootInterval = 60;
                    shootCooldown = 0;
                    running = true;
                    spawnEnemy();
                    timer.start();
                    return;
                }
                if (!running) return;
                if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_SPACE) spacePressed = true;
            }
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_SPACE) spacePressed = false;
            }
        });

        timer.start();
        spawnEnemy();
    }

    void spawnEnemy() {
        Random rand = new Random();
        int x = rand.nextInt(width - 50);

        int maxHp = 10;
        if (stage >= 21) maxHp = 50;
        else if (stage >= 16) maxHp = 40;
        else if (stage >= 11) maxHp = 30;
        else if (stage >= 6) maxHp = 20;
        int hp = 1 + rand.nextInt(maxHp); // 1~maxHp
        int speed = 3 + rand.nextInt(3) + stage / 2;
        enemies.add(new Enemy(x, -50, 50, 50, speed, hp, hp));
    }

    void spawnItem(int x, int y) {
        items.add(new Item(x, y));
    }

    void spawnBoss() {
        int bossHp = 60 + (stage - 1) * 40;
        int bossSpeed = 7 + stage;
        int bossW = 150 + 20 * stage;
        int bossH = 120 + 12 * stage;
        int bossBulletSpeed = 18 + stage * 3;
        boss = new Boss(width / 2 - bossW / 2, 60, bossW, bossH, bossSpeed, bossHp, bossBulletSpeed);
        bossStage = true;
        boss.dir = (new Random().nextBoolean() ? 1 : -1);
        bossShootTick = 0;
        bossShootInterval = 30 + new Random().nextInt(91);
        stageMsgFrame = 80;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!running) return;

        if (leftPressed && playerX > 0) playerX -= playerSpeed;
        if (rightPressed && playerX < width - playerW) playerX += playerSpeed;
        if (upPressed && playerY > 0) playerY -= playerSpeed;
        if (downPressed && playerY < height - playerH) playerY += playerSpeed;

        if (spacePressed) {
            if (shootCooldown == 0) {
                for (int i = 0; i < bulletCount; i++) {
                    int offset = (i - bulletCount / 2) * 15;
                    bullets.add(new Bullet(playerX + playerW / 2 - 3 + offset, playerY));
                }
                shootCooldown = 8;
            }
        }
        if (shootCooldown > 0) shootCooldown--;

        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).y -= 18;
            if (bullets.get(i).y < 0) bullets.remove(i--);
        }

        if (!bossStage) {
            for (int i = 0; i < enemies.size(); i++) {
                Enemy e2 = enemies.get(i);
                e2.y += e2.speed;
                if (e2.y > height) {
                    enemies.remove(i--);
                    spawnEnemy();
                    continue;
                }

                Rectangle enemyRect = new Rectangle(e2.x, e2.y, e2.w, e2.h);
                Rectangle playerRect = new Rectangle(playerX, playerY, playerW, playerH);

                if (enemyRect.intersects(playerRect)) {
                    enemies.remove(i--);
                    lives--;
                    if (lives <= 0) running = false;
                    spawnEnemy();
                    continue;
                }

                for (int j = 0; j < bullets.size(); j++) {
                    Rectangle bulletRect = new Rectangle(bullets.get(j).x, bullets.get(j).y, 7, 18);
                    if (enemyRect.intersects(bulletRect)) {
                        e2.hp--;
                        bullets.remove(j--);
                        if (e2.hp <= 0) {
                            if (new Random().nextInt(200) == 0) {
                                spawnItem(e2.x + e2.w / 2 - 22, e2.y + e2.h / 2 - 22);
                            }
                            score += 10 * stage * e2.initHp;
                            enemies.remove(i--);
                            spawnEnemy();
                        }
                        break;
                    }
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

        if (bossStage) {
            if (boss != null) {
                boss.x += boss.speed * boss.dir;
                if (boss.x < 0 || boss.x + boss.w > width) boss.dir *= -1;

                bossShootTick++;
                if (bossShootTick >= bossShootInterval) {
                    bossShootTick = 0;
                    bossShootInterval = 30 + new Random().nextInt(91);
                    bossBullets.add(new BossBullet(boss.x + boss.w / 2 - 6, boss.y + boss.h, 16, 36, boss.bulletSpeed));
                }

                Rectangle bossRect = new Rectangle(boss.x, boss.y, boss.w, boss.h);
                Rectangle playerRect = new Rectangle(playerX, playerY, playerW, playerH);
                if (bossRect.intersects(playerRect)) {
                    lives--;
                    if (lives <= 0) running = false;
                }
                for (int j = 0; j < bullets.size(); j++) {
                    Rectangle bulletRect = new Rectangle(bullets.get(j).x, bullets.get(j).y, 7, 18);
                    if (bossRect.intersects(bulletRect)) {
                        boss.hp--;
                        bullets.remove(j--);
                        if (boss.hp <= 0) {
                            boss = null;
                            bossStage = false;
                            bossBullets.clear();
                            stage++;
                            bulletCount = 1;
                            stageMsgFrame = 70;
                            break;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < bossBullets.size(); i++) {
            BossBullet bb = bossBullets.get(i);
            bb.y += bb.speed;
            Rectangle bbRect = new Rectangle(bb.x, bb.y, bb.w, bb.h);
            Rectangle playerRect = new Rectangle(playerX, playerY, playerW, playerH);
            if (bbRect.intersects(playerRect)) {
                bossBullets.remove(i--);
                lives--;
                if (lives <= 0) running = false;
            } else if (bb.y > height) {
                bossBullets.remove(i--);
            }
        }

        int nextBossScore = (stage) * 3000;
        if (score >= nextBossScore && !bossStage && boss == null) {
            enemies.clear();
            bossBullets.clear();
            spawnBoss();
        }
        if (!bossStage) {
            while (enemies.size() < stage + 2) {
                spawnEnemy();
            }
        }
        if (stageMsgFrame > 0) stageMsgFrame--;
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
        for (Enemy e2 : enemies) {
            if (e2.initHp >= 30) g.setColor(Color.RED);
            else if (e2.initHp >= 20) g.setColor(Color.YELLOW);
            else if (e2.initHp >= 10) g.setColor(Color.ORANGE);
            else g.setColor(Color.BLUE);
            g.fillRect(e2.x, e2.y, e2.w, e2.h);
            if (e2.initHp >= 30) g.setColor(Color.WHITE);
            else g.setColor(Color.BLACK);
            g.setFont(new Font("굴림", Font.BOLD, 16));
            g.drawString("HP:" + e2.hp, e2.x + e2.w / 2 - 16, e2.y + e2.h / 2);
        }
        g.setColor(Color.PINK);
        for (Item item : items) {
            g.fillOval(item.x, item.y, item.size, item.size);
        }
        if (bossStage && boss != null) {
            g.setColor(Color.MAGENTA);
            g.fillRect(boss.x, boss.y, boss.w, boss.h);
            g.setColor(Color.WHITE);
            g.setFont(new Font("굴림", Font.BOLD, 30));
            g.drawString("BOSS", boss.x + boss.w / 2 - 44, boss.y + boss.h / 2 - 10);
            g.setFont(new Font("굴림", Font.BOLD, 22));
            g.drawString("HP: " + boss.hp, boss.x + boss.w / 2 - 34, boss.y + boss.h / 2 + 24);
        }
        g.setColor(Color.ORANGE);
        for (BossBullet bb : bossBullets) {
            g.fillOval(bb.x, bb.y, bb.w, bb.h);
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
            if (bossStage && boss != null) {
                g.setColor(Color.MAGENTA);
                g.drawString("STAGE " + stage + " BOSS!", width / 2 - 150, 90);
            } else {
                g.setColor(Color.GREEN);
                g.drawString("STAGE " + stage + "!", width / 2 - 90, 60);
            }
        }
        if (!running) {
            g.setFont(new Font("굴림", Font.BOLD, 52));
            g.setColor(Color.WHITE);
            g.drawString("Game Over!", width / 2 - 160, 340);
            g.setFont(new Font("굴림", Font.PLAIN, 22));
            g.setColor(Color.YELLOW);
            g.drawString("스페이스바로 다시 시작!", width / 2 - 130, 410);
        }
    }

    static class Bullet {
        int x, y;
        Bullet(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Enemy {
        int x, y, w, h, speed, hp, initHp;
        Enemy(int x, int y, int w, int h, int speed, int hp, int initHp) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.speed = speed;
            this.hp = hp;
            this.initHp = initHp;
        }
    }

    static class Boss {
        int x, y, w, h, speed, hp, dir = 1, bulletSpeed;
        Boss(int x, int y, int w, int h, int speed, int hp, int bulletSpeed) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.speed = speed;
            this.hp = hp;
            this.bulletSpeed = bulletSpeed;
        }
    }

    static class Item {
        int x, y, size = 45;
        Item(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class BossBullet {
        int x, y, w, h, speed;
        BossBullet(int x, int y, int w, int h, int speed) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.speed = speed;
        }
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
