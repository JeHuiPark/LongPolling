package io.github.jehuipark.polling.core;

import java.util.HashMap;
import java.util.Map;

import io.github.jehuipark.polling.model.ResponseMap;

/**
 * Polling 제어
 * 
 * @author JH
 *
 */
public class PollingManager {

	private Map<String, Polling<?>> pollingMap = new HashMap<>();
	private Map<String, LifeThread> threadMap = new HashMap<>();

	private PollingManager() {
	};

	/**
	 * 폴링 초기화 및 전체 생명주기 초기화 이미 생성되있다면 생명주기 갱신
	 *
	 * @param id
	 *            폴링 구분값
	 * @param lifeTime
	 *            전체 생명주기
	 * @return 초기화된 폴링
	 */
	public Polling<?> start(String id, long lifeTime, Builder<?> builder) {
		boolean refresh = false;
		if (pollingMap.get(id) == null) {
			create(id, lifeTime, builder);
		} else {
			refresh = true;
		}
		
		start(id, refresh);
		return pollingMap.get(id);
	}

	/**
	 * 폴링 시작
	 *
	 * @param id
	 * @return
	 */
	private ResponseMap<?> start(String id, boolean refresh) {
		ResponseMap<?> responseMap = pollingMap.get(id).getResponseMap();
		
		if(refresh)  refresh(id);
		
		LifeThread lifeThread = threadMap.get(id);
		Polling<?> polling = pollingMap.get(id);
		if (lifeThread != null && lifeThread.isAlive()) {

			if (polling.getWorkState() == Polling.REST)
				polling.job();
			else {
				initError(responseMap, Polling.ALREADY_WORKING);
			}
		} else {
			initError(responseMap, Polling.DEAD);
		}
		return responseMap;
	}

	/**
	 * 폴링 중단
	 *
	 * @param id
	 */
	public void destroy(String id) {
		LifeThread lifeThread = threadMap.get(id);
		if (lifeThread != null && lifeThread.isAlive()) {
			lifeThread.interrupt();
		}
		waitLifeThread(lifeThread);
		remove(id);
	}

	/**
	 * 폴링 및 생명주기 초기화
	 * 
	 * @param id
	 * @param lifeTime
	 */
	private void create(String id, long lifeTime, Builder<?> builder) {
		System.out.println("create");
		Polling<?> polling = builder.build();
		pollingMap.put(id, polling);
		LifeThread lifeThread = new LifeThread(id, lifeTime);
		threadMap.put(id, lifeThread);
		lifeThread.start();
	}

	/**
	 * 생명주기 갱신
	 * 
	 * @param id
	 */
	private void refresh(String id) {
		System.out.println("refresh");
		LifeThread lifeThread = threadMap.get(id);
		if (lifeThread != null)
			lifeThread.refresh();
	}

	/**
	 * threadMap에서 생명주기 스레드 제거 pollingMap에서 폴링 제거
	 * 
	 * @param id
	 */
	private void remove(String id) {
		System.out.println("remove id");
		threadMap.remove(id);
		pollingMap.remove(id);
	}

	/**
	 * 에러 정의
	 * 
	 * @param resultMap
	 * @param status
	 */
	private void initError(ResponseMap<?> responseMap, int status) {
		responseMap.setUpdate(false);
		responseMap.setStatus(status);
	}

	private void waitLifeThread(LifeThread lifeThread) {
		try {
			lifeThread.join();
		} catch (InterruptedException e) {
			lifeThread.interrupt();
		}
	}

	/**
	 * 생명주기 제어 스레드
	 * 
	 * @author JH
	 *
	 */
	private class LifeThread extends Thread {

		long lifeTime;
		long endTime;
		Polling<?> polling;
		String id;

		LifeThread(String id, long lifeTime) {
			this.polling = pollingMap.get(id);
			this.id = id;
			this.lifeTime = lifeTime;
		}

		@Override
		public void run() {
			endTime = getEndTime();
			int comandCode = Polling.SYSTEM_COMMAND;
			while (true) {
				if (System.currentTimeMillis() >= endTime) {
					remove(id);
					break;
				}
				if (this.isInterrupted()) {
					comandCode = Polling.DESTROY_COMMAND;
					break;
				}
			}
			_desytroy(comandCode);
		}

		/**
		 * 스레드 종료시간 연장
		 */
		public void refresh() {
			endTime = getEndTime();
		}

		/**
		 * 폴링 파괴
		 * 
		 * @param commandCode
		 */
		public void _desytroy(int commandCode) {
			System.out.println("LifeThread _desytroy");
			if (polling != null)
				polling.destroy(commandCode);
		}

		/**
		 * 생명주기 종료시간 획득
		 * 
		 * @return 현재시간 + 생명주기
		 */
		private long getEndTime() {
			return System.currentTimeMillis() + lifeTime;
		}

	}

	public static class Lazy {
		public static final PollingManager INSTANCE = new PollingManager();
	}
}
