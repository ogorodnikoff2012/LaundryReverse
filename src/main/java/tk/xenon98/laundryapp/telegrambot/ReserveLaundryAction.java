package tk.xenon98.laundryapp.telegrambot;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tk.xenon98.laundryapp.driver.laundry.LaundryAppDriver;

public class ReserveLaundryAction {

	private static final Logger LOG = LoggerFactory.getLogger(ReserveLaundryAction.class);

	private static final String LAST_REQUEST_NOT_FINISHED =
			"Cannot launch new request until previous request is not finished.";
	private static final String REQUEST_PLACED = "Trying to reserve washer...";
	private final LaundryTelegramBot bot;
	private final LaundryAppDriver laundryAppDriver;
	private CompletableFuture<Void> pendingRequest = null;
	private Message statusMessage = null;

	public ReserveLaundryAction(final LaundryTelegramBot bot, final LaundryAppDriver laundryAppDriver) {
		this.bot = bot;
		this.laundryAppDriver = laundryAppDriver;

		bot.registerCommand("/reserve_washer", "Reserve a washer", this::request);
	}

	public synchronized void request(final Message requestMessage) {
		if (pendingRequest != null && !pendingRequest.isDone()) {
			bot.reply(requestMessage, LAST_REQUEST_NOT_FINISHED);
			return;
		}

		pendingRequest = CompletableFuture.supplyAsync(() -> {
			try {
				return laundryAppDriver.reserveWasher(this::updateStatus);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new CompletionException(e);
			} catch (Exception e) {
				throw new CompletionException(e);
			}
		}).thenAcceptAsync(completionStatus -> {
			bot.reply(requestMessage, completionStatus);
			reset();
		});
		statusMessage = bot.reply(requestMessage, REQUEST_PLACED);
	}

	private synchronized void reset() {
		this.statusMessage = null;
		this.pendingRequest = null;
	}

	private synchronized void updateStatus(final String newStatus) {
		LOG.info("Updating status: " + newStatus);
		assert statusMessage != null;
		final var editMessageText = new EditMessageText();
		editMessageText.setChatId(statusMessage.getChatId());
		editMessageText.setMessageId(statusMessage.getMessageId());
		editMessageText.setText(newStatus);
		try {
			bot.execute(editMessageText);
		} catch (TelegramApiException e) {
			LOG.error("Update status: ", e);
		}
	}

}
