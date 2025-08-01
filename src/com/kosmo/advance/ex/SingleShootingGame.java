package com.kosmo.advance.ex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

class CharacterSelectDialog extends JDialog {
    private int selectedIndex = -1; // 선택한 인덱스 (0~3)

    public CharacterSelectDialog(JFrame parent) {
        super(parent, "캐릭터 선택", true);
        setLayout(new FlowLayout());
        setSize(620, 200);

        String[] imgNames = {"가은이미지1.png", "지형이미지1.png", "지형이미지2.png", "혜지이미지1.png"};
        String[] captions = {"가은", "지형1", "지형2", "혜지"};

        for (int i = 0; i < imgNames.length; i++) {
            final int idx = i;
            JButton btn = new JButton("<html><center>" + captions[i] + "<br><img src='" + getClass().getResource(imgNames[i]) + "' width='90' height='90'></center></html>");
            // 또는 이미지크기 크게 하고 싶으면 아래처럼
            // JButton btn = new JButton(new ImageIcon(getClass().getResource(imgNames[i])));
            btn.setPreferredSize(new Dimension(120, 120));
            btn.addActionListener(e -> {
                selectedIndex = idx;
                setVisible(false);
            });
            add(btn);
        }
        setLocationRelativeTo(parent);
    }

    public int getSelectedIndex() { return selectedIndex; }
}






public class SingleShootingGame extends JPanel implements ActionListener {
    Image[] playerImages;
    int selectedIndex;

    int width = 900, height = 800;
    Timer timer = new Timer(10, this);
    int playerX = width / 2 - 30;
    int playerY = height - 90;
    int playerW = 50, playerH = 50;
    int playerSpeed = 8;

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

    public SingleShootingGame(Image[] playerImages, int selectedIndex) {//song 변경
        this.playerImages = playerImages;  //song 변경
        this.selectedIndex = selectedIndex; //song 변경
        setPreferredSize(new Dimension(width, height)); //song 변경
        setBackground(Color.BLACK); //song 변경
        setFocusable(true); //song 변경

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

        int nextStage = score / 500 + 1;
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

        if (playerImages != null && selectedIndex >= 0 && playerImages[selectedIndex] != null) {   //영주수정
            g.drawImage(playerImages[selectedIndex], playerX, playerY, playerW, playerH, this); //영주수정
        } else { //영주수정
            g.setColor(Color.CYAN); //영주수정
            g.fillRect(playerX, playerY, playerW, playerH); //영주수정
        }



        //g.setColor(Color.CYAN);
        //g.fillRect(playerX, playerY, playerW, playerH);
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
            String[] imgNames = {"가은이미지1.png", "지형이미지1.png", "지형이미지2.png", "혜지이미지1.png"};  //영주수정
            Image[] playerImages = new Image[imgNames.length];  //영주수정
            // ★ 이미지 배열에 미리 로딩
            for (int i = 0; i < imgNames.length; i++) {  //영주수정
                try {  //영주수정
                    playerImages[i] = new ImageIcon(SingleShootingGame.class.getResource(imgNames[i])).getImage(); //영주수정
                } catch (Exception e) {  //영주수정
                    System.out.println(imgNames[i] + " 이미지 로드 실패");  //영주수정
                }  //영주수정
            }  //영주수정

            // ★ 캐릭터 선택 다이얼로그 띄우기
            JFrame dummy = new JFrame(); //영주수정
            CharacterSelectDialog dlg = new CharacterSelectDialog(dummy);  //영주수정
            dlg.setVisible(true);  //영주수정

            int selectedIdx = dlg.getSelectedIndex();  //영주수정
            if (selectedIdx == -1) System.exit(0);  //영주수정



            JFrame frame = new JFrame("슈팅 게임");
            SingleShootingGame game = new SingleShootingGame(playerImages, selectedIdx);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(game);
            frame.pack();
            frame.setResizable(false);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}
