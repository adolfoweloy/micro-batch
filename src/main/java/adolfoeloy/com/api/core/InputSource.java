package adolfoeloy.com.api.core;

/**
 * Defines the input source for the Job.
 * The input can be contents from a database, file, or any other source.
 *
 * @param <T> defines the content type of the input, e.g. String, Integer, etc.
 */
public interface InputSource<T> {

    T read();

}
