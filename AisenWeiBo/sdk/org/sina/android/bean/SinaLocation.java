package org.sina.android.bean;

import java.io.Serializable;

public class SinaLocation implements Serializable {

	private static final long serialVersionUID = 1910597414549241983L;

	private Double longitude;

	private Double latitude;

	private Integer accuracy;

	private Integer altitude;

	private Integer altitude_accuracy;

	private SinaAddress address;

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Integer getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(Integer accuracy) {
		this.accuracy = accuracy;
	}

	public Integer getAltitude() {
		return altitude;
	}

	public void setAltitude(Integer altitude) {
		this.altitude = altitude;
	}

	public SinaAddress getAddress() {
		return address;
	}

	public void setAddress(SinaAddress address) {
		this.address = address;
	}

	public Integer getAltitude_accuracy() {
		return altitude_accuracy;
	}

	public void setAltitude_accuracy(Integer altitude_accuracy) {
		this.altitude_accuracy = altitude_accuracy;
	}

}
