package forms.shapes;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;

import java.util.StringTokenizer;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class GeneralShape extends Path2D.Double {

	private static final long serialVersionUID = 1L;
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/
	
	protected Color color = Color.BLACK;
	
	/***************************************************************************
	 * Constructors.
	 **************************************************************************/
	
	/**
     * Constructs a GeneralShape with an empty path and black color.
     */
	public GeneralShape () {
		super();
	}
	
	/**
     * Constructs a GeneralShape with the specified Shape and black color.
     * @param s The Shape to construct the GeneralShape from.
     */
	public GeneralShape(Shape s) {
		super(s);
	}
	
	/**
     * Constructs a GeneralShape with the specified Shape and color.
     * @param s The Shape to construct the GeneralShape from.
     * @param c The color of the GeneralShape.
     */
	public GeneralShape(Shape s, Color c) {
		super(s);
		color = c;
	}
	
	/***************************************************************************
	 * Methods.
	 **************************************************************************/

	/**
     * Translates the shape by the specified distances.
     * @param dx The distance to translate along the x-axis.
     * @param dy The distance to translate along the y-axis.
     */
    public void translate(double dx, double dy) {
        AffineTransform tx = AffineTransform.getTranslateInstance(dx, dy);
        this.transform(tx);
    }

    /**
     * Rotates the shape by the specified angle around the specified point.
     * @param angle The angle of rotation in degrees.
     * @param x The x-coordinate of the rotation point.
     * @param y The y-coordinate of the rotation point.
     */
    public void rotate(double angle, double x, double y) {
        AffineTransform tx = AffineTransform.getRotateInstance(Math.toRadians(angle), x, y);
        this.transform(tx);
    }

    /**
     * Scales the shape by the specified scaling factors.
     * @param sx The scaling factor along the x-axis.
     * @param sy The scaling factor along the y-axis.
     */
    public void scale(double sx, double sy) {
        AffineTransform tx = AffineTransform.getScaleInstance(sx, sy);
        this.transform(tx);
    }
    
    /**
     * Reflects the shape across the x-axis or y-axis.
     * @param xAxis If true, reflects the shape across the x-axis; otherwise, across the y-axis.
     */
    public void reflect(boolean xAxis) {
        AffineTransform reflectTransform;
        if (xAxis) {
            reflectTransform = AffineTransform.getScaleInstance(1, -1);
            reflectTransform.translate(0, -(2*this.getBounds2D().getY() + this.getBounds2D().getHeight()));
        } else {
            reflectTransform = AffineTransform.getScaleInstance(-1, 1);
            reflectTransform.translate(-(2*this.getBounds2D().getX() + this.getBounds2D().getWidth()), 0);
        }
        this.transform(reflectTransform);
    }

    /**
     * Gets the area of the shape.
     * @return The Area object representing the shape's area.
     */
    public Area getArea() {
        return new Area(this);
    }
    
    /**
     * Gets the color of the shape.
     * @return The color of the shape.
     */
	public Color getColor() {
		return color;
	}
	
	/**
     * Sets the color of the shape.
     * @param c The color to set for the shape.
     */
	public void setColor(Color c) {
		color = c;
	}
	
	/**
     * Adds the specified shape to this shape.
     * @param other The shape to add.
     */
	public void add(GeneralShape other) {
		Area area1 = new Area(this);
        Area area2 = new Area(other);
        area1.add(area2);
        this.reset();
        this.append(area1.getPathIterator(null), true);
	}

	/**
     * Intersects this shape with the specified shape.
     * @param other The shape to intersect with.
     */
	public void intersect(GeneralShape other) {
	    Area area1 = new Area(this);
	    Area area2 = new Area(other);
	    area1.intersect(area2);
	    this.reset();
        this.append(area1.getPathIterator(null), true);
	}

	/**
     * Subtracts the specified shape from this shape.
     * @param other The shape to subtract.
     */
	public void subtract(GeneralShape other) {
	    Area area1 = new Area(this);
	    Area area2 = new Area(other);
	    area1.subtract(area2);
	    this.reset();
        this.append(area1.getPathIterator(null), true);
	}
	
	/**
     * Computes the exclusive OR of this shape and the specified shape.
     * @param other The other shape for the exclusive OR.
     */
	public void exclusiveOr(GeneralShape other) {
	    Area area1 = new Area(this);
	    Area area2 = new Area(other);
	    area1.exclusiveOr(area2);
	    this.reset();
        this.append(area1.getPathIterator(null), true);
	}
	
	/**
     * Converts the shape to a textual representation.
     * @return A string representing the shape's data in a text format.
     */
    public String toTextFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("GeneralShape;");
        sb.append(color.getRGB()).append(";");
        // Append the path coordinates
        PathIterator iterator = getPathIterator(null);
        while (!iterator.isDone()) {
            double[] coords = new double[6];
            int type = iterator.currentSegment(coords);
            sb.append(type).append(":");
            for (int i = 0; i < coords.length; i++) {
                sb.append(coords[i]).append(",");
            }
            iterator.next();
            sb.append(";");
        }
        return sb.toString();
    }
    
    /**
     * Constructs a GeneralShape from its textual representation.
     * @param text The text representing the shape's data.
     * @return A GeneralShape object constructed from the text representation, or null if the text is invalid.
     */
    public static GeneralShape fromTextFormat(String text) {
        StringTokenizer tokenizer = new StringTokenizer(text, ";");
        String shapeType = tokenizer.nextToken();
        if (!shapeType.equals("GeneralShape")) {
            return null; // Not a GeneralShape
        }
        int colorRGB = Integer.parseInt(tokenizer.nextToken());
        Color color = new Color(colorRGB);
        GeneralShape shape = new GeneralShape();
        shape.setColor(color);
        Path2D path = new Path2D.Double();
        while (tokenizer.hasMoreTokens()) {
            String segment = tokenizer.nextToken();
            StringTokenizer segmentTokenizer = new StringTokenizer(segment, ":,");
            int type = Integer.parseInt(segmentTokenizer.nextToken());
            double[] coords = new double[6];
            for (int i = 0; i < coords.length; i++) {
                coords[i] = java.lang.Double.parseDouble(segmentTokenizer.nextToken());
            }
            switch (type) {
                case PathIterator.SEG_MOVETO: path.moveTo(coords[0], coords[1]); break;
                case PathIterator.SEG_LINETO: path.lineTo(coords[0], coords[1]); break;
                case PathIterator.SEG_QUADTO: path.quadTo(coords[0], coords[1], coords[2], coords[3]); break;
                case PathIterator.SEG_CUBICTO: path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]); break;
                case PathIterator.SEG_CLOSE: path.closePath(); break;
            }
        }
        shape.append(path, false);
        return shape;
    }
	
}
