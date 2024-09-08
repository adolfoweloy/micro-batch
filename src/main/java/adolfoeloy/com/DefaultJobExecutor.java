package adolfoeloy.com;

import adolfoeloy.com.api.core.Batch;
import adolfoeloy.com.api.core.Job;
import adolfoeloy.com.api.core.JobExecutor;
import adolfoeloy.com.api.serviceprovider.BatchProcessor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Default implementation of the JobExecutor interface that processes strings.
 */
public class DefaultJobExecutor implements JobExecutor<String, String> {

    private final int batchSize;
    private final Duration frequencyMilliseconds;

    private final Queue<Job<String, String>> jobQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Message> message = new ConcurrentLinkedQueue<>();
    private final BatchProcessor<String, String> batchProcessor;

    private enum Message {
        STOP,
        PAUSE,
        RESUME
    }

    public DefaultJobExecutor(
            int batchSize,
            BatchProcessor<String, String> batchProcessor,
            Duration frequencyMilliseconds
    ) {
        this.batchProcessor = batchProcessor;
        this.batchSize = batchSize;
        this.frequencyMilliseconds = frequencyMilliseconds;
    }

    @Override
    public void submit(Job<String, String> job) {
        jobQueue.add(job);
    }

    @Override
    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(frequencyMilliseconds.toMillis());

                    getBatches()
                            .parallelStream()
                            .forEach(batchProcessor::processBatch);

                    var msg = message.poll();
                    if (msg != null)  {
                        if (msg == Message.STOP) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    /**
     * Retrieves all batches from the job queue.
     */
    private List<Batch<String, String>> getBatches() throws InterruptedException {
        var batches = new ArrayList<Batch<String, String>>();

        synchronized (jobQueue) {
            while (!jobQueue.isEmpty()) {
                batches.add(getBatch());
            }
        }

        return batches;
    }

    private Batch<String, String> getBatch() {
        var count = 0;
        var jobs = new ArrayList<Job<String, String>>();
        while (!jobQueue.isEmpty() && count < batchSize)  {
            var job = jobQueue.poll();
            jobs.add(job);
            count++;
        }
        return new Batch<>(jobs, "Batch " + System.currentTimeMillis());
    }

    @Override
    public void shutdown() {
        message.add(Message.STOP);
    }
}
