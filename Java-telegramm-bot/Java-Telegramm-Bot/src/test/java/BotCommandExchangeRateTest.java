import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.BotCommandExchangeRate;

import static org.mockito.Mockito.*;

public class BotCommandExchangeRateTest {
    @Test
    public void testGetExchangeRate_WithValidCurrency_ShouldReturnCorrectRate() {

        BotCommandExchangeRate botCommand = new BotCommandExchangeRate();
        String baseCurrency = "USD";
        String targetCurrency = "EUR";
        String dataDay = "01";
        String dataMonth = "01";
        String dataYear = "2023";

        double exchangeRate = botCommand.getExchangeRate(baseCurrency, targetCurrency, dataDay, dataMonth, dataYear);
        boolean result = exchangeRate != 0;

        assertTrue(result);
    }

    @Test
    public void testGetExchangeRate_WithValidCurrency_ShouldReturnNotCorrectRateFirst() {

        BotCommandExchangeRate botCommand = new BotCommandExchangeRate();
        String baseCurrency = "USO1";
        String targetCurrency = "EUR";
        String dataDay = "01";
        String dataMonth = "01";
        String dataYear = "2023";

        double exchangeRate = botCommand.getExchangeRate(baseCurrency, targetCurrency, dataDay, dataMonth, dataYear);
        boolean result = exchangeRate != 0;

        assertFalse(result);
    }

    @Test
    public void testGetExchangeRate_WithValidCurrency_ShouldReturnNotCorrectRateSecond() {

        BotCommandExchangeRate botCommand = new BotCommandExchangeRate();
        String baseCurrency = "USD";
        String targetCurrency = "EUROo";
        String dataDay = "01";
        String dataMonth = "01";
        String dataYear = "2023";

        double exchangeRate = botCommand.getExchangeRate(baseCurrency, targetCurrency, dataDay, dataMonth, dataYear);
        boolean result = exchangeRate != 0;

        assertFalse(result);
    }

    @Test
    public void testGetExchangeRate_WithValidCurrency_ShouldReturnNotCorrectRateDate() {

        BotCommandExchangeRate botCommand = new BotCommandExchangeRate();
        String baseCurrency = "USD";
        String targetCurrency = "EUR";
        String dataDay = "41";
        String dataMonth = "30";
        String dataYear = "2025";

        double exchangeRate = botCommand.getExchangeRate(baseCurrency, targetCurrency, dataDay, dataMonth, dataYear);
        boolean result = exchangeRate != 0;

        assertFalse(result);
    }

    @Test
    public void testValidateDate_ValidDate_ReturnsFalse() {
        String dataDay = "30";
        String dataMonth = "11";
        String dataYear = "2023";
        String todayDataDay = "01";
        String todayDataMonth = "12";
        String todayDataYear = "2023";

        boolean result = BotCommandExchangeRate.validateDate(dataDay, dataMonth, dataYear, todayDataDay, todayDataMonth, todayDataYear);

        assertFalse(result);
    }

    @Test
    public void testValidateDate_ValidDate_ReturnsTrue() {
        String dataDay = "31";
        String dataMonth = "02";
        String dataYear = "2022";
        String todayDataDay = "01";
        String todayDataMonth = "12";
        String todayDataYear = "2023";

        boolean result = BotCommandExchangeRate.validateDate(dataDay, dataMonth, dataYear, todayDataDay, todayDataMonth, todayDataYear);

        assertTrue(result);
    }
}