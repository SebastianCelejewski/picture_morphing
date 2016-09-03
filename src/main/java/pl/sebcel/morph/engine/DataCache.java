package pl.sebcel.morph.engine;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdelaunay.delaunay.geometries.DTriangle;

import pl.sebcel.morph.model.TransformAnchor;
import pl.sebcel.morph.model.TriangleToTriangleTransformer;

public class DataCache {

	public static class CacheKey {
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

	private Map<DTriangle, List<TransformAnchor>> anchorsForTriangles = new HashMap<DTriangle, List<TransformAnchor>>();

	private Map<CacheKey, TriangleToTriangleTransformer> transformers1 = new HashMap<CacheKey, TriangleToTriangleTransformer>();

	private Map<CacheKey, TriangleToTriangleTransformer> transformers2 = new HashMap<CacheKey, TriangleToTriangleTransformer>();

	private Map<Double, BufferedImage> imagesCache = new HashMap<Double, BufferedImage>();

	private Map<Double, List<int[]>> trianglesCache = new HashMap<Double, List<int[]>>();

	public void clearAll() {
		anchorsForTriangles.clear();
		transformers1.clear();
		transformers2.clear();
		imagesCache.clear();
		trianglesCache.clear();
	}

	public boolean containsAnchorsForTriangle(DTriangle triangle) {
		return anchorsForTriangles.containsKey(triangle);
	}

	public void putAnchorsForTriangle(DTriangle triangle, List<TransformAnchor> anchorsForTriangle) {
		anchorsForTriangles.put(triangle, anchorsForTriangle);
	}

	public List<TransformAnchor> getAnchorsForTriangle(DTriangle triangle) {
		return anchorsForTriangles.get(triangle);
	}

	public boolean containsImagesForPhase(double phase) {
		return imagesCache.containsKey(phase);
	}

	public void putImagesForPhase(double phase, BufferedImage sourceTransformedImage, BufferedImage targetTransformedImage, BufferedImage outputImage) {
		imagesCache.put(phase, null);
		imagesCache.put(phase + 2, sourceTransformedImage);
		imagesCache.put(phase + 4, targetTransformedImage);
		imagesCache.put(phase + 6, outputImage);
	}

	public void putTrianglesForPhase(double phase, List<int[]> sourceTrianglesEdges, List<int[]> targetTrianglesEdges, List<int[]> currentTrianglesEdges) {
		trianglesCache.put(phase + 2, sourceTrianglesEdges);
		trianglesCache.put(phase + 4, targetTrianglesEdges);
		trianglesCache.put(phase + 6, currentTrianglesEdges);
	}

	public BufferedImage getSourceTransformedImageForPhase(double phase) {
		return imagesCache.get(phase + 2);
	}

	public BufferedImage getTargetTransformedImageForPhase(double phase) {
		return imagesCache.get(phase + 4);
	}

	public BufferedImage getOutputTransformedImageForPhase(double phase) {
		return imagesCache.get(phase + 6);
	}

	public List<int[]> getSourceTrianglesForPhase(double phase) {
		return trianglesCache.get(phase + 2);
	}

	public List<int[]> getTargetTrianglesForPhase(double phase) {
		return trianglesCache.get(phase + 4);
	}

	public List<int[]> getCurrentTrianglesForPhase(double phase) {
		return trianglesCache.get(phase + 6);
	}

	public boolean containsSourceImageTransformers(CacheKey key) {
		return transformers1.containsKey(key);
	}

	public boolean containsTargetImageTransformers(CacheKey key) {
		return transformers2.containsKey(key);
	}

	public void putsSourceImageTransformers(CacheKey key, TriangleToTriangleTransformer transformer) {
		transformers1.put(key, transformer);
	}

	public void putsTargetImageTransformers(CacheKey key, TriangleToTriangleTransformer transformer) {
		transformers2.put(key, transformer);
	}

	public TriangleToTriangleTransformer getSourceImageTransformers(CacheKey key) {
		return transformers1.get(key);
	}

	public TriangleToTriangleTransformer getTargetImageTransformers(CacheKey key) {
		return transformers2.get(key);
	}

}