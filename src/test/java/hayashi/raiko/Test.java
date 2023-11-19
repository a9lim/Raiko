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

        for(TestQueuable<Integer> t: queue.getDeque())
            System.out.print(t + "  ");
        System.out.println();
        for(TestQueuable<Integer> t: queue.getDeque())
            System.out.print("0" + t.getIdentifier() + "  ");

        System.out.println();

        System.out.println();
        queue.shuffle(1);

        for(TestQueuable<Integer> t: queue.getDeque())
            System.out.print(t + "  ");
        System.out.println();
        for(TestQueuable<Integer> t: queue.getDeque())
            System.out.print("0" +  t.getIdentifier() + "  ");

        System.out.println();

    }
    public static void testShuffle(int num, int len){
        DoubleDealingQueue<TestQueuable<Integer>> queue = new DoubleDealingQueue<>();
        int[] arr = new int[len];
        for(int i = 0; i < len; i++)
            arr[i] = i;

        for(int s: arr)
            queue.add(new TestQueuable<>(s));

        for(TestQueuable<Integer> t: queue.getDeque())
            System.out.print(t + "  ");

        System.out.println();
        queue.shuffle();

        for(TestQueuable<Integer> t: queue.getDeque())
            System.out.print(t + "  ");

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

        for(TestQueuable<Integer> t: queue.getDeque())
            System.out.print(t + "  ");

        System.out.println();
        queue.moveItem(4,7);

        for(TestQueuable<Integer> t: queue.getDeque())
            System.out.print(t + "  ");
    }
}
