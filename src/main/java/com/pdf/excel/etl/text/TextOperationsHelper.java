package com.pdf.excel.etl.text;

import technology.tabula.RectangularTextContainer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Cosmin Chiriac
 * Date: 2025-03-01
 */
public class TextOperationsHelper {
  public void skipDuplicatedPenultimateRow(StringBuilder text) {
    String duplicatePenultimatePipeRowPattern = ".*\\|.*\\|.*\\|.*\\|.*\\|.*\\|.*\\||.*\\|";
    if (text.toString().matches(duplicatePenultimatePipeRowPattern)) {
      String textInput = text.toString().replaceAll("\\|+", "|");
      text.setLength(0);
      text.append(textInput);
    }
  }

  public String normalizeLineBreaksAndJoinWithPipeDelimiter(RectangularTextContainer content) {
    // Note: Cell.getText() uses \r to concat text chunks
    return String.join("", content.getText().replace("\r", " "), "|");
  }

  public String replaceDiacritics(String fileContent) {
    fileContent = fileContent.replace("ă", "a");
    fileContent = fileContent.replace("â", "a");
    fileContent = fileContent.replace("î", "i");
    fileContent = fileContent.replace("ș", "s");
    fileContent = fileContent.replace("ş", "s");
    fileContent = fileContent.replace("ț", "t");
    fileContent = fileContent.replace("ţ", "t");
    fileContent = fileContent.replace("á", "a");
    fileContent = fileContent.replace("é", "e");

    fileContent = fileContent.replace("Ă", "A");
    fileContent = fileContent.replace("Â", "A");
    fileContent = fileContent.replace("Î", "I");
    fileContent = fileContent.replace("Ș", "S");
    fileContent = fileContent.replace("Ț", "T");
    fileContent = fileContent.replace("Ţ", "T");
    fileContent = fileContent.replace("Á", "A");
    fileContent = fileContent.replace("É", "E");
    return fileContent;
  }

  public String extractValue(String input, String pattern) {
    Pattern regex = Pattern.compile(pattern);
    Matcher matcher = regex.matcher(input);

    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return null; // Pattern not found
    }
  }
}
