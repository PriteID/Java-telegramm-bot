package telegram;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileWriter;
import java.io.IOException;


public class BotCommandAuthors extends BotCommand {
    public BotCommandAuthors() {
        super("authors", "демонстрирую вам списк моих авторов");
        String fileName = "helpAboutList.txt";

        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            fileWriter.write(getCommandIdentifier() + " - " + getDescription() + "\n");
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Ошибка при записи данных в файл: " + e.getMessage());
        }
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        String text = "Мои авторы:" + "\n" + "Лаврентьев Максим " + "\n" + "Волков Михаил " + "\n" + "Боровиков Дмитрий ";
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(Long.toString(chat.getId()));
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            System.out.println("не получилось отправить из-за:");
            e.printStackTrace();
        }
    }
}