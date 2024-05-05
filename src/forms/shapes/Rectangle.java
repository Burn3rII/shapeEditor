package forms.shapes;

import java.awt.Point;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class Rectangle extends GeneralShape {

	private static final long serialVersionUID = 1L;
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/
	
	private double x, y, width, height;
	
	/***************************************************************************
	 * Constructors.
	 **************************************************************************/

	/**
     * Constructs a rectangle with the specified coordinates and dimensions.
     * @param x The x-coordinate of the upper-left corner of the rectangle.
     * @param y The y-coordinate of the upper-left corner of the rectangle.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     */
	public Rectangle(double x, double y, double width, double height) {
    	super();
    	
    	this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        this.moveTo(x, y);
        this.lineTo(x + width, y);
        this.lineTo(x + width, y + height);
        this.lineTo(x, y + height);
        this.closePath();
    }
	
	/**
     * Constructs a rectangle using two points as diagonally opposite corners of the rectangle.
     * @param start The starting point of the rectangle.
     * @param end The ending point of the rectangle.
     */
	public Rectangle(Point start, Point end) {
		this(Math.min(start.x, end.x), Math.min(start.y, end.y), Math.abs(start.x - end.x), Math.abs(start.y - end.y));
    }
	
	
	/***************************************************************************
	 * Methods.
	 **************************************************************************/
    
	/**
     * Gets the x-coordinate of the upper-left corner of the rectangle.
     * @return The x-coordinate of the upper-left corner.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the upper-left corner of the rectangle.
     * @return The y-coordinate of the upper-left corner.
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the width of the rectangle.
     * @return The width of the rectangle.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Gets the height of the rectangle.
     * @return The height of the rectangle.
     */
    public double getHeight() {
        return height;
    }
    
}