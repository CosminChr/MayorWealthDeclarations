package com.pdf.excel.etl.file;

import com.opencsv.CSVWriter;
import com.pdf.excel.etl.model.Owner;
import com.pdf.excel.etl.model.Property;
import com.pdf.excel.etl.model.PropertyType;
import com.pdf.excel.etl.model.Vehicle;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Author: Cosmin Chiriac
 * Date: 2025-03-01
 */
public class CsvFilesGenerator {

  public void generateCsvFiles(Set<Owner> owners) throws IOException {
    try (final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
         final CSVWriter writer = new CSVWriter(new PrintWriter(new OutputStreamWriter(bOut, StandardCharsets.UTF_8), true))) {

      // the csv file header
      writer.writeNext(new String[]{
          "Nume", "Prenume", "Primarie", "Localitate", "Judet", "Categorie", "Anul dobandirii", "Suprafata", "Cota-parte", "Modul de dobandire", "Titularul", "Partid", "Judet"
      });
      for (Owner owner : owners) {
        // if the owner has any properties we write his name and assets into the file
        if (hasOwnerAssets(owner, PropertyType.LAND)) {

          writeAssetsIntoCsvFile(owner, PropertyType.LAND, writer);
        }
      }
      writer.flush();
      IOUtils.write(bOut.toByteArray(), new FileOutputStream("Terenuri.csv"));
    }

    try (final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
         final CSVWriter writer = new CSVWriter(new PrintWriter(bOut))) {

      writer.writeNext(new String[]{
          "Nume", "Prenume", "Primarie", "Localitate", "Judet", "Categorie", "Anul dobandirii", "Suprafata", "Cota-parte", "Modul de dobandire", "Titularul", "Partid", "Judet"
      });
      for (Owner owner : owners) {
        if (hasOwnerAssets(owner, PropertyType.BUILDING)) {
          writeAssetsIntoCsvFile(owner, PropertyType.BUILDING, writer);
        }
      }
      writer.flush();
      IOUtils.write(bOut.toByteArray(), new FileOutputStream("Cladiri.csv"));
    }

    try (final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
         final CSVWriter writer = new CSVWriter(new PrintWriter(bOut))) {
      writer.writeNext(new String[]{
          "Nume", "Prenume", "Primarie", "Natura", "Marca", "Nr de bucati", "Anul de fabricatie", "Modul de dobandire", "Partid", "Judet"
      });
      for (Owner owner : owners) {
        if (hasOwnerVehicles(owner)) {
          writeVehiclesIntoCsvFile(owner, writer);
        }
      }
      writer.flush();
      IOUtils.write(bOut.toByteArray(), new FileOutputStream("Vehicule.csv"));
    }
  }

  private static boolean hasOwnerVehicles(Owner owner) {
    return !owner.getVehicles().isEmpty();
  }

  private static boolean hasOwnerAssets(Owner owner, PropertyType land) {
    return !owner.getProperties().stream().filter(property -> property.getPropertyType() == land).toList().isEmpty();
  }

  private static void writeVehiclesIntoCsvFile(Owner owner, CSVWriter writer) {
    for (Vehicle vehicle : owner.getVehicles()) {
      writer.writeNext(new String[]{
          String.join(", ", owner.getLastName(), owner.getFatherInitial()),
          owner.getFirstName(),
          owner.getOffice(),
          vehicle.getType(),
          vehicle.getBrand(),
          vehicle.getPieces(),
          vehicle.getProductionYear(),
          vehicle.getAcquisitionManner(),
          owner.getParty(),
          owner.getCounty()
      });
    }
  }

  private static void writeAssetsIntoCsvFile(Owner owner, PropertyType land, CSVWriter writer) {
    for (Property property : owner.getProperties()) {
      if (property.getPropertyType() == land) {
        writer.writeNext(new String[]{
            String.join(", ", owner.getLastName(), owner.getFatherInitial()),
            owner.getFirstName(),
            owner.getOffice(),
            property.getLocality(),
            property.getCounty(),
            property.getCategory(),
            property.getAcquisitionYear(),
            property.getArea(),
            property.getShare(),
            property.getAcquisitionManner(),
            property.getHolder(),
            owner.getParty(),
            owner.getCounty()
        });
      }
    }
  }
}
