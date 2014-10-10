package org.sina.android.bean;

import java.io.Serializable;

/**
 * 指定用户所在分组的信息
 * 
 * @author wangdan
 * 
 */
public class GroupListed implements Serializable {

	private static final long serialVersionUID = -8507187317594802589L;

	private String id;

	private String idstr;

	private String name;

	private String mode;

	private String visible;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIdstr() {
		return idstr;
	}

	public void setIdstr(String idstr) {
		this.idstr = idstr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getVisible() {
		return visible;
	}

	public void setVisible(String visible) {
		this.visible = visible;
	}

}
