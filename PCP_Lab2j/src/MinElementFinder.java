import java.util.Random;
import java.util.Scanner;

public class MinElementFinder {

    private static int[] array;
    private static int globalMin = Integer.MAX_VALUE;
    private static int globalMinIndex = -1;
    private static final Object lockObj = new Object();
    private static int finishedThreads = 0;

    public static void main(String[] args) {
        int arraySize = 1000000;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of worker threads: ");
        int totalThreads = scanner.nextInt();
        scanner.close();

        array = generateArray(arraySize);

        int chunkSize = arraySize / totalThreads;

        for (int i = 0; i < totalThreads; i++) {
            int start = i * chunkSize;
            int end = (i == totalThreads - 1) ? arraySize : start + chunkSize;

            Thread thread = new Thread(() -> findLocalMin(start, end));
            thread.start();
        }

        while (true) {
            synchronized (lockObj) {
                if (finishedThreads == totalThreads) {
                    break;
                }
            }
        }

        System.out.println("Minimal element: " + globalMin + ", index: " + globalMinIndex);
    }

    private static int[] generateArray(int size) {
        Random rand = new Random();
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = rand.nextInt(9999) + 1;
        }

        int negativeIndex = rand.nextInt(size);
        arr[negativeIndex] = -37;
        return arr;
    }

    private static void findLocalMin(int start, int end) {
        int localMin = Integer.MAX_VALUE;
        int localMinIndex = -1;

        for (int i = start; i < end; i++) {
            if (array[i] < localMin) {
                localMin = array[i];
                localMinIndex = i;
            }
        }

        synchronized (lockObj) {
            if (localMin < globalMin) {
                globalMin = localMin;
                globalMinIndex = localMinIndex;
            }
            finishedThreads++;
        }
    }
}
