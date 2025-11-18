package task3;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class MergeSortTask extends RecursiveTask<int[]> {
    private final int[] array;
    private static final int THRESHOLD = 10_000;

    public MergeSortTask(int[] array) {
        this.array = array;
    }

    // конструктор

    @Override
    protected int[] compute() {
        if (array.length <= THRESHOLD) {
            // использовать стандартную сортировку для малых массивов
            int[] sorted = array.clone();
            Arrays.sort(sorted);
            return sorted;
        } else {
            // разделить массив
            int mid = array.length / 2;
            int[] left = Arrays.copyOfRange(array, 0, mid);
            int[] right = Arrays.copyOfRange(array, mid, array.length);

            // создать подзадачи
            MergeSortTask leftTask = new MergeSortTask(left);
            MergeSortTask rightTask = new MergeSortTask(right);

            leftTask.fork();
            int[] rightResult = rightTask.compute();
            int[] leftResult = leftTask.join();

            // слить результаты
            return merge(leftResult, rightResult);
        }
    }

    private int[] merge(int[] left, int[] right) {
        int[] result = new int[left.length + right.length];
        int i = 0, j = 0, k = 0;

        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) {
                result[k++] = left[i++];
            } else {
                result[k++] = right[j++];
            }
        }

        while (i < left.length)
            result[k++] = left[i++];

        while (j < right.length)
            result[k++] = right[j++];

        return result;
    }

    public static void main(String[] args) {

        System.out.println("=== Параллельная сортировка слиянием ===");

        int size = 1_000_000;
        int[] array = new int[size];
        Random rnd = new Random();

        for (int i = 0; i < size; i++)
            array[i] = rnd.nextInt(1_000_000);

        System.out.println("Размер массива: " + size);
        System.out.println("Порог разделения: " + THRESHOLD);

        System.out.println("\nГенерация случайного массива...");
        System.out.println("Первые 10 элементов до сортировки:");
        System.out.println(Arrays.toString(Arrays.copyOf(array, 10)));

        //  стандартная сортировка
        int[] arrStd = array.clone();
        long startStd = System.currentTimeMillis();
        Arrays.sort(arrStd);
        long stdTime = System.currentTimeMillis() - startStd;

        System.out.println("\nСтандартная сортировка (Arrays.sort):");
        System.out.println("Время выполнения: " + stdTime + " мс");
        System.out.println("Первые 10 элементов: " +
                Arrays.toString(Arrays.copyOf(arrStd, 10)));

        // параллельная сортировка
        int[] arrPar = array.clone();
        long startPar = System.currentTimeMillis();

        ForkJoinPool pool = ForkJoinPool.commonPool();
        MergeSortTask task = new MergeSortTask(arrPar);
        arrPar = pool.invoke(task);

        long parTime = System.currentTimeMillis() - startPar;

        System.out.println("\nПараллельная сортировка (Fork/Join):");
        System.out.println("Время выполнения: " + parTime + " мс");
        System.out.println("Первые 10 элементов: " +
                Arrays.toString(Arrays.copyOf(arrPar, 10)));

        //  проверка корректности
        boolean correct = Arrays.equals(arrStd, arrPar);
        System.out.println("\nПроверка корректности: " + (correct ? "Массив отсортирован правильно" : "✗ Ошибка!"));

        double speedup = (double) stdTime / parTime;
        System.out.println("Ускорение: " + String.format("%.2f", speedup) + "x");
    }
}
