package adolfoeloy.com.api.core;

/**
 * The JobExecutor orchestrate Job execution.
 * @param <T> the input type of the jobs
 * @param <R> the output type of the jobs
 */
public interface JobExecutor<T, R> {
    /**
     * Allow clients to submit a single Job for immediate execution.
     * @param job the job to be executed
     * @return the result of the job
     */
//    JobResult<R> submitSingle(Job<T, R> job);

    /**
     * Allow clients to submit a Job for execution in batches.
     * @param job the job to be executed
     */
    void submit(Job<T, R> job);

    /**
     * Start the JobExecutor. This means that batches of jobs will be processed periodically.
     */
    void start();

    /**
     * Shutdown the JobExecutor after all previously accepted jobs have been processed.
     */
    void shutdown();
}
