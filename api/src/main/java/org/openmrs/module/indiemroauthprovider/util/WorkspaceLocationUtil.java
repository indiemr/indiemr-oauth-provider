package org.openmrs.module.indiemroauthprovider.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;

/**
 * Session location -> workspace root. A workspace is an OpenMRS Location tree whose top has no
 * parent; sessions legitimately sit at either level (bare-username root, or a named clinic child),
 * so the jump has to be a walk rather than a lookup.
 * <p>
 * Deliberately NOT driven by the {@code indi_workspace} location tag: measured on the prod dump
 * (2026-08-24) 21 of the login-location children have an untagged parent, so tag coverage cannot be
 * trusted. Parent-chain walk it is.
 * <p>
 * Each parent is re-fetched by uuid — copied from {@code AppointmentReminderLocationUtil} — because
 * the parent hanging off a detached Location is a lazy proxy that blows up outside its session. The
 * depth guard is insurance against future data with a cycle; the tree measures exactly 2 levels
 * today.
 */
public final class WorkspaceLocationUtil {
	
	private static final int MAX_DEPTH = 10;
	
	private static final Log log = LogFactory.getLog(WorkspaceLocationUtil.class);
	
	private WorkspaceLocationUtil() {
	}
	
	/**
	 * @return the location set on the caller's session
	 * @throws IllegalArgumentException when the session carries no location (the known
	 *             empty-sessionLocation login bug produces exactly this) — callers surface it as a
	 *             clean 400 rather than a 500
	 */
	public static Location requireSessionLocation() {
		Location sessionLocation = Context.getUserContext() != null ? Context.getUserContext().getLocation() : null;
		if (sessionLocation == null) {
			throw new IllegalArgumentException("No session location — log in against a clinic location and retry");
		}
		return sessionLocation;
	}
	
	public static Location resolveWorkspaceRoot(Location location) {
		if (location == null) {
			throw new IllegalArgumentException("Location is required to resolve a workspace root");
		}
		LocationService locationService = Context.getLocationService();
		Location current = locationService.getLocationByUuid(location.getUuid());
		if (current == null) {
			throw new IllegalArgumentException("Unknown location: " + location.getUuid());
		}
		
		int depth = 0;
		while (current.getParentLocation() != null) {
			if (++depth > MAX_DEPTH) {
				log.warn("Location parent chain exceeded " + MAX_DEPTH + " hops from uuid=" + location.getUuid()
				        + " — stopping at '" + current.getName() + "'");
				break;
			}
			Location parent = locationService.getLocationByUuid(current.getParentLocation().getUuid());
			if (parent == null) {
				break;
			}
			current = parent;
		}
		return current;
	}
	
	/** Convenience for the endpoints: the caller's own workspace root, never a client-supplied one. */
	public static Location currentWorkspaceRoot() {
		return resolveWorkspaceRoot(requireSessionLocation());
	}
}
