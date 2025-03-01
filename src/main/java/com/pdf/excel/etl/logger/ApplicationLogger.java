package com.pdf.excel.etl.logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Author: Cosmin Chiriac
 * Date: 2025-03-01
 */
public class ApplicationLogger {

  public void logExecutionDuration(LocalDateTime startTime) throws InterruptedException {
    LocalDateTime endTime = LocalDateTime.now();
    System.out.println(String.join("", "Processing started at: ", startTime.toString()));
    System.out.println(String.join("", "Processing finished at: ", endTime.toString()));
    final Duration duration = Duration.between(startTime, LocalDateTime.now());
    final long hours = duration.toHours();
    final long minutes = duration.toMinutes() % 60;
    System.out.println("Processing took : " + hours + " hours, " + minutes + " minutes and " + duration.toSeconds() + " seconds");
    Thread.sleep(10000);
  }

  public void logCsvFilesProcessingStart() {
    System.out.printf("Finished processing pdf files.%nGenerating csv files...");
  }

  public LocalDateTime logProcessingStartTime() {
    final LocalDateTime startTime = LocalDateTime.now();
    System.out.println(String.join("", "Processing started at: ", startTime.toString()));
    return startTime;
  }

  public void logTheNumberOfFilesFound(List<String> pdfFileNames) {
    if (pdfFileNames.size() == 1) {
      System.out.printf("Found %d pdf file to process%n", pdfFileNames.size());
    } else {
      System.out.printf("Found %d pdf files to process%n", pdfFileNames.size());
    }
  }
}
