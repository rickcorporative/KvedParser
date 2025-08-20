package com.demo.pages.yourcontrol;

import com.demo.core.base.PageTools;
import com.demo.utils.Constants;
import com.demo.utils.PlaywrightTools;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class HomePage extends PageTools {
    private String input = "//input[@id='q-l']";
    private String searchButton = "//button[span[text()='%s']]";
    private String searchItemBlocks = "//div[@class='search-item-block']";
    private String companyLink = "//a[contains(@href,'company_details')]";

    @Step("Open home page")
    public void openHomePage() {
        PlaywrightTools.openUrl(Constants.BASE_URL);
    }

    @Step("Type into input")
    public void typeIntoInput(String value){
        type(value, input);
    }

    @Step("ClickSearchButton")
    public void clickSearchButton(){
        click(searchButton, "Пошук");
    }

    @Step("Parse")
    public void parse(){
        Locator resultBlocks = getPage().locator(searchItemBlocks);
        int count = resultBlocks.count();

        for (int i = 1; i < count; i++) {
            Locator block = resultBlocks.nth(i);
            Locator link = block.locator("xpath=." + companyLink);

            if (link.isVisible()) {

                String href = link.getAttribute("href");
                if (href != null) {
                    if (href.startsWith("/")) {
                        // Берём origin из текущей страницы
                        String baseUrl = getPage().url().split("/", 4)[0] + "//" + getPage().url().split("/", 4)[2];
                        href = baseUrl + href;
                    }

                    Page newPage = getPage().context().newPage();
                    newPage.navigate(href);

                    CompanyPage companyPage = new CompanyPage(newPage);

                    companyPage.getCompanyPhone();

                    newPage.close();
                }

            } else {
                System.out.println("Link not visible in block #" + i);
            }
        }
    }

}
