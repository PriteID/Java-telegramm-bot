package telegram;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.*;


import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class StartCommand extends BotCommand {

    public StartCommand() { super("start", "Команда для запуска бота");
        String filePath = "helpAboutList.txt";
        File file = new File(filePath);
        if (file.exists()) file.delete();
        try {
            if (file.createNewFile()) {
                System.out.println("cписок описания комманд успешно создан");
            } else {
                System.out.println("Не удалось создать список описания команд!");
            }
        } catch (IOException e) {
            System.out.println("Ошибка при создании списка: " + e.getMessage());
        }
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        String text1 = "Здраствуйте, я бот Testbot_1012. Мои возможности вы можете узнать прописав комманду /help";
        String text2 = "Как я могу вам помочь?";

        SendMessage message1 = new SendMessage();
        SendMessage message2 = new SendMessage();
        message1.setText(text1);
        message2.setText(text2);
        message1.setChatId(Long.toString(chat.getId()));
        message2.setChatId(Long.toString(chat.getId()));

        try {
            absSender.execute(message1);
            absSender.execute(message2);
        } catch (TelegramApiException e) {
            System.out.println("Не получилось отправить из-за:");
            e.printStackTrace();
        }
    }

}