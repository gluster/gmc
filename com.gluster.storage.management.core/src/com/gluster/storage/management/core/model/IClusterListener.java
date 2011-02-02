package com.gluster.storage.management.core.model;

/**
 * Interface for a cluster listener. Every registered listener will be notified
 * on various events happening on the cluster.
 */
public interface IClusterListener {
	public void serverAdded(GlusterServer server);

	public void serverRemoved(GlusterServer server);

	public void serverChanged(GlusterServer server, Event event);

	public void volumeAdded(Volume volume);

	public void volumeRemoved(Volume volume);

	public void volumeChanged(Volume volume, Event event);
}
