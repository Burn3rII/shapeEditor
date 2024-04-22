package forms;

import forms.windowsContents.Panel;

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

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import forms.managers.CursorManager;
import forms.managers.FileManager;
import forms.managers.StateManager;
import forms.managers.WindowManager;

import javax.swing.ImageIcon;
import javax.swing.JColorChooser;

import java.io.IOException;

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

	/***************************************************************************
	 * Methods.
	 **************************************************************************/
    
	/**
	 * Launch the application.
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
	 * Creates the application.
	 */
	public MainWindow() {
		windowManager = new WindowManager(this);
		cursorManager = new CursorManager(this);
		
		helpText = new Label(); 
		getContentPane().add(helpText, BorderLayout.SOUTH);
		
		panel = new Panel(windowManager, helpText, cursorManager);
		getContentPane().add(panel, BorderLayout.CENTER);
		
		fileManager = new FileManager(panel);
		
		setBounds(100, 100, 1600, 900);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				windowManager.manageClosure();
			}
		});
		
		initializeMenuBar();
	}

	/**
	 * Initialize the menu bar.
	 */
	private void initializeMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/folder.png")));
		mntmOpen.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	try {
		    		if(fileManager.openFile()) {
		    			helpText.setText("File restored.");
		    			panel.repaint();
		    		}
		    	} catch (IOException ex) {
	                helpText.setText("Error: Failed to restore file. IOException occurred.");
	            } catch (ClassNotFoundException ex) {
	                helpText.setText("Error: Failed to restore file. Class not found exception occurred.");
	            } catch (Exception ex) {
	                helpText.setText("Error: Failed to restore file. Unknown error occurred.");
	            }
		    	
		    }
		});
		
		JMenu mnNew = new JMenu("New");
		mnNew.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/file.png")));
		mnFile.add(mnNew);
		
		JMenuItem mntmNewCanvas = new JMenuItem("Canvas");
		mntmNewCanvas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				windowManager.createNewCanvas();
			}
		});
		mnNew.add(mntmNewCanvas);
		
		JMenuItem mntmNewWindow = new JMenuItem("Window");
		mntmNewWindow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainWindow newWindow = new MainWindow();
				newWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				newWindow.setVisible(true);
			}
		});
		mnNew.add(mntmNewWindow);
		mntmOpen.setSelectedIcon(null);
		mnFile.add(mntmOpen);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		mntmSave.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/save.png")));
		mntmSave.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	panel.setMousePressed(false);
		    	try {
		    		if(fileManager.saveFile()) {
		    			windowManager.setHasSaved(true);
		                helpText.setText("File saved.");
		    		}
		    	} catch (IOException ex) {
	                helpText.setText("Error: Failed to save file. IOException occurred.");
	            } catch (Exception ex) {
	                helpText.setText("Error: Failed to save file. Unknown error occurred.");
	            }
		    }
		});
		mntmSave.setSelectedIcon(null);
		mnFile.add(mntmSave);
		
		JMenu mnCanvas = new JMenu("Canvas");
		menuBar.add(mnCanvas);
		
		JMenuItem mntmBackgroundColor = new JMenuItem("Background color");
		mntmBackgroundColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.changeBackgroundColor();
			}
		});
		mnCanvas.add(mntmBackgroundColor);
		
		JMenu mnShapes = new JMenu("Shapes");
		menuBar.add(mnShapes);
		
		JMenuItem mntmPaintColor = new JMenuItem("Paint color");
		mntmPaintColor.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/paint.png")));
		mntmPaintColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color selectedColor = JColorChooser.showDialog(panel, "Choose a Color", Color.BLACK);
		        if (selectedColor != null) {
		        	panel.setApplyColorState();
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
				panel.setMousePressed(false);
				panel.setCreationState(StateManager.ShapeType.OVAL);
    			helpText.setText("Oval creation. Click, drag and drop to create the oval.");
			}
		});
		mntmOval.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		mnInsert.add(mntmOval);
		
		JMenu mnComposition = new JMenu("Composition");
		mnComposition.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/composition.png")));
		mnShapes.add(mnComposition);
		
		JMenuItem mntmUnion = new JMenuItem("Union");
		mntmUnion.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/union.png")));
		mntmUnion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.setCompositionState(StateManager.CompositionType.UNION);
				if (panel.getSelectedShape() == null) {
					helpText.setText("Union creation. Select a first shape.");
				} else {
					helpText.setText("Create union. Select a second shape to create the union. The color of the first selected shape will be applied.");
				}
			}
		});
		mnComposition.add(mntmUnion);
		
		JMenuItem mntmIntersection = new JMenuItem("Intersection");
		mntmIntersection.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/intersection.png")));
		mntmIntersection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.setCompositionState(StateManager.CompositionType.INTERSECTION);
				if (panel.getSelectedShape() == null) {
					helpText.setText("Intersection creation. Select a first shape.");
				} else {
					helpText.setText("Intersection creation. Select a second shape to create the intersection. The color of the first selected shape will be applied.");
				}
			}
		});
		mnComposition.add(mntmIntersection);
		
		JMenuItem mntmDifference = new JMenuItem("Difference");
		mntmDifference.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/difference.png")));
		mntmDifference.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.setCompositionState(StateManager.CompositionType.DIFFERENCE);
				if (panel.getSelectedShape() == null) {
					helpText.setText("Difference creation. Select a first shape.");
				} else {
					helpText.setText("Difference creation. Select a second shape to create the difference. The color of the first selected shape will be applied.");
				}
			}
		});
		mnComposition.add(mntmDifference);
		
		JMenuItem mntmSymetricDifference = new JMenuItem("SymetricDifference");
		mntmSymetricDifference.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/resources/icons/symetricdifference.png")));
		mntmSymetricDifference.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.setCompositionState(StateManager.CompositionType.SYMETRICDIFFERENCE);
				if (panel.getSelectedShape() == null) {
					helpText.setText("Symetric difference creation. Select a first shape.");
				} else {
					helpText.setText("Symetric difference creation. Select a second shape to create the xor. The color of the first selected shape will be applied.");
				}
			}
		});
		mnComposition.add(mntmSymetricDifference);
		mnShapes.add(mntmPaintColor);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmShow = new JMenuItem("Show");
		mntmShow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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