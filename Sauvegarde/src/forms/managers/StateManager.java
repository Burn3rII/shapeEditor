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
    	TOP,
    	BOTTOM,
    	LEFT,
    	RIGHT
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
        OVAL
    }
    private ShapeType shapeToCreateType = null;
    
    /***************************************************************************
	 * Methods.
	 **************************************************************************/

	public StateManager(Panel panel, CursorManager cursorManager) {
		this.panel = panel;
		this.cursorManager = cursorManager;
	}
	
	public SystemState getActualState() {
		return actualState;
	}
	
	public void setNeutralState() {
		actualState = SystemState.NEUTRAL;
	}
	
	public void setCreationState(ShapeType type) {
		actualState = SystemState.CREATION;
		shapeToCreateType = type;
		cursorManager.setDefaultCursor();
		panel.deselectSelectedShape();
		panel.repaint();
	}
	
	public void setMoveState(Point p, int x, int y) {
		actualState = SystemState.MOVE;
    	panel.getMouseInShape().setLocation(p.x - x, p.y - y);
    	panel.setDragStart(p);
	}
	
	public void setResizeState(Point p, int x, int y, int w, int h) {
		actualState = SystemState.RESIZE;
		panel.getMouseInShape().setLocation(p.x - x, p.y - y);
    	panel.setInitialW(w);
    	panel.setInitialH(h);

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
        panel.setDragStart(p);
	}
	
	public void flipResizeState(boolean ResizeStateW, boolean ResizeStateH) {
		if (resizeType == ResizeType.BOTTOM_RIGHT && ResizeStateW ){
            resizeType = ResizeType.BOTTOM_LEFT;
        } else if (resizeType == ResizeType.BOTTOM_LEFT && ResizeStateW) {
            resizeType = ResizeType.BOTTOM_RIGHT;
        } else if (resizeType == ResizeType.BOTTOM_RIGHT && ResizeStateH) {
            resizeType = ResizeType.TOP_RIGHT;
        } else if (resizeType == ResizeType.BOTTOM_LEFT && ResizeStateH) {
            resizeType = ResizeType.TOP_LEFT;	            
        } else if (resizeType == ResizeType.TOP_RIGHT && ResizeStateW) {
            resizeType = ResizeType.TOP_LEFT;
        } else if (resizeType == ResizeType.TOP_LEFT && ResizeStateW) {
            resizeType = ResizeType.TOP_RIGHT;
        } else if (resizeType == ResizeType.TOP_RIGHT && ResizeStateH) {
            resizeType = ResizeType.BOTTOM_RIGHT;
        } else if (resizeType == ResizeType.TOP_LEFT && ResizeStateH) {
            resizeType = ResizeType.BOTTOM_LEFT;
        } else if (resizeType == ResizeType.RIGHT && ResizeStateW) {
            resizeType = ResizeType.LEFT;
        } else if (resizeType == ResizeType.LEFT && ResizeStateW) {
            resizeType = ResizeType.RIGHT;
        } else if (resizeType == ResizeType.BOTTOM && ResizeStateH) {
            resizeType = ResizeType.TOP;
        } else if (resizeType == ResizeType.TOP && ResizeStateH) {
            resizeType = ResizeType.BOTTOM;
        }
	}
	
	public void setCompositionState(CompositionType type) {
		actualState = SystemState.COMPOSITION;
		compositionType = type;
	}

	public void setApplyColorState() {
		actualState = SystemState.APPLYCOLOR;
    	cursorManager.setPaintCursor();
    	panel.deselectSelectedShape();
    	panel.repaint();
	}
	
	public ResizeType getResizeType() {
		return resizeType;
	}

	public CompositionType getCompositionType() {
		return compositionType;
	}

	public ShapeType getShapeToCreateType() {
		return shapeToCreateType;
	}

	public void reset() {
		setNeutralState();
		resizeType = null;
		compositionType = null;
		shapeToCreateType = null;
	}
	
}
