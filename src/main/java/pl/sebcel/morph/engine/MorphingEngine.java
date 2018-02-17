package pl.sebcel.morph.engine;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.jdelaunay.delaunay.ConstrainedMesh;
import org.jdelaunay.delaunay.geometries.DPoint;
import org.jdelaunay.delaunay.geometries.DTriangle;

import pl.sebcel.morph.engine.DataCache.CacheKey;
import pl.sebcel.morph.gui.MainFrame;
import pl.sebcel.morph.gui.PicturePane;
import pl.sebcel.morph.model.TransformAnchor;
import pl.sebcel.morph.model.TransformData;
import pl.sebcel.morph.model.TriangleToTriangleTransformer;

public class MorphingEngine {

	private BufferedImage sourceImage;

	private BufferedImage targetImage;

	private BufferedImage sourceTransformedImage;

	private BufferedImage targetTransformedImage;

	private BufferedImage outputImage;

	private List<DTriangle> sourceTriangles;

	private List<DTriangle> currentTriangles;

	private List<double[]> sourceTrianglesEdges;

	private List<double[]> targetTrianglesEdges;

	private List<double[]> currentTrianglesEdges;

	private DataCache dataCache = new DataCache();

	private double phase;

	private MainFrame mainFrame;

	private TransformData project;

	private TransformAnchor selectedAnchor;

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

		dataCache.clearAll();

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

	public List<double[]> getTriangles(PicturePane.Role role) {
		switch (role) {
		case SOURCE:
			return sourceTrianglesEdges;
		case TARGET:
			return targetTrianglesEdges;
		default:
			return currentTrianglesEdges;
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

		dataCache.clearAll();
	}

	public void anchorMoved() {
		dataCache.clearAll();
	}

	private void processImages() {
		if (project != null) {
			if (dataCache.containsImagesForPhase(phase)) {
				sourceTransformedImage = dataCache.getSourceTransformedImageForPhase(phase);
				targetTransformedImage = dataCache.getTargetTransformedImageForPhase(phase);
				outputImage = dataCache.getOutputTransformedImageForPhase(phase);
				sourceTrianglesEdges = dataCache.getSourceTrianglesForPhase(phase);
				targetTrianglesEdges = dataCache.getTargetTrianglesForPhase(phase);
				currentTrianglesEdges = dataCache.getCurrentTrianglesForPhase(phase);
			} else {

				long startTime = getStartTime();
				System.out.println("Starting picture morphing for phase " + phase);

				sourceTriangles = triangulate();
				currentTriangles = calculateCurrentTriangles();
				sourceTrianglesEdges = calculateSourceTrianglesEdges();
				targetTrianglesEdges = calculateTargetTrianglesEdges();
				currentTrianglesEdges = calculateCurrentTrianglesEdges();
				sourceTransformedImage = transformSourceImage();
				targetTransformedImage = transformTargetImage();
				outputImage = blendTransformedImages();
				dataCache.putImagesForPhase(phase, sourceTransformedImage, targetTransformedImage, outputImage);
				dataCache.putTrianglesForPhase(phase, sourceTrianglesEdges, targetTrianglesEdges, currentTrianglesEdges);

				System.out.println("Total processing time: " + getDuration(startTime) + " ms");
			}
		}
	}
	
	private List<DTriangle> triangulate() {
		if (project.getAnchors().size() < 3) {
			return new ArrayList<DTriangle>();
		}
		try {
			long startTime = getStartTime();

			ConstrainedMesh mesh = new ConstrainedMesh();
			for (TransformAnchor anchor : project.getAnchors()) {
				double x = anchor.getX(0);
				double y = anchor.getY(0);
				DPoint point = new DPoint(x, y, 0);
				mesh.addPoint(point);
			}
			mesh.processDelaunay();

			List<DTriangle> result = mesh.getTriangleList();
			System.out.println(" - triangulation time: " + getDuration(startTime) + " ms.");
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			return new ArrayList<DTriangle>();
		}
	}	
	
	private List<DTriangle> calculateCurrentTriangles() {
		try {
			long startTime = getStartTime();
			List<DTriangle> result = new ArrayList<>();
			for (DTriangle sourceTriangle : sourceTriangles) {
				List<TransformAnchor> anchorsForTriangle = getAnchorsForSourceTriangle(sourceTriangle);
				double x0 = anchorsForTriangle.get(0).getX(phase);
				double y0 = anchorsForTriangle.get(0).getY(phase);
				double x1 = anchorsForTriangle.get(1).getX(phase);
				double y1 = anchorsForTriangle.get(1).getY(phase);
				double x2 = anchorsForTriangle.get(2).getX(phase);
				double y2 = anchorsForTriangle.get(2).getY(phase);
				DTriangle targetTriangle = new DTriangle(new DPoint(x0, y0, 0), new DPoint(x1, y1, 0), new DPoint(x2, y2, 0));
				result.add(targetTriangle);
			}
			System.out.println(" - current triangles calculation: " + getDuration(startTime) + " ms");
			return result;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to calculate current triangles from source triangles: " + ex.getMessage(), ex);
		}
	}	

	private BufferedImage transformSourceImage() {
		if (sourceImage == null) {
			return null;
		}
		long startTime = getStartTime();
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();
		int type = sourceImage.getType();

		BufferedImage result = new BufferedImage(width, height, type);

		int transformationSteps = width * height;
		System.out.println(" - transformation steps : " + transformationSteps);

		int gridXCount = 4;
		int gridYCount = 4;

		double gridXStep = width / gridXCount;
		double gridYStep = height / gridYCount;

		List<Thread> workers = new ArrayList<Thread>();

		for (int gx = 0; gx < width; gx += gridXStep) {
			for (int gy = 0; gy < height; gy += gridYStep) {
				final int gx1 = gx;
				final int gy1 = gy;
				Thread t = new Thread(() -> klumpf(gx1, gx1 + gridXStep, gy1, gy1 + gridYStep, width, height, result));
				workers.add(t);
				t.start();
			}
		}

		try {
			for (Thread t : workers) {
				t.join();
			}
		} catch (Exception ex) {
			// intentional
		}
		System.out.println(" - source image transformation time: " + getDuration(startTime) + " ms");
		return result;
	}

	private BufferedImage transformTargetImage() {
		if (targetImage == null) {
			return null;
		}
		long startTime = getStartTime();
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();
		int type = sourceImage.getType();

		BufferedImage result = new BufferedImage(width, height, type);

		int gridXCount = 4;
		int gridYCount = 4;

		double gridXStep = width / gridXCount;
		double gridYStep = height / gridYCount;

		List<Thread> workers = new ArrayList<Thread>();

		for (int gx = 0; gx < width; gx += gridXStep) {
			for (int gy = 0; gy < height; gy += gridYStep) {
				final int gx1 = gx;
				final int gy1 = gy;
				Thread t = new Thread(() -> klumpf2(gx1, gx1 + gridXStep, gy1, gy1 + gridYStep, width, height, result));
				workers.add(t);
				t.start();
			}
		}

		try {
			for (Thread t : workers) {
				t.join();
			}
		} catch (Exception ex) {
			// intentional
		}

		System.out.println(" - target image transformation time: " + getDuration(startTime) + " ms");
		return result;
	}
	
	private void klumpf(double minX, double maxX, double minY, double maxY, double width, double height, BufferedImage result) {
		for (double x = minX; x < maxX; x += 1) {
			for (double y = minY; y < maxY; y += 1) {
				double newX = x;
				double newY = y;
				DTriangle triangle = getCurrentTriangleForPoint(x, y);

				if (triangle != null) {
					TriangleToTriangleTransformer transformer = getTransformer1ForTriangle(triangle, phase);
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

				int rgb = sourceImage.getRGB((int) newX, (int) newY);
				result.setRGB((int) x, (int) y, rgb);
			}
		}
	}	

	private void klumpf2(double minX, double maxX, double minY, double maxY, double width, double height, BufferedImage result) {
		for (double x = minX; x < maxX; x += 1) {
			for (double y = minY; y < maxY; y += 1) {
				double newX = x;
				double newY = y;
				DTriangle triangle = getCurrentTriangleForPoint(x, y);

				if (triangle != null) {
					TriangleToTriangleTransformer transformer = getTransformer2ForTriangle(triangle, phase);
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

				int rgb = targetImage.getRGB((int) newX, (int) newY);
				result.setRGB((int) x, (int) y, rgb);
			}
		}
	}
	private BufferedImage blendTransformedImages() {
		if (sourceImage == null || targetImage == null) {
			return null;
		}
		long startTime = getStartTime();
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

		System.out.println(" - image blending time: " + getDuration(startTime) + " ms");
		return result;
	}

	private List<double[]> calculateSourceTrianglesEdges() {
		long startTime = getStartTime();
		List<double[]> edges = new ArrayList<double[]>();
		for (DTriangle triangle : sourceTriangles) {
			List<TransformAnchor> anchorsForTriangle = getAnchorsForSourceTriangle(triangle);
			double x0 = anchorsForTriangle.get(0).getX(0d);
			double y0 = anchorsForTriangle.get(0).getY(0d);
			double x1 = anchorsForTriangle.get(1).getX(0d);
			double y1 = anchorsForTriangle.get(1).getY(0d);
			double x2 = anchorsForTriangle.get(2).getX(0d);
			double y2 = anchorsForTriangle.get(2).getY(0d);
			edges.add(new double[] { x0, y0, x1, y1 });
			edges.add(new double[] { x1, y1, x2, y2 });
			edges.add(new double[] { x2, y2, x0, y0 });
		}
		System.out.println(" - source triangles calculation time: " + getDuration(startTime) + " ms");
		return edges;
	}

	private List<double[]> calculateTargetTrianglesEdges() {
		long startTime = getStartTime();
		List<double[]> edges = new ArrayList<double[]>();
		for (DTriangle triangle : sourceTriangles) {
			List<TransformAnchor> anchorsForTriangle = getAnchorsForSourceTriangle(triangle);
			double x0 = anchorsForTriangle.get(0).getX(1d);
			double y0 = anchorsForTriangle.get(0).getY(1d);
			double x1 = anchorsForTriangle.get(1).getX(1d);
			double y1 = anchorsForTriangle.get(1).getY(1d);
			double x2 = anchorsForTriangle.get(2).getX(1d);
			double y2 = anchorsForTriangle.get(2).getY(1d);
			edges.add(new double[] { x0, y0, x1, y1 });
			edges.add(new double[] { x1, y1, x2, y2 });
			edges.add(new double[] { x2, y2, x0, y0 });
		}
		System.out.println(" - target triangles calculation: " + getDuration(startTime) + " ms");
		return edges;
	}

	private List<double[]> calculateCurrentTrianglesEdges() {
		long startTime = getStartTime();
		List<double[]> edges = new ArrayList<double[]>();
		for (DTriangle triangle : sourceTriangles) {
			List<TransformAnchor> anchorsForTriangle = getAnchorsForSourceTriangle(triangle);
			double x0 = anchorsForTriangle.get(0).getX(phase);
			double y0 = anchorsForTriangle.get(0).getY(phase);
			double x1 = anchorsForTriangle.get(1).getX(phase);
			double y1 = anchorsForTriangle.get(1).getY(phase);
			double x2 = anchorsForTriangle.get(2).getX(phase);
			double y2 = anchorsForTriangle.get(2).getY(phase);

			edges.add(new double[] { x0, y0, x1, y1 });
			edges.add(new double[] { x1, y1, x2, y2 });
			edges.add(new double[] { x2, y2, x0, y0 });
		}
		System.out.println(" - current triangles calculation: " + getDuration(startTime) + " ms");
		return edges;
	}
	
	private TriangleToTriangleTransformer getTransformer1ForTriangle(DTriangle currentTriangle, double phase) {
		try {
			CacheKey key = new CacheKey(currentTriangle, phase);
			if (!dataCache.containsSourceImageTransformers(key)) {
				List<TransformAnchor> anchorsForTriangle = getAnchorsForCurrentTriangle(currentTriangle);
				DPoint t1p1 = new DPoint(anchorsForTriangle.get(0).getX(0.0), anchorsForTriangle.get(0).getY(0.0), 0);
				DPoint t1p2 = new DPoint(anchorsForTriangle.get(1).getX(0.0), anchorsForTriangle.get(1).getY(0.0), 0);
				DPoint t1p3 = new DPoint(anchorsForTriangle.get(2).getX(0.0), anchorsForTriangle.get(2).getY(0.0), 0);

				DPoint t2p1 = new DPoint(anchorsForTriangle.get(0).getX(phase), anchorsForTriangle.get(0).getY(phase), 0);
				DPoint t2p2 = new DPoint(anchorsForTriangle.get(1).getX(phase), anchorsForTriangle.get(1).getY(phase), 0);
				DPoint t2p3 = new DPoint(anchorsForTriangle.get(2).getX(phase), anchorsForTriangle.get(2).getY(phase), 0);

				DTriangle t1 = new DTriangle(t1p1, t1p2, t1p3);
				DTriangle t2 = new DTriangle(t2p1, t2p2, t2p3);

				TriangleToTriangleTransformer transformer = new TriangleToTriangleTransformer(t2, t1);
				dataCache.putsSourceImageTransformers(key, transformer);
			}

			return dataCache.getSourceImageTransformers(key);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private TriangleToTriangleTransformer getTransformer2ForTriangle(DTriangle currentTriangle, double phase) {
		try {
			CacheKey key = new CacheKey(currentTriangle, phase);
			if (!dataCache.containsTargetImageTransformers(key)) {
				List<TransformAnchor> anchorsForTriangle = getAnchorsForCurrentTriangle(currentTriangle);
				DPoint t1p1 = new DPoint(anchorsForTriangle.get(0).getX(1.0), anchorsForTriangle.get(0).getY(1.0), 0);
				DPoint t1p2 = new DPoint(anchorsForTriangle.get(1).getX(1.0), anchorsForTriangle.get(1).getY(1.0), 0);
				DPoint t1p3 = new DPoint(anchorsForTriangle.get(2).getX(1.0), anchorsForTriangle.get(2).getY(1.0), 0);

				DPoint t2p1 = new DPoint(anchorsForTriangle.get(0).getX(phase), anchorsForTriangle.get(0).getY(phase), 0);
				DPoint t2p2 = new DPoint(anchorsForTriangle.get(1).getX(phase), anchorsForTriangle.get(1).getY(phase), 0);
				DPoint t2p3 = new DPoint(anchorsForTriangle.get(2).getX(phase), anchorsForTriangle.get(2).getY(phase), 0);

				DTriangle t1 = new DTriangle(t1p1, t1p2, t1p3);
				DTriangle t2 = new DTriangle(t2p1, t2p2, t2p3);

				TriangleToTriangleTransformer transformer = new TriangleToTriangleTransformer(t2, t1);
				dataCache.putsTargetImageTransformers(key, transformer);
			}

			return dataCache.getTargetImageTransformers(key);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to get target transformer for triangle: " + ex.getMessage(), ex);
		}
	}

	private DTriangle getCurrentTriangleForPoint(double x, double y) {
		if (currentTriangles == null) {
			return null;
		}
		try {
			DPoint point = new DPoint(x, y, 0);

			for (DTriangle triangle : currentTriangles) {
				if (triangle.isInside(point)) {
					return triangle;
				}
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find current triangle for a point " + x + "," + y + ": " + ex.getMessage());
		}
	}
	
	private List<TransformAnchor> getAnchorsForSourceTriangle(DTriangle triangle) {
		if (!dataCache.containsAnchorsForTriangle(triangle)) {
			List<TransformAnchor> anchorsForTriangle = new ArrayList<TransformAnchor>();
			if (triangle != null) {
				for (DPoint point : triangle.getPoints()) {
					double px = point.getX();
					double py = point.getY();

					for (TransformAnchor anchor : project.getAnchors()) {
						if (Math.abs(anchor.getX(0.0) - px) < 0.01 && Math.abs(anchor.getY(0.0) - py) < 0.01) {
							anchorsForTriangle.add(anchor);
						}
					}
				}
			}
			dataCache.putAnchorsForTriangle(triangle, anchorsForTriangle);
		}

		return dataCache.getAnchorsForTriangle(triangle);
	}	
	
	private List<TransformAnchor> getAnchorsForCurrentTriangle(DTriangle triangle) {
		if (!dataCache.containsAnchorsForTriangle(triangle)) {
			List<TransformAnchor> anchorsForTriangle = new ArrayList<TransformAnchor>();
			if (triangle != null) {
				for (DPoint point : triangle.getPoints()) {
					double px = point.getX();
					double py = point.getY();

					for (TransformAnchor anchor : project.getAnchors()) {
						if (Math.abs(anchor.getX(phase) - px) < 0.01 && Math.abs(anchor.getY(phase) - py) < 0.01) {
							anchorsForTriangle.add(anchor);
						}
					}
				}
			}
			dataCache.putAnchorsForTriangle(triangle, anchorsForTriangle);
		}

		return dataCache.getAnchorsForTriangle(triangle);
	}		

	public TransformData getProject() {
		return project;
	}

	public void setSelectedAnchor(TransformAnchor anchor) {
		this.selectedAnchor = anchor;
	}

	public TransformAnchor getSelectedAnchor() {
		return selectedAnchor;
	}

	private long getStartTime() {
		return new Date().getTime();
	}

	private long getDuration(long startTime) {
		return new Date().getTime() - startTime;
	}
}