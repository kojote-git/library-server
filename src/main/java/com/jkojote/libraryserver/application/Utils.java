package com.jkojote.libraryserver.application;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class Utils {

    public static String readFile(File file, boolean trim)
    throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String str;
            while ((str = reader.readLine()) != null) {
                if (trim)
                    str = str.trim();
                sb.append(str).append('\n');
            }
            return sb.toString();
        }
    }

    public static List<String> readLines(File file, boolean trim)
    throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String str;
            while ((str = reader.readLine()) != null) {
                if (trim)
                    str = str.trim();
                lines.add(str);
            }
        }
        return lines;
    }
}
