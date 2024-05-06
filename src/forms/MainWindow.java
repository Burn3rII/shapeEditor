package forms;

import forms.managers.CursorManager;
import forms.managers.FileManager;
import forms.managers.StateManager;
import forms.managers.WindowManager;
import forms.windowsContents.Panel;
import forms.network.Client;
import forms.network.Server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Label;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JCheckBoxMenuItem;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.BindException;
import java.net.MalformedURLException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;


/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class MainWindow extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/

	private Panel panel;
	private Label helpText;
	private WindowManager windowManager;
	private CursorManager cursorManager;
	private FileManager fileManager;
	private Server server;
	private Client client;
	
	/***************************************************************************
	 * Constructors.
	 **************************************************************************/
	
	/**
	 * Constructor for MainWindow class.
	 * Initializes window, cursor, and file managers, help text, and main panel.
	 */
	public MainWindow() {
		windowManager = new WindowManager(this);
		cursorManager = new CursorManager(this);
		
		helpText = new Label();
		helpText.setFont(new Font("Arial", Font.BOLD, 14));
		getContentPane().add(helpText, BorderLayout.SOUTH);
		
		panel = new Panel(windowManager, helpText, cursorManager);
		getContentPane().add(panel, BorderLayout.CENTER);
		
		fileManager = new FileManager(panel);
		try {
			server = new Server(fileManager);
		} catch (RemoteException e) {
			helpText.setText("Error during server creation. RemoteException occurred.");
		}
		client = new Client(fileManager);
		
		setBounds(100, 100, 1600, 900);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				windowManager.manageClosure(server, client);
			}
		});
		
		initializeMenuBar();
	}

	/***************************************************************************
	 * Methods.
	 **************************************************************************/
    
	/**
	 * Main method of the application. Sets the UI look and feel and launches the main window.
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Initialize the menu bar.
	 */
	private void initializeMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenu mnNew = new JMenu("New");
		mnNew.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/file.png")));
		mnFile.add(mnNew);
		
		JMenuItem mntmNewCanvas = new JMenuItem("Canvas");
		mntmNewCanvas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.repaint();
				windowManager.createNewCanvas();
			}
		});
		mnNew.add(mntmNewCanvas);
		
		JMenuItem mntmNewWindow = new JMenuItem("Window");
		mntmNewWindow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.repaint();
				MainWindow newWindow = new MainWindow();
				newWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				newWindow.setVisible(true);
			}
		});
		mnNew.add(mntmNewWindow);
		
		JMenu mnOpen = new JMenu("Open");
		mnOpen.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/folder.png")));
		mnFile.add(mnOpen);
		
		JMenuItem mntmOpenLocally = new JMenuItem("Local file");
		mntmOpenLocally.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	panel.repaint();
		    	try {
		    		if (fileManager.openFile()) {
		    			helpText.setText("File restored.");
		    			panel.repaint();
		    		}
		    	} catch (FileNotFoundException ex) {
		    		helpText.setText("Error: Failed to restore file. FileNotFoundException occurred. " + ex.getMessage());
		    	} catch (IOException ex) {
	                helpText.setText("Error: Failed to restore file. IOException occurred. " + ex.getMessage());
	            } catch (Exception ex) {
	                helpText.setText("Error: Failed to restore file. Unknown error occurred. " + ex.getMessage());
	            }
		    }
		});
		mnOpen.add(mntmOpenLocally);
		
		JMenuItem mntmOpenDistant = new JMenuItem("File on a distant machine");
		mntmOpenDistant.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	panel.repaint();
		    	if (client.isConnected()) {
		    		try {
		    			client.restoreFileFromServer();
		    			helpText.setText("File restored.");
		    			panel.repaint();
		    		} catch (IOException ex) {
		    			helpText.setText("Error: Failed to restore file. IOException occurred. " + ex.getMessage());
		    		}
		    	} else {
		            JOptionPane.showMessageDialog(null, "Error: Application is not connected to a server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
		    	}
		    }
		});
		mnOpen.add(mntmOpenDistant);
		
		JMenu mnSave = new JMenu("Save");
		mnSave.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/save.png")));
		mnFile.add(mnSave);
		
		JMenu mnSaveFile = new JMenu("Save file");
		mnSave.add(mnSaveFile);
		
		JMenuItem mntmSaveLocally = new JMenuItem("Save locally");
		mntmSaveLocally.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		mntmSaveLocally.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	panel.repaint();
		    	panel.setMousePressed(false);
		    	try {
		    		if (fileManager.saveFile()) {
		    			windowManager.setHasSaved(true);
		                helpText.setText("File saved.");
		    		}
		    	} catch (IOException ex) {
	                helpText.setText("Error: Failed to save file. IOException occurred. " + ex.getMessage());
	            } catch (Exception ex) {
	                helpText.setText("Error: Failed to save file. Unknown error occurred. " + ex.getMessage());
	            }
		    }
		});
		mnSaveFile.add(mntmSaveLocally);
		
		JMenuItem mntmSaveDistant = new JMenuItem("Save on a distant machine");
		mntmSaveDistant.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	panel.repaint();
		    	if (server.isConnected()) {
		    		try {
		    			Integer chosenPort = server.choosePort();
		    			
		    	        if (chosenPort != null) {
		    	        	server.saveFile(chosenPort);
			    			windowManager.setHasSaved(true);
			    			helpText.setText("File saved.");
		    	        }
		    		} catch (IOException ex) {
		    			helpText.setText("Error: Failed to save file. IOException occurred. " + ex.getMessage());
		    			ex.printStackTrace();
		    		}
		    	} else {
	            JOptionPane.showMessageDialog(null, "Error: Server hasn't been started.", "Connection Error", JOptionPane.ERROR_MESSAGE);
		    	}
		    }
		});
		mnSaveFile.add(mntmSaveDistant);
		
		JMenuItem mntmSaveAsImage = new JMenuItem("Save as image");
		mntmSaveAsImage.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	panel.repaint();
		    	panel.setMousePressed(false);
		    	try {
		    		if (fileManager.saveAsImage()) {
		                helpText.setText("Image created.");
		    		}
		    	} catch (IOException ex) {
	                helpText.setText("Error: Failed to save file as image. IOException occurred. " + ex.getMessage());
	            } catch (Exception ex) {
	                helpText.setText("Error: Failed to save file as image. Unknown error occurred. " + ex.getMessage());
	            }
		    }
		});
		mnSave.add(mntmSaveAsImage);
		
		JMenu mnServer = new JMenu("Server");
		mnServer.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/server.png")));
		mnFile.add(mnServer);
		
		JMenuItem mntmStart = new JMenuItem("Start");
		mntmStart.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	panel.repaint();
		    	try {
		    		int port = server.start(client.getPort());
		    		if (port == -2) {
		    			helpText.setText("Server already started on this port");
		    		} else if (port != -1) {
		    			helpText.setText("Server started on port " + port);
		    		}
		    	} catch (RemoteException ex) {
		    		helpText.setText("Error: Failed to start server. RemoteException occurred. Please try on another port. " + ex.getMessage());
		    	} catch (MalformedURLException ex) {
		    		helpText.setText("Error: Failed to start server. MalformedURLException occurred. " + ex.getMessage());
		    	} catch (NotBoundException ex) {
		    		helpText.setText("Error: Failed to start server. NotBoundException occurred. " + ex.getMessage());
				} catch (BindException ex) {
					helpText.setText("Error: Failed to start server. BindException occurred. " + ex.getMessage());
				}
		    }
		});
		mnServer.add(mntmStart);
		
		JMenuItem mntmConnect = new JMenuItem("Connect to another server");
		mntmConnect.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	panel.repaint();
		    	try {
		    		String[] serverInfos = client.connectToServer(server.getPorts());
			    	helpText.setText("Connected to server (ip:" + serverInfos[0] + ", port:" + serverInfos[1] + ").");
		    	} catch (RemoteException ex) {
		    		helpText.setText("Error: Failed to connect to server. RemoteException occurred. " + ex.getMessage());
		    	} catch (MalformedURLException ex) {
		    		helpText.setText("Error: Failed to connect to server. MalformedURLException occurred. " + ex.getMessage());
		    	}  catch (NotBoundException ex) {
		    		helpText.setText("Error: Failed to connect to server. NotBoundException occurred. " + ex.getMessage());
		    	} catch (BindException ex) {
		    		helpText.setText("Error: Failed to start server. BindException occurred. " + ex.getMessage());
				}
		    }
		});
		mnServer.add(mntmConnect);
		
		JMenu mnCanvas = new JMenu("Canvas");
		menuBar.add(mnCanvas);
		
		JMenu mnBackground = new JMenu("Background");
		mnCanvas.add(mnBackground);
		
		JMenuItem mntmBackgroundColor = new JMenuItem("Color");
		mntmBackgroundColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.repaint();
				panel.changeBackgroundColor();
			}
		});
		mnBackground.add(mntmBackgroundColor);
		
		JMenuItem mntmBackgroundImage = new JMenuItem("Image");
		mntmBackgroundImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.repaint();
				panel.changeBackgroundImage();
			}
		});
		mnBackground.add(mntmBackgroundImage);
		
		JMenu mnShapes = new JMenu("Shapes");
		menuBar.add(mnShapes);
		
		JMenuItem mntmPaintColor = new JMenuItem("Paint color");
		mntmPaintColor.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/paint.png")));
		mntmPaintColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.repaint();
				Color selectedColor = JColorChooser.showDialog(panel, "Choose a Color", Color.BLACK);
		        if (selectedColor != null) {
		        	panel.setApplyColorState(selectedColor);
		        	helpText.setText("Paint color mode. Click on the shapes to which you want to apply the chosen color.");
		        }
			}
		});
		
		JMenu mnInsert = new JMenu("Insert");
		mnInsert.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/insert.png")));
		mnShapes.add(mnInsert);
		
		JMenuItem mntmRectangle = new JMenuItem("Rectangle");
		mntmRectangle.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/rectangle.png")));
		mntmRectangle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		mntmRectangle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.repaint();
				panel.setMousePressed(false);
				panel.setCreationState(StateManager.ShapeType.RECTANGLE);
    			helpText.setText("Rectangle creation. Click, drag and drop to create the rectangle.");
			}
		});
		mnInsert.add(mntmRectangle);
		
		JMenuItem mntmOval = new JMenuItem("Oval");
		mntmOval.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/oval.png")));
		mntmOval.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.repaint();
				panel.setMousePressed(false);
				panel.setCreationState(StateManager.ShapeType.OVAL);
    			helpText.setText("Oval creation. Click, drag and drop to create the oval.");
			}
		});
		mntmOval.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		mnInsert.add(mntmOval);
		
		JMenuItem mntmTriangle = new JMenuItem("Triangle");
		mntmTriangle.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/triangle.png")));
		mntmTriangle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.repaint();
				panel.setMousePressed(false);
				panel.setCreationState(StateManager.ShapeType.TRIANGLE);
    			helpText.setText("Triangle creation. Click 3 times to create the 3 points of the triangle.");
			}
		});
		mntmTriangle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
		mnInsert.add(mntmTriangle);
		
		JMenu mnComposition = new JMenu("Composition");
		mnComposition.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/composition.png")));
		mnShapes.add(mnComposition);
		
		JMenuItem mntmUnion = new JMenuItem("Union");
		mntmUnion.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/union.png")));
		mntmUnion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.repaint();
				panel.setCompositionState(StateManager.CompositionType.UNION);
				if (panel.getSelectedShape() == null) {
					helpText.setText("Union creation. Select a first shape.");
				} else {
					helpText.setText("Union creation. Select a second shape to create the union. The color and outline of the first selected shape will be applied.");
				}
			}
		});
		mnComposition.add(mntmUnion);
		
		JMenuItem mntmIntersection = new JMenuItem("Intersection");
		mntmIntersection.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/intersection.png")));
		mntmIntersection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.repaint();
				panel.setCompositionState(StateManager.CompositionType.INTERSECTION);
				if (panel.getSelectedShape() == null) {
					helpText.setText("Intersection creation. Select a first shape.");
				} else {
					helpText.setText("Intersection creation. Select a second shape to create the intersection. The color and outline of the first selected shape will be applied.");
				}
			}
		});
		mnComposition.add(mntmIntersection);
		
		JMenuItem mntmDifference = new JMenuItem("Difference");
		mntmDifference.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/difference.png")));
		mntmDifference.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.repaint();
				panel.setCompositionState(StateManager.CompositionType.DIFFERENCE);
				if (panel.getSelectedShape() == null) {
					helpText.setText("Difference creation. Select a first shape.");
				} else {
					helpText.setText("Difference creation. Select a second shape to create the difference. The color and outline of the first selected shape will be applied.");
				}
			}
		});
		mnComposition.add(mntmDifference);
		
		JMenuItem mntmSymetricDifference = new JMenuItem("SymetricDifference");
		mntmSymetricDifference.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/symetricdifference.png")));
		mntmSymetricDifference.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.repaint();
				panel.setCompositionState(StateManager.CompositionType.SYMETRICDIFFERENCE);
				if (panel.getSelectedShape() == null) {
					helpText.setText("Symetric difference creation. Select a first shape.");
				} else {
					helpText.setText("Symetric difference creation. Select a second shape to create the xor. The color and outline of the first selected shape will be applied.");
				}
			}
		});
		mnComposition.add(mntmSymetricDifference);
		mnShapes.add(mntmPaintColor);
		
		JMenu mnPreferences = new JMenu("Preferences");
		menuBar.add(mnPreferences);
		
		JCheckBoxMenuItem chckbxmntmAskForSave = new JCheckBoxMenuItem("Ask for save on close");
		chckbxmntmAskForSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.repaint();
				windowManager.switchAskForSave();
			}
		});
		chckbxmntmAskForSave.setSelected(true);
		mnPreferences.add(chckbxmntmAskForSave);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmShow = new JMenuItem("Show");
		mntmShow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.repaint();
				windowManager.showHelpWindow();
			}
		});
		mnHelp.add(mntmShow);
	}
	
	/**
	 * Reset the window.
	 */
	public void reset() {
		panel.reset();
	}
	
}