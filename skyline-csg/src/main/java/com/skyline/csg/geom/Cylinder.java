package com.skyline.csg.geom;

import java.util.*;

import javax.vecmath.*;

import com.skyline.csg.*;

public class Cylinder extends CSG {

	protected double radius = 0.5d;
	protected double height = 1.6d; // golden ratio
	protected int slices = 16;

	// working vars
	protected Vector3d s, e, ray, axisZ, axisY, axisX;

	public Cylinder() {
		generatePolys();
	}

	/**
	 * Create a Cylinder with the given radius, height, and number of radial
	 * slices.
	 * 
	 * @param radius
	 * @param height
	 * @param slices
	 */
	public Cylinder(double radius, double height, int slices) {
		this.radius = radius;
		this.height = height;
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
		System.out.println("START: " + start.toString());
		Vertex end = new Vertex(e, axisZ, new TexCoord2f()); // Vertex for the
																// end point.
		System.out.println("END: " + end.toString());

		this.polygons = new ArrayList<Polygon>();
		for (int i = 0; i < slices; i++) {
			double t0 = i / (double) slices;
			double t1 = (i + 1) / (double) slices;
			// The "pie slice" at the top of the Cylinder.
			polygons.add(new Polygon(Arrays.asList(new Vertex[] { start, makeVertex(0, t0, -1), makeVertex(0, t1, -1) })));
			// The "rectangle" on the side of the cylinder.
			polygons.add(new Polygon(Arrays.asList(new Vertex[] { makeVertex(0, t1, 0), makeVertex(0, t0, 0), makeVertex(1, t0, 0), makeVertex(1, t1, 0) })));
			// The "pie slice" on the bottom.
			polygons.add(new Polygon(Arrays.asList(new Vertex[] { end, makeVertex(1, t1, 1), makeVertex(1, t0, 1) })));
		}

	}

	protected Vertex makeVertex(double stack, double slice, double normalBlend) {

		double angle = slice * Math.PI * 2; // what's the starting angle of the
											// slice?
											// System.out.println("slice: " +
											// slice + ", angle: " + angle);
		Vector3d out = new Vector3d(axisX); // vector pointing to the vertex (to
											// be used for the normal).
		out.scale(Math.cos(angle));
		Vector3d yTemp = new Vector3d(axisY);
		yTemp.scale(Math.sin(angle));
		out.add(yTemp);

		Vector3d pos = new Vector3d(s); // position of the vertex.

		Vector3d rayStack = new Vector3d(ray);
		rayStack.scale(stack);

		Vector3d outRadius = new Vector3d(out);
		outRadius.scale(radius);

		pos.add(rayStack); // vertical component
		pos.add(outRadius); // radial component.

		Vector3d blendZ = new Vector3d(axisZ);
		blendZ.scale(normalBlend);

		Vector3d normal = new Vector3d(out);
		normal.scale(1 - Math.abs(normalBlend));
		normal.add(blendZ);

		// System.out.println("Making vertex at position " + pos.toString());
		return new Vertex(pos, normal, new TexCoord2f()); // this will break
															// textures.
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
		generatePolys();
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
		generatePolys();
	}

	public int getSlices() {
		return slices;
	}

	public void setSlices(int slices) {
		this.slices = slices;
		generatePolys();
	}

}
