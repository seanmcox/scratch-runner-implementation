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

import com.shtick.utils.scratch.runner.core.Opcode;

/**
 * @author sean.cox
 *
 */
public class OpcodeTracker implements ServiceListener{
	private HashMap<String,ServiceReference<Opcode>> opcodes=new HashMap<>();
	private BundleContext bundleContext;

	/**
	 * @param bundleContext
	 */
	public OpcodeTracker(BundleContext bundleContext) {
		super();
		this.bundleContext = bundleContext;
		try{
			synchronized(opcodes){
				bundleContext.addServiceListener(this, "(objectClass=com.shtick.uitls.scratch.runner.core.Opcode)");
				ServiceReference<?>[] references=bundleContext.getServiceReferences(Opcode.class.getName(), null);
				if(references!=null){
					for(ServiceReference<?> ref:references){
						try{
							registerOpcode(ref);
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
	 * @param opcodeID
	 * @return The Opcode with the given id.
	 */
	public Opcode getOpcode(String opcodeID){
		ServiceReference<?> reference = opcodes.get(opcodeID);
		if(reference==null)
			return null;
		return (Opcode)bundleContext.getService(reference);
	}
	
	/**
	 * 
	 * @return A set of already registered appFactories.
	 */
	public Set<Opcode> getOpcodes(){
		HashSet<Opcode> retval=new HashSet<>();
		synchronized(opcodes){
			Opcode service;
			for(ServiceReference<?> reference:opcodes.values()){
				service=(Opcode)bundleContext.getService(reference);
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
	private void registerOpcode(ServiceReference<?> ref) throws AbstractMethodError{
		Object service=bundleContext.getService(ref);
		if(!(service instanceof Opcode))
			return;
		Opcode opcode=(Opcode)service;
		if(!opcodes.containsKey(opcode.getOpcode()))
			opcodes.put(opcode.getOpcode(),(ServiceReference<Opcode>)ref);
	}

	/**
	 * The caller of this method should be synchronized on the appFactoryServices object.
	 * 
	 * @param ref
	 */
	private void unregisterOpcode(ServiceReference<?> ref){
		Object service=bundleContext.getService(ref);
		if(!(service instanceof Opcode))
			return;
		Opcode opcode=(Opcode)service;
		opcodes.remove(opcode.getOpcode());
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		synchronized(opcodes){
			if(event.getType() == ServiceEvent.REGISTERED){
				ServiceReference<?> ref=event.getServiceReference();
				registerOpcode(ref);
			}
			else if(event.getType() == ServiceEvent.UNREGISTERING){
				ServiceReference<?> ref=event.getServiceReference();
				unregisterOpcode(ref);
			}
		}
	}
}
