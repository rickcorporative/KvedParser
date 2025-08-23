package com.demo.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * RFC-4180 CSV writer for Google Sheets import:
 * - delimiter: comma (,)
 * - quotes: only when needed (commas, quotes, CR/LF inside cell)
 * - escaping: double quotes => ""
 * - sanitization: remove tabs/CR/LF to keep flat rows (optional but safer)
 */
public class CsvWriter implements Closeable {
    private static final char DELIM = ',';            // запятая — как в твоём удачном примере
    private static final boolean ALWAYS_QUOTE = false; // кавычки только при необходимости

    private final Writer out;

    public CsvWriter(String path, boolean append, boolean writeHeaders) throws IOException {
        boolean fileExists = Files.exists(Paths.get(path));

        // Если хочешь, можно добавить BOM в самый-самый начало первого файла (некоторые Excel помогают лучше понимать UTF-8)
        // if (!fileExists && !append) { ... }

        this.out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path, append),
                StandardCharsets.UTF_8
        ));

        // Пиши заголовки только если файл новый или если явно попросили
        if (!fileExists || writeHeaders) {
            writeRow("Full Name", "Short Name", "Manager", "Phones", "EDRPOU", "Location", "URL");
            flush();
        }
    }

    /** Принудительная запись буфера на диск */
    public void flush() throws IOException {
        out.flush();
    }

    /** Запись строки CSV по RFC-4180 с запятой и условными кавычками */
    public void writeRow(String... cells) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) sb.append(DELIM);
            sb.append(csvEscape(cells[i]));
        }
        sb.append('\n');
        out.write(sb.toString());
    }

    // ---------- helpers ----------

    /**
     * Лёгкая «плоская» санитаризация: уберём табы/CR/LF и экзотические пробелы,
     * чтобы каждая запись была в одну строку.
     * (Если хочешь оставлять переносы внутри ячейки — убери .replace('\n',' ') и .replace('\r',' '))
     */
    private static String sanitize(String s) {
        if (s == null) return "";
        String x = s
                .replace('\u00A0',' ')  // NBSP
                .replace('\u2007',' ')  // FIGURE SPACE
                .replace('\u202F',' ')  // NNBSP
                .replace('\t',' ')
                .replace('\r',' ')
                .replace('\n',' ');
        // схлопнем множественные пробелы
        return x.replaceAll("\\s+", " ").trim();
    }

    /**
     * Экранируем по RFC-4180:
     * - если ячейка содержит запятую, кавычку или CR/LF — оборачиваем в кавычки
     * - внутренние кавычки удваиваем
     */
    private static String csvEscape(String s) {
        String v = sanitize(s);
        boolean needQuotes = v.indexOf(DELIM) >= 0 || v.contains("\"") || v.contains("\n") || v.contains("\r");
        v = v.replace("\"", "\"\""); // экранируем двойные кавычки
        if (ALWAYS_QUOTE || needQuotes) {
            return "\"" + v + "\"";
        }
        return v;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
