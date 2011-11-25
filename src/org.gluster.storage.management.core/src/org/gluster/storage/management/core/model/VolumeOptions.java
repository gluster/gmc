/*******************************************************************************
 * Copyright (c) 2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *  
 * Gluster Management Console is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement(name="options")
public class VolumeOptions {
	private List<VolumeOption> options = new ArrayList<VolumeOption>();

	public VolumeOptions() {
	}
	
	public String get(String key) {
		for(VolumeOption option : options) {
			if(option.getKey().equals(key)) {
				return option.getValue();
			}
		}
		return null;
	}
	
	public void put(String key, String value) {
		VolumeOption option = getOption(key);
		if(option != null) {
			option.setValue(value);
		} else {
			options.add(new VolumeOption(key, value));
		}
	}

	@XmlElement(name="option", type=VolumeOption.class)
	public List<VolumeOption> getOptions() {
		return options;
	}
	
	public void setOptions(List<VolumeOption> options) {
		this.options = options;
	}
	
	public void clear() {
		options.clear();
	}

	public boolean remove(String key) {
		return options.remove(getOption(key));
	}
	
	public VolumeOption getOption(String key) {
		for(VolumeOption option : options) {
			if(option.getKey().equals(key)) {
				return option;
			}
		}
		return null;
	}

	public int size() {
		return options.size();
	}

	public boolean containsKey(String key) {
		return get(key) != null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof VolumeOptions)) {
			return false;
		}
		
		VolumeOptions options = (VolumeOptions)obj;
		if(getOptions().size() != options.size()) {
			return false;
		}
		
		for(VolumeOption option : getOptions()) {
			if(!(option.getValue().equals(options.get(option.getKey())))) {
				return false;
			}
		}
		
		return true;
	}

	public void copyFrom(VolumeOptions options) {
		this.options.clear();
		this.options.addAll(options.getOptions());
	}
}