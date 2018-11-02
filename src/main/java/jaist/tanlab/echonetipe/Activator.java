package jaist.tanlab.echonetipe;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.core.service.CseService;
import org.eclipse.om2m.interworking.service.InterworkingService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import jaist.tanlab.echonetipe.controllers.RequestRouter;
import jaist.tanlab.echonetipe.controllers.TaskManager;


public class Activator implements BundleActivator{
	private static Log logger = LogFactory.getLog(Activator.class);
	private ServiceTracker<Object, Object> cseServiceTracker;
	public static BundleContext context;
	
	@Override
	public void start(BundleContext context) throws Exception {
		logger.info("----------Starting ECHONET Lite - oneM2M Gateway-----------");
		Activator.context = context;
		logger.info("Registering IPE services...");
		context.registerService(InterworkingService.class.getName(), new RequestRouter(), null);
		logger.info("---IPE Service Registered---");

        cseServiceTracker = new ServiceTracker<Object, Object>(context, CseService.class.getName(), null) {
            public void removedService(ServiceReference<Object> reference, Object service) {
                logger.info("CseService removed");
            }

            public Object addingService(ServiceReference<Object> reference) {
                logger.info("CseService discovered");
                CseService cseService = (CseService) this.context.getService(reference);
                TaskManager.setCse(cseService);
                new Thread(){
                    public void run(){
                        try {
                        	TaskManager.start();
                        } catch (Exception e) {
                            logger.error("Can not start IPE service", e);
                        }
                    }
                }.start();
                return cseService;
            }
        };
        cseServiceTracker.open();
    }

	@Override
	public void stop(BundleContext context) throws Exception {
		logger.info("Stop ECHONET Lite oneM2M Gateway");
        try {
        	TaskManager.stop();
        } catch (Exception e) {
            logger.error("Stop IPE service error", e);
        }
		
	}

}
