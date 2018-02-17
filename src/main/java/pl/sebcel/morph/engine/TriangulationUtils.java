package pl.sebcel.morph.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jdelaunay.delaunay.ConstrainedMesh;
import org.jdelaunay.delaunay.geometries.DPoint;
import org.jdelaunay.delaunay.geometries.DTriangle;

import pl.sebcel.morph.model.TransformAnchor;

public class TriangulationUtils {

	/**
	 * Splits image area into triangles using transformation anchors defined by user
	 */
	public List<TransformationTriangle> triangulate(List<TransformAnchor> transformationAnchors) {
		if (transformationAnchors.size() < 3) {
			return new ArrayList<TransformationTriangle>(); // clean it
		}

		try { // clean it
			ConstrainedMesh mesh = new ConstrainedMesh();
			for (TransformAnchor anchor : transformationAnchors) {
				double x = anchor.getX(0);
				double y = anchor.getY(0);
				DPoint point = new DPoint(x, y, 0);
				mesh.addPoint(point);
			}
			mesh.processDelaunay();
			List<TransformationTriangle> result = mesh.getTriangleList().stream().map(x -> convertDTriangleToTransformationTriangle(x, transformationAnchors)).collect(Collectors.toList());
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			return new ArrayList<TransformationTriangle>(); // clean it
		}
	}

	/**
	 * Converts DTriangle object used by org.jdelaunay.delaunay to TransformationTriangle object
	 */
	public TransformationTriangle convertDTriangleToTransformationTriangle(DTriangle triangle, List<TransformAnchor> transformationAnchors) {
		List<TransformAnchor> anchorsForTriangle = new ArrayList<TransformAnchor>();
		for (DPoint point : triangle.getPoints()) {
			double px = point.getX();
			double py = point.getY();

			for (TransformAnchor anchor : transformationAnchors) {
				if (Math.abs(anchor.getX(0.0) - px) < 0.01 && Math.abs(anchor.getY(0.0) - py) < 0.01) {
					anchorsForTriangle.add(anchor);
				}
			}
		}
		return new TransformationTriangle(anchorsForTriangle);
	}
}
