package adolfoeloy.com.api.core;

import java.util.Optional;

/**
 * The result of a Job execution.
 * @param <T> the type of the result
 */
public record JobResult<T>(
    Optional<T> result,
    JobResultStatus status
) { }
