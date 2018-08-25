package io.github.jehuipark.polling.core;

import io.github.jehuipark.polling.core.Polling.Observe;
import io.github.jehuipark.polling.core.Polling.Validation;
import io.github.jehuipark.polling.model.ResponseMap;

public class Builder<T> {
	
	private Observe<T> observe;
	private Validation<T> validation;
	private long interval;
	private long transactionTime;
	
	Polling<T> build() {
		Polling<T> polling = new Polling<>();
		polling.setInterval(interval);
		polling.setTransactionTime(transactionTime);
		polling.setObserve(observe);
		polling.setValidation(validation);
		polling.setResponseMap(new ResponseMap<T>());
		return polling;
	}
	
	public Builder<T> setObserve(Observe<T> observe) {
		this.observe = observe;
		return this;
	}
	public Builder<T> setValidation(Validation<T> validation) {
		this.validation = validation;
		return this;
	}
	public Builder<T> setInterval(long interval){
		this.interval = interval;
		return this;
	}
	public Builder<T> setTransactionTime(long transactionTime){
		this.transactionTime = transactionTime;
		return this;
	}
	
	
}
