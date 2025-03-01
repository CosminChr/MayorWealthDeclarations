package com.pdf.excel.etl.file;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * Author: Cosmin Chiriac
 * Date: 2025-03-01
 */
public class FileSystemHelper {
  public File createPdfFile(String pdfFileName) {
    final File file = new File(pdfFileName);
    System.out.println("Processing file:" + pdfFileName);
    return file;
  }

  public boolean isPdfFile(File file) {
    long fileSizeInBytes = file.length();
    // Convert the size to kilobytes
    long fileSizeInKB = fileSizeInBytes / 1024;
    // all the pdf files surpass this threshold
    return fileSizeInKB < 105;
  }

  public List<String> getExistingPdfFileNamesInResourceDirectory(Stream<Path> filesStream) {
    return filesStream
        .filter(p -> !Files.isDirectory(p))
        .map(Path::toString)
        // DS_Store file (short for "Desktop Services Store") is a hidden system file created by macOS in folders when viewed through Finder.
        .filter(string -> !string.contains(".DS_Store"))
        .filter(string -> string.contains(".pdf"))
        .toList();
  }

  public Path getResourceDirectoryPath(String[] args) {
    Path path;
    if (args != null && args.length != 0) {
      path = Paths.get(args[0]);
    } else {
      path = Paths.get("resources").toAbsolutePath();
    }
    return path;
  }
}
