package org.openmrs.module.indiemroauthprovider.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openmrs.BaseOpenmrsData;

@Entity
@Table(name = "indiemr_resource_event_mapping")
public class ResourceEventMapping extends BaseOpenmrsData {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "external_event_id", nullable = false)
	private ExternalEvent externalEvent;
	
	@Column(name = "internal_resource_type", nullable = false, length = 64)
	private String internalResourceType;
	
	@Column(name = "internal_resource_uuid", nullable = false, length = 64)
	private String internalResourceUuid;
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public ExternalEvent getExternalEvent() {
		return externalEvent;
	}
	
	public void setExternalEvent(ExternalEvent externalEvent) {
		this.externalEvent = externalEvent;
	}
	
	public String getInternalResourceType() {
		return internalResourceType;
	}
	
	public void setInternalResourceType(String internalResourceType) {
		this.internalResourceType = internalResourceType;
	}
	
	public String getInternalResourceUuid() {
		return internalResourceUuid;
	}
	
	public void setInternalResourceUuid(String internalResourceUuid) {
		this.internalResourceUuid = internalResourceUuid;
	}
}
