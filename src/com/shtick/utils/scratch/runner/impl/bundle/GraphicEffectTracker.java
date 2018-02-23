/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.bundle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import com.shtick.utils.scratch.runner.core.GraphicEffect;
import com.shtick.utils.scratch.runner.core.Opcode;

/**
 * @author sean.cox
 *
 */
public class GraphicEffectTracker implements ServiceListener{
	private HashMap<String,ServiceReference<GraphicEffect>> effects=new HashMap<>();
	private BundleContext bundleContext;

	/**
	 * @param bundleContext
	 */
	public GraphicEffectTracker(BundleContext bundleContext) {
		super();
		this.bundleContext = bundleContext;
		try{
			synchronized(effects){
				bundleContext.addServiceListener(this, "(objectClass=com.shtick.uitls.scratch.runner.core.GraphicEffect)");
				ServiceReference<?>[] references=bundleContext.getServiceReferences(GraphicEffect.class.getName(), null);
				if(references!=null){
					for(ServiceReference<?> ref:references){
						try{
							registerGraphicEffect(ref);
						}
						catch(AbstractMethodError t){
							Object service=bundleContext.getService(ref);
							System.err.println(service.getClass().getCanonicalName());
							System.err.flush();
							t.printStackTrace();
						}
					}
				}
			}
		}
		catch(InvalidSyntaxException t){
			throw new RuntimeException(t);
		}
	}
	
	/**
	 * 
	 * @param effectID
	 * @return The GraphicEffect with the given id.
	 */
	public GraphicEffect getGraphicEffect(String effectID){
		ServiceReference<?> reference = effects.get(effectID);
		if(reference==null)
			return null;
		return (GraphicEffect)bundleContext.getService(reference);
	}
	
	/**
	 * 
	 * @return A set of already registered GraphicEffects.
	 */
	public Set<GraphicEffect> getGraphicEffects(){
		HashSet<GraphicEffect> retval=new HashSet<>();
		synchronized(effects){
			GraphicEffect service;
			for(ServiceReference<?> reference:effects.values()){
				service=(GraphicEffect)bundleContext.getService(reference);
				retval.add(service);
			}
		}
		return retval;
	}
	
	/**
	 * The caller of this method should be synchronized on the appFactoryServices object.
	 * 
	 * @param ref
	 * @throws AbstractMethodError If the AppFactoryService is not compatible with this implementation of the AppTracker sufficient to be registered.
	 */
	private void registerGraphicEffect(ServiceReference<?> ref) throws AbstractMethodError{
		Object service=bundleContext.getService(ref);
		if(!(service instanceof GraphicEffect))
			return;
		GraphicEffect effect=(GraphicEffect)service;
		if(!effects.containsKey(effect.getName()))
			effects.put(effect.getName(),(ServiceReference<GraphicEffect>)ref);
	}

	/**
	 * The caller of this method should be synchronized on the appFactoryServices object.
	 * 
	 * @param ref
	 */
	private void unregisterGraphicEffect(ServiceReference<?> ref){
		Object service=bundleContext.getService(ref);
		if(!(service instanceof Opcode))
			return;
		GraphicEffect effect=(GraphicEffect)service;
		effects.remove(effect.getName());
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		synchronized(effects){
			if(event.getType() == ServiceEvent.REGISTERED){
				ServiceReference<?> ref=event.getServiceReference();
				registerGraphicEffect(ref);
			}
			else if(event.getType() == ServiceEvent.UNREGISTERING){
				ServiceReference<?> ref=event.getServiceReference();
				unregisterGraphicEffect(ref);
			}
		}
	}
}
