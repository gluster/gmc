package com.gluster.storage.management.core.model;

/**
 * Default listener - doesn't do anything. Sub-class and override the method for
 * the event you want to handle.
 */
public class DefaultClusterListener implements IClusterListener {

	@Override
	public void serverAdded(GlusterServer server) {
	}

	@Override
	public void serverRemoved(GlusterServer server) {
	}

	@Override
	public void serverChanged(GlusterServer server, Event event) {
	}

	@Override
	public void volumeAdded(Volume volume) {
	}

	@Override
	public void volumeRemoved(Volume volume) {
	}

	@Override
	public void volumeChanged(Volume volume, Event event) {
	}
}
