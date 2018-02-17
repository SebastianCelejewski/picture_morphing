package pl.sebcel.morph.gui;

import java.awt.image.BufferedImage;

public class ImageSet {

	private BufferedImage sourceImage;

	private BufferedImage targetImage;

	private BufferedImage sourceTransformedImage;

	private BufferedImage targetTransformedImage;

	private BufferedImage outputImage;

	public BufferedImage getSourceImage() {
		return sourceImage;
	}

	public void setSourceImage(BufferedImage sourceImage) {
		this.sourceImage = sourceImage;
	}

	public BufferedImage getTargetImage() {
		return targetImage;
	}

	public void setTargetImage(BufferedImage targetImage) {
		this.targetImage = targetImage;
	}

	public BufferedImage getSourceTransformedImage() {
		return sourceTransformedImage;
	}

	public void setSourceTransformedImage(BufferedImage sourceTransformedImage) {
		this.sourceTransformedImage = sourceTransformedImage;
	}

	public BufferedImage getTargetTransformedImage() {
		return targetTransformedImage;
	}

	public void setTargetTransformedImage(BufferedImage targetTransformedImage) {
		this.targetTransformedImage = targetTransformedImage;
	}

	public BufferedImage getOutputImage() {
		return outputImage;
	}

	public void setOutputImage(BufferedImage outputImage) {
		this.outputImage = outputImage;
	}

}