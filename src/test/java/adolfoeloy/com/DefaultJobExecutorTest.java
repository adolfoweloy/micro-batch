package adolfoeloy.com;

import adolfoeloy.com.api.core.InputSource;
import adolfoeloy.com.api.core.Job;
import adolfoeloy.com.api.core.JobExecutor;
import adolfoeloy.com.api.core.JobResult;
import adolfoeloy.com.api.serviceprovider.Batch;
import adolfoeloy.com.api.serviceprovider.BatchProcessor;
import net.jodah.concurrentunit.Waiter;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultJobExecutorTest {
    private final Waiter waiter = new Waiter();
    private final int BATCH_SIZE = 2;

    @Test
    void submitSingle_should_process_the_job_immediately_and_return_its_result() {
        // Given
        var underTest = getExecutor(BATCH_SIZE, new ConcurrentLinkedQueue<>());

        // When
        underTest.start();
        var result = underTest.submitSingle(new MyTestJob(() -> "Running single job"));
        underTest.shutdown();

        // Then
        assertThat(result).isEqualTo(new JobResult<>("Running single job"));
    }

    @Test
    void submit_should_process_jobs_split_into_batches_given_the_frequency_of_two_seconds()
            throws InterruptedException, TimeoutException {

        // Given
        var resultCollection = new ConcurrentLinkedQueue<String>();
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

    private JobExecutor<String, String> getExecutor(int batchSize, Queue<String> resultCollection) {
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
        public JobResult<String> execute() {
            return new JobResult<>(inputSource.read());
        }
    }

    private record MyTestBatchProcessor(
            Waiter waiter,
            Queue<String> resultCollection
    ) implements BatchProcessor<String, String> {

        @Override
        public void processBatch(Batch<String, String> batch) {
            // collect the output of each job that is operating on a given input function
            batch.jobs().forEach(job -> resultCollection.add(job.execute().result()));

            // resume after each batch is processed
            waiter.resume();
        }

    }
}
