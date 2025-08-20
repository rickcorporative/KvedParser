package com.demo.data.abstractClasses;

import com.demo.utils.DateTime;
import com.demo.utils.Generator;

public class AbstractDocument {
    protected String documentName = String.format("%s%d%s", Generator.genString(3), Generator.genInt(1111, 9999), Generator.genString(3)).toUpperCase();
    protected String issuingState = "Alaska";
    protected String issuingDate = DateTime.getDateMinusMonth(DateTime.getLocalDateTimeByPattern("MM/dd/yyyy"), 48);
    protected String expirationDate = DateTime.getDatePlusMonth(DateTime.getLocalDateTimeByPattern("MM/dd/yyyy"), 24);

    public String getIssuingDateFormatted() {
        return this.issuingDate.replace("/", "-");
    }

    public String getExpirationDateFormatted() {
        return this.expirationDate.replace("/", "-");
    }
}
