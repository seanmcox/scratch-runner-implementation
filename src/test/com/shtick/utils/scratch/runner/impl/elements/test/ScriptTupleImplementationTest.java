/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import com.shtick.utils.scratch.runner.core.FeatureSet;
import com.shtick.utils.scratch.runner.core.GraphicEffect;
import com.shtick.utils.scratch.runner.core.InvalidScriptDefinitionException;
import com.shtick.utils.scratch.runner.core.Opcode;
import com.shtick.utils.scratch.runner.core.OpcodeAction;
import com.shtick.utils.scratch.runner.core.OpcodeControl;
import com.shtick.utils.scratch.runner.core.OpcodeSubaction;
import com.shtick.utils.scratch.runner.core.ScratchRuntime;
import com.shtick.utils.scratch.runner.core.ScriptTupleRunner;
import com.shtick.utils.scratch.runner.core.SoundMonitor;
import com.shtick.utils.scratch.runner.core.StageMonitorCommand;
import com.shtick.utils.scratch.runner.core.ValueListener;
import com.shtick.utils.scratch.runner.core.elements.BlockTuple;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.core.elements.control.BasicJumpBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.ReadLocalVarBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.SetLocalVarBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.TestBlockTuple;
import com.shtick.utils.scratch.runner.impl.bundle.FeatureSetGeneratorTracker;
import com.shtick.utils.scratch.runner.impl.elements.BlockTupleImplementation;
import com.shtick.utils.scratch.runner.impl.elements.ScriptTupleImplementation;
import com.shtick.utils.scratch.runner.standard.blocks.util.DummyScratchRuntimeImplementation;

/**
 * @author sean.cox
 *
 */
public class ScriptTupleImplementationTest {
	private static final OpcodeFeatureSet OPCODE_REGISTRY=new OpcodeFeatureSet();
	private static final DummyScratchRuntimeImplementation RUNTIME;
	static {
		OPCODE_REGISTRY.registerOpcode(new NopOpcode("1"));
		OPCODE_REGISTRY.registerOpcode(new NopOpcode("2"));
		OPCODE_REGISTRY.registerOpcode(new NopOpcode("3"));
		OPCODE_REGISTRY.registerOpcode(new NopOpcode("4"));
		OPCODE_REGISTRY.registerOpcode(new NopOpcode("5"));
		OPCODE_REGISTRY.registerOpcode(new NopOpcode());
		OPCODE_REGISTRY.registerOpcode(new EmptyOpcode());
		OPCODE_REGISTRY.registerOpcode(new SimpleWrapperOpcode());
		OPCODE_REGISTRY.registerOpcode(new ForeverLoopOpcode());
		OPCODE_REGISTRY.registerOpcode(new SkipOpcode());
		OPCODE_REGISTRY.registerOpcode(new PointlessLocalOpcode());
		OPCODE_REGISTRY.registerOpcode(new NestedPointlessLocalOpcode());
		try {
			RUNTIME = new DummyScratchRuntimeImplementation(480,360,OPCODE_REGISTRY);
		}
		catch(IOException t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * 
	 */
	@Test
	public void testConstructor() {
		AllBadScriptContext context = new AllBadScriptContext();
		BlockTuple[] blockTuples = new BlockTuple[] {
				new BlockTupleImplementation("1",new ArrayList<>(0)),
				new BlockTupleImplementation("2",new ArrayList<>(0)),
				new BlockTupleImplementation("3",new ArrayList<>(0)),
				new BlockTupleImplementation("4",new ArrayList<>(0)),
				new BlockTupleImplementation("5",new ArrayList<>(0))
		};
		ScriptTupleImplementation tuple = null;
		try {
			tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
		}
		catch(InvalidScriptDefinitionException t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
		assertEquals(context,tuple.getContext());
		assertArrayEquals(blockTuples,tuple.getBlockTuples().toArray());
	}

	/**
	 * 
	 */
	@Test
	public void testToArray() {
		AllBadScriptContext context = new AllBadScriptContext();
		BlockTuple[] blockTuples = new BlockTuple[] {
				new BlockTupleImplementation("1",new ArrayList<>(0)),
				new BlockTupleImplementation("2",new ArrayList<>(0)),
				new BlockTupleImplementation("3",new ArrayList<>(0)),
				new BlockTupleImplementation("4",new ArrayList<>(0)),
				new BlockTupleImplementation("5",new ArrayList<>(0))
		};
		ScriptTupleImplementation tuple = null;
		try {
			tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
		}
		catch(InvalidScriptDefinitionException t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
		assertArrayEquals(blockTuples,tuple.toArray());
	}

	/**
	 * 
	 */
	@Test
	public void testBlockTupleFunctions() {
		AllBadScriptContext context = new AllBadScriptContext();
		BlockTuple[] blockTuples = new BlockTuple[] {
				new BlockTupleImplementation("1",new ArrayList<>(0)),
				new BlockTupleImplementation("2",new ArrayList<>(0)),
				new BlockTupleImplementation("3",new ArrayList<>(0)),
				new BlockTupleImplementation("4",new ArrayList<>(0)),
				new BlockTupleImplementation("5",new ArrayList<>(0))
		};
		ScriptTupleImplementation tuple = null;
		try {
			tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
		}
		catch(InvalidScriptDefinitionException t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
		assertEquals(blockTuples.length,tuple.getBlockTupleCount());
		for(int i=0;i<blockTuples.length;i++) {
			assertEquals(blockTuples[i],tuple.getBlockTuple(i));
		}
	}

	/**
	 * 
	 */
	@Test
	public void testBasicScriptResolution() {
		AllBadScriptContext context = new AllBadScriptContext();
		
		try{ // Simple nop
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertArrayEquals(blockTuples,resolved);
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // Simple nop, nop
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0)),
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertArrayEquals(blockTuples,resolved);
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // Simple empty... super basic control substitution is happening.
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("empty",new ArrayList<>(0))
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertArrayEquals(new BlockTuple[0], resolved);
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // Sandwiched empty... super basic control substitution is happening.
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0)),
					new BlockTupleImplementation("empty",new ArrayList<>(0)),
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertArrayEquals(new BlockTuple[] {blockTuples[0],blockTuples[2]}, resolved);
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // Simple wrapper... super basic control substitution is happening.
			BlockTuple[] blockTuplesNested = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("wrapper",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertArrayEquals(blockTuplesNested, resolved);
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // Forever loop... basic loop substitution.
			BlockTuple[] blockTuplesNested = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("foreverLoop",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertEquals(2,resolved.length);
			assertEquals(blockTuplesNested[0], resolved[0]);
			assertTrue(resolved[1] instanceof BasicJumpBlockTuple);
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // skip... basic control substitution.
			BlockTuple[] blockTuplesNested = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("skip",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertEquals(2,resolved.length);
			assertEquals(blockTuplesNested[0], resolved[1]);
			assertTrue(resolved[0] instanceof BasicJumpBlockTuple);
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
	}

	/**
	 * 
	 */
	@Test
	public void testBasicJumpResolution() {
		AllBadScriptContext context = new AllBadScriptContext();
		
		try{ // Forever loop... basic jump.
			BlockTuple[] blockTuplesNested = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("foreverLoop",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertEquals(2,resolved.length);
			assertTrue(resolved[1] instanceof BasicJumpBlockTuple);
			assertEquals(0,((BasicJumpBlockTuple)resolved[1]).getIndex().intValue());
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // skip... basic jump.
			BlockTuple[] blockTuplesNested = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("skip",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertEquals(2,resolved.length);
			assertTrue(resolved[0] instanceof BasicJumpBlockTuple);
			assertEquals(2,((BasicJumpBlockTuple)resolved[0]).getIndex().intValue());
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // Forever loop -> skip... sequential jumps reindexing.
			BlockTuple[] blockTuplesNested = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("foreverLoop",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
					new BlockTupleImplementation("skip",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertEquals(4,resolved.length);
			assertTrue(resolved[1] instanceof BasicJumpBlockTuple);
			assertEquals(0,((BasicJumpBlockTuple)resolved[1]).getIndex().intValue());
			assertTrue(resolved[2] instanceof BasicJumpBlockTuple);
			assertEquals(4,((BasicJumpBlockTuple)resolved[2]).getIndex().intValue());
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // Skip -> Forever loop... sequential jumps reindexing.
			BlockTuple[] blockTuplesNested = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("skip",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
					new BlockTupleImplementation("foreverLoop",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertEquals(4,resolved.length);
			assertTrue(resolved[3] instanceof BasicJumpBlockTuple);
			assertEquals(2,((BasicJumpBlockTuple)resolved[3]).getIndex().intValue());
			assertTrue(resolved[0] instanceof BasicJumpBlockTuple);
			assertEquals(2,((BasicJumpBlockTuple)resolved[0]).getIndex().intValue());
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNestedJumpResolution() {
		AllBadScriptContext context = new AllBadScriptContext();
		
		try{ // Nested base remains zero. No reindexing necessary.
			BlockTuple[] blockTuplesNestedNested = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			BlockTuple[] blockTuplesNested = new BlockTuple[] {
					new BlockTupleImplementation("foreverLoop",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNestedNested)})
							)),
			};
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("foreverLoop",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertEquals(3,resolved.length);
			assertTrue(resolved[1] instanceof BasicJumpBlockTuple);
			assertTrue(resolved[2] instanceof BasicJumpBlockTuple);
			assertEquals(0,((BasicJumpBlockTuple)resolved[1]).getIndex().intValue());
			assertEquals(0,((BasicJumpBlockTuple)resolved[2]).getIndex().intValue());
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // Nested skips ... All jumps point ot the end.
			BlockTuple[] blockTuplesNestedNested = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			BlockTuple[] blockTuplesNested = new BlockTuple[] {
					new BlockTupleImplementation("skip",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNestedNested)})
							)),
			};
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("skip",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertEquals(3,resolved.length);
			assertTrue(resolved[0] instanceof BasicJumpBlockTuple);
			assertTrue(resolved[1] instanceof BasicJumpBlockTuple);
			assertEquals(3,((BasicJumpBlockTuple)resolved[0]).getIndex().intValue());
			assertEquals(3,((BasicJumpBlockTuple)resolved[1]).getIndex().intValue());
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // Forever loop -> skip... sequential jumps reindexing.
			BlockTuple[] blockTuplesNested = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("foreverLoop",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
					new BlockTupleImplementation("skip",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertEquals(4,resolved.length);
			assertTrue(resolved[1] instanceof BasicJumpBlockTuple);
			assertEquals(0,((BasicJumpBlockTuple)resolved[1]).getIndex().intValue());
			assertTrue(resolved[2] instanceof BasicJumpBlockTuple);
			assertEquals(4,((BasicJumpBlockTuple)resolved[2]).getIndex().intValue());
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // Forever loop -> skip... sequential jumps reindexing.
			BlockTuple[] blockTuplesNested = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("skip",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
					new BlockTupleImplementation("foreverLoop",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			BlockTuple[] resolved = tuple.getResolvedBlockTuples();
			assertEquals(4,resolved.length);
			assertTrue(resolved[3] instanceof BasicJumpBlockTuple);
			assertEquals(2,((BasicJumpBlockTuple)resolved[3]).getIndex().intValue());
			assertTrue(resolved[0] instanceof BasicJumpBlockTuple);
			assertEquals(2,((BasicJumpBlockTuple)resolved[0]).getIndex().intValue());
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
	}

	/**
	 * 
	 */
	@Test
	public void testLocalVariableBlockResolution() {
		AllBadScriptContext context = new AllBadScriptContext();
		
		try{ // No local vars.
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0))
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			assertEquals(0,tuple.getLocalVariableCount());
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // One local vars.
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0)),
					new BlockTupleImplementation("pointlessLocal",new ArrayList<>(0))
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			assertEquals(1,tuple.getLocalVariableCount());
			assertEquals(0,((SetLocalVarBlockTuple)tuple.getResolvedBlockTuples()[1]).getLocalVarIdentifier().intValue());
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // Serial local vars.
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0)),
					new BlockTupleImplementation("pointlessLocal",new ArrayList<>(0)),
					new BlockTupleImplementation("nop",new ArrayList<>(0)),
					new BlockTupleImplementation("pointlessLocal",new ArrayList<>(0))
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			assertEquals(1,tuple.getLocalVariableCount());
			assertEquals(0,((SetLocalVarBlockTuple)tuple.getResolvedBlockTuples()[1]).getLocalVarIdentifier().intValue());
			assertEquals(0,((SetLocalVarBlockTuple)tuple.getResolvedBlockTuples()[3]).getLocalVarIdentifier().intValue());
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // Nested trivial var.
			BlockTuple[] blockTuplesNested = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0)),
					new BlockTupleImplementation("pointlessLocal",new ArrayList<>(0)),
			};
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0)),
					new BlockTupleImplementation("pointlessLocal",new ArrayList<>(0)),
					new BlockTupleImplementation("foreverLoop",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			assertEquals(1,tuple.getLocalVariableCount());
			assertEquals(0,((SetLocalVarBlockTuple)tuple.getResolvedBlockTuples()[1]).getLocalVarIdentifier().intValue());
			assertEquals(0,((SetLocalVarBlockTuple)tuple.getResolvedBlockTuples()[3]).getLocalVarIdentifier().intValue());
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
		
		try{ // Nested significant var.
			BlockTuple[] blockTuplesNested = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0)),
					new BlockTupleImplementation("pointlessLocal",new ArrayList<>(0)),
			};
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0)),
					new BlockTupleImplementation("pointlessLocal",new ArrayList<>(0)),
					new BlockTupleImplementation("pointlessLocalNested",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)})
							)),
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			assertEquals(2,tuple.getLocalVariableCount());
			assertEquals(0,((SetLocalVarBlockTuple)tuple.getResolvedBlockTuples()[1]).getLocalVarIdentifier().intValue());
			assertEquals(0,((SetLocalVarBlockTuple)tuple.getResolvedBlockTuples()[2]).getLocalVarIdentifier().intValue());
			assertEquals(1,((SetLocalVarBlockTuple)tuple.getResolvedBlockTuples()[5]).getLocalVarIdentifier().intValue());
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
	}

	/**
	 * 
	 */
	@Test
	public void testLocalVariableValueBlockResolution() {
		AllBadScriptContext context = new AllBadScriptContext();
		try{ // Nested significant var.
			BlockTuple[] blockTuplesNested = new BlockTuple[] {
					new BlockTupleImplementation("nop",new ArrayList<>(0)),
					new BlockTupleImplementation("pointlessLocalNested",new ArrayList<>(Arrays.asList(new Object[] {
							new ArrayList<>(0)
					}))),
			};
			BlockTuple[] blockTuples = new BlockTuple[] {
					new BlockTupleImplementation("pointlessLocalNested",new ArrayList<>(Arrays.asList(new Object[] {
							Arrays.asList(blockTuplesNested)
							}))),
			};
			ScriptTupleImplementation tuple = new ScriptTupleImplementation(context, blockTuples, RUNTIME);
			assertEquals(2,tuple.getLocalVariableCount());
			assertEquals(0,((SetLocalVarBlockTuple)tuple.getResolvedBlockTuples()[0]).getLocalVarIdentifier().intValue());
			assertEquals(0,((ReadLocalVarBlockTuple)((TestBlockTuple)tuple.getResolvedBlockTuples()[1]).getArguments().get(0)).getLocalVarIdentifier().intValue());
			assertEquals(1,((SetLocalVarBlockTuple)tuple.getResolvedBlockTuples()[3]).getLocalVarIdentifier().intValue());
			assertEquals(1,((ReadLocalVarBlockTuple)((TestBlockTuple)tuple.getResolvedBlockTuples()[4]).getArguments().get(0)).getLocalVarIdentifier().intValue());
		}
		catch(InvalidScriptDefinitionException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
	}
	
	private static class OpcodeFeatureSet implements FeatureSet{
		private HashMap<String,Opcode> opcodes = new HashMap<>();
		
		public void registerOpcode(Opcode opcode) {
			opcodes.put(opcode.getOpcode(), opcode);
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.FeatureSet#getOpcode(java.lang.String)
		 */
		@Override
		public Opcode getOpcode(String opcode) {
			return opcodes.get(opcode);
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.FeatureSet#getOpcodes()
		 */
		@Override
		public Set<Opcode> getOpcodes() {
			return new HashSet<>(opcodes.values());
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.FeatureSet#getGraphicEffect(java.lang.String)
		 */
		@Override
		public GraphicEffect getGraphicEffect(String id) {
			return null;
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.FeatureSet#getGraphicEffects()
		 */
		@Override
		public Set<GraphicEffect> getGraphicEffects() {
			return null;
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.FeatureSet#getStageMonitorCommand(java.lang.String)
		 */
		@Override
		public StageMonitorCommand getStageMonitorCommand(String id) {
			return null;
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.FeatureSet#getStageMonitorCommands()
		 */
		@Override
		public Set<StageMonitorCommand> getStageMonitorCommands() {
			return null;
		}
	}
	
	private static class DumbyOpcodeTracker extends FeatureSetGeneratorTracker{
		private static HashMap<String,Opcode> opcodes = new HashMap<>(10);

		public DumbyOpcodeTracker() {
			super(new BundleContext() {
				@Override
				public boolean ungetService(ServiceReference<?> arg0) {
					return false;
				}
				
				@Override
				public void removeServiceListener(ServiceListener arg0) {}
				
				@Override
				public void removeFrameworkListener(FrameworkListener arg0) {}
				
				@Override
				public void removeBundleListener(BundleListener arg0) {}
				
				@Override
				public <S> ServiceRegistration<S> registerService(Class<S> arg0, S arg1, Dictionary<String, ?> arg2) {
					return null;
				}
				
				@Override
				public ServiceRegistration<?> registerService(String arg0, Object arg1, Dictionary<String, ?> arg2) {
					return null;
				}
				
				@Override
				public ServiceRegistration<?> registerService(String[] arg0, Object arg1, Dictionary<String, ?> arg2) {
					return null;
				}
				
				@Override
				public Bundle installBundle(String arg0, InputStream arg1) throws BundleException {
					return null;
				}
				
				@Override
				public Bundle installBundle(String arg0) throws BundleException {
					return null;
				}
				
				@Override
				public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> arg0, String arg1)
						throws InvalidSyntaxException {
					return null;
				}
				
				@Override
				public ServiceReference<?>[] getServiceReferences(String arg0, String arg1) throws InvalidSyntaxException {
					return null;
				}
				
				@Override
				public <S> ServiceReference<S> getServiceReference(Class<S> arg0) {
					return null;
				}
				
				@Override
				public ServiceReference<?> getServiceReference(String arg0) {
					return null;
				}
				
				@Override
				public <S> S getService(ServiceReference<S> arg0) {
					return null;
				}
				
				@Override
				public String getProperty(String arg0) {
					return null;
				}
				
				@Override
				public File getDataFile(String arg0) {
					return null;
				}
				
				@Override
				public Bundle[] getBundles() {
					return null;
				}
				
				@Override
				public Bundle getBundle(String arg0) {
					return null;
				}
				
				@Override
				public Bundle getBundle(long arg0) {
					return null;
				}
				
				@Override
				public Bundle getBundle() {
					return null;
				}
				
				@Override
				public ServiceReference<?>[] getAllServiceReferences(String arg0, String arg1) throws InvalidSyntaxException {
					return null;
				}
				
				@Override
				public Filter createFilter(String arg0) throws InvalidSyntaxException {
					return null;
				}
				
				@Override
				public void addServiceListener(ServiceListener arg0, String arg1) throws InvalidSyntaxException {}
				
				@Override
				public void addServiceListener(ServiceListener arg0) {}
				
				@Override
				public void addFrameworkListener(FrameworkListener arg0) {}
				
				@Override
				public void addBundleListener(BundleListener arg0) {}

				@Override
				public <S> ServiceRegistration<S> registerService(Class<S> arg0, ServiceFactory<S> arg1,
						Dictionary<String, ?> arg2) {
					return null;
				}

				@Override
				public <S> ServiceObjects<S> getServiceObjects(ServiceReference<S> arg0) {
					return null;
				}
			});
		}
	}
	
	private class AllBadScriptContext implements ScriptContext{

		@Override
		public ScriptContext getContextObject() {
			throw new UnsupportedOperationException("Called getContextObject when not expected.");
		}

		@Override
		public String getObjName() {
			throw new UnsupportedOperationException("Called getObjName when not expected.");
		}

		@Override
		public com.shtick.utils.scratch.runner.core.elements.List getContextListByName(String name) {
			throw new UnsupportedOperationException("Called getContextListByName when not expected.");
		}

		@Override
		public Object getContextVariableValueByName(String name) throws IllegalArgumentException {
			throw new UnsupportedOperationException("Called getContextVariableValueByName when not expected.");
		}

		@Override
		public void setContextVariableValueByName(String name, Object value) throws IllegalArgumentException {
			throw new UnsupportedOperationException("Called setContextVariableValueByName when not expected.");
		}

		@Override
		public void addVariableListener(String var, ValueListener listener) {
			throw new UnsupportedOperationException("Called addVariableListener when not expected.");
		}

		@Override
		public void removeVariableListener(String var, ValueListener listener) {
			throw new UnsupportedOperationException("Called removeVariableListener when not expected.");
		}

		@Override
		public Object getContextPropertyValueByName(String name) throws IllegalArgumentException {
			throw new UnsupportedOperationException("Called getContextPropertyValueByName when not expected.");
		}

		@Override
		public void addContextPropertyListener(String property, ValueListener listener) {
			throw new UnsupportedOperationException("Called addContextPropertyListener when not expected.");
		}

		@Override
		public void removeContextPropertyListener(String property, ValueListener listener) {
			throw new UnsupportedOperationException("Called removeContextPropertyListener when not expected.");
		}

		@Override
		public SoundMonitor playSoundByName(String soundName) {
			throw new UnsupportedOperationException("Called playSoundByName when not expected.");
		}

		@Override
		public SoundMonitor playSoundByIndex(int index) {
			throw new UnsupportedOperationException("Called playSoundByName when not expected.");
		}

		@Override
		public void setVolume(double volume) {
			throw new UnsupportedOperationException("Called setVolume when not expected.");
		}

		@Override
		public double getVolume() {
			throw new UnsupportedOperationException("Called getVolume when not expected.");
		}

		@Override
		public void addVolumeListener(ValueListener listener) {
			throw new UnsupportedOperationException("Called addVolumeListener when not expected.");
		}

		@Override
		public void removeVolumeListener(ValueListener listener) {
			throw new UnsupportedOperationException("Called removeVolumeListener when not expected.");
		}

		@Override
		public void stopScripts() {
			throw new UnsupportedOperationException("Called stopScripts when not expected.");
		}
	}
	
	private static class NopOpcode implements OpcodeAction{
		private String opcode;
		
		public NopOpcode() {
			this.opcode = "nop";
		}

		public NopOpcode(String opcode) {
			this.opcode = opcode;
		}

		@Override
		public String getOpcode() {
			return opcode;
		}

		@Override
		public DataType[] getArgumentTypes() {
			return new DataType[0];
		}

		@Override
		public OpcodeSubaction execute(ScratchRuntime runtime, ScriptTupleRunner scriptRunner, ScriptContext context,
				Object[] arguments) {
			return null;
			// TODO Test yield handling.
		}
		
	}
	
	private static class EmptyOpcode implements OpcodeControl{
		@Override
		public String getOpcode() {
			return "empty";
		}

		@Override
		public DataType[] getArgumentTypes() {
			return new DataType[0];
		}

		@Override
		public BlockTuple[] execute(List<Object> arguments) {
			return new BlockTuple[0];
		}
	}
	
	private static class SimpleWrapperOpcode implements OpcodeControl{
		@Override
		public String getOpcode() {
			return "wrapper";
		}

		@Override
		public DataType[] getArgumentTypes() {
			return new DataType[] {DataType.SCRIPT};
		}

		@Override
		public BlockTuple[] execute(List<Object> arguments) {
			return (BlockTuple[])((java.util.List<?>)arguments.get(0)).toArray(new BlockTuple[] {});
		}
	}
	
	private static class ForeverLoopOpcode implements OpcodeControl{
		@Override
		public String getOpcode() {
			return "foreverLoop";
		}

		@Override
		public DataType[] getArgumentTypes() {
			return new DataType[] {DataType.SCRIPT};
		}

		@Override
		public BlockTuple[] execute(List<Object> arguments) {
			java.util.List<BlockTuple> script = ((java.util.List<BlockTuple>)arguments.get(0));
			BlockTuple[] retval = new BlockTuple[script.size()+1];
			for(int i=0;i<script.size();i++) {
				retval[i]=script.get(i);
			}
			retval[script.size()] = new BasicJumpBlockTuple(0);
			return retval;
		}
	}
	
	private static class SkipOpcode implements OpcodeControl{
		@Override
		public String getOpcode() {
			return "skip";
		}

		@Override
		public DataType[] getArgumentTypes() {
			return new DataType[] {DataType.SCRIPT};
		}

		@Override
		public BlockTuple[] execute(List<Object> arguments) {
			java.util.List<BlockTuple> script = ((java.util.List<BlockTuple>)arguments.get(0));
			BlockTuple[] retval = new BlockTuple[script.size()+1];
			retval[0] = new BasicJumpBlockTuple(retval.length);
			for(int i=0;i<script.size();i++) {
				retval[i+1]=script.get(i);
			}
			return retval;
		}
	}
	
	private static class PointlessLocalOpcode implements OpcodeControl{
		@Override
		public String getOpcode() {
			return "pointlessLocal";
		}

		@Override
		public DataType[] getArgumentTypes() {
			return new DataType[] {};
		}

		@Override
		public BlockTuple[] execute(List<Object> arguments) {
			BlockTuple[] retval = new BlockTuple[1];
			retval[0] = new SetLocalVarBlockTuple(0, 1);
			return retval;
		}
	}
	
	private static class NestedPointlessLocalOpcode implements OpcodeControl{
		@Override
		public String getOpcode() {
			return "pointlessLocalNested";
		}

		@Override
		public DataType[] getArgumentTypes() {
			return new DataType[] {DataType.SCRIPT};
		}

		@Override
		public BlockTuple[] execute(List<Object> arguments) {
			java.util.List<BlockTuple> script = ((java.util.List<BlockTuple>)arguments.get(0));
			BlockTuple[] retval = new BlockTuple[script.size()+2];
			retval[0] = new SetLocalVarBlockTuple(0, 1);
			retval[1] = new TestBlockTuple(new ReadLocalVarBlockTuple(0));
			for(int i=0;i<script.size();i++) {
				retval[i+2]=script.get(i);
			}
			return retval;
		}
	}
}
