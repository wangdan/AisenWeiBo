package org.aisen.android.component.orm.extra;


public class Extra {

	private String owner;// 数据拥有者

	private String key;// 数据的key

	public Extra() {

	}
	
	public Extra(String owner, String key) {
		this.key = key;
		this.owner = owner;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
}
