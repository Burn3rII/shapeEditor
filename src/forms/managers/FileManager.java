package forms.managers;

import forms.windowsContents.Panel;
import forms.shapes.GeneralShape;

import java.util.List;

import javax.swing.JFileChooser;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
	 * Constructors.
	 **************************************************************************/
    
    /**
     * Constructor for FileManager class.
     * @param panel The panel whose content is managed by the file manager.
     */
    public FileManager(Panel panel) {
        this.panel = panel;
    }
    
    /***************************************************************************
	 * Methods.
	 **************************************************************************/
    
    /**
     * Saves the content of the panel to a file.
     * @return True if the user has selected a location to save the file, false otherwise.
     * @throws Exception If an exception occurs during file writing.
     * @throws IOException If an I/O exception occurs during file writing.
     */
    public boolean saveFile() throws Exception, IOException {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(null);

        // Check if the user has selected a backup location
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getAbsolutePath();

            writeFile(fileName);

            return true;
        } else {
            return false;
        }
    }
    
    public void writeFile(String fileName) throws IOException {
    	try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            if (panel.getBackgroundImage() == null) { // No image is used as a background
                writer.write("Background_Color: " + ((Color) panel.getBackground()).getRGB());
            } else {
                writer.write("Background_Image: " + panel.getBackgroundImagePath());
            }
            writer.newLine();

            List<GeneralShape> shapes = panel.getShapes();
            for (GeneralShape shape : shapes) {
                writer.write(shape.toTextFormat());
                writer.newLine();
            }
        }
    }

    /**
     * Opens a file and loads its content into the panel.
     * @return True if the user has selected a file, false otherwise.
     * @throws Exception If an exception occurs during file reading.
     * @throws FileNotFoundException If the file to open is not found.
     * @throws IOException If an I/O exception occurs during file reading.
     */
    public boolean openFile() throws FileNotFoundException, IOException {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);

        // Check if the user has selected a file
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            restoreFile(selectedFile);
            return true;
        } else {
            return false;
        }
    }
    
    public void restoreFile(File file) throws IOException {
    	try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
    		panel.reset();
    		
        	// Background
            String bgLine = reader.readLine();
            if (bgLine != null) {
                if (bgLine.startsWith("Background_Color: ")) {
                    int rgb = Integer.parseInt(bgLine.substring(18));
                    Color bgColor = new Color(rgb);
                    panel.setBackground(bgColor);
                } else if (bgLine.startsWith("Background_Image: ")) {
                    String imagePath = bgLine.substring(18);
                    BufferedImage backgroundImage = ImageIO.read(new File(imagePath));
                    if (backgroundImage != null) { 
                    	panel.setBackgroundImage(backgroundImage, imagePath); 
                    } else panel.setBackground(Color.WHITE);
                }
            }

            // Shapes
            String line;
            while ((line = reader.readLine()) != null) {
                GeneralShape shape = GeneralShape.fromTextFormat(line);
                if (shape != null) {
                    panel.addShape(shape);
                }
            }
        }
    }
    
    /**
     * Saves the content of the panel as an image file.
     * @return True if the user has selected a file, false otherwise.
     * @throws IOException If an I/O exception occurs during image file writing.
     */
    public boolean saveAsImage() throws IOException {
    	JFileChooser fileChooser = new JFileChooser();
    	
    	fileChooser.setSelectedFile(new File("myImage.png")); // Default name and extension

        int result = fileChooser.showSaveDialog(null);

        // Check if the user has selected a file
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            String fileName = file.getName();
            String extension = "";
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i + 1);
            }

            if (extension.isEmpty()) {
                extension = "PNG"; // Default extension if no extension
                file = new File(file.getPath() + ".png");
            }

            BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);

            // Draw content of the panel on the image
            Graphics2D g2d = image.createGraphics();
            panel.paint(g2d);
            g2d.dispose();

            ImageIO.write(image, extension, file);
            
            return true;
        } else {
	        return false;
	    }
    }
}
