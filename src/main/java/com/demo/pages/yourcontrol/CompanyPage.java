package com.demo.pages.yourcontrol;

import com.demo.core.base.PageTools;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import java.util.*;
import java.util.stream.Collectors;

public class CompanyPage extends PageTools {
    // Локаторы
    private String contactLocator   = "//tr[th[normalize-space(text())='%s']]//td";
    private String fullNameLocator  = "//td[@id='contractor-name']//div[1]";
    private String shortNameLocator = "//tr[th[normalize-space(text())='Скорочена назва']]//td//div[1]";
    private String edrpouLocator    = "//tr[th[normalize-space(text())='Код ЄДРПОУ']]//td//div[1]";
    private String managerField     = "//td[@id='file-managers']//a";

    // Стоп-слова для очистки менеджеров
    private static final Set<String> STOPWORDS = Set.of(
            "Відкрити в пошуку", "Открыть в поиске", "Open in search"
    );

    public CompanyPage() { super(); }
    public CompanyPage(Page page) { super(page); }

    // ===== helpers =====
    private String norm(String s) {
        if (s == null) return "";
        String x = s.replace('\u00A0',' ')
                .replace('\u2007',' ')
                .replace('\u202F',' ')
                .replace('\t',' ')
                .replace('\r',' ')
                .replace('\n',' ');
        return x.replaceAll("\\s+", " ").trim();
    }

    private List<String> cleanDistinct(List<String> raw) {
        if (raw == null) return Collections.emptyList();
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (String s : raw) {
            String v = norm(s);
            if (!v.isEmpty() && !STOPWORDS.contains(v)) {
                seen.add(v);
            }
        }
        return new ArrayList<>(seen);
    }

    // ===== getters =====
    @Step("Get manager name")
    public String getManagerName() {
        try {
            if (!isFirstElementVisibleCheckEverySecond(managerField, 2)) {
                return "";
            }
            List<String> managers = cleanDistinct(getElementsText(managerField));
            return managers.isEmpty() ? "" : String.join("; ", managers);
        } catch (Exception e) {
            return "Нет имени руководителя";
        }
    }

    @Step("Get company location")
    public String getCompanyLocation() {
        try {
            return norm(getElementInnerText(contactLocator, "Адреса:"));
        } catch (Exception e) {
            return "Нет локации";
        }
    }

    @Step("Get company short name")
    public String getCompanyShortName() {
        try {
            return norm(getElementInnerText(shortNameLocator));
        } catch (Exception e) {
            return "Нет сокращенного имени";
        }
    }

    @Step("Get company full name")
    public String getCompanyFullName() {
        try {
            return norm(getElementInnerText(fullNameLocator));
        } catch (Exception e) {
            return "Нет полного имени";
        }
    }

    @Step("Get edrpou")
    public String getEdrpou() {
        try {
            // используем отдельный локатор, не uniLocator!
            return norm(getElementInnerText(edrpouLocator)).replaceAll("\\D", "");
        } catch (Exception e) {
            return "Без ЄДРПОУ";
        }
    }

    @Step("Get company phone(s)")
    public String getCompanyPhone() {
        try {
            if (!isFirstElementVisibleCheckEverySecond(contactLocator, 2, "Телефон:")) {
                return "";
            }

            List<String> phones = getElementsText(contactLocator, "Телефон:")
                    .stream()
                    .map(this::norm)          // чистим
                    .filter(p -> !p.isEmpty()) // убираем пустые
                    .distinct()                // убираем дубли
                    .collect(Collectors.toList());

            return phones.isEmpty() ? "" : String.join("; ", phones);

        } catch (Exception e) {
            return "Без телефона";
        }
    }
}
