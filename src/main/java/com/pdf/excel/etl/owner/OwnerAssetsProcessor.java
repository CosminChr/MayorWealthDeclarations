package com.pdf.excel.etl.owner;

import com.pdf.excel.etl.text.TextOperationsHelper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OwnerAssetsProcessor {

  private final TextOperationsHelper textOperationsHelper;

  public OwnerAssetsProcessor(TextOperationsHelper textOperationsHelper) {
    this.textOperationsHelper = textOperationsHelper;
  }

  public String buildOffice(String[] wordsAfterUndersigned) {
    final StringBuilder officeBuilder = new StringBuilder();
    for (int wordIndex = 8; wordIndex < wordsAfterUndersigned.length; wordIndex++) {
      String word = wordsAfterUndersigned[wordIndex];
      if (word.equals(",")) {
        break;
      }
      officeBuilder.append(wordsAfterUndersigned[wordIndex]);
      if (word.endsWith(",")) {
        break;
      }
      officeBuilder.append(" ");
    }
    officeBuilder.deleteCharAt(officeBuilder.length() - 1);
    return officeBuilder.toString();
  }

  public Matcher buildOwnerMatcher(PDDocument document) throws IOException {
    final PDFTextStripper pdfTextStripper = new PDFTextStripper();
    final String fileContent = textOperationsHelper.replaceDiacritics(pdfTextStripper.getText(document));
    final Pattern undersignedPattern = Pattern.compile("Subsemnat\\w*\\s+(.*)");
    return undersignedPattern.matcher(fileContent);
  }
}
