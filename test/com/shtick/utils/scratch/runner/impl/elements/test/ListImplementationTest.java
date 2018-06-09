/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation;
import com.shtick.utils.scratch.runner.impl.elements.ListImplementation;

/**
 * @author sean.cox
 *
 */
public class ListImplementationTest {
	/**
	 * 
	 */
	@Test
	public void testConstructor() {
		if(ScratchRuntimeImplementation.getScratchRuntime()==null) {
			ScratchRuntimeImplementation runtime = new ScratchRuntimeImplementation();
		}
		{
			Object[] listContents = new Object[] {new Object(), new Object()};
			String name = "egg";
			ListImplementation list = new ListImplementation(name, listContents, 0.0, 1.0, 100.0, 101.0, false);
			assertEquals(name, list.getListName());
			assertEquals(2, list.getItemCount());
			assertEquals(listContents[0], list.getItem(1));
			assertEquals(listContents[1], list.getItem(2));
			assertEquals(Double.valueOf(0.0), list.getX());
			assertEquals(Double.valueOf(1.0), list.getY());
			assertEquals(Double.valueOf(100.0), list.getWidth());
			assertEquals(Double.valueOf(101.0), list.getHeight());
			assertFalse(list.isVisible());
		}

		{
			Object[] listContents = new Object[] {};
			String name = "salad";
			ListImplementation list = new ListImplementation(name, listContents, 2.0, 3.0, 104.0, 105.0, true);
			assertEquals(name, list.getListName());
			assertEquals(0, list.getItemCount());
			assertEquals(Double.valueOf(2.0), list.getX());
			assertEquals(Double.valueOf(3.0), list.getY());
			assertEquals(Double.valueOf(104.0), list.getWidth());
			assertEquals(Double.valueOf(105.0), list.getHeight());
			assertTrue(list.isVisible());
		}
	}

	/**
	 * 
	 */
	@Test
	public void testGetItem() {
		{
			Object[] listContents = new Object[] {new Object(), new Object()};
			String name = "egg";
			ListImplementation list = new ListImplementation(name, listContents, 0.0, 1.0, 100.0, 101.0, false);
			assertEquals(name, list.getListName());
			assertEquals(2, list.getItemCount());
			try {
				list.getItem(0);
				fail("getItem(0) should have failed");
			}
			catch(IndexOutOfBoundsException t) {
				// Expected
			}
			assertEquals(listContents[0], list.getItem(1));
			assertEquals(listContents[1], list.getItem(2));
			try {
				list.getItem(3);
				fail("getItem(3) should have failed");
			}
			catch(IndexOutOfBoundsException t) {
				// Expected
			}
		}

	}

	/**
	 * 
	 */
	@Test
	public void testDeleteAll() {
		{
			Object[] listContents = new Object[] {new Object(), new Object()};
			String name = "egg";
			ListImplementation list = new ListImplementation(name, listContents, 0.0, 1.0, 100.0, 101.0, false);
			assertEquals(name, list.getListName());
			assertEquals(2, list.getItemCount());
			assertEquals(listContents[0], list.getItem(1));
			assertEquals(listContents[1], list.getItem(2));
			list.deleteAll();
			assertEquals(0, list.getItemCount());
			try {
				list.getItem(1);
				fail("getItem(1) should have failed");
			}
			catch(IndexOutOfBoundsException t) {
				// Expected
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void testDeleteItem() {
		{
			Object[] listContents = new Object[] {new Object(), new Object()};
			String name = "egg";
			ListImplementation list = new ListImplementation(name, listContents, 0.0, 1.0, 100.0, 101.0, false);
			assertEquals(name, list.getListName());
			assertEquals(2, list.getItemCount());
			assertEquals(listContents[0], list.getItem(1));
			assertEquals(listContents[1], list.getItem(2));
			list.deleteItem(1);
			assertEquals(1, list.getItemCount());
			assertEquals(listContents[1], list.getItem(1));
		}
	}

	/**
	 * 
	 */
	@Test
	public void testSetItem() {
		{
			Object[] listContents = new Object[] {new Object(), new Object()};
			String name = "egg";
			ListImplementation list = new ListImplementation(name, listContents, 0.0, 1.0, 100.0, 101.0, false);
			assertEquals(name, list.getListName());
			assertEquals(2, list.getItemCount());
			assertEquals(listContents[0], list.getItem(1));
			assertEquals(listContents[1], list.getItem(2));
			list.setItem(name, 1);
			assertEquals(2, list.getItemCount());
			assertEquals(name, list.getItem(1));
			assertEquals(listContents[1], list.getItem(2));
		}
	}

	/**
	 * 
	 */
	@Test
	public void testAddItem() {
		{
			Object[] listContents = new Object[] {new Object(), new Object()};
			String name = "egg";
			ListImplementation list = new ListImplementation(name, listContents, 0.0, 1.0, 100.0, 101.0, false);
			assertEquals(name, list.getListName());
			assertEquals(2, list.getItemCount());
			assertEquals(listContents[0], list.getItem(1));
			assertEquals(listContents[1], list.getItem(2));
			list.addItem(name);
			assertEquals(3, list.getItemCount());
			assertEquals(listContents[0], list.getItem(1));
			assertEquals(listContents[1], list.getItem(2));
			assertEquals(name, list.getItem(3));
		}

		{
			Object[] listContents = new Object[] {new Object(), new Object()};
			String name = "egg";
			ListImplementation list = new ListImplementation(name, listContents, 0.0, 1.0, 100.0, 101.0, false);
			assertEquals(name, list.getListName());
			assertEquals(2, list.getItemCount());
			assertEquals(listContents[0], list.getItem(1));
			assertEquals(listContents[1], list.getItem(2));
			list.addItem(name,3);
			assertEquals(3, list.getItemCount());
			assertEquals(listContents[0], list.getItem(1));
			assertEquals(listContents[1], list.getItem(2));
			assertEquals(name, list.getItem(3));
		}

		{
			Object[] listContents = new Object[] {new Object(), new Object()};
			String name = "egg";
			ListImplementation list = new ListImplementation(name, listContents, 0.0, 1.0, 100.0, 101.0, false);
			assertEquals(name, list.getListName());
			assertEquals(2, list.getItemCount());
			assertEquals(listContents[0], list.getItem(1));
			assertEquals(listContents[1], list.getItem(2));
			list.addItem(name,2);
			assertEquals(3, list.getItemCount());
			assertEquals(listContents[0], list.getItem(1));
			assertEquals(name, list.getItem(2));
			assertEquals(listContents[1], list.getItem(3));
		}

		{
			Object[] listContents = new Object[] {new Object(), new Object()};
			String name = "egg";
			ListImplementation list = new ListImplementation(name, listContents, 0.0, 1.0, 100.0, 101.0, false);
			assertEquals(name, list.getListName());
			assertEquals(2, list.getItemCount());
			assertEquals(listContents[0], list.getItem(1));
			assertEquals(listContents[1], list.getItem(2));
			list.addItem(name,1);
			assertEquals(3, list.getItemCount());
			assertEquals(name, list.getItem(1));
			assertEquals(listContents[0], list.getItem(2));
			assertEquals(listContents[1], list.getItem(3));
		}
	}

	/**
	 * 
	 */
	@Test
	public void testContains() {
		{
			Object[] listContents = new Object[] {new Object(), new Object()};
			String name = "egg";
			ListImplementation list = new ListImplementation(name, listContents, 0.0, 1.0, 100.0, 101.0, false);
			assertEquals(name, list.getListName());
			assertEquals(2, list.getItemCount());
			assertTrue(list.contains(listContents[0]));
			assertTrue(list.contains(listContents[1]));
			assertFalse(list.contains(name));
		}
	}
}
