package com.skyline.csg.geom;

import java.util.*;

import javax.vecmath.*;

import com.skyline.csg.*;

public class Sphere extends Icosahedron {
	// number of times we will subdivide from the originating icosahedron to
	// round it out. Each additional LOD will double the number of vertices.
	private int lod = 3;	//keep at zero, since generatePolys is called twice...

	public Sphere(double radius, int lod) {	//BUG: superClass consructor, and generatePolys, is called before radius and lod are set. Wasted effort.
		this.radius = radius;
		this.lod = lod;
		subdivide(lod);
	}

	public Sphere(double radius){
		this.radius = radius;
		subdivide(lod);
	}
	public Sphere() {
		subdivide(lod);
	}
	
	private void subdivide(int depth) {
		System.out.println("subdivide (depth=" + depth + ")");
		if (depth > 0) {
			List<Polygon> pList = new ArrayList<Polygon>();
			for (Polygon p : polygons) {
				// subdivide this poly into four.
				Vector3d pos1 = p.getVertices().get(0).pos;
				Vector3d pos2 = p.getVertices().get(1).pos;
				Vector3d pos3 = p.getVertices().get(2).pos;
				// Vector3d n1 = p.getVertices().get(0).normal;
				// Vector3d n2 = p.getVertices().get(1).normal;
				// Vector3d n3 = p.getVertices().get(2).normal;
				TexCoord2f t1 = p.getVertices().get(0).tex;
				TexCoord2f t2 = p.getVertices().get(1).tex;
				TexCoord2f t3 = p.getVertices().get(2).tex;

				Vector3d posA = new Vector3d((pos1.x + pos2.x) / 2, (pos1.y + pos2.y) / 2, (pos1.z + pos2.z) / 2);
				Vector3d posB = new Vector3d((pos2.x + pos3.x) / 2, (pos2.y + pos3.y) / 2, (pos2.z + pos3.z) / 2);
				Vector3d posC = new Vector3d((pos3.x + pos1.x) / 2, (pos3.y + pos1.y) / 2, (pos3.z + pos1.z) / 2);

				Vector3d nA = new Vector3d(posA);
				Vector3d nB = new Vector3d(posB);
				Vector3d nC = new Vector3d(posC);
				nA.normalize();
				nB.normalize();
				nC.normalize();

				Vector3d n1 = new Vector3d(pos1);
				Vector3d n2 = new Vector3d(pos2);
				Vector3d n3 = new Vector3d(pos3);
				n1.normalize();
				n2.normalize();
				n3.normalize();

				// normalize. Assumes the center is at 0,0,0
				posA.scale(pos1.length() / posA.length());
				posB.scale(pos2.length() / posB.length());
				posC.scale(pos3.length() / posC.length());

				pList.add(new Polygon(new Vertex(pos1, n1, t1),
						new Vertex(posA, nA, t1),
						new Vertex(posC, nC, t1))); // texcoords and normals are
													// effed up.

				pList.add(new Polygon(new Vertex(posA, nA, t1),
						new Vertex(pos2, n2, t2),
						new Vertex(posB, nB, t1))); // texcoords and normals are
													// effed up.

				pList.add(new Polygon(new Vertex(posA, nA, t1),
						new Vertex(posB, nB, t1),
						new Vertex(posC, nC, t1))); // texcoords and normals are
													// effed up.

				pList.add(new Polygon(new Vertex(posC, nC, t1),
						new Vertex(posB, nB, t1),
						new Vertex(pos3, n3, t3))); // texcoords and normals are
													// effed up.
			}
			polygons = pList;
			
			subdivide(depth-1);
		}
	}

}
