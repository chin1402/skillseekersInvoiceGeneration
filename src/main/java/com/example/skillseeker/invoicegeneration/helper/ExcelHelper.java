package com.example.skillseeker.invoicegeneration.helper;

import com.example.skillseeker.invoicegeneration.message.StudentClassDetails;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author I077659
 */

@Slf4j
public class ExcelHelper {

    public static boolean hasExcelFormat(MultipartFile file) {

        if (!Constants.EXCEL_TYPE.equals(file.getContentType())) {
            return false;
        }

        return true;
    }

    public static Map<String, Map<String, List<StudentClassDetails>>> readExcel(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            log.info("Reading Master Sheet with Name " + Constants.MASTER_SHEET_NAME);
            Sheet sheet = workbook.getSheet(Constants.MASTER_SHEET_NAME);
            Iterator<Row> rows = sheet.iterator();
            Map<String, Map<String, List<StudentClassDetails>>> invoicesForParent = new HashMap<>();
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();
                StudentClassDetails rowData = new StudentClassDetails();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    switch (cellIdx) {
                        case 0:
                            rowData.setStudentName(currentCell.getStringCellValue());
                            break;
                        case 1:
                            rowData.setDate(new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH).format(currentCell.getDateCellValue()));
                            rowData.setMonthOfInvoice(
                                    new SimpleDateFormat("MMMM").format(currentCell.getDateCellValue()) + ", " +
                                            new SimpleDateFormat("yyyy").format(currentCell.getDateCellValue())
                            );
                            break;
                        case 2:
                            rowData.setTopic(currentCell.getStringCellValue());
                            break;
                        case 3:
                            rowData.setPrice(String.valueOf(
                                    currentCell.getNumericCellValue())
                            );
                            break;
                        case 4:
                            rowData.setDuration(String.valueOf(
                                    currentCell.getNumericCellValue())
                            );
                            break;
                        case 5:
                            rowData.setSubject(currentCell.getStringCellValue());
                            break;
                        case 6:
                            rowData.setParentName(currentCell.getStringCellValue());
                            break;
                        case 7:
                            rowData.setSecurityApplicable(currentCell.getStringCellValue());
                            break;
                        case 8:
                            rowData.setEmail(currentCell.getStringCellValue());
                            break;
                        default:
                            break;
                    }

                    cellIdx++;
                }

                if(rowData.getParentName() != null){
                    if(!invoicesForParent.containsKey(rowData.getParentName())){
                        Map<String, List<StudentClassDetails>> newChildRecordForParent = new HashMap<>();
                        List<StudentClassDetails> newStudentRecords = new ArrayList<>();
                        newStudentRecords.add(rowData);
                        newChildRecordForParent.put(rowData.getStudentName(), newStudentRecords);
                        invoicesForParent.put(rowData.getParentName(), newChildRecordForParent);
                    } else {
                        Map<String, List<StudentClassDetails>> existingChildRecords = invoicesForParent.get(rowData.getParentName());
                        if(existingChildRecords.containsKey(rowData.getStudentName())){
                            List<StudentClassDetails> existingRecords = existingChildRecords.get(rowData.getStudentName());
                            existingRecords.add(rowData);
                        } else {
                            List<StudentClassDetails> newStudentRecords = new ArrayList<>();
                            newStudentRecords.add(rowData);
                            Map<String, List<StudentClassDetails>> ex = invoicesForParent.get(rowData.getParentName());
                            ex.merge(rowData.getStudentName(), newStudentRecords, (oldValue, newValue) -> {
                                oldValue.add(rowData);
                                return oldValue;
                            });
                        }
                    }
                }
            }

            workbook.close();
            log.info("Data Preparation Success");
            return invoicesForParent;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }
}
