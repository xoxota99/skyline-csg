package com.skyline.csg.geom;

import java.util.*;

import javax.vecmath.*;

import com.skyline.csg.*;

/**
 * Construct a solid sphere. parameters are `radius`, `slices`, and `stacks`.
 * The `slices` and `stacks` parameters control the tessellation along the //
 * longitude and latitude directions.
 * 
 * @author philippd
 * 
 */
public class Sphere extends CSG {
	private double radius, slices, stacks;
	Vector3d center = new Vector3d(0, 0, 0);

	public Sphere(){
		this(1d,20,20);
	}
	public Sphere(double radius, double slices, double stacks) {
		this.radius = radius;
		this.slices = slices;
		this.stacks = stacks;
		generatePolys();
	}

	private void generatePolys() {
		this.polygons = new ArrayList<Polygon>();
		List<Vertex> vertices = new ArrayList<Vertex>();
		for (int i = 0; i < slices; i++) {
			for (int j = 0; j < stacks; j++) {
				vertices = new ArrayList<Vertex>();
				vertices.add(makeVertex(i / slices, j / stacks));
				if (j > 0)
					vertices.add(makeVertex((i + 1) / slices, j / stacks));
				if (j < stacks - 1)
					vertices.add(makeVertex((i + 1) / slices, (j + 1) / stacks));
				vertices.add(makeVertex(i / slices, (j + 1) / stacks));
				this.polygons.add(new Polygon(vertices));
			}
		}
	}

	private Vertex makeVertex(double theta, double phi) {
		theta *= Math.PI * 2;
		phi *= Math.PI;
		Vector3d dir = new Vector3d(
				Math.cos(theta) * Math.sin(phi),
				Math.cos(phi),
				Math.sin(theta) * Math.sin(phi)
				);
		dir.scale(radius);
		Vector3d ret = new Vector3d(center);
		ret.add(dir);
		return new Vertex(ret, dir, new TexCoord2f()); // this will break
														// textures.
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
		generatePolys();
	}

	public double getSlices() {
		return slices;
	}

	public void setSlices(double slices) {
		this.slices = slices;
		generatePolys();
	}

	public double getStacks() {
		return stacks;
	}

	public void setStacks(double stacks) {
		this.stacks = stacks;
		generatePolys();
	}

	public Vector3d getCenter() {
		return center;
	}

	public void setCenter(Vector3d center) {
		this.center = center;
		generatePolys();
	}

}
