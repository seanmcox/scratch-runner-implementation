/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;

import com.shtick.utils.scratch.runner.core.elements.List;
import com.shtick.utils.scratch.runner.core.elements.Sprite;
import com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation;
import com.shtick.utils.scratch.runner.impl.elements.CostumeImplementation;
import com.shtick.utils.scratch.runner.impl.elements.ListImplementation;
import com.shtick.utils.scratch.runner.impl.elements.SoundImplementation;
import com.shtick.utils.scratch.runner.impl.elements.SpriteImplementation;
import com.shtick.utils.scratch.runner.impl.elements.VariableImplementation;
import com.shtick.utils.scratch.runner.standard.blocks.util.AllBadStage;
import com.shtick.utils.scratch.runner.standard.blocks.util.DummyScratchRuntimeImplementation;

/**
 * @author sean.cox
 *
 */
public class SpriteImplementationTest {
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
			SpriteImplementation sprite = new SpriteImplementation(
					name,
					new VariableImplementation[0],
					new HashMap<String, ListImplementation>(),
					new SoundImplementation[0],
					new CostumeImplementation[] {
							new FakeCostume("coz", 0)
					},
					0, 1, 2, 3, 1.5, Sprite.ROTATION_STYLE_NORMAL, false, 0, true, new HashMap<String,Object>(), new FakeStage(), runtime);
			assertEquals(name, sprite.getObjName());
			assertNull(sprite.getContextVariableValueByName("testVar"));
			assertNull(sprite.getContextListByName("testList"));
			// TODO It would be better to directly access details about the sound data.
			assertNull(sprite.playSoundByName("testSound"));
			assertEquals(1,sprite.getCostumeCount());
			assertEquals(0,sprite.getCurrentCostumeIndex());
			assertEquals(1.0,sprite.getScratchX(),0.0001);
			assertEquals(2.0,sprite.getScratchY(),0.0001);
			assertEquals(3.0,sprite.getScale(),0.0001);
			assertEquals(1.5,sprite.getDirection(),0.0001);
			assertEquals(Sprite.ROTATION_STYLE_NORMAL,sprite.getRotationStyle());
			assertTrue(sprite.isVisible());
			assertEquals(0,sprite.getSpriteInfo().size());
		}

		{
			String name = "salad";
			HashMap<String, ListImplementation> listMap = new HashMap<String, ListImplementation>();
			listMap.put("testList", new ListImplementation("testList", new Object[0], 0.0, 0.0, 0.0, 0.0, false, runtime));
			HashMap<String, Object> spriteInfo = new HashMap<String,Object>();
			spriteInfo.put("key", "value");
			SpriteImplementation sprite = new SpriteImplementation(
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
					1, 5, 6, 7, 0.5, Sprite.ROTATION_STYLE_NONE, false, 0, false, spriteInfo, new FakeStage(), runtime);
			assertEquals(name, sprite.getObjName());
			assertEquals(2,sprite.getContextVariableValueByName("testVar"));
			assertNotNull(sprite.getContextListByName("testList"));
			// TODO It would be better to directly access details about the sound data.
			try {
				sprite.playSoundByName("testSound");
				fail("Exception should be thrown as the sprite tries to play a non-existent sound.");
			}
			catch(Throwable t) {}
			assertEquals(2,sprite.getCostumeCount());
			assertEquals(1,sprite.getCurrentCostumeIndex());
			assertEquals(5.0,sprite.getScratchX(),0.0001);
			assertEquals(6.0,sprite.getScratchY(),0.0001);
			assertEquals(7.0,sprite.getScale(),0.0001);
			assertEquals(0.5,sprite.getDirection(),0.0001);
			assertEquals(Sprite.ROTATION_STYLE_NONE,sprite.getRotationStyle());
			assertFalse(sprite.isVisible());
			assertEquals(1,sprite.getSpriteInfo().size());
			assertEquals("value",sprite.getSpriteInfo().get("key"));
		}
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

	private class FakeStage extends AllBadStage{

		@Override
		public List getContextListByName(String name) {
			return null;
		}

		@Override
		public Object getContextVariableValueByName(String name) throws IllegalArgumentException {
			return null;
		}
		
	}
}
