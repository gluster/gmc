/**
 * 
 */
package org.gluster.storage.management.core.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.gluster.storage.management.core.model.VolumeLogMessage;


@XmlRootElement(name = "logMessages")
public class LogMessageListResponse {
	private List<VolumeLogMessage> logMessages = new ArrayList<VolumeLogMessage>();
	
	public LogMessageListResponse() {
	}
	
	public LogMessageListResponse(List<VolumeLogMessage> logMessages) {
		setLogMessages(logMessages);
	}
	
	@XmlElement(name = "logMessage", type = VolumeLogMessage.class)
	public List<VolumeLogMessage> getLogMessages() {
		return logMessages;
	}

	public void setLogMessages(List<VolumeLogMessage> logMessages) {
		this.logMessages = logMessages;
	}
}
