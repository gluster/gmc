/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Gateway.
 *
 * Gluster Management Gateway is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Gateway is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
/**
 * 
 */
package org.gluster.storage.management.gateway.filters;

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
