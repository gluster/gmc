package com.gluster.storage.management.core.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.gluster.storage.management.core.utils.ProcessResult;

@XmlRootElement(name="status")
public class Status {
	@XmlElement(name="code", type=Integer.class)
	private Integer code;
	
	private String message;

	public Status() {
	}

	public boolean isSuccess() {
		return code == 0;
	}

	public Status(Integer executionStatus, String xmlData) {
		this.code = executionStatus;
	}

	public Status(ProcessResult result) {
		this.code = result.getExitValue();
	}

	public String getCode() {
		return code.toString();
	}

	public void setCode(Integer executionStatus) {
		this.code = executionStatus;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}