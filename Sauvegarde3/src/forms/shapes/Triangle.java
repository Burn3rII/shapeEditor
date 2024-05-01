package forms.shapes;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class Triangle extends GeneralShape {

	private static final long serialVersionUID = 1L;
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/
	
	private List<Point> points = new ArrayList<>();
	
	/***************************************************************************
	 * Constructors.
	 **************************************************************************/

	/**
     * Constructs a triangle with the specified three points.
     * @param point1 The first point of the triangle.
     * @param point2 The second point of the triangle.
     * @param point3 The third point of the triangle.
     */
	public Triangle(Point point1, Point point2, Point point3) {
    	super();

    	points.add(point1); points.add(point2); points.add(point3);
    	
    	this.moveTo(point1.x, point1.y);
    	this.lineTo(point2.x, point2.y);
    	this.lineTo(point3.x, point3.y);
        this.closePath();
    }
	
	
	/***************************************************************************
	 * Methods.
	 **************************************************************************/
    
	/**
     * Gets the list of points that define the triangle.
     * @return The list of points.
     */
    public List<Point> getPoints() {
        return points;
    }
    
}