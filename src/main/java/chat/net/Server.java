package chat.net;

import chat.Core;
import chat.function.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static chat.net.CommandHandler.getOnlineList;
import static chat.net.CommandHandler.getStory;

public class Server {

    public static LinkedList<ServerSomthing> serverList = new LinkedList<>();
    public static Story story;
    public static Timer executor = new Timer();

    public static void main(String[] args) throws IOException {
        executor.schedule(new CommandHandler.Killer(), 1L, 1L);
        try (ServerSocket server = new ServerSocket(Core.serverPORT)) {
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

    /**
     * Массовый обзвон
     *
     * @param message текст обзвона
     */
    public static void massSend(Message message) {
        serverList.forEach(e -> e.send(message));
    }

    /**
     * Базовый поток сервера
     */
    static class ServerSomthing extends Thread {

        private final Socket socket;
        private final BufferedReader in;
        public final BufferedWriter out;
        public String name;

        /**
         * базовый конструктор потока
         *
         * @param socket Сокет потока
         * @throws IOException Ошибка игнора
         */
        public ServerSomthing(Socket socket) throws IOException {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            Server.story.printStory(out);
            start();
        }

        @Override
        /**
         * Запуск серверного потока
         */
        public void run(){
            String word;
            try{
                word = in.readLine();
                Message message = Message.infoMessage(word + " в сети");
                Server.massSend(message);
                Server.story.addStoryEl(message);
                System.out.println(message);
                this.name = word;
                try{
                    while (true) {
                        word = in.readLine();
                        if (word.equals("disconnect")) {
                            this.downService();
                            break;
                        }
                        //TODO вынести в обработчик
                        if (word.startsWith("||")) {
                            handle(word);
                            continue;
                        }
                        message = Message.chatMessage(word, this.name);
                        System.out.println(message);
                        Server.story.addStoryEl(message);
                        massSend(message);
                    }
                } catch (NullPointerException ignored) {
                }


            } catch (IOException e) {
                this.downService();
            }
        }

        /**
         * Базовая отправка сообщений на клиент привязаный к данному потоку
         *
         * @param msg
         */
        public void send(Message msg) {
            try {
                out.write(msg.toSend() + "\n");
                out.flush();
            } catch (IOException ignored) {
            }

        }

        /**
         * Закрытие потока
         */
        public void downService() {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                    in.close();
                    out.close();
                    this.interrupt();
                }
            } catch (IOException ignored) {
            }
        }

        /**
         * Обработка входящих комманд
         *
         * @param s команда
         */
        public void handle(String s) {
            if (s.equals("||online")) {
                send(getOnlineList());
            }
            if (s.startsWith("||story")) {
                if (s.length() < 10) {
                    return;
                }
                int index = 0;
                try {
                    index = Integer.parseInt(s.substring(9));
                } catch (NumberFormatException ignored) {
                }
                send(new Message(getStory(index)));
            }
        }
    }

    /**
     * Класс хранящий историю
     */
    static class Story {
        public LinkedList<Message> story = new LinkedList<>();
        public LinkedList<Message> totalStory = new LinkedList<>();

        /**
         * Добавление информации в кратковременную и долговременную истории
         *
         * @param el сообщение
         */
        public void addStoryEl(Message el) {
            if (story.size() >= 20) {
                story.removeFirst();
            }
            story.add(el);
            totalStory.add(el);
        }

        /**
         * отправка истории в поток
         *
         * @param writer поток
         */
        public void printStory(BufferedWriter writer) {
            if (story.size() > 0) {
                try {
                    for (Message msg : story) {
                        writer.write(msg.toSend() + "\n");
                    }
                    writer.flush();
                } catch (IOException ignored) {
                }

            }

        }
    }
}

class CommandHandler {
    /**
     * Обработка серверного запроса на онлайн лист
     *
     * @return строка для передачи
     */
    public static Message getOnlineList() {
        StringBuilder rtn = new StringBuilder();
        for (Server.ServerSomthing vr : Server.serverList) {
            rtn.append(vr.name).append("/s");
        }
        return Message.onlineMessage(rtn.toString());
    }

    /**
     * Запрос истории через сервер
     *
     * @param end индекс последнего сообщения
     * @return строка для отсылки
     */
    public static String getStory(int end) {
        StringBuilder rtn = new StringBuilder("||story");
        ArrayList<String> rtna = new ArrayList<>();
        int size = Server.story.totalStory.size() - 1;
        int startPos = 0;
        int endPos = 20;
        if (size - 20 - end > 0) {
            startPos = size - 20 - end;
            endPos = size - end;
        }
        if (size < 20) {
            endPos = size;
        }
        for (int i = startPos; i < endPos; i++) {
            rtna.add(Server.story.totalStory.get(i).toString());
        }
        rtna.forEach(s -> rtn.append(s).append("/s"));
        return rtn.toString();
    }

    /**
     * Отсылка сообщения об отключении
     *
     * @param name ник отключившегося
     */
    public static void disconnectMessage(String name) {
        Message message = Message.infoMessage(name + " отключился");
        Server.story.addStoryEl(message);
        Server.massSend(message);
        System.out.println(message);
    }

    /**
     * Обзвон участников с исключением неактивных и рассылкой
     *
     * @throws ConcurrentModificationException
     */
    public static void onlineChecker() throws ConcurrentModificationException {
        if (Server.serverList != null) {
            ArrayList<String> disconnected = new ArrayList<>();
            ArrayList<Server.ServerSomthing> removed = new ArrayList<>();
            for (Server.ServerSomthing vr : Server.serverList) {
                try {
                    vr.out.write(Message.pingMessage().toSend() + "\n");
                    vr.out.flush();
                }catch(IOException e){
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

    /**
     * Вызыватель очистки не активных участников
     */
    static class Killer extends TimerTask {
        @Override
        public void run() {
            try {
                onlineChecker();
            }catch(ConcurrentModificationException ignored){

            }
        }
    }
}
