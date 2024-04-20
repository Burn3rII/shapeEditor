package forms;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class HelpWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	public HelpWindow() {
        setTitle("Aide");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));

        // Section des raccourcis
        textArea.append("Shortcuts :\n");
        textArea.append("- Ctrl + S to save\n");
        textArea.append("- Ctrl + R to create a rectangle\n");
        textArea.append("- Ctrl + O to create an oval\n");

        JScrollPane scrollPane = new JScrollPane(textArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HelpWindow helpWindow = new HelpWindow();
            helpWindow.setVisible(true);
        });
    }
}