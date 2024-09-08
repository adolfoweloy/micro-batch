package adolfoeloy.com.api.core;

import java.util.List;

public record BatchResult<T>(List<JobResult<T>> results) { }
