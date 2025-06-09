import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CountDownLatch;

public class Main {

    private int threadCount;
    private final int step = 2;

    private Thread[] threads;
    private AtomicBoolean[] canStop;
    private int stopDelay;
    private CountDownLatch startedLatch;

    public static void main(String[] args) {
        new Main().start();
    }

    void start() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the number of worker threads: ");
        threadCount = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter stop delay (in seconds): ");
        stopDelay = Integer.parseInt(scanner.nextLine());

        threads = new Thread[threadCount];
        canStop = new AtomicBoolean[threadCount];
        startedLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            canStop[i] = new AtomicBoolean(false);
            int threadId = i;
            threads[i] = new Thread(() -> calculator(threadId));
            threads[i].start();
        }

        // Wait for all threads to start
        try {
            startedLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Start controller
        Thread controllerThread = new Thread(this::controller);
        controllerThread.start();
    }

    void calculator(int threadId) {
        long sum = 0;
        long current = 0;
        int count = 0;

        // Signal that this thread has started
        startedLatch.countDown();

        while (!canStop[threadId].get()) {
            sum += current;
            current += step;
            count++;
        }

        System.out.printf("Thread %d: Sum = %d, Elements = %d%n", threadId, sum, count);
    }

    void controller() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (AtomicBoolean stopFlag : canStop) {
                    stopFlag.set(true);
                }
            }
        }, stopDelay * 1000L);
    }
}
