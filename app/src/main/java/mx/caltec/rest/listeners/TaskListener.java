package mx.caltec.rest.listeners;

public interface TaskListener<T> {
    void onError(String msg);
    void onSuccess(T item);
}
