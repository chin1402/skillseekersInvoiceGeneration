package com.example.skillseeker.invoicegeneration.message;

import com.itextpdf.text.Document;

/**
 * @author I077659
 */
public class InvoiceHeaderData {

    private String parentName;
    private String securityApplicable;
    private String email;
    private String monthOfInvoice;
    private String invoiceFileName;

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getSecurityApplicable() {
        return securityApplicable;
    }

    public void setSecurityApplicable(String securityApplicable) {
        this.securityApplicable = securityApplicable;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMonthOfInvoice() {
        return monthOfInvoice;
    }

    public void setMonthOfInvoice(String monthOfInvoice) {
        this.monthOfInvoice = monthOfInvoice;
    }

    public String getInvoiceFileName() {
        return invoiceFileName;
    }

    public void setInvoiceFileName(String invoiceFileName) {
        this.invoiceFileName = invoiceFileName;
    }
}
