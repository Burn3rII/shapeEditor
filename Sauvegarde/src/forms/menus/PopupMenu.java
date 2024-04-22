package forms.menus;

import forms.shapes.ColorArea;
import forms.windowsContents.Panel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JColorChooser;

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
	 * Methods.
	 **************************************************************************/

    public PopupMenu(Panel panel) {
        this.panel = panel;
        initialize();
    }

    private void initialize() {
        JMenuItem mntmColor = new JMenuItem("Color");
        mntmColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                panel.setSelectedColor(JColorChooser.showDialog(panel, "Choose a color", Color.BLACK));
                
                Color selectedColor = panel.getSelectedColor();
                ColorArea selectedShape = panel.getSelectedShape();
                
                if (selectedColor != null) {
                	selectedShape.setColor(selectedColor);
                    panel.repaintAroundShape(selectedShape, 0, 0);
                }
            }
        });
        add(mntmColor);

        JMenu mnDepth = new JMenu("Depth");
        add(mnDepth);

        JMenuItem mntmForground = new JMenuItem("Foreground");
        mntmForground.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                panel.adjustSelectedShapeDepth(Panel.DepthAction.FORGROUND);
            }
        });
        mnDepth.add(mntmForground);

        JMenuItem mntmForward = new JMenuItem("Forward");
        mntmForward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.adjustSelectedShapeDepth(Panel.DepthAction.FORWARD);
            }
        });
        mnDepth.add(mntmForward);

        JMenuItem mntmBackward = new JMenuItem("Backward");
        mntmBackward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.adjustSelectedShapeDepth(Panel.DepthAction.BACKWARD);
            }
        });
        mnDepth.add(mntmBackward);

        JMenuItem mntmBackground = new JMenuItem("Background");
        mntmBackground.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.adjustSelectedShapeDepth(Panel.DepthAction.BACKGROUND);
            }
        });
        mnDepth.add(mntmBackground);

        JMenuItem mntmDuplicate = new JMenuItem("Duplicate");
        mntmDuplicate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.duplicateSelectedShape();
            }
        });
        add(mntmDuplicate);

        JMenuItem mntmDelete = new JMenuItem("Delete");
        mntmDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	panel.deleteSelectedShape();
            }
        });
        add(mntmDelete);
    }
    
}
