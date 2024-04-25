package forms.managers;

import forms.windows.HelpWindow;
import forms.MainWindow;

import javax.swing.JOptionPane;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class WindowManager {
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/
	
	private MainWindow window;
	
	private boolean hasSaved = false;
	private boolean askForSave = true;
	
	/***************************************************************************
	 * Methods.
	 **************************************************************************/

	public WindowManager(MainWindow window) {
		this.window = window;
	}
	
	public void setHasSaved(boolean state) {
		hasSaved = state;
	}
	
	public void createNewCanvas() {
		if (!hasSaved) {
			confirmNewCanvas();
		} else {
			window.reset();
		}
	}
	
	private void confirmNewCanvas() {
		int option = JOptionPane.showConfirmDialog(
				window, 
				"You have unsaved changes. Are you sure you want to reset the canvas ?",
				"Close Confirmation", 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE);
		if (option == JOptionPane.YES_OPTION) {
			window.reset();
		}
	}
	
	public void manageClosure() {
		if (!hasSaved && askForSave) {
			confirmClosure();
		} else {
			window.dispose(); // We use JFrame.dispoe() rather than System.exit(0) because the other windows have to stay opened even if this one is closed.
			// When the last displayable window within the Java virtual machine (VM) is disposed of, the VM may terminate.
		}
	}
	
	public void switchAskForSave() {
		askForSave = (askForSave == true ? false : true);
	}
	
	private void confirmClosure() {
		int option = JOptionPane.showConfirmDialog(
				window, 
				"You have unsaved changes. Are you sure you want to close the application ?",
				"Close Confirmation", 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE);
		if (option == JOptionPane.YES_OPTION) {
			window.dispose(); // We use JFrame.dispoe() rather than System.exit(0) because the other windows have to stay opened even if this one is closed.
			// When the last displayable window within the Java virtual machine (VM) is disposed of, the VM may terminate.
		}
	}
	
	public void showHelpWindow() {
        HelpWindow helpWindow = new HelpWindow();
        helpWindow.setVisible(true);
    }

}
