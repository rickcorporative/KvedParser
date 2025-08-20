package com.demo.pages.yourcontrol;

import com.demo.core.base.PageTools;
import com.demo.utils.Constants;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import java.util.List;


public class CompanyPage extends PageTools {
    private String phone = "//tr[td[normalize-space(text())='Телефон:']]//a";

    public CompanyPage() {
        super();
    }

    public CompanyPage(Page page) {
        super(page);
    }

    @Step("Get company phone")
    public void getCompanyPhone(){
        if (isFirstElementVisibleCheckEverySecond(phone, Constants.SMALL_TIMEOUT)) {
            // получаем список всех телефонов
            List<String> phones = getElementsInnerTexts(phone);

            if (!phones.isEmpty()) {
                System.out.println("Phones: " + String.join(", ", phones));
            } else {
                System.out.println("Phone: not found");
            }
        } else {
            System.out.println("Phone: not found");
        }
    }
}
