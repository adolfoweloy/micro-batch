package adolfoeloy.com.api.core;

/**
 * Defines a Job to be executed. The Job takes in an input source that produces T and returns R.
 * @param <T> the input type
 * @param <R> the output type
 */
public abstract class Job<T, R> {

    protected final InputSource<T> inputSource;

    protected Job(InputSource<T> inputSource) {
        this.inputSource = inputSource;
    }

    protected abstract R execute();
}
