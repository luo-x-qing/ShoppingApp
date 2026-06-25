package com.example.demo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the admin attraction.html Thymeleaf template structure.
 * Reads the raw template file and validates HTML elements, IDs, and page structure.
 */
class AdminAttractionPageTest {

    private static Document doc;

    @BeforeAll
    static void setUp() throws IOException {
        File template = new File("src/main/resources/templates/admin/attraction.html");
        assertTrue(template.exists(), "Template file not found");
        doc = Jsoup.parse(template, "UTF-8");
    }

    @Test
    void testPageTitle() {
        String title = doc.title();
        assertEquals("景点管理 - 管理员", title);
    }

    @Test
    void testBootstrapCdnIncluded() {
        Elements links = doc.select("link[href]");
        boolean hasBootstrap = links.stream()
                .anyMatch(l -> l.attr("href").contains("bootstrap"));
        assertTrue(hasBootstrap, "Bootstrap CDN should be included");
    }

    @Test
    void testSidebarNavigation() {
        Element sidebar = doc.selectFirst(".list-group");
        assertNotNull(sidebar, "Sidebar .list-group should exist");

        Elements links = sidebar.select("a");
        assertTrue(links.size() >= 6, "Sidebar should have at least 6 nav items");

        assertTrue(links.stream().anyMatch(l -> l.text().contains("仪表盘")));
        assertTrue(links.stream().anyMatch(l -> l.text().contains("酒店管理")));
        assertTrue(links.stream().anyMatch(l -> l.text().contains("景点管理")));
        assertTrue(links.stream().anyMatch(l -> l.text().contains("用户管理")));
        assertTrue(links.stream().anyMatch(l -> l.text().contains("商家管理")));
    }

    @Test
    void testActiveSidebarItem() {
        Element activeItem = doc.selectFirst(".list-group .active");
        assertNotNull(activeItem, "Active sidebar item should exist");
        assertTrue(activeItem.text().contains("景点管理"));
    }

    @Test
    void testPageHeader() {
        Element h3 = doc.selectFirst("h3");
        assertNotNull(h3);
        assertTrue(h3.text().contains("景点管理"));
    }

    @Test
    void testStatisticsCards() {
        Element totalCard = doc.getElementById("totalCount");
        assertNotNull(totalCard, "Total count card should exist");

        Element normalCard = doc.getElementById("normalCount");
        assertNotNull(normalCard, "Normal count card should exist");

        Element provinceCard = doc.getElementById("provinceCount");
        assertNotNull(provinceCard, "Province count card should exist");
    }

    @Test
    void testSearchFilterBar() {
        Element searchInput = doc.getElementById("searchKeyword");
        assertNotNull(searchInput, "Search keyword input should exist");

        Element filterProvince = doc.getElementById("filterProvince");
        assertNotNull(filterProvince, "Province filter select should exist");

        Element searchBtn = doc.selectFirst("button:contains(搜索)");
        assertNotNull(searchBtn, "Search button should exist");
    }

    @Test
    void testActionButtons() {
        Element addBtn = doc.selectFirst("button:contains(添加景点)");
        assertNotNull(addBtn, "Add attraction button should exist");
        assertTrue(addBtn.attr("data-target").contains("#addModal"),
                "Add button should target #addModal");

        Element refreshBtn = doc.selectFirst("button:contains(刷新)");
        assertNotNull(refreshBtn, "Refresh button should exist");

        Element importBtn = doc.selectFirst("button:contains(一键导入全国景点)");
        assertNotNull(importBtn, "Import all button should exist");

        Element resetBtn = doc.selectFirst("button:contains(重置并重新编号)");
        assertNotNull(resetBtn, "Reset button should exist");
    }

    @Test
    void testAttractionTable() {
        Element table = doc.selectFirst("table");
        assertNotNull(table, "Table should exist");

        Element thead = table.selectFirst("thead");
        assertNotNull(thead, "Table thead should exist");

        Element tbody = doc.getElementById("attractionList");
        assertNotNull(tbody, "Table body #attractionList should exist");

        Elements headers = thead.select("th");
        assertTrue(headers.size() >= 8, "Table should have at least 8 columns");
    }

    @Test
    void testAddModalStructure() {
        Element modal = doc.getElementById("addModal");
        assertNotNull(modal, "Add modal should exist");
        assertTrue(modal.classNames().contains("modal"), "Add modal should have modal class");
        assertTrue(modal.classNames().contains("fade"), "Add modal should have fade class");

        Element nameInput = modal.getElementById("name");
        assertNotNull(nameInput, "Add form should have name input");

        Element provinceSelect = modal.getElementById("province");
        assertNotNull(provinceSelect, "Add form should have province select");

        Element cityInput = modal.getElementById("city");
        assertNotNull(cityInput, "Add form should have city input");

        Element photoFile = modal.getElementById("photoFile");
        assertNotNull(photoFile, "Add form should have photo file input");
    }

    @Test
    void testEditModalStructure() {
        Element modal = doc.getElementById("editModal");
        assertNotNull(modal, "Edit modal should exist");

        Element editId = modal.getElementById("editId");
        assertNotNull(editId, "Edit form should have hidden id input");

        Element editName = modal.getElementById("editName");
        assertNotNull(editName, "Edit form should have name input");

        Element editProvince = modal.getElementById("editProvince");
        assertNotNull(editProvince, "Edit form should have province select");

        Element editCity = modal.getElementById("editCity");
        assertNotNull(editCity, "Edit form should have city input");
    }

    @Test
    void testScriptsIncluded() {
        Elements scripts = doc.select("script[src]");
        boolean hasJQuery = scripts.stream()
                .anyMatch(s -> s.attr("src").contains("jquery"));
        assertTrue(hasJQuery, "jQuery CDN should be included");

        boolean hasBootstrapJs = scripts.stream()
                .anyMatch(s -> s.attr("src").contains("bootstrap") && s.attr("src").contains("js"));
        assertTrue(hasBootstrapJs, "Bootstrap JS CDN should be included");
    }

    @Test
    void testInlineScriptFunctions() {
        Element inlineScript = doc.selectFirst("script:not([src])");
        assertNotNull(inlineScript, "Inline script block should exist");

        String js = inlineScript.html();

        assertTrue(js.contains("function loadAttractions()"), "loadAttractions function should exist");
        assertTrue(js.contains("function renderList("), "renderList function should exist");
        assertTrue(js.contains("function updateStatistics("), "updateStatistics function should exist");
        assertTrue(js.contains("function saveAttraction("), "saveAttraction function should exist");
        assertTrue(js.contains("function openEdit("), "openEdit function should exist");
        assertTrue(js.contains("function updateAttraction("), "updateAttraction function should exist");
        assertTrue(js.contains("function deleteAttraction("), "deleteAttraction function should exist");
        assertTrue(js.contains("function importAll("), "importAll function should exist");
        assertTrue(js.contains("function resetAndRenumber("), "resetAndRenumber function should exist");
        assertTrue(js.contains("function searchAttractions("), "searchAttractions function should exist");
        assertTrue(js.contains("function refreshList("), "refreshList function should exist");
    }

    @Test
    void testApiBaseUrl() {
        Element inlineScript = doc.selectFirst("script:not([src])");
        assertNotNull(inlineScript);

        String js = inlineScript.html();
        assertTrue(js.contains("API_BASE = \"http://localhost:8080\""),
                "API_BASE should be set to localhost:8080");
    }

    @Test
    void testProvinceOptions() {
        Element provinceSelect = doc.getElementById("province");
        assertNotNull(provinceSelect);
        Elements options = provinceSelect.select("option");
        assertTrue(options.size() > 30, "Should have province options for all 34 regions");
    }

    @Test
    void testNavbarExists() {
        Element navbar = doc.selectFirst(".navbar");
        assertNotNull(navbar, "Navbar should exist");
        assertTrue(navbar.text().contains("旅游资源管理系统"), "Navbar should contain system title");
    }
}
