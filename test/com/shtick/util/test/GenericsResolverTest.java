/**
 * 
 */
package com.shtick.util.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.shtick.util.GenericsResolver;

/**
 * @author sean.cox
 *
 */
public class GenericsResolverTest {

	/**
	 * 
	 */
	@Test
	public void test() {
		List<Class<?>> list;
		list=GenericsResolver.getTypeArguments(SimpleInterface.class, SimpleClass1.class);
		assertEquals(0, list.size());
		list=GenericsResolver.getTypeArguments(SimpleInterface.class, SimpleClass2.class);
		assertEquals(0, list.size());

		list=GenericsResolver.getTypeArguments(SimpleGenericInterface.class, SimpleGenericClass1.class);
		assertEquals(1, list.size());
		assertEquals(Integer.class, list.get(0));
		list=GenericsResolver.getTypeArguments(SimpleGenericInterface.class, SimpleGenericClass2.class);
		assertEquals(1, list.size());
		assertEquals(Integer.class, list.get(0));
		list=GenericsResolver.getTypeArguments(SimpleGenericSubInterface.class, SimpleGenericClass1.class);
		assertEquals(1, list.size());
		assertEquals(Integer.class, list.get(0));
		list=GenericsResolver.getTypeArguments(SimpleGenericInterface.class, SimpleGenericSubInterface.class);
		assertEquals(1, list.size());
		assertEquals(null, list.get(0));

		list=GenericsResolver.getTypeArguments(ComplexGenericInterface.class, ComplexGenericClass1.class);
		assertEquals(2, list.size());
		assertEquals(Long.class, list.get(0));
		assertEquals(Double.class, list.get(1));
		list=GenericsResolver.getTypeArguments(ComplexGenericInterface.class, ComplexGenericClass2.class);
		assertEquals(2, list.size());
		assertEquals(Long.class, list.get(0));
		assertEquals(Double.class, list.get(1));
		list=GenericsResolver.getTypeArguments(ComplexGenericSubInterface.class, ComplexGenericClass1.class);
		assertEquals(2, list.size());
		assertEquals(Long.class, list.get(0));
		assertEquals(Double.class, list.get(1));
		list=GenericsResolver.getTypeArguments(ComplexGenericInterface.class, ComplexGenericSubInterface.class);
		assertEquals(2, list.size());
		assertEquals(null, list.get(0));
		assertEquals(null, list.get(1));
	}
	
	private interface SimpleInterface{}
	
	@SuppressWarnings("unused")
	private interface SimpleGenericInterface<T>{}
	
	@SuppressWarnings("unused")
	private interface ComplexGenericInterface<T,U>{}
	
	private interface SimpleSubInterface extends SimpleInterface{}
	
	private interface SimpleGenericSubInterface<T> extends SimpleGenericInterface<T>{}
	
	private interface ComplexGenericSubInterface<T,U> extends ComplexGenericInterface<T,U>{}
	
	private class SimpleClass1 implements SimpleSubInterface{}
	
	private class SimpleGenericClass1 implements SimpleGenericSubInterface<Integer>{}
	
	private class ComplexGenericClass1 implements ComplexGenericSubInterface<Long,Double>{}
	
	private class SimpleClass2 implements SimpleInterface{}
	
	private class SimpleGenericClass2 implements SimpleGenericInterface<Integer>{}
	
	private class ComplexGenericClass2 implements ComplexGenericInterface<Long,Double>{}
}
