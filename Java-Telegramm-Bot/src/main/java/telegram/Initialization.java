package telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Initialization {
    public static void start() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new BotConnection());
            System.out.println("инициализация бота прошла успешно");
        } catch (TelegramApiException e) {
            System.out.println("инициализация бота не удалась, из-за:");
            e.printStackTrace();
        }
    }
}