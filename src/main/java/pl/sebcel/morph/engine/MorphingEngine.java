package pl.sebcel.morph.engine;

import java.awt.image.BufferedImage;
import java.util.List;

import pl.sebcel.morph.gui.ImageSet;
import pl.sebcel.morph.model.TransformAnchor;

public class MorphingEngine {

	private TriangulationUtils triangulationUtils = new TriangulationUtils();
	private TransformationEngine transformationEngine = new TransformationEngine();
	private BlendingEngine blendingEngine = new BlendingEngine();
	
	private DataCache dataCache = new DataCache();

	public void processImages(ImageSet imageSet, List<TransformAnchor> anchors, double phase) {
		if (dataCache.containsImagesForPhase(phase)) {
			imageSet.setSourceTransformedImage(dataCache.getSourceTransformedImageForPhase(phase));
			imageSet.setTargetTransformedImage(dataCache.getTargetTransformedImageForPhase(phase));
			imageSet.setOutputImage(dataCache.getOutputTransformedImageForPhase(phase));
		} else {
			List<TransformationTriangle> triangles = triangulationUtils.triangulate(anchors);
			BufferedImage sourceTransformedImage = transformationEngine.transformImage(imageSet.getSourceImage(), triangles, 0, phase);
			BufferedImage targetTransformedImage = transformationEngine.transformImage(imageSet.getTargetImage(), triangles, 1, phase);
			BufferedImage outputImage = blendingEngine.blendTransformedImages(sourceTransformedImage, targetTransformedImage, phase);
			imageSet.setSourceTransformedImage(sourceTransformedImage);
			imageSet.setTargetTransformedImage(targetTransformedImage);
			imageSet.setOutputImage(outputImage);
			
			dataCache.putImagesForPhase(phase, sourceTransformedImage, targetTransformedImage, outputImage);
		}
	}
	
	public void invalidateCache() {
		dataCache.clearAll();
	}
}