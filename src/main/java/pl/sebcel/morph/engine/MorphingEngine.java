package pl.sebcel.morph.engine;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jdelaunay.delaunay.ConstrainedMesh;
import org.jdelaunay.delaunay.geometries.DPoint;
import org.jdelaunay.delaunay.geometries.DTriangle;

import pl.sebcel.morph.gui.MainFrame;
import pl.sebcel.morph.gui.PicturePane;
import pl.sebcel.morph.model.TransformAnchor;
import pl.sebcel.morph.model.TransformData;
import pl.sebcel.morph.model.TriangleToTriangleTransformer;

public class MorphingEngine {

	private static class CacheKey {
		private DTriangle triangle;
		private Double phase;

		public CacheKey(DTriangle triangle, Double phase) {
			this.triangle = triangle;
			this.phase = phase;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((phase == null) ? 0 : phase.hashCode());
			result = prime * result + ((triangle == null) ? 0 : triangle.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (phase == null) {
				if (other.phase != null)
					return false;
			} else if (!phase.equals(other.phase))
				return false;
			if (triangle == null) {
				if (other.triangle != null)
					return false;
			} else if (!triangle.equals(other.triangle))
				return false;
			return true;
		}

	}

	private BufferedImage sourceImage;

	private BufferedImage targetImage;

	private BufferedImage sourceTransformedImage;

	private BufferedImage targetTransformedImage;

	private BufferedImage outputImage;

	private List<DTriangle> triangles;

	private List<int[]> sourceTrianglesEdges;

	private List<int[]> targetTrianglesEdges;

	private Map<DTriangle, List<TransformAnchor>> anchorsForTriangles = new HashMap<DTriangle, List<TransformAnchor>>();

	private Map<CacheKey, TriangleToTriangleTransformer> transformers1 = new HashMap<CacheKey, TriangleToTriangleTransformer>();

	private Map<CacheKey, TriangleToTriangleTransformer> transformers2 = new HashMap<CacheKey, TriangleToTriangleTransformer>();

	private Map<Double, BufferedImage> imagesCache = new HashMap<Double, BufferedImage>();

	private double phase;

	private MainFrame mainFrame;

	private TransformData project;

	public void setProject(TransformData project) {
		this.project = project;

		if (project != null) {
			loadSourceImage(project.getSourceImagePath());
			loadTargetImage(project.getTargetImagePath());
		} else {
			sourceImage = null;
			targetImage = null;
			sourceTransformedImage = null;
			targetTransformedImage = null;
			outputImage = null;
		}

		transformers1.clear();
		transformers2.clear();
		anchorsForTriangles.clear();
		imagesCache.clear();

		setPhase(0.5);
	}

	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public void loadSourceImage(String path) {
		sourceImage = loadImage(path);
	}

	public void loadTargetImage(String path) {
		targetImage = loadImage(path);
	}

	public void setSourceImage(File file) {
		project.setSourceImagePath(file.getAbsolutePath());
		loadSourceImage(file.getAbsolutePath());
	}

	public void setTargetImage(File file) {
		project.setTargetImagePath(file.getAbsolutePath());
		loadTargetImage(file.getAbsolutePath());
	}

	private BufferedImage loadImage(String path) {
		System.out.println("Loading image from " + path);
		if (path == null) {
			return null;
		}
		try {
			return ImageIO.read(new File(path));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to load image from file " + path + ": " + ex.getMessage(), ex);
		}
	}

	public BufferedImage getImage(PicturePane.Role role) {
		switch (role) {
		case SOURCE:
			return sourceImage;
		case TARGET:
			return targetImage;
		case SOURCE_TRANSFORMED:
			return sourceTransformedImage;
		case TARGET_TRANSFORMED:
			return targetTransformedImage;
		case OUTPUT:
			return outputImage;
		}
		throw new RuntimeException("Invalid role: " + role);
	}

	public List<int[]> getTriangles(PicturePane.Role role) {
		switch (role) {
		case SOURCE:
			return sourceTrianglesEdges;
		case TARGET:
			return targetTrianglesEdges;
		default:
			return null;
		}
	}

	public void setPhase(double phase) {
		this.phase = phase;
		processImages();
		mainFrame.repaint();
	}

	public double getPhase() {
		return phase;
	}

	public List<TransformAnchor> getAnchors() {
		if (project != null) {
			return project.getAnchors();
		} else {
			return null;
		}
	}

	public void addAnchor(TransformAnchor anchor) {
		project.getAnchors().add(anchor);
		mainFrame.repaint();

		transformers1.clear();
		transformers2.clear();
		anchorsForTriangles.clear();
		imagesCache.clear();
	}

	private void processImages() {
		if (project != null) {
			if (imagesCache.containsKey(phase)) {
				sourceTransformedImage = imagesCache.get(phase + 1);
				targetTransformedImage = imagesCache.get(phase + 2);
				outputImage = imagesCache.get(phase + 3);
			} else {
				triangles = triangulate();
				sourceTrianglesEdges = calculateSourceTrianglesEdges();
				targetTrianglesEdges = calculateTargetTrianglesEdges();
				sourceTransformedImage = transformSourceImage();
				targetTransformedImage = transformTargetImage();
				outputImage = blendTransformedImages();
				imagesCache.put(phase, null);
				imagesCache.put(phase + 1, sourceTransformedImage);
				imagesCache.put(phase + 2, targetTransformedImage);
				imagesCache.put(phase + 3, outputImage);
			}
		}
	}

	private BufferedImage transformSourceImage() {
		if (sourceImage == null) {
			return null;
		}
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();
		int type = sourceImage.getType();

		BufferedImage result = new BufferedImage(width, height, type);

		for (double x = 0; x < width; x += 0.25) {
			for (double y = 0; y < height; y += 0.25) {
				double newX = x;
				double newY = y;
				DTriangle triangle = getTriangleForPoint((int) x, (int) y);

				if (triangle != null) {
					List<TransformAnchor> anchorsForTriangle = getAnchorsForTriangle(triangle);
					TriangleToTriangleTransformer transformer = getTransformer1ForTriangle(triangle, anchorsForTriangle, phase);
					newX = (int) transformer.transformX(x, y);
					newY = (int) transformer.transformY(x, y);
				}

				if (newX >= width) {
					newX = width - 1;
				}
				if (newX < 0) {
					newX = 0;
				}
				if (newY >= height) {
					newY = height - 1;
				}
				if (newY < 0) {
					newY = 0;
				}

				int rgb = sourceImage.getRGB((int) x, (int) y);
				result.setRGB((int) newX, (int) newY, rgb);
			}
		}

		return result;
	}

	private BufferedImage transformTargetImage() {
		if (targetImage == null) {
			return null;
		}
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();
		int type = sourceImage.getType();

		BufferedImage result = new BufferedImage(width, height, type);

		for (double x = 0; x < width; x += 1) {
			for (double y = 0; y < height; y += 1) {
				double newX = x;
				double newY = y;
				DTriangle triangle = getTriangleForPoint((int) x, (int) y);

				if (triangle != null) {
					List<TransformAnchor> anchorsForTriangle = getAnchorsForTriangle(triangle);
					TriangleToTriangleTransformer transformer = getTransformer2ForTriangle(triangle, anchorsForTriangle, 1 - phase);
					newX = (int) transformer.transformX(x, y);
					newY = (int) transformer.transformY(x, y);
				}

				if (newX >= width) {
					newX = width - 1;
				}
				if (newX < 0) {
					newX = 0;
				}
				if (newY >= height) {
					newY = height - 1;
				}
				if (newY < 0) {
					newY = 0;
				}

				try {
					int rgb = targetImage.getRGB((int) newX, (int) newY);
					result.setRGB((int) x, (int) y, rgb);
				} catch (Exception ex) {
					// System.out.println(x+","+y+" -> "+newX+","+newY+" width: "+width+", height: "+height);
				}

			}
		}

		return result;
	}

	private BufferedImage blendTransformedImages() {
		if (sourceImage == null || targetImage == null) {
			return null;
		}
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();
		int type = sourceImage.getType();

		BufferedImage result = new BufferedImage(width, height, type);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				int sourceRGB = sourceTransformedImage.getRGB(x, y);
				int targetRGB = targetTransformedImage.getRGB(x, y);

				Color sourceColor = new Color(sourceRGB);
				Color targetColor = new Color(targetRGB);

				int diffBlue = targetColor.getBlue() - sourceColor.getBlue();
				int diffGreen = targetColor.getGreen() - sourceColor.getGreen();
				int diffRed = targetColor.getRed() - sourceColor.getRed();
				int diffAlpha = targetColor.getAlpha() - sourceColor.getAlpha();

				int mixBlue = sourceColor.getBlue() + (int) (diffBlue * phase);
				int mixGreen = sourceColor.getGreen() + (int) (diffGreen * phase);
				int mixRed = sourceColor.getRed() + (int) (diffRed * phase);
				int mixAlpha = sourceColor.getAlpha() + (int) (diffAlpha * phase);

				Color mixColor = new Color(mixRed, mixGreen, mixBlue, mixAlpha);
				int mixRGB = mixColor.getRGB();

				result.setRGB(x, y, mixRGB);
			}
		}

		return result;
	}

	private List<DTriangle> triangulate() {
		if (project.getAnchors().size() < 3) {
			return new ArrayList<DTriangle>();
		}
		try {
			ConstrainedMesh mesh = new ConstrainedMesh();
			for (TransformAnchor anchor : project.getAnchors()) {
				int x = anchor.getX(0);
				int y = anchor.getY(0);
				DPoint point = new DPoint(x, y, 0);
				mesh.addPoint(point);
			}
			mesh.processDelaunay();
			return mesh.getTriangleList();
		} catch (Exception ex) {
			ex.printStackTrace();
			return new ArrayList<DTriangle>();
		}
	}

	private List<int[]> calculateSourceTrianglesEdges() {
		List<int[]> edges = new ArrayList<int[]>();
		for (DTriangle triangle : triangles) {
			List<TransformAnchor> anchorsForTriangle = getAnchorsForTriangle(triangle);
			int x0 = anchorsForTriangle.get(0).getX(0d);
			int y0 = anchorsForTriangle.get(0).getY(0d);
			int x1 = anchorsForTriangle.get(1).getX(0d);
			int y1 = anchorsForTriangle.get(1).getY(0d);
			int x2 = anchorsForTriangle.get(2).getX(0d);
			int y2 = anchorsForTriangle.get(2).getY(0d);
			edges.add(new int[] { x0, y0, x1, y1 });
			edges.add(new int[] { x1, y1, x2, y2 });
			edges.add(new int[] { x2, y2, x0, y0 });
		}
		return edges;
	}

	private List<int[]> calculateTargetTrianglesEdges() {
		List<int[]> edges = new ArrayList<int[]>();
		for (DTriangle triangle : triangles) {
			List<TransformAnchor> anchorsForTriangle = getAnchorsForTriangle(triangle);
			int x0 = anchorsForTriangle.get(0).getX(1d);
			int y0 = anchorsForTriangle.get(0).getY(1d);
			int x1 = anchorsForTriangle.get(1).getX(1d);
			int y1 = anchorsForTriangle.get(1).getY(1d);
			int x2 = anchorsForTriangle.get(2).getX(1d);
			int y2 = anchorsForTriangle.get(2).getY(1d);
			edges.add(new int[] { x0, y0, x1, y1 });
			edges.add(new int[] { x1, y1, x2, y2 });
			edges.add(new int[] { x2, y2, x0, y0 });
		}
		return edges;
	}

	private TriangleToTriangleTransformer getTransformer1ForTriangle(DTriangle triangle, List<TransformAnchor> anchorsForTriangle, double phase) {
		try {
			CacheKey key = new CacheKey(triangle, phase);
			if (!transformers1.containsKey(key)) {
				DPoint t1p1 = new DPoint(anchorsForTriangle.get(0).getX(0.0), anchorsForTriangle.get(0).getY(0.0), 0);
				DPoint t1p2 = new DPoint(anchorsForTriangle.get(1).getX(0.0), anchorsForTriangle.get(1).getY(0.0), 0);
				DPoint t1p3 = new DPoint(anchorsForTriangle.get(2).getX(0.0), anchorsForTriangle.get(2).getY(0.0), 0);

				DPoint t2p1 = new DPoint(anchorsForTriangle.get(0).getX(phase), anchorsForTriangle.get(0).getY(phase), 0);
				DPoint t2p2 = new DPoint(anchorsForTriangle.get(1).getX(phase), anchorsForTriangle.get(1).getY(phase), 0);
				DPoint t2p3 = new DPoint(anchorsForTriangle.get(2).getX(phase), anchorsForTriangle.get(2).getY(phase), 0);

				DTriangle t1 = new DTriangle(t1p1, t1p2, t1p3);
				DTriangle t2 = new DTriangle(t2p1, t2p2, t2p3);

				TriangleToTriangleTransformer transformer = new TriangleToTriangleTransformer(t1, t2);
				transformers1.put(key, transformer);
			}

			return transformers1.get(key);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private TriangleToTriangleTransformer getTransformer2ForTriangle(DTriangle triangle, List<TransformAnchor> anchorsForTriangle, double phase) {
		try {
			CacheKey key = new CacheKey(triangle, phase);
			if (!transformers2.containsKey(key)) {
				DPoint t1p1 = new DPoint(anchorsForTriangle.get(0).getX(0.0), anchorsForTriangle.get(0).getY(0.0), 0);
				DPoint t1p2 = new DPoint(anchorsForTriangle.get(1).getX(0.0), anchorsForTriangle.get(1).getY(0.0), 0);
				DPoint t1p3 = new DPoint(anchorsForTriangle.get(2).getX(0.0), anchorsForTriangle.get(2).getY(0.0), 0);

				DPoint t2p1 = new DPoint(anchorsForTriangle.get(0).getX(phase), anchorsForTriangle.get(0).getY(phase), 0);
				DPoint t2p2 = new DPoint(anchorsForTriangle.get(1).getX(phase), anchorsForTriangle.get(1).getY(phase), 0);
				DPoint t2p3 = new DPoint(anchorsForTriangle.get(2).getX(phase), anchorsForTriangle.get(2).getY(phase), 0);

				DTriangle t1 = new DTriangle(t1p1, t1p2, t1p3);
				DTriangle t2 = new DTriangle(t2p1, t2p2, t2p3);

				TriangleToTriangleTransformer transformer = new TriangleToTriangleTransformer(t1, t2);
				transformers2.put(key, transformer);
			}

			return transformers2.get(key);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private DTriangle getTriangleForPoint(int x, int y) {
		if (triangles == null) {
			return null;
		}
		try {
			DPoint point = new DPoint(x, y, 0);

			for (DTriangle triangle : triangles) {
				if (triangle.isInside(point)) {
					return triangle;
				}
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find a triangle for a point " + x + "," + y + ": " + ex.getMessage());
		}
	}

	private List<TransformAnchor> getAnchorsForTriangle(DTriangle triangle) {
		if (!anchorsForTriangles.containsKey(triangle)) {
			List<TransformAnchor> anchorsForTriangle = new ArrayList<TransformAnchor>();
			if (triangle != null) {
				for (DPoint point : triangle.getPoints()) {
					double px = point.getX();
					double py = point.getY();

					for (TransformAnchor anchor : project.getAnchors()) {
						if (anchor.getX(0.0) == px && anchor.getY(0.0) == py) {
							anchorsForTriangle.add(anchor);
						}
					}
				}
			}
			anchorsForTriangles.put(triangle, anchorsForTriangle);

		}

		return anchorsForTriangles.get(triangle);
	}

	public TransformData getProject() {
		return project;
	}

}