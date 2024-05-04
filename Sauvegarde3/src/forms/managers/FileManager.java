package forms.managers;

import forms.windowsContents.Panel;
import forms.exceptions.NotConnectedException;
import forms.serverrmi.ServerInterface;
import forms.shapes.GeneralShape;

import java.util.ArrayList;
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
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

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
            String filename = selectedFile.getAbsolutePath();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
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

            return true;
        } else {
            return false;
        }
    }
    
    public boolean saveDistantFile(ServerManager serverManager) throws IOException, NotConnectedException, NumberFormatException, NotBoundException, AccessException, RemoteException, Exception  {
        if (!StateManager.getServerState()) {
            throw new NotConnectedException("Application is not connected to the server.");
        }
    	
    	
        String[] serverConfig = serverManager.fetchServerConfig();
        String ip = serverConfig[0];
        int port = Integer.parseInt(serverConfig[1]);
        ServerInterface stub = (ServerInterface) Naming.lookup("rmi://" + ip + ":" + port + "/Server");
        List<String> lines = new ArrayList<>();
        if (panel.getBackgroundImage() == null) { // No image is used as a background
        	lines.add("Background_Color: " + ((Color) panel.getBackground()).getRGB());
        } else {
        	lines.add("Background_Image: " + panel.getBackgroundImagePath());
        }

        List<GeneralShape> shapes = panel.getShapes();
        for (GeneralShape shape : shapes) {
        	lines.add(shape.toTextFormat());
        }

        stub.saveDistant(lines.toArray(new String[0]));
        return true;
    	 /*catch (NumberFormatException e1) {
	            System.err.println("Could not parse string to an integer: " + e1.toString());
		        return false;/*
    		}
    		/*
    		int port = Integer.parseInt(serverConfig[1]);
            //ServerInterface stub = (ServerInterface) Naming.lookup("rmi://localhost:1099/SaveDistant");
    		ServerInterface stub = (ServerInterface) Naming.lookup("rmi://" + ip + ":" + port + "/Server");
    		
            List<String> lines = new ArrayList<>();
            if (panel.getBackgroundImage() == null) { // No image is used as a background
            	lines.add("Background_Color: " + ((Color) panel.getBackground()).getRGB());
            } else {
            	lines.add("Background_Image: " + panel.getBackgroundImagePath());
            }

            List<GeneralShape> shapes = panel.getShapes();
            for (GeneralShape shape : shapes) {
            	lines.add(shape.toTextFormat());
            }

            stub.saveDistant(lines.toArray(new String[0]));
            return true;
    		*/

    	/*} catch (NotBoundException e) {
            System.err.println("The specified name is not bound in the RMI registry: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (AccessException e) {
            System.err.println("Access to the required operation is denied: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (RemoteException e) {
            System.err.println("Remote method invocation failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
            return false;
        }*/
    }

    /**
     * Opens a file and loads its content into the panel.
     * @return True if the user has selected a file, false otherwise.
     * @throws Exception If an exception occurs during file reading.
     * @throws FileNotFoundException If the file to open is not found.
     * @throws IOException If an I/O exception occurs during file reading.
     */
    public boolean openFile() throws Exception, FileNotFoundException, IOException, NumberFormatException {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);

        // Check if the user has selected a file
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            panel.reset();

            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
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

            return true;
        } else {
            return false;
        }
    }
    
    public boolean openDistantFile(ServerManager serverManager) throws Exception, FileNotFoundException, IOException {
        if (!StateManager.getServerState()) {
            throw new NotConnectedException("Application is not connected to the server.");
        }
    	
    	try {
    		String[] serverConfig = serverManager.fetchServerConfig();
    		String ip = serverConfig[0];
    		int port = Integer.parseInt(serverConfig[1]); 
            //ServerInterface stub = (ServerInterface) Naming.lookup("rmi://localhost:1099/SaveDistant");
    		ServerInterface stub = (ServerInterface) Naming.lookup("rmi://" + ip + ":" + port + "/Server");

            
            String[] lines = stub.loadDistant();
            if (lines == null || lines.length == 0) {
                throw new FileNotFoundException("File not found on the server.");
            }
            
            panel.reset();

            for (String line : lines) {
                if (line.startsWith("Background_Color: ")) {
                	try {
                        int rgb = Integer.parseInt(line.substring(18));
                        Color bgColor = new Color(rgb);
                        panel.setBackground(bgColor);
                	} catch (NumberFormatException e1) {
    		            System.err.println("Could not parse string to an integer: " + e1.toString());
    			        return false;
                	}

                } else if (line.startsWith("Background_Image: ")) {
                    String imagePath = line.substring(18);
                    BufferedImage backgroundImage = ImageIO.read(new File(imagePath));
                    if (backgroundImage != null) { 
                        panel.setBackgroundImage(backgroundImage, imagePath); 
                    } else panel.setBackground(Color.WHITE);
                } else {
                    GeneralShape shape = GeneralShape.fromTextFormat(line);
                    if (shape != null) {
                        panel.addShape(shape);
                    }
                }
            }
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("File not found on the server.");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
            return false;
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
