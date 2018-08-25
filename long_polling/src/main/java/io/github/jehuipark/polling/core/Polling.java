package io.github.jehuipark.polling.core;

import io.github.jehuipark.polling.model.ResponseMap;

/**
 * LongPolling 프로세스를 처리
 * <p>
 * PublicManager 클래스의 start 메소드를 통하여 실행
 * 
 * @author JH
 *
 */
public class Polling<T> {

	public final static int ALREADY_WORKING = -3;
	public final static int DEAD = -2;
	public final static int NO_DATA = -1;

	public final static int UNDEFINED = 0;

	public final static int DESTROY_COMMAND = 1;
	public final static int SYSTEM_COMMAND = 2;
	public final static int TRANSACTION_COMMAND = 3;

	public final static int REST = 11;
	public final static int WORKING = 12;

	private Observe<T> observe;
	private Validation<T> validation;
	private T lastData = null;
	private TransactionThread transaction;
	private long interval;
	private long transactionTime;
	private ResponseMap<T> responseMap;
	private int commandState = UNDEFINED;
	private int workState = REST;

	/**
	 * polling 프로세스 실행
	 * <p>
	 * PublicManager 클래스로 접근
	 * 
	 * @return 결과
	 */
	synchronized ResponseMap<T> job() {
		setWorkState(WORKING);

		transaction = new TransactionThread(() -> {
			T observData = observe.apply();
			boolean isUpdate = false;
			if (observData != null) {
				if (lastData != null) {
					isUpdate = validation.apply(observData, lastData);
				} else {
					isUpdate = true;
				}
				save(observData, isUpdate);
				responseMap.setData(observData);
			} else {
				responseMap.setStatus(NO_DATA);
			}
			responseMap.setUpdate(isUpdate);
			return isUpdate;
		});

		transaction.start();
		transactionWait();

		if (responseMap.isUpdate() == false && commandState != UNDEFINED)
			responseMap.setStatus(commandState);

		setWorkState(REST);
		return responseMap;
	}

	public void setObserve(Observe<T> observe) {
		this.observe = observe;
	}

	public void setValidation(Validation<T> validation) {
		this.validation = validation;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public long getTransactionTime() {
		return transactionTime;
	}

	public void setTransactionTime(long transactionTime) {
		this.transactionTime = transactionTime;
	}

	int getCommandState() {
		return commandState;
	}

	void setCommandState(int commandState) {
		this.commandState = commandState;
	}

	int getWorkState() {
		return workState;
	}

	void setWorkState(int workState) {
		this.workState = workState;
	}

	private void setLastData(T lastData) {
		this.lastData = lastData;
	}
	public ResponseMap<T> getResponseMap() {
		return responseMap;
	}

	public void setResponseMap(ResponseMap<T> responseMap) {
		this.responseMap = responseMap;
	}

	/**
	 * 폴링 중단
	 * 
	 * @param commandCode
	 */
	void destroy(int commandCode) {
		System.out.println("polling destroy");
		setCommandState(commandCode);
		transaction.interrupt();
	}

	/**
	 * 변경데이터 저장
	 * 
	 * @param data
	 * @param isUpdate
	 */
	private void save(T data, boolean isUpdate) {
		if (isUpdate)
			setLastData(data);
	}

	/**
	 * 트랜잭션 대기
	 */
	private void transactionWait() {
		try {
			transaction.join();
		} catch (InterruptedException e) {
			transaction.interrupt();
		}
	}

	/**
	 * 트랜잭션 스레드
	 * <p>
	 * 스레드 종료조건 :
	 * <p>
	 * 1. 트랜잭션 시간 초과
	 * <p>
	 * 2. 관찰 데이터 변경
	 * <p>
	 * 3. PollingManager에 의한 중단 요청
	 * 
	 * @author JH
	 *
	 */
	private class TransactionThread extends Thread {
		Runner runner;

		TransactionThread(Runner runner) {
			this.runner = runner;
		}

		@Override
		public void run() {
			long endTime = System.currentTimeMillis() + transactionTime;
			while (true) {
				if (this.isInterrupted()) {
					break;
				}
				if (System.currentTimeMillis() >= endTime) {
					setCommandState(TRANSACTION_COMMAND);
					break;
				}
				if (runner.wrap()) {
					break;
				}
				try {
					TransactionThread.sleep(interval);
				} catch (InterruptedException e) {
					this.interrupt();
				}
			}
		}
	}

	public interface Observe<T> {
		T apply();
	}

	public interface Validation<R> {
		boolean apply(R value, R origin);
	}

	private interface Runner {
		boolean wrap();
	}
}
