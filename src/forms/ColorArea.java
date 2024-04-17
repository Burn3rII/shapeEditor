package forms;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;

public class ColorArea extends Area {
	private Color color = Color.BLACK;

	public ColorArea() {
	}

	public ColorArea(Shape s) {
		super(s);
	}
	
	public ColorArea(Shape s, Color c) {
		super(s);
		color = c;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color c) {
		color = c;
	}
}
