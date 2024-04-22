package forms.windowsContents;

import forms.managers.CursorManager;
import forms.managers.StateManager;
import forms.managers.StateManager.CompositionType;
import forms.managers.StateManager.ResizeType;
import forms.managers.StateManager.ShapeType;
import forms.managers.StateManager.SystemState;
import forms.managers.WindowManager;
import forms.menus.PopupMenu;
import forms.shapes.ColorArea;
import forms.shapes.ShapeCreator;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.JColorChooser;
import javax.swing.JPanel;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class Panel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/
	
	//----- Objects -----
	private StateManager stateManager;
    private WindowManager windowManager;
    private CursorManager cursorManager;
	private Label helpText;
	private PopupMenu popupMenu;
	private ShapeCreator shapeCreator;
	
	//----- States -----
    public enum DepthAction {
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
    private Point mouseInShape = new Point(); // Has to be initialized at the beginning because it's used with the method setLocation(int x, int y) and can't be null
    private int initialW = 0;
    private int initialH = 0;
    
    // To paint shapes
    private Color selectedColor = null;
    
    // To avoid problems with mouseReleased on shortcuts
    private boolean mousePressed = false;
    
    /***************************************************************************
	 * Methods.
	 **************************************************************************/

    public Panel(WindowManager windowManager, Label helpText, CursorManager cursorManager) {
    	this.stateManager = new StateManager(this, cursorManager);
        this.windowManager = windowManager;
        this.cursorManager = cursorManager;
        this.helpText = helpText;
        popupMenu = new PopupMenu(this);
        shapeCreator = new ShapeCreator();
        initialize();
    }

    private void initialize() {
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mousePressed = true;
                if (e.getButton() == MouseEvent.BUTTON1) { // Left click
                	SystemState actualState = stateManager.getActualState();
                	switch(actualState) {
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
		                        	repaint();
		                        } else if (p.x > x + 10 && p.x < x + w - 10 && p.y > y + 10 && p.y < y + h - 10) { // Inside
		                        	stateManager.setMoveState(p, x, y);
		                        } else { // On the edges
		                        	stateManager.setResizeState(p, x, y, w, h);
		                        }
	                		} else {
	                			selectShape(e.getPoint());
	                			if (selectedShape != null) {
	                				repaintAroundShape(selectedShape, 5, 10);
	                			}
	                		}
							break;
						case CREATION:
							computeStartPoint(e);
					        break;
					    case COMPOSITION:
					    	composeShapes(e);
							break;
						case APPLYCOLOR:
							applySelectedColor(e.getPoint());
							break;
						default :
							break;
                	}
			    } else if (e.getButton() == MouseEvent.BUTTON3) { // Right click
			    	if (selectedShape != null && selectedShape.contains(e.getPoint())) {
			    		popupMenu.show(Panel.this, e.getX(), e.getY());
			    	}
			    }
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (!mousePressed) return;
				
				mousePressed = false;
				
				SystemState actualState = stateManager.getActualState();
				
				if (actualState == SystemState.CREATION) {
					createShape(e);
                }
				
				if (actualState != SystemState.COMPOSITION && actualState != SystemState.APPLYCOLOR) {
					stateManager.setNeutralState();
					helpText.setText("");
				}
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (selectedShape != null) { 
					// If a shape is selected but the selected shape is under an other shape, 
					// the user has to be able to select the shape above it by clicking on it.
					selectShape(e.getPoint());
	                repaint();
				}
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (selectedShape != null) {
					Rectangle bounds = selectedShape.getBounds();
					Point point = e.getPoint();
					cursorManager.adaptCursor(point, bounds);
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				SystemState actualState = stateManager.getActualState();
				switch(actualState) {
					case CREATION:
				    	computeEndPoint(e);
				        break;
					case MOVE:
				    	moveSelectedShape(e);
				        break;
				    case RESIZE:
				    	resizeSelectedShape(e);
				        break;
				    default:
				    	break;
				}
				repaint();
			}
		});
    }
    
    
    //----- List operations -----
	public List<ColorArea> getAreas() {
		return areas;
	}
	
	public void addArea(ColorArea area) {
		areas.add(area);
	}
	
	
	//----- Attributes update -----
	public void setMousePressed(boolean state) {
		mousePressed = state;
	}
	
	public void setDragStart(Point p) {
		dragStart = p;
	}
	
	public Point getMouseInShape() {
		return mouseInShape;
	}
	
	public void setInitialW(int w) {
		initialW = w;
		
	}

	public void setInitialH(int h) {
		initialH = h;
	}
	
	private void computeStartPoint(MouseEvent e) {
		startPoint = e.getPoint();
	}
	
	private void computeEndPoint(MouseEvent e) {
		// Respect window limits by updating mouse coordinates if the mouse leaves the window
    	int panelW = getWidth();
        int panelH = getHeight();
        
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
	
	
	
	//----- Shape creation and composition -----
	private void createShape(MouseEvent e) {
		// Respect window limits by updating mouse coordinates if the mouse leaves the window
		int mouseX = e.getXOnScreen();
        int mouseY = e.getYOnScreen();

        int panelX = getLocationOnScreen().x;
        int panelY = getLocationOnScreen().y;
        int panelW = getWidth();
        int panelH = getHeight();
        
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
        ShapeType shapeToCreateType = stateManager.getShapeToCreateType();
        if (shapeToCreateType == ShapeType.RECTANGLE) {
        	createdShape = shapeCreator.createRectangle(startPoint, endPoint);
        } else if (shapeToCreateType == ShapeType.OVAL) {
        	createdShape = shapeCreator.createOval(startPoint, endPoint);
        }
        ColorArea createdShapeRegion = new ColorArea(createdShape);
    	areas.add(createdShapeRegion);
    	
    	windowManager.setHasSaved(false);
    	
        startPoint = null;
        endPoint = null;
	}
	
	private void composeShapes(MouseEvent e) {
		if (selectedShape != null) { // 1st shape already selected
			ColorArea composedShape = new ColorArea(selectedShape, selectedShape.getColor()); // Contains the composed shape to be created
			int firstShapeIndex = selectedShapeIndex; // Save 1st shape
			selectShape(e.getPoint()); // Select 2nd shape
			if (selectedShape == null || selectedShape == areas.get(firstShapeIndex)) { // Composition creation cancelled
				stateManager.setNeutralState();
				helpText.setText("");
                repaint();
                return;
			}
			areas.remove(firstShapeIndex); // Destroy 1st shape
			CompositionType compositionType = stateManager.getCompositionType();
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
			
			windowManager.setHasSaved(false);
			
			selectShape(e.getPoint());
			stateManager.setNeutralState();
			helpText.setText("");
			repaint();
		} else { // Must select 1st shape
			selectShape(e.getPoint());
			if (selectedShape == null) { // No shape selected -> cancel
				stateManager.setNeutralState();
				helpText.setText("");
			} else { // Shape selected -> update helpText
				CompositionType compositionType = stateManager.getCompositionType();
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
			repaint();
		}
	}
	
	
	
	//----- Selected shape -----
	public ColorArea getSelectedShape() {
		return selectedShape;
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
	
	public void deselectSelectedShape() {
		selectedShape = null;
		selectedShapeIndex = -1;
	}
	
	private void moveSelectedShape(MouseEvent e) {
		Rectangle bounds = selectedShape.getBounds();
        int shapeW = (int) bounds.getWidth();
        int shapeH = (int) bounds.getHeight();

        int panelW = getWidth();
        int panelH = getHeight();
        
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
    	
    	windowManager.setHasSaved(false);

        dragStart = p;
	}
	
	private void resizeSelectedShape(MouseEvent e) {
		int panelW = getWidth();
	    int panelH = getHeight();
	    
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
	    double centerX = bounds.getCenterX();
		double centerY = bounds.getCenterY();
		double w = bounds.getWidth();
		double h = bounds.getHeight();
		double scaleX = 1 - dx / w;
		double scaleY = 1 - dy / h;
		

        double x = areas.get(selectedShapeIndex).getBounds2D().getX();
        double y = areas.get(selectedShapeIndex).getBounds2D().getY();
        

		boolean ResizeStateW = false;
		boolean ResizeStateH = false;
		
		ResizeType resizeType = stateManager.getResizeType();
		
        // Case where the shape is too small
		if (w <= 2.1 || h <= 2.1) {
			if (w <= 2.1) {
				if ((p.x >= x+4) && (resizeType == ResizeType.LEFT || resizeType == ResizeType.TOP_LEFT || resizeType == ResizeType.BOTTOM_LEFT)){
					ResizeStateW = true;
				}
				else if ((p.x <= x-2) && (resizeType == ResizeType.RIGHT || resizeType == ResizeType.TOP_RIGHT || resizeType == ResizeType.BOTTOM_RIGHT)) {
					ResizeStateW = true;
				}
			}
			else {
				if ((p.y >= y+4) && (resizeType == ResizeType.TOP || resizeType == ResizeType.TOP_LEFT || resizeType == ResizeType.TOP_RIGHT)){
					ResizeStateH = true;
				}
				else if ((p.y <= y-2) && (resizeType == ResizeType.BOTTOM || resizeType == ResizeType.BOTTOM_LEFT || resizeType == ResizeType.BOTTOM_RIGHT)) {
					ResizeStateH = true;
				}
			}
			
			stateManager.flipResizeState(ResizeStateW, ResizeStateH); // Change the direction of the resize
	        
	    }

		
	    switch(resizeType) {
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
	            transform.scale(scaleX, 1 + dy / w);
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
	    
	    if ((w >= 2 && h >= 2) || (ResizeStateW || ResizeStateH)) {
	    	areas.get(selectedShapeIndex).transform(transform);
	    	windowManager.setHasSaved(false);
		}

	    dragStart = p;
	}

	public void adjustSelectedShapeDepth(DepthAction action) {
		switch(action) {
			case FORGROUND:
				if (selectedShapeIndex != areas.size()-1) {
	        		areas.add(selectedShape);
	            	areas.remove(selectedShapeIndex);
	            	selectedShapeIndex = areas.size()-1;
	        	}
				break;
			case FORWARD:
				if (selectedShapeIndex < areas.size()-1) {
	        		areas.add(selectedShapeIndex + 2, selectedShape);
	            	areas.remove(selectedShapeIndex);
	            	selectedShapeIndex += 1;
	        	}
				break;
			case BACKGROUND:
				if (selectedShapeIndex != 0) {
            		areas.add(0, selectedShape);
                	areas.remove(selectedShapeIndex+1);
                	selectedShapeIndex = 0;
            	}
				break;
			case BACKWARD:
				if (selectedShapeIndex > 0) {
            		areas.add(selectedShapeIndex - 1, selectedShape);
                	areas.remove(selectedShapeIndex+1);
                	selectedShapeIndex -= 1;
            	}
				break;
		}
	}
	
	public void duplicateSelectedShape() {
        ColorArea duplicatedShape = new ColorArea(selectedShape);
        duplicatedShape.setColor(selectedShape.getColor());
        areas.add(duplicatedShape);
        
        AffineTransform transformation = new AffineTransform();
    	transformation.translate(10, 10);
    	duplicatedShape.transform(transformation);
    	
    	selectedShape = duplicatedShape;
    	selectedShapeIndex = areas.size()-1;
    	repaintAroundShape(selectedShape, 15, 20);
	}
	
	public void deleteSelectedShape() {
		if (selectedShape != null) {
			Shape savedShape = selectedShape;
    		stateManager.setNeutralState();
    		cursorManager.setDefaultCursor();
            areas.remove(selectedShapeIndex);
            
            windowManager.setHasSaved(false);
            
            selectedShape = null;
            selectedShapeIndex = -1;
            repaintAroundShape(savedShape, 5, 10);
        }
	}
	
	
	
	//----- Color -----
	public Color getSelectedColor() {
		return selectedColor;
	}
	
	public void setSelectedColor(Color c) {
		selectedColor = c;
	}
	
	private void applySelectedColor(Point point) {
        for (int i = areas.size() - 1; i >= 0; i--) {
        	ColorArea currentShape = areas.get(i);
            if (currentShape.contains(point)) {
            	currentShape.setColor(selectedColor);
                repaintAroundShape(currentShape, 0, 0);
                return;
            }
        }
        cursorManager.setDefaultCursor();
        stateManager.setNeutralState();
    }
	
	public void changeBackgroundColor() {
		selectedColor = JColorChooser.showDialog(this, "Choose a color", Color.BLACK);
        if (selectedColor != null) {
        	setBackground(selectedColor);
        }
	}

	
	
	//----- States -----
	public void setApplyColorState() {
		stateManager.setApplyColorState();
	}

	public void setCreationState(ShapeType type) {
		stateManager.setCreationState(type);
	}

	public void setCompositionState(CompositionType type) {
		stateManager.setCompositionState(type);
	}
	
	
	
	//----- Painting -----
	private void paintStoredShapes(Graphics2D g2d) {
		for (Shape shape : areas) {
			Color color = ((ColorArea) shape).getColor();
	        g2d.setColor(color);
            g2d.fill(shape);
        }
	}
	
	private void paintShapeBeingCreated(Graphics2D g2d) {
		SystemState actualState = stateManager.getActualState();
		ShapeType shapeToCreateType = stateManager.getShapeToCreateType();
		
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
            
            if (stateManager.getActualState() != SystemState.COMPOSITION) {
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
	
	// Refresh the panel
	@Override
    protected void paintComponent(Graphics g) {
		super.paintComponent(g);
         
		Graphics2D g2d = (Graphics2D) g;
		paintStoredShapes(g2d);
		paintShapeBeingCreated(g2d);
		paintSelectedShape(g2d);
    }
	
	/**
	 * Repaints around a shape with a margin of topLeftMargin on left and on top and of bottomRightMargin on right and on bottom
	 * Cheaper than repainting the whole canvas
	 */
	public void repaintAroundShape(Shape shape, int topLeftMargin, int bottomRightMargin) {
		Rectangle bounds = shape.getBounds();
        int x = (int) bounds.getX();
        int y = (int) bounds.getY();
        int w = (int) bounds.getWidth();
        int h = (int) bounds.getHeight();
        
        repaint(x-topLeftMargin, y-topLeftMargin, w+bottomRightMargin, h+bottomRightMargin);
	}
		
	
	
	//----- Reset -----
	public void reset() {
		stateManager.reset();
		cursorManager.setDefaultCursor();
	    areas.clear();
	    startPoint = null;
	    endPoint = null;
	    selectedShape = null;
	    selectedShapeIndex = -1;
	    dragStart = null;
	    mouseInShape = new Point();
	    windowManager.setHasSaved(true);
	    selectedColor = null;
	    mousePressed = false;
	    setBackground(Color.WHITE);
	    repaint();
	}

}
