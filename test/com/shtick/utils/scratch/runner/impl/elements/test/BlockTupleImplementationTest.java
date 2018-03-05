/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.shtick.util.GenericsResolver;
import com.shtick.utils.scratch.runner.impl.elements.BlockTupleImplementation;

/**
 * @author sean.cox
 *
 */
public class BlockTupleImplementationTest {

	/**
	 * 
	 */
	@Test
	public void testConstructor() {
		{ // Basic test
			ArrayList<Object> args = new ArrayList<>(2);
			args.add(0);
			args.add(1);
			BlockTupleImplementation subject = new BlockTupleImplementation("hello", args);
			assertEquals("hello",subject.getOpcode());
			assertNotNull(subject.getArguments());
			assertEquals(args.size(),subject.getArguments().size());
			for(int i=0;i<args.size();i++)
				assertEquals(args.get(i),subject.getArguments().get(i));
		}

		{ // A little variety
			ArrayList<Object> args = new ArrayList<>(4);
			args.add(new Object());
			args.add(null);
			args.add("to die");
			args.add(256);
			BlockTupleImplementation subject = new BlockTupleImplementation("world", args);
			assertEquals("world",subject.getOpcode());
			assertNotNull(subject.getArguments());
			assertEquals(args.size(),subject.getArguments().size());
			for(int i=0;i<args.size();i++)
				assertEquals(args.get(i),subject.getArguments().get(i));
		}

		{ // Empty args
			ArrayList<Object> args = new ArrayList<>(0);
			BlockTupleImplementation subject = new BlockTupleImplementation("blah", args);
			assertEquals("blah",subject.getOpcode());
			assertNotNull(subject.getArguments());
			assertEquals(0,subject.getArguments().size());
		}
	}

	/**
	 * 
	 */
	@Test
	public void testToArray() {
		{ // Basic test
			ArrayList<Object> args = new ArrayList<>(2);
			args.add(0);
			args.add(1);
			BlockTupleImplementation subject = new BlockTupleImplementation("hello", args);
			Object[] array = subject.toArray();
			assertEquals(args.size()+1,array.length);
			assertEquals("hello",array[0]);
			for(int i=0;i<args.size();i++)
				assertEquals(args.get(i),array[i+1]);
		}

		{ // A little variety
			ArrayList<Object> args = new ArrayList<>(4);
			args.add(new Object());
			args.add(null);
			args.add("to die");
			args.add(256);
			BlockTupleImplementation subject = new BlockTupleImplementation("world", args);
			Object[] array = subject.toArray();
			assertEquals(args.size()+1,array.length);
			assertEquals("world",array[0]);
			for(int i=0;i<args.size();i++)
				assertEquals(args.get(i),array[i+1]);
		}

		{ // Empty args
			ArrayList<Object> args = new ArrayList<>(0);
			BlockTupleImplementation subject = new BlockTupleImplementation("blah", args);
			Object[] array = subject.toArray();
			assertEquals(1,array.length);
			assertEquals("blah",array[0]);
		}
	}
}
