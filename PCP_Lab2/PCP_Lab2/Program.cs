using System;
using System.Threading;

class Program
{
    static int[] array;
    static int globalMin = int.MaxValue;
    static int globalMinIndex = -1;
    static readonly object lockObj = new object();
    static int finishedThreads = 0;

    static void Main()
    {
        int arraySize = 1000000;
        int totalThreads;
        Console.Write("Enter the number of worker threads: ");
        totalThreads = int.Parse(Console.ReadLine());

        array = GenerateArray(arraySize);

        int chunkSize = arraySize / totalThreads;

        for (int i = 0; i < totalThreads; i++)
        {
            int start = i * chunkSize;
            int end = (i == totalThreads - 1) ? arraySize : start + chunkSize;

            Thread thread = new Thread(() => FindLocalMin(start, end));
            thread.Start();
        }

        while (true)
        {
            lock (lockObj)
            {
                if (finishedThreads == totalThreads)
                    break;
            }

        }

        Console.WriteLine($"Minimal element: {globalMin}, index: {globalMinIndex}");
    }

    static int[] GenerateArray(int size)
    {
        Random rand = new Random();
        int[] arr = new int[size];
        for (int i = 0; i < size; i++)
            arr[i] = rand.Next(1, 10000);

        int negativeIndex = rand.Next(size);
        arr[negativeIndex] = -37;
        return arr;
    }

    static void FindLocalMin(int start, int end)
    {
        int localMin = int.MaxValue;
        int localMinIndex = -1;

        for (int i = start; i < end; i++)
        {
            if (array[i] < localMin)
            {
                localMin = array[i];
                localMinIndex = i;
            }
        }

        lock (lockObj)
        {
            if (localMin < globalMin)
            {
                globalMin = localMin;
                globalMinIndex = localMinIndex;
            }

            finishedThreads++;
        }
    }
}

