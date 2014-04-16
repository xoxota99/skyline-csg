package com.skyline.csg;

import java.util.*;

import javax.vecmath.*;

import com.skyline.csg.geom.*;

/**
 * Represents a Plane as a Vector3d3 and a scalar. (Essentially, take the
 * normal, then travel along it for a scalar distance to arrive at the Plane
 * Point. Then reuse the normal for a point/normal definition of a plane).
 * 
 * ported from http://evanw.github.io/csg.js/
 * 
 * @author philippd
 * 
 */
public class Plane {
	Vector3d normal;
	double w;
/**
 * 
 * @param normal
 * @param w
 * @verified
 */
	public Plane(Vector3d normal, double w) {
		this.normal = new Vector3d(normal);
		this.w = w;
	};

	/**
	 * @verified
	 */
	public Plane clone() {
		return new Plane(new Vector3d(this.normal), this.w);
	}

	/**
	 * Flip a plane across the origin.
	 * @verified
	 */
	public void flip() {
		this.normal.negate();
		this.w = -this.w;
	}

	/**
	 * Split `polygon` by this plane if needed, then put the polygon or polygon
	 * fragments in the appropriate lists. Coplanar polygons go into either
	 * `coplanarFront` or `coplanarBack` depending on their orientation with
	 * respect to this plane. Polygons in front or in back of this plane go into
	 * either `front` or `back`.
	 * 
	 * @param polygon
	 * @param coplanarFront
	 * @param coplanarBack
	 * @param front
	 * @param back
	 * @verified
	 */
	public void splitPolygon(Polygon polygon, List<Polygon> coplanarFront, List<Polygon> coplanarBack, List<Polygon> front, List<Polygon> back) {
		final int COPLANAR = 0; // on the plane.
		final int FRONT = 1; // in front of the plane.
		final int BACK = 2; // behind the plane.
		final int SPANNING = 3; // spanning the plane (partially in
												// front, partially in back). Really
												// only applies to polys.

		// Classify each point as well as the entire polygon into one of the
		// above four classes.
		int polygonType = COPLANAR;
		int[] types = new int[polygon.vertices.size()];
//		List<Integer> types = new ArrayList<Integer>();
		for (int i=0;i<polygon.vertices.size();i++){
			Vertex v =polygon.vertices.get(i);
			double t = this.normal.dot(v.pos) - this.w;
			int type = (t < -CSG.EPSILON) ? BACK : (t > CSG.EPSILON) ? FRONT : COPLANAR;
			polygonType |= type;
			types[i]=type;
		}

		// Put the polygon in the correct list, splitting it when necessary.
		switch (polygonType) {
		case COPLANAR:
			//All the points in the poly were between -EPSILON and EPSILON.
			(this.normal.dot(polygon.plane.normal) > 0 ? coplanarFront : coplanarBack).add(polygon);
			break;
		case FRONT:
			front.add(polygon);
			break;
		case BACK:
			back.add(polygon);
			break;
		case SPANNING:
			List<Vertex> f = new ArrayList<Vertex>();
			List<Vertex> b = new ArrayList<Vertex>();
			for (int i = 0; i < polygon.vertices.size(); i++) {
				int j = (i + 1) % polygon.vertices.size(); //circular
				int ti = types[i], tj = types[j];
				Vertex vi = polygon.vertices.get(i);
				Vertex vj = polygon.vertices.get(j);
				if (ti != BACK)
					f.add(vi);
				if (ti != FRONT)
					b.add(ti != BACK ? vi.clone() : vi);
				if ((ti | tj) == SPANNING) {
					Vector3d posDelta = new Vector3d(vj.pos);
					posDelta.sub(vi.pos);
					double t = (this.w - this.normal.dot(vi.pos)) / this.normal.dot(posDelta);
					Vertex v = vi.interpolate(vj, t);
					f.add(v);
					b.add(v.clone());
				}
			}
			if (f.size() >= 3) //more than 3 vertices...
				front.add(new Polygon(f, polygon.shared));
			if (b.size() >= 3) //more than 3 vertices...
				back.add(new Polygon(b, polygon.shared));
			break;
		}
	}
/**
 * Create a plan from three points.
 * 
 * @param a
 * @param b
 * @param c
 * @return
 * @verified
 */
	public static Plane fromPoints(Vector3d a, Vector3d b, Vector3d c) {

		Vector3d b2 = new Vector3d();
		b2.sub(b, a);

		Vector3d c2 = new Vector3d();
		c2.sub(c, a);

		Vector3d n = new Vector3d();
		n.cross(b2, c2);
		n.normalize();

		double w = n.dot(a);
		return new Plane(n, w);
	}

	public void translate(Vector3d v) {
		// from
		// http://www.gamedev.net/topic/358624-how-to-translate-a-plane-defined-as-normal-and-distance/
		Vector3d norm = new Vector3d(normal);
		norm.scale(w);

		// not sure about this.
		double d = (norm.x * v.x + norm.y * v.y + norm.z * v.z) / Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
		w += d;
	}

	public void rotate(Quat4d rotation) {
		// just rotate the normal around the origin.
		Util.rotate(normal, rotation);
	}

	public void scale(Vector3d scaleFactor) {
		// get three points on the plane, scale them, then recalculate the
		// plane.
		Vector3d a = new Vector3d(this.normal);
		a.normalize();
		a.scale(this.w);
		// a is now a point on the plane. specifically, it's the point on the
		// plane closest to the origin.

		Vector3d b = new Vector3d(), c = new Vector3d();

		// TODO: What about the corner case where two of the three components
		// are zero? I think we handle that automatically.
		if (normal.x == 0) {
			// special case
			b.set(normal.x, -normal.z, normal.y); // rotate the normal
													// orthogonally.
			c.set(normal.x, -normal.y, -normal.z);
		} else if (normal.y == 0) {
			// special case
			b.set(-normal.z, normal.y, normal.x); // rotate the normal
													// orthogonally.
			c.set(-normal.x, normal.y, -normal.z);
		} else if (normal.z == 0) {
			// special case
			b.set(-normal.y, normal.x, normal.z); // rotate the normal
													// orthogonally.
			c.set(-normal.x, -normal.y, normal.z);
		} else {
			b.set(-normal.y, normal.x, normal.z); // rotate the normal
													// orthogonally.
			c.set(normal.x, -normal.z, normal.y);
		}

		b.add(a); // add a, so the new vector is coplanar.
		c.add(a);

		a.set(a.x * scaleFactor.x, a.y * scaleFactor.y, a.z * scaleFactor.z);
		b.set(b.x * scaleFactor.x, b.y * scaleFactor.y, b.z * scaleFactor.z);
		c.set(c.x * scaleFactor.x, c.y * scaleFactor.y, c.z * scaleFactor.z);

		Plane p = fromPoints(a, b, c);

		// all that trouble, just for this.
		this.normal.set(p.normal);
		this.w = p.w;
	}
}
