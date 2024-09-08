# Introduction

This library is a simple implementation of micro-batch processing 
with extension points for job definition and batch processing results.

Within a given configured period of time (micro-batch interval), a collection of 
batches are processed in sequence.

# Usage

1. Create a `JobExecutor` instance setting the batch size and interval.
2. Implement the `Job` interface in terms of an `InputSource` which provides the input data that may come from anywhere (e.g. database, file, network, etc.).
3. Implement the `BatchProcessor` interface to process the input data and process the results.

# Examples

Let's consider a very simple example to process strings provided by an `InputSource` that provides a simple text.
But first, let's create an instance of `Job` that will be responsible for reading the input data and processing it.

```java
class HumbleJob extends Job<String, String> {
    protected HumbleJob(InputSource<String> inputSource) {
        super(inputSource);
    }

    @Override
    public String execute() {
        return inputSource.read();
    }
}
```

Now, let's create a `BatchProcessor` that will process the input data and return the results.

```java
record MyBatchProcessor(List<String> resultCollection) 
    implements BatchProcessor<String, String> {

    @Override
    public void processBatch(Batch<String, String> batch) {
        var successfulResults = batch.process().results().stream()
                .filter(r -> r.status() == JobResultStatus.SUCCESS && r.result().isPresent())
                .map(JobResult::result)
                .map(Optional::get)
                .toList();
        resultCollection.addAll(successfulResults);
    }
}
```

Notice that `MyBatchProcessor` is collecting all successful results in a list. But that could be anything else such as notifying a third party application about the results, writing the results to a file, etc.

Finally, let's create an instance of `JobExecutor` instance and run the job.

```java 
var batchSize = 10;
var jobExecutor = new DefaultJobExecutor(
    batchSize,
    new MyBatchProcessor(resultCollection),
    Duration.ofSeconds(2)
);
```

This will run the job every 2 seconds and process the input data in batches of 10 elements.