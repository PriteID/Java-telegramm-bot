package telegram;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static telegram.BotCommandCurrencyChart.*;

public class BotConnection extends TelegramLongPollingCommandBot {

    public BotConnection() {
        register(new StartCommand());
        register(new BotCommandHelp());
        register(new BotCommandAbout());
        register(new BotCommandAuthors());
        register(new BotCommandExchangeRate());
        register(new BotCommandCurrencyChart());
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            if (callbackData.startsWith("remove_currency_")) {
                String currencyToRemove = callbackData.replace("remove_currency_", "");
                List<String> seriesNames = ((BotCommandCurrencyChart) getRegisteredCommand("currency_chart")).getSeriesNames();
                int startYear = ((BotCommandCurrencyChart) getRegisteredCommand("currency_chart")).getStartYear();
                List<String> updatedCurrencyNames = new ArrayList<>(seriesNames);
                updatedCurrencyNames.removeIf(currency -> currency.equals(currencyToRemove));
                List<List<Integer[]>> updatedYValues = getCurrencyValues(startYear, updatedCurrencyNames);
                List<Integer> xValues = generateXData(updatedYValues);
                XYChart updatedChart = createChart(xValues, updatedYValues, updatedCurrencyNames);
                updatedChart.getStyler().setPlotGridLinesVisible(true);
                updatedChart.getStyler().setPlotGridLinesColor(new Color(200, 200, 200));

                try {
                    Long chatId = update.getCallbackQuery().getMessage().getChatId();
                    Integer messageId = update.getCallbackQuery().getMessage().getMessageId() - 1; // ID предыдущего сообщения
                    Integer messageId1 = update.getCallbackQuery().getMessage().getMessageId();
                    DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), messageId);
                    DeleteMessage deleteMessage1 = new DeleteMessage(chatId.toString(), messageId1);
                    execute(deleteMessage);
                    execute(deleteMessage1);
                    File updatedChartImage = new File("C:\\Users\\paulj\\IdeaProjects\\Java-telegramm-bot1\\Java-Telegramm-Bot\\chart.png");
                    BitmapEncoder.saveBitmap(updatedChart, updatedChartImage.getAbsolutePath(), BitmapEncoder.BitmapFormat.PNG);
                    InputFile updatedPhoto = new InputFile(updatedChartImage);
                    SendPhoto sendUpdatedPhoto = new SendPhoto();
                    sendUpdatedPhoto.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
                    sendUpdatedPhoto.setPhoto(updatedPhoto);
                    execute(sendUpdatedPhoto);
                } catch (IOException | TelegramApiException e) {
                    System.out.println("Ошибка при отправке обновленного изображения: " + e.getMessage());
                }
                SendMessage message = new SendMessage();
                message.setText("Вы удалили валюту " + currencyToRemove + ". Хотите добавить её обратно?");
                message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
                InlineKeyboardMarkup addCurrencyKeyboard = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                List<InlineKeyboardButton> firstRow = new ArrayList<>(); // Создаем новый список для первой строки
                InlineKeyboardButton addButton = new InlineKeyboardButton();
                addButton.setText("Добавить " + currencyToRemove);
                addButton.setCallbackData("add_currency_" + currencyToRemove);
                firstRow.add(addButton); // Добавляем кнопку "Добавить" в первую строку

                for (String currency : updatedCurrencyNames) {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText("Удалить " + currency);
                    button.setCallbackData("remove_currency_" + currency);
                    firstRow.add(button); // Добавляем кнопку "Удалить" в первую строку
                }

                rowList.add(firstRow); // Добавляем первую строку в список строк клавиатуры
                addCurrencyKeyboard.setKeyboard(rowList);
                message.setReplyMarkup(addCurrencyKeyboard);
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    System.out.println("Ошибка при отправке сообщения: " + e.getMessage());
                }
            } else if (callbackData.startsWith("add_currency_")) {
                List<String> seriesNames = ((BotCommandCurrencyChart) getRegisteredCommand("currency_chart")).getSeriesNames();
                int startYear = ((BotCommandCurrencyChart) getRegisteredCommand("currency_chart")).getStartYear();
                List<List<Integer[]>> updatedYValues = getCurrencyValues(startYear, seriesNames);
                List<Integer> xValues = generateXData(updatedYValues);
                XYChart updatedChart = createChart(xValues, updatedYValues, seriesNames);
                updatedChart.getStyler().setPlotGridLinesVisible(true);
                updatedChart.getStyler().setPlotGridLinesColor(new Color(200, 200, 200));
                try {
                    Long chatId = update.getCallbackQuery().getMessage().getChatId();
                    Integer messageId = update.getCallbackQuery().getMessage().getMessageId() - 1; // ID предыдущего сообщения
                    Integer messageId1 = update.getCallbackQuery().getMessage().getMessageId();
                    DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), messageId);
                    DeleteMessage deleteMessage1 = new DeleteMessage(chatId.toString(), messageId1);
                    execute(deleteMessage);
                    execute(deleteMessage1);
                    File updatedChartImage = new File("C:\\Users\\paulj\\IdeaProjects\\Java-telegramm-bot1\\Java-Telegramm-Bot\\chart.png");
                    BitmapEncoder.saveBitmap(updatedChart, updatedChartImage.getAbsolutePath(), BitmapEncoder.BitmapFormat.PNG);
                    InputFile updatedPhoto = new InputFile(updatedChartImage);
                    SendPhoto sendUpdatedPhoto = new SendPhoto();
                    sendUpdatedPhoto.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
                    sendUpdatedPhoto.setPhoto(updatedPhoto);
                    execute(sendUpdatedPhoto);
                } catch (IOException | TelegramApiException e) {
                    System.out.println("Ошибка при отправке обновленного изображения: " + e.getMessage());
                }
            }
        } else {
            System.out.println("отсутствует команда: " + update.getMessage().getText());
        }
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