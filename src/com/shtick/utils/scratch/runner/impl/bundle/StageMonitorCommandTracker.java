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
							registerCommand(ref);
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
	 * @param command
	 * @return The Command with the given id.
	 */
	public StageMonitorCommand getCommand(String command){
		ServiceReference<?> reference = commands.get(command);
		if(reference==null)
			return null;
		return (StageMonitorCommand)bundleContext.getService(reference);
	}
	
	/**
	 * 
	 * @return A set of already registered appFactories.
	 */
	public Set<StageMonitorCommand> getCommands(){
		HashSet<StageMonitorCommand> retval=new HashSet<>();
		synchronized(commands){
			StageMonitorCommand service;
			for(ServiceReference<?> reference:commands.values()){
				service=(StageMonitorCommand)bundleContext.getService(reference);
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
	private void registerCommand(ServiceReference<?> ref) throws AbstractMethodError{
		Object service=bundleContext.getService(ref);
		if(!(service instanceof StageMonitorCommand))
			return;
		StageMonitorCommand command=(StageMonitorCommand)service;
		if(!commands.containsKey(command.getCommand()))
			commands.put(command.getCommand(),(ServiceReference<StageMonitorCommand>)ref);
	}

	/**
	 * The caller of this method should be synchronized on the appFactoryServices object.
	 * 
	 * @param ref
	 */
	private void unregisterCommand(ServiceReference<?> ref){
		Object service=bundleContext.getService(ref);
		if(!(service instanceof StageMonitorCommand))
			return;
		StageMonitorCommand command=(StageMonitorCommand)service;
		commands.remove(command.getCommand());
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		synchronized(commands){
			if(event.getType() == ServiceEvent.REGISTERED){
				ServiceReference<?> ref=event.getServiceReference();
				registerCommand(ref);
			}
			else if(event.getType() == ServiceEvent.UNREGISTERING){
				ServiceReference<?> ref=event.getServiceReference();
				unregisterCommand(ref);
			}
		}
	}
}
