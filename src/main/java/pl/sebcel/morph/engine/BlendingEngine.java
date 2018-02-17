package pl.sebcel.morph.engine;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class BlendingEngine {
	public BufferedImage blendTransformedImages(BufferedImage sourceImage, BufferedImage targetImage, double phase) {
		if (sourceImage == null || targetImage == null) {
			return null;
		}
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();
		int type = sourceImage.getType();

		BufferedImage result = new BufferedImage(width, height, type);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				int sourceRGB = sourceImage.getRGB(x, y);
				int targetRGB = targetImage.getRGB(x, y);

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
}