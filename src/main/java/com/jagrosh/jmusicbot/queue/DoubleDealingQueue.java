/*
 * Copyright 2016 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.queue;

import java.util.*;

/**
 *
 * @author John Grosh (jagrosh)
 * @param <T>
 */
public class DoubleDealingQueue<T extends Queueable> {
    private final Deque<T> deque = new ArrayDeque<>();
    
    public void add(T item)
    {
        deque.add(item);
    }

    public void push(T item)
    {
        deque.push(item);
    }

    public void addAt(int i, T item)
    {
        if(i >= deque.size())
            deque.add(item);
        else if(i <= 0)
            deque.push(item);
        else {
            T[] helper = (T[]) new Object[i];
            for(; i > 0; i--)
                helper[i] = deque.pop();
            deque.push(item);
            for(T t: helper)
                deque.push(t);
        }
    }

    public int size()
    {
        return deque.size();
    }
    
    public T pull()
    {
        return deque.pop();
    }
    
    public boolean isEmpty()
    {
        return deque.isEmpty();
    }
    
    public Deque<T> getDeque()
    {
        return deque;
    }
    
    public T get(int index)
    {
        Iterator<T> iterator = deque.iterator();
        while(--index > 1) {
            iterator.next();
        }
        return iterator.next();
    }
    
    public T remove(int index)
    {
        Iterator<T> iterator = deque.iterator();
        while(--index > 1) {
            iterator.next();
        }
        T out = iterator.next();
        iterator.remove();
        return out;
    }
    
    public int removeAll(long identifier)
    {
        int count = 0;
        Iterator<T> iterator = deque.iterator();
        for(int i = deque.size(); i>0; i--)
            if(iterator.next().getIdentifier()==identifier)
            {
                iterator.remove();
                count++;
            }
        return count;
    }
    
    public void clear()
    {
        deque.clear();
    }

    // swap implementation in deque soon
    /*
    public int shuffle(long identifier)
    {
        List<Integer> iset = new ArrayList<>();
        for(int i = 0; i< deque.size(); i++)
        {
            if(deque.get(i).getIdentifier()==identifier)
                iset.add(i);
        }
        for(int j=0; j<iset.size(); j++)
        {
            int first = iset.get(j);
            int second = iset.get((int)(Math.random()*iset.size()));
            T temp = deque.get(first);
            deque.set(first, deque.get(second));
            deque.set(second, temp);
        }
        return iset.size();
    }
    */

    // placeholder so stuff doesn't break :skull:
    public int shuffle(long i) {
        return 0;
    }

    public void skip(int number) {
        while(--number > 0)
            deque.pop();
    }


    // rewrite this at some point
    /**
     * Move an item to a different position in the list
     * @param from The position of the item
     * @param to The new position of the item
     * @return the moved item
     */
    public T moveItem(int from, int to) {
        T item = remove(from);
        addAt(to, item);
        return item;
    }
}
