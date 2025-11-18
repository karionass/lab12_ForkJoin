package task1;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class ArraySumTask extends RecursiveTask<Long> {

    private final int[] array;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 100_000;

    public ArraySumTask(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    // конструктор
    @Override
    protected Long compute() {
        int length = end - start;

        if (length <= THRESHOLD) {
            // прямое вычисление
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        }
        else {
            // разделение на подзадачи
            int mid = start + length / 2;

            ArraySumTask leftTask = new ArraySumTask(array, start, mid);
            ArraySumTask rightTask = new ArraySumTask(array, mid, end);

            leftTask.fork(); // асинхронно

            long rightResult = rightTask.compute(); // выполняем здесь
            long leftResult = leftTask.join();     // ждём левую задачу

            return leftResult + rightResult;
        }
    }

    public static void main(String[] args) {

        System.out.println("=== Параллельное суммирование массива ===");
        int size = 10_000_000;

        int[] array = new int[size];
        for (int i = 0; i < size; i++)
            array[i] = i + 1;

        System.out.println("Размер массива: " + size);
        System.out.println("Порог разделения: " + THRESHOLD);

        // последовательное суммирование
        long startSeq = System.currentTimeMillis();

        long seqSum = 0;
        for (int value : array)
            seqSum += value;

        long seqTime = System.currentTimeMillis() - startSeq;

        System.out.println("\nПоследовательное суммирование:");
        System.out.println("Результат: " + seqSum);
        System.out.println("Время выполнения: " + seqTime + " мс");

        // параллельное суммирование
        long startPar = System.currentTimeMillis();

        ForkJoinPool pool = ForkJoinPool.commonPool();
        ArraySumTask task = new ArraySumTask(array, 0, array.length);
        long parSum = pool.invoke(task);

        long parTime = System.currentTimeMillis() - startPar;

        System.out.println("\nПараллельное суммирование (Fork/Join):");
        System.out.println("Результат: " + parSum);
        System.out.println("Время выполнения: " + parTime + " мс");

        double speedup = (double) seqTime / parTime;
        System.out.println("\nУскорение: " + String.format("%.2f", speedup) + "x");
    }
}
