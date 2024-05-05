package forms.shapes;

import java.awt.Point;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class Oval extends GeneralShape {
	
	private static final long serialVersionUID = 1L;
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/
	
	private double x, y, width, height;
	
	/***************************************************************************
	 * Constructors.
	 **************************************************************************/

	/**
     * Constructs an oval with the specified coordinates and dimensions.
     * @param x The x-coordinate of the upper-left corner of the framing rectangle of the oval.
     * @param y The y-coordinate of the upper-left corner of the framing rectangle of the oval.
     * @param width The width of the oval.
     * @param height The height of the oval.
     */
	public Oval(double x, double y, double width, double height) {
        super();
        
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        // Method found here : https://stackoverflow.com/questions/2172798/how-to-draw-an-oval-in-html5-canvas
        double kappa = 0.5522848; // Here's an explanation for the value of kappa : https://en.wikipedia.org/wiki/Composite_B%C3%A9zier_curve
        double ox = (width/2) * kappa; // control point offset horizontal
        double oy = (height/2) * kappa; // control point offset vertical
        double xe = x + width; // x-end
        double ye = y + height; // y-end
        double xm = x + width/2; // x-middle
        double ym = y + height/2; // y-middle
        
        this.moveTo(x, ym);
        this.curveTo(x, ym - oy, xm - ox, y, xm, y);
        this.curveTo(xm + ox, y, xe, ym - oy, xe, ym);
        this.curveTo(xe, ym + oy, xm + ox, ye, xm, ye);
        this.curveTo(xm - ox, ye, x, ym + oy, x, ym);
        this.closePath();
    }
	
	/**
     * Constructs an oval using two points as diagonally opposite corners of the bounding rectangle.
     * @param start The starting point of the oval.
     * @param end The ending point of the oval.
     */
	public Oval(Point start, Point end) {
		this(Math.min(start.x, end.x), Math.min(start.y, end.y), Math.abs(start.x - end.x), Math.abs(start.y - end.y));
    }
	
	/***************************************************************************
	 * Methods.
	 **************************************************************************/
    
	/**
     * Gets the x-coordinate of the upper-left corner of the bounding rectangle of this oval.
     * @return The x-coordinate of the upper-left corner of the bounding rectangle.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the upper-left corner of the bounding rectangle of this oval.
     * @return The y-coordinate of the upper-left corner of the bounding rectangle.
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the width of the bounding rectangle of this oval.
     * @return The width of the bounding rectangle.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Gets the height of the bounding rectangle of this oval.
     * @return The height of the bounding rectangle.
     */
    public double getHeight() {
        return height;
    }
    
}
