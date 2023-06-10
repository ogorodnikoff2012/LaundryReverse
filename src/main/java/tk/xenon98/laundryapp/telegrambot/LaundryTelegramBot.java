package tk.xenon98.laundryapp.telegrambot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class LaundryTelegramBot extends TelegramLongPollingBot implements ITelegramBotUpdateListener {

	private static final Logger LOG = LoggerFactory.getLogger(LaundryTelegramBot.class);

	private final String botUsername;
	private final Set<Long> trustedUserIds = new HashSet<>();

	private final Map<String, Consumer<Message>> commands = new HashMap<>();
	private final Map<String, String> commandDescriptions = new HashMap<>();

	public LaundryTelegramBot(final String botUsername, final String botToken) {
		super(botToken);
		this.botUsername = botUsername;
		registerCommand("/help", "Print help", this::replyHelp);
	}

	public void addTrustedUser(final long userId) {
		this.trustedUserIds.add(userId);
	}

	public void registerCommand(final String command, final String description, final Consumer<Message> action) {
		this.commands.put(command, action);
		this.commandDescriptions.put(command, description);
	}

	@Override
	public void onUpdateReceived(final Update update) {
		handleUpdate(update);
	}

	@Override
	public String getBotUsername() {
		return botUsername;
	}

	@Override
	public void onMessage(final int updateId, final Message message) {
		try {
			ensureTrustedSource(message);
		} catch (UntrustedSourceException e) {
			final var answer = UntrustedSourceException.HUMAN_STRING + "\n" + e.getMessage();
			reply(message, answer);
			return;
		}

		if (!message.hasText()) {
			reply(message, "I currently accept only text messages :(");
			return;
		}

		final var commandEntities = message.getEntities().stream().filter(entity -> entity.getType().equals("bot_command")).collect(Collectors.toList());
		if (commandEntities.size() != 1) {
			replyHelp(message);
		}
		final String command = commandEntities.get(0).getText();
		if (!commands.containsKey(command)) {
			replyHelp(message);
		}
		commands.get(command).accept(message);
	}

	private void replyHelp(final Message message) {
		final var sb = new StringBuilder();
		sb.append("Available commands:");
		commandDescriptions.forEach((key, value) -> sb.append("\n").append(key).append(" - ").append(value));
		reply(message, sb.toString());
	}

	private void ensureTrustedSource(final Message message) throws UntrustedSourceException {
		if (!message.getChat().isUserChat()) {
			throw new UntrustedSourceException("Accepts only private messages");
		}
		final var user = message.getFrom();
		if (user == null) {
			throw new UntrustedSourceException("WTF?! user == null");
		}
		final long userId = user.getId();
		if (!trustedUserIds.contains(userId)) {
			final var sb = new StringBuilder();
			sb.append("User ").append(user.getFirstName());
			if (user.getLastName() != null) {
				sb.append(' ').append(user.getLastName());
			}
			if (user.getUserName() != null) {
				sb.append(" (@").append(user.getUserName()).append(")");
			}
			sb.append(" is not listed in the Trusted Users list. (userId=").append(userId).append(')');
			throw new UntrustedSourceException(sb.toString());
		}
	}

	public Message reply(final Message requestMessage, final String answer) {
		LOG.info("Sending reply: " + answer);
		final var sendMessage = new SendMessage();
		sendMessage.setReplyToMessageId(requestMessage.getMessageId());
		sendMessage.setChatId(requestMessage.getChatId());
		sendMessage.setText(answer);
		try {
			return execute(sendMessage);
		} catch (TelegramApiException e) {
			LOG.error("Error sending reply", e);
			return null;
		}
	}


	private static class UntrustedSourceException extends Exception {

		public static final String HUMAN_STRING =
				"Sorry, I cannot accept messages from untrusted sources. Details:";

		public UntrustedSourceException(final String message) {
			super(message);
		}
	}
}
