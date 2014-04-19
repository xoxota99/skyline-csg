package com.skyline.csg;

import java.util.logging.*;

import javax.vecmath.*;

import com.skyline.csg.geom.*;

/**
 * Represents a vertex of a polygon. Use your own vertex class instead of this
 * one to provide additional features like texture coordinates and vertex
 * colors. Custom vertex classes need to provide a `pos` property and `clone()`,
 * `flip()`, and `interpolate()` methods that behave analogous to the ones
 * defined by `CSG.Vertex`. This class provides `normal` so convenience
 * functions like `CSG.sphere()` can return a smooth vertex normal, but `normal`
 * is not used anywhere else.
 * 
 * ported from http://evanw.github.io/csg.js/
 * 
 * @author philippd
 * @verified
 */
public class Vertex {

	public Vector3d pos, normal;
	public TexCoord2f tex;

	/**
	 * @param pos
	 *            position of the Vertex.
	 * @param normal
	 * @verified
	 */
	public Vertex(Vector3d pos, Vector3d normal, TexCoord2f tex) {
		this.pos = new Vector3d(pos);
		this.normal = new Vector3d(normal);
		this.tex = new TexCoord2f(tex);
	}

	/**
	 * Invert all orientation-specific data (e.g. vertex normal). Called when
	 * the orientation of a polygon is flipped.
	 * 
	 * @verified
	 */
	public void flip() {
		this.normal.negate();
	}

	/**
	 * Create a new vertex between this vertex and `other` by linearly
	 * interpolating all properties using a parameter of `t`. Subclasses should
	 * override this to interpolate additional properties (such as TextCoords)
	 * 
	 * @param other
	 * @param t
	 * @return
	 * @verified
	 */
	public Vertex interpolate(Vertex other, double t) {
		Vector3d newPos = new Vector3d(this.pos);
		newPos.interpolate(other.pos, t);

		Vector3d newNormal = new Vector3d(this.normal);
		newNormal.interpolate(other.normal, t);

		TexCoord2f newTex = new TexCoord2f(this.tex);
		newTex.interpolate(other.tex, (float) t);

		return new Vertex(newPos, newNormal, newTex);
	}

	/**
	 * @verified
	 */
	public Vertex clone() {
		// constructor takes care of cloning these params.
		return new Vertex(this.pos, this.normal, this.tex);
	}

	public void translate(double x, double y, double z) {
		pos.x += x;
		pos.y += y;
		pos.z += z;
	}

	public void translate(Vector3d v) {
		// no change to normal or texCoord.
		pos.add(v);
	}

	/**
	 * Rotate around the world origin (0,0,0) by the provided theta values.
	 * First Z, then Y, then X.
	 * 
	 * @param rotation
	 *            A Rotation Quaternion containing the angles, in radians, to
	 *            rotate around the x, y, and z axes.
	 */
	public void rotate(Quat4d rotation) {
		Util.rotate(pos, rotation);
		Util.rotate(normal, rotation);
	}

	public Vector3d getPos() {
		return pos;
	}

	public void setPos(Vector3d pos) {
		this.pos = pos;
	}

	public Vector3d getNormal() {
		return normal;
	}

	public void setNormal(Vector3d normal) {
		this.normal = normal;
	}

	public boolean equals(Object o) {
		if (o instanceof Vertex) {
			return equals((Vertex) o);
		}
		return false;
	}

	public boolean equals(Vertex v) {
		// TODO: Is it wrong to use the same epsilon value for position AND
		// normal? Normal is normalized, and less likely to exceed epsilon.
		return this.pos.epsilonEquals(v.pos, CSG.EPSILON)
				&& this.normal.epsilonEquals(v.normal, CSG.EPSILON)
				&& this.tex.epsilonEquals(v.tex, (float) CSG.EPSILON);
	}

	public String toString() {
		return String.format("{\"position\": {\"x\": %.6f,\"y\": %.6f,\"z\": %.6f}, \"normal\": {\"x\": %.6f,\"y\": %.6f,\"z\": %.6f}, \"texture\": {\"x\": %.6f,\"y\": %.6f}}", pos.x, pos.y, pos.z, normal.x, normal.y, normal.z, tex.x, tex.y);
	}

	public void scale(Vector3d scaleFactor) {
		this.pos.set(this.pos.x * scaleFactor.x, this.pos.y * scaleFactor.y, this.pos.z * scaleFactor.z);

		if (scaleFactor.x != scaleFactor.y || scaleFactor.x != scaleFactor.z) {
			// anisotropic, so we need to scale the normal as well.
			this.normal = Util.scaleNormal(normal, scaleFactor);
		}

		// TODO: texCoord scaling.
	}

	public TexCoord2f getTex() {
		return tex;
	}

	public void setTex(TexCoord2f tex) {
		this.tex = tex;
	}
}
