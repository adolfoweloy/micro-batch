package adolfoeloy.com.api.core;

/**
 * Represents the status of a Job execution.
 * Future work will very likely replace this enum with sealed classes representing both
 * a success job and a failure job.
 */
public enum JobResultStatus {
    SUCCESS,
    FAILURE
}
