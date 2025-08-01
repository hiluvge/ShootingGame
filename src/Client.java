import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client extends JPanel {
    private GameData gameData;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Client(String host, int port) throws IOException {
        setPreferredSize(new Dimension(GameData.WIDTH, GameData.HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        Socket socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                try {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) out.writeObject("LEFT");
                    else if (e.getKeyCode() == KeyEvent.VK_RIGHT) out.writeObject("RIGHT");
                    else if (e.getKeyCode() == KeyEvent.VK_SPACE) out.writeObject("FIRE");
                } catch (IOException ignored) {}
            }
        });
        new Thread(() -> {
            try {
                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof GameData d) {
                        gameData = d;
                        repaint();
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "서버와 연결이 끊어졌습니다.");
                System.exit(0);
            }
        }).start();
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameData == null) return;
        g.setColor(Color.CYAN);
        g.fillRect(gameData.p1X, gameData.p1Y, 60, 60);
        g.setColor(Color.ORANGE);
        g.fillRect(gameData.p2X, gameData.p2Y, 60, 60);
        g.setColor(Color.YELLOW);
        for (GameData.Bullet b : gameData.p1Bullets) g.fillOval(b.x, b.y, 8, 16);
        g.setColor(Color.PINK);
        for (GameData.Bullet b : gameData.p2Bullets) g.fillOval(b.x, b.y, 8, 16);
        g.setColor(Color.WHITE);
        g.setFont(new Font("굴림", Font.BOLD, 22));
        g.drawString("P1: " + gameData.p1Score, 20, 30);
        g.drawString("P2: " + gameData.p2Score, 700, 30);
    }
    public static void main(String[] args) {
        try {
            String host = JOptionPane.showInputDialog(null, "서버 IP주소", "접속", JOptionPane.QUESTION_MESSAGE);
            if (host == null || host.isBlank()) System.exit(0);
            JFrame frame = new JFrame("네트워크 슈팅게임");
            Client game = new Client(host.trim(), 9999);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(game);
            frame.pack();
            frame.setResizable(false);
            frame.setVisible(true);
            game.requestFocusInWindow();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "접속 실패\n" + e.getMessage());
        }
    }
}
