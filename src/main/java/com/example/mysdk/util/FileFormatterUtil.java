package com.example.mysdk.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileFormatterUtil {
    private static final String TAG = "FileFormatterUtil";

    /**
     * 將 CSV 或 TSV 檔案轉換為 JSON 格式的字串，並可選擇是否寫入檔案
     * @param inputFilePath 輸入檔案路徑
     * @param writeToFile 是否寫入檔案
     * @param outputFilePath 輸出檔案路徑
     * @param outputFileName 輸出檔案名稱
     * @param listener 進度監聽器，可為 null
     * @return JSON 字串
     */
    public static String convertToJson(String inputFilePath, boolean writeToFile, String outputFilePath, String outputFileName, OnProgressListener listener) {
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists() || !inputFile.canRead()) {
            Log.w(TAG, "convertToJson: input file not exists or can't read");
            return "[]";
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            List<String> lines = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }

            reader.close();

            if (lines.isEmpty()) {
                return "[]";
            }

            Log.d(TAG, "convertToJson: Starting mission...");
            String delimiter = "[,\\t ]+";
            String[] headers = lines.get(0).split(delimiter);
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim();
            }

            List<Map<String, String>> data = new ArrayList<>();
            int total = lines.size() - 1;
            for (int i = 1; i < lines.size(); i++) {
                String[] values = lines.get(i).split(delimiter);
                if (values.length == headers.length) {
                    Map<String, String> row = new HashMap<>();
                    for (int j = 0; j < headers.length; j++) {
                        row.put(headers[j], values[j].trim());
                    }
                    data.add(row);
                }
                if (listener != null) {
                    listener.onProgress(i, total);
                }
            }

            StringBuilder jsonBuilder = new StringBuilder("[");
            for (int i = 0; i < data.size(); i++) {
                Map<String, String> row = data.get(i);
                jsonBuilder.append("{");
                int j = 0;
                for (Map.Entry<String, String> entry : row.entrySet()) {
                    jsonBuilder.append("\"").append(escapeJson(entry.getKey())).append("\":\"").append(escapeJson(entry.getValue())).append("\"");
                    if(j<row.size()-1){
                        jsonBuilder.append(",");
                    }
                    j++;
                }
                jsonBuilder.append("}");
                if (i < data.size() - 1) {
                    jsonBuilder.append(",");
                }
            }
            jsonBuilder.append("]");
            String jsonString = jsonBuilder.toString();

            if (writeToFile) {
                if (outputFilePath == null || outputFilePath.isEmpty() || outputFileName == null || outputFileName.isEmpty()) {
                    Log.e(TAG, "Output path or file name is missing. If you want to write to a file, please provide this information");
                    return jsonString;
                }

                File outputDir = new File(outputFilePath);
                if (!outputDir.exists()) {
                    if (!outputDir.mkdirs()) {
                        Log.e(TAG, "Failed to create output directory: " + outputFilePath);
                        return jsonString;
                    }
                }

                File outputFile = new File(outputDir, outputFileName);
                FileWriter writer = new FileWriter(outputFile);
                writer.write(jsonString);
                writer.close();
                Log.i(TAG, "JSON data written to file: " + outputFile.getAbsolutePath());
            }
            return jsonString;
        } catch (IOException ex) {
            Log.e(TAG, "Error processing file: " + ex.getMessage());
            return "[]";
        }
    }

    public interface OnProgressListener {
        void onProgress(int current, int total);
    }

    private static String escapeJson(String input) {
        return input.replace("\"", "\\\"");
    }
}
