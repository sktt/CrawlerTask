package crawlertask.main;

public interface ICallback<T> {
    /**
     * 
     * @param eventName Name of the callback event
     * @param result The result
     */
    public void callbackEvent(String eventName, T result);
}
