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

import java.text.SimpleDateFormat;
import java.util.Date;


public class BotCommandExchangeRate extends BotCommand {
    UserCurrencyMemory userCurrencyMemory = new UserCurrencyMemory();
    public BotCommandExchangeRate() {
        super("exchange_rate", "Показывает вам актуальный курс валют(для подробностей напишите exchange_rate)");
        String fileName = "helpAboutList.txt";

        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            fileWriter.write(getCommandIdentifier() + " - " + getDescription() + "\n");
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Ошибка при записи данных в файл: " + e.getMessage());
        }
    }

    public static boolean validateDate(String dataDay, String dataMonth, String dataYear, String todayDataDay, String todayDataMonth, String todayDataYear) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            dateFormat.setLenient(false);

            String inputDate = String.format("%02d.%02d.%04d", Integer.parseInt(dataDay), Integer.parseInt(dataMonth), Integer.parseInt(dataYear));
            String todayDate = String.format("%02d.%02d.%04d", Integer.parseInt(todayDataDay), Integer.parseInt(todayDataMonth), Integer.parseInt(todayDataYear));

            Date date = dateFormat.parse(inputDate);
            Date today = dateFormat.parse(todayDate);

            return date.after(today) || date.equals(today);
        } catch (Exception e) {
            return true;
        }
    }

    public double getExchangeRate(String baseCurrency, String targetCurrency, String dataDay, String dataMonth, String dataYear) {
        double exchangeRate = 0.0;

        try {

            if (dataDay.length() == 1) {
                dataDay = "0" + dataDay;
            }
            if (dataMonth.length() == 1) {
                dataMonth = "0" + dataMonth;
            }

            String url = "http://www.cbr.ru/scripts/XML_daily.asp?date_req=" + dataDay + "/" + dataMonth + "/" + dataYear;

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

            if (targetCurrencyValue != 0.0) {
                exchangeRate = baseCurrencyValue / targetCurrencyValue;
            } else {
                exchangeRate = 0.0;
            }

        } catch (IOException | ParserConfigurationException | SAXException e) {
            System.out.println("Ошибка при получении курса валюты: " + e.getMessage());
        }
        return exchangeRate;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        int userId = Math.toIntExact(user.getId());

        if (arguments.length != 0) {
            boolean dateCheckFlag = false;
            boolean correctValueFlag = false;
            double currencyExchangeRate;
            LocalDate currentDate = LocalDate.now();
            String dataDay = null;
            String dataMonth = null;
            String dataYear = null;
            String text;
            String baseCurrency = arguments[0];
            String targetCurrency = null;
            if(arguments.length != 1)
                targetCurrency = arguments[1];
            String todaydataDay = String.valueOf(currentDate.getDayOfMonth());
            String todayDataMonth = String.valueOf(currentDate.getMonthValue());
            String todayDataYear = String.valueOf(currentDate.getYear());
            switch (arguments.length){
                case(5):{
                    userCurrencyMemory.rememberCurrency(userId, baseCurrency);
                    userCurrencyMemory.rememberCurrency(userId, targetCurrency);
                    dataDay = arguments[2];
                    dataMonth = arguments[3];
                    dataYear = arguments[4];
                    dateCheckFlag = validateDate(dataDay, dataMonth, dataYear, todaydataDay, todayDataMonth, todayDataYear);
                    break;
                }
                case(4):{
                    targetCurrency = userCurrencyMemory.getMostUsedCurrency(userId);
                    dataDay = arguments[1];
                    dataMonth = arguments[2];
                    dataYear = arguments[3];
                    dateCheckFlag = validateDate(dataDay, dataMonth, dataYear, todaydataDay, todayDataMonth, todayDataYear);
                    break;
                }
                case(2):{
                    userCurrencyMemory.rememberCurrency(userId, baseCurrency);
                    userCurrencyMemory.rememberCurrency(userId, targetCurrency);
                    dataDay = todaydataDay;
                    dataMonth = todayDataMonth;
                    dataYear = todayDataYear;
                    break;
                }
                case(1):{
                    targetCurrency = userCurrencyMemory.getMostUsedCurrency(userId);
                    dataDay = todaydataDay;
                    dataMonth = todayDataMonth;
                    dataYear = todayDataYear;
                    break;
                }
                default: {
                    correctValueFlag = true;
                    break;
                }
            }

            if (correctValueFlag) {
                text = "Вы ввели неподдерживаемые символы, проверьте корректность написанного!";
            } else {
                if (dateCheckFlag) {
                    text = "Дата введена некорректно или дата еще не наступила!";
                } else {
                    currencyExchangeRate = Math.round(getExchangeRate(baseCurrency, targetCurrency, dataDay, dataMonth, dataYear) * 10000.0) / 10000.0;
                    if (currencyExchangeRate == 0) {
                        text = "На заданную вами дату Центральный Банк РФ не отслеживал курс одной из валют!";
                    } else {
                        text = "1 " + baseCurrency + " соответствует " + Double.toString(currencyExchangeRate) + "\n " + targetCurrency;
                    }
                }
            }

            SendMessage message = new SendMessage();
            message.setText(text);
            message.setChatId(Long.toString(chat.getId()));
            try {
                absSender.execute(message);
            } catch (TelegramApiException e) {
                System.out.println("Не удалось отправить сообщение из-за ошибки:");
                e.printStackTrace();
            }

        } else {
            String dataDay;
            String dataMonth;
            String dataYear;
            String text = null;
            LocalDate currentDate = LocalDate.now();
            dataDay = String.valueOf(currentDate.getDayOfMonth());
            dataMonth = String.valueOf(currentDate.getMonthValue());
            dataYear = String.valueOf(currentDate.getYear());

            if (dataDay.length() == 1) {
                dataDay = "0" + dataDay;
            }
            if (dataMonth.length() == 1) {
                dataMonth = "0" + dataMonth;
            }

            String url = "http://www.cbr.ru/scripts/XML_daily.asp?date_req=" + dataDay + "/" + dataMonth + "/" + dataYear;

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

                StringBuilder textBuilder = new StringBuilder("Вы можете узнать отношение курса одной валюты к другой валюте: /exchange_rate USD AUD, так вы получите курс Доллара к Австралийскому доллару на сегодняшний день." + "\n" + "Список доступных валют:" + "\n\n");
                textBuilder.append("RUB - Российский рубль\n");
                for (int i = 0; i < valuteNodes.getLength(); i++) {
                    Element valuteElement = (Element) valuteNodes.item(i);
                    listOfCurrenciesCharCode = valuteElement.getElementsByTagName("CharCode").item(0).getTextContent().replace(",", ".");
                    listOfCurrenciesName = valuteElement.getElementsByTagName("Name").item(0).getTextContent().replace(",", ".");
                    System.out.println(listOfCurrenciesName);
                    textBuilder.append(listOfCurrenciesCharCode).append(" - ").append(listOfCurrenciesName).append("\n");
                }
                textBuilder.append("\nТакже вы можете указать дату после заданных валют: /exchange_rate USD AUD 01 01 1999, также вы можете в дальнейшем указывать лишь одну валюту /exchange_rate AUD и вы получите курс заданной вами валюты относительно валюты которую вы чаще используете, но учтите, что введенная самая первая вами валюта будет выбрана как главная");
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
                System.out.println("Не удалось отправить сообщение из-за ошибки:");
                e.printStackTrace();
            }
        }
    }
}