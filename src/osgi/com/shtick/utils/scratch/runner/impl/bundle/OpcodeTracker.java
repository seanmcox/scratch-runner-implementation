/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.bundle;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import com.shtick.utils.scratch.runner.core.Opcode;
import com.shtick.utils.scratch.runner.core.OpcodeRegistry;

/**
 * @author sean.cox
 *
 */
public class OpcodeTracker implements ServiceListener{
	private BundleContext bundleContext;

	/**
	 * @param bundleContext
	 */
	public OpcodeTracker(BundleContext bundleContext) {
		super();
		this.bundleContext = bundleContext;
		try{
			synchronized(bundleContext){
				bundleContext.addServiceListener(this, "(objectClass=com.shtick.uitls.scratch.runner.core.Opcode)");
				ServiceReference<?>[] references=bundleContext.getServiceReferences(Opcode.class.getName(), null);
				if(references!=null){
					for(ServiceReference<?> ref:references){
						try{
							OpcodeRegistry.getOpcodeRegistry().registerOpcode((Opcode)bundleContext.getService(ref));
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
				OpcodeRegistry.getOpcodeRegistry().registerOpcode((Opcode)bundleContext.getService(ref));
			}
			else if(event.getType() == ServiceEvent.UNREGISTERING){
				ServiceReference<?> ref=event.getServiceReference();
				OpcodeRegistry.getOpcodeRegistry().unregisterOpcode((Opcode)bundleContext.getService(ref));
			}
		}
	}
}
