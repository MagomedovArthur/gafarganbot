package core.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import core.updates.DataStorage;
import javassist.NotFoundException;

import static core.updates.BotUpdates.*;

public class ResponseFromButtonsOfSupposedWordsCommandProcessor implements ChatCommandProcessor {

    private final Message message;
    private final TelegramBot bot;
    private final CallbackQuery callbackQuery;

    public ResponseFromButtonsOfSupposedWordsCommandProcessor(Message message, TelegramBot bot, CallbackQuery callbackQuery) {
        this.message = message;
        this.bot = bot;
        this.callbackQuery = callbackQuery;
    }

    @Override
    public void execute() throws NotFoundException {
        var chatId = message.chat().id();
        String userMessage = callbackQuery.data();
        var language = DataStorage.instance().getLastSelectedDictionary(chatId);
        WordSearchCommandProcessor wordSearch = new WordSearchCommandProcessor(message, bot);
        switch (language) {
            case CommandsList.LEZGI_RUS -> {
                wordSearch.sendAnswerToUser(lezgiRusDictionary, userMessage, chatId);
                bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
            }
            case CommandsList.RUS_LEZGI -> {
                wordSearch.sendAnswerToUser(rusLezgiDictionary, userMessage, chatId);
                bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
            }
            default -> {
            }
        }
    }
}