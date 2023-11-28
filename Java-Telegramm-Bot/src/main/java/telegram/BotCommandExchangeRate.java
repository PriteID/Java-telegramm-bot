package telegram;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.Objects;


public class BotCommandExchangeRate extends BotCommand {
    public BotCommandExchangeRate() { super("exchange_rate", "Показывает вам актуальный курс валют(для подробностей напишите exchange_rate)");
        String fileName = "helpAboutList.txt";

        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            fileWriter.write(getCommandIdentifier()+" - "+getDescription()+"\n");
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Ошибка при записи данных в файл: " + e.getMessage());
        }
    }

    private double getExchangeRate(String baseCurrency, String targetCurrency, String DataDay, String DataMonth, String DataYear) {
        double exchangeRate = 0.0;

        try {
            String url = "http://www.cbr.ru/scripts/XML_daily.asp?date_req="+DataDay+"/"+DataMonth+"/"+DataYear;

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(response.toString()));
            Document document = builder.parse(inputSource);

            NodeList valuteNodes = document.getElementsByTagName("Valute");
            double baseCurrencyValue = 0;
            double targetCurrencyValue = 0;

            for (int i = 0; i < valuteNodes.getLength(); i++) {
                Element valuteElement = (Element) valuteNodes.item(i);
                String valuteCharCode = valuteElement.getElementsByTagName("CharCode").item(0).getTextContent();
                if (valuteCharCode.equals(baseCurrency)) {
                    baseCurrencyValue = Double.parseDouble(valuteElement.getElementsByTagName("Value").item(0).getTextContent().replace(",", "."));
                }
                if (valuteCharCode.equals(targetCurrency)) {
                    targetCurrencyValue = Double.parseDouble(valuteElement.getElementsByTagName("Value").item(0).getTextContent().replace(",", "."));
                }
            }

            if (baseCurrency.equals("RUB")) {
                baseCurrencyValue = 1;
            }
            if (targetCurrency.equals("RUB")) {
                targetCurrencyValue = 1;
            }

            exchangeRate = baseCurrencyValue / targetCurrencyValue;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            System.out.println("Ошибка при получении курса валюты: " + e.getMessage());
        }
        return exchangeRate;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments){

        if (arguments != null && arguments.length > 0) {
            boolean dateCheckFlag = false;
            double currencyExchangeRate;
            String DataDay;
            String DataMonth;
            String DataYear;
            String text;
            String baseCurrency = arguments[0];
            String targetCurrency = arguments[1];
            LocalDate currentDate = LocalDate.now();
            String todayDataDay = String.valueOf(currentDate.getDayOfMonth());
            String todayDataMonth = String.valueOf(currentDate.getMonthValue());
            String todayDataYear = String.valueOf(currentDate.getYear());


            if (arguments.length > 2) {
                DataYear = arguments[4];

                if (arguments[2].length() == 1) {
                    DataDay = "0" + arguments[2];
                } else {
                    DataDay = arguments[2];
                }

                if (arguments[3].length() == 1){
                    DataMonth = "0"+arguments[3];
                } else {
                    DataMonth = arguments[3];
                }

                if (Integer.parseInt(DataDay) > Integer.parseInt(todayDataDay)) {
                    dateCheckFlag = true;
                }
                if (Integer.parseInt(DataMonth) < Integer.parseInt(todayDataMonth) && dateCheckFlag) {
                    dateCheckFlag = false;
                } else if(Integer.parseInt(DataMonth) > Integer.parseInt(todayDataMonth)){
                    dateCheckFlag = true;
                }
                if (Integer.parseInt(DataYear) < Integer.parseInt(todayDataYear) && dateCheckFlag) {
                    dateCheckFlag = false;
                } else if(Integer.parseInt(DataYear) > Integer.parseInt(todayDataYear)){
                    dateCheckFlag = true;
                }

            } else {
                DataDay = todayDataDay;
                DataMonth = todayDataMonth;
                DataYear = todayDataYear;
            }

            if (dateCheckFlag) {
                text = "Введена не корректаня или не достигнутая дата!";
            } else {
                currencyExchangeRate =  Math.round(getExchangeRate(baseCurrency,targetCurrency,DataDay,DataMonth,DataYear) * 10000.0) / 10000.0;
                if (currencyExchangeRate == 0) {
                    text = "На заданную вами дату, одну из валют Центральный Банк РФ не отслеживал!";
                } else {
                    text = "1 "+baseCurrency+" соответствует "+Double.toString(currencyExchangeRate)+"\n "+targetCurrency;
                }
            }

            SendMessage message = new SendMessage();
            message.setText(text);
            message.setChatId(Long.toString(chat.getId()));
            try {
                absSender.execute(message);
            } catch (TelegramApiException e) {
                System.out.println("не получилось отправить из-за:");
                e.printStackTrace();
            }

        } else {
            String DataDay;
            String DataMonth;
            String DataYear;
            String text = null;
            LocalDate currentDate = LocalDate.now();
            DataDay = String.valueOf(currentDate.getDayOfMonth());
            DataMonth = String.valueOf(currentDate.getMonthValue());
            DataYear = String.valueOf(currentDate.getYear());
            String url = "http://www.cbr.ru/scripts/XML_daily.asp?date_req="+DataDay+"/"+DataMonth+"/"+DataYear;

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "windows-1251"));
                StringBuilder response = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputSource inputSource = new InputSource(new StringReader(response.toString()));
                Document document = builder.parse(inputSource);

                NodeList valuteNodes = document.getElementsByTagName("Valute");
                String listOfCurrenciesCharCode;
                String listOfCurrenciesName;

                StringBuilder textBuilder = new StringBuilder("Вы можете узнать отношение курса одной валюты к другой валюте: /exchange_rate USD AUD, так вы получите курс Доллара к Австралийскому доллару на сегодняшний день."+"\n"+"Список доступных валют:"+"\n\n");
                textBuilder.append("RUB - Российский рубль\n");
                for (int i = 0; i < valuteNodes.getLength(); i++) {
                    Element valuteElement = (Element) valuteNodes.item(i);
                    listOfCurrenciesCharCode = valuteElement.getElementsByTagName("CharCode").item(0).getTextContent().replace(",", ".");
                    listOfCurrenciesName = valuteElement.getElementsByTagName("Name").item(0).getTextContent().replace(",", ".");
                    System.out.println(listOfCurrenciesName);
                    textBuilder.append(listOfCurrenciesCharCode).append(" - ").append(listOfCurrenciesName).append("\n");
                }
                textBuilder.append("\nТак же вы можете указать дату после заданных валют: /exchange_rate USD AUD 01 01 1999");
                text = textBuilder.toString();
            } catch (IOException | ParserConfigurationException | SAXException e) {
                System.out.println("Ошибка при получении данных: " + e.getMessage());
            }

            SendMessage message = new SendMessage();
            message.setText(Objects.requireNonNull(text));
            message.setChatId(Long.toString(chat.getId()));

            try {
                absSender.execute(message);
            } catch (TelegramApiException e) {
                System.out.println("не получилось отправить из-за:");
                e.printStackTrace();
            }
        }
    }
}
