/**
 * 
 */
package com.gluster.storage.management.server.filters;

import java.util.ArrayList;
import java.util.List;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

/**
 * Gluster resource filter factory. As of now, this creates only one filter - the audit filter {@code AuditFilter}
 */
public class GlusterResourceFilterFactory implements ResourceFilterFactory {

	public GlusterResourceFilterFactory() {
	}
	
	/* (non-Javadoc)
	 * @see com.sun.jersey.spi.container.ResourceFilterFactory#create(com.sun.jersey.api.model.AbstractMethod)
	 */
	@Override
	public List<ResourceFilter> create(AbstractMethod arg0) {
		List<ResourceFilter> filters = new ArrayList<ResourceFilter>();
		filters.add(new AuditFilter());
		
		return filters;
	}
}
