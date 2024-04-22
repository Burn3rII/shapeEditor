package forms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.net.URL;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.Label;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.ImageIcon;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class MainWindow {
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/

	private JFrame frame;
	
	private ShapeCreator shapeCreator = new ShapeCreator();
	
	//----- States -----
	private enum SystemState {
		NEUTRAL,
		CREATION,
		MOVE,
		RESIZE,
		COMPOSITION,
		APPLYCOLOR
	}
	SystemState actualState = SystemState.NEUTRAL;
	
    private enum ResizeType {
    	TOP_LEFT,
    	TOP_RIGHT,
    	BOTTOM_LEFT,
    	BOTTOM_RIGHT,
    	TOP,
    	BOTTOM,
    	LEFT,
    	RIGHT
	}
    ResizeType resizeType = null;
    
    private enum CompositionType {
    	UNION,
    	INTERSECTION,
    	DIFFERENCE,
    	SYMETRICDIFFERENCE
	}
    CompositionType compositionType = null;
    
    private enum ShapeType {
        RECTANGLE,
        OVAL
    }
    ShapeType shapeToCreateType = null;
	
    private enum DepthAction {
    	FORGROUND,
    	FORWARD,
    	BACKWARD,
    	BACKGROUND
    }
    
    //----- Other attributes -----
	private List<ColorArea> areas = new ArrayList<>();
	
	private Queue<String> undoQueue = new LinkedList<>();
    
    // For selected shape
    private ColorArea selectedShape = null;
    private int selectedShapeIndex = -1;
    
    // For shape creation
    private Point startPoint = null;
    private Point endPoint = null;
    
    // For move and resize
    private Point dragStart = null;
    
    // To keep the shapes in the canvas during move and resize
    private Point mouseInShape = new Point();
    private int initialW = 0;
    private int initialH = 0;
    
    // To display message if not saved
    private boolean hasSaved = true;
    
    // To paint shapes
    private Color selectedColor = null;
    
    // To avoid problems with mouseReleased on shortcuts
    private boolean mousePressed = false;

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
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1600, 900);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (!hasSaved) {
					confirmClosure();
				} else {
					frame.dispose(); // We use JFrame.dispoe() rather than System.exit(0) because the other windows have to stay opened even if this one is closed.
					// When the last displayable window within the Java virtual machine (VM) is disposed of, the VM may terminate.
				}
			}
		});
		
		Label helpText = new Label();
		frame.getContentPane().add(helpText, BorderLayout.SOUTH);
		
		JPopupMenu popupMenu = new JPopupMenu();
		
		JPanel panel = new JPanel() {
			private static final long serialVersionUID = 1L;

			// Refresh the panel
			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                Graphics2D g2d = (Graphics2D) g;
                paintStoredShapes(g2d);
                paintShapeBeingCreated(g2d);
                paintSelectedShape(g2d);
            }
        };
        panel.setBackground(Color.WHITE);
        panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mousePressed = true;
                if (e.getButton() == MouseEvent.BUTTON1) { // Left click
                	switch (actualState) {
	                	case NEUTRAL:
	                		if (selectedShape != null) {
	                			Point p = e.getPoint();
		                    	Rectangle bounds = selectedShape.getBounds();
		                        int x = (int) bounds.getX();
		                        int y = (int) bounds.getY();
		                        int w = (int) bounds.getWidth();
		                        int h = (int) bounds.getHeight();
		                        
		                        if (p.x < x - 10 || p.x > x + w + 10 || p.y < y - 10 || p.y > y + h + 10) { // Outside selected shape
		                        	selectShape(e.getPoint());
		                        	panel.repaint();
		                        } else if (p.x > x + 10 && p.x < x + w - 10 && p.y > y + 10 && p.y < y + h - 10) { // Inside
		                        	setMoveState(p, x, y);
		                        } else { // On the edges
		                        	setResizeState(p, x, y, w, h);
		                        }
	                		} else {
	                			selectShape(e.getPoint());
	                			if (selectedShape != null) {
	                				repaintAroundShape(selectedShape, 5, 10, panel);
	                			}
	                		}
							break;
						case CREATION:
							computeStartPoint(e);
					        break;
					    case COMPOSITION:
					    	composeShapes(e, panel, helpText);
							break;
						case APPLYCOLOR:
							applySelectedColor(e.getPoint(), panel);
							break;
						default :
							break;
                	}
			    } else if (e.getButton() == MouseEvent.BUTTON3) { // Right click
			    	if (selectedShape != null && selectedShape.contains(e.getPoint())) {
			    		popupMenu.show(panel, e.getX(), e.getY());
			    	}
			    }
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (!mousePressed) return;
				
				mousePressed = false;
				
				if (actualState == SystemState.CREATION) {
					createShape(e, panel);
                }
				
				if (actualState != SystemState.COMPOSITION && actualState != SystemState.APPLYCOLOR) {
					setNeutralState();
					helpText.setText("");
				}
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (selectedShape != null) { 
					// If a shape is selected but the selected shape is under an other shape, 
					// the user has to be able to select the shape above it by clicking on it.
					selectShape(e.getPoint());
	                panel.repaint();
				}
			}
		});
		panel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				adaptCursor(e);
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				switch (actualState) {
					case CREATION:
				    	computeEndPoint(e, panel);
				        break;
					case MOVE:
				    	moveSelectedShape(e, panel);
				        break;
				    case RESIZE:
				    	resizeSelectedShape(e, panel);
				        break;
				    default:
				    	break;
				}
				panel.repaint();
			}
		});
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		
		JMenuItem mntmColor = new JMenuItem("Color");
        mntmColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	selectedColor = JColorChooser.showDialog(frame, "Choose a color", Color.BLACK);
                if (selectedColor != null) {
                	selectedShape.setColor(selectedColor);
                	repaintAroundShape(selectedShape, 0, 0, panel);
                }
            }
        });
        popupMenu.add(mntmColor);
        
        JMenu mnDepth = new JMenu("Depth");
		popupMenu.add(mnDepth);
		
		JMenuItem mntmForground = new JMenuItem("Forground");
        mntmForground.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	adjustSelectedShapeDepth(DepthAction.FORGROUND, panel);
            }
        });
        mnDepth.add(mntmForground);
        
        JMenuItem mntmForward = new JMenuItem("Forward");
        mntmForward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	adjustSelectedShapeDepth(DepthAction.FORWARD, panel);
            }
        });
        mnDepth.add(mntmForward);
        
        JMenuItem mntmBackward = new JMenuItem("Backward");
        mntmBackward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	adjustSelectedShapeDepth(DepthAction.BACKWARD, panel);
            }
        });
        mnDepth.add(mntmBackward);
        
        JMenuItem mntmBackground = new JMenuItem("Background");
        mntmBackground.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	adjustSelectedShapeDepth(DepthAction.BACKGROUND, panel);
            }
        });
        mnDepth.add(mntmBackground);
        
        JMenuItem mntmDuplicate = new JMenuItem("Duplicate");
        mntmDuplicate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	duplicateSelectedShape(panel);
            }
        });
        popupMenu.add(mntmDuplicate);
		
        JMenuItem mntmDelete = new JMenuItem("Delete");
        mntmDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	deleteSelectedShape(panel);
            }
        });
        popupMenu.add(mntmDelete);
	
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		//mntmOpen.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/icons/folder.png")));
		mntmOpen.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        openFile(panel, helpText);
		    }
		});
		
		JMenu mnNew = new JMenu("New");
		//mnNew.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/icons/file.png")));
		mnFile.add(mnNew);
		
		JMenuItem mntmNewCanvas = new JMenuItem("Canvas");
		mntmNewCanvas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!hasSaved) {
					confirmNewCanvas(panel);
				} else {
					resetApp(panel);
				}
			}
		});
		mnNew.add(mntmNewCanvas);
		
		JMenuItem mntmNewWindow = new JMenuItem("Window");
		mntmNewWindow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainWindow newWindow = new MainWindow();
				newWindow.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				newWindow.frame.setVisible(true);
			}
		});
		mnNew.add(mntmNewWindow);
		mntmOpen.setSelectedIcon(null);
		mnFile.add(mntmOpen);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		//mntmSave.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/icons/save.png")));
		mntmSave.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	mousePressed = false;
		    	saveFile(helpText);
		    }
		});
		mntmSave.setSelectedIcon(null);
		mnFile.add(mntmSave);
		
		JMenu mnCanvas = new JMenu("Canvas");
		menuBar.add(mnCanvas);
		
		JMenuItem mntmBackgroundColor = new JMenuItem("Background color");
		mntmBackgroundColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedColor = JColorChooser.showDialog(frame, "Choose a color", Color.BLACK);
                if (selectedColor != null) {
                	panel.setBackground(selectedColor);
                }
			}
		});
		mnCanvas.add(mntmBackgroundColor);
		
		JMenu mnShapes = new JMenu("Shapes");
		menuBar.add(mnShapes);
		
		JMenuItem mntmPaintColor = new JMenuItem("Paint color");
		//mntmPaintColor.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/icons/paint.png")));
		mntmPaintColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedColor = JColorChooser.showDialog(frame, "Choose a Color", Color.BLACK);
                if (selectedColor != null) {
                	setApplyColorState(panel);
                	helpText.setText("Paint color mode. Click on the shapes to which you want to apply the chosen color.");
                }
			}
		});
		
		JMenu mnInsert = new JMenu("Insert");
		mnShapes.add(mnInsert);
		
		JMenuItem mntmRectangle = new JMenuItem("Rectangle");
		//mntmRectangle.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/icons/rectangle.png")));
		mntmRectangle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		mntmRectangle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mousePressed = false;
				setCreationState(panel);
				shapeToCreateType = ShapeType.RECTANGLE;
    			helpText.setText("Rectangle creation. Click, drag and drop to create the rectangle.");
			}
		});
		mnInsert.add(mntmRectangle);
		
		JMenuItem mntmOval = new JMenuItem("Oval");
		//mntmOval.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/icons/oval.png")));
		mntmOval.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mousePressed = false;
				setCreationState(panel);
				shapeToCreateType = ShapeType.OVAL;
    			helpText.setText("Oval creation. Click, drag and drop to create the oval.");
			}
		});
		mntmOval.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		mnInsert.add(mntmOval);
		
		JMenu mnComposition = new JMenu("Composition");
		mnShapes.add(mnComposition);
		
		JMenuItem mntmUnion = new JMenuItem("Union");
		//mntmUnion.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/icons/union.png")));
		mntmUnion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actualState = SystemState.COMPOSITION;
				compositionType = CompositionType.UNION;
				if (selectedShape == null) {
					helpText.setText("Union creation. Select a first shape.");
				} else {
					helpText.setText("Create union. Select a second shape to create the union. The color of the first selected shape will be applied.");
				}
			}
		});
		mnComposition.add(mntmUnion);
		
		JMenuItem mntmIntersection = new JMenuItem("Intersection");
		//mntmIntersection.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/icons/intersection.png")));
		mntmIntersection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actualState = SystemState.COMPOSITION;
				compositionType = CompositionType.INTERSECTION;
				if (selectedShape == null) {
					helpText.setText("Intersection creation. Select a first shape.");
				} else {
					helpText.setText("Intersection creation. Select a second shape to create the intersection. The color of the first selected shape will be applied.");
				}
			}
		});
		mnComposition.add(mntmIntersection);
		
		JMenuItem mntmDifference = new JMenuItem("Difference");
		//mntmDifference.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/icons/difference.png")));
		mntmDifference.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actualState = SystemState.COMPOSITION;
				compositionType = CompositionType.DIFFERENCE;
				if (selectedShape == null) {
					helpText.setText("Difference creation. Select a first shape.");
				} else {
					helpText.setText("Difference creation. Select a second shape to create the difference. The color of the first selected shape will be applied.");
				}
			}
		});
		mnComposition.add(mntmDifference);
		
		JMenuItem mntmSymetricDifference = new JMenuItem("SymetricDifference");
		//mntmSymetricDifference.setIcon(new ImageIcon(MainWindow.class.getResource("/forms/icons/symetricdifference.png")));
		mntmSymetricDifference.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actualState = SystemState.COMPOSITION;
				compositionType = CompositionType.SYMETRICDIFFERENCE;
				if (selectedShape == null) {
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
				showHelpWindow();
			}
		});
		mnHelp.add(mntmShow);
	}
	
	/**
	 * Repaints around a shape with a margin of topLeftMargin on left and on top and of bottomRightMargin on right and on bottom
	 * Cheaper than repainting the whole canvas
	 */
	private void repaintAroundShape(Shape shape, int topLeftMargin, int bottomRightMargin, JPanel panel) {
		Rectangle bounds = shape.getBounds();
        int x = (int) bounds.getX();
        int y = (int) bounds.getY();
        int w = (int) bounds.getWidth();
        int h = (int) bounds.getHeight();
        
        panel.repaint(x-topLeftMargin, y-topLeftMargin, w+bottomRightMargin, h+bottomRightMargin);
	}
	
	private void setNeutralState() {
		actualState = SystemState.NEUTRAL;
	}
	
	private void setCreationState(JPanel panel) {
		actualState = SystemState.CREATION;
		setDefaultCursor();
		selectedShape = null;
	    selectedShapeIndex = -1;
		panel.repaint();
	}
	
	private void setMoveState(Point p, int x, int y) {
		actualState = SystemState.MOVE;
    	mouseInShape.setLocation(p.x - x, p.y - y);
    	dragStart = p;
	}
	
	private void setResizeState(Point p, int x, int y, int w, int h) {
		actualState = SystemState.RESIZE;
    	mouseInShape.setLocation(p.x - x, p.y - y);
    	initialW = w;
    	initialH = h;

        // Determine resizing type
        if (p.x > x - 10 && p.x < x + 10 && p.y > y - 10 && p.y < y + 10) {
            resizeType = ResizeType.TOP_LEFT;
        } else if (p.x > x + w - 10 && p.x < x + w + 10 && p.y > y - 10 && p.y < y + 10) {
            resizeType = ResizeType.TOP_RIGHT;
        } else if (p.x > x - 10 && p.x < x + 10 && p.y > y + h - 10 && p.y < y + h + 10) {
            resizeType = ResizeType.BOTTOM_LEFT;
        } else if (p.x > x + w - 10 && p.x < x + w + 10 && p.y > y + h - 10 && p.y < y + h + 10) {
            resizeType = ResizeType.BOTTOM_RIGHT;
        } else if (p.x > x - 10 && p.x < x + 10 && p.y > y && p.y < y + h) {
            resizeType = ResizeType.LEFT;
        } else if (p.x > x + w - 10 && p.x < x + w + 10 && p.y > y && p.y < y + h) {
            resizeType = ResizeType.RIGHT;
        } else if (p.x > x && p.x < x + w && p.y > y - 10 && p.y < y + 10) {
            resizeType = ResizeType.TOP;
        } else if (p.x > x && p.x < x + w && p.y > y + h - 10 && p.y < y + h + 10) {
            resizeType = ResizeType.BOTTOM;
        }
        dragStart = p;
	}
	
	private void setApplyColorState(JPanel panel) {
		actualState = SystemState.APPLYCOLOR;
    	setPaintCursor();
    	selectedShape = null;
    	panel.repaint();
	}
	
	private void paintStoredShapes(Graphics2D g2d) {
		for (Shape shape : areas) {
			Color color = ((ColorArea) shape).getColor();
	        g2d.setColor(color);
            g2d.fill(shape);
        }
	}
	
	private void paintShapeBeingCreated(Graphics2D g2d) {
		if (actualState == SystemState.CREATION && startPoint != null && endPoint != null) {
            Shape currentShape = null;
            
            if (shapeToCreateType == ShapeType.RECTANGLE) {
            	currentShape = shapeCreator.createRectangle(startPoint, endPoint);
            } else if (shapeToCreateType == ShapeType.OVAL) {
                currentShape = shapeCreator.createOval(startPoint, endPoint);
            }
            
            Area currentShapeRegion = new Area(currentShape);
	        g2d.setColor(Color.BLACK);
        	g2d.fill(currentShapeRegion);
        }
	}
	
	private void paintSelectedShape(Graphics2D g2d) {
		if (selectedShape != null) {
            g2d.setColor(Color.RED);
            g2d.draw(selectedShape);
            
            if (actualState != SystemState.COMPOSITION) {
            	g2d.setColor(Color.BLUE);
                Rectangle bounds = selectedShape.getBounds();
                int x = (int) bounds.getX();
                int y = (int) bounds.getY();
                int w = (int) bounds.getWidth();
                int h = (int) bounds.getHeight();
                int halfW = w / 2;
                int halfH = h / 2;

                // Corners
                g2d.fillRect(x - 5, y - 5, 10, 10); // Top-left
                g2d.fillRect(x + w - 5, y - 5, 10, 10); // Top-right
                g2d.fillRect(x - 5, y + h - 5, 10, 10); // Bottom-left
                g2d.fillRect(x + w - 5, y + h - 5, 10, 10); // Bottom-right

                // Sides
                g2d.fillRect(x + halfW - 5, y - 5, 10, 10); // Top
                g2d.fillRect(x + halfW - 5, y + h - 5, 10, 10); // Bottom
                g2d.fillRect(x - 5, y + halfH - 5, 10, 10); // Left
                g2d.fillRect(x + w - 5, y + halfH - 5, 10, 10); // Right
            }
        }
	}
	
	private void computeStartPoint(MouseEvent e) {
		startPoint = e.getPoint();
	}
	
	private void computeEndPoint(MouseEvent e, JPanel panel) {
		// Respect window limits by updating mouse coordinates if the mouse leaves the window
    	int panelW = panel.getWidth();
        int panelH = panel.getHeight();
        
        int mouseX = e.getPoint().x;
        int mouseY = e.getPoint().y;
        
        int mouseXCorrection = 0;
        int mouseYCorrection = 0;

        if (mouseX < 0) {
        	mouseXCorrection = -mouseX;
        } else if (mouseX > panelW) {
        	mouseXCorrection = panelW - mouseX;
        }

        if (mouseY < 0) {
        	mouseYCorrection = -mouseY;
        } else if (mouseY > panelH) {
        	mouseYCorrection = panelH - mouseY;
        }

        e.translatePoint(mouseXCorrection, mouseYCorrection);
        
    	endPoint = e.getPoint();
    }
	
	private void createShape(MouseEvent e, JPanel panel) {
		// Respect window limits by updating mouse coordinates if the mouse leaves the window
		int mouseX = e.getXOnScreen();
        int mouseY = e.getYOnScreen();

        int panelX = panel.getLocationOnScreen().x;
        int panelY = panel.getLocationOnScreen().y;
        int panelW = panel.getWidth();
        int panelH = panel.getHeight();
        
        int mouseXCorrection = 0;
        int mouseYCorrection = 0;

        if (mouseX < panelX) {
        	mouseXCorrection = panelX - mouseX;
        } else if (mouseX > panelX + panelW) {
        	mouseXCorrection = panelX + panelW - mouseX;
        }

        if (mouseY < panelY) {
        	mouseYCorrection = panelY - mouseY;
        } else if (mouseY > panelY + panelH) {
        	mouseYCorrection = panelY + panelH - mouseY;
        }

        e.translatePoint(mouseXCorrection, mouseYCorrection);
        
        endPoint = e.getPoint();
        
        Shape createdShape = null;
        if (shapeToCreateType == ShapeType.RECTANGLE) {
        	createdShape = shapeCreator.createRectangle(startPoint, endPoint);
        } else if (shapeToCreateType == ShapeType.OVAL) {
        	createdShape = shapeCreator.createOval(startPoint, endPoint);
        }
        ColorArea createdShapeRegion = new ColorArea(createdShape);
    	areas.add(createdShapeRegion);
    	
    	hasSaved = false;
    	
        startPoint = null;
        endPoint = null;
	}
	
	private void composeShapes(MouseEvent e, JPanel panel, Label helpText) {
		if (selectedShape != null) { // 1st shape already selected
			ColorArea composedShape = new ColorArea(selectedShape, selectedShape.getColor()); // Contains the composed shape to be created
			int firstShapeIndex = selectedShapeIndex; // Save 1st shape
			selectShape(e.getPoint()); // Select 2nd shape
			if (selectedShape == null || selectedShape == areas.get(firstShapeIndex)) { // Composition creation cancelled
				setNeutralState();
				helpText.setText("");
                panel.repaint();
                return;
			}
			areas.remove(firstShapeIndex); // Destroy 1st shape
			switch(compositionType) { // Create composed shape
				case UNION :
					composedShape.add(selectedShape);
					break;
				case INTERSECTION :
					composedShape.intersect(selectedShape);
					break;
				case DIFFERENCE :
					composedShape.subtract(selectedShape);
					break;
				case SYMETRICDIFFERENCE :
					composedShape.exclusiveOr(selectedShape);
					break;
			}
			areas.remove(selectedShape); // Destroy 2nd shape
			areas.add(composedShape);
			
			hasSaved = false;
			
			selectShape(e.getPoint());
			setNeutralState();
			helpText.setText("");
			panel.repaint();
		} else { // Must select 1st shape
			selectShape(e.getPoint());
			if (selectedShape == null) { // No shape selected -> cancel
				setNeutralState();
				helpText.setText("");
			} else { // Shape selected -> update helpText
				switch(compositionType) {
					case UNION :
						helpText.setText("Create union. Select a second shape to create the union. The color of the first selected shape will be applied.");
						break;
					case INTERSECTION :
						helpText.setText("Intersection creation. Select a second shape to create the intersection. The color of the first selected shape will be applied.");
						break;
					case DIFFERENCE :
						helpText.setText("Difference creation. Select a second shape to create the difference. The color of the first selected shape will be applied.");
						break;
					case SYMETRICDIFFERENCE :
						helpText.setText("Symetric difference creation. Select a second shape to create the xor. The color of the first selected shape will be applied.");
						break;
					}
			}
			panel.repaint();
		}
	}
	
	private void selectShape(Point point) {
        for (int i = areas.size() - 1; i >= 0; i--) {
            if (areas.get(i).contains(point)) {
                selectedShape = areas.get(i);
                selectedShapeIndex = i;
                return;
            }
        }
        selectedShape = null;
        selectedShapeIndex = -1;
    }
	
	private void moveSelectedShape(MouseEvent e, JPanel panel) {
		Rectangle bounds = selectedShape.getBounds();
        int shapeW = (int) bounds.getWidth();
        int shapeH = (int) bounds.getHeight();

        int panelW = panel.getWidth();
        int panelH = panel.getHeight();
        
        int mouseX = e.getPoint().x;
        int mouseY = e.getPoint().y;
        
        int mouseXCorrection = 0;
        int mouseYCorrection = 0;

        if (mouseX < mouseInShape.x) {
        	mouseXCorrection = mouseInShape.x - mouseX;
        } else if (mouseX > panelW - (shapeW - mouseInShape.x)) {
        	mouseXCorrection = panelW - (shapeW - mouseInShape.x) - mouseX;
        }
        
        if (mouseY < mouseInShape.y) {
        	mouseYCorrection = mouseInShape.y - mouseY;
        } else if (mouseY > panelH - (shapeH - mouseInShape.y)) {
        	mouseYCorrection = panelH - (shapeH - mouseInShape.y) - mouseY;
        }
        
        e.translatePoint(mouseXCorrection, mouseYCorrection);
        
    	Point p = e.getPoint();
        double dx = p.x - dragStart.x;
        double dy = p.y - dragStart.y;
        
        AffineTransform transformation = new AffineTransform();
    	transformation.translate(dx, dy);
    	areas.get(selectedShapeIndex).transform(transformation);
    	
    	hasSaved = false;

        dragStart = p;
	}
	
	private void resizeSelectedShape(MouseEvent e, JPanel panel) {
		int panelW = panel.getWidth();
	    int panelH = panel.getHeight();
	    
	    int mouseX = e.getPoint().x;
	    int mouseY = e.getPoint().y;
	    
	    Rectangle bounds = selectedShape.getBounds();
        int shapeX = (int) bounds.getX();
        int shapeY = (int) bounds.getY();
        int shapeW = (int) bounds.getWidth();
        int shapeH = (int) bounds.getHeight();
	    
	    int mouseXCorrection = 0;
	    int mouseYCorrection = 0;

	    if (shapeX < 1 && mouseX <= mouseInShape.x) {
	    	mouseXCorrection = -mouseX + mouseInShape.x;
	    } else if (shapeX + shapeW  >= panelW && mouseX >= panelW + (mouseInShape.x - initialW)) {
	    	mouseXCorrection = -(mouseX - panelW - (mouseInShape.x - initialW));
	    }

	    if (shapeY < 1 && mouseY <= mouseInShape.y) {
	    	mouseYCorrection = -mouseY + mouseInShape.y;
	    } else if (shapeY + shapeH  >= panelH && mouseY >= panelH + (mouseInShape.y - initialH)) {
	    	mouseYCorrection = -(mouseY - panelH - (mouseInShape.y - initialH));
	    }

	    e.translatePoint(mouseXCorrection, mouseYCorrection);
	    
		Point p = e.getPoint();
	    double dx = p.x - dragStart.x;
	    double dy = p.y - dragStart.y;
	    
	    AffineTransform transform = new AffineTransform();
	    double centerX = areas.get(selectedShapeIndex).getBounds2D().getCenterX();
		double centerY = areas.get(selectedShapeIndex).getBounds2D().getCenterY();
		double w = areas.get(selectedShapeIndex).getBounds2D().getWidth();
		double h = areas.get(selectedShapeIndex).getBounds2D().getHeight();
		double scaleX = 1 - dx / w;
		double scaleY = 1 - dy / h;
		
	    
        double x = areas.get(selectedShapeIndex).getBounds2D().getX();
        double y = areas.get(selectedShapeIndex).getBounds2D().getY();
        

		boolean invertResizeW = false;
		boolean invertResizeH = false;
		
        // cas lorsque le resize est trop petit		
		System.out.println(p);
		System.out.println(w);
		System.out.println(x);
		//System.out.println(dx);
		if (w <= 2 || h <= 2) {
			if (w <= 2) {
				if ((p.x >= x+3) && (resizeType == ResizeType.LEFT || resizeType == ResizeType.TOP_LEFT || resizeType == ResizeType.BOTTOM_LEFT)){
					invertResizeW = true;
				}
				else if ((p.x <= x-2) && (resizeType == ResizeType.RIGHT || resizeType == ResizeType.TOP_RIGHT || resizeType == ResizeType.BOTTOM_RIGHT)) {
					invertResizeW = true;
				}
			}
			else {
				if ((p.y >= y+3) && (resizeType == ResizeType.TOP || resizeType == ResizeType.TOP_LEFT || resizeType == ResizeType.TOP_RIGHT)){
					invertResizeH = true;
				}
				else if ((p.y <= y-2) && (resizeType == ResizeType.BOTTOM || resizeType == ResizeType.BOTTOM_LEFT || resizeType == ResizeType.BOTTOM_RIGHT)) {
					invertResizeH = true;
				}
			}

			// c'est la fonction flipResizeType(invertResizeW, invertResizeH) :
	        if (resizeType == ResizeType.BOTTOM_RIGHT && invertResizeW ){
	            resizeType = ResizeType.BOTTOM_LEFT;
	        } else if (resizeType == ResizeType.BOTTOM_LEFT && invertResizeW) {
	            resizeType = ResizeType.BOTTOM_RIGHT;
	        } else if (resizeType == ResizeType.BOTTOM_RIGHT && invertResizeH) {
	            resizeType = ResizeType.TOP_RIGHT;
	        } else if (resizeType == ResizeType.BOTTOM_LEFT && invertResizeH) {
	            resizeType = ResizeType.TOP_LEFT;	            
	        } else if (resizeType == ResizeType.TOP_RIGHT && invertResizeW) {
	            resizeType = ResizeType.TOP_LEFT;
	        } else if (resizeType == ResizeType.TOP_LEFT && invertResizeW) {
	            resizeType = ResizeType.TOP_RIGHT;
	        } else if (resizeType == ResizeType.TOP_RIGHT && invertResizeH) {
	            resizeType = ResizeType.BOTTOM_RIGHT;
	        } else if (resizeType == ResizeType.TOP_LEFT && invertResizeH) {
	            resizeType = ResizeType.BOTTOM_LEFT;
	        } else if (resizeType == ResizeType.RIGHT && invertResizeW) {
	            resizeType = ResizeType.LEFT;
	        } else if (resizeType == ResizeType.LEFT && invertResizeW) {
	            resizeType = ResizeType.RIGHT;
	        } else if (resizeType == ResizeType.BOTTOM && invertResizeH) {
	            resizeType = ResizeType.TOP;
	        } else if (resizeType == ResizeType.TOP && invertResizeH) {
	            resizeType = ResizeType.BOTTOM;
	        }
	    }
		
	    switch (resizeType) {
	        case TOP_LEFT:
	        	transform.translate(dx/2, dy/2);
	        	transform.translate(centerX, centerY); // Translation towards the center
	        	transform.scale(scaleX, scaleY); // Scale relative to center
	        	transform.translate(-centerX, -centerY); // Reverse translation
	        	break;
	        case TOP_RIGHT:
	            transform.translate(dx/2, dy/2);
	            transform.translate(centerX, centerY);
	            transform.scale(1 + dx / w, scaleY);
	            transform.translate(-centerX, -centerY);
	            break;
	        case BOTTOM_LEFT:
	            transform.translate(dx/2, dy/2);
	            transform.translate(centerX, centerY);
	            transform.scale(scaleX, 1 + dy / h);
	            transform.translate(-centerX, -centerY);
	            break;
	        case BOTTOM_RIGHT:
	        	transform.translate(dx/2, dy/2);
	        	transform.translate(centerX, centerY);
	            transform.scale(1 + dx / w, 1 + dy / h);
	            transform.translate(-centerX, -centerY);
	            break;
	        case TOP:
	            transform.translate(0, dy/2);
	            transform.translate(centerX, centerY);
	            transform.scale(1, scaleY);
	            transform.translate(-centerX, -centerY);
	            break;
	        case BOTTOM:
	        	transform.translate(0, dy/2);
	        	transform.translate(centerX, centerY);
	            transform.scale(1, 1 + dy / h);
	            transform.translate(-centerX, -centerY);
	            break;
	        case LEFT:
	            transform.translate(dx/2, 0);
	            transform.translate(centerX, centerY);
	            transform.scale(scaleX, 1);
	            transform.translate(-centerX, -centerY);
	            break;
	        case RIGHT:
	        	transform.translate(dx/2, 0);
	        	transform.translate(centerX, centerY);
	            transform.scale(1 + dx / w, 1);
	            transform.translate(-centerX, -centerY);
	            break;
	    }
	    
	    ColorArea testShape = new ColorArea();
	    testShape.add(areas.get(selectedShapeIndex));
	    testShape.transform(transform);
	    w = testShape.getBounds2D().getWidth();
		h = testShape.getBounds2D().getHeight();
		
	    
	    if ((w>=1 && h>=1) || (invertResizeW || invertResizeH)){
	    	//flip_resizeType();
	    	areas.get(selectedShapeIndex).transform(transform);
	    	hasSaved = false;
		}
	    
	    dragStart = p;
	}
	
	/* création d'une fonction pour tester si un booloean reverseResize peut être true ou false
	 * private boolean testReverseResize(Point p, w, h) {
		return true/false;
	}*/
	
	/* création d'une fonction flipResize(Point p, w, h) pour changer le mode actuel de resize 
	 * en fonction des booleans reverseResize (et donc de la fonction testReverseResize(...) :
	 * private void flipResize(Point p, double w, double h) {
	 * 		
	 * }
	 */

	private void applySelectedColor(Point point, JPanel panel) {
        for (int i = areas.size() - 1; i >= 0; i--) {
        	ColorArea currentShape = areas.get(i);
            if (currentShape.contains(point)) {
            	currentShape.setColor(selectedColor);
                repaintAroundShape(currentShape, 0, 0, panel);
                return;
            }
        }
        setDefaultCursor();
        setNeutralState();
    }
	
	private void adjustSelectedShapeDepth(DepthAction action, JPanel panel) {
		switch(action) {
			case FORGROUND:
				if (selectedShapeIndex != areas.size()-1) {
	        		areas.add(selectedShape);
	            	areas.remove(selectedShapeIndex);
	            	selectedShapeIndex = areas.size()-1;
	            	repaintAroundShape(selectedShape, 0, 0, panel);
	        	}
				break;
			case FORWARD:
				if (selectedShapeIndex < areas.size()-1) {
	        		areas.add(selectedShapeIndex + 2, selectedShape);
	            	areas.remove(selectedShapeIndex);
	            	selectedShapeIndex += 1;
	            	repaintAroundShape(selectedShape, 0, 0, panel);
	        	}
				break;
			case BACKGROUND:
				if (selectedShapeIndex != 0) {
            		areas.add(0, selectedShape);
                	areas.remove(selectedShapeIndex+1);
                	selectedShapeIndex = 0;
                	repaintAroundShape(selectedShape, 0, 0, panel);
            	}
				break;
			case BACKWARD:
				if (selectedShapeIndex > 0) {
            		areas.add(selectedShapeIndex - 1, selectedShape);
                	areas.remove(selectedShapeIndex+1);
                	selectedShapeIndex -= 1;
                	repaintAroundShape(selectedShape, 0, 0, panel);
            	}
				break;
		}
	}
	
	private void duplicateSelectedShape(JPanel panel) {
        ColorArea duplicatedShape = new ColorArea(selectedShape);
        duplicatedShape.setColor(selectedShape.getColor());
        areas.add(duplicatedShape);
        
        AffineTransform transformation = new AffineTransform();
    	transformation.translate(10, 10);
    	duplicatedShape.transform(transformation);
    	
    	selectedShape = duplicatedShape;
    	selectedShapeIndex = areas.size()-1;
    	repaintAroundShape(selectedShape, 15, 20, panel);
	}
	
	private void deleteSelectedShape(JPanel panel) {
		if (selectedShape != null) {
			Shape savedShape = selectedShape;
    		setNeutralState();
    		setDefaultCursor();
            areas.remove(selectedShapeIndex);
            
            hasSaved = false;
            
            selectedShape = null;
            selectedShapeIndex = -1;
            repaintAroundShape(savedShape, 5, 10, panel);
        }
	}
	
	private void setDefaultCursor() {
		Cursor customCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		frame.setCursor(customCursor);
	}
	
	private void setPaintCursor() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
        URL imageUrl = getClass().getResource("/forms/cursors/paint.png");
        Image image = toolkit.getImage(imageUrl);
        Cursor customCursor = toolkit.createCustomCursor(image, new Point(0, 0), "img");
        frame.setCursor(customCursor);
	}
	
	private void adaptCursor(MouseEvent e) {
		// Adapt cursor to mouse location
		if (selectedShape != null) {
        	Point p = e.getPoint();
        	
        	Rectangle bounds = selectedShape.getBounds();
            int x = (int) bounds.getX();
            int y = (int) bounds.getY();
            int w = (int) bounds.getWidth();
            int h = (int) bounds.getHeight();
            
            if (p.x > x + 10 && p.x < x + w - 10 && p.y > y + 10 && p.y < y + h - 10) {
            	Cursor customCursor = new Cursor(Cursor.MOVE_CURSOR);
            	frame.setCursor(customCursor);
            } else if (p.x > x - 10 && p.x < x + 10 && p.y > y - 10 && p.y < y + 10) {
            	Cursor customCursor = new Cursor(Cursor.NW_RESIZE_CURSOR);
            	frame.setCursor(customCursor);
            } else if (p.x > x + w - 10 && p.x < x + w + 10 && p.y > y - 10 && p.y < y + 10) {
            	Cursor customCursor = new Cursor(Cursor.NE_RESIZE_CURSOR);
            	frame.setCursor(customCursor);
            } else if (p.x > x - 10 && p.x < x + 10 && p.y > y + h - 10 && p.y < y + h + 10) {
            	Cursor customCursor = new Cursor(Cursor.SW_RESIZE_CURSOR);
            	frame.setCursor(customCursor);
            } else if (p.x > x + w - 10 && p.x < x + w + 10 && p.y > y + h - 10 && p.y < y + h + 10) {
            	Cursor customCursor = new Cursor(Cursor.SE_RESIZE_CURSOR);
            	frame.setCursor(customCursor);
            } else if (p.x > x - 10 && p.x < x + 10 && p.y > y && p.y < y + h) {
            	Cursor customCursor = new Cursor(Cursor.W_RESIZE_CURSOR);
            	frame.setCursor(customCursor);
            } else if (p.x > x + w - 10 && p.x < x + w + 10 && p.y > y && p.y < y + h) {
            	Cursor customCursor = new Cursor(Cursor.E_RESIZE_CURSOR);
            	frame.setCursor(customCursor);
            } else if (p.x > x && p.x < x + w && p.y > y - 10 && p.y < y + 10) {
            	Cursor customCursor = new Cursor(Cursor.N_RESIZE_CURSOR);
            	frame.setCursor(customCursor);
            } else if (p.x > x && p.x < x + w && p.y > y + h - 10 && p.y < y + h + 10) {
            	Cursor customCursor = new Cursor(Cursor.S_RESIZE_CURSOR);
            	frame.setCursor(customCursor);
            } else {
            	setDefaultCursor();
            }
		}
	}
	
	private void openFile(JPanel panel, Label helpText) {
		JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null); 
        
        // Check if the user has selected a file
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Deserialization
            try {
                FileInputStream file = new FileInputStream(selectedFile);
                ObjectInputStream in = new ObjectInputStream(file);

                resetApp(panel);
                while (true) { // Read all objects from file
                    try {
                    	@SuppressWarnings("unchecked")
                    	Entry<Shape,Color> object = (Entry<Shape,Color>) in.readObject();
                    	Shape shape = object.getKey();
                    	Color color = object.getValue();
                    	
                        ColorArea objectRegion = new ColorArea(shape, color);
                        areas.add(objectRegion);
                    } catch (EOFException ex) { // End of file
                        break;
                    }
                }

                in.close();
                file.close();

                panel.repaint();
                helpText.setText("File restored.");
            } catch (IOException ex) {
                helpText.setText("Error: Failed to restore file. IOException occurred.");
            } catch (ClassNotFoundException ex) {
                helpText.setText("Error: Failed to restore file. Class not found exception occurred.");
            } catch (Exception ex) {
                helpText.setText("Error: Failed to restore file. Unknown error occurred.");
            }
        }
	}
	
	private void saveFile(Label helpText) {
		JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(null);

        // Check if the user has selected a backup location
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filename = selectedFile.getAbsolutePath();

            // Serialization
            try {
                FileOutputStream file = new FileOutputStream(filename);
                ObjectOutputStream out = new ObjectOutputStream(file);

                for (Shape shape : areas) { // Save each object in the file
                    Shape s = AffineTransform.getTranslateInstance(0,0).createTransformedShape(shape); // Enables an Area to be serialized by creating a Shape.
                    Color c = ((ColorArea) shape).getColor();

                    Entry<Shape,Color> object=new SimpleEntry<>(s,c);
                    
                    out.writeObject(object);
                }

                out.close();
                file.close();
                
                hasSaved = true;
                helpText.setText("File saved.");

            } catch (IOException ex) {
                helpText.setText("Error: Failed to save file. IOException occurred.");
            } catch (Exception ex) {
                helpText.setText("Error: Failed to save file. Unknown error occurred.");
            }
        }
	}
	
	private void confirmNewCanvas(JPanel panel) {
		int option = JOptionPane.showConfirmDialog(
				frame, 
				"You have unsaved changes. Are you sure you want to reset the canvas ?",
				"Close Confirmation", 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE);
		if (option == JOptionPane.YES_OPTION) {
			resetApp(panel);
		}
	}
	
	private void confirmClosure() {
		int option = JOptionPane.showConfirmDialog(
				frame, 
				"You have unsaved changes. Are you sure you want to close the application ?",
				"Close Confirmation", 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE);
		if (option == JOptionPane.YES_OPTION) {
			frame.dispose(); // We use JFrame.dispoe() rather than System.exit(0) because the other windows have to stay opened even if this one is closed.
			// When the last displayable window within the Java virtual machine (VM) is disposed of, the VM may terminate.
		}
	}
	
	private void showHelpWindow() {
        HelpWindow helpWindow = new HelpWindow();
        helpWindow.setVisible(true);
    }
	
	private void resetApp(JPanel panel) {
		setNeutralState();
		setDefaultCursor();
	    resizeType = null;
	    compositionType = null;
	    shapeToCreateType = null;
	    areas.clear();
	    startPoint = null;
	    endPoint = null;
	    selectedShape = null;
	    selectedShapeIndex = -1;
	    dragStart = null;
	    mouseInShape = new Point();
	    hasSaved = true;
	    selectedColor = null;
	    panel.setBackground(Color.WHITE);
	    panel.repaint();
	}
	
}
