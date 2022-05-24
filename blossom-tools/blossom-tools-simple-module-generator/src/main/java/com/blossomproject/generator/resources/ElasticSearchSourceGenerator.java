package com.blossomproject.generator.resources;

import static org.elasticsearch.xcontent.XContentFactory.jsonBuilder;

import com.blossomproject.generator.configuration.model.Field;
import com.blossomproject.generator.configuration.model.Settings;
import com.blossomproject.generator.configuration.model.StringField;
import com.blossomproject.generator.configuration.model.TemporalField;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import org.elasticsearch.xcontent.XContentBuilder;

public class ElasticSearchSourceGenerator implements ResourceGenerator {

  private Path changelogDir;

  private Path fullChangelogPath;

  @Override
  public void prepare(Settings settings) {
    try {
      changelogDir = settings.getResourcePath().resolve("elasticsearch");
      Files.createDirectories(changelogDir);
      fullChangelogPath = changelogDir.resolve(settings.getEntityNameLowerUnderscore() + ".json");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void generate(Settings settings, Map<String, String> params) {
    try {
      XContentBuilder xbMapping = jsonBuilder().prettyPrint()
        .startObject();
      builSettings(xbMapping);
      buildMappings(xbMapping, settings);
      xbMapping.endObject();

      byte[] xMappingBytes = new byte[]{};
      xbMapping.getOutputStream().write(xMappingBytes);
      Files.write(fullChangelogPath, xMappingBytes, StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void builSettings(XContentBuilder xbMapping) throws IOException {
    xbMapping.startObject("settings")
      .field("number_of_shards").value("1")
      .field("number_of_replicas").value("0")
      .startObject("index")
      .startObject("analysis")
      .startObject("filter")
      .startObject("suggest_edgengram_filter")
      .field("type").value("edgeNGram")
      .field("min_gram").value(1)
      .field("max_gram").value(20)
      .endObject()
      .endObject()
      .startObject("analyzer")
      .startObject("email")
      .field("tokenizer").value("lowercase")
      .startArray("filter").value("lowercase").value("suggest_edgengram_filter")
      .endArray()
      .endObject()
      .startObject("default")
      .field("tokenizer").value("standard")
      .startArray("filter").value("lowercase").value("asciifolding").value("elision")
      .endArray()
      .endObject()
      .startObject("suggest")
      .field("tokenizer").value("standard")
      .startArray("filter").value("lowercase").value("asciifolding").value("elision").value("suggest_edgengram_filter")
      .endArray()
      .endObject()
      .endObject()
      .endObject()
      .endObject()
      .endObject();
  }

  private void buildMappings(XContentBuilder xbMapping, Settings settings) throws IOException {
    xbMapping.startObject("mappings")
      .startObject(settings.getEntityNameLowerUnderscore())
      .startObject("properties");
    buildMappingsSummary(xbMapping);
    buildMappingsDto(xbMapping, settings);
    xbMapping.endObject()
      .endObject()
      .endObject();


  }

  private void buildMappingsSummary(XContentBuilder xbMapping) throws IOException {
    xbMapping.startObject("summary")
      .field("type").value("object")
      .startObject("properties")
      .startObject("id")
      .field("type").value("string")
      .field("index").value("false")
      .endObject()
      .startObject("type")
      .field("type").value("string")
      .field("index").value("false")
      .endObject()
      .startObject("name")
      .field("type").value("string")
      .field("analyzer").value("suggest")
      .field("search_analyzer").value("default")
      .endObject()
      .startObject("description")
      .field("type").value("string")
      .field("analyzer").value("suggest")
      .field("search_analyzer").value("default")
      .endObject()
      .startObject("uri")
      .field("type").value("string")
      .field("index").value("false")
      .endObject()
      .endObject()
      .endObject();
  }

  private void buildMappingsDto(XContentBuilder xbMapping, Settings settings) throws IOException {
    xbMapping.startObject("dto")
      .field("type").value("object")
      .startObject("properties")
      .startObject("id")
      .field("type").value("string")
      .field("index").value("false")
      .endObject();

    buildMappingsDtoFields(xbMapping, settings);

    xbMapping.startObject("creationDate")
      .field("type").value("long")
      .endObject()
      .startObject("modificationDate")
      .field("type").value("long")
      .endObject()
      .startObject("creationUser")
      .field("type").value("string")
      .field("index").value("false")
      .endObject()
      .startObject("modificationUser")
      .field("type").value("string")
      .field("index").value("false")
      .endObject()
      .endObject()
      .endObject();
  }

  private void buildMappingsDtoFields(XContentBuilder xbMapping, Settings settings) throws IOException {
    for (Field field : settings.getFields()) {

      if (field.getJdbcType() == "long") {
        xbMapping.startObject(field.getName());
        xbMapping.field("type").value("long");
      } else if (field.getJdbcType() == "integer") {
        xbMapping.startObject(field.getName());
        xbMapping.field("type").value("integer");
      } else if (field instanceof TemporalField) {
        xbMapping.startObject(field.getName());
        xbMapping.field("type").value("long");
      } else if (field.getJdbcType() == "boolean") {
        xbMapping.startObject(field.getName());
        xbMapping.field("type").value("boolean");
      } else if (field instanceof StringField) {
        xbMapping.startObject(field.getName());
        xbMapping.field("type").value("string");
        xbMapping.field("analyzer").value("suggest");
        xbMapping.field("search_analyzer").value("default");
      } else {
        return;
      }
      xbMapping.endObject();
    }
  }
}
