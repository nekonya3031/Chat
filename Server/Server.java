import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Server {

    public static final int PORT = 8080;
    public static LinkedList<ServerSomthing> serverList = new LinkedList<>(); // список всех нитей - экземпляров
    // сервера, слушающих каждый своего клиента
    public static Story story; // история переписки
    public static Timer executor = new Timer();

    public static void main(String[] args) throws IOException {
        executor.schedule(new CommandHandler.Killer(), 1L, 1L);
        try (ServerSocket server = new ServerSocket(PORT)) {
            story = new Story();
            System.out.println("Сервер запущен");
            while (true) {
                Socket socket = server.accept();
                try {
                    ServerSomthing ss = new ServerSomthing(socket);
                    serverList.add(ss);
                } catch (IOException e) {
                    socket.close();
                    break;
                }
            }
        }
    }

    static class ServerSomthing extends Thread {

        private final Socket socket;
        private final BufferedReader in;
        public final BufferedWriter out;
        public String name;


        public ServerSomthing(Socket socket) throws IOException {
            this.socket = socket;
            // если потоку ввода/вывода приведут к генерированию искдючения, оно проброситься дальше
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            Server.story.printStory(out); // поток вывода передаётся для передачи истории последних 10
            // сооюбщений новому поключению
            start(); // вызываем run()
        }

        @Override
        public void run() {
            String word;
            try {
                // первое сообщение отправленное сюда - это никнейм
                word = in.readLine();
                String message = word + " в сети";
                for (ServerSomthing vr : Server.serverList) {
                    vr.send(message); // отослать принятое сообщение с привязанного клиента всем остальным влючая его
                }
                Server.story.addStoryEl(message);
                System.out.println(message);
                this.name = word;
                try {
                    while (true) {
                        word = in.readLine();
                        if (word.equals("disconnect")) {
                            this.downService(); // харакири
                            break; // если пришла пустая строка - выходим из цикла прослушки
                        }
                        if (word.equals("||online")) {
                            out.write(CommandHandler.getOnlineList());
                            out.flush();
                            continue;
                        }
                        message = this.name + ": " + word;
                        System.out.println(message);
                        Server.story.addStoryEl(message);
                        for (ServerSomthing vr : Server.serverList) {
                            vr.send(message);
                        }
                    }
                } catch (NullPointerException ignored) {
                }


            } catch (IOException e) {
                this.downService();
            }
        }

        public void send(String msg) {
            try {
                out.write(msg + "\n");
                out.flush();
            } catch (IOException ignored) {
            }

        }

        public void downService() {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                    in.close();
                    out.close();
                    for (ServerSomthing vr : Server.serverList) {
                        if (vr.equals(this)) vr.interrupt();
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    static class Story {

        public LinkedList<String> story = new LinkedList<>();


        public void addStoryEl(String el) {

            if (story.size() >= 20) {
                story.removeFirst();
            }
            story.add(el);
        }

        public void printStory(BufferedWriter writer) {
            if (story.size() > 0) {
                try {
                    writer.write("Последние 20 сообщений" + "\n");
                    for (String vr : story) {
                        writer.write(vr + "\n");
                    }
                    writer.write("/конец/" + "\n");
                    writer.flush();
                } catch (IOException ignored) {
                }

            }

        }
    }
}

class CommandHandler {
    public static String getOnlineList() {
        StringBuilder rtn = new StringBuilder("||online");
        for (Server.ServerSomthing vr : Server.serverList) {
            rtn.append(vr.name).append("/s");
        }
        rtn.append("\n");
        return rtn.toString();
    }

    public static void disconnectMessage(String name) {
        Server.story.addStoryEl(name + " отключился");
        for (Server.ServerSomthing vr : Server.serverList) {
            vr.send(name + " отключился" + "\n");
        }
        System.out.println(name + " отключился");
    }

    public static void onlineChecker() {
        if (Server.serverList != null) {
            ArrayList<String> disconnected = new ArrayList<>();
            ArrayList<Server.ServerSomthing> removed = new ArrayList<>();
            for (Server.ServerSomthing vr : Server.serverList) {
                try {
                    vr.out.write("||activePing");
                    vr.out.flush();
                } catch (IOException e) {
                    removed.add(vr);
                    disconnected.add(vr.name);
                    vr.downService();
                }
            }
            for (Server.ServerSomthing vr : removed) {
                Server.serverList.remove(vr);
            }
            for (String s : disconnected) {
                disconnectMessage(s);
            }
            return;
        }
    }

    static class Killer extends TimerTask {
        @Override
        public void run() {
            onlineChecker();
        }
    }
}
