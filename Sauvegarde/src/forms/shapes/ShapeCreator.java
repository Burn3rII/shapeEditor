package forms.shapes;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class ShapeCreator {
	
	/***************************************************************************
	 * Methods.
	 **************************************************************************/
	
	public Rectangle createRectangle(Point start, Point end) {
        int x = Math.min(start.x, end.x);
        int y = Math.min(start.y, end.y);
        int width = Math.abs(start.x - end.x);
        int height = Math.abs(start.y - end.y);
        return new Rectangle(x, y, width, height);
    }
	
	public Ellipse2D.Double createOval(Point start, Point end) {
        int x = Math.min(start.x, end.x);
        int y = Math.min(start.y, end.y);
        int width = Math.abs(start.x - end.x);
        int height = Math.abs(start.y - end.y);
        return new Ellipse2D.Double(x, y, width, height);
    }
	
}
