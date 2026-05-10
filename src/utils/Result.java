package utils;

/**
 * A simple container for service layer responses.
 * Can carry a success value of type T or a failure error message.
 */
public final class Result<T> {

    private final T value;
    private final String error;

    private Result(T value, String error) {
        this.value = value;
        this.error = error;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> failure(String error) {
        return new Result<>(null, error);
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isFailure() {
        return error != null;
    }

    public T getValue() {
        return value;
    }

    public String getError() {
        return error;
    }

    public void ifSuccess(java.util.function.Consumer<? super T> action) {
        if (isSuccess()) {
            action.accept(value);
        }
    }

    public void ifFailure(java.util.function.Consumer<String> action) {
        if (isFailure()) {
            action.accept(error);
        }
    }
}
