package org.aisen.android.network.task;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import android.os.Bundle;
import android.text.TextUtils;

import org.aisen.android.common.utils.Logger;

public class TaskManager implements ITaskManager {
	
	static final String TAG = "TaskManager";

	private LinkedHashMap<String, WeakReference<WorkTask>> taskCache;
	private HashMap<String, Integer> taskCountMap;

	public TaskManager() {
		taskCountMap = new HashMap<String, Integer>();
		taskCache = new LinkedHashMap<String, WeakReference<WorkTask>>();
	}

	@Override
	public void addTask(WorkTask task) {
		if (task != null && !TextUtils.isEmpty(task.getTaskId())) {
			int count = taskCountMap.keySet().contains(task.getTaskId()) ? taskCountMap.get(task.getTaskId()) : 0;
			taskCountMap.put(task.getTaskId(), ++count);
			cancelExistTask(task.getTaskId(), true);

			taskCache.put(task.getTaskId(), new WeakReference<WorkTask>(task));

			Logger.d(TAG, String.format("addTask() --->%s", task.getTaskId()));
		}
	}

	@Override
	public void removeTask(String taskId, boolean cancelIfRunning) {
		cancelExistTask(taskId, cancelIfRunning);
	}

	@Override
	public void removeAllTask(boolean mayInterruptIfRunning) {
		Set<String> keySet = taskCache.keySet();
		for (String key : keySet) {
			WorkTask task = getTaskById(key);
			if (task != null)
				task.cancel(mayInterruptIfRunning);
		}
		taskCache.clear();
	}

	private void cancelExistTask(String taskId, boolean mayInterruptIfRunning) {
		WorkTask existTask = getTaskById(taskId);

		if (existTask != null)
			Logger.d(TAG, String.format("interrupt exist task --->%s", taskId));

		if (existTask != null)
			existTask.cancel(mayInterruptIfRunning);

		taskCache.remove(taskId);
	}

	private WorkTask getTaskById(String taskId) {
		WeakReference<WorkTask> existTaskRef = taskCache.get(taskId);
		if (existTaskRef != null)
			return existTaskRef.get();
		return null;
	}

	@Override
	public int getTaskCount(String taskId) {
		if(TextUtils.isEmpty(taskId))
			return 0;
		
		return taskCountMap.keySet().contains(taskId) ? taskCountMap.get(taskId) : 0;
	}
	
	public void cleatTaskCount(String taskId) {
		if(!TextUtils.isEmpty(taskId))
			taskCountMap.remove(taskId);
	}
	
	public void save(Bundle outState) {
		outState.putSerializable("map", taskCountMap);
	}
	
	public void restore(Bundle savedInstanceState) {
		if (savedInstanceState.getSerializable("map") != null) {
			taskCountMap = (HashMap<String, Integer>) savedInstanceState.getSerializable("map");
		}
	}

}
