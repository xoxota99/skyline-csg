package com.skyline.csg.geom;

import javax.vecmath.*;

import junit.framework.Assert;

import org.junit.Test;

public class UtilTest {

	/**
	 * Test rotation of a point around the origin (0,0,0)
	 */
	@Test
	public void testRotation() {
		// w is cos(theta / 2) and (x, y, z) is the axis times sin(theta / 2)
		double deg = 180;	//180 degrees.
		double theta = Math.PI*2 / 360 * deg;
		Point3d point = new Point3d(0, 1, 1);
		Quat4d rot = new Quat4d(0, 0, 10 * Math.sin(theta / 2), Math.cos(theta / 2)); // 180
																							// degrees
																							// around
																							// Z
																							// axis
		Util.rotate(point, rot);
		System.out.printf("Rotated: {%f, %f, %f}\n",point.x, point.y, point.z);
		Assert.assertEquals((int) point.x, 0);
		Assert.assertEquals((int) point.y, -1);
		Assert.assertEquals((int) point.z, 1);
	}
	
	@Test
	/**
	 * Test Anisotropic scaling of a normal vector.
	 */
	public void testNormalScaling() {
		Vector3d v = new Vector3d(1,1,1);
		v.normalize();
		Vector3d s = new Vector3d(3,20,1);
		System.out.printf("Unscaled: {%f, %f, %f}\n",v.x,v.y,v.z);
		v=Util.scaleNormal(v, s);
		System.out.printf("Scaled: {%f, %f, %f}\n",v.x,v.y,v.z);
	}
}
