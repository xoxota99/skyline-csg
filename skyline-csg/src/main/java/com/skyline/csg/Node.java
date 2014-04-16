package com.skyline.csg;

import java.util.*;

import javax.vecmath.*;

/**
 * Holds a node in a BSP tree. A BSP tree is built from a collection of polygons
 * by picking a polygon to split along. That polygon (and all other coplanar
 * polygons) are added directly to that node and the other polygons are added to
 * the front and/or back subtrees. This is not a leafy BSP tree since there is
 * no distinction between internal and leaf nodes.
 * 
 * ported from http://evanw.github.io/csg.js/
 * 
 * @author philippd
 * @verified
 */
public class Node {

	private List<Polygon> polygons = new ArrayList<Polygon>();
	private Plane plane;
	private Node front, back;

	public Node(List<Polygon> polygons){
		this.plane=null;
		this.front=null;
		this.back=null;
		this.polygons = new ArrayList<Polygon>();
		if(polygons!=null && polygons.size()>0){
			this.build(polygons);
		}
	}
	
	public Node() {
		this(null);
	}

	/**
	 * @verified
	 */
	public Node clone() {
		Node node = new Node();
		if (this.plane != null) {
			node.plane = this.plane.clone();
		}
		if (this.front != null) {
			node.front = this.front.clone();
		}
		if (this.back != null) {
			node.back = this.back.clone();
		}
		if (this.polygons != null && this.polygons.size() > 0) {
			node.polygons = new ArrayList<Polygon>();
			for (Polygon p : this.polygons) {
				node.polygons.add(p.clone());
			}
		}
		return node;
	}

	/**
	 * Invert this node in place, converting solid space to empty space, and
	 * empty space to solid space.
	 * @verified
	 */
	public void invert() {

		for (Polygon p : this.polygons) {
			p.flip();
		}
		this.plane.flip();
		if (this.front != null) {
			this.front.invert();
		}
		if (this.back != null) {
			this.back.invert();
		}
		Node temp = this.front;
		this.front = this.back;
		this.back = temp;
	}

	/**
	 * Recursively remove all polygons in `polygons` that are inside this BSP
	 * tree.
	 * 
	 * @param polygons
	 * @return
	 * @verified
	 */
	public List<Polygon> clipPolygons(List<Polygon> polygons){
		if(this.plane==null) return new ArrayList<Polygon>(polygons);
		List<Polygon> front = new ArrayList<Polygon>();
		List<Polygon> back = new ArrayList<Polygon>();
		for (Polygon p : polygons) {
			this.plane.splitPolygon(p, front, back, front, back);
		}
		if (this.front != null) 
			front = this.front.clipPolygons(front);
		
		if (this.back != null)
			back = this.back.clipPolygons(back);
		else
			back = new ArrayList<Polygon>();
		
		front.addAll(back);
		return front;
	}

	/**
	 * Remove all polygons in this BSP tree that are inside the other BSP tree
	 * `bsp`.
	 * 
	 * @param bsp
	 *            the Node to clip to.
	 * @verified
	 */
	public void clipTo(Node bsp) {
		this.polygons = bsp.clipPolygons(this.polygons);
		if (this.front != null)
			this.front.clipTo(bsp);
		if (this.back != null)
			this.back.clipTo(bsp);
	}

	/**
	 * 
	 * @return a list of all polygons in this BSP tree. Including this node, and
	 *         all child nodes (front and back).
	 * @verified
	 */
	public List<Polygon> allPolygons() {
		List<Polygon> retval = new ArrayList<Polygon>(this.polygons);
		if (this.front != null)
			retval.addAll(this.front.allPolygons());
		if (this.back != null)
			retval.addAll(this.back.allPolygons());
		return retval;
	}

	/**
	 * Build a BSP tree out of `polygons`. When called on an existing tree, the
	 * new polygons are filtered down to the bottom of the tree and become new
	 * nodes there. Each set of polygons is partitioned using the first polygon
	 * in the set. (no heuristic is used to pick a good split).
	 * 
	 * @param polygons
	 * @verified
	 */
	public void build(List<Polygon> polygons) {
		if (polygons == null || polygons.size() == 0)
			return;

		if (this.plane == null)
			this.plane = polygons.get(0).plane.clone();

		if (this.polygons == null)
			this.polygons = new ArrayList<Polygon>();

		List<Polygon> front = new ArrayList<Polygon>();
		List<Polygon> back = new ArrayList<Polygon>();
		for (Polygon p : polygons) {
			this.plane.splitPolygon(p, this.polygons, this.polygons, front, back);
		}
		if (front.size() > 0) {
			if (this.front == null)
				this.front = new Node();
			this.front.build(front);
		}
		if (back.size() > 0) {
			if (this.back == null)
				this.back = new Node();
			this.back.build(back);
		}

	}

	/**
	 * Moves this Node, in place.
	 * 
	 * @param v
	 */
	public void translate(Vector3d v) {
		if (this.plane != null)
			this.plane.translate(v);
		if (this.front != null)
			this.front.translate(v);
		if (this.back != null)
			this.back.translate(v);
		for (Polygon p : this.polygons) {
			p.translate(v);
		}
	}

	/**
	 * Rotate around the world origin (0,0,0)
	 * 
	 * @param rotation
	 */
	public void rotate(Quat4d rotation) {
		if (this.plane != null)
			this.plane.rotate(rotation);
		if (this.front != null)
			this.front.rotate(rotation);
		if (this.back != null)
			this.back.rotate(rotation);
		for (Polygon p : this.polygons) {
			p.rotate(rotation);
		}
	}

	public void scale(Vector3d v) {
		if (this.plane != null)
			this.plane.scale(v);
		if (this.front != null)
			this.front.scale(v);
		if (this.back != null)
			this.back.scale(v);
		for (Polygon p : this.polygons) {
			p.scale(v);
		}
	}
}
