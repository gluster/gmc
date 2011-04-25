/**
 * 
 */
package com.gluster.storage.management.core.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.gluster.storage.management.core.model.LogMessage;
import com.gluster.storage.management.core.model.Status;

@XmlRootElement(name = "response")
public class LogMessageListResponse extends AbstractResponse {
	private List<LogMessage> logMessages = new ArrayList<LogMessage>();
	
	public LogMessageListResponse() {
	}
	
	public LogMessageListResponse(Status status, List<LogMessage> logMessages) {
		setStatus(status);
		setLogMessages(logMessages);
	}

	@XmlElementWrapper(name = "logMessages")
	@XmlElement(name = "logMessage", type = LogMessage.class)
	public List<LogMessage> getLogMessages() {
		return logMessages;
	}

	public void setLogMessages(List<LogMessage> logMessages) {
		this.logMessages = logMessages;
	}

	@Override
	public Object getData() {
		return getLogMessages();
	}
}
