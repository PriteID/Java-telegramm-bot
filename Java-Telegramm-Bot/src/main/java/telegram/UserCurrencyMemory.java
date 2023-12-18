package telegram;

import java.util.HashMap;
import java.util.Map;

public class UserCurrencyMemory {
    private HashMap<Integer, String> mostUsedCurrency;
    private HashMap<String, Integer> currencyUsageCount;

    public UserCurrencyMemory() {
        mostUsedCurrency = new HashMap<>();
        currencyUsageCount = new HashMap<>();
    }

    public void rememberCurrency(int userId, String currency) {
        currencyUsageCount.put(currency, currencyUsageCount.getOrDefault(currency, 0) + 1);
        String mostUsed = "";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : currencyUsageCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                mostUsed = entry.getKey();
                maxCount = entry.getValue();
            }
        }
        mostUsedCurrency.put(userId, mostUsed);
    }

    public String getMostUsedCurrency(int userId) {
        return mostUsedCurrency.get(userId);
    }
}
