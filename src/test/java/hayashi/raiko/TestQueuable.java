package hayashi.raiko;

import hayashi.raiko.queue.Queueable;
public class TestQueuable<T> implements Queueable{
    private T s;
    private final long identifier;

    public TestQueuable(T j){
        this(j,0);
    }

    public TestQueuable(T j, long i){
        s = j;
        identifier = i;
    }

    public T getS() {
        return s;
    }

    public void setS(T s) {
        this.s = s;
    }

    @Override
    public String toString() {
        return s.toString();
    }

    @Override
    public long getIdentifier() {
        return identifier;
    }
}
