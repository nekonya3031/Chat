package chat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import chat.*;

public class Grapfics extends JFrame implements KeyListener{
    JTextPane textPane;
    JTextField inputer;
    JPanel panel;
    JButton send;
    JTextPane online;
    java.util.Timer timer = new java.util.Timer();

    public Grapfics(){
        super("Chat" + " - " + Core.version.type + ' ' + Core.version.version + " | " + Core.version.prefix);
        timer.schedule(new OnlineChecker(), 1L, 1L);
        textPane = new JTextPane();
        online = new JTextPane();
        inputer = new JTextField();
        panel = new JPanel();
        send = new JButton(">");
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                out(inputer.getText());
                inputer.setText("");
            }
        });
        textPane.setText(textPane.getText() + "\n");
        textPane.setEditable(false);
        online.setEditable(false);
        textPane.setBounds(10, 10, Core.size.x - 200, Core.size.y - 100);
        textPane.setBackground(new Color(43, 43, 43));
        textPane.setForeground(new Color(255,255,255));
        inputer.setBounds(10, Core.size.y - 80, Core.size.x - 80, 25);
        inputer.setBackground(new Color(49, 51, 53));
        inputer.setForeground(new Color(255,255,255));
        send.setBounds(Core.size.x - 60, Core.size.y - 80, 40, 25);
        send.setBackground(new Color(116, 122, 128));
        send.setForeground(new Color(255,255,255));
        online.setBounds(Core.size.x - 180, 10, 160, Core.size.y - 100);
        online.setBackground(new Color(43, 43, 43));
        online.setForeground(new Color(255,255,255));
        panel.setLayout(null);
        panel.add(textPane);
        panel.add(online);
        panel.add(inputer);
        panel.add(send);
        panel.setBackground(new Color(60, 63, 65));
        setContentPane(panel);
        this.setSize(Core.size.x, Core.size.y);
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

    public static void main(String[] args) {
        new Grapfics();
    }

    public void in(String s) {
        String string = textPane.getText();
        ArrayList<String> strings = new ArrayList<>();
        for(String str : string.split("\n")){
            strings.add(str);
        }
        for(String str : s.split("\n")){
            strings.add(str);
        }
        while(strings.size() > 24){
            strings.remove(0);
        }
        String out = "";
        for(String str : strings){
            out += str + "\n";
        }
        textPane.setText(out);
    }

    public static void out(String s) {
        Client.send(s);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            out(inputer.getText());
            inputer.setText("");
        }
    }

    public String loginDialog() {
        return JOptionPane.showInputDialog(this.panel,
                "<html><h2>Введите никнейм");
    }

    public void online() {
        String text = "В сети:\n";
        for (String s : ClientSomthing.online) {
            text += s + "\n";
        }
        if (online.getText().equals(text)) {
            return;
        }
        online.setText(text);
    }

    public static class OnlineChecker extends TimerTask {
        @Override
        public void run() {
            if (Client.clientSomthing != null) {
                out("||online");
            }
        }
    }
}
