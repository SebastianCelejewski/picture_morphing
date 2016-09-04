package pl.sebcel.morph.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import pl.sebcel.morph.engine.MorphingEngine;
import pl.sebcel.morph.model.TransformAnchor;

public class PicturePane extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

	public static enum Role {
		SOURCE, TARGET, SOURCE_TRANSFORMED, TARGET_TRANSFORMED, OUTPUT
	}

	private static final long serialVersionUID = 1L;
	private JLabel pictureLabel;
	private JCheckBox showTriangles = new JCheckBox("Show triangles");
	private double zoom = 1;
	private int panX = 0;
	private int panY = 0;

	private Role role;
	private MorphingEngine engine;
	private MainFrame mainFrame;

	private double mouseX = 0;
	private double mouseY = 0;

	private boolean showAnchors = false;

	private List<TransformAnchor> anchors;
	private List<double[]> triangles;
	private TransformAnchor highlightedAnchor;
	private TransformAnchor selectedAnchor;
	private double phase;

	public PicturePane(Role role) {
		this.role = role;

		this.setLayout(new BorderLayout());
		pictureLabel = new JLabel("No photo loaded yet");
		this.add(pictureLabel, BorderLayout.CENTER);
		this.add(showTriangles, BorderLayout.SOUTH);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);

		if (role == Role.SOURCE) {
			this.phase = 0.0;
		}
		if (role == Role.TARGET) {
			this.phase = 1.0;
		}

		showTriangles.addActionListener(e -> {
			this.repaint();
		});
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
				int width = getX(image.getWidth());
				int height = getY(image.getHeight());

				BufferedImage resizedImg = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = resizedImg.createGraphics();

				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				int x = (int) (panX * zoom);
				int y = (int) (panY * zoom);
				int w = (int) (width - panX * zoom);
				int h = (int) (height - panY * zoom);
				g2.drawImage(engine.getImage(role), x, y, w, h, null);
				g2.dispose();

				pictureLabel.setIcon(new ImageIcon(resizedImg));
				pictureLabel.setText("");
			} else {
				pictureLabel.setIcon(null);
				pictureLabel.setText("No image loaded yet");
			}

			triangles = engine.getTriangles(role);
			anchors = engine.getAnchors();
			selectedAnchor = engine.getSelectedAnchor();
		}
		super.repaint();
	}

	public void paint(Graphics g) {
		super.paint(g);

		if (showAnchors && anchors != null) {
			g.setColor(Color.RED);
			for (TransformAnchor anchor : anchors) {
				int x = getX(anchor.getX(phase));
				int y = getY(anchor.getY(phase));
				g.fillRect(x - 1, y - 1, 2, 2);

				if (anchor == highlightedAnchor) {
					g.setColor(Color.RED);
					g.drawArc(getX(highlightedAnchor.getX(phase)) - 5, getY(highlightedAnchor.getY(phase)) - 5, 10, 10, 0, 359);
				}
			}
		}

		if (selectedAnchor != null) {
			int x = getX(selectedAnchor.getX(phase));
			int y = getY(selectedAnchor.getY(phase));
			g.setColor(Color.YELLOW);
			g.drawOval(x - 6, y - 6, 11, 11);
		}

		if (triangles != null && showTriangles.isSelected()) {
			g.setColor(Color.blue);
			for (double[] triangle : triangles) {
				int x1 = getX(triangle[0]);
				int y1 = getY(triangle[1]);
				int x2 = getX(triangle[2]);
				int y2 = getY(triangle[3]);
				g.drawLine(x1, y1, x2, y2);
			}
		}

		g.setColor(Color.BLUE);

	}

	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			double x = getReversedX(e.getX());
			double y = getReversedY(e.getY());
			TransformAnchor anchor = new TransformAnchor();
			anchor.setOriginalX(x);
			anchor.setOriginalY(y);
			anchor.setTargetX(x);
			anchor.setTargetY(y);
			engine.addAnchor(anchor);
			this.repaint();
		}

		if (SwingUtilities.isRightMouseButton(e)) {
			TransformAnchor selectedAnchor = findHighlightedAnchor();
			engine.setSelectedAnchor(selectedAnchor);
			this.repaint();
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
	}

	public void mouseReleased(MouseEvent e) {
		mx = 0;
		my = 0;
	}

	private int mx = 0;
	private int my = 0;

	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e) && mx != 0 && my != 0) {
			int dx = e.getX() - mx;
			int dy = e.getY() - my;
			this.panX += dx / zoom;
			this.panY += dy / zoom;
			mx = e.getX();
			my = e.getY();

			this.repaint();
		} else {
			this.mouseX = getReversedX(e.getX());
			this.mouseY = getReversedY(e.getY());

			if (highlightedAnchor != null) {
				if (phase == 0.0) {
					highlightedAnchor.setOriginalX(this.mouseX);
					highlightedAnchor.setOriginalY(this.mouseY);
				}

				if (phase == 1.0) {
					highlightedAnchor.setTargetX(this.mouseX);
					highlightedAnchor.setTargetY(this.mouseY);
				}

				engine.anchorMoved();
			}

			this.repaint();
		}
	}

	public void mouseMoved(MouseEvent e) {
		this.mouseX = getReversedX(e.getX());
		this.mouseY = getReversedY(e.getY());
		this.repaint();
		TransformAnchor anchor = findHighlightedAnchor();
		mainFrame.setHighlightedAnchor(anchor);
	}

	public void highlightAnchor(TransformAnchor anchor) {
		this.highlightedAnchor = anchor;
		this.repaint();
	}

	private TransformAnchor findHighlightedAnchor() {
		if (anchors == null) {
			return null;
		}
		TransformAnchor highlightedAnchor = null;
		for (TransformAnchor anchor : anchors) {
			int x = getX(anchor.getX(phase));
			int y = getY(anchor.getY(phase));
			int mx = getX(mouseX);
			int my = getY(mouseY);
			double mouseDistance = Math.sqrt((x - mx) * (x - mx) + (y - my) * (y - my));
			if (mouseDistance < 10 && highlightedAnchor == null) {
				highlightedAnchor = anchor;
			}
		}
		return highlightedAnchor;
	}

	private int getX(double x) {
		return (int) ((x + panX) * zoom);
	}

	private int getY(double y) {
		return (int) ((y + panY) * zoom);
	}

	private double getReversedX(int x) {
		return x / zoom - panX;
	}

	private double getReversedY(int y) {
		return y / zoom - panY;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		double rotation = e.getPreciseWheelRotation();
		if (rotation < 0) {
			if (zoom < 4) {
				zoom = zoom * 2;
				this.repaint();
			}
		}
		if (rotation > 0) {
			if (zoom > 0.125) {
				zoom = zoom / 2;
				this.repaint();
			}
		}
	}
}