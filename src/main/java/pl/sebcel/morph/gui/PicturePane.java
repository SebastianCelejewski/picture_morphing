package pl.sebcel.morph.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import pl.sebcel.morph.engine.MorphingEngine;
import pl.sebcel.morph.model.TransformAnchor;

public class PicturePane extends JPanel implements MouseListener, MouseMotionListener {

    public static enum Role {
        SOURCE, TARGET, SOURCE_TRANSFORMED, TARGET_TRANSFORMED, OUTPUT
    }

    private static final long serialVersionUID = 1L;
    private JLabel pictureLabel;

    private Role role;
    private MorphingEngine engine;
    private MainFrame mainFrame;

    private int mouseX = 0;
    private int mouseY = 0;

    private boolean showAnchors = false;

    private List<TransformAnchor> anchors;
    private TransformAnchor selectedAnchor;
    private double phase;

    public PicturePane(Role role) {
        this.role = role;

        this.setLayout(new BorderLayout());
        pictureLabel = new JLabel("No photo loaded yet");
        this.add(pictureLabel, BorderLayout.CENTER);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        if (role == Role.SOURCE) {
            this.phase = 0.0;
        }
        if (role == Role.TARGET) {
            this.phase = 1.0;
        }
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void setMorphingEngine(MorphingEngine engine) {
        this.engine = engine;
    }

    public void setShowAnchors(boolean showAnchors) {
        this.showAnchors = showAnchors;
    }

    @Override
    public void repaint() {
        if (engine != null) {
            if (role == Role.OUTPUT || role == Role.SOURCE_TRANSFORMED || role == Role.TARGET_TRANSFORMED) {
                this.phase = engine.getPhase();
            }

            BufferedImage image = engine.getImage(role);
            if (image != null) {
                pictureLabel.setIcon(new ImageIcon(image));
                pictureLabel.setText("");
            } else {
                pictureLabel.setIcon(null);
                pictureLabel.setText("No image loaded yet");
            }
            anchors = engine.getAnchors();
        }
        super.repaint();
    }

    public void paint(Graphics g) {
        super.paint(g);

        if (showAnchors && anchors != null) {
            g.setColor(Color.yellow);
            for (TransformAnchor anchor : anchors) {
                int x = anchor.getX(phase);
                int y = anchor.getY(phase);
                g.fillRect(x, y, 2, 2);

                if (anchor == selectedAnchor) {
                    g.setColor(Color.yellow);
                    g.drawArc(selectedAnchor.getX(phase) - 5, selectedAnchor.getY(phase) - 5, 10, 10, 0, 359);
                }
            }
        }

        g.setColor(Color.BLUE);

    }

    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            int x = e.getX();
            int y = e.getY();
            TransformAnchor anchor = new TransformAnchor();
            anchor.setOriginalX(x);
            anchor.setOriginalY(y);
            anchor.setTargetX(x);
            anchor.setTargetY(y);
            engine.addAnchor(anchor);
        }
    }

    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mouseDragged(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();

        if (selectedAnchor != null) {
            if (phase == 0.0) {
                selectedAnchor.setOriginalX(this.mouseX);
                selectedAnchor.setOriginalY(this.mouseY);
            }

            if (phase == 1.0) {
                selectedAnchor.setTargetX(this.mouseX);
                selectedAnchor.setTargetY(this.mouseY);
            }
        }

        this.repaint();
    }

    public void mouseMoved(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
        this.repaint();
        TransformAnchor anchor = findSelectedAnchor();
        mainFrame.setSelectedAnchor(anchor);
    }

    public void selectAnchor(TransformAnchor anchor) {
        this.selectedAnchor = anchor;
        this.repaint();
    }

    private TransformAnchor findSelectedAnchor() {
        if (anchors == null) {
            return null;
        }
        TransformAnchor selectedAnchor = null;
        for (TransformAnchor anchor : anchors) {
            int x = anchor.getX(phase);
            int y = anchor.getY(phase);
            double mouseDistance = Math.sqrt((x - mouseX) * (x - mouseX) + (y - mouseY) * (y - mouseY));
            if (mouseDistance < 10 && selectedAnchor == null) {
                selectedAnchor = anchor;
            }
        }
        return selectedAnchor;
    }
}