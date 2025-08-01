import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 9999;
    private static final GameData gameData = new GameData();
    private static ObjectOutputStream out1, out2;
    private static ObjectInputStream in1, in2;
    private static volatile boolean client1Active = true, client2Active = true;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Socket client1 = serverSocket.accept();
        out1 = new ObjectOutputStream(client1.getOutputStream());
        in1 = new ObjectInputStream(client1.getInputStream());
        Socket client2 = serverSocket.accept();
        out2 = new ObjectOutputStream(client2.getOutputStream());
        in2 = new ObjectInputStream(client2.getInputStream());
        Executors.newSingleThreadExecutor().execute(() -> handleInput(in1, 1));
        Executors.newSingleThreadExecutor().execute(() -> handleInput(in2, 2));
        while (client1Active || client2Active) {
            try {
                synchronized (gameData) {
                    if (client1Active) {
                        try { out1.reset(); out1.writeObject(gameData); } catch (IOException e) { client1Active = false; }
                    }
                    if (client2Active) {
                        try { out2.reset(); out2.writeObject(gameData); } catch (IOException e) { client2Active = false; }
                    }
                }
                Thread.sleep(30);
            } catch (Exception e) {}
        }
        serverSocket.close();
    }
    private static void handleInput(ObjectInputStream in, int playerNum) {
        try {
            while (true) {
                String command = (String) in.readObject();
                synchronized (gameData) {
                    int speed = 10;
                    if (playerNum == 1) {
                        switch (command) {
                            case "LEFT" -> gameData.p1X -= speed;
                            case "RIGHT" -> gameData.p1X += speed;
                            case "FIRE" -> gameData.p1Bullets.add(new GameData.Bullet(gameData.p1X + 30, gameData.p1Y));
                        }
                    } else {
                        switch (command) {
                            case "LEFT" -> gameData.p2X -= speed;
                            case "RIGHT" -> gameData.p2X += speed;
                            case "FIRE" -> gameData.p2Bullets.add(new GameData.Bullet(gameData.p2X + 30, gameData.p2Y + 60));
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (playerNum == 1) client1Active = false;
            else client2Active = false;
        }
    }
}
