package com.skyline.csg.geom;

import java.util.*;

import javax.vecmath.*;

import com.skyline.csg.*;

public class Icosahedron extends CSG {

	protected double radius = 1d;

	public Icosahedron(double radius) {
		this.radius = radius;
		generatePolys();
	}

	public Icosahedron() {
		generatePolys();
	}

	protected void generatePolys() {
		System.out.println("Icosahedron.generatePolys");
		this.polygons = new ArrayList<Polygon>();

		double t = (1 + Math.sqrt(5)) / 2;

		double v[][] = {
				{ -1, t, 0 }, { 1, t, 0 }, { -1, -t, 0 }, { 1, -t, 0 },
				{ 0, -1, t }, { 0, 1, t }, { 0, -1, -t }, { 0, 1, -t },
				{ t, 0, -1 }, { t, 0, 1 }, { -t, 0, -1 }, { -t, 0, 1 }
		};

		int f[][] = {
				{ 0, 11, 5 }, { 0, 5, 1 }, { 0, 1, 7 }, { 0, 7, 10 }, { 0, 10, 11 },
				{ 1, 5, 9 }, { 5, 11, 4 }, { 11, 10, 2 }, { 10, 7, 6 }, { 7, 1, 8 },
				{ 3, 9, 4 }, { 3, 4, 2 }, { 3, 2, 6 }, { 3, 6, 8 }, { 3, 8, 9 },
				{ 4, 9, 5 }, { 2, 4, 11 }, { 6, 2, 10 }, { 8, 6, 7 }, { 9, 8, 1 }
		};

		for (int i = 0; i < f.length; i++) {
			// for each face
			Vector3d[] pos = new Vector3d[3];
			Vector3d[] norm = new Vector3d[3];
			for (int j = 0; j < 3; j++) {
				pos[j] = new Vector3d(
						v[f[i][j]][0],
						v[f[i][j]][1],
						v[f[i][j]][2]);
				pos[j].normalize();
				pos[j].scale(radius);

//				System.out.println("Sphere vertex at (" + pos[j].x + ", " + pos[j].y + ", " + pos[j].z + ")");

				// base normal is just the position vertex itself, normalized.
				norm[j] = new Vector3d(
						v[f[i][j]][0],
						v[f[i][j]][1],
						v[f[i][j]][2]);
				norm[j].normalize();
			}

			// With the flat surfaces of an icosahedron, all three vertices
			// defining a surface have the same normal vector, which is te
			// surface normal.

			// now get the surface normal for each face, and add it to the
			// vertex normal.
			Vector3d surfaceNormal = new Vector3d();
			Vector3d v1 = new Vector3d(pos[1]);
			Vector3d v2 = new Vector3d(pos[2]);

			v2.sub(pos[0]);
			v1.sub(pos[0]);
			surfaceNormal.cross(v2, v1);
			surfaceNormal.negate();
			surfaceNormal.normalize();

			norm[0] = surfaceNormal;
			norm[1] = surfaceNormal;
			norm[2] = surfaceNormal;

			Polygon p = new Polygon(
					new Vertex(pos[0],
							norm[0],
							new TexCoord2f()
					),
					new Vertex(pos[1],
							norm[1],
							new TexCoord2f()
					),
					new Vertex(pos[2],
							norm[1],
							new TexCoord2f()
					)
					);
			polygons.add(p);
		}

	}
}
