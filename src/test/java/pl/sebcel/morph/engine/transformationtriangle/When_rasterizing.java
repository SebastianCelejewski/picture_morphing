package pl.sebcel.morph.engine.transformationtriangle;

import java.awt.Point;
import java.util.List;

import org.jdelaunay.delaunay.geometries.DPoint;
import org.jdelaunay.delaunay.geometries.DTriangle;
import org.junit.Assert;
import org.junit.Test;

import pl.sebcel.morph.engine.TransformationTriangle;

public class When_rasterizing {

	@Test
	public void should_return_pixels_inside_the_triangle() throws Exception {
		DPoint p1 = new DPoint(2, 4, 0);
		DPoint p2 = new DPoint(4, 2, 0);
		DPoint p3 = new DPoint(6, 6, 0);

		DTriangle triangle = new DTriangle(p1, p2, p3);

		List<Point> pixels = TransformationTriangle.rasterize(triangle);

		Assert.assertEquals(10, pixels.size());
		assertContains(pixels, 2, 4);
		assertContains(pixels, 3, 3);
		assertContains(pixels, 3, 4);
		assertContains(pixels, 4, 2);
		assertContains(pixels, 4, 3);
		assertContains(pixels, 4, 4);
		assertContains(pixels, 4, 5);
		assertContains(pixels, 5, 4);
		assertContains(pixels, 5, 5);
		assertContains(pixels, 6, 6);
	}

	private void assertContains(List<Point> pixels, int x, int y) {
		Assert.assertTrue(pixels.contains(new Point(x, y)));
	}
}