import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Server {

    public static final int PORT = 8080;
    public static LinkedList<ServerSomthing> serverList = new LinkedList<>(); // список всех нитей - экземпляров
    // сервера, слушающих каждый своего клиента
    public static Story story; // история переписки

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        story = new Story();
        System.out.println("Сервер запущен");
        try {
            while (true) {
                Socket socket = server.accept();
                try {
                    ServerSomthing ss = new ServerSomthing(socket);
                    serverList.add(ss);
                } catch (IOException e) {
                    socket.close();
                }
            }
        } finally {
            server.close();
        }
    }


    static class ServerSomthing extends Thread {

        private Socket socket;
        private BufferedReader in;
        private BufferedWriter out;
        private String name;


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
                    for (ServerSomthing vr : Server.serverList) {

                        vr.send(word.substring(6)+ " подключился"); // отослать принятое сообщение с привязанного клиента всем остальным влючая его
                    }
                    Server.story.addStoryEl(word.substring(6)+ " подключился");
                    System.out.println(word.substring(6)+ " подключился ("+this.getName()+")");
                    this.name=word.substring(6);
                try {
                    while (true) {
                        word = in.readLine();
                        if (word.equals("stop")) {
                            this.downService(); // харакири
                            break; // если пришла пустая строка - выходим из цикла прослушки
                        }
                        System.out.println("Получено: " + word);
                        Server.story.addStoryEl(word);
                        for (ServerSomthing vr : Server.serverList) {
                            vr.send(word); // отослать принятое сообщение с привязанного клиента всем остальным влючая его
                        }
                    }
                } catch (NullPointerException ignored) {
                }


            } catch (IOException e) {
                this.downService();
            }
        }

        private void send(String msg) {
            try {
                out.write(msg + "\n");
                out.flush();
            } catch (IOException ignored) {
                this.downService();
            }

        }

        private void downService() {
            Server.story.addStoryEl(name+" отключился");
            for (ServerSomthing vr : Server.serverList) {
                if(vr==this){continue;}
                vr.send(name+" отключился"); // отослать принятое сообщение с привязанного клиента всем остальным влючая его
            }
            System.out.println(name+ " отключился ("+this.getName()+")");
            try {
                if (!socket.isClosed()) {
                    socket.close();
                    in.close();
                    out.close();
                    for (ServerSomthing vr : Server.serverList) {
                        if (vr.equals(this)) vr.interrupt();
                        Server.serverList.remove(this);
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    static class Story {

        private LinkedList<String> story = new LinkedList<>();


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
}
