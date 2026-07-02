package org.openmrs.module.indiemroauthprovider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;

public class IndiemrOauthProviderActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void started() {
		log.info("Started IndiEMR OAuth Provider Module");
	}
	
	@Override
	public void stopped() {
		log.info("Stopped IndiEMR OAuth Provider Module");
	}
}
