/**
 * 
 */
package com.gluster.storage.management.core.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.gluster.storage.management.core.model.VolumeLogMessage;
import com.gluster.storage.management.core.model.Status;

@XmlRootElement(name = "response")
public class LogMessageListResponse extends AbstractResponse {
	private List<VolumeLogMessage> logMessages = new ArrayList<VolumeLogMessage>();
	
	public LogMessageListResponse() {
	}
	
	public LogMessageListResponse(Status status, List<VolumeLogMessage> logMessages) {
		setStatus(status);
		setLogMessages(logMessages);
	}

	@XmlElementWrapper(name = "logMessages")
	@XmlElement(name = "logMessage", type = VolumeLogMessage.class)
	public List<VolumeLogMessage> getLogMessages() {
		return logMessages;
	}

	public void setLogMessages(List<VolumeLogMessage> logMessages) {
		this.logMessages = logMessages;
	}

	@Override
	public Object getData() {
		return getLogMessages();
	}
}
