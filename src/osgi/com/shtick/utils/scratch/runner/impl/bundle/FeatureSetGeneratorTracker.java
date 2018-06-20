/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.bundle;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import com.shtick.utils.scratch.runner.core.FeatureLibrary;
import com.shtick.utils.scratch.runner.core.FeatureSetGenerator;

/**
 * @author sean.cox
 *
 */
public class FeatureSetGeneratorTracker implements ServiceListener{
	private BundleContext bundleContext;

	/**
	 * @param bundleContext
	 */
	public FeatureSetGeneratorTracker(BundleContext bundleContext) {
		super();
		this.bundleContext = bundleContext;
		try{
			synchronized(bundleContext){
				bundleContext.addServiceListener(this, "(objectClass=com.shtick.utils.scratch.runner.core.FeatureSetGenerator)");
				ServiceReference<?>[] references=bundleContext.getServiceReferences(FeatureSetGenerator.class.getName(), null);
				if(references!=null){
					for(ServiceReference<?> ref:references){
						try{
							FeatureSetGenerator generator = (FeatureSetGenerator)bundleContext.getService(ref);
							FeatureLibrary.registerFeatureSetGenerator(generator);
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
		synchronized(bundleContext){
			if(event.getType() == ServiceEvent.REGISTERED){
				ServiceReference<?> ref=event.getServiceReference();
				FeatureSetGenerator generator = (FeatureSetGenerator)bundleContext.getService(ref);
				FeatureLibrary.registerFeatureSetGenerator(generator);
			}
			else if(event.getType() == ServiceEvent.UNREGISTERING){
				ServiceReference<?> ref=event.getServiceReference();
				FeatureSetGenerator generator = (FeatureSetGenerator)bundleContext.getService(ref);
				FeatureLibrary.unregisterFeatureSetGenerator(generator);
			}
		}
	}
}
