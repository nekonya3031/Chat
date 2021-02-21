import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Grapfics extends JFrame {
    JTextPane textPane;
    JTextField inputer;

    public Grapfics() {
        super("Chat");
        this.setSize(500, 500);
        textPane = new JTextPane();
        textPane.setText(textPane.getText() + "NEVER\n");
        textPane.setEditable(false);
        inputer = new JTextField();
        this.add(textPane);
        //this.add(inputer);
        this.show();
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
        textPane.setText(textPane.getText() + s + "\n");
    }
}
