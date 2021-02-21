import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client {
    public static String ipAddr = "shizashizashiza.ml";
    public static int port = 8080;
    public static ClientSomthing clientSomthing;

    public static void main(String[] args) {
        clientSomthing = new ClientSomthing(ipAddr, port);
    }

    public static void send(String s) {
        clientSomthing.send(s);
    }
}

class ClientSomthing {
    public static Grapfics g = new Grapfics();
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private BufferedReader inputUser;
    public static ArrayList<String> online = new ArrayList<>();

    public void send(String s) {
        try {
            out.write(s + "\n");
            out.flush();
        } catch (IOException ignored) {

        }
    }

    public ClientSomthing(String addr, int port) {
        try {
            this.socket = new Socket(addr, port);
        } catch (IOException e) {
            System.err.println("Socket failed");
        }
        try {
            inputUser = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.pressNickname();
            new ReadMsg().start();
            new WriteMsg().start();
            out.write("||online" + "\n");
            out.flush();
        } catch (IOException e) {
            ClientSomthing.this.downService();
        }
    }

    public static void log(String s) {
        System.out.println(s);
        g.in(s + "\n");
    }


    private void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void pressNickname() {
        try {
            String nickname = g.loginDialog();
            out.write(nickname + "\n");
            out.flush();
        } catch (IOException ignored) {
        }
    }

    public static class Handler {
        public static void handle(String message) {
            if (message.startsWith("||online")) {
                online(message);
            }
        }

        public static void online(String message) {
            message = message.substring(8);
            ArrayList<String> rtn = new ArrayList<>();
            for (String s : message.split("/s")) {
                rtn.add(s);
            }
            online = rtn;
            g.online();
        }
    }

    public class WriteMsg extends Thread {

        @Override
        public void run() {
            while (true) {
                String userWord;
                try {
                    userWord = inputUser.readLine();
                    if (userWord.equals("disconnect")) {
                        out.write("disconnect" + "\n");
                        ClientSomthing.this.downService();
                        break;
                    } else {
                        out.write(userWord + "\n");
                    }
                    out.flush();
                } catch (IOException e) {
                    ClientSomthing.this.downService();

                }

            }
        }
    }

    private class ReadMsg extends Thread {
        @Override
        public void run() {

            String str;
            try {
                while (true) {
                    str = in.readLine();
                    if (str.equals("stop")) {
                        ClientSomthing.this.downService();
                        break;
                    }
                    if (str.startsWith("||")) {
                        Handler.handle(str);
                        continue;
                    }
                    log(str);
                }
            } catch (IOException e) {
                ClientSomthing.this.downService();
            }
        }
    }
}


