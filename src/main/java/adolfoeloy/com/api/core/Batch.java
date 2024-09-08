package adolfoeloy.com.api.core;

import java.util.List;
import java.util.Optional;

public record Batch<T, R>(List<Job<T, R>> jobs, String name) {

    public BatchResult<R> process() {
        var result = jobs.stream()
            .map(job -> {
                try {
                    return new JobResult<>(
                            // only batch can execute the job
                            // this way, Batch is always the way to get job results
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
