package telegram;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class BotCommandAbout extends BotCommand {
    public BotCommandAbout() {
        super("about", "кратко описываю выбранную вами комманду");
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
        StringBuilder text = new StringBuilder();
        if (arguments != null && arguments.length > 0) {
            String commandName = arguments[0];
            String fileName = "helpAboutList.txt";

            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String line;
                boolean foundCommand = false;
                boolean CommandIsFound = false;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" - ");
                    for (String part : parts) {
                        if (foundCommand) {
                            text.append(commandName).append(" - ");
                            text.append(part);
                            foundCommand = false;
                            CommandIsFound = true;
                            break;
                        }
                        if (part.contains(commandName)) {
                            foundCommand = true;
                        }
                    }
                }
                if (!CommandIsFound) {
                    text.append("Такую команду я не смогу выполнить. Проверьте правильность написания выбранной команды");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            text.append("Введите название команды без / после about");
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
///