package forms.managers;

import forms.MainWindow;
import forms.network.Client;
import forms.network.Server;
import forms.windows.HelpWindow;

import java.rmi.RemoteException;

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
	
	private boolean hasSaved = true;
	private boolean askForSave = true;
	
	/***************************************************************************
	 * Constructors.
	 **************************************************************************/
	
	/**
     * Constructor for WindowManager class.
     * @param window The associated main window.
     */
	public WindowManager(MainWindow window) {
		this.window = window;
	}
	
	/***************************************************************************
	 * Methods.
	 **************************************************************************/

	/**
     * Sets the flag indicating whether the canvas has been saved.
     * @param state The state indicating whether the canvas has been saved.
     */
	public void setHasSaved(boolean state) {
		hasSaved = state;
	}
	
	/**
     * Creates a new canvas, prompting the user to save changes if necessary.
     */
	public void createNewCanvas() {
		if (!hasSaved) {
			confirmNewCanvas();
		} else {
			window.reset();
		}
	}
	
	/**
     * Prompts the user to confirm before closing the application or resetting the canvas with unsaved changes.
     */
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
	
	/**
     * Manages the closure of the application, prompting the user to save changes if necessary.
     */
	public void manageClosure(Server server, Client client) {
		if (!hasSaved && askForSave) {
			confirmClosure(server, client);
		} else {
			close(server, client);
		}
	}
	
	/**
     * Closes the application.
     */
	public void close(Server server, Client client) {
		try {
			server.stop();
			System.out.println("Server stopped.");
		} catch (RemoteException e) {
			System.err.println("Error while disconnecting from the server: " + e.getMessage());
		}
		
		try {
			client.disconnect();
			System.out.println("Client disconnected from server.");
		} catch (RemoteException e) {
			System.err.println("Error while disconnecting from the server: " + e.getMessage());
		}
		
		window.dispose(); // We use JFrame.dispoe() rather than System.exit(0) because the other windows have to stay opened even if this one is closed.
		// When the last displayable window within the Java virtual machine (VM) is disposed of, the VM may terminate.
	}
	
	/**
     * Toggles the flag indicating whether to ask for saving changes before certain actions.
     */
	public void switchAskForSave() {
		askForSave = (askForSave == true ? false : true);
	}
	
	/**
     * Prompts the user to confirm before closing the application with unsaved changes.
     */
	private void confirmClosure(Server server, Client client) {
		int option = JOptionPane.showConfirmDialog(
				window, 
				"You have unsaved changes. Are you sure you want to close the application ?",
				"Close Confirmation", 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE);
		if (option == JOptionPane.YES_OPTION) {
			close(server, client);
		}
	}
	
	/**
     * Displays the help window.
     */
	public void showHelpWindow() {
        HelpWindow helpWindow = new HelpWindow();
        helpWindow.setVisible(true);
    }

}
