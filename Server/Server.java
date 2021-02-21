import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ConcurrentModificationException;
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
        try (ServerSocket server = new ServerSocket(PORT)) {
            story = new Story();
            executor.schedule(new Kicker(), (long) 0.1f);
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
        private final BufferedWriter out;
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
                this.downService();
            }

        }

        public void downService() {
            Server.story.addStoryEl(name + " отключился");
            for (ServerSomthing vr : Server.serverList) {
                if (vr.equals(this)) {
                    continue;
                }
                vr.send(name + " отключился"); // отослать принятое сообщение с привязанного клиента всем остальным влючая его
            }
            System.out.println(name + " отключился (" + this.getName() + ")");
            try {
                if (!socket.isClosed()) {
                    socket.close();
                    in.close();
                    out.close();
                    for (ServerSomthing vr : Server.serverList) {
                        if (vr.equals(this)) vr.interrupt();
                        try {
                            Server.serverList.remove(this);
                        } catch (ConcurrentModificationException ignored) {
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    static class Story {

        public LinkedList<String> story = new LinkedList<>();


        public void addStoryEl(String el) {
            // если сообщений больше 10, удаляем первое и добавляем новое
            // иначе просто добавить
            if (story.size() >= 10) {
                story.removeFirst();
                story.add(el);
            } else {
                story.add(el);
            }
        }

        public void printStory(BufferedWriter writer) {
            if (story.size() > 0) {
                try {
                    writer.write("Последние 10 сообщений" + "\n");
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

    public static class Kicker extends TimerTask {
        @Override
        public void run() {
            for (ServerSomthing vr : Server.serverList) {
                vr.send("||activePing");
            }
        }
    }
}

class CommandHandler {
    public static String getOnlineList() {
        for (Server.ServerSomthing vr : Server.serverList) {
            vr.send("||activePing");
        }
        StringBuilder rtn = new StringBuilder("||online");
        for (Server.ServerSomthing vr : Server.serverList) {
            rtn.append(vr.name).append("/s");
        }
        rtn.append("\n");
        return rtn.toString();
    }
}
