/*******************************************************************************
 * Australian National University Data Commons
 * Copyright (C) 2013  The Australian National University
 * 
 * This file is part of Australian National University Data Commons.
 * 
 * Australian National University Data Commons is free software: you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package au.edu.anu.datacommons.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A service class that manages task scheduling. Tasks can be submitted to one of the following pools:
 * <p>
 * <ul>
 * <li>Fixed-size thread pool for most tasks (priority 4)
 * <li>Unbounded thread pool (priority 3) for tasks that wait for other tasks to complete.
 * <li>Fixed-size thread pool for idle tasks (priority 1)
 * </ul>
 * 
 * @author Rahul Khanna
 * 
 */
@Component
public class ThreadPoolService implements AutoCloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolService.class);

	private Set<ExecutorService> execs = new HashSet<>();

	private ScheduledThreadPoolExecutor scheduledThreadPool;
	private ThreadPoolExecutor cachedThreadPool;
	private ScheduledThreadPoolExecutor idleThreadPool;

	private List<Runnable> cleanupTasks = new ArrayList<Runnable>();

	/**
	 * Creates an instance of the the Thread Pool Service.
	 * 
	 * @param nThreads
	 */
	public ThreadPoolService(int nThreads) {
		scheduledThreadPool = new ScheduledThreadPoolExecutor(nThreads, new ThreadPoolFactory("fixed",
				Thread.NORM_PRIORITY - 1));
		execs.add(scheduledThreadPool);

		cachedThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), new ThreadPoolFactory("cache", Thread.NORM_PRIORITY - 2));
		execs.add(cachedThreadPool);

		idleThreadPool = new ScheduledThreadPoolExecutor(nThreads, new ThreadPoolFactory("idle", Thread.MIN_PRIORITY));
		execs.add(idleThreadPool);
	}

	/**
	 * @see ScheduledThreadPoolExecutor#submit(Callable)
	 */
	public <T> Future<T> submit(Callable<T> task) {
		return scheduledThreadPool.submit(task);
	}

	/**
	 * @see ScheduledThreadPoolExecutor#submit(Runnable)
	 */
	public Future<?> submit(Runnable task) {
		return scheduledThreadPool.submit(task);
	}

	/**
	 * @see ScheduledThreadPoolExecutor#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)
	 */
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return scheduledThreadPool.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

	/**
	 * @see ScheduledThreadPoolExecutor#submit(Callable)
	 */
	public <T> Future<T> submitCachedPool(Callable<T> task) {
		return cachedThreadPool.submit(task);
	}

	/**
	 * @see ThreadPoolExecutor#submit(Callable)
	 */
	public <T> Future<T> submitIdlePool(Callable<T> task) {
		return idleThreadPool.submit(task);
	}

	/**
	 * @see ScheduledThreadPoolExecutor#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)
	 */
	public ScheduledFuture<?> scheduleWhenIdleWithFixedDelay(Runnable command, long initialDelay, long delay,
			TimeUnit unit) {
		return idleThreadPool.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

	/**
	 * Adds a cleaup task to the list of tasks that will be performed on the shutdown of this instance.
	 * 
	 * @param task
	 *            Cleanup task as Runnable
	 */
	public synchronized void addCleanupTask(Runnable task) {
		this.cleanupTasks.add(task);
	}

	/**
	 * Waits until all tasks in the thread pool finish. Then performs clean up tasks.
	 */
	@Override
	@PreDestroy
	public void close() throws Exception {
		LOGGER.info("Thread Pool Service Shutdown: WAITING...");
		for (ExecutorService es : execs) {
			es.shutdown();
		}
		for (ExecutorService es : execs) {
			es.awaitTermination(10, TimeUnit.MINUTES);
		}
		LOGGER.info("Thread Pool Service Shutdown: SUCCESS.");

		LOGGER.info("Cleanup tasks: RUNNING...");
		for (Runnable task : cleanupTasks) {
			try {
				task.run();
			} catch (Exception e) {
				// Error in cleanup tasks cannot be handled by the application.
				LOGGER.error(e.getMessage(), e);
			}
		}
		LOGGER.info("Cleanup tasks: FINISHED.");
	}
}
