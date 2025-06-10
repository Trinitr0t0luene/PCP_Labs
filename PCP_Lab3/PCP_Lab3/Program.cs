using System;
using System.Collections.Generic;
using System.Threading;

class Program
{
    static Queue<int> buffer = new Queue<int>();
    static int bufferCapacity;

    private static Semaphore Access;
    private static Semaphore Full;
    private static Semaphore Empty;

    static int totalProduced = 0;
    static int totalConsumed = 0;

    class Producer
    {
        private int id;
        private int toProduce;

        public Producer(int id, int toProduce)
        {
            this.id = id;
            this.toProduce = toProduce;
            Console.WriteLine($"[Створено Виробника {id}] Має виробити {toProduce} одиниць продукції");
        }

        public void Produce()
        {
            for (int i = 0; i < toProduce; i++)
            {
                Empty.WaitOne();
                Access.WaitOne();
                buffer.Enqueue(i);
                totalProduced++;
                Console.WriteLine($"[Виробник {id}] Виробив продукцію {i} (Загалом вироблено: {totalProduced})");
                Access.Release();
                Full.Release();
                Thread.Sleep(new Random().Next(50, 350));
            }
        }
    }

    class Consumer
    {
        private int id;
        private int toConsume;

        public Consumer(int id, int toConsume)
        {
            this.id = id;
            this.toConsume = toConsume;
            Console.WriteLine($"\t[Створено Споживача {id}] Має спожити {toConsume} одиниць продукції");
        }

        public void Consume()
        {
            for (int i = 0; i < toConsume; i++)
            {
                Full.WaitOne();
                Access.WaitOne();
                int item = buffer.Dequeue();
                totalConsumed++;
                Console.WriteLine($"\t[Споживач {id}] Спожив продукцію {item} (Загалом спожито: {totalConsumed})");
                Access.Release();
                Empty.Release();
                Thread.Sleep(new Random().Next(80, 380));
            }
        }
    }

    static void Main()
    {
        Console.Write("Місткість сховища: ");
        bufferCapacity = int.Parse(Console.ReadLine());

        Console.Write("Кількість продукції: ");
        int totalProduction = int.Parse(Console.ReadLine());

        Console.Write("Кількість виробників: ");
        int producerCount = int.Parse(Console.ReadLine());

        Console.Write("Кількість споживачів: ");
        int consumerCount = int.Parse(Console.ReadLine());

        int[] producerShares = new int[producerCount];
        int[] consumerShares = new int[consumerCount];

        int baseProd = totalProduction / producerCount;
        int baseCons = totalProduction / consumerCount;

        for (int i = 0; i < producerCount; i++)
            producerShares[i] = (i == producerCount - 1) ? totalProduction - baseProd * (producerCount - 1) : baseProd;

        for (int i = 0; i < consumerCount; i++)
            consumerShares[i] = (i == consumerCount - 1) ? totalProduction - baseCons * (consumerCount - 1) : baseCons;

        Empty = new Semaphore(bufferCapacity, bufferCapacity);
        Full = new Semaphore(0, bufferCapacity);
        Access = new Semaphore(1, 1);

        Producer[] producers = new Producer[producerCount];
        Consumer[] consumers = new Consumer[consumerCount];
        Thread[] threads = new Thread[producerCount + consumerCount];

        for (int i = 0; i < producerCount; i++)
        {
            producers[i] = new Producer(i + 1, producerShares[i]);
            threads[i] = new Thread(producers[i].Produce);
            threads[i].Start();
        }

        for (int i = 0; i < consumerCount; i++)
        {
            consumers[i] = new Consumer(i + 1, consumerShares[i]);
            threads[producerCount + i] = new Thread(consumers[i].Consume);
            threads[producerCount + i].Start();
        }

        foreach (var t in threads)
            t.Join();

        Console.WriteLine("\nВся продукція вироблена і спожита");
    }
}
