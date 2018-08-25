# LongPolling

[LongPolling(지연응답)의 개념](https://d2.naver.com/helloworld/1052 "참조")

### example
``` java
    	long LIFE_TIME = 10000;
    	long TRANSACTION = 5000;
    	long INTERVAL = 100;
    	
    	PollingManager pollingManager = PollingManager.Lazy.INSTANCE;
    	Builder<Integer> builder = new Builder<>();
    	builder.setInterval(INTERVAL)
    	.setTransactionTime(TRANSACTION)
    	.setObserve(()->{
    		return new Random().nextInt(3);
    	})
    	.setValidation((value, origin)->{
    		boolean isUpdate = false;
    		if(value != origin)
    			isUpdate = true;
			return isUpdate;
    	});
    	
    	pollingManager.start("id", LIFE_TIME, builder);
```
