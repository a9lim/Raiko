// Copyright 2023 Aidan Lim (southernscreamer32) <aidanlim192@gmail.com>
// Copyright 2016-2018 John Grosh (jagrosh) <john.a.grosh@gmail.com> & Kaidan Gustave (TheMonitorLizard).
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

package hayashi.raiko;

import hayashi.raiko.queue.DoubleDealingQueue;

import java.util.Iterator;

public class Test {

    public static void main(String[] args){
        testMoveTo(10);

    }
    public static void testShuffleMine(int len){
        DoubleDealingQueue<TestQueuable<Integer>> queue = new DoubleDealingQueue<>();
        int[] arr = new int[len];
        for(int i = 0; i < len; i++)
            arr[i] = i+10;

        for(int s: arr)
            queue.add(new TestQueuable<>(s,(int)(Math.random()+0.25)));

        queue.getDeque().forEach(t -> System.out.print(t + "  "));
        System.out.println();
        queue.getDeque().forEach(t -> System.out.print("0" + t.getIdentifier() + "  "));

        System.out.println();

        System.out.println();
        queue.shuffle(1);

        queue.getDeque().forEach(t -> System.out.print(t + "  "));
        System.out.println();
        queue.getDeque().forEach(t -> System.out.print("0" + t.getIdentifier() + "  "));

        System.out.println();

    }
    public static void testShuffle(int num, int len){
        DoubleDealingQueue<TestQueuable<Integer>> queue = new DoubleDealingQueue<>();
        int[] arr = new int[len];
        for(int i = 0; i < len; i++)
            arr[i] = i;

        for(int s: arr)
            queue.add(new TestQueuable<>(s));

        queue.getDeque().forEach(t -> System.out.print(t + "  "));

        System.out.println();
        queue.shuffle();

        queue.getDeque().forEach(t -> System.out.print(t + "  "));

        System.out.println();
        int[] arr2 = new int[queue.size()];

        for(int i = 0; i < num; i++) {
            queue.shuffle();
            Iterator<TestQueuable<Integer>> iterator = queue.getDeque().iterator();
            for (int j = 0; j < arr2.length; j++) {
                arr2[j] += iterator.next().getS();
            }
        }
        for(int i : arr2)
            System.out.print(i/(double)num + " ");
    }

    public static void testMoveTo(int len){
        DoubleDealingQueue<TestQueuable<Integer>> queue = new DoubleDealingQueue<>();
        int[] arr = new int[len];
        for(int i = 0; i < len; i++)
            arr[i] = i+10;

        for(int s: arr)
            queue.add(new TestQueuable<>(s));

        queue.getDeque().forEach(t -> System.out.print(t + "  "));

        System.out.println();
        queue.moveItem(4,7);

        queue.getDeque().forEach(t -> System.out.print(t + "  "));
    }
}
