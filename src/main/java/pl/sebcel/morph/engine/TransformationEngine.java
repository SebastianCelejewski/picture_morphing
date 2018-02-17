package pl.sebcel.morph.engine;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

import org.jdelaunay.delaunay.geometries.DTriangle;

import pl.sebcel.morph.model.TriangleToTriangleTransformer;

public class TransformationEngine {

	public BufferedImage transformImage(BufferedImage sourceImage, List<TransformationTriangle> triangles, double fromPhase, double toPhase) {
		BufferedImage resultImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), sourceImage.getType());

		for (TransformationTriangle triangle : triangles) {
			transformImageFragment(sourceImage, resultImage, triangle, fromPhase, toPhase);
		}

		return resultImage;
	}

	private void transformImageFragment(BufferedImage sourceImage, BufferedImage resultImage, TransformationTriangle triangle, double fromPhase, double toPhase) {
		List<Point> trianglePixels = triangle.rasterize(toPhase);
		TriangleToTriangleTransformer transformer = getTransformer(triangle, toPhase, fromPhase);

		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();

		for (Point targetPixel : trianglePixels) {
			Point sourcePixel = transformer.transform(targetPixel);

			int sx = cropToBounds(sourcePixel.x, 0, width - 1);
			int sy = cropToBounds(sourcePixel.y, 0, height - 1);
			int tx = cropToBounds(targetPixel.x, 0, width - 1);
			int ty = cropToBounds(targetPixel.y, 0, height - 1);

			int rgb = sourceImage.getRGB(sx, sy);
			resultImage.setRGB(tx, ty, rgb);
		}
	}

	private int cropToBounds(int value, int minValue, int maxValue) {
		if (value < minValue) {
			value = minValue;
		}
		if (value > maxValue) {
			value = maxValue;
		}
		return value;
	}

	private TriangleToTriangleTransformer getTransformer(TransformationTriangle triangle, double fromPhase, double toPhase) {
		DTriangle t1 = triangle.getDTriangleForPhase(toPhase);
		DTriangle t2 = triangle.getDTriangleForPhase(fromPhase);

		TriangleToTriangleTransformer transformer = new TriangleToTriangleTransformer(t2, t1);
		return transformer;
	}
}