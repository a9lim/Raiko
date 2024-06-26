// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
//
// This file is part of Raiko.
//
// Raiko is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// Raiko is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Raiko. If not, see <http://www.gnu.org/licenses/>.

package a9lim.raiko.queue;

import java.util.*;


// Deque with a few extra features
public class DoubleDealingQueue<T extends Queueable> {
    private Deque<T> deque;

    // Standard constructor
    public DoubleDealingQueue(){
         deque = new ArrayDeque<>();
    }

    // Known size constructor
    public DoubleDealingQueue(int i){
        deque = new ArrayDeque<>(i);
    }

    // Normal Deque adders
    public void add(T item) {
        deque.add(item);
    }

    public void push(T item) {
        deque.push(item);
    }

    // Arbitrary index add
    public void add(int index, T item) {
        ArrayDeque<T> helper;
        if(index < deque.size()/2) {
            helper = new ArrayDeque<>(index);
            while(index-- > 0)
                helper.push(deque.pop());
            deque.push(item);
            helper.forEach(deque::push);
        } else {
            index = deque.size() - index;
            helper = new ArrayDeque<>(index);
            while(index-- > 0)
                helper.push(deque.removeLast());
            deque.add(item);
            deque.addAll(helper);
        }
    }

    // Normal Deque getters
    public T peek() {
        return deque.peek();
    }

    public T peekLast() {
        return deque.peekLast();
    }

    // Arbitrary index get
    public T get(int index) {
        Iterator<T> iterator;
        if(index < deque.size()/2) {
            iterator = deque.iterator();
            while (index-- > 0)
                iterator.next();
        } else {
            iterator = deque.descendingIterator();
            while (++index < deque.size())
                iterator.next();
        }
        return iterator.next();
    }

    // Normal Deque removers
    public T pop() {
        return deque.pop();
    }

    public T popLast() {
        return deque.removeLast();
    }

    // Arbitrary index remove
    public T remove(int index) {
        Iterator<T> iterator;
        if(index < deque.size()/2) {
            iterator = deque.iterator();
            while (index-- > 0)
                iterator.next();
        } else {
            iterator = deque.descendingIterator();
            while (++index < deque.size())
                iterator.next();
        }
        T out = iterator.next();
        iterator.remove();
        return out;
    }

    // Removal by identity
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

    // Normal Deque methods
    public int size() {
        return deque.size();
    }

    public boolean isEmpty() {
        return deque.isEmpty();
    }

    public void clear() {
        deque.clear();
    }

    public Deque<T> getDeque() {
        return deque;
    }

    public void reverse(){
        deque = deque.reversed();
    }

    // Shuffle by identity
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

    // Shuffle deque
    public void shuffle() {
        ArrayList<T> out = new ArrayList<>(deque);
        Collections.shuffle(out);
        deque.clear();
        deque.addAll(out);
    }

    // Remove first n elements
    public void skip(int number) {
        while (--number > 0)
            deque.pop();
    }

    // Remove last n elements
    public void backskip(int number) {
        while (--number > 0)
            deque.removeLast();
    }

    // Move within deque
    public T moveItem(int from, int to) {
        int i = 0;
        ArrayDeque<T> helper = new ArrayDeque<>(to);
        while (i++ < from)
            helper.push(deque.pop());
        T B = deque.pop();
        if (to > from)
            while (i++ < to)
                helper.push(deque.pop());
        else
            while (i-- > to)
                deque.push(helper.pop());
        deque.push(B);
        helper.forEach(deque::push);
        return B;
    }

    // Swap two elements
    public List<T> swap(int a, int b) {
        int i;
        if (b > a) {
            i = a;
            a = b;
            b = i;
        }
        a--;
        b++;
        i = 0;
        ArrayDeque<T> helper = new ArrayDeque<>(a);
        while (++i < b)
            helper.push(deque.pop());
        T B = deque.pop();
        while (i++ < a)
            helper.push(deque.pop());
        T A = deque.pop();
        deque.push(B);
        while (--i > b)
            deque.push(helper.pop());
        deque.push(A);
        helper.forEach(deque::push);
        List<T> out = new LinkedList<>();
        out.add(A);
        out.add(B);
        return out;
    }

    public String toString(){
        StringBuilder out = new StringBuilder();
        deque.forEach(out::append);
        return out.toString();
    }
}
