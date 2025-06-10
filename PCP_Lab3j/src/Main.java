import java.util.*;
import java.util.concurrent.Semaphore;

public class Main {
    private static Queue<Integer> buffer = new LinkedList<>();
    private static int bufferCapacity;

    private static Semaphore access;
    private static Semaphore full;
    private static Semaphore empty;

    private static int totalProduced = 0;
    private static int totalConsumed = 0;

    static class Producer implements Runnable {
        private int id;
        private int toProduce;

        public Producer(int id, int toProduce) {
            this.id = id;
            this.toProduce = toProduce;
            System.out.println("[Створено Виробника " + id + "] Має виробити " + toProduce + " одиниць продукції");
        }

        @Override
        public void run() {
            Random rand = new Random();
            for (int i = 0; i < toProduce; i++) {
                try {
                    empty.acquire();
                    access.acquire();
                    buffer.add(i);
                    totalProduced++;
                    System.out.println("[Виробник " + id + "] Виробив продукцію " + i +
                            " (Загалом вироблено: " + totalProduced + ")");
                    access.release();
                    full.release();
                    Thread.sleep(rand.nextInt(300) + 50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    static class Consumer implements Runnable {
        private int id;
        private int toConsume;

        public Consumer(int id, int toConsume) {
            this.id = id;
            this.toConsume = toConsume;
            System.out.println("\t[Створено Споживача " + id + "] Має спожити " + toConsume + " одиниць продукції");
        }

        @Override
        public void run() {
            Random rand = new Random();
            for (int i = 0; i < toConsume; i++) {
                try {
                    full.acquire();
                    access.acquire();
                    int item = buffer.remove();
                    totalConsumed++;
                    System.out.println("\t[Споживач " + id + "] Спожив продукцію " + item +
                            " (Загалом спожито: " + totalConsumed + ")");
                    access.release();
                    empty.release();
                    Thread.sleep(rand.nextInt(300) + 80);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Місткість сховища: ");
        bufferCapacity = scanner.nextInt();

        System.out.print("Кількість продукції: ");
        int totalProduction = scanner.nextInt();

        System.out.print("Кількість виробників: ");
        int producerCount = scanner.nextInt();

        System.out.print("Кількість споживачів: ");
        int consumerCount = scanner.nextInt();

        int[] producerShares = new int[producerCount];
        int[] consumerShares = new int[consumerCount];

        int baseProd = totalProduction / producerCount;
        int baseCons = totalProduction / consumerCount;

        for (int i = 0; i < producerCount; i++) {
            producerShares[i] = (i == producerCount - 1)
                    ? totalProduction - baseProd * (producerCount - 1)
                    : baseProd;
        }

        for (int i = 0; i < consumerCount; i++) {
            consumerShares[i] = (i == consumerCount - 1)
                    ? totalProduction - baseCons * (consumerCount - 1)
                    : baseCons;
        }

        access = new Semaphore(1);
        full = new Semaphore(0);
        empty = new Semaphore(bufferCapacity);

        Thread[] threads = new Thread[producerCount + consumerCount];

        for (int i = 0; i < producerCount; i++) {
            Producer producer = new Producer(i + 1, producerShares[i]);
            threads[i] = new Thread(producer);
            threads[i].start();
        }

        for (int i = 0; i < consumerCount; i++) {
            Consumer consumer = new Consumer(i + 1, consumerShares[i]);
            threads[producerCount + i] = new Thread(consumer);
            threads[producerCount + i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\nВся продукція вироблена і спожита");
    }
}
