package com.gluster.storage.management.core.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.gluster.storage.management.core.model.RunningTask;
import com.gluster.storage.management.core.model.Status;

@XmlRootElement(name = "response")
public class RunningTaskListResponse extends AbstractResponse {
	private List<RunningTask> runningTasks = new ArrayList<RunningTask>();
	
	public RunningTaskListResponse() {
		
	}
	
	
	public RunningTaskListResponse(Status status, List<RunningTask> runningTasks) {
		setStatus(status);
		setRunningTasks(runningTasks);
	}
	
	@XmlElementWrapper(name = "runningTasks")
	@XmlElement(name = "runningTask", type=RunningTask.class)
	public List<RunningTask> getRunningTasks() {
		return runningTasks;
	}

	/**
	 * @param runningTasks
	 *            the runningTasks to set
	 */
	public void setRunningTasks(List<RunningTask> runningTasks) {
		this.runningTasks = runningTasks;
	}

	@Override
	public Object getData() {
		return getRunningTasks();
	}

}
