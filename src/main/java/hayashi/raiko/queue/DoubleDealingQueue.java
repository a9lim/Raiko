// southernscreamer32, 2023
// :3

package hayashi.raiko.queue;

import java.util.*;

public class DoubleDealingQueue<T extends Queueable> {
    private final Deque<T> deque;

    public DoubleDealingQueue(){
         deque = new ArrayDeque<>();
    }

    public DoubleDealingQueue(int i){
        deque = new ArrayDeque<>(i);
    }

    public void add(T item) {
        deque.add(item);
    }

    public void push(T item) {
        deque.push(item);
    }

    public void add(int i, T item) {
        if (i >= deque.size()) {
            deque.add(item);
            return;
        }
        if (i <= 0) {
            deque.push(item);
            return;
        }
        ArrayDeque<T> helper = new ArrayDeque<>(i);
        for (; i > 0; i--)
            helper.push(deque.pop());
        deque.push(item);
        for (T t : helper)
            deque.push(t);
    }

    public int size() {
        return deque.size();
    }

    public T pop() {
        return deque.pop();
    }

    public boolean isEmpty() {
        return deque.isEmpty();
    }

    public Deque<T> getDeque() {
        return deque;
    }

    public T get(int index) {
        Iterator<T> iterator = deque.iterator();
        while (--index > 1)
            iterator.next();
        return iterator.next();
    }

    public T remove(int index) {
        Iterator<T> iterator = deque.iterator();
        while (--index > 1)
            iterator.next();
        T out = iterator.next();
        iterator.remove();
        return out;
    }

    public int removeAll(long identifier) {
        int count = 0;
        Iterator<T> iterator = deque.iterator();
        while (iterator.hasNext())
            if (iterator.next().getIdentifier() == identifier) {
                iterator.remove();
                count++;
            }
        return count;
    }

    public void clear() {
        deque.clear();
    }

    public int shuffle(long identifier) {
        ArrayDeque<Integer> iset = new ArrayDeque<>();
        ArrayList<T> out = new ArrayList<>();
        ArrayDeque<T> helper = new ArrayDeque<>();
        T temp;
        int size = deque.size();
        iset.push(-1);
        for (int i = 1; i <= size; i++)
            if ((temp = deque.pop()).getIdentifier() == identifier) {
                iset.push(i);
                out.add(temp);
            } else {
                helper.push(temp);
            }
        Collections.shuffle(out);
        for (int j = 0; size > 0; size--)
            if (size > iset.peek()) {
                deque.push(helper.pop());
            } else {
                deque.push(out.get(j++));
                iset.pop();
            }
        return out.size();
    }

    public void shuffle() {
        ArrayList<T> out = new ArrayList<>(deque);
        Collections.shuffle(out);
        deque.clear();
        for (T t : out)
            deque.push(t);
    }

    public void skip(int number) {
        while (--number > 0)
            deque.pop();
    }

    public void backskip(int number) {
        while (--number > 0)
            deque.removeLast();
    }

    public T moveItem(int from, int to) {
        int i;
        ArrayDeque<T> helper = new ArrayDeque<>(to);
        for (i = 0; i < from; i++)
            helper.push(deque.pop());
        T B = deque.pop();
        if (to > from)
            for (; i < to; i++)
                helper.push(deque.pop());
        else
            for (; i > to; i--)
                deque.push(helper.pop());
        deque.push(B);
        for (T t : helper)
            deque.push(t);
        return B;
    }

    public void swap(int a, int b) {
        int i;
        if (b > a) {
            i = a;
            a = b;
            b = i;
        }
        a--;
        ArrayDeque<T> helper = new ArrayDeque<>(a);
        for (i = 0; i < b; i++)
            helper.push(deque.pop());
        T B = deque.pop();
        for (; i < a; i++)
            helper.push(deque.pop());
        T A = deque.pop();
        deque.push(B);
        for (; i > b; i--)
            deque.push(helper.pop());
        deque.push(A);
        for (T t : helper)
            deque.push(t);
    }

    public String toString(){
        StringBuilder out = new StringBuilder();
        for(T t: deque)
            out.append(t);
        return out.toString();
    }
}
