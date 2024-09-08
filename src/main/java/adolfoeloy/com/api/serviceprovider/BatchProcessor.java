package adolfoeloy.com.api.serviceprovider;

import adolfoeloy.com.api.core.Batch;

/**
 * This interface defines the contract for clients of micro-batch to process a batch and to handle the results.
 *
 * @param <T> the input type
 * @param <R> the output type
 */
public interface BatchProcessor<T, R> {

    void processBatch(Batch<T, R> batch);

}