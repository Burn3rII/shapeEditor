package forms.shapes;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class ColorArea extends Area {
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/
	
	private Color color = Color.BLACK;
	
	/***************************************************************************
	 * Methods.
	 **************************************************************************/

	/**
	 * Default constructor which creates an empty area.
	 */
	public ColorArea() {
	}

	/**
	 * Constructor which creates an area with the shape s.
	 */
	public ColorArea(Shape s) {
		super(s);
	}
	
	/**
	 * Constructor which creates an area with the shape s and the color c.
	 */
	public ColorArea(Shape s, Color c) {
		super(s);
		color = c;
	}
	
	/**
	 * Returns the color of the shape.
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * Set the color of the shape.
	 */
	public void setColor(Color c) {
		color = c;
	}
	
}
