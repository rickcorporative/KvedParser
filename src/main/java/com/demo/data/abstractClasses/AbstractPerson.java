package com.demo.data.abstractClasses;

import com.demo.data.Address;
import com.demo.data.DriversLicense;
import com.demo.utils.DateTime;
import com.demo.utils.Generator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractPerson {
    protected String firstName = Generator.genString(8);
    protected String middleName = Generator.genString(3);
    protected String lastName = Generator.genString(8);
    protected String suffix = Generator.genString(3);
    protected String countryCode = "United States";
    protected String mobileNumber = Generator.genMobilePhone(7);
    protected String email = Generator.genEmail();
    protected String birthDate = "01/01/2000";
    protected String ssn = String.format("666-%s-%s", Generator.genInt(10, 99), Generator.genInt(1111, 9999));

    protected Address homeAddress = new Address();
    protected Address mailingAddress = new Address();

    protected String companyName = Generator.genString(8);
    protected String employmentType = "Full-Time";
    protected String income = "100.00";
    protected String frequency = "Weekly";
    protected boolean currentlyEmployed = true;
    protected boolean selfEmployed = false;
    protected String startWorkDate = DateTime.getDateMinusMonth(DateTime.getLocalDateTimeByPattern("MM/dd/yyyy"), 3);
    protected String endWorkDate = DateTime.getDateMinusMonth(DateTime.getLocalDateTimeByPattern("MM/dd/yyyy"), 2);

    protected AbstractDocument driversLicense = new DriversLicense();

    protected String passwordEmail = "Password1!";
    protected String appPassword;

    protected AbstractPerson setDefaultData() {
        return this;
    }

    public String getFullName() {
        return String.format("%s %s %s %s", firstName, middleName, lastName, suffix);
    }

    public String getFirstNameLastNameSuffix() {
        return String.format("%s %s %s", firstName, lastName, suffix);
    }

    public String getFirstNameLastName() {
        return String.format("%s %s", firstName, lastName);
    }

    public String getEmailFormatted() {
        int atIndex = email.indexOf('@');
        if (atIndex == -1) return email; // not a valid email

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);

        int plusIndex = localPart.indexOf('+');
        if (plusIndex != -1) {
            localPart = localPart.substring(0, plusIndex);
        }

        return localPart + domainPart;
    }

    public String getMobileNumberFormatted() {
        return this.mobileNumber.replaceAll("[()\\-]", "");
    }

    public String getSsnFormatted() {
        return this.ssn.replaceAll("[^0-9]", "");
    }

    public String getBirthDateFormatted() {
        return this.birthDate.replace("/", "-");
    }
}
