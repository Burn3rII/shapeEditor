package forms.managers;


import forms.shapes.ColorArea;
import forms.windowsContents.Panel;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import javax.swing.JFileChooser;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class FileManager {
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/
	
	private Panel panel;
	
	/***************************************************************************
	 * Methods.
	 **************************************************************************/
    
	public FileManager(Panel panel) {
		this.panel = panel;
	}
	
	public boolean openFile() throws IOException, ClassNotFoundException {
		JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null); 
        
        // Check if the user has selected a file
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Deserialization
            FileInputStream file = new FileInputStream(selectedFile);
            ObjectInputStream in = new ObjectInputStream(file);

            panel.reset();
            while (true) { // Read all objects from file
                try {
                	@SuppressWarnings("unchecked")
                	Entry<Shape,Color> object = (Entry<Shape,Color>) in.readObject();
                	Shape shape = object.getKey();
                	Color color = object.getValue();
                	
                    ColorArea objectRegion = new ColorArea(shape, color);
                    panel.addArea(objectRegion);
                } catch (EOFException ex) { // End of file
                    break;
                }
            }

            in.close();
            file.close();
            return true;
        } else return false;
	}
	
	public boolean saveFile() throws IOException {
		JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(null);

        // Check if the user has selected a backup location
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filename = selectedFile.getAbsolutePath();

            // Serialization
            FileOutputStream file = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(file);

            for (Shape shape : panel.getAreas()) { // Save each object in the file
                Shape s = AffineTransform.getTranslateInstance(0,0).createTransformedShape(shape); // Enables an Area to be serialized by creating a Shape.
                Color c = ((ColorArea) shape).getColor();

                Entry<Shape,Color> object=new SimpleEntry<>(s,c);
                
                out.writeObject(object);
            }

            out.close();
            file.close();
            return true;
        } else return false;
	}
	
}
