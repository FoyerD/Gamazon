package Application.utils;
import java.util.Arrays;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class IntArrayConverter implements AttributeConverter<int[], String> {

    @Override
    public String convertToDatabaseColumn(int[] attribute) {
        if (attribute == null || attribute.length == 0) return "";
        return Arrays.stream(attribute)
                     .mapToObj(String::valueOf)
                     .collect(Collectors.joining(","));
    }

    @Override
    public int[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return new int[0];
        return Arrays.stream(dbData.split(","))
                     .mapToInt(Integer::parseInt)
                     .toArray();
    }
}
