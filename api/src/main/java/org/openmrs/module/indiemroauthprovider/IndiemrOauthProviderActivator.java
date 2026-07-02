package org.openmrs.module.indiemroauthprovider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;

public class IndiemrOauthProviderActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void started() {
		log.info("Started IndiEMR OAuth Provider Module");
		ensureGlobalProperty(ModuleConstants.GP_PUBLIC_BASE_URL, "");
		ensureGlobalProperty(ModuleConstants.GP_ENC_KEY, "");
		ensureGlobalProperty(ModuleConstants.GP_GOOGLE_CLIENT_ID, "");
		ensureGlobalProperty(ModuleConstants.GP_GOOGLE_CLIENT_SECRET, "");
		ensureGlobalProperty(ModuleConstants.GP_GOOGLE_REDIRECT_URI, "");
	}
	
	@Override
	public void stopped() {
		log.info("Stopped IndiEMR OAuth Provider Module");
	}
	
	private void ensureGlobalProperty(String property, String defaultValue) {
		if (Context.getAdministrationService().getGlobalProperty(property) == null) {
			Context.getAdministrationService().setGlobalProperty(property, defaultValue);
		}
	}
}
