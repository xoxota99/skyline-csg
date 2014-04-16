package com.skyline.csg;

import java.util.*;

import javax.vecmath.*;

/**
 * Represents a convex polygon. The vertices used to initialize a polygon must
 * be coplanar and form a convex loop.
 * 
 * Each convex polygon has a `shared` property, which is shared between all
 * polygons that are clones of each other or were split from the same polygon.
 * This can be used to define per-polygon properties (such as surface color).
 * 
 * ported from http://evanw.github.io/csg.js/
 * 
 * @author philippd
 * @verified
 */
public class Polygon {

	Plane plane;
	List<Vertex> vertices;
	boolean shared;

	public List<Vertex> getVertices() {
		return vertices;
	}

	public Polygon(Vertex... vertices) {
		this(Arrays.asList(vertices), false);
	}

	public Polygon(List<Vertex> vertices) {
		this(vertices, false);
	}

	/**
	 * @verified
	 * @param vertices
	 * @param shared
	 */
	public Polygon(List<Vertex> vertices, boolean shared) {

		this.vertices = vertices; // TODO: Should limit to 3 vertices?
		this.shared = shared;
		// Polygon can theoretically have any number of coplanar points, but we
		// only need three to establish the plane.
		this.plane = Plane.fromPoints(vertices.get(0).pos, vertices.get(1).pos, vertices.get(2).pos);
	}

	/**
	 * Deep copy.
	 * @param other
	 */
	public Polygon(Polygon other){
		this.vertices = new ArrayList<Vertex>();
		for(Vertex v : other.vertices){
			this.vertices.add(new Vertex(new Vector3d(v.pos),new Vector3d(v.normal),new TexCoord2f(v.tex)));
		}
		this.plane = new Plane(other.plane.normal,other.plane.w);
		this.shared=other.shared;
	}
	
	/**
	 * Shallow copy
	 */
	public Polygon clone() {
		List<Vertex> vertices = new ArrayList<Vertex>();
		vertices.addAll(this.vertices);
		return new Polygon(vertices, this.shared);
	}

	public void flip() {
		Collections.reverse(this.vertices);
		for (Vertex v : this.vertices) {
			v.flip();
		}
		this.plane.flip();
	}

	public void translate(Vector3d v) {
		this.plane.translate(v);
		for (Vertex vert : this.vertices) {
			vert.translate(v);
		}
	}

	public void rotate(Quat4d rotation) {
		this.plane.rotate(rotation);
		for (Vertex vert : this.vertices) {
			vert.rotate(rotation);
		}
	}

	public void scale(Vector3d scaleFactor) {
		for (Vertex vert : this.vertices) {
			vert.scale(scaleFactor);
		}
		// Scaling normals is a pain in the ass. Just rebuild the plane.
		// Can't replace the existing reference, though, since it might be held
		// elsewhere. Instead just update it.
		Plane p = Plane.fromPoints(vertices.get(0).pos, vertices.get(1).pos, vertices.get(2).pos);
		this.plane.normal.set(p.normal.x, p.normal.y, p.normal.z);
		this.plane.w = p.w;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{\"vertices\": [\n");
		int i = 0;
		for (Vertex v : vertices) {
			i++;
			sb.append(v.toString());
			if (i < vertices.size()) {
				sb.append(", ");
			}
		}
		sb.append("]}\n");
		return sb.toString();
	}

	/**
	 * Extrude this polygon along it's normal vector by the specified distance.
	 * 
	 * @param distance
	 * @return A @{link CSG
	 */
	public CSG extrude(double distance) {
		// get the translation vector.
		Vector3d dir = new Vector3d(this.plane.normal);
		dir.scale(distance);
		// create a second poly (deep copy)
		Polygon other = new Polygon(this);
		// translate it.
		other.translate(dir);
		// flip it.
		other.flip(); // reverses the order of the vertices?

		// join the two polys.
		List<Polygon> pList = new ArrayList<Polygon>();
		pList.add(this);
		pList.add(other);
		// using the existing set of points, create additional polys that will
		// join these two. Ensure the normals are facing "outwards".
		for (int i = 0; i < vertices.size(); i++) {
			// every edge of this poly must be joined to the corresponding edge
			// in the other poly by two new "side" polys (triangles)
			int i2 = other.vertices.size() - 1 - i; // index in the flipped
													// poly.
			// assumption: Vertices are ordered in ccw order in the list.
			pList.add(new Polygon(
					this.vertices.get(i),
					other.vertices.get(i2),
					other.vertices.get((i2 + 1) % other.vertices.size())));

			pList.add(new Polygon(
					this.vertices.get(i),
					this.vertices.get((i + 1) % this.vertices.size()),
					other.vertices.get(i2)));
		}

		// TODO: If distance < 0, the normals should face "inwards". This is an
		// intrusion, instead of an extrusion.

		return CSG.fromPolygons(pList);
	}
}
