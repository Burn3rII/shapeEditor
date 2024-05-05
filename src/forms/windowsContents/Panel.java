package forms.windowsContents;

import forms.managers.CursorManager;
import forms.managers.StateManager;
import forms.managers.StateManager.CompositionType;
import forms.managers.StateManager.ResizeType;
import forms.managers.StateManager.ShapeType;
import forms.managers.StateManager.SystemState;
import forms.managers.WindowManager;
import forms.menus.PopupMenu;
import forms.shapes.GeneralShape;
import forms.shapes.Rectangle;
import forms.shapes.Oval;
import forms.shapes.Triangle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Label;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.JColorChooser;

import javax.imageio.ImageIO;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.IOException;

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
	
	//----- States -----
    public enum DepthAction {
    	FORGROUND,
    	FORWARD,
    	BACKWARD,
    	BACKGROUND
    }
    
    //----- Other attributes -----
	private List<GeneralShape> shapes = new ArrayList<>();
	
	// Background
	private BufferedImage backgroundImage = null; // Set this to null if you want a color to be used as background
	private String backgroundImagePath = null;
    
    // For selected shape
    private GeneralShape selectedShape = null;
    private int selectedShapeIndex = -1;
    
    // For shape creation
    private Point startPoint = null;
    private Point endPoint = null;
    private List<Point> pointCollection = new ArrayList<>(); // Generic collection that can be used for multiple purposes. Warning: for better 
    //performances, pointCollection is never cleared after it has been used. Every function that uses it has to clear it before using it.
    private Point lastTrianglePoint = null; // Used to be able to display the futur triangle while it's being created
    
    // For move and resize
    private Point dragStart = null;
    
    // To keep the shapes in the canvas during move and resize
    private Point mouseInShape = new Point(); // Has to be initialized at the beginning because it's used with the method setLocation(int x, int y) and can't be null
    private int initialW = 0;
    private int initialH = 0;
    
    // To paint shapes
    private Color selectedColor = null;
    private Color selectedOutlineColor = null;
    
    // To avoid problems with mouseReleased on shortcuts
    private boolean mousePressed = false;
    
    /***************************************************************************
	 * Methods.
	 **************************************************************************/

    /**
     * Constructs a new Panel with the specified window manager, help text label, and cursor manager.
     * 
     * @param windowManager The associated window manager.
     * @param helpText The associated label to display help text.
     * @param cursorManager The associated cursor manager.
     */
    public Panel(WindowManager windowManager, Label helpText, CursorManager cursorManager) {
    	this.stateManager = new StateManager(this, cursorManager);
        this.windowManager = windowManager;
        this.cursorManager = cursorManager;
        this.helpText = helpText;
        popupMenu = new PopupMenu(this);
        initialize();
    }

    /**
	 * Initialize the panel.
	 */
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
	                			java.awt.Rectangle bounds = selectedShape.getBounds();
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
						case CREATION: continueShapeCreation(e); break;
					    case COMPOSITION: composeShapes(e); break;
						case APPLYCOLOR: applySelectedColor(e.getPoint()); break;
						default: break;
                	}
			    } else if (e.getButton() == MouseEvent.BUTTON3) { // Right click
			    	if (selectedShape != null && selectedShape.contains(e.getPoint())) {
			    		popupMenu.show(Panel.this, e.getX(), e.getY());
			    	}
			    }
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (!mousePressed) return; // If "released" is triggered before "pressed" has been, the behaviour is 
				//considered to be abnormal (particularly if a shortcut is used) and nothing is done.
				
				mousePressed = false;
				
				SystemState actualState = stateManager.getActualState();
				switch(actualState) {
					case APPLYCOLOR: break;
					case COMPOSITION: break;
					case CREATION: {
						ShapeType shapeToCreateType = stateManager.getShapeToCreateType();
						switch(shapeToCreateType) {
							case OVAL: createShape(e); stateManager.setNeutralState(); helpText.setText(""); break;
							case RECTANGLE: createShape(e); stateManager.setNeutralState(); helpText.setText(""); break;
							case TRIANGLE: if (pointCollection.size() == 3) { stateManager.setNeutralState(); helpText.setText(""); } break;
						}
						break;
					}
					case MOVE: stateManager.setNeutralState(); helpText.setText(""); break;
					case NEUTRAL: stateManager.setNeutralState(); helpText.setText(""); break;
					case RESIZE: stateManager.setNeutralState(); helpText.setText(""); break;
				}
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (selectedShape != null) {
					// If a shape is selected but the selected shape is under an other shape, 
					// the user has to be able to select the shape above it by clicking on it.
					selectShape(e.getPoint());
					if (selectedShape == null) cursorManager.setDefaultCursor();
	                repaint();
				}
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (selectedShape != null) {
					java.awt.Rectangle bounds = selectedShape.getBounds();
					Point point = e.getPoint();
					cursorManager.adaptCursor(point, bounds);
				} else {
					SystemState actualState = stateManager.getActualState();
					if (actualState == SystemState.CREATION) {
						ShapeType shapeToCreateType = stateManager.getShapeToCreateType();
						if (shapeToCreateType == ShapeType.TRIANGLE && pointCollection.size() == 2) {
							lastTrianglePoint = e.getPoint();
							repaint();
						}
					}
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				SystemState actualState = stateManager.getActualState();
				switch(actualState) {
					case CREATION: {
						ShapeType shapeToCreateType = stateManager.getShapeToCreateType();
						switch(shapeToCreateType) {
							case OVAL: computeEndPoint(e); repaint(); break;
							case RECTANGLE: computeEndPoint(e); repaint(); break;
							default: break;	
						}
						break;
					}
					case MOVE: moveSelectedShape(e); repaint(); break;
				    case RESIZE: resizeSelectedShape(e); repaint(); break;
				    default: break;
				}
			}
		});
    }
    
    
    //----- List operations -----
    /**
     * Returns the list of shapes currently present on the panel.
     * @return The list of shapes.
     */
	public List<GeneralShape> getShapes() {
		return shapes;
	}
	

	/**
	 * Adds a shape to the list of shapes on the panel.
	 * @param shape The shape to be added.
	 */
	public void addShape(GeneralShape shape) {
		shapes.add(shape);
	}
	
	
	//----- Attributes update -----
	/**
	 * Sets the state of mouse press.
	 * @param state The state of mouse press.
	 */
	public void setMousePressed(boolean state) {
		mousePressed = state;
	}
	
	/**
	 * Sets the starting point for drag.
	 * @param p The starting point for drag.
	 */
	public void setDragStart(Point p) {
		dragStart = p;
	}
	
	/**
	 * Returns the mouse in shape point.
	 * @return The mouse in shape point.
	 */
	public Point getMouseInShape() {
		return mouseInShape;
	}
	
	/**
	 * Sets the initial width of a shape.
	 * @param w The initial width of the shape.
	 */
	public void setInitialW(int w) {
		initialW = w;
	}

	/**
	 * Sets the initial height of a shape.
	 * @param h The initial height of the shape.
	 */
	public void setInitialH(int h) {
		initialH = h;
	}
	
	/**
	 * Computes the starting point.
	 * @param e The MouseEvent object.
	 */
	private void computeStartPoint(MouseEvent e) {
		startPoint = e.getPoint();
	}
	
	/**
	 * Adds a point to the collection.
	 * @param p The point to be added.
	 */
	private void addPointToCollection(Point p) {
		pointCollection.add(p);
	}
	
	/**
	 * Computes the end point respecting window limits.
	 * @param e The MouseEvent object.
	 */
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
	/**
	 * Continues the shape creation process based on the current state.
	 * @param e The MouseEvent object.
	 */
	private void continueShapeCreation(MouseEvent e) {
		ShapeType shapeToCreateType = stateManager.getShapeToCreateType();
		
		switch(shapeToCreateType) {
			case OVAL: computeStartPoint(e); break;
			case RECTANGLE: computeStartPoint(e); break;
			case TRIANGLE: {
				addPointToCollection(e.getPoint()); 
				if (pointCollection.size() == 3) createShape();
				break;
			}
		}
	}
	
	/**
	 * Creates a shape based on the MouseEvent object.
	 * For shapes created by drag and drop (rectangle, oval etc...)
	 * @param e The MouseEvent object.
	 */
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
        
        GeneralShape createdShape = null;
        ShapeType shapeToCreateType = stateManager.getShapeToCreateType();
        switch(shapeToCreateType) {
			case RECTANGLE: createdShape = new Rectangle(startPoint, endPoint); break;
			case OVAL: createdShape = new Oval(startPoint, endPoint); break;
			default: break;
        }
    	shapes.add(createdShape);
    	
    	windowManager.setHasSaved(false);
    	
        startPoint = null;
        endPoint = null;
	}
	
	/**
	 * Creates a shape based on collected points.
	 * For shapes created by clicks (triangle etc...) -> no need to check for window limits
	 */
	private void createShape() {
        GeneralShape createdShape = null;
        ShapeType shapeToCreateType = stateManager.getShapeToCreateType();
        switch(shapeToCreateType) {
			case TRIANGLE: createdShape = new Triangle(pointCollection.get(0), pointCollection.get(1), pointCollection.get(2)); break;
			default: break;
        }
    	shapes.add(createdShape);
    	
    	windowManager.setHasSaved(false);
    	
    	lastTrianglePoint = null;
	}
	
	/**
	 * Composes shapes based on the selected composition type.
	 * @param e The MouseEvent object.
	 */
	private void composeShapes(MouseEvent e) {
		if (selectedShape != null) { // 1st shape already selected
			GeneralShape firstShape = shapes.get(selectedShapeIndex); // Save 1st shape
			selectShape(e.getPoint()); // Select 2nd shape
			if (selectedShape == null || selectedShape.equals(firstShape)) { // Composition creation cancelled
				stateManager.setNeutralState();
				helpText.setText("");
                repaint();
                return;
			}
			CompositionType compositionType = stateManager.getCompositionType();
			switch(compositionType) { // Create composed shape
				case UNION: firstShape.add(selectedShape); break;
				case INTERSECTION: firstShape.intersect(selectedShape); break;
				case DIFFERENCE: firstShape.subtract(selectedShape); break;
				case SYMETRICDIFFERENCE: firstShape.exclusiveOr(selectedShape); break;
			}
			shapes.remove(selectedShape); // Destroy 2nd shape
			
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
					case UNION: helpText.setText("Create union. Select a second shape to create the union. The color of the first selected shape will be applied."); break;
					case INTERSECTION: helpText.setText("Intersection creation. Select a second shape to create the intersection. The color of the first selected shape will be applied."); break;
					case DIFFERENCE: helpText.setText("Difference creation. Select a second shape to create the difference. The color of the first selected shape will be applied."); break;
					case SYMETRICDIFFERENCE: helpText.setText("Symetric difference creation. Select a second shape to create the xor. The color of the first selected shape will be applied."); break;
				}
			}
			repaint();
		}
	}
	
	
	
	//----- Selected shape -----
	/**
	 * Gets the selected shape.
	 * @return The selected shape.
	 */
	public GeneralShape getSelectedShape() {
		return selectedShape;
	}
	
	/**
	 * Selects the first shape that contains the given point.
	 * @param point The point to select the shape.
	 */
	private void selectShape(Point point) {
        for (int i = shapes.size() - 1; i >= 0; i--) {
            if (shapes.get(i).contains(point)) {
                selectedShape = shapes.get(i);
                selectedShapeIndex = i;
                return;
            }
        }
        selectedShape = null;
        selectedShapeIndex = -1;
    }
	
	/**
	 * Deselects the selected shape.
	 */
	public void deselectSelectedShape() {
		selectedShape = null;
		selectedShapeIndex = -1;
	}
	
	/**
	 * Moves the selected shape.
	 * @param e The MouseEvent object.
	 */
	private void moveSelectedShape(MouseEvent e) {
		java.awt.Rectangle bounds = selectedShape.getBounds();
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

    	selectedShape.translate(dx, dy);
    	
    	windowManager.setHasSaved(false);

        dragStart = p;
	}
	
	/**
	 * Resizes the selected shape.
	 * @param e The MouseEvent object.
	 */
	private void resizeSelectedShape(MouseEvent e) {
		//----- 1st step : compute mouse correction to make sure the shape stays in the boundaries.
		int panelW = getWidth();
	    int panelH = getHeight();
	    
	    Point mouse = e.getPoint();
	    int mouseX = mouse.x;
	    int mouseY = mouse.y;
	    
	    java.awt.Rectangle bounds = selectedShape.getBounds();
        int shapeX = (int) bounds.getX();
        int shapeY = (int) bounds.getY();
        double shapeW = bounds.getWidth(); // We need double later to calculate scale
        double shapeH = bounds.getHeight(); // We need double later to calculate scale
	    
	    int mouseXCorrection = 0;
	    int mouseYCorrection = 0;
	    
	    ResizeType previousResizeType = stateManager.getResizeType();
	    
	    if (shapeX < 1 
	    	&& mouseX <= mouseInShape.x // Second condition : to still be able to move the shape when we come back in the boundaries
	    	&& (previousResizeType == ResizeType.TOP_LEFT || previousResizeType == ResizeType.LEFT || previousResizeType == ResizeType.BOTTOM_LEFT)) { // Third condition : to still be able to resize when the shape takes the whole width/height of the canvas
	    	mouseXCorrection = -mouseX + mouseInShape.x;
	    } else if (shapeX + shapeW  >= panelW 
	    		   && mouseX >= panelW + (mouseInShape.x - initialW) 
	    		   && (previousResizeType == ResizeType.TOP_RIGHT || previousResizeType == ResizeType.RIGHT || previousResizeType == ResizeType.BOTTOM_RIGHT)) {
	    	mouseXCorrection = -(mouseX - panelW - (mouseInShape.x - initialW));
	    }

	    if (shapeY < 1 
	    	&& mouseY <= mouseInShape.y 
	    	&& (previousResizeType == ResizeType.TOP_LEFT || previousResizeType == ResizeType.TOP || previousResizeType == ResizeType.TOP_RIGHT)) {
	    	mouseYCorrection = -mouseY + mouseInShape.y;
	    } else if (shapeY + shapeH  >= panelH 
	    		   && mouseY >= panelH + (mouseInShape.y - initialH) 
	    		   && (previousResizeType == ResizeType.BOTTOM_LEFT || previousResizeType == ResizeType.BOTTOM || previousResizeType == ResizeType.BOTTOM_RIGHT)) {
	    	mouseYCorrection = -(mouseY - panelH - (mouseInShape.y - initialH));
	    }

	    e.translatePoint(mouseXCorrection, mouseYCorrection);
	    
	    mouse = e.getPoint(); // Update attributes with new, corrected values (the others stay the same)
	    mouseX = mouse.x;
	    mouseY = mouse.y;
        
	    //----- 2nd step : check if we flipped the resize type (i.e. crossed on of the edges while resizing) and make the appropriate changes if so.
        double dx = mouseX - dragStart.x;
	    double dy = mouseY - dragStart.y;
        
	    //ResizeType previousResizeType = stateManager.getResizeType();
	    
		boolean resizeTypeFlipped = stateManager.checkResizeTypeFlip(dx, dy, shapeW, shapeH);
		
		ResizeType resizeType = stateManager.getResizeType();
		
		if (resizeTypeFlipped) { // If we flipped the state, to make sure the shape stays at the exact same place (i.e. for 
			// example, the left border becomes the right border if we cross from RIGHT to LEFT), we have to translate the 
			// shape of its width/height, depending on the case. We must apply the transform before the 3rd step.
			switch(resizeType) {
		        case TOP_LEFT:
		        	if (previousResizeType == ResizeType.TOP_RIGHT) selectedShape.translate(-shapeW, 0);
		        	else selectedShape.translate(0, -shapeH);
		        	break;
		        case TOP_RIGHT:
		        	if (previousResizeType == ResizeType.TOP_LEFT) selectedShape.translate(shapeW, 0);
		        	else selectedShape.translate(0, -shapeH);
		            break;
		        case BOTTOM_LEFT:
		        	if (previousResizeType == ResizeType.BOTTOM_RIGHT) selectedShape.translate(-shapeW, 0);
		        	else selectedShape.translate(0, shapeH);
		            break;
		        case BOTTOM_RIGHT:
		        	if (previousResizeType == ResizeType.BOTTOM_LEFT) selectedShape.translate(shapeW, 0);
		        	else selectedShape.translate(0, shapeH);
		            break;
		        case TOP:
		        	selectedShape.translate(0, -shapeH);
		            break;
		        case BOTTOM:
		        	selectedShape.translate(0, shapeH);
		            break;
		        case LEFT:
		        	selectedShape.translate(-shapeW, 0);
		            break;
		        case RIGHT:
		        	selectedShape.translate(shapeW, 0);
		            break;
			}
			bounds = selectedShape.getBounds(); // Update attributes after the transform
		}
		
		//----- 3rd step : apply the final resize
		double centerX = bounds.getCenterX();
		double centerY = bounds.getCenterY();
		double scaleXL = 1 - dx / shapeW;
		double scaleYT = 1 - dy / shapeH;
		double scaleXR = 1 + dx / shapeW;
		double scaleYB = 1 + dy / shapeH;
		if (scaleXL == 0) scaleXL = 1; // Very important : if scale = 0, the shape will disappear because the width will be 0, making it impossible to grow again (0*x = 0).
		if (scaleYT == 0) scaleYT = 1;
		if (scaleXR == 0) scaleXR = 1;
		if (scaleYB == 0) scaleYB = 1;
		
		

		AffineTransform transform = new AffineTransform();
		switch(resizeType) {
	        case TOP_LEFT:
	        	transform.translate(dx/2, dy/2);
	        	transform.translate(centerX, centerY); // Translation towards the center
	        	transform.scale(scaleXL, scaleYT); // Scale relative to center
	        	transform.translate(-centerX, -centerY); // Reverse translation
	        	break;
	        case TOP_RIGHT:
	            transform.translate(dx/2, dy/2);
	            transform.translate(centerX, centerY);
	            transform.scale(scaleXR, scaleYT);
	            transform.translate(-centerX, -centerY);
	            break;
	        case BOTTOM_LEFT:
	            transform.translate(dx/2, dy/2);
	            transform.translate(centerX, centerY);
	            transform.scale(scaleXL, scaleYB);
	            transform.translate(-centerX, -centerY);
	            break;
	        case BOTTOM_RIGHT:
	        	transform.translate(dx/2, dy/2);
	        	transform.translate(centerX, centerY);
	            transform.scale(scaleXR, scaleYB);
	            transform.translate(-centerX, -centerY);
	            break;
	        case TOP:
	            transform.translate(0, dy/2);
	            transform.translate(centerX, centerY);
	            transform.scale(1, scaleYT);
	            transform.translate(-centerX, -centerY);
	            break;
	        case BOTTOM:
	        	transform.translate(0, dy/2);
	        	transform.translate(centerX, centerY);
	            transform.scale(1, scaleYB);
	            transform.translate(-centerX, -centerY);
	            break;
	        case LEFT:
	            transform.translate(dx/2, 0);
	            transform.translate(centerX, centerY);
	            transform.scale(scaleXL, 1);
	            transform.translate(-centerX, -centerY);
	            break;
	        case RIGHT:
	        	transform.translate(dx/2, 0);
	        	transform.translate(centerX, centerY);
	            transform.scale(scaleXR, 1);
	            transform.translate(-centerX, -centerY);
	            break;
	    }
		    
	    selectedShape.transform(transform);
		windowManager.setHasSaved(false);
		
		if(resizeTypeFlipped) { // To make sure the next time this function is called the 1st step works well, we have to update mouseInShape and initialW/H if the state has been flipped
			bounds = selectedShape.getBounds();
	        shapeX = (int) bounds.getX();
	        shapeY = (int) bounds.getY();
			mouseInShape.setLocation(mouseX - shapeX, mouseY - shapeY);
			initialW = (int) bounds.getWidth();
	        initialH = (int) bounds.getHeight();
	        
	        cursorManager.adaptCursor(resizeType);
		}
	
		dragStart = mouse; // Current mouse location becomes the previous one for the next iteration
	}

	/**
	 * Adjusts the depth of the selected shape based on the given action.
	 * @param action The action to adjust the depth.
	 */
	public void adjustSelectedShapeDepth(DepthAction action) {
		switch(action) {
			case FORGROUND:
				if (selectedShapeIndex != shapes.size()-1) {
	        		shapes.add(selectedShape);
	            	shapes.remove(selectedShapeIndex);
	            	selectedShapeIndex = shapes.size()-1;
	        	}
				break;
			case FORWARD:
				if (selectedShapeIndex < shapes.size()-1) {
	        		shapes.add(selectedShapeIndex + 2, selectedShape);
	            	shapes.remove(selectedShapeIndex);
	            	selectedShapeIndex += 1;
	        	}
				break;
			case BACKGROUND:
				if (selectedShapeIndex != 0) {
            		shapes.add(0, selectedShape);
                	shapes.remove(selectedShapeIndex+1);
                	selectedShapeIndex = 0;
            	}
				break;
			case BACKWARD:
				if (selectedShapeIndex > 0) {
            		shapes.add(selectedShapeIndex - 1, selectedShape);
                	shapes.remove(selectedShapeIndex+1);
                	selectedShapeIndex -= 1;
            	}
				break;
		}
		repaintAroundShape(selectedShape, 0, 0);
	}
	
	/**
	 * Reflects the selected shape along the specified axis.
	 * @param xAxis If true, reflects along the x-axis; otherwise, reflects along the y-axis.
	 */
	public void symmetryOnSelectedShape(boolean xAxis) {
		selectedShape.reflect(xAxis);
		repaintAroundShape(selectedShape, 0, 0);
	}
	
	/**
	 * Duplicates the selected shape.
	 */
	public void duplicateSelectedShape() {
		GeneralShape duplicatedShape = selectedShape.duplicate();
        shapes.add(duplicatedShape);
        
        java.awt.Rectangle bounds = selectedShape.getBounds();
        int shapeX = (int) bounds.getX();
        int shapeY = (int) bounds.getY();
        int shapeW = (int) bounds.getWidth();
        int shapeH = (int) bounds.getHeight();
        
        // The new shape musn't be created outside of the panel.
        if (!rectangleNotInPanel(shapeX+10, shapeY+10, shapeW, shapeH)) {
        	duplicatedShape.translate(10, 10);
        } else if (!rectangleNotInPanel(shapeX-10, shapeY-10, shapeW, shapeH)) {
        	duplicatedShape.translate(-10, -10);
        }
    	
    	selectedShape = duplicatedShape;
    	selectedShapeIndex = shapes.size()-1;
    	repaintAroundShape(selectedShape, 15, 20);
	}
	
	/**
	 * Deletes the selected shape.
	 */
	public void deleteSelectedShape() {
		if (selectedShape != null) {
			Shape savedShape = selectedShape;
    		stateManager.setNeutralState();
    		cursorManager.setDefaultCursor();
            shapes.remove(selectedShapeIndex);
            
            windowManager.setHasSaved(false);
            
            selectedShape = null;
            selectedShapeIndex = -1;
            repaintAroundShape(savedShape, 5, 10);
        }
	}
	
	
	
	//----- Color -----
	/**
	 * Gets the selected color.
	 * @return The selected color.
	 */
	public Color getSelectedColor() {
		return selectedColor;
	}
	
	/**
	 * Sets the selected color.
	 * @param c The color to set as selected.
	 */
	public void setSelectedColor(Color c) {
		selectedColor = c;
	}
	
	/**
	 * Gets the selected outline color.
	 * @return The selected outline color.
	 */
	public Color getSelectedOutlineColor() {
		return selectedOutlineColor;
	}
	
	/**
	 * Sets the selected outline color.
	 * @param c The color to set as selected.
	 */
	public void setSelectedOutlineColor(Color c) {
		selectedOutlineColor = c;
	}
	
	/**
	 * Applies the selected color to the first shape containing the given point.
	 * @param point The point to apply the color.
	 */
	private void applySelectedColor(Point point) {
        for (int i = shapes.size() - 1; i >= 0; i--) {
        	GeneralShape currentShape = shapes.get(i);
            if (currentShape.contains(point)) {
            	currentShape.setColor(selectedColor);
                repaintAroundShape(currentShape, 0, 0);
                return;
            }
        }
        cursorManager.setDefaultCursor();
        stateManager.setNeutralState();
    }
	
	
	
	//----- Background -----
	/**
	 * Gets the background image.
	 * @return The background image.
	 */
	public  BufferedImage getBackgroundImage() {
		return backgroundImage;
	}
	
	/**
	 * Gets the path of the background image.
	 * @return The path of the background image.
	 */
	public String getBackgroundImagePath() {
		return backgroundImagePath;
	}
	
	/**
	 * Sets the background image and its path.
	 * @param image The background image.
	 * @param path The path of the background image.
	 */
	public void setBackgroundImage(BufferedImage image, String path) {
		backgroundImage = image;
		backgroundImagePath = path;
	}
	
	/**
	 * Changes the background color.
	 */
	public void changeBackgroundColor() {
		selectedColor = JColorChooser.showDialog(this, "Choose a color", Color.BLACK);
        if (selectedColor != null) {
        	setBackground(selectedColor);
        	backgroundImage = null;
        	backgroundImagePath = null;
        }
	}
	
	/**
	 * Changes the background image.
	 */
	public void changeBackgroundImage() {
		JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                backgroundImage = ImageIO.read(selectedFile);
                if (backgroundImage == null) { helpText.setText("Please select a valid image."); return; }
                backgroundImagePath = selectedFile.getAbsolutePath();
                backgroundImage = resizeImage(backgroundImage, getWidth(), getHeight());
                repaint();
                helpText.setText("Background set.");
            } catch (IOException e) {
            	helpText.setText("Error: Failed to load image. IOException occurred.");
            }
        }
	}
	
	/**
	 * Resizes the given image to the specified width and height.
	 * @param originalImage The original image to resize.
	 * @param width The new width of the image.
	 * @param height The new height of the image.
	 * @return The resized image.
	 */
	private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        Image tmp = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = resizedImage.getGraphics();
        g.drawImage(tmp, 0, 0, null);
        g.dispose();
        return resizedImage;
    }

	
	
	//----- States -----
	/**
	 * Sets the apply color state with the given color.
	 * @param c The color for the apply color state.
	 */
	public void setApplyColorState(Color c) {
		selectedColor = c;
		stateManager.setApplyColorState();
	}

	/**
	 * Sets the creation state with the specified shape type.
	 * @param type The type of shape to create.
	 */
	public void setCreationState(ShapeType type) {
		stateManager.setCreationState(type);
		if (type == ShapeType.TRIANGLE) pointCollection.clear(); // See warning in the attributes
	}

	/**
	 * Sets the composition state with the specified composition type.
	 * @param type The type of composition to perform.
	 */
	public void setCompositionState(CompositionType type) {
		stateManager.setCompositionState(type);
	}
	
	
	
	//----- Painting -----
	/**
	 * Paints the background.
	 * @param g2d The Graphics2D object.
	 */
	private void paintBackground(Graphics g2d) {
		if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
	}
	
	/**
	 * Paints the stored shapes.
	 * @param g2d The Graphics2D object.
	 */
	private void paintStoredShapes(Graphics2D g2d) {
		for (GeneralShape shape : shapes) {
			// Inside
			Color color = shape.getColor();
	        g2d.setColor(color);
            g2d.fill(shape);
            
            // Outline
            int outlineThickness = shape.getOutlineThickness();
            if (outlineThickness > 0) {
            	color = shape.getOutlineColor();
                g2d.setColor(color);
                
                Stroke savedStroke = g2d.getStroke();
                Stroke outline = new BasicStroke(shape.getOutlineThickness()*2); // Method : to get an intern outline, we double the size of the
                //outline and restraint it to the part inside the shape with clip()
                
                g2d.clip(shape);
                g2d.setStroke(outline);
                
                g2d.draw(shape);
                
                g2d.setClip(null);
    	        g2d.setStroke(savedStroke);
    	        
    	        g2d.draw(shape); // We draw it one last time without clip for a better result, especially with round shapes (clip isn't very precise)
            }
        }
	}
	
	/**
	 * Paints the shape being created.
	 * @param g2d The Graphics2D object.
	 */
	private void paintShapeBeingCreated(Graphics2D g2d) {
		SystemState actualState = stateManager.getActualState();
		ShapeType shapeToCreateType = stateManager.getShapeToCreateType();
		
		if (actualState == SystemState.CREATION) {
            Shape currentShape = null;
            
            switch(shapeToCreateType) {
				case RECTANGLE: {
					if (startPoint != null && endPoint != null && mousePressed) {
						currentShape = new Rectangle(startPoint, endPoint); break;
					} else return;
				}
				case OVAL: {
					if (startPoint != null && endPoint != null && mousePressed) {
						currentShape = new Oval(startPoint, endPoint); break;
					} else return;
				}
				case TRIANGLE: {
					if (pointCollection.size() == 2 && lastTrianglePoint != null) {
						currentShape = new Triangle(pointCollection.get(0), pointCollection.get(1), lastTrianglePoint); break;
					} else return;
				}
				default: break;
            }
            
	        g2d.setColor(Color.BLACK);
        	g2d.fill(currentShape);
        }
	}
	
	/**
	 * Paints the selected shape.
	 * @param g2d The Graphics2D object.
	 */
	private void paintSelectedShape(Graphics2D g2d) {
		if (selectedShape != null) {
            g2d.setColor(Color.RED);
            g2d.draw(selectedShape);
            
            if (stateManager.getActualState() != SystemState.COMPOSITION) {
            	g2d.setColor(Color.BLUE);
            	java.awt.Rectangle bounds = selectedShape.getBounds();
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
	
	/**
	 * Refreshes the panel by painting its components.
	 * @param g The Graphics object.
	 */
	@Override
    protected void paintComponent(Graphics g) {
		super.paintComponent(g);
         
		Graphics2D g2d = (Graphics2D) g;
		paintBackground(g2d);
		paintStoredShapes(g2d);
		paintShapeBeingCreated(g2d);
		paintSelectedShape(g2d);
    }
	
	/**
	 * Repaints around a shape with a margin of topLeftMargin on left and on top and of bottomRightMargin on right and on bottom
	 * Cheaper than repainting the whole canvas
	 * @param shape The shape to repaint around.
	 * @param topLeftMargin The margin on the top and left.
	 * @param bottomRightMargin The margin on the bottom and right.
	 */
	public void repaintAroundShape(Shape shape, int topLeftMargin, int bottomRightMargin) {
		java.awt.Rectangle bounds = shape.getBounds();
        int x = (int) bounds.getX();
        int y = (int) bounds.getY();
        int w = (int) bounds.getWidth();
        int h = (int) bounds.getHeight();
        
        repaint(x-topLeftMargin, y-topLeftMargin, w+bottomRightMargin, h+bottomRightMargin);
	}
	
	
	
	//----- Tests -----
	/**
	 * Checks if the rectangle defined by the given parameters is not within the panel boundaries.
	 * @param x The x-coordinate of the rectangle.
	 * @param y The y-coordinate of the rectangle.
	 * @param w The width of the rectangle.
	 * @param h The height of the rectangle.
	 * @return True if the rectangle is not within the panel boundaries; otherwise, false.
	 */
	private boolean rectangleNotInPanel(int x, int y, int w, int h) {
		int panelX = getX();
	    int panelY = getY();
		int panelW = getWidth();
	    int panelH = getHeight();
	    
		return (x < panelX || y < panelY || x + w > panelX + panelW || y + h > panelY + panelH);
	}
	
	
	
	//----- Reset -----
	/**
	 * Resets the drawing panel to its initial state.
	 */
	public void reset() {
		stateManager.reset();
		cursorManager.setDefaultCursor();
		helpText.setText("");
	    shapes.clear();
	    backgroundImage = null;
		backgroundImagePath = null;
		selectedShape = null;
	    selectedShapeIndex = -1;
	    startPoint = null;
	    endPoint = null;
	    pointCollection.clear();
	    lastTrianglePoint = null;
	    dragStart = null;
	    mouseInShape = new Point();
	    initialW = 0;
	    initialH = 0;
	    selectedColor = null;
	    mousePressed = false;
	    setBackground(Color.WHITE);
	    windowManager.setHasSaved(true);
	    repaint();
	}

}