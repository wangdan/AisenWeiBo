package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

public class MenuBean implements Serializable {

	private static final long serialVersionUID = 6761310516837802125L;

	private int iconRes;
	
	private int titleRes;// ac的标题
	
	private int menuTitleRes; // 菜单的标题
	
	private String type;

	public int getIconRes() {
		return iconRes;
	}

	public void setIconRes(int iconRes) {
		this.iconRes = iconRes;
	}

	public int getTitleRes() {
		return titleRes;
	}

	public void setTitleRes(int titleRes) {
		this.titleRes = titleRes;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getMenuTitleRes() {
		return menuTitleRes;
	}

	public void setMenuTitleRes(int menuTitleRes) {
		this.menuTitleRes = menuTitleRes;
	}
	
}
