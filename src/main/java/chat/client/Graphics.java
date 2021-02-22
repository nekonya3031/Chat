package chat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TimerTask;

import static chat.Core.size;
import static chat.Core.version;

public class Graphics extends JFrame{
    JTextPane textPane;
    JTextField input;
    JPanel panel;
    JButton send;
    JTextPane online;
    java.util.Timer timer = new java.util.Timer();
    public Graphics(){
        super("Chat" + " - " + version.type + ' ' + version.version + " | " + version.prefix);
        timer.schedule(new OnlineChecker(), 1L, 1L);
        textPane = new JTextPane();
        online = new JTextPane();
        input = new JTextField();
        panel = new JPanel();
        send = new JButton(">");
        send.addActionListener(e -> {
            out(input.getText());
            input.setText("");
        });
        textPane.setText(textPane.getText() + "\n");
        textPane.setEditable(false);
        online.setEditable(false);
        textPane.setBounds(10, 10, size.x - 200, size.y - 100);
        textPane.setBackground(new Color(43, 43, 43));
        textPane.setForeground(new Color(255, 255, 255));
        input.setBounds(10, size.y - 80, size.x - 80, 25);
        input.setBackground(new Color(49, 51, 53));
        input.setForeground(new Color(255, 255, 255));
        send.setBounds(size.x - 60, size.y - 80, 40, 25);
        send.setBackground(new Color(116, 122, 128));
        send.setForeground(new Color(255, 255, 255));
        online.setBounds(size.x - 180, 10, 160, size.y - 100);
        online.setBackground(new Color(43, 43, 43));
        online.setForeground(new Color(255,255,255));
        panel.setLayout(null);
        panel.add(textPane);
        panel.add(online);
        panel.add(input);
        panel.add(send);
        panel.setBackground(new Color(60, 63, 65));
        setContentPane(panel);
        this.setSize(size.x, size.y);
        setResizable(false);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                Client.exit();
                e.getWindow().dispose();
            }
        });
        setVisible(true);
    }

    /**
     * Обработка ввода нового сообщения
     * @param s сообщение
     */
    //TODO подсчет полученых строк и фикс перегрузки
    public void in(String s) {
        String string = textPane.getText();
        ArrayList<String> strings = new ArrayList<>();
        Collections.addAll(strings, string.split("\n"));
        Collections.addAll(strings, s.split("\n"));
        while(strings.size() > 24){
            strings.remove(0);
        }
        StringBuilder out = new StringBuilder();
        for(String str : strings){
            out.append(str).append("\n");
        }
        textPane.setText(out.toString());
    }

    /**
     * Отправка сообщения на сервер
     * @param s текст сообщения
     */
    public static void out(String s) {
        Client.send(s);
    }

    /**
     * Запрос никнейма
     * @return Введенный в поле ник, при отмене/закрытии null
     */
    public String loginDialog() {
        return JOptionPane.showInputDialog(this.panel,
                "Введите никнейм");
    }

    /**
     * Вывод информации об людях онлайн
     */
    public void online() {
        StringBuilder text = new StringBuilder("В сети:\n");
        for (String s : ClientSomthing.online) {
            text.append(s).append("\n");
        }
        if (online.getText().equals(text.toString())) {
            return;
        }
        online.setText(text.toString());
    }

    /**
     * Отсылка запросов на получение списка людей онлайн
     */
    public static class OnlineChecker extends TimerTask {
        @Override
        public void run() {
            if (Client.clientSomthing != null) {
                out("||online");
            }
        }
    }
}
