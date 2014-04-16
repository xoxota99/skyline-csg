package com.skyline.csg;

import java.util.*;

import javax.vecmath.*;

/**
 * 
 * ported from http://evanw.github.io/csg.js/
 * 
 * @author philippd
 */
public class CSG {

	protected List<Polygon> polygons = new ArrayList<Polygon>();
	static final double EPSILON = 1e-5;

	/**
	 * Return a new CSG solid representing space in either this solid or in the
	 * solid `csg`. Neither this solid nor the solid `csg` are modified.
	 * 
	 * 
	 * <pre>
	 *      A.union(B)
	 *  
	 *      +-------+            +-------+
	 *      |       |            |       |
	 *      |   A   |            |       |
	 *      |    +--+----+   =   |       +----+
	 *      +----+--+    |       +----+       |
	 *           |   B   |            |       |
	 *           |       |            |       |
	 *           +-------+            +-------+
	 * 
	 * </pre>
	 * 
	 * @param other
	 * @return
	 * @verified
	 */
	public CSG union(CSG other) {
		Node a = new Node(this.clone().polygons);
		Node b = new Node(other.clone().polygons);
		a.clipTo(b);
		b.clipTo(a);
		b.invert();
		b.clipTo(a);
		b.invert();
		a.build(b.allPolygons());
		return CSG.fromPolygons(a.allPolygons());
	}

	/**
	 * Return a new CSG solid representing space in this solid but not in the
	 * solid `csg`. Neither this solid nor the solid `csg` are modified.
	 * 
	 * <pre>
	 *      A.subtract(B)
	 *  
	 *      +-------+            +-------+
	 *      |       |            |       |
	 *      |   A   |            |       |
	 *      |    +--+----+   =   |    +--+
	 *      +----+--+    |       +----+
	 *           |   B   |
	 *           |       |
	 *           +-------+
	 * 
	 * </pre>
	 * 
	 * @param other
	 * @return
	 * @verified
	 */
	public CSG subtract(CSG other) {
		Node a = new Node(this.clone().polygons);
		Node b = new Node(other.clone().polygons);
		a.invert();
		a.clipTo(b);
		b.clipTo(a);
		b.invert();
		b.clipTo(a);
		b.invert();
		a.build(b.allPolygons());
		a.invert();
		return CSG.fromPolygons(a.allPolygons());
	}

	/**
	 * Return a new CSG solid representing space both this solid and in the
	 * solid `csg`. Neither this solid nor the solid `csg` are modified.
	 * 
	 * <pre>
	 *      A.intersect(B)
	 *  
	 *      +-------+
	 *      |       |
	 *      |   A   |
	 *      |    +--+----+   =   +--+
	 *      +----+--+    |       +--+
	 *           |   B   |
	 *           |       |
	 *           +-------+
	 * 
	 * </pre>
	 * 
	 * @param other
	 * @return
	 * @verified
	 */
	public CSG intersect(CSG other) {
		Node a = new Node(this.clone().polygons);
		Node b = new Node(other.clone().polygons);
		a.invert();
		b.clipTo(a);
		b.invert();
		a.clipTo(b);
		b.clipTo(a);
		a.build(b.allPolygons());
		a.invert();
		return CSG.fromPolygons(a.allPolygons());
	}

	/**
	 * Return a new CSG solid with solid and empty space switched. This solid is
	 * not modified.
	 * 
	 * @return
	 * @verified
	 */
	public CSG inverse() {
		CSG csg = this.clone();
		for (Polygon p : csg.polygons) {
			p.flip();
		}
		return csg;
	}

	public void translate(Vector3d v) {
		for (Polygon p : this.polygons) {
			p.translate(v);
		}
	}

	public void scale(Vector3d v) {
		for (Polygon p : this.polygons) {
			p.scale(v);
		}
	}

	/**
	 * Rotate according to x/y/z values in the provided Quaternion, around the
	 * world x/y/z axes, respectively. Rotation occurs in reverse order (Z
	 * first, then Y, then X).
	 * 
	 * @param rotation
	 *            A rotation quaternion. a Rotation quat has 4 dimensions that
	 *            describe the axis of rotation (in x, y, and z coordinates of a
	 *            point that the axis passes through), and a rotation, in
	 *            radians:
	 *            <ul>
	 *            <li>x - xRotation * sin(theta/2)</li>
	 *            <li>y - yRotation * sin(theta/2)</li>
	 *            <li>z - zRotation * sin(theta/2)</li>
	 *            <li>w - cos(theta/2)</li>
	 *            </ul>
	 * @return
	 */
	public void rotate(Quat4d rotation) {
		for (Polygon p : this.polygons) {
			p.rotate(rotation);
		}
	}

	public static CSG fromPolygons(List<Polygon> polygons) {
		CSG csg = new CSG();
		csg.polygons = polygons;
		return csg;
	}

	public List<Polygon> getPolygons() {
		return this.polygons;
	}

	public String toString() {
		String retval = "\n{\"polys\": [";
		int i = 0;
		for (Polygon p : polygons) {
			i++;
			retval += p.toString();
			if (i < polygons.size()) {
				retval += ", ";
			}
		}
		retval += "]}";
		return retval;
	}

	public CSG clone() {
		CSG csg = new CSG();
		for (Polygon p : polygons) {
			csg.polygons.add(p.clone());
		}
		return csg;
	}
}
