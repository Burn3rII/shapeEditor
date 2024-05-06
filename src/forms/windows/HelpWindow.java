package forms.windows;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class HelpWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	
	/***************************************************************************
	 * Methods.
	 **************************************************************************/

	/**
	 * Creates the help window.
	 */
	public HelpWindow() {
        setTitle("Help");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initializeTextArea();
    }

	/**
	 * Creates the text area and fills it with the text.
	 */
    private void initializeTextArea() {
    	JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));

        // Shortcuts
        textArea.append("Shortcuts :\n");
        textArea.append("- Ctrl + S to save\n");
        textArea.append("- Ctrl + R to create a rectangle\n");
        textArea.append("- Ctrl + O to create an oval\n");
        textArea.append("- Ctrl + T to create a triangle\n");

        JScrollPane scrollPane = new JScrollPane(textArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
	}
    
}