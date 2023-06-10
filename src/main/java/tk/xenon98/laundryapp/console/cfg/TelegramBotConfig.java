package tk.xenon98.laundryapp.console.cfg;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import tk.xenon98.laundryapp.driver.laundry.LaundryAppDriver;
import tk.xenon98.laundryapp.telegrambot.LaundryTelegramBot;
import tk.xenon98.laundryapp.telegrambot.ReserveLaundryAction;

@Configuration
public class TelegramBotConfig {

	@Value("${tk.xenon98.laundryapp.telegrambot.username}")
	private String username;

	@Value("${tk.xenon98.laundryapp.telegrambot.token}")
	private String token;

	@Value("#{'${tk.xenon98.laundryapp.telegrambot.trustedUsers}'.split(',')}")
	private List<Long> trustedUsers;

	@Bean
	protected TelegramBotsApi telegramBotsApi() throws TelegramApiException {
		return new TelegramBotsApi(DefaultBotSession.class);
	}

	@Bean
	public LaundryTelegramBot laundryTelegramBot(@Autowired TelegramBotsApi telegramBotsApi)
			throws TelegramApiException {
		final var bot = new LaundryTelegramBot(username, token);
		for (long userId : trustedUsers) {
			bot.addTrustedUser(userId);
		}
		telegramBotsApi.registerBot(bot);
		return bot;
	}

	@Bean
	public ReserveLaundryAction reserveLaundryAction(@Autowired LaundryTelegramBot laundryTelegramBot, @Autowired
	LaundryAppDriver laundryAppDriver) {
		return new ReserveLaundryAction(laundryTelegramBot, laundryAppDriver);
	}
}
