package telegram;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;


public class BotCommandHelp extends BotCommand {
    public BotCommandHelp() { super("help", "вывожу список доступных команд");
        String fileName = "helpAboutList.txt";

        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            fileWriter.write(getCommandIdentifier()+" - "+getDescription()+"\n");
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Ошибка при записи данных в файл: " + e.getMessage());
        }
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        StringBuilder text = new StringBuilder();
        text.append("Я способен реагировать на команды:"+"\n");

        String fileName = "helpAboutList.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" - ");
                if (parts.length > 0) {
                    String name = parts[0].trim();
                    text.append("/").append(name).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        SendMessage message = new SendMessage();
        message.setText(text.toString());
        message.setChatId(Long.toString(chat.getId()));
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            System.out.println("не получилось отправить из-за:");
            e.printStackTrace();
        }
    }
}