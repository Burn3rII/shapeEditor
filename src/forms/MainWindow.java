package forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
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
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.JPopupMenu;
import java.awt.Label;

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
    	DIFFERENCE
	}
    CompositionType compositionType = null;
    
    private enum ShapeType {
        RECTANGLE,
        OVAL
    }
    ShapeType shapeToCreateType = null;
	
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
    
    private Point mouseInShape = new Point();
    
    private boolean hasSaved = true;
    
    private Color selectedColor = null;

	/***************************************************************************
	 * Methods.
	 **************************************************************************/
    
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
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
					System.exit(0);
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
        panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
                if (actualState == SystemState.CREATION) {
                	computeStartPoint(e);
                    return;
                }
                
                if (selectedShape != null && actualState != SystemState.COMPOSITION) {
                	Point p = e.getPoint();
                	
                	Rectangle bounds = selectedShape.getBounds();
                    int x = (int) bounds.getX();
                    int y = (int) bounds.getY();
                    int w = (int) bounds.getWidth();
                    int h = (int) bounds.getHeight();
                    
                    if (p.x < x - 10 || p.x > x + w + 10 || p.y < y - 10 || p.y > y + h + 10) {
                    	setNeutralState();
                    } else if (p.x > x + 10 && p.x < x + w - 10 && p.y > y + 10 && p.y < y + h - 10) {
                    	setMoveState(p, x, y);
                    } else {
                    	setResizeState(p, x, y, w, h);
                    }
                }
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (actualState == SystemState.CREATION) {
					createShape(e, panel);
                }
				
				if (actualState != SystemState.COMPOSITION) {
					setNeutralState();
					helpText.setText("");
				}
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) { // Left click
					if (actualState != SystemState.COMPOSITION) {
						selectShape(e.getPoint());
						setNeutralState();
		                helpText.setText("");
		                panel.repaint();
					} else {
						composeShapes(e, panel, helpText);
					}
			    } else if (e.getButton() == MouseEvent.BUTTON3) { // Right click
			    	if (selectedShape != null && selectedShape.contains(e.getPoint())) {
			    		popupMenu.show(panel, e.getX(), e.getY());
			    	}
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
					case NEUTRAL: {
						break;
					}
					case CREATION: {
				    	computeEndPoint(e, panel);
				        break;
				    }
					case MOVE: {
				    	moveSelectedShape(e, panel);
				        break;
				    }
				    case RESIZE: {
				    	resizeSelectedShape(e, panel);
				        break;
				    }
					case COMPOSITION: {
						break;
					}
				}
				panel.repaint();
			}
		});
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		
		JMenuItem mntmColor = new JMenuItem("Color");
        mntmColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	selectedColor = JColorChooser.showDialog(frame, "Choose a Color", Color.BLACK);
                if (selectedColor != null) {
                	selectedShape.setColor(selectedColor);
                	panel.repaint();
                }
            }
        });
        popupMenu.add(mntmColor);
		
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
		mntmOpen.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        openFile(panel, helpText);
		    }
		});
		mntmOpen.setSelectedIcon(null);
		mnFile.add(mntmOpen);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	saveFile(helpText);
		    }
		});
		mntmSave.setSelectedIcon(null);
		mnFile.add(mntmSave);
		
		JMenu mnInsert = new JMenu("Insert");
		menuBar.add(mnInsert);
		
		JMenu mnBasicShape = new JMenu("Basic shape");
		mnInsert.add(mnBasicShape);
		
		JMenuItem mntmRectangle = new JMenuItem("Rectangle");
		mntmRectangle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		mntmRectangle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCreationState(panel);
				shapeToCreateType = ShapeType.RECTANGLE;
    			helpText.setText("Rectangle creation. Click, drag and drop to create the rectangle.");
			}
		});
		mnBasicShape.add(mntmRectangle);
		
		JMenuItem mntmOval = new JMenuItem("Oval");
		mntmOval.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCreationState(panel);
				shapeToCreateType = ShapeType.OVAL;
    			helpText.setText("Oval creation. Click, drag and drop to create the oval.");
			}
		});
		mntmOval.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		mnBasicShape.add(mntmOval);
		
		JMenu mnComposition = new JMenu("Composition");
		menuBar.add(mnComposition);
		
		JMenuItem mntmUnion = new JMenuItem("Union");
		mntmUnion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actualState = SystemState.COMPOSITION;
				compositionType = CompositionType.UNION;
				if (selectedShape == null) {
					helpText.setText("Union creation. Select a first shape.");
				} else {
					helpText.setText("Create union. Select a second shape to create the union.");
				}
			}
		});
		mnComposition.add(mntmUnion);
		
		JMenuItem mntmIntersection = new JMenuItem("Intersection");
		mntmIntersection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actualState = SystemState.COMPOSITION;
				compositionType = CompositionType.INTERSECTION;
				if (selectedShape == null) {
					helpText.setText("Intersection creation. Select a first shape.");
				} else {
					helpText.setText("Intersection creation. Select a second shape to create the intersection.");
				}
			}
		});
		mnComposition.add(mntmIntersection);
		
		JMenuItem mntmDifference = new JMenuItem("Difference");
		mntmDifference.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actualState = SystemState.COMPOSITION;
				compositionType = CompositionType.DIFFERENCE;
				if (selectedShape == null) {
					helpText.setText("Difference creation. Select a first shape.");
				} else {
					helpText.setText("Difference creation. Select a second shape to create the difference.");
				}
			}
		});
		mnComposition.add(mntmDifference);
		
		JMenu mnShapes = new JMenu("Shapes");
		menuBar.add(mnShapes);
		
		JMenuItem mntmDefaultColor = new JMenuItem("Paint color");
		mntmDefaultColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedColor = JColorChooser.showDialog(frame, "Choose a Color", Color.BLACK);
                if (selectedColor != null) {
                	selectedShape.setColor(selectedColor);
                	panel.repaint();
                }
			}
		});
		mnShapes.add(mntmDefaultColor);
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
            int x = Math.min(startPoint.x, endPoint.x);
            int y = Math.min(startPoint.y, endPoint.y);
            int width = Math.abs(startPoint.x - endPoint.x);
            int height = Math.abs(startPoint.y - endPoint.y);
            Shape currentShape = null;
            if (shapeToCreateType == ShapeType.RECTANGLE) {
            	currentShape = new Rectangle(x, y, width, height);
            } else if (shapeToCreateType == ShapeType.OVAL) {
                currentShape = new Ellipse2D.Double(x, y, width, height);
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
	
	private void setDefaultCursor() {
		Cursor customCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		frame.setCursor(customCursor);
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
	
	private void computeStartPoint(MouseEvent e) {
		startPoint = e.getPoint();
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
	
	private void confirmClosure() {
		int option = JOptionPane.showConfirmDialog(
				frame, 
				"You have unsaved changes. Are you sure you want to close the application ?",
				"Close Confirmation", 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE);
		if (option == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}
	
	private void resizeSelectedShape(MouseEvent e, JPanel panel) {
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
	            transform.scale(1 + dx / areas.get(selectedShapeIndex).getBounds2D().getWidth(), scaleY);
	            transform.translate(-centerX, -centerY);
	            break;
	        case BOTTOM_LEFT:
	            transform.translate(dx/2, dy/2);
	            transform.translate(centerX, centerY);
	            transform.scale(scaleX, 1 + dy / areas.get(selectedShapeIndex).getBounds2D().getHeight());
	            transform.translate(-centerX, -centerY);
	            break;
	        case BOTTOM_RIGHT:
	        	transform.translate(dx/2, dy/2);
	        	transform.translate(centerX, centerY);
	            transform.scale(1 + dx / areas.get(selectedShapeIndex).getBounds2D().getWidth(), 1 + dy / areas.get(selectedShapeIndex).getBounds2D().getHeight());
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
	            transform.scale(1, 1 + dy / areas.get(selectedShapeIndex).getBounds2D().getHeight());
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
	            transform.scale(1 + dx / areas.get(selectedShapeIndex).getBounds2D().getWidth(), 1);
	            transform.translate(-centerX, -centerY);
	            break;
	    }
	    
	    ColorArea testShape = new ColorArea();
	    testShape.add(areas.get(selectedShapeIndex));
	    testShape.transform(transform);
	    w = testShape.getBounds2D().getWidth();
		h = testShape.getBounds2D().getHeight();
	    
	    if (w > 30 && h > 30) {
	    	areas.get(selectedShapeIndex).transform(transform);
	    	hasSaved = false;
		}

	    dragStart = p;
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

	private void setNeutralState() {
		actualState = SystemState.NEUTRAL;
	}
	
	private void setCreationState(JPanel panel) {
		selectedShape = null;
	    selectedShapeIndex = -1;
		actualState = SystemState.CREATION;
		setDefaultCursor();
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
	
	private void createShape(MouseEvent e, JPanel panel) {
		// Respect window limits by updating mouse coordinates if the mouse leaves the window
		int mouseX = e.getXOnScreen();
        int mouseY = e.getYOnScreen();

        int panelX = panel.getLocationOnScreen().x;
        int panelY = panel.getLocationOnScreen().y;
        int panelWidth = panel.getWidth();
        int panelHeight = panel.getHeight();

        if (mouseX < panelX) {
            mouseX = panelX;
        } else if (mouseX > panelX + panelWidth) {
            mouseX = panelX + panelWidth;
        }

        if (mouseY < panelY) {
            mouseY = panelY;
        } else if (mouseY > panelY + panelHeight) {
            mouseY = panelY + panelHeight;
        }

        e.translatePoint(mouseX - e.getXOnScreen(), mouseY - e.getYOnScreen());
        
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
        panel.repaint();
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
			areas.remove(firstShapeIndex); // Destroy first shape
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
					helpText.setText("Create union. Select a second shape to create the union.");
					break;
				case INTERSECTION :
					helpText.setText("Intersection creation. Select a second shape to create the intersection.");
					break;
				case DIFFERENCE :
					helpText.setText("Difference creation. Select a second shape to create the difference.");
					break;
				}
			}
			panel.repaint();
		}
	}
	
	private void deleteSelectedShape(JPanel panel) {
		if (selectedShape != null) {
    		setNeutralState();
    		setDefaultCursor();
            areas.remove(selectedShapeIndex);
            
            hasSaved = false;
            
            selectedShape = null;
            selectedShapeIndex = -1;
            panel.repaint();
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

                resetApp();
                while (true) { // Read all objects from file
                    try {
                        Shape object = (Shape) in.readObject();
                        ColorArea objectRegion = new ColorArea(object);
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
                helpText.setText("Error during restoration.");
            } catch (ClassNotFoundException ex) {
                helpText.setText("Error during restoration.");
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
                    out.writeObject(s);
                }

                out.close();
                file.close();
                
                hasSaved = true;
                helpText.setText("File saved.");

            } catch (IOException ex) {
            	helpText.setText("Error during saving.");
            }
        }
	}
	
	private void resetApp() {
		setNeutralState();
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
	}
	
}