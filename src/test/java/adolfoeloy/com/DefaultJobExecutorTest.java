package adolfoeloy.com;

import adolfoeloy.com.api.core.Batch;
import adolfoeloy.com.api.core.InputSource;
import adolfoeloy.com.api.core.Job;
import adolfoeloy.com.api.core.JobExecutor;
import adolfoeloy.com.api.core.JobResult;
import adolfoeloy.com.api.core.JobResultStatus;
import adolfoeloy.com.api.serviceprovider.BatchProcessor;
import net.jodah.concurrentunit.Waiter;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultJobExecutorTest {
    private final Waiter waiter = new Waiter();
    private final int BATCH_SIZE = 2;

    @Test
    void submitSingle_should_process_the_job_immediately_and_return_its_result()
            throws InterruptedException, TimeoutException {
        // Given
        var resultCollection = new ConcurrentLinkedQueue<JobResult<String>>();
        var underTest = getExecutor(BATCH_SIZE, resultCollection);

        // When
        underTest.start();
        underTest.submit(new MyTestJob(() -> "Running single job"));
        underTest.shutdown();

        // Then
        waiter.await(3, TimeUnit.SECONDS, 1);
        assertThat(resultCollection).containsExactly(
                new JobResult<>(Optional.of("Running single job"), JobResultStatus.SUCCESS)
        );
    }

    @Test
    void submit_should_process_jobs_split_into_batches_given_the_frequency_of_two_seconds()
            throws InterruptedException, TimeoutException {

        // Given
        var resultCollection = new ConcurrentLinkedQueue<JobResult<String>>();
        var executor = getExecutor(BATCH_SIZE, resultCollection);

        // When
        executor.start();
        executor.submit(new MyTestJob(() -> "Hello " + System.currentTimeMillis()));
        executor.submit(new MyTestJob(() -> "Hello " + System.currentTimeMillis()));
        executor.submit(new MyTestJob(() -> "Hello " + System.currentTimeMillis()));
        executor.shutdown();

        // Then
        var expectedBatches = 2;
        var expectedJobs = 3;

        waiter.await(3, TimeUnit.SECONDS, expectedBatches);
        assertThat(resultCollection).hasSize(expectedJobs);
    }

    @Test
    void submit_should_handle_jobs_that_fail_without_stopping_processing_other_successful_jobs()
            throws InterruptedException, TimeoutException {

        // Given
        var resultCollection = new ConcurrentLinkedQueue<JobResult<String>>();
        var executor = getExecutor(BATCH_SIZE, resultCollection);

        // When
        executor.start();
        executor.submit(new MyTestJob(() -> { throw new RuntimeException("Oops!"); }));
        executor.submit(new MyTestJob(() -> "Hello " + System.currentTimeMillis()));
        executor.submit(new MyTestJob(() -> "Hello " + System.currentTimeMillis()));
        executor.shutdown();

        // Then
        var expectedBatches = 2;
        waiter.await(3, TimeUnit.SECONDS, expectedBatches);

        var statusesFound = resultCollection.stream().map(JobResult::status);
        assertThat(statusesFound).containsExactlyInAnyOrder(
                JobResultStatus.FAILURE,
                JobResultStatus.SUCCESS,
                JobResultStatus.SUCCESS
        );
    }

    @Test
    void shutdown_should_wait_all_pending_jobs_to_finish_before_closing()
            throws InterruptedException, TimeoutException {

        // Given
        var resultCollection = new ConcurrentLinkedQueue<JobResult<String>>();
        var executor = getExecutor(BATCH_SIZE, resultCollection);

        // When
        executor.start();
        executor.submit(new MyTestJob(() -> {
            Thread.sleep(1000);
            return "Hello " + System.currentTimeMillis();
        }));
        executor.submit(new MyTestJob(() -> {
            Thread.sleep(2000);
            return "Hello " + System.currentTimeMillis();
        }));
        executor.submit(new MyTestJob(() -> {
            Thread.sleep(1000);
            return "Hello " + System.currentTimeMillis();
        }));
        var beforeShutdown = Instant.now().getEpochSecond();
        executor.shutdown();
        var afterShutdown = Instant.now().getEpochSecond();

        // if seconds between is 0, it means that shutdown indeed didn't wait.
        assertThat(afterShutdown - beforeShutdown).isBetween(1L, 3L);

        // Then
        var expectedBatches = 2;
        waiter.await(5, TimeUnit.SECONDS, expectedBatches);

        var statusesFound = resultCollection.stream().map(JobResult::status);
        assertThat(statusesFound).containsExactlyInAnyOrder(
                JobResultStatus.SUCCESS,
                JobResultStatus.SUCCESS,
                JobResultStatus.SUCCESS
        );
    }

    private JobExecutor<String, String> getExecutor(int batchSize, Queue<JobResult<String>> resultCollection) {
        return new DefaultJobExecutor(
                batchSize,
                new MyTestBatchProcessor(waiter, resultCollection),
                Duration.ofSeconds(2)
        );
    }

    private static class MyTestJob extends Job<String, String> {
        protected MyTestJob(InputSource<String> inputSource) {
            super(inputSource);
        }

        @Override
        public String execute() throws InterruptedException {
            return inputSource.read();
        }
    }

    private record MyTestBatchProcessor(
            Waiter waiter,
            Queue<JobResult<String>> resultCollection
    ) implements BatchProcessor<String, String> {

        @Override
        public void processBatch(Batch<String, String> batch) {
            resultCollection.addAll(
                batch.process().results()
            );

            // resume after each batch is processed
            waiter.resume();
        }

    }
}
