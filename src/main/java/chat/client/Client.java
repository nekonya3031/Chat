package chat.client;

import chat.Core;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

public class Client{
    public static ClientSomthing clientSomthing;

    public static void main(String[] args){
        clientSomthing = new ClientSomthing(Core.ipAddr, Core.port);
    }

    /**
     * Публичная отправка сообщения
     * @param s Текст сообщения
     */
    public static void send(String s) {
        clientSomthing.send(s);
    }

    /**
     * Публичный метод остановки приложения
     */
    public static void exit() {
        clientSomthing.downService();
    }

    /**
     * Приватный метод остановки приложения
     */
    public static void exitpr() {
        System.exit(0);
    }
}

class ClientSomthing{
    public static Graphics g = new Graphics();
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private BufferedReader inputUser;
    public static ArrayList<String> online = new ArrayList<>();

    /**
     * Базовый отсылатель сообщений
     * @param s Отпраляемая строка
     */
    public void send(String s){
        try{
            out.write(s + "\n");
            out.flush();
        }catch(IOException ignored) {

        }
    }

    /**
     * Базовый конструктор сокетклиентского класса
     * @param address айпи сокета
     * @param port порт сокета
     */
    public ClientSomthing(String address, int port){
        try{
            this.socket = new Socket(address, port);
        }catch(IOException e) {
            System.err.println("Socket failed");
        }
        try{
            inputUser = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.pressNickname();
            new ReadMsg().start();
            //new WriteMsg().start();
            out.write("||online" + "\n");
            out.flush();
        }catch(IOException e) {
            ClientSomthing.this.downService();
        }
    }

    /**
     * Запись сообщения в консоль и вывод на графику
     * @param s текст сообщения
     */
    public static void log(String s){
        System.out.println(s);
        g.in(s + "\n");
    }

    /**
     * Завершение работы приложения
     */
    public void downService(){
        try{
            if(!socket.isClosed()){
                socket.close();
                out.write("disconnect");
                in.close();
                out.close();
            }
            Client.exitpr();
        }catch(IOException ignored){}
    }

    private void pressNickname(){
        try{
            String nickname = g.loginDialog();
            out.write(nickname + "\n");
            out.flush();
        }catch(IOException ignored){}
    }

    /**
     * Обработчик команд приходящих с сервера
     */
    public static class Handler{
        public static void handle(String message) {
            if (message.startsWith("||online")) {
                online(message);
            }
        }
        public static void online(String message){
            message = message.substring(8);
            ArrayList<String> rtn = new ArrayList<>();
            Collections.addAll(rtn, message.split("/s"));
            online = rtn;
            g.online();
        }
    }

    @Deprecated
    public class WriteMsg extends Thread{

        @Override
        public void run(){
            while (true){
                String userWord;
                try{
                    userWord = inputUser.readLine();
                    if(userWord.equals("disconnect")){
                        out.write("disconnect" + "\n");
                        ClientSomthing.this.downService();
                        break;
                    }else{
                        out.write(userWord + "\n");
                    }
                    out.flush();
                }catch(IOException e){
                    ClientSomthing.this.downService();

                }
            }
        }
    }

    /**
     * Прослушка сообщений из сокета
     */
    private class ReadMsg extends Thread{
        @Override
        public void run(){
            String str;
            try{
                while(true){
                    str = in.readLine();
                    if (str.equals("disconnect")) {
                        ClientSomthing.this.downService();
                        break;
                    }
                    if (str.startsWith("||")) {
                        Handler.handle(str);
                    } else {
                        log(str);
                    }

                }
            }catch(IOException e){
                ClientSomthing.this.downService();
            }
        }
    }
}
