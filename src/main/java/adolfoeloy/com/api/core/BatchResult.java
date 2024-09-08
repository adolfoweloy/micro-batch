package adolfoeloy.com.api.core;

import java.util.List;

/**
 * Represents the result of a batch of jobs.
 * @param <T> the output type of the jobs
 */
public record BatchResult<T>(List<JobResult<T>> results) { }
