package forms.managers;

import forms.windowsContents.Panel;

import java.awt.Point;

public class StateManager {
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/
	
    //----- Objects -----
    private Panel panel;
    private CursorManager cursorManager;
	
	//----- States -----
    public enum SystemState {
		NEUTRAL,
		CREATION,
		MOVE,
		RESIZE,
		COMPOSITION,
		APPLYCOLOR
	}
	private SystemState actualState = SystemState.NEUTRAL;
	
	public enum ResizeType {
    	TOP_LEFT,
    	TOP_RIGHT,
    	BOTTOM_LEFT,
    	BOTTOM_RIGHT,
    	LEFT,
    	RIGHT,
    	TOP,
    	BOTTOM
	}
	private ResizeType resizeType = null;
    
    public enum CompositionType {
    	UNION,
    	INTERSECTION,
    	DIFFERENCE,
    	SYMETRICDIFFERENCE
	}
    private CompositionType compositionType = null;
    
    public enum ShapeType {
        RECTANGLE,
        OVAL,
        TRIANGLE
    }
    private ShapeType shapeToCreateType = null;
    
    private static boolean serverState = false;
    
    /***************************************************************************
	 * Constructors.
	 **************************************************************************/
    
    /**
     * Constructor for StateManager class.
     * @param panel The associated panel.
     * @param cursorManager The associated cursor manager.
     */
    public StateManager(Panel panel, CursorManager cursorManager) {
		this.panel = panel;
		this.cursorManager = cursorManager;
	}
    
    /***************************************************************************
	 * Methods.
	 **************************************************************************/

    /**
     * Gets the current system state.
     * @return The current system state.
     */
	public SystemState getActualState() {
		return actualState;
	}
	
	/**
     * Sets the system state to neutral.
     */
	public void setNeutralState() {
		actualState = SystemState.NEUTRAL;
	}
	
	/**
     * Sets the system state to creation mode and specifies the type of shape to create.
     * @param type The type of shape to create.
     */
	public void setCreationState(ShapeType type) {
		actualState = SystemState.CREATION;
		shapeToCreateType = type;
		cursorManager.setDefaultCursor();
		panel.deselectSelectedShape();
		panel.repaint();
	}
	
	/**
     * Sets the system state to move mode and initializes the moving parameters.
     * @param p The mouse point.
     * @param x The x-coordinate of the shape that has to be moved.
     * @param y The y-coordinate of the shape that has to be moved.
     */
	public void setMoveState(Point p, int x, int y) {
		actualState = SystemState.MOVE;
    	panel.getMouseInShape().setLocation(p.x - x, p.y - y);
    	panel.setDragStart(p);
	}
	
	/**
     * Sets the system state to resize mode and initializes the resizing parameters.
     * @param p The mouse point.
     * @param x The x-coordinate of the shape that has to be resized.
     * @param y The y-coordinate of the shape that has to be resized.
     * @param w The width of the shape that has to be resized.
     * @param h The height of the shape that has to be resized.
     */
	public void setResizeState(Point p, int x, int y, int w, int h) {
		actualState = SystemState.RESIZE;
		panel.getMouseInShape().setLocation(p.x - x, p.y - y);
    	panel.setInitialW(w);
    	panel.setInitialH(h);
    	panel.setDragStart(p);

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
	}
	
	/**
     * Checks if the resize type needs to be flipped.
     * @param dx The change in x-coordinate.
     * @param dy The change in y-coordinate.
     * @param shapeW The width of the shape.
     * @param shapeH The height of the shape.
     * @return True if the resize type is flipped, false otherwise.
     */
	public boolean checkResizeTypeFlip(double dx, double dy, double shapeW, double shapeH) {
		boolean stateFlipped = false;
		
		if (dx >= shapeW) {
			switch(resizeType) {
				case TOP_LEFT: resizeType = ResizeType.TOP_RIGHT; stateFlipped = true; break;
				case LEFT: resizeType = ResizeType.RIGHT; stateFlipped = true; break;
				case BOTTOM_LEFT: resizeType = ResizeType.BOTTOM_RIGHT; stateFlipped = true; break;
				default: break;
			}
		} else if (-dx >= shapeW) {
			switch(resizeType) {
				case TOP_RIGHT: resizeType = ResizeType.TOP_LEFT; stateFlipped = true; break;
				case RIGHT: resizeType = ResizeType.LEFT; stateFlipped = true; break;
				case BOTTOM_RIGHT: resizeType = ResizeType.BOTTOM_LEFT; stateFlipped = true; break;
				default: break;
			}
		}
		
		if (dy >= shapeH) {
			switch(resizeType) {
				case TOP_LEFT: resizeType = ResizeType.BOTTOM_LEFT; stateFlipped = true; break;
				case TOP: resizeType = ResizeType.BOTTOM; stateFlipped = true; break;
				case TOP_RIGHT: resizeType = ResizeType.BOTTOM_RIGHT; stateFlipped = true; break;
				default: break;
			}
		} else if (-dy >= shapeH) {
			switch(resizeType) {
				case BOTTOM_LEFT: resizeType = ResizeType.TOP_LEFT; stateFlipped = true; break;
				case BOTTOM: resizeType = ResizeType.TOP; stateFlipped = true; break;
				case BOTTOM_RIGHT: resizeType = ResizeType.TOP_RIGHT; stateFlipped = true; break;
				default: break;
			}
		}
		
		return stateFlipped;
	}
	
	/**
     * Sets the system state to composition mode and specifies the type of composition operation.
     * @param type The type of composition operation.
     */
	public void setCompositionState(CompositionType type) {
		actualState = SystemState.COMPOSITION;
		compositionType = type;
	}

	/**
     * Sets the system state to apply color mode.
     */
	public void setApplyColorState() {
		actualState = SystemState.APPLYCOLOR;
    	cursorManager.setPaintCursor();
    	panel.deselectSelectedShape();
    	panel.repaint();
	}
	
	/**
     * Gets the type of resizing operation.
     * @return The type of resizing operation.
     */
	public ResizeType getResizeType() {
		return resizeType;
	}

	/**
     * Gets the type of composition operation.
     * @return The type of composition operation.
     */
	public CompositionType getCompositionType() {
		return compositionType;
	}

	/**
     * Gets the type of shape to create.
     * @return The type of shape to create.
     */
	public ShapeType getShapeToCreateType() {
		return shapeToCreateType;
	}

	public static boolean getServerState() {
		return serverState;
	}

	public static void setServerState(boolean serverEnabled) {
		serverState = serverEnabled;
	}

	/**
     * Resets the state manager to its initial state.
     */
	public void reset() {
		setNeutralState();
		resizeType = null;
		compositionType = null;
		shapeToCreateType = null;
	}
	
}
