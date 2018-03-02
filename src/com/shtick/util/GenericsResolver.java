/**
 * 
 */
package com.shtick.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sean.cox
 *
 */
public class GenericsResolver {

	/**
	 * Get the actual type arguments a child class has used to extend a generic
	 * base class.
	 * 
	 * One weakness to this method is that it is tricky to pair up types with their
	 * corresponding parameters. It would be nice to return an explicit mapping.
	 * This weakness would affect base classes with more than one generic defined.
	 * For the time being, it seems that the returned list contains types in the
	 * same order as the parameter declaration. At least one path to obtain these
	 * values (ie. using getTypeParameters()) guarantees this ordering, but the
	 * other (ie. using getActualTypeArguments()) is contractually ambiguous.
	 * 
	 * TODO Refactor this to return an explicit mapping or guarantee return value ordering.
	 *
	 * @param baseClass
	 *            the base class
	 * @param childClass
	 *            the child class
	 * @return a list of the raw classes for the actual type arguments.
	 */
	public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass, Class<? extends T> childClass) {
		Map<Type, Type> resolvedTypes = new HashMap<>();
		Type type = childClass;
		Type nextType=null;
		Class<?> c;
		// start walking up the inheritance hierarchy until we hit baseClass
		
		c=getClass(type);
		while ((!c.equals(baseClass))&&(baseClass.isAssignableFrom(c))) {
			if (type instanceof Class) {
				// there is no useful information for us in raw types, so just
				// keep going.
				nextType = ((Class<?>) type).getGenericSuperclass();
			}
			else {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Class<?> rawType = (Class<?>) parameterizedType.getRawType();

				Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
				TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
				for (int i = 0; i < actualTypeArguments.length; i++)
					resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);

				if (!rawType.equals(baseClass))
					nextType = rawType.getGenericSuperclass();
			}
			if(baseClass.isInterface()){
				Type[] interfaces;
				if (type instanceof Class)
					interfaces = ((Class<?>) type).getGenericInterfaces();
				else
					interfaces = ((Class<?>)((ParameterizedType) type).getRawType()).getGenericInterfaces();
				type=nextType;
				for(Type interf:interfaces){
					if(baseClass.isAssignableFrom(getClass(interf))){
						type=interf;
						break;
					}
				}
			}
			c=getClass(type);
		}

		// finally, for each actual type argument provided to baseClass,
		// determine (if possible)
		// the raw class for that type argument.
		Type[] actualTypeArguments;
		if (type instanceof Class) 
			actualTypeArguments = ((Class<?>)type).getTypeParameters();
		else
			actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
		List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
		// resolve types by chasing down type variables.
		for (Type baseType : actualTypeArguments) {
			while (resolvedTypes.containsKey(baseType))
				baseType = resolvedTypes.get(baseType);
			typeArgumentsAsClasses.add(getClass(baseType));
		}
		return typeArgumentsAsClasses;
	}

	/**
	 * Get the underlying class for a type, or null if the type is a variable
	 * type.
	 * 
	 * @param type
	 *            the type
	 * @return the underlying class
	 */
	public static Class<?> getClass(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		}
		else if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		}
		else if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			Class<?> componentClass = getClass(componentType);
			if (componentClass != null)
				return Array.newInstance(componentClass, 0).getClass();
		}
		return null;
	}
}
