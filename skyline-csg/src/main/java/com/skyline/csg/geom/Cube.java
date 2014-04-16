package com.skyline.csg.geom;

import java.util.*;

import javax.vecmath.*;

import com.skyline.csg.*;

public class Cube extends CSG {
	private double radius;
	private Vector3d center;

	public Cube() {
		this(1f);
	}

	public Cube(double radius) {
		this(radius, new Vector3d(0, 0, 0));
	}

	/**
	 * Construct an axis-aligned solid cube with "radius".
	 * 
	 * @param radius
	 * @return
	 */
	public Cube(double radius, Vector3d center) {
		this.radius = radius;
		this.center = center;
		generatePolys();
	}

	private void generatePolys() {
		int[][][] v = {
				{ { 0, 4, 6, 2 }, { -1, 0, 0 }, { 2, 0, 1, 3 } },
				{ { 1, 3, 7, 5 }, { +1, 0, 0 }, { 2, 0, 1, 3 } },
				{ { 0, 1, 5, 4 }, { 0, -1, 0 }, { 2, 0, 1, 3 } },
				{ { 2, 6, 7, 3 }, { 0, +1, 0 }, { 2, 0, 1, 3 } },
				{ { 0, 2, 3, 1 }, { 0, 0, -1 }, { 2, 0, 1, 3 } },
				{ { 4, 5, 7, 6 }, { 0, 0, +1 }, { 2, 0, 1, 3 } }
		};
		Vector3d c = (center == null ? new Vector3d(0, 0, 0) : center);
		this.polygons = new ArrayList<Polygon>();
		for (int[][] v2 : v) {
			List<Vertex> vertices = new ArrayList<Vertex>();
			for (int i : v2[0]) {
				/*
				 * v2[0] encodes 4 vertices. Each entry is a 3-bit integer that
				 * encodes the unit extent for an axis. i & 1 > x extent. i & 2
				 * > y i & 4 > z. Extent is either -1 or 1, then we multiply by
				 * the radius and add the center.
				 */
				Vector3d pos = new Vector3d(
						c.x + radius * (2 * ((i & 1) != 0 ? 1 : 0) - 1),
						c.y + radius * (2 * ((i & 2) != 0 ? 1 : 0) - 1),
						c.z + radius * (2 * ((i & 4) != 0 ? 1 : 0) - 1)
						);

				Vector3d norm = new Vector3d(v2[1][0], v2[1][1], v2[1][2]);

				TexCoord2f tex = new TexCoord2f(
						(v2[2][0] & 2) >> 1,
						v2[2][0] & 1
						);

				Vertex vertex = new Vertex(pos, norm, tex);
				vertices.add(vertex);
			}
			this.polygons.add(new Polygon(vertices));
		}
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
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
