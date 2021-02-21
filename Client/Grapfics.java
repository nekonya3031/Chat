import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Grapfics extends JFrame {
    JTextPane textPane;
    JTextField inputer;
    JPanel panel;
    JButton send;

    public Grapfics() {
        super("Chat");
        textPane = new JTextPane();
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
        textPane.setBounds(10, 10, 300, 400);
        inputer.setBounds(10, 420, 250, 25);
        send.setBounds(270, 420, 25, 25);
        panel.setLayout(null);
        panel.add(textPane);
        panel.add(inputer);
        panel.add(send);
        panel.setBackground(new Color(0, 150, 0));
        setContentPane(panel);
        this.setSize(500, 500);
        setResizable(false);
        setVisible(true);
    }

    public static void main(String[] args) {
        new Grapfics();
    }

    public void in(String s) {
        String string = textPane.getText();
        ArrayList<String> strings = new ArrayList<>();
        for (String baka : string.split("\n")) {
            strings.add(baka);
        }
        for (String teme : s.split("\n")) {
            strings.add(teme);
        }
        while (strings.size() > 24) {
            strings.remove(0);
        }
        String out = "";
        for (String baka : strings) {
            out += baka + "\n";
        }
        textPane.setText(out);
    }

    public void out(String s) {
        Client.send(s);
    }
}
