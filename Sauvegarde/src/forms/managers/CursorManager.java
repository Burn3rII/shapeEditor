package forms.managers;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import java.net.URL;

import forms.managers.StateManager.ResizeType;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class CursorManager {
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/
	
	private Component component;
	
	/***************************************************************************
	 * Methods.
	 **************************************************************************/

	public CursorManager(Component component) {
		this.component = component;
	}
	
	public void setDefaultCursor() {
		Cursor customCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		component.setCursor(customCursor);
	}
	
	public void setPaintCursor() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
        URL imageUrl = getClass().getResource("/forms/cursors/paint.png");
        Image image = toolkit.getImage(imageUrl);
        Cursor customCursor = toolkit.createCustomCursor(image, new Point(0, 0), "img");
        component.setCursor(customCursor);
	}
	
	/**
	 * Adapt cursor to mouse location, showing default cursor if point outside the bounds, resize cursors  if on the edges and move cursor if inside.
	 */
	public void adaptCursor(Point p, Rectangle bounds) {
        int x = (int) bounds.getX();
        int y = (int) bounds.getY();
        int w = (int) bounds.getWidth();
        int h = (int) bounds.getHeight();
        
        if (p.x > x + 10 && p.x < x + w - 10 && p.y > y + 10 && p.y < y + h - 10) {
        	Cursor customCursor = new Cursor(Cursor.MOVE_CURSOR);
        	component.setCursor(customCursor);
        } else if (p.x > x - 10 && p.x < x + 10 && p.y > y - 10 && p.y < y + 10) {
        	Cursor customCursor = new Cursor(Cursor.NW_RESIZE_CURSOR);
        	component.setCursor(customCursor);
        } else if (p.x > x + w - 10 && p.x < x + w + 10 && p.y > y - 10 && p.y < y + 10) {
        	Cursor customCursor = new Cursor(Cursor.NE_RESIZE_CURSOR);
        	component.setCursor(customCursor);
        } else if (p.x > x - 10 && p.x < x + 10 && p.y > y + h - 10 && p.y < y + h + 10) {
        	Cursor customCursor = new Cursor(Cursor.SW_RESIZE_CURSOR);
        	component.setCursor(customCursor);
        } else if (p.x > x + w - 10 && p.x < x + w + 10 && p.y > y + h - 10 && p.y < y + h + 10) {
        	Cursor customCursor = new Cursor(Cursor.SE_RESIZE_CURSOR);
        	component.setCursor(customCursor);
        } else if (p.x >= x - 10 && p.x <= x + 10 && p.y >= y && p.y <= y + h) {
        	Cursor customCursor = new Cursor(Cursor.W_RESIZE_CURSOR);
        	component.setCursor(customCursor);
        } else if (p.x >= x + w - 10 && p.x <= x + w + 10 && p.y >= y && p.y <= y + h) {
        	Cursor customCursor = new Cursor(Cursor.E_RESIZE_CURSOR);
        	component.setCursor(customCursor);
        } else if (p.x >= x && p.x <= x + w && p.y >= y - 10 && p.y <= y + 10) {
        	Cursor customCursor = new Cursor(Cursor.N_RESIZE_CURSOR);
        	component.setCursor(customCursor);
        } else if (p.x >= x && p.x <= x + w && p.y >= y + h - 10 && p.y <= y + h + 10) {
        	Cursor customCursor = new Cursor(Cursor.S_RESIZE_CURSOR);
        	component.setCursor(customCursor);
        } else {
        	setDefaultCursor();
        }
	}

	/**
	 * Adapt resize cursor to resizeType.
	 */
	public void adaptCursor(ResizeType resizeType) {
		Cursor customCursor;
		switch(resizeType) {
			case TOP_LEFT: 
				customCursor = new Cursor(Cursor.NW_RESIZE_CURSOR);
	        	component.setCursor(customCursor);
				break;
			case TOP_RIGHT: 
				customCursor = new Cursor(Cursor.NE_RESIZE_CURSOR);
	        	component.setCursor(customCursor);
				break;
			case BOTTOM_LEFT: 
				customCursor = new Cursor(Cursor.SW_RESIZE_CURSOR);
	        	component.setCursor(customCursor);
				break;
			case BOTTOM_RIGHT:
				customCursor = new Cursor(Cursor.SE_RESIZE_CURSOR);
	        	component.setCursor(customCursor);
				break;
			case LEFT: 
				customCursor = new Cursor(Cursor.W_RESIZE_CURSOR);
	        	component.setCursor(customCursor);
				break;
			case RIGHT: 
				customCursor = new Cursor(Cursor.E_RESIZE_CURSOR);
	        	component.setCursor(customCursor);
				break;
			case TOP: 
				customCursor = new Cursor(Cursor.N_RESIZE_CURSOR);
	        	component.setCursor(customCursor);
				break;
			case BOTTOM: 
				customCursor = new Cursor(Cursor.S_RESIZE_CURSOR);
	        	component.setCursor(customCursor);
				break;
		}
	}
	
}
