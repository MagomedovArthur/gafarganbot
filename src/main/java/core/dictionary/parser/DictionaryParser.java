package core.dictionary.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.dictionary.model.Dictionary;
import core.dictionary.model.Expression;
import core.dictionary.model.ExpressionDetails;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryParser {

    public static Map<String, List<ExpressionDetails>> parse(String fileName) throws IOException {
        Map<String, List<ExpressionDetails>> dictionaryMap = new HashMap<>();
        /* Читаем JSON из файла */
        InputStream is = DictionaryParser.class.getClassLoader().getResourceAsStream(fileName);
        String json = readJsonFromFile(is);
        /* Парсим его */
        ObjectMapper objectMapper = new ObjectMapper();
        Dictionary dictionary = objectMapper.readValue(json, Dictionary.class);
        List<Expression> expressions = dictionary.getExpressions();
        for (Expression expression : expressions) {
            dictionaryMap.put(expression.getSpelling().toLowerCase().replaceAll("ё", "е"),
                    expression.getDetails());
        }
        return dictionaryMap;
    }

    private static String readJsonFromFile(InputStream is) throws IOException {
        try (InputStreamReader streamReader =
                     new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}