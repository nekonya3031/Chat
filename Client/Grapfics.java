import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class Grapfics extends JFrame {
    JTextPane textPane;
    JTextField inputer;
    JPanel panel;

    public Grapfics() {
        super("Chat");
        textPane = new JTextPane();
        inputer = new JTextField();
        textPane.setText(textPane.getText() + "\n");
        textPane.setEditable(false);
        textPane.setBounds(10, 10, 400, 400);
        inputer.setBounds(10, 420, 400, 50);
        add(textPane);
        add(inputer);
        add(inputer);
        this.setSize(500, 500);
        setResizable(false);
        setVisible(true);
    }

    public static void main(String[] args) {
        new Grapfics();
    }

    public void addKeyBoardListener() {
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                }
            }
        });
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
}
