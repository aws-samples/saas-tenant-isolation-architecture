package tenant.vendinglayer.template;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PolicyLoader {

    private static Stream<File> streamFiles(File file) {
        return file.isDirectory() ?
            Arrays.stream(Objects.requireNonNull(file.listFiles()))
                .flatMap(PolicyLoader::streamFiles) : Arrays.stream(new File[]{file});
    }

    private static String stringifyFile(File file) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str).append("\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return sb.toString();
    }

    public static String assemblePolicyTemplates(File directory) {
        return PolicyLoader.streamFiles(directory)
            .filter(file -> file.getName().toLowerCase().endsWith(".json"))
            .map(PolicyLoader::stringifyFile)
            .collect(Collectors.joining(","));
    }

}
