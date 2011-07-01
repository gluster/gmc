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
package com.gluster.storage.management.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement
public class VolumeOptions {
	private Map<String, String> optionsMap = new HashMap<String, String>();

	public VolumeOptions() {
	}
	
	public String get(String key) {
		return optionsMap.get(key);
	}
	
	public String put(String key, String value) {
		return optionsMap.put(key, value);
	}

	@XmlElement(name="option", type=VolumeOption.class)
	public List<VolumeOption> getOptions() {
		List<VolumeOption> options = new ArrayList<VolumeOption>();
		for(Entry<String, String> entry : optionsMap.entrySet()) {
			options.add(new VolumeOption(entry.getKey(), entry.getValue()));
		}
		return options;
	}
	
	public Set<Entry<String, String>> getOptionsMap() {
		return optionsMap.entrySet();
	}
	
	public void setOptionsMap(Map<String, String> optionsMap) {
		this.optionsMap = optionsMap;
	}
	
	public void clear() {
		optionsMap.clear();
	}

	public Set<Entry<String, String>> entrySet() {
		return optionsMap.entrySet();
	}

	public Set<String> keySet() {
		return optionsMap.keySet();
	}

	public String remove(String key) {
		return optionsMap.remove(key);
	}

	public int size() {
		return optionsMap.size();
	}

	public boolean containsKey(String key) {
		return optionsMap.containsKey(key);
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
		for(Entry<String, String> entry : options.entrySet()) {
			optionsMap.put(entry.getKey(), entry.getValue());
		}
	}
}