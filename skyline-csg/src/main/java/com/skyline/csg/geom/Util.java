package com.skyline.csg.geom;

import javax.vecmath.*;

public class Util {

	/**
	 * rotate t around an axis described in a rotation Quaternion.
	 * 
	 * @param t
	 *            the point to rotate, in 3-space.
	 * @param rotation
	 *            A rotation quaternion. a Rotation quat has 4 dimensions that
	 *            describe the axis of rotation (in x, y, and z coordinates of a
	 *            point that the axis passes through), and a rotation, in
	 *            radians:
	 *            <ul>
	 *            <li>x - xRotation * sin(theta/2)</li>
	 *            <li>y - yRotation * sin(theta/2)</li>
	 *            <li>z - zRotation * sin(theta/2)</li>
	 *            <li>w - cos(theta/2)</li>
	 *            </ul>
	 */
	public static void rotate(Tuple3d point, Quat4d rotation) {

		rotation.normalize();

		// R*V
		final double RVx = rotation.w * point.x + rotation.y * point.z - rotation.z * point.y;
		final double RVy = rotation.w * point.y - rotation.x * point.z + rotation.z * point.x;
		final double RVz = rotation.w * point.z + rotation.x * point.y - rotation.y * point.x;
		final double RVw = rotation.x * point.x + rotation.y * point.y + rotation.z * point.z;

		// R*V*R^-1
		final double RVCx = RVx * rotation.w - RVy * rotation.z + RVz * rotation.y + RVw * rotation.x;
		final double RVCy = RVx * rotation.z + RVy * rotation.w - RVz * rotation.x + RVw * rotation.y;
		final double RVCz = RVy * rotation.x - RVx * rotation.y + RVz * rotation.w + RVw * rotation.z;

		point.set(RVCx, RVCy, RVCz);
	}

	/**
	 * Anisotropic scaling of a normal vector. For normals, we need to multiply
	 * by the transpose of the inverse of the transformation matrix instead of
	 * just scaling. (ie: divide instead of multiply). For scaling, the
	 * transpose is a no-op, so we can simplify this to a simple inversion.
	 * 
	 * @param normal
	 * @param scale
	 * @return the scaled normal vector, before normalization. That is, the
	 *         returned vector will have the correct "direction", but will not
	 *         be of unit length.
	 */
	public static Vector3d scaleNormal(Vector3d normal, Vector3d scale) {
		assert (scale.x != 0 && scale.y != 0 && scale.z != 0) : "Can't scale by zero value.";
		return new Vector3d(normal.x / scale.x, normal.y / scale.y, normal.z / scale.z);
	}
}
