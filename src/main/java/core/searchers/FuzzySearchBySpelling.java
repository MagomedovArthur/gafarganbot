package core.searchers;

import core.dictionary.parser.DictionaryRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static core.searchers.StringSimilarity.similarity;

public class FuzzySearchBySpelling {

    public Response findSimilarWordsBySpelling(String lang, DictionaryRepository dictionary, String userMessage) {
        record WordSim(String supposedWord, Double sim) {
        }
        final List<WordSim> wordList = dictionary.getDictionaryByLang(lang).keySet().stream()
                .parallel()
                .map(supposedWord -> new WordSim(supposedWord.replaceAll("i", "I"), similarity(supposedWord, userMessage.toLowerCase())))
                .filter(wordSim -> wordSim.sim() >= 0.5)
                .sorted(Comparator.comparing(WordSim::sim).reversed())
                .limit(7)
                .toList();
        if (wordList.isEmpty()) {
            return new Response("<b>❌Жагъай гаф авач</b>");
        }
        final List<String> supposedWords = wordList.stream()
                .map(WordSim::supposedWord)
                .toList();
        return new Response("\uD83E\uDD14жагъай гаф авач, ибуруз килиг:\n", supposedWords);
    }
}