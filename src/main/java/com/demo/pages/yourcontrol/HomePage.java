package com.demo.pages.yourcontrol;

import com.demo.core.base.PageTools;
import com.demo.utils.Constants;
import com.demo.utils.CsvWriter;
import com.demo.utils.PlaywrightTools;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import java.util.*;

public class HomePage extends PageTools {
    private String input = "//input[@id='q']";
    private String searchButton = "//button[@id='search-submit']";
    private String companyBlock = "//div[@class='search-item-block']";
    private String companyLink = "//a[contains(@href, 'contractor/')]";
    private String companyKved = "//div[@id='activity-qa']";
    private String filterKvedSelect = "//button[text()='Види діяльності']";
    private String kvedOption = "//label[@title='%s']";
    private String cross = "//span[contains(@class, 'mdi mdi-close yc-dialog-close')]";
    private String[] requiredKveds = {"10.71", "10.72", "56.10", "56.30", "47.11", "47.29", "47.25", "96.09", "52.11.0", "55.30.2"};

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

    @Step("Select kved")
    public void selectKved(String value){
        click(filterKvedSelect);
        click(kvedOption, value);
    }

    @Step("Select kved")
    public void clickCross(){
        click(cross);
    }

    @Step("Parse")
    public void parse(String kved, String query, int page, int product) {
        int pageNum = page;
        int processed = 0;       // сколько реально обработали
        int limit = 25;          // ⚡ всегда по 25 за запуск
        int startIndex = product; // начинаем с product внутри страницы

        String csvPath = "target/companies.csv";

        try (CsvWriter csv = new CsvWriter(csvPath, true, false)) {
            while (processed < limit) {
                String currentUrl = Constants.BASE_URL;
                String nextUrl;

                if (currentUrl.contains("&page=")) {
                    nextUrl = currentUrl.replaceAll("&page=\\d+", "&page=" + pageNum);
                } else {
                    if (Objects.equals(kved, "")) {
                        nextUrl = currentUrl + "/search/?country=1&q=" + query + "&s%5B%5D=1&t=0&page=" + pageNum;
                    } else {
                        nextUrl = currentUrl + "/search/?country=1&q=" + query +
                                "&s%5B%5D=1&k%5B%5D=" + kved + "&t=0&page=" + pageNum;
                    }

                }

                System.out.println("Navigating to: " + nextUrl);
                getPage().navigate(nextUrl);

                Locator resultBlocks = getPage().locator(companyBlock);
                int count = resultBlocks.count();

                if (count == 0) {
                    System.out.println("----------------No more companies. Stopping at page " + pageNum);
                    break;
                }

                // начинаем с product внутри страницы
                for (int i = startIndex; i < count && processed < limit; i++) {
                    Locator block = resultBlocks.nth(i);
                    Locator link = block.locator("xpath=." + companyLink);
                    int companyTypeIndex = i + 1;

                    String companyType = "((" + companyBlock + ")[" + companyTypeIndex + "])" + companyKved;

                    if (isElementVisibleCheckEverySecond(companyType, 1)) {
                        String companyTypeText = getElementInnerText(companyType);
                        for (int j = 0; j < requiredKveds.length; j++) {
                            if (companyTypeText.contains(requiredKveds[j])) {
                                logInfo("Description of next company is " + requiredKveds[j] + ". Opening page...");
                                if (link.isVisible()) {
                                    String href = link.getAttribute("href");
                                    if (href != null) {
                                        if (href.startsWith("/")) {
                                            String baseUrl = getPage().url().split("/", 4)[0] + "//" +
                                                    getPage().url().split("/", 4)[2];
                                            href = baseUrl + href;
                                        }

                                        // Задержка перед открытием компании
                                        humanPause(1000, 3000);

                                        logInfo("-------Start processing company------");
                                        Page newPage = getPage().context().newPage();
                                        newPage.navigate(href);

                                        CompanyPage companyPage = new CompanyPage(newPage);

                                        String fullName   = companyPage.getCompanyFullName();
                                        String shortName  = companyPage.getCompanyShortName();
                                        String erdpou     = companyPage.getEdrpou();
                                        String location   = companyPage.getCompanyLocation();
                                        String managerName= companyPage.getManagerName();
                                        String phones     = companyPage.getCompanyPhone();

                                        // 📌 Записываем строку в CSV
                                        csv.writeRow(fullName, shortName, managerName,
                                                phones, erdpou, location, href);
                                        csv.flush(); // ✅ сразу сохраняем строку

                                        newPage.close();

                                        processed++;
                                        System.out.println("✅ Обработано компаний: " + processed);

                                        // Пауза после компании
                                        humanPause(1000, 3000);
                                    }
                                }
                            }
                        }
                    } else {
                        logInfo("Description of next company is not visible. Skipping it...");
                    }
                }

                // после первой страницы сбрасываем offset
                startIndex = 0;
                pageNum++;

                // Пауза перед переходом на новую страницу
                humanPause(3000, 6000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        PlaywrightTools.sleep(5);
    }


    private void humanPause() {
        try {
            int delay = 1000 + new Random().nextInt(4000); // от 1 до 5 секунд
            Thread.sleep(delay);

            // иногда двигаем мышь случайно
            if (new Random().nextBoolean()) {
                int x = 100 + new Random().nextInt(800);
                int y = 100 + new Random().nextInt(600);
                getPage().mouse().move(x, y);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
