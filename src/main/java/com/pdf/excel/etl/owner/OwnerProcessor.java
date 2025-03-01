package com.pdf.excel.etl.owner;

import com.pdf.excel.etl.file.FileSystemHelper;
import com.pdf.excel.etl.model.Building;
import com.pdf.excel.etl.model.CountyEnum;
import com.pdf.excel.etl.model.Land;
import com.pdf.excel.etl.model.Owner;
import com.pdf.excel.etl.model.Property;
import com.pdf.excel.etl.model.PropertyType;
import com.pdf.excel.etl.model.Vehicle;
import com.pdf.excel.etl.text.TextOperationsHelper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OwnerProcessor {

  private final FileSystemHelper fileSystemHelper;
  private final TextOperationsHelper textOperationsHelper;
  private final OwnerAssetsProcessor ownerAssetsProcessor;

  public OwnerProcessor(FileSystemHelper fileSystemHelper, TextOperationsHelper textOperationsHelper, OwnerAssetsProcessor ownerAssetsProcessor) {
    this.fileSystemHelper = fileSystemHelper;
    this.textOperationsHelper = textOperationsHelper;
    this.ownerAssetsProcessor = ownerAssetsProcessor;
  }

  public Set<Owner> processOwnersAndTheirAssets(List<String> pdfFileNames) throws IOException {
    final Set<Owner> owners = new LinkedHashSet<>();

    for (String pdfFileName : pdfFileNames) {
      final File file = fileSystemHelper.createPdfFile(pdfFileName);
      if (fileSystemHelper.isPdfFile(file)) {
        try (PDDocument document = Loader.loadPDF(file)) {

          final Owner owner = buildOwner(pdfFileName, document);

          final SpreadsheetExtractionAlgorithm spreadsheetExtractionAlgorithm = new SpreadsheetExtractionAlgorithm();
          final PageIterator pageIterator = new ObjectExtractor(document).extract();

          // this cursor is used to determine if the iteration is going through lands or buildings
          // for lands it's 0 and for buildings it's 1
          int propertyCursor = 0;

          // this cursor is used to determine if the processing of the current table is still is progress
          // for processed table it is 0, for in progress property it is 1
          int tableProcessingCursor = 0;
          Matcher matcher;
          do {
            // iterate over the pages of the document
            final Page page = pageIterator.next();
            final List<Table> tables = spreadsheetExtractionAlgorithm.extract(page);
            // iterate over the tables of the page
            for (Table table : tables) {
              final List<List<RectangularTextContainer>> rows = table.getRows();
              // iterate over the rows of the table
              for (List<RectangularTextContainer> cells : rows) {
                final StringBuilder text = new StringBuilder();
                // iterate over the cells of the row
                for (RectangularTextContainer content : cells) {
                  String cellContent = textOperationsHelper.normalizeLineBreaksAndJoinWithPipeDelimiter(content);
                  cellContent = textOperationsHelper.replaceDiacritics(cellContent);
                  propertyCursor = skipBuildingsTableHeader(cellContent, propertyCursor);
                  text.append(cellContent);

                  textOperationsHelper.skipDuplicatedPenultimateRow(text);

                  final Map<Character, Integer> characterFrequencies =
                      text.chars()
                          .mapToObj(c -> (char) c)
                          .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(c -> 1)));

                  // this is the pattern of a row of the tables found in the pdf files
                  String rowPattern = ".*\\|.*\\|.*\\|.*\\|.*\\|.*\\|.*\\|";

                  if (processAddress(text, characterFrequencies, propertyCursor, rowPattern, tableProcessingCursor)) {
                    tableProcessingCursor = processOwnerAddressAndUpdateProcessingCursor(text, characterFrequencies, tableProcessingCursor, owners, owner);
                  } else if (processBuildings(text, characterFrequencies, propertyCursor, rowPattern, tableProcessingCursor)) {
                    tableProcessingCursor = processOwnerBuildingsAndUpdateProcessingCursor(text, characterFrequencies, tableProcessingCursor, owners, owner);
                  } else if (processVehicles(text, characterFrequencies)) {
                    processOwnerVehicles(text, characterFrequencies, owner, owners);
                  }
                }
              }
            }
          } while (pageIterator.hasNext());
        }
      }
    }
    return owners;
  }

  public int skipBuildingsTableHeader(String cellContent, int propertyIndex) {
    if (cellContent.contains("Titularul2") && propertyIndex == 0) {
      propertyIndex++;
    }
    return propertyIndex;
  }

  public void processOwnerVehicles(StringBuilder text, Map<Character, Integer> characterFrequencies, Owner owner, Set<Owner> owners) {
    final String[] vehicleTokens = text.toString().split("\\|");
    if (!(characterFrequencies.get('|') == 5 && vehicleTokens[1].isEmpty())) {

      final Vehicle vehicle = new Vehicle();
      vehicle.setOwner(owner);
      vehicle.setType(vehicleTokens[0]);
      if (vehicle.getType().equals("Alt mijloc de")) {
        vehicle.setType("Alt mijloc de transport");
      }
      if (!vehicleTokens[1].isEmpty()) {
        vehicle.setBrand(vehicleTokens[1]);
        vehicle.setPieces(vehicleTokens[2]);
        vehicle.setProductionYear(vehicleTokens[3]);
        vehicle.setAcquisitionManner(vehicleTokens[4]);
        if (vehicle.getAcquisitionManner().equals("Contract de")) {
          vehicle.setAcquisitionManner("Contract de vanzare cumparare");
        }
        if (vehicle.getAcquisitionManner().equals("Contract de vanzare")) {
          vehicle.setAcquisitionManner("Contract de vanzare cumparare");
        }
      } else {
        vehicle.setBrand(vehicleTokens[2]);
        vehicle.setPieces(vehicleTokens[3]);
        vehicle.setProductionYear(vehicleTokens[4]);
        vehicle.setAcquisitionManner(vehicleTokens[5]);
        if (vehicle.getAcquisitionManner().equals("Contract de")) {
          vehicle.setAcquisitionManner("Contract de vanzare cumparare");
        }
      }
      owner.getVehicles().add(vehicle);
      owners.add(owner);
    }
  }

  public int processOwnerBuildingsAndUpdateProcessingCursor(StringBuilder text, Map<Character, Integer> characterFrequencies, int tableProcessingProgressIndex, Set<Owner> owners, Owner owner) {
    Matcher matcher;
    if ((text.toString().contains("Adresa") && characterFrequencies.get('|') == 1 && tableProcessingProgressIndex == 1)) {
      final Pattern pattern = Pattern.compile("(.+?)\\s*Adresa:");
      matcher = pattern.matcher(text.toString());
      if (matcher.find()) {
        String result = matcher.group(1);
        String[] split = result.split(" ");
        final StringBuilder newLocalityValue = new StringBuilder();
        for (int i = split.length - 2; i > 0; i--) {
          if (split[i].equals("Localitate:")) {
            break;
          }
          newLocalityValue.append(" ").append(split[i]);
        }
        String[] words = newLocalityValue.toString().split(" ");

        // Create a StringBuilder to build the reversed string
        StringBuilder reversedStringBuilder = new StringBuilder();

        // Iterate through the words in reverse order and append to the StringBuilder
        for (int i = words.length - 1; i >= 0; i--) {
          reversedStringBuilder.append(words[i]);
          if (i > 0) {
            reversedStringBuilder.append(" ");
          }
        }
        // Get the reversed string
        String localityName = reversedStringBuilder.toString();
        final Owner currentOwner = new ArrayList<>(owners).get(owners.size() - 1);
        final Property currentProperty = new ArrayList<>(currentOwner.getProperties()).get(currentOwner.getProperties().size() - 1);
        currentProperty.setLocality(localityName);
      }
      tableProcessingProgressIndex = 0;
    } else if (tableProcessingProgressIndex == 1) {
      final String[] landTokens = text.toString().split("\\|");
      final String locationToken = landTokens[0];

      final Owner currentOwner = new ArrayList<>(owners).get(owners.size() - 1);
      final Property currentProperty = new ArrayList<>(currentOwner.getProperties()).get(currentOwner.getProperties().size() - 1);
      if (currentProperty.getCounty() == null) {
        final String countyPattern = "Judet: (\\w+)";
        final String county = textOperationsHelper.extractValue(locationToken, countyPattern);
        currentProperty.setCounty(county);
      }
      if (currentProperty.getLocality() == null) {
        final String localityPattern = "Localitate: (\\w+)";
        final String locality = textOperationsHelper.extractValue(locationToken, localityPattern);
        currentProperty.setLocality(locality);
      } else {
        final Pattern pattern = Pattern.compile("(.+?)\\s*Adresa:");
        matcher = pattern.matcher(locationToken);
        if (matcher.find()) {
          String result = matcher.group(1);
          String[] split = result.split(" ");
          final StringBuilder newLocalityValue = new StringBuilder();
          for (int i = split.length - 2; i > 0; i--) {
            if (split[i].equals("Localitate:")) {
              break;
            }
            newLocalityValue.append(" ").append(split[i]);
          }
          newLocalityValue.deleteCharAt(newLocalityValue.length() - 1);
          String currentLocality = currentProperty.getLocality();
          currentProperty.setLocality(currentLocality + newLocalityValue);
        }
      }

      final String lastTwoPattern = "\\|\\|([^|]+)\\|$";
      final String newHolderValue = textOperationsHelper.extractValue(text.toString(), lastTwoPattern);
      if (newHolderValue != null) {
        final String currentHolderValue = currentProperty.getHolder();
        currentProperty.setHolder(String.join(" ", currentHolderValue, newHolderValue));
      }

      final String firstTwoFromLastThreePattern = "\\|([^|]+)\\|\\|\\|$";
      final String newAcquisitionManner = textOperationsHelper.extractValue(text.toString(), firstTwoFromLastThreePattern);
      if (newAcquisitionManner != null) {
        final String currentAcquisitionManner = currentProperty.getAcquisitionManner();
        currentProperty.setAcquisitionManner(String.join(" ", currentAcquisitionManner, newAcquisitionManner));
      }
      tableProcessingProgressIndex = 0;
    } else {
      final Property building = new Building();
      building.setPropertyType(PropertyType.BUILDING);

      final String[] landTokens = text.toString().split("\\|");
      final String[] locationTokens = landTokens[0].split(" ");
      if (locationTokens.length < 6) {
        tableProcessingProgressIndex = 1;
        if (locationTokens.length >= 4) {
          building.setCounty(locationTokens[3]);
        }
      } else {
        StringBuilder locality = new StringBuilder();
        for (int i = 5; i < locationTokens.length; i++) {
          final String token = locationTokens[i];
          if (token.equals("-")) {
            break;
          }
          locality.append(token);
          locality.append(" ");
        }

        if (Arrays.stream(locationTokens).noneMatch(token -> token.equals("-"))) {
          tableProcessingProgressIndex = 1;
        } else {
          locality.deleteCharAt(locality.length() - 1);
        }
        building.setLocality(locality.toString());
        building.setCounty(locationTokens[3]);
      }
      building.setCategory(landTokens[1]);
      building.setAcquisitionYear(landTokens[2]);
      building.setArea(landTokens[3]);
      building.setShare(landTokens[4]);
      building.setAcquisitionManner(landTokens[5]);
      building.setHolder(landTokens.length == 7 ? landTokens[6] : "");
      owner.getProperties().add(building);
      owners.add(owner);
    }
    return tableProcessingProgressIndex;
  }

  public int processOwnerAddressAndUpdateProcessingCursor(StringBuilder text, Map<Character, Integer> characterFrequencies, int tableProcessingProgressIndex, Set<Owner> owners, Owner owner) {
    Matcher matcher;
    if (text.toString().contains("Adresa") && characterFrequencies.get('|') == 1 && tableProcessingProgressIndex == 1) {
      final Pattern pattern = Pattern.compile("(.+?)\\s*Adresa:");
      matcher = pattern.matcher(text.toString());
      if (matcher.find()) {
        String result = matcher.group(1);
        String[] split = result.split(" ");
        final StringBuilder newLocalityValue = new StringBuilder();
        for (int i = split.length - 2; i > 0; i--) {
          if (split[i].equals("Localitate:")) {
            break;
          }
          newLocalityValue.append(" ").append(split[i]);
        }
        String[] words = newLocalityValue.toString().split(" ");

        // Create a StringBuilder to build the reversed string
        StringBuilder reversedStringBuilder = new StringBuilder();

        // Iterate through the words in reverse order and append to the StringBuilder
        for (int i = words.length - 1; i >= 0; i--) {
          reversedStringBuilder.append(words[i]);
          if (i > 0) {
            reversedStringBuilder.append(" ");
          }
        }

        // Get the reversed string
        String localityName = reversedStringBuilder.toString();
        final Owner currentOwner = new ArrayList<>(owners).get(owners.size() - 1);
        final Property currentProperty = new ArrayList<>(currentOwner.getProperties()).get(currentOwner.getProperties().size() - 1);
        currentProperty.setLocality(localityName);
      }
      tableProcessingProgressIndex = 0;
    } else if (tableProcessingProgressIndex == 1) {
      final String[] landTokens = text.toString().split("\\|");
      final String locationToken = landTokens[0];

      final Owner currentOwner = new ArrayList<>(owners).get(owners.size() - 1);
      final Property currentProperty = new ArrayList<>(currentOwner.getProperties()).get(currentOwner.getProperties().size() - 1);
      if (currentProperty.getCounty() == null) {
        final String countyPattern = "Judet: (\\w+)";
        final String county = textOperationsHelper.extractValue(locationToken, countyPattern);
        currentProperty.setCounty(county);
      }
      if (currentProperty.getLocality() == null) {
        final String localityPattern = "Localitate: (\\w+)";
        final String locality = textOperationsHelper.extractValue(locationToken, localityPattern);
        currentProperty.setLocality(locality);
      } else {
        final Pattern pattern = Pattern.compile("(.+?)\\s*Adresa:");
        matcher = pattern.matcher(locationToken);
        if (matcher.find()) {
          String result = matcher.group(1);
          String[] split = result.split(" ");
          final StringBuilder newLocalityValue = new StringBuilder();
          for (int i = split.length - 2; i > 0; i--) {
            if (split[i].equals("Localitate:")) {
              break;
            }
            newLocalityValue.append(" ").append(split[i]);
          }
          newLocalityValue.deleteCharAt(newLocalityValue.length() - 1);
          String currentLocality = currentProperty.getLocality();
          currentProperty.setLocality(currentLocality + newLocalityValue);
        }
      }

      final String lastTwoPattern = "\\|\\|([^|]+)\\|$";
      final String newHolderValue = textOperationsHelper.extractValue(text.toString(), lastTwoPattern);
      if (newHolderValue != null) {
        final String currentHolderValue = currentProperty.getHolder();
        currentProperty.setHolder(String.join(" ", currentHolderValue, newHolderValue));
      }

      final String firstTwoFromLastThreePattern = "\\|([^|]+)\\|\\|\\|$";
      final String newAcquisitionManner = textOperationsHelper.extractValue(text.toString(), firstTwoFromLastThreePattern);
      if (newAcquisitionManner != null) {
        final String currentAcquisitionManner = currentProperty.getAcquisitionManner();
        currentProperty.setAcquisitionManner(String.join(" ", currentAcquisitionManner, newAcquisitionManner));
      }
      tableProcessingProgressIndex = 0;
    } else {
      final Property land = new Land();
      land.setPropertyType(PropertyType.LAND);

      final String[] landTokens = text.toString().split("\\|");
      final String[] locationTokens = landTokens[0].split(" ");
      if (locationTokens.length < 6) {
        tableProcessingProgressIndex = 1;
        if (locationTokens.length >= 4) {
          land.setCounty(locationTokens[3]);
        }
      } else {
        StringBuilder locality = new StringBuilder();
        for (int i = 5; i < locationTokens.length; i++) {
          final String token = locationTokens[i];
          if (token.equals("-")) {
            break;
          }
          locality.append(token);
          locality.append(" ");
        }
        if (Arrays.stream(locationTokens).noneMatch(token -> token.equals("-"))) {
          tableProcessingProgressIndex = 1;
        } else {
          locality.deleteCharAt(locality.length() - 1);
        }
        land.setLocality(locality.toString());
        land.setCounty(locationTokens[3]);
      }
      land.setCategory(landTokens[1]);
      land.setAcquisitionYear(landTokens[2]);
      land.setArea(landTokens[3]);
      land.setShare(landTokens[4]);
      land.setAcquisitionManner(landTokens[5]);
      land.setHolder(landTokens.length == 7 ? landTokens[6] : "");
      owner.getProperties().add(land);
      owners.add(owner);
    }
    return tableProcessingProgressIndex;
  }

  public static boolean processVehicles(StringBuilder text, Map<Character, Integer> characterFrequencies) {
    return (text.toString().startsWith("Autovehicul") || text.toString().startsWith("Autoturism") || text.toString().startsWith("Alt mijloc de transport")
        || text.toString().startsWith("Tractor") || text.toString().startsWith("Masina agricola") || text.toString().startsWith("salupa")) && characterFrequencies.get('|') >= 5;
  }

  public static boolean processBuildings(StringBuilder text, Map<Character, Integer> characterFrequencies, int propertyIndex, String rowPattern, int tableProcessingProgressIndex) {
    return (text.toString().startsWith("Tara") && characterFrequencies.get('|') == 7 && propertyIndex == 1 && text.toString().matches(rowPattern)) ||
        (text.toString().contains("Adresa") && characterFrequencies.get('|') == 7 && propertyIndex == 1 && tableProcessingProgressIndex == 1) ||
        (text.toString().startsWith("Tara") && text.toString().contains("Adresa") && characterFrequencies.get('|') == 1 && tableProcessingProgressIndex == 1);
  }

  public static boolean processAddress(StringBuilder text, Map<Character, Integer> characterFrequencies, int propertyIndex, String rowPattern, int tableProcessingProgressIndex) {
    return (text.toString().startsWith("Tara") && characterFrequencies.get('|') == 7 && propertyIndex == 0 && text.toString().matches(rowPattern)) ||
        (text.toString().contains("Adresa") && characterFrequencies.get('|') == 7 && propertyIndex == 0 && tableProcessingProgressIndex == 1) ||
        (text.toString().startsWith("Tara") && text.toString().contains("Adresa") && characterFrequencies.get('|') == 1 && tableProcessingProgressIndex == 1);
  }

  public Owner buildOwner(String pdfFileName, PDDocument document) throws IOException {
    final Owner owner = new Owner();
    final String[] tokens = pdfFileName.split("_");
    final String party = tokens[tokens.length - 1].substring(0, tokens[tokens.length - 1].length() - 4);
    final String countyString = tokens[0];
    final String[] countyTokens = countyString.split(String.join("", "\\", FileSystems.getDefault().getSeparator()));
    final CountyEnum countyEnum = CountyEnum.valueOf(countyTokens[countyTokens.length - 1]);
    owner.setCounty(countyEnum.getName());
    owner.setParty(party);

    if (isOwnerPresent(document)) {
      final Matcher matcher = ownerAssetsProcessor.buildOwnerMatcher(document);
      // Retrieve the captured words and split them into a String array
      final String[] wordsAfterUndersigned = matcher.group(1).split("\\s+");
      owner.setLastName(wordsAfterUndersigned[0]);
      owner.setFatherInitial(wordsAfterUndersigned[1]);
      owner.setFirstName(wordsAfterUndersigned[2].substring(0, wordsAfterUndersigned[2].length() - 1));
      final String office = ownerAssetsProcessor.buildOffice(wordsAfterUndersigned);
      owner.setOffice(office);
    }
    return owner;
  }

  private boolean isOwnerPresent(PDDocument document) throws IOException {
    return ownerAssetsProcessor.buildOwnerMatcher(document).find();
  }
}
