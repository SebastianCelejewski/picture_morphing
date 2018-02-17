package pl.sebcel.morph.engine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import pl.sebcel.morph.gui.MainFrame;
import pl.sebcel.morph.gui.PicturePane;
import pl.sebcel.morph.model.TransformAnchor;
import pl.sebcel.morph.model.TransformData;

public class MorphingEngine {

	private TriangulationUtils triangulationUtils = new TriangulationUtils();
	private TransformationEngine transformationEngine = new TransformationEngine();
	private BlendingEngine blendingEngine = new BlendingEngine();

	private BufferedImage sourceImage;

	private BufferedImage targetImage;

	private BufferedImage sourceTransformedImage;

	private BufferedImage targetTransformedImage;

	private BufferedImage outputImage;

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
		try {
			processImages();
		} catch (Exception ex) {
			ex.printStackTrace(); // clean it
		}
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
				List<TransformationTriangle> triangles = triangulationUtils.triangulate(project.getAnchors());
				sourceTransformedImage = transformationEngine.transformImage(sourceImage, triangles, 0, phase);
				targetTransformedImage = transformationEngine.transformImage(targetImage, triangles, 1, phase);
				outputImage = blendingEngine.blendTransformedImages(sourceTransformedImage, targetTransformedImage, phase);
				dataCache.putImagesForPhase(phase, sourceTransformedImage, targetTransformedImage, outputImage);
			}
		}
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
}