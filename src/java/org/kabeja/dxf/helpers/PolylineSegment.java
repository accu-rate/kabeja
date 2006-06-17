/*
 * Created on 27.10.2005
 *
 */
package org.kabeja.dxf.helpers;

import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFVertex;

/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class PolylineSegment {
	private boolean bulged = false;

	private Point point1 = new Point();

	private Point point2 = new Point();

	private Point point3 = new Point();

	private Point point4 = new Point();

	private double radius;

	private double bulgeHeight;



	private DXFVertex start;

	private DXFPolyline p;

	public static double DELTA=0.001;

	public PolylineSegment(DXFVertex start, DXFVertex end, DXFPolyline p) {
	this.start =  start;

		this.p = p;
		if (this.start.getBulge() != 0.0) {

			double l = MathUtils.distance(start.getPoint(), end.getPoint());
			// do nothing if the points are the same

			this.radius = getRadius(Math.abs(start.getBulge()), l);
			this.bulgeHeight = start.getBulge() * l / 2;

			setBulged(true);
			createCurvedTrapezium(start, end, this.radius,l);
		} else {
			createTrapezium(start, end);
		}
	}

	/**
	 * @return Returns the bulge.
	 */
	public double getBulge() {
		return this.start.getBulge();
	}


	/**
	 * @return Returns the bulged.
	 */
	public boolean isBulged() {
		return bulged;
	}

	/**
	 * @param bulged
	 *            The bulged to set.
	 */
	public void setBulged(boolean bulged) {
		this.bulged = bulged;
	}

	/**
	 * @return Returns the point1.
	 */
	public Point getPoint1() {
		return point1;
	}

	/**
	 * @param point1
	 *            The point1 to set.
	 */
	public void setPoint1(Point point1) {
		this.point1 = point1;
	}

	/**
	 * @return Returns the point2.
	 */
	public Point getPoint2() {
		return point2;
	}

	/**
	 * @param point2
	 *            The point2 to set.
	 */
	public void setPoint2(Point point2) {
		this.point2 = point2;
	}

	/**
	 * @return Returns the point3.
	 */
	public Point getPoint3() {
		return point3;
	}

	/**
	 * @param point3
	 *            The point3 to set.
	 */
	public void setPoint3(Point point3) {
		this.point3 = point3;
	}

	/**
	 * @return Returns the point4.
	 */
	public Point getPoint4() {
		return point4;
	}

	/**
	 * @param point4
	 *            The point4 to set.
	 */
	public void setPoint4(Point point4) {
		this.point4 = point4;
	}

	/**
	 * @return Returns the radius.
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * @param radius
	 *            The radius to set.
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	protected void createCurvedTrapezium(DXFVertex start, DXFVertex end,
			double radius, double length) {

		// first get the center point of the arc

		// middle point of chord
		double s = length / 2;
		Vector edgeDirection = MathUtils.getVector(start.getPoint(), end
				.getPoint());
		edgeDirection = MathUtils.normalize(edgeDirection);
		Point mp = MathUtils.getPointOfStraightLine(start.getPoint(),
				edgeDirection, s);

		double t = Math.sqrt(Math.pow(radius, 2) - Math.pow(s, 2));

		Vector d = p.getExtrusion().getNormal();
		d = MathUtils.crossProduct(d, edgeDirection);
		d = MathUtils.normalize(d);
		boolean right = false;
		// the centerPoint
		if (start.getBulge() < 0) {
			// is on the left side of the chord
			t = (-1) * t;
			right = true;
		}

		// the center point of the arc
		mp = MathUtils.getPointOfStraightLine(mp, d, t);

		double c = 0.0;
		if (start.getStartWidth() > 0.0) {
			c = start.getStartWidth() / 2;
		} else {
			c = p.getStartWidth() / 2;
		}

		double fix = 1.0;
		if (right) {
			fix = -1.0;
		}

		// direction vector from start to center point
		d = MathUtils.getVector(start.getPoint(), mp);
		d = MathUtils.normalize(d);
		point1 = MathUtils.getPointOfStraightLine(start.getPoint(), d,
				(fix * c));
		point2 = MathUtils.getPointOfStraightLine(start.getPoint(), d,
				(fix * c * (-1)));

		if (start.getEndWidth() > 0.0) {
			c = start.getEndWidth() / 2;
		} else {
			c = this.p.getEndWidth() / 2;
		}
		d = MathUtils.getVector(end.getPoint(), mp);
		d = MathUtils.normalize(d);
		point3 = MathUtils.getPointOfStraightLine(end.getPoint(), d,
				(fix * c * (-1)));
		point4 = MathUtils.getPointOfStraightLine(end.getPoint(), d,
				(fix * c));

	}

	protected void createTrapezium(DXFVertex start, DXFVertex end) {

		// we start at the start side
		double c = 0.0;
		if (start.getStartWidth() > 0.0) {
			c = start.getStartWidth() / 2;

		} else {
			c = this.p.getStartWidth() / 2;

		}

		Vector v = this.p.getExtrusion().getNormal();

		// Vector v = DXFConstants.DEFAULT_Z_AXIS_VECTOR;
		Vector x = MathUtils.getVector(start.getPoint(), end.getPoint());


		// calculate the y vector
		v = MathUtils.crossProduct(v, x);
		v = MathUtils.normalize(v);
		point1 = MathUtils.getPointOfStraightLine(start.getPoint(), v, c);
		point2 = MathUtils.getPointOfStraightLine(start.getPoint(), v,
				(-1.0 * c));


		// on the end side
		if (start.getEndWidth() > 0.0) {
			c = start.getEndWidth() / 2;

		} else {
			c = this.p.getEndWidth() / 2;

		}

		point3 = MathUtils
				.getPointOfStraightLine(end.getPoint(), v, (-1.0 * c));
		point4 = MathUtils.getPointOfStraightLine(end.getPoint(), v, c);



	}

	/**
	 * Caculate the radius of a cut circle segment between 2 DXFVertex
	 *
	 * @param bulge
	 *            the vertex bulge
	 * @param length
	 *            the length of the circle cut
	 */

	protected double getRadius(double bulge, double length) {

		double h = bulge * length / 2;
		double value = h / 2 + Math.pow(length, 2) / (8 * h);
		return value;
	}

	/**
	 * @return Returns the bulgeHeight.
	 */
	public double getBulgeHeight() {
		return bulgeHeight;
	}

	public void connect(PolylineSegment next) {

		// connect only if the angle between the
		// segments is > 0

		// first connection point
		Vector d1 = MathUtils.getVector(point1, point4);
		Vector d2 = MathUtils.getVector(next.getPoint4(), next.getPoint1());
		double angle = MathUtils.getAngle(d1, d2);
		if (Math.abs(angle) > DELTA) {

			Point p = MathUtils.getIntersection(point1, d1, next.getPoint4(),
					d2);
			setPoint4(p);


			next.setPoint1(p);

			d1 = MathUtils.getVector(point2, point3);
			d2 = MathUtils.getVector(next.getPoint3(), next.getPoint2());
			p = MathUtils.getIntersection(point2, d1, next.getPoint3(), d2);
			setPoint3(p);
			next.setPoint2(p);

		}


	}


	public double getInnerRadius(){
		double r = (this.start.getStartWidth()+this.start.getEndWidth())/2;
		if(r == 0.0){
			r = (this.p.getStartWidth()+this.p.getEndWidth())/2;
		}
		return getRadius() - r;
	}

}