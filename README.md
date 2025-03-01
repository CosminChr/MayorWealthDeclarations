# PDF-Excel ETL Project

## Overview

This project is a Java-based ETL (Extract, Transform, Load) application that processes PDF and Excel files. It leverages several powerful libraries:

- **Apache PDFBox** and **Tabula** for PDF extraction
- **OpenCSV** for CSV manipulation
- **Maven** for dependency management and packaging

## Generating the Executable (.exe) File

This project includes the **Launch4j Maven Plugin**, which converts your Java application into a native Windows executable (`.exe`).

### Prerequisites

Before proceeding, ensure you have:

- **JDK 17** (or compatible version)
- **Maven 3.x+** (for building)

### Step-by-Step Guide

#### 1. Clone the Repository

```bash
git clone <repository-url>
cd pdf-excel-etl
```

#### 2. Build with Maven

Run the following command to clean and install the project:

```bash
mvn clean install
```

This command performs the following actions:

- **Clean**: Removes previously compiled code and artifacts
- **Install**: Compiles code, runs tests, and packages the application into a JAR file
- **Launch4j Plugin**: Converts the JAR file into a Windows executable

#### 3. Locate the Executable

After successful completion, find the executable at:

```
target/Primari.exe
```

### Usage Instructions

#### Placement

Place `Primari.exe` in the directory containing your PDF files for processing. This is crucial as the executable expects to find input PDF files in the same folder from which it runs.

#### Execution

Simply double-click the executable on a Windows system. The application will:

1. Process PDF files in the current directory
2. Output converted data according to the application logic

### Customizing the Executable

The generated `.exe` includes:

| Setting | Value |
|---------|-------|
| Main Class | `com.pdf.excel.etl.Main` |
| Java Runtime | JDK 17+ required |

You can adjust these configurations in the `pom.xml` file, including:
- Executable name
- Java version requirements
- Other executable-related settings

### Troubleshooting

| Issue | Solution |
|-------|----------|
| **Missing JDK** | Ensure JDK 17+ is installed. Configure JDK path in the `pom.xml` under the `jre` section. |
| **Dependencies** | Verify internet connection and Maven repository status if downloads fail. |
| **No PDF Files Found** | Confirm the `.exe` is in the same directory as the PDF files to be processed. |

## Conclusion

This setup allows easy distribution of your Java application as a native Windows executable. The `.exe` file can be run and shared without installing a JDK on target machines.

> **Important**: For proper functioning, ensure the executable is placed in the same directory as the PDF files to be processed.