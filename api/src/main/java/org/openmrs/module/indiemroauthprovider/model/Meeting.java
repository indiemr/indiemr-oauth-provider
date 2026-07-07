package org.openmrs.module.indiemroauthprovider.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openmrs.BaseChangeableOpenmrsData;

@Entity
@Table(name = "indiemr_meeting")
public class Meeting extends BaseChangeableOpenmrsData {
	
	public static final String STATUS_CREATED = "created";
	
	public static final String STATUS_SENT = "sent";
	
	public static final String STATUS_JOINED = "joined";
	
	public static final String STATUS_EXPIRED = "expired";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "external_event_id", nullable = false)
	private ExternalEvent externalEvent;
	
	@Column(nullable = false, unique = true, length = 64)
	private String token;
	
	@Column(name = "meeting_url", nullable = false, length = 2048)
	private String meetingUrl;
	
	@Column(nullable = false, length = 20)
	private String status = STATUS_CREATED;
	
	@Column(name = "expires_at", nullable = false)
	private Date expiresAt;
	
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
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getMeetingUrl() {
		return meetingUrl;
	}
	
	public void setMeetingUrl(String meetingUrl) {
		this.meetingUrl = meetingUrl;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public Date getExpiresAt() {
		return expiresAt;
	}
	
	public void setExpiresAt(Date expiresAt) {
		this.expiresAt = expiresAt;
	}
}
