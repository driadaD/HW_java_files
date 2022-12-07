package guru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import guru.qa.model.Glossary;
import guru.qa.model.NewJson;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class FilesParsingTest {

    ClassLoader cl = FilesParsingTest.class.getClassLoader();

    @Test
    void pdfParseTest() throws Exception {
        open("https://junit.org/junit5/docs/current/user-guide/");
        File downloadedPdf = $("a[href='junit-user-guide-5.9.1.pdf']").download();
        PDF content = new PDF(downloadedPdf);
        assertThat(content.author).contains("Sam Brannen");
    }

    @Test
    void xlsParseTest() throws Exception {
        try (InputStream resourceAsStream = cl.getResourceAsStream("тестовый.xlsx")) {
            XLS content = new XLS(resourceAsStream);
            assertThat(content.excel.getSheetAt(0).getRow(2).getCell(0).getStringCellValue()).contains("Элизабет");
            assertThat(content.excel.getSheetAt(0).getRow(2).getCell(1).getStringCellValue()).contains("Беннет");

        }
    }

    @Test
    void csvParseTest() throws Exception {
        try (
                InputStream resource = cl.getResourceAsStream("qa_guru.csv");
                CSVReader reader = new CSVReader(new InputStreamReader(resource))
        ) {
            List<String[]> content = reader.readAll();
            assertThat(content.get(4)[0]).contains("Kris");
            assertThat(content.get(4)[1]).contains("Grey");
        }
    }

    @Test
    void zipParseTest() throws Exception {
        try (
                InputStream resource = cl.getResourceAsStream("qa_guru.txt.zip");
                ZipInputStream zis = new ZipInputStream(resource);
        ) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                assertThat(entry.getName()).contains("qa_guru.txt");
            }
        }
    }

    @Test
    void zipFileParseTest() throws Exception {
        try (
                InputStream is = cl.getResourceAsStream("arhiv.zip");
                ZipInputStream zis = new ZipInputStream(is)
        ) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains(".pdf")) {
                    PDF content = new PDF(zis);
                    assertThat(content.text).contains("Курс Пимслера:");
                } else if (entry.getName().contains(".xlsx")) {
                    XLS content = new XLS(zis);
                    assertThat(content.excel.getSheetAt(0).getRow(2).getCell(0).getStringCellValue()).contains("Элизабет");
                    assertThat(content.excel.getSheetAt(0).getRow(2).getCell(1).getStringCellValue()).contains("Беннет");
                } else if (entry.getName().contains(".csv")) {
                    CSVReader reader = new CSVReader(new InputStreamReader(zis));
                    List<String[]> content = reader.readAll();
                    assertThat(content.get(4)[0]).contains("Kris");
                    assertThat(content.get(4)[1]).contains("Grey");
                }
            }
        }
    }

    @Test
    void jsonParseTest() throws Exception{
        Gson gson = new Gson();
        try (
                InputStream resource = cl.getResourceAsStream("glossary.json");
                InputStreamReader reader = new InputStreamReader(resource)
        ) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            assertThat(jsonObject.get("title").getAsString()).isEqualTo("example glossary");
            assertThat(jsonObject.get("GlossDiv").getAsJsonObject().get("flag").getAsBoolean()).isTrue();
            assertThat(jsonObject.get("GlossDiv").getAsJsonObject().get("GlossList")
                    .getAsJsonObject().get("GlossEntry")
                    .getAsJsonObject().get("GlossSee")
                    .getAsString()).isEqualTo("markup");
        }
    }

    @Test
    void jsonParseImprovedTest() throws Exception{
        Gson gson = new Gson();
        try (
                InputStream resource = cl.getResourceAsStream("glossary.json");
                InputStreamReader reader = new InputStreamReader(resource)
        ) {
            Glossary jsonObject = gson.fromJson(reader, Glossary.class);
            assertThat(jsonObject.title).isEqualTo("example glossary");
            assertThat(jsonObject.glossDiv.title).isEqualTo("S");
            assertThat(jsonObject.glossDiv.flag).isTrue();
        }
    }

    @Test
    void jsonParseTest1() throws IOException {
            String[] object = {"Продукты", "Лодку"};
            ObjectMapper mapper = new ObjectMapper();

            try (
                    InputStream resource = cl.getResourceAsStream("new.json");
                    InputStreamReader reader = new InputStreamReader(resource)
            ) {
                NewJson newJson = mapper.readValue(reader, NewJson.class);
                assertArrayEquals(object, newJson.getPeoples()[2].getObject());
            }
        }
}
