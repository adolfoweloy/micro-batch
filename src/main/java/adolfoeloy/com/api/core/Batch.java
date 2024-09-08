package adolfoeloy.com.api.core;

import java.util.List;
import java.util.Optional;

/**
 * Represents a batch of jobs to be executed.
 * @param <T> the input type
 * @param <R> the output type
 */
public record Batch<T, R>(List<Job<T, R>> jobs, String name) {

    public BatchResult<R> process() {
        var result = jobs.stream()
            .map(job -> {
                try {
                    return new JobResult<>(
                        Optional.ofNullable(job.execute()),
                        JobResultStatus.SUCCESS
                    );
                } catch (Exception e) {
                    return new JobResult<R>(
                        Optional.empty(),
                        JobResultStatus.FAILURE
                    );
                }
            })
            .toList();
        return new BatchResult<>(result);
    }

}
