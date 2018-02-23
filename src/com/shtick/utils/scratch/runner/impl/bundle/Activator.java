package com.shtick.utils.scratch.runner.impl.bundle;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.shtick.utils.scratch.runner.core.ScratchRuntime;
import com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation;

/**
 **/
public class Activator implements BundleActivator {
	private ServiceRegistration<?> runtimeRegistration;
	/**
	 * A source for registered Opcodes.
	 */
	public static OpcodeTracker OPCODE_TRACKER;
	/**
	 * A source for registered GraphicEffects
	 */
	public static GraphicEffectTracker GRAPHIC_EFFECT_TRACKER;
	/**
	 * A source for registered GraphicEffects
	 */
	public static StageMonitorCommandTracker STAGE_MONITOR_COMMAND_TRACKER;
	
    /**
     * Implements BundleActivator.start(). Prints
     * a message and adds itself to the bundle context as a service
     * listener.
     * @param context the framework context for the bundle.
     **/
    @Override
	public void start(BundleContext context){
		System.out.println(this.getClass().getCanonicalName()+": Starting.");
		OPCODE_TRACKER = new OpcodeTracker(context);
		GRAPHIC_EFFECT_TRACKER = new GraphicEffectTracker(context);
		STAGE_MONITOR_COMMAND_TRACKER = new StageMonitorCommandTracker(context);
		runtimeRegistration=context.registerService(ScratchRuntime.class.getName(), new ScratchRuntimeImplementation(),new Hashtable<String, String>());
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
		context.removeServiceListener(OPCODE_TRACKER);
		OPCODE_TRACKER = null;
		context.removeServiceListener(GRAPHIC_EFFECT_TRACKER);
		GRAPHIC_EFFECT_TRACKER = null;
		context.removeServiceListener(STAGE_MONITOR_COMMAND_TRACKER);
		STAGE_MONITOR_COMMAND_TRACKER = null;
		if(runtimeRegistration!=null)
			runtimeRegistration.unregister();
    }

}