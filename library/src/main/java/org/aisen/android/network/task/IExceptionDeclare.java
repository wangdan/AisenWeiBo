package org.aisen.android.network.task;

public interface IExceptionDeclare {

	void checkResponse(String response) throws TaskException;

	String checkCode(String code);

}
