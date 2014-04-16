package com.skyline.csg.geom;

import java.util.*;

import javax.vecmath.*;

import com.skyline.csg.*;

/**
 * An ordinary (single holed) torus.
 * <p>
 * The center is by default the origin.
 * 
 * @author phil
 * 
 */
public class Torus extends CSG {
	private int circleSamples;

	private int radialSamples;

	private double innerRadius;

	private double outerRadius;

	public Torus() {
		this(50, 50, .5, 1);
	}

	/**
	 * Constructs a new Torus. Center is the origin, but the Torus may be
	 * transformed.
	 * 
	 * @param circleSamples
	 *            The number of samples along the circles.
	 * @param radialSamples
	 *            The number of samples along the radial.
	 * @param innerRadius
	 *            The radius of the inner beginning of the Torus.
	 * @param outerRadius
	 *            The radius of the outer end of the Torus.
	 */
	public Torus(int circleSamples, int radialSamples, double innerRadius, double outerRadius) {
		this.circleSamples = circleSamples;
		this.radialSamples = radialSamples;
		this.innerRadius = innerRadius;
		this.outerRadius = outerRadius;
		generatePolys();
	}

	private void generatePolys() {
		List<Vector3d> pos = new ArrayList<Vector3d>();
		List<Vector3d> normals = new ArrayList<Vector3d>();
		List<TexCoord2f> texs = new ArrayList<TexCoord2f>();

		float inverseCircleSamples = 1.0f / circleSamples;
		float inverseRadialSamples = 1.0f / radialSamples;
		int i = 0;
		// generate the cylinder itself
		Vector3d radialAxis = new Vector3d(), torusMiddle, tempPos, tempNormal = new Vector3d();
		for (int circleCount = 0; circleCount < circleSamples; circleCount++) {
			// compute center point on torus circle at specified angle
			float circleFraction = circleCount * inverseCircleSamples;
			double theta = Math.PI * 2 * circleFraction;
			double cosTheta = Math.cos(theta);
			double sinTheta = Math.sin(theta);
			radialAxis.set(cosTheta, sinTheta, 0);
			torusMiddle = new Vector3d(radialAxis);
			torusMiddle.scale(outerRadius);

			// compute slice vertices with duplication at end point
			int iSave = i;
			for (int radialCount = 0; radialCount < radialSamples; radialCount++) {
				float radialFraction = radialCount * inverseRadialSamples;
				// in [0,1)
				double phi = Math.PI * 2 * radialFraction;
				double cosPhi = Math.cos(phi);
				double sinPhi = Math.sin(phi);
				tempNormal.set(radialAxis);
				tempNormal.scale(cosPhi);
				tempNormal.z += sinPhi;
				normals.add(new Vector3d(tempNormal));

				tempPos = new Vector3d(tempNormal);
				tempPos.scale(innerRadius);
				tempPos.add(torusMiddle);
				pos.add(new Vector3d(tempPos));

				texs.add(new TexCoord2f(radialFraction, circleFraction));
				i++;
			}

			pos.add(pos.get(iSave));
			normals.add(normals.get(iSave));
			texs.add(new TexCoord2f(1.0f, circleFraction));

			i++;
		}

		// // duplicate the cylinder ends to form a torus
		 for (int iR = 0; iR <= circleSamples; iR++, i++) {
		 pos.add(pos.get(iR));
		 normals.add(normals.get(iR));
		 texs.add(texs.get(iR));
		 // texs.add(new TexCoord2f(i * 2 + 1, 1.0f));
		 }

		System.out.println("vertCount: " + (circleSamples * radialSamples) + ", pos.size: " + pos.size() + ", normals: " + normals.size() + ", texs: " + texs.size());

		for (int iV = circleSamples; iV < pos.size() - 1; iV++) {
			Vertex v1, v2, v3;
			v1 = new Vertex(pos.get(iV), normals.get(iV), texs.get(iV));
			v2 = new Vertex(pos.get(iV + 1), normals.get(iV + 1), texs.get(iV + 1));
			v3 = new Vertex(pos.get(iV - circleSamples), normals.get(iV - circleSamples), texs.get(iV - circleSamples));
			this.polygons.add(new Polygon(v1, v2, v3));

			v1 = new Vertex(pos.get(iV - circleSamples + 1), normals.get(iV - circleSamples + 1), texs.get(iV - circleSamples + 1));

			this.polygons.add(new Polygon(v3, v2, v1));
		}

	}

}
