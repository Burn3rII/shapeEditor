package forms.menus;

import forms.shapes.GeneralShape;
import forms.windowsContents.Panel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JColorChooser;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class PopupMenu extends JPopupMenu {
	
	private static final long serialVersionUID = 1L;
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/
	
	private Panel panel;
	
	/***************************************************************************
	 * Constructors.
	 **************************************************************************/
	
	/**
     * Constructs a PopupMenu with the specified panel.
     * @param panel The associated panel.
     */
	public PopupMenu(Panel panel) {
        this.panel = panel;
        initialize();
    }
	
	/***************************************************************************
	 * Methods.
	 **************************************************************************/

	/**
     * Initializes the components of the popup menu.
     */
    private void initialize() {
        JMenuItem mntmColor = new JMenuItem("Color");
        mntmColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.repaint();
                panel.setSelectedColor(JColorChooser.showDialog(panel, "Choose a color", Color.BLACK));
                
                Color selectedColor = panel.getSelectedColor();
                GeneralShape selectedShape = panel.getSelectedShape();
                
                if (selectedColor != null) {
                	selectedShape.setColor(selectedColor);
                    panel.repaintAroundShape(selectedShape, 0, 0);
                }
            }
        });
        add(mntmColor);
        
        JMenu mnOutline = new JMenu("Outline");
        add(mnOutline);

        JMenuItem mntmOutlineColor = new JMenuItem("Color");
        mntmOutlineColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.repaint();
            	panel.setSelectedOutlineColor(JColorChooser.showDialog(panel, "Choose a color", Color.BLACK));
                
                Color selectedColor = panel.getSelectedOutlineColor();
                
                if (selectedColor != null) {
                	GeneralShape selectedShape = panel.getSelectedShape();
                	selectedShape.setOutlineColor(selectedColor);
                    panel.repaintAroundShape(selectedShape, 5, 5);
                }
            }
        });
        mnOutline.add(mntmOutlineColor);

        JMenuItem mntmOutlineThickness = new JMenuItem("Thickness");
        mntmOutlineThickness.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.repaint();
            	JSpinner spinner = new JSpinner(new SpinnerNumberModel(panel.getSelectedShape().getOutlineThickness(), 0, 10, 1));
                
                int option = JOptionPane.showOptionDialog(null, spinner, "Choose a thickness between 0 and 10 :", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                
                if (option == JOptionPane.OK_OPTION) {
                	GeneralShape selectedShape = panel.getSelectedShape();
                    int thickness = (int) spinner.getValue();
                    selectedShape.setOutlineThickness(thickness);
                    panel.repaintAroundShape(selectedShape, 5, 5);
                }
            }
        });
        mnOutline.add(mntmOutlineThickness);
        
        JMenu mnDepth = new JMenu("Depth");
        add(mnDepth);

        JMenuItem mntmForground = new JMenuItem("Foreground");
        mntmForground.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.repaint();
                panel.adjustSelectedShapeDepth(Panel.DepthAction.FORGROUND);
            }
        });
        mnDepth.add(mntmForground);

        JMenuItem mntmForward = new JMenuItem("Forward");
        mntmForward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.repaint();
            	panel.adjustSelectedShapeDepth(Panel.DepthAction.FORWARD);
            }
        });
        mnDepth.add(mntmForward);

        JMenuItem mntmBackward = new JMenuItem("Backward");
        mntmBackward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.repaint();
            	panel.adjustSelectedShapeDepth(Panel.DepthAction.BACKWARD);
            }
        });
        mnDepth.add(mntmBackward);

        JMenuItem mntmBackground = new JMenuItem("Background");
        mntmBackground.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.repaint();
            	panel.adjustSelectedShapeDepth(Panel.DepthAction.BACKGROUND);
            }
        });
        mnDepth.add(mntmBackground);
        
        JMenu mnSymmetry = new JMenu("Symmetry");
        add(mnSymmetry);
        
        JMenuItem mntmXAxis = new JMenuItem("X Axis");
        mntmXAxis.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.repaint();
            	panel.symmetryOnSelectedShape(true);
            }
        });
        mnSymmetry.add(mntmXAxis);

        JMenuItem mntmYAxis = new JMenuItem("Y Axis");
        mntmYAxis.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.repaint();
            	panel.symmetryOnSelectedShape(false);
            }
        });
        mnSymmetry.add(mntmYAxis);

        JMenuItem mntmDuplicate = new JMenuItem("Duplicate");
        mntmDuplicate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.repaint();
            	panel.duplicateSelectedShape();
            }
        });
        add(mntmDuplicate);

        JMenuItem mntmDelete = new JMenuItem("Delete");
        mntmDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.repaint();
            	panel.deleteSelectedShape();
            }
        });
        add(mntmDelete);
    }
    
}
