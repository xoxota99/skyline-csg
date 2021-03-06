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
	public String name = "CSG:" + System.currentTimeMillis();

	public CSG union(CSG other, String name) {
		CSG retval = this.union(other);
		retval.name = name;
		return retval;
	}

	public CSG subtract(CSG other, String name) {
		CSG retval = this.subtract(other);
		retval.name = name;
		return retval;
	}

	public CSG intersect(CSG other, String name) {
		CSG retval = this.intersect(other);
		retval.name = name;
		return retval;
	}

	public CSG scale(Vector3d v, String name) {
		CSG retval = this.scale(v);
		retval.name = name;
		return retval;
	}

	public CSG translate(Vector3d v, String name) {
		CSG retval = this.translate(v);
		retval.name = name;
		return retval;
	}

	public CSG translate(double x, double y, double z, String name) {
		CSG retval = this.translate(x,y,z);
		retval.name = name;
		return retval;
	}

	public CSG rotate(Quat4d rotation, String name) {
		CSG retval = this.rotate(rotation);
		retval.name = name;
		return retval;
	}

	public CSG rotate(float xAngle, float yAngle, float zAngle, String name) {
		CSG retval = this.rotate(xAngle, yAngle, zAngle);
		retval.name = name;
		return retval;
	}

	public static CSG fromPolygons(List<Polygon> polygons, String name) {
		CSG retval = fromPolygons(polygons);
		retval.name = name;
		return retval;
	}

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
		if (this.polygons == null || this.polygons.size() == 0) {
			return other;
		} else if (other.polygons == null || other.polygons.size() == 0) {
			return this;
		}
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
	 * @return the intersection of A and B, or this solid, if {@link other} is
	 *         null, or consists of no polygons.
	 * @verified
	 */
	public CSG subtract(CSG other) {
		if (this.polygons == null || this.polygons.size() == 0
				|| other.polygons == null || other.polygons.size() == 0) {
			return this;
		}
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
	 * @return the intersection of A and B, or no-op, if either {@link other},
	 *         or consists of no polygons.
	 * @verified
	 */
	public CSG intersect(CSG other) {
		if (this.polygons == null || this.polygons.size() == 0) {
			return other; // intersection with the universe.
		} else if (other == null || other.polygons == null || other.polygons.size() == 0) {
			return this; // intersection with the universe.
		}
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
	 * This operator is a bit special, and can only be aplied in the context of
	 * another operator. For example, creating a sphere, and then inverting it
	 * will result in a nonsense polyhedron, with no bounds.
	 * 
	 * @return
	 * @verified
	 */
	public CSG inverse() {
		// If we just blindly flip every poly, shared vertices will be flipped
		// more than once.
		// Instead, build a set of vertices, flip them, then flip the poly
		// plane.
		CSG csg = this.clone();
		Set<Vertex> vSet = new HashSet<Vertex>();
		for (Polygon p : csg.polygons) {
			Collections.reverse(p.vertices);
			vSet.addAll(p.vertices);
			p.plane.flip();
		}

		for (Vertex vx : vSet) {
			vx.flip();
		}
		return csg;
	}

	public CSG translate(double x, double y, double z) {
		CSG retval = new CSG(this);
		// if we just blindly translate every polygon, shared vertices will be
		// translated more than once.
		// Instead, build a set of all vertices, and translate them.
		Set<Vertex> vSet = new HashSet<Vertex>();
		for (Polygon p : retval.polygons) {
			vSet.addAll(p.vertices);
		}

		for (Vertex vx : vSet) {
			vx.translate(x, y, z);
		}
		return retval;
	}

	public CSG translate(Vector3d v) {
		return translate(v.x, v.y, v.z);
	}

	public CSG scale(Vector3d v) {
		CSG retval = new CSG(this);

		// if we just blindly scale every polygon, shared vertices will be
		// scaled more than once.
		// Instead, build a set of all vertices, and scale them.
		Set<Vertex> vSet = new HashSet<Vertex>();
		for (Polygon p : retval.polygons) {
			vSet.addAll(p.vertices);
		}

		for (Vertex vx : vSet) {
			vx.scale(v);
		}

		for (Polygon p : retval.polygons) {
			// Scaling normals is a pain in the ass. Just rebuild the plane.
			// Can't replace the existing reference, though, since it might be
			// held
			// elsewhere. Instead just update it.
			Plane pl = Plane.fromPoints(p.vertices.get(0).pos, p.vertices.get(1).pos, p.vertices.get(2).pos);
			p.plane.normal.set(pl.normal.x, pl.normal.y, pl.normal.z);
			p.plane.w = pl.w;
		}
		return retval;
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
	public CSG rotate(Quat4d rotation) {
		CSG retval = new CSG(this);
		// if we just blindly rotate every polygon, shared vertices will be
		// rotated more than once.
		// Instead, build a set of all vertices, and rotate them.
		Set<Vertex> vSet = new HashSet<Vertex>();
		for (Polygon p : retval.polygons) {
			p.plane.rotate(rotation);
			vSet.addAll(p.vertices);
		}

		for (Vertex vx : vSet) {
			vx.rotate(rotation);
		}
		return retval;
	}

	/**
	 * Rotates by the xAngle, yAngle and zAngle angles (in radians), (aka pitch,
	 * yaw, roll) in the local coordinate space.
	 * 
	 */
	public CSG rotate(float xAngle, float yAngle, float zAngle) {
		Quat4d q = quatFromAngles(xAngle, yAngle, zAngle);
		return rotate(q);
	}

	private Quat4d quatFromAngles(float xAngle, float yAngle, float zAngle) {

		Quat4d q = new Quat4d();
		float angle;
		float sinY, sinZ, sinX, cosY, cosZ, cosX;
		angle = zAngle * 0.5f;
		sinZ = (float) Math.sin(angle);
		cosZ = (float) Math.cos(angle);
		angle = yAngle * 0.5f;
		sinY = (float) Math.sin(angle);
		cosY = (float) Math.cos(angle);
		angle = xAngle * 0.5f;
		sinX = (float) Math.sin(angle);
		cosX = (float) Math.cos(angle);

		// variables used to reduce multiplication calls.
		float cosYXcosZ = cosY * cosZ;
		float sinYXsinZ = sinY * sinZ;
		float cosYXsinZ = cosY * sinZ;
		float sinYXcosZ = sinY * cosZ;

		q.w = (cosYXcosZ * cosX - sinYXsinZ * sinX);
		q.x = (cosYXcosZ * sinX + sinYXsinZ * cosX);
		q.y = (sinYXcosZ * cosX + cosYXsinZ * sinX);
		q.z = (cosYXsinZ * cosX - sinYXcosZ * sinX);

		q.normalize();

		return q;
	}

	public static CSG fromPolygons(List<Polygon> polygons) {
		CSG csg = new CSG();
		csg.polygons = polygons;
		return csg;
	}

	protected CSG() {
	}

	/**
	 * Deep Copy. A new CSG object will be created. All Polys and vertices will
	 * be copies of, not references to, polys and vertices in the source CSG
	 * object.
	 **/
	public CSG(CSG other) {
		for (Polygon p : other.polygons) {
			Polygon p2 = new Polygon(p);
			this.polygons.add(p2);
		}
		this.name = other.name + "Copy";
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

	/**
	 * Shallow copy
	 */
	public CSG clone() {
		CSG csg = new CSG();
		for (Polygon p : polygons) {
			csg.polygons.add(p.clone());
		}
		return csg;
	}
}
