package com.example.demo.Assignment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        Path inpFile = Paths.get("src/main/java/com/example/demo/Assignment/inp.txt");

        try {
            List<String> lines = Files.readAllLines(inpFile);
            String logLevel = lines.get(0);
            String timeStamp = lines.get(1);
            String service = lines.get(2);
            String message = lines.get(3);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
