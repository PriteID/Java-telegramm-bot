package telegram;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.awt.*;
import java.io.*;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static java.lang.Float.NaN;

public class BotCommandCurrencyChart extends BotCommand {
    private int startYear;
    private List<String> seriesNames;
    public int getStartYear(){
        return startYear;
    }
    public List<String> getSeriesNames() {
        return seriesNames;
    }

    public BotCommandCurrencyChart() {
        super("currency_chart", "Показывает вам как менялся курс заданной/ых вами валют с заданного вами года. Пример: /currency_chart 2020 USD AUD ZAR RSD JPY");
        String fileName = "helpAboutList.txt";

        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            fileWriter.write(getCommandIdentifier() + " - " + getDescription() + "\n");
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Ошибка при записи данных в файл: " + e.getMessage());
        }
    }

    public static List<List<Integer[]>> getCurrencyValues(int startYear, List<String> seriesNames) {
        List<List<Integer[]>> yValues = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (String currencyCode : seriesNames) {
            List<Integer[]> currencyValues = new ArrayList<>();
            for (int year = startYear; year <= currentDate.getYear(); year++) {
                for (int month = 1; month <= 12; month += 2) {
                    if (month == 7 || month == 9 || month == 11) {
                        continue;
                    }

                    LocalDate date = LocalDate.of(year, month, 1);
                    String formattedDate = date.format(formatter);
                    String url = "http://www.cbr.ru/scripts/XML_daily.asp?date_req=" + formattedDate;

                    try {
                        String valueString = getString(currencyCode, url);
                        double value = Double.parseDouble(valueString.replace(",", "."));

                        int currencyValue = (int) value;
                        currencyValues.add(new Integer[]{currencyValue});

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            yValues.add(currencyValues);
            System.out.println("Done for " + currencyCode);
        }

        return yValues;
    }

    static String getString(String currencyCode, String url) throws IOException {
        URL obj = new URL(url);
        String xmlResponse = getXmlResponse(obj);
        int startIndex = xmlResponse.indexOf("<CharCode>" + currencyCode + "</CharCode>");
        int valueIndex = xmlResponse.indexOf("<Value>", startIndex);
        int endIndex = xmlResponse.indexOf("</Value>", valueIndex);
        String valueString = xmlResponse.substring(valueIndex + 7, endIndex);
        return valueString;
    }

    private static String getXmlResponse(URL obj) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        reader.close();

        String xmlResponse = response.toString();
        return xmlResponse;
    }

    public static XYChart createChart(List<Integer> xValues, List<List<Integer[]>> yValues, List<String> seriesNames) {
        XYChart chart = new XYChartBuilder().width(1080).height(1080).title("График").xAxisTitle("X").yAxisTitle("Y").build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setLegendFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));

        for (int i = 0; i < yValues.size(); i++) {
            List<Integer[]> y = yValues.get(i);
            String seriesName = seriesNames.get(i);
            List<Integer> filteredY = new ArrayList<>();
            for (Integer[] value : y) {
                if (isInteger(value)) {
                    filteredY.add(value[0]);
                }
            }
            XYSeries series = chart.addSeries(seriesName, xValues.subList(0, filteredY.size()), filteredY);
            series.setLineColor(getRandomColor());
        }

        return chart;
    }

    public static boolean isInteger(Integer[] value) {
        try {
            Integer.parseInt(String.valueOf(value[0]));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    public static List<Integer> generateXData(List<List<Integer[]>> yValues) {
        int maxLength = yValues.stream().mapToInt(List::size).max().orElse(0);
        List<Integer> x = new ArrayList<>();
        for (int i = 1; i <= maxLength; i++) {
            x.add(i);
        }
        return x;
    }

    public static Color getRandomColor() {
        int r = (int) (Math.random() * 256);
        int g = (int) (Math.random() * 256);
        int b = (int) (Math.random() * 256);
        return new Color(r, g, b);
    }

    public static List<String> getNames(){
        List<String> arrNames = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();
        String dataDay = String.valueOf(currentDate.getDayOfMonth());
        String dataMonth = String.valueOf(currentDate.getMonthValue());
        String dataYear = String.valueOf(currentDate.getYear());

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

            for (int i = 0; i < valuteNodes.getLength(); i++) {
                Element valuteElement = (Element) valuteNodes.item(i);
                listOfCurrenciesCharCode = valuteElement.getElementsByTagName("CharCode").item(0).getTextContent().replace(",", ".");
                arrNames.add(listOfCurrenciesCharCode);
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            System.out.println("Ошибка при получении данных: " + e.getMessage());
        }
        return arrNames;
    }

    public static boolean checkNumberInRange(String input) {
        LocalDate currentDate = LocalDate.now();
        int todayDataYear = currentDate.getYear();
        try {
            int number = Integer.parseInt(input);
            if (number == NaN) {
                return false;
            }
            if (number < 1999 || number > todayDataYear) {
                return false;
            } else {
                return true;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static InlineKeyboardMarkup createCurrencyRemovalKeyboard(List<String> currencyNames) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        for (String currency : currencyNames) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Удалить " + currency);
            button.setCallbackData("remove_currency_" + currency);
            row.add(button);
            rowList.add(row);
        }

        keyboardMarkup.setKeyboard(rowList);
        return keyboardMarkup;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        List<String> correctNames = getNames();
        boolean flagConfrimMessege = false;
        if (arguments.length < 2) {
            StringBuilder text = new StringBuilder("Введите сначала год, а потом любое колл-во волют, график которых вы бы хотели отследить. Например: /currency_chart 2021 USD AUD \nСписок доступных валют:\n");
            for (String i : correctNames) {
                text.append(i).append("\n");
            }
            SendMessage message = new SendMessage();
            message.setText(Objects.requireNonNull(text.toString()));
            message.setChatId(Long.toString(chat.getId()));

            try {
                absSender.execute(message);
            } catch (TelegramApiException e) {
                System.out.println("не получилось отправить из-за:");
                e.printStackTrace();
            }
            flagConfrimMessege = true;
        }
        boolean flagCorrectYear = checkNumberInRange(arguments[0]);

        if (flagCorrectYear && !flagConfrimMessege) {
            if (arguments[0].isEmpty()) {
                StringBuilder text = new StringBuilder("Введите сначала год, а потом любое колл-во волют, график которых вы бы хотели отследить. Например: /currency_chart 2021 USD AUD \nСписок доступных валют:\n");
                for (String i : correctNames) {
                    text.append(i).append("\n");
                }
                SendMessage message = new SendMessage();
                message.setText(Objects.requireNonNull(text.toString()));
                message.setChatId(Long.toString(chat.getId()));

                try {
                    absSender.execute(message);
                } catch (TelegramApiException e) {
                    System.out.println("не получилось отправить из-за:");
                    e.printStackTrace();
                }
            }
            startYear = Integer.parseInt(arguments[0]);
            arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
            seriesNames = new ArrayList<>(Arrays.asList(arguments));
            List<String> wrongNames = new ArrayList<>();
            boolean correctFlag = true;
            for (String i : seriesNames) {
                if (correctNames.contains(i)) {
                } else {
                    correctFlag = false;
                    wrongNames.add(i);
                }
            }

            if (correctFlag) {
                List<List<Integer[]>> yValues = getCurrencyValues(startYear, seriesNames);

                List<Integer> xValues = generateXData(yValues);

                XYChart chart = createChart(xValues, yValues, seriesNames);
                chart.getStyler().setPlotGridLinesVisible(true);
                chart.getStyler().setPlotGridLinesColor(new Color(200, 200, 200));

                try {
                    File chartImage = new File("C:\\Users\\paulj\\IdeaProjects\\Java-telegramm-bot1\\Java-Telegramm-Bot\\chart.png");
                    BitmapEncoder.saveBitmap(chart, chartImage.getAbsolutePath(), BitmapEncoder.BitmapFormat.PNG);

                    InputFile photo = new InputFile(chartImage);
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(chat.getId().toString());
                    sendPhoto.setPhoto(photo);
                    absSender.execute(sendPhoto);
                } catch (IOException | TelegramApiException e) {
                    System.out.println("Ошибка при отправке изображения: " + e.getMessage());
                }
                List<String> currencyName = new ArrayList<>(Arrays.asList(arguments));
                InlineKeyboardMarkup removalKeyboard = createCurrencyRemovalKeyboard(currencyName);
                SendMessage message = new SendMessage();
                message.setText("Теперь вы можете выбрать валюту которую хотите убрать с графика");
                message.setChatId(Long.toString(chat.getId()));
                message.setReplyMarkup(removalKeyboard);
                try {
                    absSender.execute(message);
                } catch (TelegramApiException e) {
                    System.out.println("Ошибка при создании кнопок: " + e.getMessage());
                }
            } else {
                String text;
                text = ("Вы ввели некорректные названия: \n");
                for (String i : wrongNames) {
                    text += i + "\n";
                }
                text += "Пожалуйста введите корректные названия из списка. Получить список вы можете прописав /currency_chart";

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
        } else if(!flagConfrimMessege){
            String text = "";
            text = "Введенная дата некорректна. Допустимый дипазон от 1999г. до наступившего года!";
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
}
