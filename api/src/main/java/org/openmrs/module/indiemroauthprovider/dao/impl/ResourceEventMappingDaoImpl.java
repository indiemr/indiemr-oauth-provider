package org.openmrs.module.indiemroauthprovider.dao.impl;

import java.util.List;

import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.indiemroauthprovider.dao.ResourceEventMappingDao;
import org.openmrs.module.indiemroauthprovider.model.ExternalEvent;
import org.openmrs.module.indiemroauthprovider.model.ResourceEventMapping;
import org.openmrs.module.indiemroauthprovider.util.OpenmrsDataUtils;

public class ResourceEventMappingDaoImpl implements ResourceEventMappingDao {
	
	private static final String MAPPING_FETCH = "from ResourceEventMapping m "
	        + "join fetch m.externalEvent e join fetch e.oauthAccount a join fetch a.oauthProvider p ";
	
	private DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public ResourceEventMapping save(ResourceEventMapping mapping) {
		OpenmrsDataUtils.setCreated(mapping);
		sessionFactory.getCurrentSession().saveOrUpdate(mapping);
		return mapping;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<ResourceEventMapping> findByInternalResource(String internalResourceType, String internalResourceUuid) {
		return sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            MAPPING_FETCH + "where m.internalResourceType = :type and m.internalResourceUuid = :uuid "
		                    + "and m.voided = false and e.voided = false").setParameter("type", internalResourceType)
		        .setParameter("uuid", internalResourceUuid).list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<ResourceEventMapping> findByProviderAndInternalResource(String providerUuid, String internalResourceType,
	        String internalResourceUuid) {
		return sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            MAPPING_FETCH + "where a.provider.uuid = :providerUuid and m.internalResourceType = :type "
		                    + "and m.internalResourceUuid = :uuid and m.voided = false and e.voided = false")
		        .setParameter("providerUuid", providerUuid).setParameter("type", internalResourceType)
		        .setParameter("uuid", internalResourceUuid).list();
	}
	
	@Override
	public ExternalEvent findActiveEventByProviderAndInternalResource(String providerUuid, String internalResourceType,
	        String internalResourceUuid, String externalResourceType) {
		List<ResourceEventMapping> mappings = findByProviderAndInternalResource(providerUuid, internalResourceType,
		    internalResourceUuid);
		for (ResourceEventMapping mapping : mappings) {
			ExternalEvent event = mapping.getExternalEvent();
			if (externalResourceType.equals(event.getExternalResourceType())) {
				return event;
			}
		}
		return null;
	}
	
	@Override
	public void voidByInternalResource(String internalResourceType, String internalResourceUuid, String reason) {
		voidMappings(findByInternalResource(internalResourceType, internalResourceUuid), reason);
	}
	
	@Override
	public void voidByProviderAndInternalResource(String providerUuid, String internalResourceType,
	        String internalResourceUuid, String reason) {
		voidMappings(findByProviderAndInternalResource(providerUuid, internalResourceType, internalResourceUuid), reason);
	}
	
	private void voidMappings(List<ResourceEventMapping> mappings, String reason) {
		for (ResourceEventMapping mapping : mappings) {
			OpenmrsDataUtils.voidWithReason(mapping, reason);
			sessionFactory.getCurrentSession().merge(mapping);
		}
	}
}
