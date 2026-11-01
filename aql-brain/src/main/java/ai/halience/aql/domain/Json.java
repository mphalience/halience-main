package ai.halience.aql.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * JSON column converters. Flexible, product-extensible parts of the shared contracts (claims, payloads, lineage, refs)
 * are stored as JSON text so the semantic contract can grow without schema churn, and so the store stays portable
 * between H2 and PostgreSQL.
 */
public final class Json {

    public static final JsonMapper MAPPER = JsonMapper.builder().build();

    private Json() {
    }

    public static String write(Object value) {
        return MAPPER.writeValueAsString(value);
    }

    public static Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }
        return MAPPER.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {
        });
    }

    @Converter
    public static class MapConverter implements AttributeConverter<Map<String, Object>, String> {
        @Override
        public String convertToDatabaseColumn(Map<String, Object> attribute) {
            return attribute == null ? null : write(attribute);
        }

        @Override
        public Map<String, Object> convertToEntityAttribute(String dbData) {
            return readMap(dbData);
        }
    }

    @Converter
    public static class StringListConverter implements AttributeConverter<List<String>, String> {
        @Override
        public String convertToDatabaseColumn(List<String> attribute) {
            return attribute == null ? null : write(attribute);
        }

        @Override
        public List<String> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isBlank()) {
                return new ArrayList<>();
            }
            return MAPPER.readValue(dbData, new TypeReference<ArrayList<String>>() {
            });
        }
    }

    @Converter
    public static class ObjectListConverter implements AttributeConverter<List<Map<String, Object>>, String> {
        @Override
        public String convertToDatabaseColumn(List<Map<String, Object>> attribute) {
            return attribute == null ? null : write(attribute);
        }

        @Override
        public List<Map<String, Object>> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isBlank()) {
                return new ArrayList<>();
            }
            return MAPPER.readValue(dbData, new TypeReference<ArrayList<Map<String, Object>>>() {
            });
        }
    }
}
