package adolfoeloy.com.api.serviceprovider;

public interface BatchProcessor<T, R> {

    void processBatch(Batch<T, R> batch);

}