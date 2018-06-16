/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.bundle;

import java.util.HashMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import com.shtick.utils.scratch.runner.core.GraphicEffect;
import com.shtick.utils.scratch.runner.core.GraphicEffectRegistry;

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
							GraphicEffectRegistry.getGraphicEffectRegistry().registerGraphicEffect((GraphicEffect)bundleContext.getService(ref));
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

	@Override
	public void serviceChanged(ServiceEvent event) {
		synchronized(effects){
			if(event.getType() == ServiceEvent.REGISTERED){
				ServiceReference<?> ref=event.getServiceReference();
				GraphicEffectRegistry.getGraphicEffectRegistry().registerGraphicEffect((GraphicEffect)bundleContext.getService(ref));
			}
			else if(event.getType() == ServiceEvent.UNREGISTERING){
				ServiceReference<?> ref=event.getServiceReference();
				GraphicEffectRegistry.getGraphicEffectRegistry().unregisterGraphicEffect((GraphicEffect)bundleContext.getService(ref));
			}
		}
	}
}
