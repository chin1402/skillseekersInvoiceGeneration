package com.example.skillseeker.invoicegeneration.service;

import com.example.skillseeker.invoicegeneration.helper.Constants;
import com.example.skillseeker.invoicegeneration.message.AmountDetails;
import com.example.skillseeker.invoicegeneration.message.InvoiceHeaderData;
import com.example.skillseeker.invoicegeneration.message.StudentClassDetails;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author I077659
 */

@Service
@Slf4j
public class PdfGeneratorService {

    @Autowired
    private EmailService emailService;

    private static final String IMAGE_SOURCE = "images/logo.jpg";
    private static final Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
            Font.BOLD, BaseColor.BLACK);
    private static final Font redFont = new Font(Font.FontFamily.COURIER, 11,
            Font.BOLD, BaseColor.RED);
    private static final Font detailsFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.BOLD);
    private static final Font parentsNameFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.BOLD, new BaseColor(153,76,0,0));
    private static final Font studentNameFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.BOLD, new BaseColor(0, 153, 76, 0));

    private static final Font grandTotalFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
            Font.BOLD, new BaseColor(0, 153, 76, 0));
    private static final Font tableEntriesFont = new Font(Font.FontFamily.TIMES_ROMAN, 10,
            Font.NORMAL, BaseColor.BLACK);
    private static final Font tableHeadingFont = new Font(Font.FontFamily.TIMES_ROMAN, 10,
            Font.BOLD, BaseColor.BLACK);

    private static float grandTotalForParent;

    public void generatePdf(Map<String, Map<String, List<StudentClassDetails>>> dataPerParent) {
        String fileName = "";
        try {
            for(Map.Entry<String, Map<String, List<StudentClassDetails>>> parentDataForEachStudent: dataPerParent.entrySet()){
                grandTotalForParent = 0;
                InvoiceHeaderData invoiceHeaderData = getHeaderDataForParent(parentDataForEachStudent);
                fileName = parentDataForEachStudent.getKey() + " (" + invoiceHeaderData.getMonthOfInvoice() + ")" + ".pdf";
                invoiceHeaderData.setInvoiceFileName(fileName);

                log.info("Preparing invoice for " + fileName);

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(fileName));
                document.open();
                addMetaData(document);
                addPage(document, parentDataForEachStudent, invoiceHeaderData);
                document.close();

                log.info("Invoice generation successful for " + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMetaData(Document document) {
        document.addTitle(Constants.SKILL_SEEKERS);
        document.addSubject(Constants.INVOICE);
        document.addKeywords(Constants.SKILL_SEEKERS);
        document.addAuthor(Constants.SKILL_SEEKERS);
        document.addCreator(Constants.SKILL_SEEKERS);
    }

    private void addPage(Document document, Map.Entry<String, Map<String, List<StudentClassDetails>>> parentData, InvoiceHeaderData invoiceHeaderData)
            throws DocumentException, IOException {
        Paragraph preface = new Paragraph();
        boolean securityAmountApplicable = false;
        int counter = 1;
        addEmptyLine(preface, 1);
        preface.add(
                new Paragraph(String.format(Constants.INVOICE_HEADING, invoiceHeaderData.getMonthOfInvoice()), catFont)
        );

        addEmptyLine(preface, 1);
        preface.add(new Paragraph(
                String.format(Constants.SUB_HEADING_2, parentData.getKey()), parentsNameFont)
        );

        addEmptyLine(preface, 1);
        for(Map.Entry<String, List<StudentClassDetails>> eachStudentData: parentData.getValue().entrySet()){
            preface.add(addOverviewTable(eachStudentData.getValue()));
            if(invoiceHeaderData.getSecurityApplicable() != null && parentData.getValue().size() == counter &&
                    invoiceHeaderData.getSecurityApplicable().equals("X")){
                securityAmountApplicable = true;
            }
            preface.add(addAmountTable(eachStudentData.getValue(), securityAmountApplicable));
            counter++;
        }

        addEmptyLine(preface, 1);
        Paragraph grandTotalPara = new Paragraph(
                String.format(Constants.COL_GRAND_TOTAL, grandTotalForParent), grandTotalFont);
        grandTotalPara.setAlignment(Element.ALIGN_RIGHT);
        preface.add(grandTotalPara);

        addEmptyLine(preface, 1);

        if(securityAmountApplicable) {
            preface.add(new Paragraph(
                    Constants.SECURITY_WARNING, redFont)
            );
        }

        Image image = Image.getInstance(
                Objects.requireNonNull(getClass().getClassLoader().getResource(IMAGE_SOURCE)));
        image.setAlignment(Element.ALIGN_RIGHT);

        document.add(image);

        document.add(preface);
    }

    private Paragraph addAmountTable(List<StudentClassDetails> studentData, boolean securityApplicable) {
        Paragraph invoiceParagraph = new Paragraph();
        invoiceParagraph.add(new Paragraph(
                String.format(Constants.SUB_HEADING_DETAILS, studentData.get(0).getStudentName()), detailsFont)
        );
        invoiceParagraph.add(createAmountTable(studentData, securityApplicable));

        return invoiceParagraph;
    }

    private InvoiceHeaderData getHeaderDataForParent(Map.Entry<String, Map<String, List<StudentClassDetails>>> parentData) {
        InvoiceHeaderData headerData = new InvoiceHeaderData();
        Map<String, List<StudentClassDetails>> studentData = parentData.getValue();
        for(Map.Entry<String, List<StudentClassDetails>> entries: studentData.entrySet()){
            StudentClassDetails studentDetails = entries.getValue().get(0);
            headerData.setParentName(studentDetails.getParentName());
            headerData.setMonthOfInvoice(studentDetails.getMonthOfInvoice());
            headerData.setEmail(studentDetails.getEmail());
            headerData.setSecurityApplicable(studentDetails.getSecurityApplicable());
            break;
        }

        return headerData;
    }

    private Paragraph addOverviewTable(List<StudentClassDetails> studentData) {
        Paragraph invoiceParagraph = new Paragraph();
        invoiceParagraph.add(new Paragraph(
                String.format(Constants.SUB_HEADING_1, studentData.get(0).getStudentName()), studentNameFont)
        );
        addEmptyLine(invoiceParagraph, 1);
        invoiceParagraph.add(createOverviewTable(studentData));

        return invoiceParagraph;
    }

    private Paragraph createOverviewTable(List<StudentClassDetails> studentData) {
        Paragraph tableParagraph = new Paragraph();
        PdfPTable table = new PdfPTable(new float[] { 2, 3, 2, 1, 1});

        PdfPCell c1 = new PdfPCell(new Phrase(Constants.COL_SESSION_DATE, tableHeadingFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase(Constants.COL_TOPICS, tableHeadingFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase(Constants.COL_SUBJECT, tableHeadingFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase(Constants.COL_DURATION, tableHeadingFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase(Constants.COL_PRICE, tableHeadingFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        table.setHeaderRows(1);

        studentData.forEach(row -> {
            table.addCell(new Phrase(row.getDate(), tableEntriesFont));
            table.addCell(new Phrase(row.getTopic(), tableEntriesFont));
            table.addCell(new Phrase(row.getSubject(), tableEntriesFont));
            table.addCell(new Phrase(row.getDuration(), tableEntriesFont));
            table.addCell(new Phrase(row.getPrice(), tableEntriesFont));
        });

        tableParagraph.add(table);

        return tableParagraph;
    }

    private Paragraph createAmountTable(List<StudentClassDetails> studentData, boolean securityApplicable) {
        Map<String, AmountDetails> amountDetailsForStudent = new HashMap<>();
        float totalSum = 0;
        float securityAmount = securityApplicable ? Constants.SECURITY_DEPOSIT_AMOUNT : 0;
        Paragraph tableParagraph = new Paragraph();
        PdfPTable table = new PdfPTable(new float[] {3, 1, 2, 1});

        PdfPCell c1 = new PdfPCell(new Phrase(Constants.COL_SUBJECT, tableHeadingFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase(Constants.COL_PRICE, tableHeadingFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase(Constants.COL_NO_OF_HOURS, tableHeadingFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase(Constants.COL_TOTAL, tableHeadingFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        studentData.forEach(data -> {
            float price = Float.parseFloat(data.getPrice()) / Float.parseFloat(data.getDuration());
            float duration = Float.parseFloat(data.getDuration());
            if(amountDetailsForStudent.containsKey(data.getSubject())){
                AmountDetails details = amountDetailsForStudent.get(data.getSubject());
                details.setTotalHours(duration + details.getTotalHours());
            } else {
                amountDetailsForStudent.put(data.getSubject(), new AmountDetails(price, duration));
            }
        });

        table.setHeaderRows(1);

        for(Map.Entry<String, AmountDetails> aggregatedValue: amountDetailsForStudent.entrySet()){
            float totalForSubject = aggregatedValue.getValue().getPrice() * aggregatedValue.getValue().getTotalHours();
            totalSum = totalSum + totalForSubject;

            table.addCell(new Phrase(aggregatedValue.getKey(), tableEntriesFont));
            table.addCell(new Phrase(getFloatValue(
                    aggregatedValue.getValue().getPrice()), tableEntriesFont)
            );
            table.addCell(new Phrase(
                    getFloatValue(aggregatedValue.getValue().getTotalHours()), tableEntriesFont)
            );
            table.addCell(new Phrase(
                    getFloatValue(totalForSubject), tableEntriesFont)
            );
        }

        table.addCell(new Phrase("", tableEntriesFont));
        table.addCell(new Phrase("", tableEntriesFont));
        table.addCell(new Phrase(Constants.COL_TOTAL, tableHeadingFont));
        table.addCell(new Phrase(
                getFloatValue(totalSum), tableEntriesFont)
        );

        if(securityApplicable) {
            totalSum = totalSum + securityAmount;
            table.addCell(new Phrase("", tableEntriesFont));
            table.addCell(new Phrase("", tableEntriesFont));
            table.addCell(new Phrase(Constants.COL_SECURITY, tableHeadingFont));
            table.addCell(new Phrase(
                    getFloatValue(securityAmount), tableEntriesFont)
            );

            table.addCell(new Phrase("", tableEntriesFont));
            table.addCell(new Phrase("", tableEntriesFont));
            table.addCell(new Phrase(Constants.COL_CUMULATIVE_TOTAL, tableHeadingFont));
            table.addCell(new Phrase(
                    getFloatValue(totalSum), tableEntriesFont)
            );
        }

        tableParagraph.add(table);
        grandTotalForParent = grandTotalForParent + totalSum;
        return tableParagraph;
    }

    private void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    private String getFloatValue(float f){
        return new DecimalFormat("#.00").format(f);
    }

    public void sendPdf(Map<String, Map<String, List<StudentClassDetails>>> dataPerParent) {
        String fileName = "";
        int counter = 0;
        try {
            for(Map.Entry<String, Map<String, List<StudentClassDetails>>> parentDataForEachStudent: dataPerParent.entrySet()){
                InvoiceHeaderData invoiceHeaderData = getHeaderDataForParent(parentDataForEachStudent);
                fileName = parentDataForEachStudent.getKey() + " (" + invoiceHeaderData.getMonthOfInvoice() + ")" + ".pdf";

                invoiceHeaderData.setInvoiceFileName(fileName);
                emailService.sendEmailWithAttachment(invoiceHeaderData, counter);
                counter++;
            }
        } catch (Exception e) {
            log.error("Error: "+ e.getMessage());
        }
    }
}
