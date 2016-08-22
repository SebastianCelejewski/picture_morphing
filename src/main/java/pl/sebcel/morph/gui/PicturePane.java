package pl.sebcel.morph.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
	private static final int scale = 2;

	private Role role;
	private MorphingEngine engine;
	private MainFrame mainFrame;

	private int mouseX = 0;
	private int mouseY = 0;

	private boolean showAnchors = false;

	private List<TransformAnchor> anchors;
	private List<int[]> triangles;
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
				int width = image.getWidth() / scale;
				int height = image.getHeight() / scale;

				BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = resizedImg.createGraphics();

				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.drawImage(engine.getImage(role), 0, 0, width, height, null);
				g2.dispose();

				pictureLabel.setIcon(new ImageIcon(resizedImg));
				pictureLabel.setText("");
			} else {
				pictureLabel.setIcon(null);
				pictureLabel.setText("No image loaded yet");
			}

			triangles = engine.getTriangles(role);
			anchors = engine.getAnchors();
		}
		super.repaint();
	}

	public void paint(Graphics g) {
		super.paint(g);

		if (showAnchors && anchors != null) {
			g.setColor(Color.RED);
			for (TransformAnchor anchor : anchors) {
				int x = anchor.getX(phase) / scale;
				int y = anchor.getY(phase) / scale;
				g.fillRect(x, y, 2, 2);

				if (anchor == selectedAnchor) {
					g.setColor(Color.RED);
					g.drawArc(selectedAnchor.getX(phase) / scale - 5, selectedAnchor.getY(phase) / scale - 5, 10, 10, 0, 359);
				}
			}
		}

		if (triangles != null) {
			g.setColor(Color.blue);
			for (int[] triangle : triangles) {
				int x1 = triangle[0] / scale;
				int y1 = triangle[1] / scale;
				int x2 = triangle[2] / scale;
				int y2 = triangle[3] / scale;
				g.drawLine(x1, y1, x2, y2);
			}
		}

		g.setColor(Color.BLUE);

	}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			int x = e.getX() * scale;
			int y = e.getY() * scale;
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
		this.mouseX = e.getX() * scale;
		this.mouseY = e.getY() * scale;

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
		this.mouseX = e.getX() * scale;
		this.mouseY = e.getY() * scale;
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