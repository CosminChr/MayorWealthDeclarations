package com.pdf.excel.etl;

import com.pdf.excel.etl.model.Owner;
import com.pdf.excel.etl.logger.ApplicationLogger;
import com.pdf.excel.etl.file.CsvFilesGenerator;
import com.pdf.excel.etl.file.FileSystemHelper;
import com.pdf.excel.etl.owner.OwnerAssetsProcessor;
import com.pdf.excel.etl.owner.OwnerProcessor;
import com.pdf.excel.etl.text.TextOperationsHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Author: Cosmin Chiriac
 * Date: 2025-03-01
 */
public class Main {

  public static void main(String[] args) throws InterruptedException {
  try {
    final CsvFilesGenerator csvFilesGenerator = new CsvFilesGenerator();
    final ApplicationLogger logger = new ApplicationLogger();
    final FileSystemHelper fileSystemHelper = new FileSystemHelper();
    final TextOperationsHelper textOperationsHelper = new TextOperationsHelper();
    final OwnerAssetsProcessor ownerAssetsProcessor = new OwnerAssetsProcessor(textOperationsHelper);
    final OwnerProcessor ownerProcessor = new OwnerProcessor(fileSystemHelper, textOperationsHelper, ownerAssetsProcessor);

    final LocalDateTime startTime = logger.logProcessingStartTime();
    Path path = fileSystemHelper.getResourceDirectoryPath(args);
    try (Stream<Path> filesStream = Files.walk(path)) {
      final List<String> pdfFileNames = fileSystemHelper.getExistingPdfFileNamesInResourceDirectory(filesStream);
      logger.logTheNumberOfFilesFound(pdfFileNames);
      // the list of mayors
      final Set<Owner> owners = ownerProcessor.processOwnersAndTheirAssets(pdfFileNames);
      logger.logCsvFilesProcessingStart();
      csvFilesGenerator.generateCsvFiles(owners);
    }
    logger.logExecutionDuration(startTime);
    } catch (Exception ex) {
      System.out.println(ex);
      Thread.sleep(10000);
    }
  }
}