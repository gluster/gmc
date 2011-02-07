package com.gluster.storage.management.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.core.runtime.PlatformObject;

import com.gluster.storage.management.core.utils.StringUtils;

@XmlRootElement
public class Entity extends PlatformObject implements Filterable {
	private static final long serialVersionUID = 1L;

	protected String name;
	protected List<Entity> children = new ArrayList<Entity>();
	private Entity parent;

	public Entity() {
		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Entity getParent() {
		return parent;
	}

	public void setParent(Entity parent) {
		this.parent = parent;
	}

	public List<? extends Entity> getChildren() {
		return children;
	}

	public void setChildren(List<Entity> children) {
		this.children = children;
	}

	public Entity(String name, Entity parent) {
		this.name = name;
		this.parent = parent;
	}

	public Entity(String name, Entity parent, List<Entity> children) {
		this.name = name;
		this.parent = parent;
		this.children = children;
	}
	
	@Override
	public boolean filter(String filterString, boolean caseSensitive) {
		return StringUtils.filterString(getName(), filterString, caseSensitive);
	}
}
