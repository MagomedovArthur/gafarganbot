package core.searchers;

import core.dictionary.parser.DictionaryRepository;
import core.dictionary.model.Definition;
import core.dictionary.model.DefinitionDetails;
import core.dictionary.model.ExpressionDetails;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static core.utils.MarkupLineEditor.convertMarkupToHTML;
import static core.utils.WordCapitalize.*;

public class SearchBySpelling {

    public Response findResponseBySpelling(String lang, DictionaryRepository dictionaries, String spelling) {
        List<ExpressionDetails> expressionDetails = dictionaries.getExpressionDetails(lang, spelling);
        if (expressionDetails == null) {
            return null;
        }
        int amountOfDetails = 1;
        boolean expressionExample = false;
        StringBuilder tags = new StringBuilder();
        StringBuilder outputMessage = new StringBuilder();
        for (ExpressionDetails details : expressionDetails) {
            /* Если слово есть в словаре, но нет перевода, вывести "перевод не найден" */
            if (details.getDefinitionDetails().isEmpty() && details.getExamples() == null) {
                return new Response("<b>❌Таржума жагъанач</b>");
            }
            /* Если к слову отсутвуют переводы, есть только examples, то вывести example */
            if ((details.getDefinitionDetails().size() == 0 || details.getDefinitionDetails() == null)
                && details.getExamples() != null) {
                outputMessage.append(capitalizeFirstLetter(spelling));
                return new Response(sendOnlyExamples(outputMessage, details));
            }
            /* Если у слова несколько или одно значений, то пронумеровать */
            markupExpressionsSpelling(spelling, amountOfDetails, outputMessage, expressionDetails, details);
            for (DefinitionDetails definitionDetails : details.getDefinitionDetails()) {
                StringBuilder definitionValues = new StringBuilder();
                for (Definition definitions : definitionDetails.getDefinitions()) {
                    boolean isTagNull = definitions.getTags() == null;
                    tags.append(!isTagNull && definitions.getTags().contains("см.")
                            ? definitions.getValue().replaceAll("\\{", "").replaceAll("}", "") + ", "
                            : ""
                    );
                    definitionValues.append(isTagNull || !definitions.getTags().contains("см.")
                            ? definitions.getValue().replaceAll("[<{]", "[").replaceAll("[>}]", "]") + ", "
                            : "");
                }
                if (!definitionValues.isEmpty()) { /* Присваиваем value и удаляем запятую на конце */
                    outputMessage
                            .append("➡️<b>️ ")
                            .append(definitionValues.deleteCharAt(definitionValues.length() - 2))
                            .append("</b>\n\n");
                }
                if (definitionDetails.getExamples() != null) {
                    definitionDetails.getExamples().forEach(example -> {
                        outputMessage.append(convertMarkupToHTML(example.getRaw())).append("\n");
                    });
                    outputMessage.append("\n");
                }
            }
            if (!expressionExample) {
                expressionExample = details.getExamples() != null;
            }
            amountOfDetails++;
        }
        /* Если теги есть, присваиваем к концу ответной строки */
        if (!tags.isEmpty()) {
            outputMessage.append(createTags(tags));
        }
        /* Выводим ответ с наличием общих примеров */
        if (expressionExample) {
            return new Response(outputMessage.toString().replaceAll("ё", "е"),
                    List.of(spelling.toLowerCase() + "=example"));
        } else {
            return new Response(outputMessage.toString());
        }
    }

    private void markupExpressionsSpelling(String userMessage, int amountOfDetails, StringBuilder outputMessage,
                                           List<ExpressionDetails> expressionDetails, ExpressionDetails details) {
        if (expressionDetails.size() > 1) {
            outputMessage.append(details.getInflection() != null
                    ? capitalizeFirstLetterWithNum(amountOfDetails + ". ", userMessage + " (" + details.getInflection() + ")")
                    : capitalizeFirstLetterWithNum(amountOfDetails + ". ", userMessage));
        } else {
            outputMessage.append(details.getInflection() != null
                    ? capitalizeFirstLetter(userMessage + " (" + details.getInflection() + ")")
                    : capitalizeFirstLetter(userMessage));
        }
    }

    private String sendOnlyExamples(StringBuilder outputMessage, ExpressionDetails details) {
        details.getExamples().forEach(example -> {
            outputMessage.append(convertMarkupToHTML(example.getRaw())).append("\n");
        });
        return outputMessage.toString().replaceAll("ё", "е");
    }

    private StringBuilder createTags(StringBuilder tags) {
        StringBuilder result = new StringBuilder("\n<i>мадни клг.:</i> ");
        String[] tagArr = tags.toString().split(", ");
        Set<String> tagList = new HashSet<>(Arrays.asList(tagArr));
        for (String tag : tagList) {
            if (!tag.isEmpty()) {
                result.append("<code>").append(tag).append("</code>").append(", ");
            }
        }
        return result.deleteCharAt(result.length() - 2);
    }
}