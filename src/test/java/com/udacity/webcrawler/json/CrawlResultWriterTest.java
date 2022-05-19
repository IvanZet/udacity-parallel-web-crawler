package com.udacity.webcrawler.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udacity.webcrawler.testing.CloseableStringWriter;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

public final class CrawlResultWriterTest {
  @Test
  public void testBasicJsonFormatting() throws Exception {
    // We are using a LinkedHashMap because the iteration order of the map matters.
    Map<String, Integer> counts = new LinkedHashMap<>();
    counts.put("foo", 12);
    counts.put("bar", 1);
    counts.put("foobar", 98);
    CrawlResult result =
        new CrawlResult.Builder()
            .setUrlsVisited(17)
            .setWordCounts(counts)
            .build();

    CrawlResultWriter resultWriter = new CrawlResultWriter(result);
    CloseableStringWriter stringWriter = new CloseableStringWriter();
    resultWriter.write(stringWriter);
    assertWithMessage("Streams should usually be closed in the same scope where they were created")
        .that(stringWriter.isClosed())
        .isFalse();
    String written = stringWriter.toString();

    // The purpose of all the wildcard matchers (".*") is to make sure we allow the JSON output to
    // contain extra whitespace where it does not matter.
    Pattern expected =
        Pattern.compile(".*\\{" +
            ".*\"wordCounts\".*:.*\\{" +
            ".*\"foo\".*:12.*," +
            ".*\"bar\".*:.*1," +
            ".*\"foobar\".*:.*98" +
            ".*}.*,.*" +
            ".*\"urlsVisited\".*:.*17" +
            ".*}.*", Pattern.DOTALL);

    assertThat(written).matches(expected);
  }

  // FIXME: tests assumes that in SUT, data not appended, but overwritten
  @Test
  public void testBasicJsonFormattingPath() throws Exception {
    Map<String, Integer> counts = new LinkedHashMap<>();
    counts.put("foo", 12);
    counts.put("bar", 1);
    counts.put("foobar", 98);
    CrawlResult result =
            new CrawlResult.Builder()
                    .setUrlsVisited(17)
                    .setWordCounts(counts)
                    .build();
    Path writePath = Path.of(System.getProperty("user.dir") +
            "/src/test/java/com/udacity/webcrawler/json/crawl-result.json");
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);
    resultWriter.write(writePath);
    // Expected written data
    Path fixturePath = Path.of(System.getProperty("user.dir") +
            "/src/test/java/com/udacity/webcrawler/json/crawl-result-fixture.json");
    // Compare SUTs output and fixture
    Object written;
    Object fixture;
    var objectMapper = new ObjectMapper();
    try (BufferedReader reader1 = Files.newBufferedReader(writePath);
         BufferedReader reader2 = Files.newBufferedReader(fixturePath)) {
      written = objectMapper.readValue(reader1, Object.class);
      fixture = objectMapper.readValue(reader2, Object.class);
    }
    assertThat(written).isEqualTo(fixture);
  }
}
