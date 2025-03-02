package com.example.skillseeker.invoicegeneration.controller;

import com.example.skillseeker.invoicegeneration.helper.ExcelHelper;
import com.example.skillseeker.invoicegeneration.service.ExcelService;
import com.example.skillseeker.invoicegeneration.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author I077659
 */

@RestController
@RequestMapping("/skillSeeker/api")
public class SkillSeekerController {

    @Autowired
    ExcelService fileService;

    @GetMapping(path = "/generateInvoices")
    public ResponseEntity<ResponseMessage> generateInvoices(@RequestParam("file") MultipartFile file){
        String message = "";
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                fileService.generateInvoices(file);
                message = "Invoices generated successfully from File: " + file.getOriginalFilename();
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!" + "Detail Error: " + e.getMessage();
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
            }
        }

        message = "Please upload an excel file!";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));
    }

    @GetMapping(path = "/send")
    public ResponseEntity<ResponseMessage> sendInvoices(@RequestParam("file") MultipartFile file){
        String message = "";
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                fileService.sendInvoices(file);
                message = "Invoices Email Sent successfully";
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!" + "Detail Error: " + e.getMessage();
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
            }
        }

        message = "Please upload an excel file!";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));
    }

}
