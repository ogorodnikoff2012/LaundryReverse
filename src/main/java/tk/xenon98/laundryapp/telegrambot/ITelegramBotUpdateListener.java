package tk.xenon98.laundryapp.telegrambot;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.ChosenInlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.ShippingQuery;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;

public interface ITelegramBotUpdateListener {

	default void onMessage(final int updateId, final Message message) {
	}

	default void onInlineQuery(final int updateId, final InlineQuery inlineQuery) {
	}

	default void onChosenInlineQuery(final int updateId, final ChosenInlineQuery chosenInlineQuery) {
	}

	default void onCallbackQuery(final int updateId, final CallbackQuery callbackQuery) {
	}

	default void onEditedMessage(final int updateId, final Message message) {
	}

	default void onChannelPost(final int updateId, final Message message) {
	}

	default void onEditedChannelPost(final int updateId, final Message message) {
	}

	default void onShippingQuery(final int updateId, final ShippingQuery shippingQuery) {
	}

	default void onPreCheckoutQuery(final int updateId, final PreCheckoutQuery preCheckoutQuery) {
	}

	default void onPoll(final int updateId, final Poll poll) {
	}

	default void onPollAnswer(final int updateId, final PollAnswer pollAnswer) {
	}

	default void onMyChatMember(final int updateId, final ChatMemberUpdated chatMemberUpdated) {
	}

	default void onChatMember(final int updateId, final ChatMemberUpdated chatMemberUpdated) {
	}

	default void onChatJoinRequest(final int updateId, final ChatJoinRequest chatJoinRequest) {
	}

	default void handleUpdate(final Update update) {
		final int updateId = update.getUpdateId();
		if (update.hasMessage()) {
			onMessage(updateId, update.getMessage());
		} else if (update.hasInlineQuery()) {
			onInlineQuery(updateId, update.getInlineQuery());
		} else if (update.hasChosenInlineQuery()) {
			onChosenInlineQuery(updateId, update.getChosenInlineQuery());
		} else if (update.hasCallbackQuery()) {
			onCallbackQuery(updateId, update.getCallbackQuery());
		} else if (update.hasEditedMessage()) {
			onEditedMessage(updateId, update.getEditedMessage());
		} else if (update.hasChannelPost()) {
			onChannelPost(updateId, update.getChannelPost());
		} else if (update.hasEditedChannelPost()) {
			onEditedChannelPost(updateId, update.getEditedChannelPost());
		} else if (update.hasShippingQuery()) {
			onShippingQuery(updateId, update.getShippingQuery());
		} else if (update.hasPreCheckoutQuery()) {
			onPreCheckoutQuery(updateId, update.getPreCheckoutQuery());
		} else if (update.hasPoll()) {
			onPoll(updateId, update.getPoll());
		} else if (update.hasPollAnswer()) {
			onPollAnswer(updateId, update.getPollAnswer());
		} else if (update.hasMyChatMember()) {
			onMyChatMember(updateId, update.getMyChatMember());
		} else if (update.hasChatMember()) {
			onChatMember(updateId, update.getChatMember());
		} else if (update.hasChatJoinRequest()) {
			onChatJoinRequest(updateId, update.getChatJoinRequest());
		}
	}
}
