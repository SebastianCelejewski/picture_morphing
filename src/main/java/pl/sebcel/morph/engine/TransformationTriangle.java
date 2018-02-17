package pl.sebcel.morph.engine;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.jdelaunay.delaunay.error.DelaunayError;
import org.jdelaunay.delaunay.geometries.DPoint;
import org.jdelaunay.delaunay.geometries.DTriangle;

import com.vividsolutions.jts.geom.Coordinate;

import pl.sebcel.morph.model.TransformAnchor;

public class TransformationTriangle {

	private List<TransformAnchor> anchors;

	public TransformationTriangle(List<TransformAnchor> anchors) {
		this.anchors = anchors;
	}

	public List<Point> rasterize(double phase) {
		DTriangle dTriangle = getDTriangleForPhase(phase);
		return rasterize(dTriangle);
	}

	public static List<Point> rasterize(DTriangle triangle) {
		int minX = triangle.getPoints().stream().map(x -> x.getX()).sorted().findFirst().get().intValue();
		int maxX = triangle.getPoints().stream().map(x -> x.getX()).sorted().skip(2).findFirst().get().intValue();
		int minY = triangle.getPoints().stream().map(x -> x.getY()).sorted().findFirst().get().intValue();
		int maxY = triangle.getPoints().stream().map(x -> x.getY()).sorted().skip(2).findFirst().get().intValue();

		List<Point> pixels = new ArrayList<Point>();

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				try {
					if (triangle.contains(new Coordinate(x, y, 0))) {
						pixels.add(new Point(x, y));
					}
				} catch (Exception ex) { // clean it
				}
			}
		}

		return pixels;

	}

	public DTriangle getDTriangleForPhase(double phase) {
		try {
			DPoint p1 = new DPoint(anchors.get(0).getX(phase), anchors.get(0).getY(phase), 0);
			DPoint p2 = new DPoint(anchors.get(1).getX(phase), anchors.get(1).getY(phase), 0);
			DPoint p3 = new DPoint(anchors.get(2).getX(phase), anchors.get(2).getY(phase), 0);

			return new DTriangle(p1, p2, p3);
		} catch (DelaunayError ex) {
			throw new RuntimeException("Failed to convert TranformationTriangle into DTriangle: " + ex.getMessage(), ex);
		}
	}

	@Override
	public String toString() {
		return "(" + anchors.get(0).getX(0) + "," + anchors.get(0).getY(0) + "),(" + anchors.get(1).getX(0) + "," + anchors.get(1).getY(0) + "),(" + anchors.get(2).getX(0) + "," + anchors.get(2).getY(0) + "),";
	}
}