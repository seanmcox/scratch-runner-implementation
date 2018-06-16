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

import com.shtick.utils.scratch.runner.core.StageMonitorCommand;
import com.shtick.utils.scratch.runner.core.StageMonitorCommandRegistry;

/**
 * @author sean.cox
 *
 */
public class StageMonitorCommandTracker implements ServiceListener{
	private HashMap<String,ServiceReference<StageMonitorCommand>> commands=new HashMap<>();
	private BundleContext bundleContext;

	/**
	 * @param bundleContext
	 */
	public StageMonitorCommandTracker(BundleContext bundleContext) {
		super();
		this.bundleContext = bundleContext;
		try{
			synchronized(commands){
				bundleContext.addServiceListener(this, "(objectClass=com.shtick.uitls.scratch.runner.core.StageMonitorCommand)");
				ServiceReference<?>[] references=bundleContext.getServiceReferences(StageMonitorCommand.class.getName(), null);
				if(references!=null){
					for(ServiceReference<?> ref:references){
						try{
							StageMonitorCommandRegistry.getStageMonitorCommandRegistry().registerStageMonitorCommand((StageMonitorCommand)bundleContext.getService(ref));
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
		synchronized(commands){
			if(event.getType() == ServiceEvent.REGISTERED){
				ServiceReference<?> ref=event.getServiceReference();
				StageMonitorCommandRegistry.getStageMonitorCommandRegistry().registerStageMonitorCommand((StageMonitorCommand)bundleContext.getService(ref));
			}
			else if(event.getType() == ServiceEvent.UNREGISTERING){
				ServiceReference<?> ref=event.getServiceReference();
				StageMonitorCommandRegistry.getStageMonitorCommandRegistry().unregisterStageMonitorCommand((StageMonitorCommand)bundleContext.getService(ref));
			}
		}
	}
}
