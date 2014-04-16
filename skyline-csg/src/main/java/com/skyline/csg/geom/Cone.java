package com.skyline.csg.geom;

import java.util.*;

import javax.vecmath.*;

import com.skyline.csg.*;

public class Cone extends Cylinder {

	/**
	 * Create a Cone with the given radius at the base, height, and number of radial
	 * slices.
	 * 
	 * @param radius radius of the cone at it's base
	 * @param height
	 * @param slices
	 */
	public Cone(double radius, double height, int slices) {
		this.radius=radius;
		this.height=height;
		this.slices = slices;
		generatePolys();
	}

	private void generatePolys() {
		// 1, 1, 16

		s = new Vector3d(0, -height / 2, 0); // "start" point. Center
												// of the "top" of the
												// cylinder
		e = new Vector3d(0, height / 2, 0); // "end" point. Center of
											// the "bottom" of the
											// cylinder
		ray = new Vector3d(e); // vector from the start point to the
								// end point.
		ray.sub(s);

		axisZ = new Vector3d(ray);
		axisZ.normalize();
		boolean isY = (Math.abs(axisZ.y) > 0.5);

		axisX = new Vector3d(isY ? 1 : 0, isY ? 0 : 1, 0);
		axisX.cross(axisX, axisZ);
		axisX.normalize();

		axisY = new Vector3d(axisX);
		axisY.cross(axisY, axisZ);
		axisY.normalize();

		Vector3d negZ = new Vector3d(axisZ); // negated Z Axis, to provide
												// normal
		// vector for "start" point.
		negZ.negate();
		// TODO: This will break textures.
		Vertex start = new Vertex(s, negZ, new TexCoord2f()); // Vertex for the
																// start point.
		Vertex end = new Vertex(e, axisZ, new TexCoord2f()); // Vertex for the
																// end point.
		System.out.println("START: " + start.toString());
		System.out.println("END: " + end.toString());

		this.polygons = new ArrayList<Polygon>();
		for (int i = 0; i < slices; i++) {
			double t0 = i / (double) slices;
			double t1 = (i + 1) / (double) slices;
			polygons.add(new Polygon(Arrays.asList(new Vertex[] { start, makeVertex(0, t0, -1), makeVertex(0, t1, -1) })));

			polygons.add(new Polygon(Arrays.asList(new Vertex[] { end, makeVertex(0, t1, 0), makeVertex(0, t0, 0) })));
		}

	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public int getSlices() {
		return slices;
	}

	public void setSlices(int slices) {
		this.slices = slices;
	}

}
