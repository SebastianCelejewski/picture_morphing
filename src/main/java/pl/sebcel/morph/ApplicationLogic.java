package pl.sebcel.morph;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import pl.sebcel.morph.engine.MorphingEngine;
import pl.sebcel.morph.gui.ImageSet;
import pl.sebcel.morph.gui.MainFrame;
import pl.sebcel.morph.gui.PicturePane.Role;
import pl.sebcel.morph.model.TransformAnchor;
import pl.sebcel.morph.model.TransformData;
import pl.sebcel.morph.utils.FileOperations;

public class ApplicationLogic {

	private MainFrame mainFrame;

	private MorphingEngine morphingEngine;

	private FileOperations fileOperations;

	private TransformData project;

	private ImageSet imageSet = new ImageSet();

	private double currentPhase = 0.5;

	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public void setMorphingEngine(MorphingEngine morphingEngine) {
		this.morphingEngine = morphingEngine;
	}

	public void setFileOperations(FileOperations fileOperations) {
		this.fileOperations = fileOperations;
	}

	public void createNewProject() {
		this.project = new TransformData();
		this.imageSet = new ImageSet();
		this.currentPhase = 0.5;
		this.morphingEngine.invalidateCache();
	}

	public void setProject(TransformData project) {
		this.project = project;
		this.imageSet = new ImageSet();
		this.morphingEngine.invalidateCache();
		imageSet.setSourceImage(fileOperations.loadImage(project.getSourceImagePath()));
		imageSet.setTargetImage(fileOperations.loadImage(project.getTargetImagePath()));
		this.currentPhase = 0.5;
		processImages();
		mainFrame.repaint();
	}

	public void setSourceImage(File file) {
		project.setSourceImagePath(file.getAbsolutePath());
		imageSet.setSourceImage(fileOperations.loadImage(project.getSourceImagePath()));
		mainFrame.repaint();
	}

	public void setTargetImage(File file) {
		project.setTargetImagePath(file.getAbsolutePath());
		imageSet.setTargetImage(fileOperations.loadImage(project.getTargetImagePath()));
		mainFrame.repaint();
	}

	public TransformData getProject() {
		return project;
	}

	public void closeProject() {
		this.project = null;
	}

	public List<TransformAnchor> getAnchors() {
		if (project != null) {
			return project.getAnchors();
		} else {
			return null;
		}
	}

	private void processImages() {
		morphingEngine.processImages(imageSet, project.getAnchors(), currentPhase);
	}

	public void addAnchor(TransformAnchor anchor) {
		project.getAnchors().add(anchor);
		morphingEngine.invalidateCache();
		mainFrame.repaint();
	}

	public void setPhase(double phase) {
		this.currentPhase = phase;
		processImages();
		mainFrame.repaint();
	}

	public double getPhase() {
		return currentPhase;
	}

	public BufferedImage getImage(Role role) {
		switch (role) {
		case SOURCE:
			return imageSet.getSourceImage();
		case TARGET:
			return imageSet.getTargetImage();
		case SOURCE_TRANSFORMED:
			return imageSet.getSourceTransformedImage();
		case TARGET_TRANSFORMED:
			return imageSet.getTargetTransformedImage();
		case OUTPUT:
			return imageSet.getOutputImage();
		}
		return null;
	}
}