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
