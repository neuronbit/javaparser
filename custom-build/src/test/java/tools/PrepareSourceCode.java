package tools;

import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class PrepareSourceCode {
    public static void main(String[] args) throws IOException {
        Path srcDir = Paths.get("/Users/shunyun/workspace/java/javaparser");
        final File destDir = new File("/Users/shunyun/workspace/java/javaparser-build");
        System.out.println(srcDir.toAbsolutePath() +":" + srcDir.toFile().exists());
        System.out.println(destDir.getCanonicalPath()+":" + destDir.exists());
        final String javaparserVersionNumber = "3.26.0";
        final String neuronbitJavaparserVersionNumber = "3.26.0";
        try (ProgressBar pb = new ProgressBar("Prepare Source Code", 6000)) {
            if (destDir.exists()) {
                pb.setExtraMessage("deleting " + destDir.getAbsolutePath());
                FileUtils.deleteDirectory(destDir);
                pb.stepBy(10);
            }
            pb.setExtraMessage("copying "+ srcDir.toAbsolutePath() + " to " + destDir.getAbsolutePath());
            FileUtils.copyDirectory(srcDir.toFile(), destDir);
            pb.stepBy(10);
            int cores = Runtime.getRuntime().availableProcessors();
            final ExecutorService executorService = Executors.newFixedThreadPool(cores * 4);
            pb.setExtraMessage("rewriting files " + destDir.getAbsolutePath());
            Files.walk(destDir.toPath())
                    .filter(path -> !path.getFileName().startsWith("."))
                    .filter(path -> path.toFile().isFile())
                    .forEach(path -> {
                            final File file = path.toFile();
                            final File newFile = new File(file.getParentFile(), file.getName() + ".new");
                            try {
                                BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
                                LineNumberReader reader = new LineNumberReader(new FileReader(file));
                                String line;
                                String previousLine = null;
                                while ((line = reader.readLine()) != null) {
                                    if (line.contains("com.github")) {
                                        line = line.replace("com.github", "com.neuronbit");
                                    }
                                    if (line.contains(javaparserVersionNumber) && previousLine != null && (previousLine.contains("javaparser") || previousLine.contains("pom"))) {
                                        line = line.replace(javaparserVersionNumber, neuronbitJavaparserVersionNumber);
                                    }
                                    writer.write(line);
                                    writer.write(System.lineSeparator());
                                    previousLine = line;
                                }
                                reader.close();
                                writer.close();
                                file.delete();
                                newFile.renameTo(file);
                                pb.stepBy(1);
                            } catch (IOException e) {
                                System.err.printf("process file %s failed%n", file.getAbsolutePath());
                            }
                         ;
                    });
            List<File> dirs = new ArrayList<>();
            pb.setExtraMessage("renaming packages " + destDir.getAbsolutePath());
            Files.walk(destDir.toPath())
                    .filter(path -> !path.getFileName().startsWith("."))
                    .filter(path -> path.toFile().isDirectory())
                    .forEach(path -> {
                        if (path.toFile().getName().equals("github")) {
                            if (path.getParent() != null && path.getParent().toFile().getName().equals("com")) {
                                dirs.add(path.toFile());
                            }
                        }
                    });

            for (File dir : dirs) {
                if (!dir.renameTo(dir.getParentFile().toPath().resolve("neuronbit").toFile())) {
                    System.err.printf("rename dir %s failed%n", dir.getAbsolutePath());
                }
                pb.stepBy(1);
            }
            executorService.shutdown();
        }
    }
}
