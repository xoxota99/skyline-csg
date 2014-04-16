package com.skyline.csg.geom;

import java.util.*;

import javax.vecmath.*;

import com.skyline.csg.*;

public class Cube extends CSG {
	private double radius = 1;
	private Vector3d center = new Vector3d(0, 0, 0);

	public Cube() {
		generatePolys();
	}

	public Cube(double radius) {
		this.radius = radius;
		generatePolys();
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

		this.polygons = new ArrayList<Polygon>();
		// for each side / poly
		for (int[][] v2 : v) {
			List<Vertex> vertices = new ArrayList<Vertex>();
			// for each corner on a side.
			for (int i : v2[0]) {
				/*
				 * v2[0] encodes 4 vertices. Each entry is a 3-bit integer that
				 * encodes the unit extent for an axis. i & 1 > x extent. i & 2
				 * > y i & 4 > z. Extent is either -1 or 1, then we multiply by
				 * the radius and add the center to get xyz coordinates. Neat
				 * hack to encode xyz as a single integer. So if Radius is .5,
				 * the cube will be 1x1x1.
				 */
				int xVal = (2 * ((i & 1) != 0 ? 1 : 0) - 1);
				int yVal = (2 * ((i & 2) != 0 ? 1 : 0) - 1);
				int zVal = (2 * ((i & 4) != 0 ? 1 : 0) - 1);
				System.out.println("Cube vertex at (" + (center.x + this.radius * xVal)+", "+(center.x + this.radius * yVal)+", "+(center.x + this.radius * zVal)+")");
				Vector3d pos = new Vector3d(
						center.x + this.radius * xVal,
						center.y + this.radius * yVal,
						center.z + this.radius * zVal
						);

				// Normal for this vertex, which is the same as the normal for
				// the side. All four vertices of a side share the same normal.
				Vector3d norm = new Vector3d(v2[1][0], v2[1][1], v2[1][2]);

				TexCoord2f tex = new TexCoord2f(
						(v2[2][0] & 2) >> 1,
						v2[2][0] & 1
						);
				// Not sure what v2[1..3] are used for...

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
