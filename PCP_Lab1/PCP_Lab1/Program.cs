using System;
using System.Threading;

namespace ThreadedSequenceSum
{
    class Program
    {
        private int threadCount;
        private int step = 2;

        private Thread[] threads;
        private bool[] canStop;
        private int stopDelay;
        private ManualResetEventSlim[] startedEvents;

        static void Main(string[] args)
        {
            new Program().Start();
        }

        void Start()
        {
            Console.Write("Enter the number of worker threads: ");
            threadCount = int.Parse(Console.ReadLine());

            Console.Write("Enter stop delay (in seconds): ");
            stopDelay = int.Parse(Console.ReadLine());

            threads = new Thread[threadCount];
            canStop = new bool[threadCount];
            startedEvents = new ManualResetEventSlim[threadCount];

            for (int i = 0; i < threadCount; i++)
            {
                canStop[i] = false;
                startedEvents[i] = new ManualResetEventSlim(false);

                int threadId = i;
                threads[i] = new Thread(() => Calculator(threadId));
                threads[i].Start();
            }

            Thread controllerThread = new Thread(Controller);
            controllerThread.Start();
        }

        void Calculator(int threadId)
        {
            long sum = 0;
            long current = 0;
            int count = 0;

            startedEvents[threadId].Set();

            while (!canStop[threadId])
            {
                sum += current;
                current += step;
                count++;
            }

            Console.WriteLine($"Thread {threadId}: Sum = {sum}, Elements = {count}");
        }

        void Controller()
        {

            new Timer(_ =>
            {
                for (int i = 0; i < threadCount; i++)
                {
                    canStop[i] = true;
                }
            }, null, stopDelay * 1000, Timeout.Infinite);
        }

    }
}
