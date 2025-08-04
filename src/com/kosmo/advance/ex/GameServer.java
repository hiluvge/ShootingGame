package com.kosmo.advance.ex;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {

    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            System.out.println("🎮 슈팅 게임 서버 시작! 포트 9999에서 대기 중...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("👤 클라이언트 접속: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                handler.start();
            }

        } catch (IOException e) {
            System.err.println("서버 오류: " + e.getMessage());
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("스트림 생성 실패");
            }
        }

        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("📩 받은 메시지: " + inputLine);
                    broadcast(inputLine, this);
                }
            } catch (IOException e) {
                System.out.println("❌ 연결 끊김: " + socket.getInetAddress());
            } finally {
                try {
                    socket.close();
                    clients.remove(this);
                } catch (IOException e) {}
            }
        }

        private void broadcast(String message, ClientHandler sender) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    if (client != sender) {
                        client.out.println(message);
                    }
                }
            }
        }
    }
}