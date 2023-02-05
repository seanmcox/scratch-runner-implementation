package com.shtick.utils.scratch.runner.impl.bundle;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.shtick.utils.scratch.runner.impl.ScratchRuntimeFactoryImplementation;

/**
 **/
public class Activator implements BundleActivator {
	private ServiceRegistration<?> runtimeRegistration;
	/**
	 * A source for registered Opcodes.
	 */
	private static FeatureSetGeneratorTracker FEATURE_SET_GENERATOR_TRACKER;
	
    /**
     * Implements BundleActivator.start(). Prints
     * a message and adds itself to the bundle context as a service
     * listener.
     * @param context the framework context for the bundle.
     **/
    @Override
	public void start(BundleContext context){
		System.out.println(this.getClass().getCanonicalName()+": Starting.");
		FEATURE_SET_GENERATOR_TRACKER = new FeatureSetGeneratorTracker(context);
		runtimeRegistration=context.registerService(com.shtick.utils.scratch.runner.core.ScratchRuntimeFactory.class.getName(), new ScratchRuntimeFactoryImplementation(),new Hashtable<String, String>());
    }

    /**
     * Implements BundleActivator.stop(). Prints
     * a message and removes itself from the bundle context as a
     * service listener.
     * @param context the framework context for the bundle.
     **/
    @Override
	public void stop(BundleContext context){
		System.out.println(this.getClass().getCanonicalName()+": Stopping.");
		context.removeServiceListener(FEATURE_SET_GENERATOR_TRACKER);
		FEATURE_SET_GENERATOR_TRACKER = null;
		if(runtimeRegistration!=null)
			runtimeRegistration.unregister();
    }

}