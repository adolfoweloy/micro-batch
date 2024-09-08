package adolfoeloy.com.api.serviceprovider;

import adolfoeloy.com.api.core.Job;
import adolfoeloy.com.api.core.JobResult;

import java.util.List;

public record Batch<T, R>(List<Job<T, R>> jobs, String name) {

    List<JobResult<R>> process() {
        return jobs.stream()
               .map(Job::execute)
               .toList();
    }

}
