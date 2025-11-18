package task2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class FileSearchTask extends RecursiveAction {
    private final File directory;
    private final String extension;
    private final List<String> results;

    public FileSearchTask(File directory, String extension, List<String> results) {
        this.directory = directory;
        this.extension = extension;
        this.results = results;
    }

    // конструктор

    @Override
    protected void compute() {
        File[] files = directory.listFiles();
        if (files == null) return;

        List<FileSearchTask> subTasks = new ArrayList<>();

        for (File file : files) {
            if (file.isDirectory()) {
                // создать подзадачу для директории
                FileSearchTask task = new FileSearchTask(file, extension, results);
                subTasks.add(task);
                task.fork();
            } else if (file.getName().endsWith(extension)) {
                    results.add(file.getAbsolutePath());
            }
        }

        // дождаться завершения всех подзадач
        for (FileSearchTask task : subTasks) {
            task.join();
        }
    }

    private static void createTestDirectory(String rootPath) throws IOException {
        File root = new File(rootPath);
        root.mkdirs();

        new File(root, "file1.txt").createNewFile();
        new File(root, "file2.java").createNewFile();

        File sub1 = new File(root, "subdir1");
        sub1.mkdir();
        new File(sub1, "doc1.txt").createNewFile();

        File sub2 = new File(sub1, "subdir2");
        sub2.mkdir();
        new File(sub2, "notes.txt").createNewFile();
        new File(sub2, "image.png").createNewFile();

        File sub3 = new File(root, "subdir3");
        sub3.mkdir();
        new File(sub3, "data.txt").createNewFile();
    }

    public static void main(String[] args) {

        System.out.println("=== Параллельный поиск файлов ===");

        String rootPath = "test_directory";
        String ext = ".txt";

        // потокобезопасный список
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        try {
            createTestDirectory(rootPath);
        } catch (IOException e) {
            System.out.println("Ошибка создания структуры директорий: " + e.getMessage());
            return;
        }

        File rootDir = new File(rootPath);

        long start = System.currentTimeMillis();

        ForkJoinPool pool = ForkJoinPool.commonPool();
        FileSearchTask searchTask = new FileSearchTask(rootDir, ext, results);
        pool.invoke(searchTask);

        long time = System.currentTimeMillis() - start;

        System.out.println("\nКорневая директория: " + rootPath);
        System.out.println("Искомое расширение: " + ext);
        System.out.println("\nНайденные файлы:");

        int index = 1;
        for (String file : results) {
            System.out.println(index++ + ". " + file);
        }

        System.out.println("\nВсего найдено: " + results.size() + " файлов");
        System.out.println("Время выполнения: " + time + " мс");
    }
}
