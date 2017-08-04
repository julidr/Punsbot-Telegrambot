package co.accionese.punsbot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

public class Main {

    public static void main(String[] args) {
        System.out.println("Telegram PunsBotHandler Test");
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        PunsBot punsBot = new PunsBot();
        try {
            telegramBotsApi.registerBot(punsBot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }

    }
}
