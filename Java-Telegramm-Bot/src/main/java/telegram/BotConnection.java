package telegram;

import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.objects.Update;


public class BotConnection extends TelegramLongPollingCommandBot {

    public BotConnection() {
        register(new StartCommand());
        register(new BotCommandHelp());
        register(new BotCommandAbout());
        register(new BotCommandAuthors());
    }

    @Override
    public void processNonCommandUpdate(Update update) {
            System.out.println("отсутствует команда: " + update.getMessage().getText());
    }

    @Override
    public String getBotUsername() {
        return new ConstantsBot().NAME;
    }

    @Override
    public String getBotToken() {
        return new ConstantsBot().TOKEN;
    }
}