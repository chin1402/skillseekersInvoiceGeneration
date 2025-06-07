package com.example.skillseeker.invoicegeneration.service;

import com.example.skillseeker.invoicegeneration.helper.ExcelHelper;
import com.example.skillseeker.invoicegeneration.message.StudentClassDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author I077659
 */
@Service
public class ExcelService {

    @Autowired
    PdfGeneratorService pdfService;

    public void generateInvoices(MultipartFile file) {
        try {
            Map<String, Map<String, List<StudentClassDetails>>> recordsForEachParent = ExcelHelper.readExcel(file.getInputStream());
            pdfService.generatePdf(new TreeMap<>(recordsForEachParent));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInvoices(MultipartFile file) {
        try {
            Map<String, Map<String, List<StudentClassDetails>>> recordsForEachParent =
                    ExcelHelper.readExcel(file.getInputStream());
            pdfService.sendPdf(new TreeMap<>(recordsForEachParent));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
