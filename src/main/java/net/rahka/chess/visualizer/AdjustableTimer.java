package net.rahka.chess.visualizer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AdjustableTimer {

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private boolean running;
	private Runnable runnable;
	private long ms;
	private ScheduledFuture<?> future;

	private void run() {
		synchronized (this) {
			if (running) {
				runnable.run();
				future = scheduler.schedule(this::run, ms, TimeUnit.MILLISECONDS);
			}
		}
	}

	public void start(Runnable runnable, long ms) {
		this.running = true;
		this.runnable = runnable;
		this.ms = ms;

		future = scheduler.schedule(this::run, ms, TimeUnit.MILLISECONDS);
	}

	public boolean isRunning() {
		return running;
	}

	public void shutdown() {
		running = false;
		future.cancel(false);
	}

	public void adjust(long ms) {
		synchronized (this) {
			this.ms = ms;
		}
	}

}
