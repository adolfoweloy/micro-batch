package adolfoeloy.com.api.core;

/**
 * The result of a Job execution.
 * @param <T> the type of the result
 */
public record JobResult<T>(T result) { }