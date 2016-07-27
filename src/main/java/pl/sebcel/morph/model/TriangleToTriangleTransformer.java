package pl.sebcel.morph.model;

import org.jdelaunay.delaunay.geometries.DTriangle;

public class TriangleToTriangleTransformer {

    private double a11, a12, a21, a22, dx, dy;

    // [ x1 ] = [ a11 a12 ] [ x ] + [dx]
    // [ y1 ] [ a21 a22 ] [ y ] [dy]

    public TriangleToTriangleTransformer(DTriangle t1, DTriangle t2) {
        double ax = t1.getPoint(0).getX();
        double ay = t1.getPoint(0).getY();

        double ax1 = t2.getPoint(0).getX();
        double ay1 = t2.getPoint(0).getY();

        double acx1 = t2.getPoint(0).getX() - t2.getPoint(2).getX();
        double abx1 = t2.getPoint(0).getX() - t2.getPoint(1).getX();

        double abx = t1.getPoint(0).getX() - t1.getPoint(1).getX();
        double aby = t1.getPoint(0).getY() - t1.getPoint(1).getY();
        double acx = t1.getPoint(0).getX() - t1.getPoint(2).getX();
        double acy = t1.getPoint(0).getY() - t1.getPoint(2).getY();

        double acy1 = t2.getPoint(0).getY() - t2.getPoint(2).getY();
        double aby1 = t2.getPoint(0).getY() - t2.getPoint(1).getY();

        a12 = (acx1 * abx - abx1 * acx) / (acy * abx - aby * acx);
        a11 = abx1 / abx - a12 * aby / abx;

        a22 = (aby1 * acx - acy1 * abx) / (aby * acx - acy * abx);
        a21 = aby1 / abx - a22 * aby / abx;

        dx = ax1 - a11 * ax - a12 * ay;
        dy = ay1 - a21 * ax - a22 * ay;
    }

    public double transformX(double x, double y) {
        return a11 * x + a12 * y + dx;
    }

    public double transformY(double x, double y) {
        return a21 * x + a22 * y + dy;
    }
}
