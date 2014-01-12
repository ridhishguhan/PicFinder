package com.riddimon.pickpix.util;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.os.HandlerThread;

/**
 * Hosts a ThreadPoolExecutor to submit tasks to be run in the background.<br>
 * This class assumes that the caller does handles all aspects of <b>fairness</b> required
 * in the event that different types of tasks are submitted at different rates.
 * 
 * @author ridhishguhan
 */
public class TaskExecutor {
	static private final Logger logger = LoggerFactory.getLogger(TaskExecutor.class);
	// threads for every core present, slightly inflated due to possibility of 
	// hidden cores
	private static final int THREAD_CORE_RATIO = 2;
	private static final int MAX_THREAD_CORE_RATIO = 5;
	// keep unused threads available for 2 seconds
	private static final int KEEP_ALIVE_TIME = 2;

	/**
	 * Queries the VM for the number of available processors and returns the optimal thread pool size
	 * @return
	 */
	static public int threadPoolSize() {
		return Runtime.getRuntime().availableProcessors() * 2;
	}

	/**
	 * Queries the VM for the number of available processors and returns the optimal max-thread pool size
	 * @return
	 */
	static public int maxThreadPoolSize() {
		return Runtime.getRuntime().availableProcessors() * MAX_THREAD_CORE_RATIO;
	}

	// member-variables and instance methods go here

	private Context context;
	// TODO: use PriorityQueue instead of LinkedBlockingQueue
	// will make it easier to implement re-ordering of downloads
	private LinkedBlockingQueue<Runnable> taskQueue = null;

	private ThreadPoolExecutor workerFarm = null;
	private static TaskExecutor instance = null;

	private TaskExecutor(Context context) {
		this.context = context.getApplicationContext();
		taskQueue = new LinkedBlockingQueue<Runnable>();
		workerFarm = new ThreadPoolExecutor(threadPoolSize(), maxThreadPoolSize()
				, KEEP_ALIVE_TIME, TimeUnit.SECONDS, taskQueue);
		//workerFarm = new ThreadPoolExecutor(1, 1, KEEP_ALIVE_TIME
		//		, TimeUnit.SECONDS, taskQueue);
	}

	static private TaskExecutor getInstance(Context context) {
		if (instance == null) {
			instance = new TaskExecutor(context);
		}
		return instance;
	}

	/**
	 * Takes in a task that has to be performed.
	 * @param context
	 * @param task
	 * @return <b>success</b> Whether the task was submitted successfully or not
	 */
	static public boolean execute(Context context, Runnable task) {
		boolean success = true;
		try {
			TaskExecutor instance = getInstance(context);
			instance.workerFarm.execute(task);
		} catch (RejectedExecutionException ex) {
			success = false;
		}
		return success;
	}

	static public Future<?> submit(Context context, Runnable task) {
		Future<?> future = null;
		try {
			TaskExecutor instance = getInstance(context);
			logger.debug("Submitting task to pool of size : {} | Max : {}"
					, instance.workerFarm.getCorePoolSize()
					, instance.workerFarm.getMaximumPoolSize());
			future = instance.workerFarm.submit(task);
		} catch (RejectedExecutionException ex) {
			logger.warn("Could not submit runnable to queue");
		}
		return future;
	}

	/**
	 * Remove the task from the thread pool's queue if possible
	 * @param context
	 * @param task
	 * @return
	 */
	static public boolean remove(Context context, Runnable task) {
		if (task == null) return false;
		TaskExecutor instance = getInstance(context);
		return instance.workerFarm.remove(task);
	}
}