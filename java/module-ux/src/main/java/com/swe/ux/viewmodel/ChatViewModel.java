package com.swe.ux.viewmodel;

import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.RPC;
import com.swe.ux.model.ChatMessage;
import com.swe.chat.ChatMessageSerializer;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The ViewModel for the Chat module.
 * Handles all frontend state, formatting, and communication with the Backend via RPC.
 */
public class ChatViewModel {

    // --- Dependencies & State ---
    private static ChatViewModel INSTANCE;
    private final AbstractRPC rpc;
    // TODO: In a real app, get this from your AuthService
    private final String currentUserId = "user-" + UUID.randomUUID().toString().substring(0, 8);
    private final String currentDisplayName = "A";

    private final Map<String, ChatMessage> messageHistory = new ConcurrentHashMap<>();
    private String currentReplyId = null;

    // --- View Callbacks ---
    private Consumer<MessageVM> onMessageAdded;
    private Consumer<String> onReplyStateChange;
    private Runnable onClearInput;

    /**
     * Presentation Model for the View.
     * Contains only pre-formatted, ready-to-display data.
     */
    public static class MessageVM {
        public final String messageId;
        public final String username;
        public final String content;
        public final String timestamp;
        public final boolean isSentByMe;
        public final String quotedContent;

        public MessageVM(String messageId, String username, String content, String timestamp, boolean isSentByMe, String quotedContent) {
            this.messageId = messageId;
            this.username = username;
            this.content = content;
            this.timestamp = timestamp;
            this.isSentByMe = isSentByMe;
            this.quotedContent = quotedContent;
        }

        public boolean hasQuote() {
            return quotedContent != null;
        }
    }


    // --- Constructor (Dependency Injection) ---
    public ChatViewModel(AbstractRPC RPC) {
        this.rpc = RPC;
        // Subscribe to incoming messages broadcast from the Core
        this.rpc.subscribe("chat:new-message", this::handleBackendMessage);
    }

    // --- RPC Handler ---
    private byte[] handleBackendMessage(byte[] data) {
        // Deserialize the raw bytes back into our rich model
        ChatMessage message = ChatMessageSerializer.deserialize(data);
        // Update the UI
        handleIncomingMessage(message);
        return new byte[0];
    }

    // --- User Actions ---

    public void sendMessage(String messageText) {
        if (messageText == null || messageText.trim().isEmpty()) {
            return;
        }

        // 1. Create the new message model
        final String messageId = UUID.randomUUID().toString();
        final ChatMessage messageToSend = new ChatMessage(
                messageId,
                this.currentUserId,
                this.currentDisplayName,
                messageText,
                this.currentReplyId
        );

        // 2. Serialize the data
        byte[] data = ChatMessageSerializer.serialize(messageToSend);

        // --- FIX START: Run the network call in a background thread ---
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                // This now runs in the background and won't freeze the UI
                System.out.println("[FRONT] Attempting to send RPC...");
                this.rpc.call("chat:send-text", data)
                        .thenRun(() -> System.out.println("[FRONT] RPC call successful! Core received it."))
                        .exceptionally(e -> {
                            System.err.println("[FRONT] RPC call FAILED: " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        });
//                this.rpc.call("chat:send-message", data);
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: In the future, you could update the UI here to show a "failed to send" error
            }
        });
        // --- FIX END ---

        // 3. Optimistic Update: Show it locally immediately
        // (This is safe because handleIncomingMessage calls the View's listener,
        // and your View correctly uses SwingUtilities.invokeLater to update itself)
        handleIncomingMessage(messageToSend);

        // 4. Clear input state
        if (onClearInput != null) onClearInput.run();
        cancelReply();
    }

    public void startReply(MessageVM messageToReply) {
        this.currentReplyId = messageToReply.messageId;
        if (onReplyStateChange != null) {
            String quote = "Replying to " + messageToReply.username + ": " +
                    messageToReply.content.substring(0, Math.min(messageToReply.content.length(), 20)) + "...";
            onReplyStateChange.accept(quote);
        }
    }

    public void cancelReply() {
        this.currentReplyId = null;
        if (onReplyStateChange != null) {
            onReplyStateChange.accept(null);
        }
    }

    // --- Core Logic ---

    private void handleIncomingMessage(final ChatMessage message) {
        messageHistory.put(message.getMessageId(), message);

        // 1. Determine sender
        final boolean isSentByMe = message.getUserId().equals(this.currentUserId);
        final String username = isSentByMe ? "You" : message.getSenderDisplayName();

        // 2. Format timestamp
        final String formattedTime = message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm"));

        // 3. Format reply quote (if it exists)
        String quotedContent = null;
        if (message.getReplyToMessageId() != null) {
            final ChatMessage repliedTo = messageHistory.get(message.getReplyToMessageId());
            if (repliedTo != null) {
                String sender = repliedTo.getUserId().equals(this.currentUserId) ? "You" : repliedTo.getSenderDisplayName();
                quotedContent = "Replying to " + sender + ": " +
                        repliedTo.getContent().substring(0, Math.min(repliedTo.getContent().length(), 20)) + "...";
            } else {
                quotedContent = "Reply to unavailable message";
            }
        }

        // 4. Create Presentation Model and notify View
        MessageVM vm = new MessageVM(message.getMessageId(), username, message.getContent(), formattedTime, isSentByMe, quotedContent);
        if (onMessageAdded != null) {
            onMessageAdded.accept(vm);
        }
    }

    // --- View Bindings ---
    public void setOnMessageAdded(Consumer<MessageVM> listener) { this.onMessageAdded = listener; }
    public void setOnReplyStateChange(Consumer<String> listener) { this.onReplyStateChange = listener; }
    public void setOnClearInput(Runnable listener) { this.onClearInput = listener; }
}