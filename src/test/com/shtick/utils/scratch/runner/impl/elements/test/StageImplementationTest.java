/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;

import com.shtick.utils.scratch.runner.core.elements.RenderableChild;
import com.shtick.utils.scratch.runner.core.elements.Sprite;
import com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation;
import com.shtick.utils.scratch.runner.impl.elements.CostumeImplementation;
import com.shtick.utils.scratch.runner.impl.elements.ListImplementation;
import com.shtick.utils.scratch.runner.impl.elements.SoundImplementation;
import com.shtick.utils.scratch.runner.impl.elements.SpriteImplementation;
import com.shtick.utils.scratch.runner.impl.elements.StageImplementation;
import com.shtick.utils.scratch.runner.impl.elements.VariableImplementation;
import com.shtick.utils.scratch.runner.standard.blocks.util.AllBadSprite;
import com.shtick.utils.scratch.runner.standard.blocks.util.DummyScratchRuntimeImplementation;

/**
 * @author sean.cox
 *
 */
public class StageImplementationTest {
	private static ScratchRuntimeImplementation runtime;
	static {
		try {
			runtime = new DummyScratchRuntimeImplementation();
		}
		catch(IOException t) {
			// Should be impossible.
		}
	}

	/**
	 * 
	 */
	@Test
	public void testConstructor() {
		{
			String name = "egg";
			StageImplementation stage = new StageImplementation(
					name,
					new VariableImplementation[0],
					new HashMap<String, ListImplementation>(),
					new SoundImplementation[0],
					new CostumeImplementation[] {
							new FakeCostume("coz", 0)
					},
					0, 1, "2", 3, 1.5, new HashMap<String,Object>(), runtime);
			assertEquals(name, stage.getObjName());
			assertNull(stage.getContextVariableValueByName("testVar"));
			assertNull(stage.getContextListByName("testList"));
			// TODO It would be better to directly access details about the sound data.
			assertNull(stage.playSoundByName("testSound"));
			assertEquals(1,stage.getCostumeCount());
			assertEquals(0,stage.getCurrentCostumeIndex());
			assertEquals(1,stage.getPenLayerID());
			assertEquals("2",stage.getPenLayerMD5());
			assertEquals(3,stage.getTempoBPM());
		}

		{
			String name = "salad";
			HashMap<String, ListImplementation> listMap = new HashMap<String, ListImplementation>();
			listMap.put("testList", new ListImplementation("testList", new Object[0], 0.0, 0.0, 0.0, 0.0, false, runtime));
			HashMap<String, Object> spriteInfo = new HashMap<String,Object>();
			spriteInfo.put("key", "value");
			StageImplementation stage = new StageImplementation(
					name,
					new VariableImplementation[] {
							new VariableImplementation("testVar", 2)
					},
					listMap,
					new SoundImplementation[] {
							new SoundImplementation("testSound", 0,""+"testSound".hashCode(), 1, 1, "WAV", new byte[0])
					},
					new CostumeImplementation[] {
							new FakeCostume("coz", 0),
							new FakeCostume("play", 1)
					},
					1, 5, "6", 7, 0.5, spriteInfo, runtime);
			assertEquals(name, stage.getObjName());
			assertEquals(2,stage.getContextVariableValueByName("testVar"));
			assertNotNull(stage.getContextListByName("testList"));
			// TODO It would be better to directly access details about the sound data.
			try {
				stage.playSoundByName("testSound");
				fail("Exception should be thrown as the sprite tries to play a non-existent sound.");
			}
			catch(Throwable t) {}
			assertEquals(2,stage.getCostumeCount());
			assertEquals(1,stage.getCurrentCostumeIndex());
			assertEquals(5,stage.getPenLayerID());
			assertEquals("6",stage.getPenLayerMD5());
			assertEquals(7,stage.getTempoBPM());
		}
	}

	/**
	 * 
	 */
	@Test
	public void testAddRemoveRenderableChildSimpleCase() {
		String name = "egg";
		StageImplementation stage = new StageImplementation(
				name,
				new VariableImplementation[0],
				new HashMap<String, ListImplementation>(),
				new SoundImplementation[0],
				new CostumeImplementation[] {
						new FakeCostume("coz", 0)
				},
				0, 1, "2", 3, 1.5, new HashMap<String,Object>(), runtime);

		Sprite sprite1 = new FakeSprite("Sprite 1");
		Sprite sprite2 = new FakeSprite("Sprite 2");
		Sprite sprite3 = new FakeSprite("Sprite 3");
		Sprite otherSprite = new FakeSprite("Other Sprite");
		RenderableChild[] renderableChildren;

		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(0,renderableChildren.length);
		
		stage.addChild(sprite1);
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(1,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);

		stage.addChild(sprite2);
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(2,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);

		stage.addChild(sprite3);
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
		
		assertFalse(stage.removeChild(otherSprite));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
		
		assertTrue(stage.removeChild(sprite2));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(2,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite3,renderableChildren[1]);
		
		assertTrue(stage.removeChild(sprite3));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(1,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		
		assertTrue(stage.removeChild(sprite1));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(0,renderableChildren.length);
	}

	/**
	 * 
	 */
	@Test
	public void testAddRemoveRenderableChildCloneCase() {
		String name = "egg";
		StageImplementation stage = new StageImplementation(
				name,
				new VariableImplementation[0],
				new HashMap<String, ListImplementation>(),
				new SoundImplementation[0],
				new CostumeImplementation[] {
						new FakeCostume("coz", 0)
				},
				0, 1, "2", 3, 1.5, new HashMap<String,Object>(), runtime);

		Sprite sprite1 = new FakeSprite("Sprite 1");
		Sprite sprite2 = new FakeSprite("Sprite 2");
		Sprite sprite3 = new FakeSprite("Sprite 3");
		Sprite clone1 = new FakeSprite(sprite1);
		Sprite clone2 = new FakeSprite(sprite2);
		Sprite clone3 = new FakeSprite(sprite3);
		Sprite otherSprite = new FakeSprite("Other Sprite");
		RenderableChild[] renderableChildren;
		
		stage.addChild(sprite1);
		stage.addChild(sprite2);
		stage.addChild(sprite3);
		
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);

		stage.addChild(clone1);
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(4,renderableChildren.length);
		assertEquals(clone1,renderableChildren[0]);
		assertEquals(sprite1,renderableChildren[1]);
		assertEquals(sprite2,renderableChildren[2]);
		assertEquals(sprite3,renderableChildren[3]);

		stage.addChild(clone2);
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(5,renderableChildren.length);
		assertEquals(clone1,renderableChildren[0]);
		assertEquals(sprite1,renderableChildren[1]);
		assertEquals(clone2,renderableChildren[2]);
		assertEquals(sprite2,renderableChildren[3]);
		assertEquals(sprite3,renderableChildren[4]);

		stage.addChild(clone3);
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(6,renderableChildren.length);
		assertEquals(clone1,renderableChildren[0]);
		assertEquals(sprite1,renderableChildren[1]);
		assertEquals(clone2,renderableChildren[2]);
		assertEquals(sprite2,renderableChildren[3]);
		assertEquals(clone3,renderableChildren[4]);
		assertEquals(sprite3,renderableChildren[5]);
		
		assertTrue(stage.removeChild(clone2));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(5,renderableChildren.length);
		assertEquals(clone1,renderableChildren[0]);
		assertEquals(sprite1,renderableChildren[1]);
		assertEquals(sprite2,renderableChildren[2]);
		assertEquals(clone3,renderableChildren[3]);
		assertEquals(sprite3,renderableChildren[4]);
		
		assertTrue(stage.removeChild(sprite2));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(4,renderableChildren.length);
		assertEquals(clone1,renderableChildren[0]);
		assertEquals(sprite1,renderableChildren[1]);
		assertEquals(clone3,renderableChildren[2]);
		assertEquals(sprite3,renderableChildren[3]);
		
		assertTrue(stage.removeChild(sprite3));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(clone1,renderableChildren[0]);
		assertEquals(sprite1,renderableChildren[1]);
		assertEquals(clone3,renderableChildren[2]);
		
		assertTrue(stage.removeChild(sprite1));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(2,renderableChildren.length);
		assertEquals(clone1,renderableChildren[0]);
		assertEquals(clone3,renderableChildren[1]);
		
		assertTrue(stage.removeChild(clone1));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(1,renderableChildren.length);
		assertEquals(clone3,renderableChildren[0]);
		
		assertTrue(stage.removeChild(clone3));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(0,renderableChildren.length);

		assertFalse(stage.removeChild(otherSprite));
		assertFalse(stage.removeChild(clone3));
	}

	/**
	 * 
	 */
	@Test
	public void testSendToBack() {
		String name = "egg";
		StageImplementation stage = new StageImplementation(
				name,
				new VariableImplementation[0],
				new HashMap<String, ListImplementation>(),
				new SoundImplementation[0],
				new CostumeImplementation[] {
						new FakeCostume("coz", 0)
				},
				0, 1, "2", 3, 1.5, new HashMap<String,Object>(), runtime);

		Sprite sprite1 = new FakeSprite("Sprite 1");
		Sprite sprite2 = new FakeSprite("Sprite 2");
		Sprite sprite3 = new FakeSprite("Sprite 3");
		Sprite otherSprite = new FakeSprite("Other Sprite");
		stage.addChild(sprite1);
		stage.addChild(sprite2);
		stage.addChild(sprite3);
		
		RenderableChild[] renderableChildren;
		
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
		
		assertTrue(stage.sendToBack(sprite1));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
		
		assertTrue(stage.sendToBack(sprite2));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite2,renderableChildren[0]);
		assertEquals(sprite1,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
		
		assertTrue(stage.sendToBack(sprite3));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite3,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite1,renderableChildren[2]);
		
		assertFalse(stage.sendToBack(otherSprite));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite3,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite1,renderableChildren[2]);
	}

	/**
	 * 
	 */
	@Test
	public void testBringToFront() {
		String name = "egg";
		StageImplementation stage = new StageImplementation(
				name,
				new VariableImplementation[0],
				new HashMap<String, ListImplementation>(),
				new SoundImplementation[0],
				new CostumeImplementation[] {
						new FakeCostume("coz", 0)
				},
				0, 1, "2", 3, 1.5, new HashMap<String,Object>(), runtime);

		Sprite sprite1 = new FakeSprite("Sprite 1");
		Sprite sprite2 = new FakeSprite("Sprite 2");
		Sprite sprite3 = new FakeSprite("Sprite 3");
		Sprite otherSprite = new FakeSprite("Other Sprite");
		stage.addChild(sprite1);
		stage.addChild(sprite2);
		stage.addChild(sprite3);
		
		RenderableChild[] renderableChildren;
		
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
		
		assertTrue(stage.bringToFront(sprite3));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
		
		assertTrue(stage.bringToFront(sprite2));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite3,renderableChildren[1]);
		assertEquals(sprite2,renderableChildren[2]);
		
		assertTrue(stage.bringToFront(sprite1));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite3,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite1,renderableChildren[2]);
		
		assertFalse(stage.bringToFront(otherSprite));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite3,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite1,renderableChildren[2]);
	}

	/**
	 * 
	 */
	@Test
	public void testSendBackNLayers() {
		String name = "egg";
		StageImplementation stage = new StageImplementation(
				name,
				new VariableImplementation[0],
				new HashMap<String, ListImplementation>(),
				new SoundImplementation[0],
				new CostumeImplementation[] {
						new FakeCostume("coz", 0)
				},
				0, 1, "2", 3, 1.5, new HashMap<String,Object>(), runtime);

		Sprite sprite1 = new FakeSprite("Sprite 1");
		Sprite sprite2 = new FakeSprite("Sprite 2");
		Sprite sprite3 = new FakeSprite("Sprite 3");
		Sprite otherSprite = new FakeSprite("Other Sprite");
		stage.addChild(sprite1);
		stage.addChild(sprite2);
		stage.addChild(sprite3);
		
		RenderableChild[] renderableChildren;
		
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
		
		assertTrue(stage.sendBackNLayers(sprite2,1));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite2,renderableChildren[0]);
		assertEquals(sprite1,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
		
		assertTrue(stage.sendBackNLayers(sprite2,-1));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
		
		assertTrue(stage.sendBackNLayers(sprite3,2));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite3,renderableChildren[0]);
		assertEquals(sprite1,renderableChildren[1]);
		assertEquals(sprite2,renderableChildren[2]);
		
		assertTrue(stage.sendBackNLayers(sprite3,-2));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
		
		assertTrue(stage.sendBackNLayers(sprite2,2));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite2,renderableChildren[0]);
		assertEquals(sprite1,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
		
		assertTrue(stage.sendBackNLayers(sprite2,-1));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
		
		assertTrue(stage.sendBackNLayers(sprite2,-2));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite3,renderableChildren[1]);
		assertEquals(sprite2,renderableChildren[2]);
		
		assertTrue(stage.sendBackNLayers(sprite2,1));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
		
		assertFalse(stage.sendBackNLayers(otherSprite,1));
		renderableChildren = stage.getAllRenderableChildren();
		assertEquals(3,renderableChildren.length);
		assertEquals(sprite1,renderableChildren[0]);
		assertEquals(sprite2,renderableChildren[1]);
		assertEquals(sprite3,renderableChildren[2]);
	}
	
	private class FakeCostume extends CostumeImplementation{

		public FakeCostume(String costumeName, long baseLayerID) {
			super(costumeName, baseLayerID, ""+costumeName.hashCode(), 1, 0, 0, null);
		}

		@Override
		protected void registerSprite(SpriteImplementation sprite, double scale, double direction, boolean mirror) {
			// Don't really try to register.
		}
	}
	
	private class FakeSprite extends AllBadSprite{
		private String name;
		private Sprite parent;
		
		public FakeSprite(String name) {
			this.name = name;
		}

		public FakeSprite(Sprite parent) {
			this.name = parent.getObjName();
			this.parent = parent;
		}

		@Override
		public String getObjName() {
			return name;
		}

		@Override
		public boolean isClone() {
			return parent!=null;
		}

		@Override
		public Sprite getCloneParent() {
			return parent;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
