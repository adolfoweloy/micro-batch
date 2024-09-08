package adolfoeloy.com.api.serviceprovider;

import adolfoeloy.com.api.core.Batch;

public interface BatchProcessor<T, R> {

    void processBatch(Batch<T, R> batch);

}