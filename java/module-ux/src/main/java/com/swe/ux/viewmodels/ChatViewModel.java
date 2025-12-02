package com.swe.ux.viewmodels;

import com.swe.chat.ChatMessageSerializer;
import com.swe.chat.FileMessageSerializer;
import com.swe.chat.MessageVM;
import com.swe.controller.Meeting.UserProfile;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.ux.model.ChatMessage;
import com.swe.ux.model.FileMessage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The ViewModel for the Chat module.
 * Handles all frontend state, formatting, and communication with the Backend via RPC.
 */
public class ChatViewModel {

    // --- Dependencies & State ---
    /** Singleton instance. */
    private static ChatViewModel instance;
    /** RPC instance. */
    private final AbstractRPC rpc;
    /** User email. */
    private final String userEmail;
    /** User display name. */
    private final String userDisplayName;

//    private static final long MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024; // 50 MB

    /** Message history map. */
    private final Map<String, MessageVM> messageHistory = new ConcurrentHashMap<>();
    /** Current reply ID. */
    private String currentReplyId = null;
    /** Attached file. */
    private File attachedFile = null;

    // --- View Callbacks ---
    /** Message added callback. */
    private Consumer<MessageVM> onMessageAdded;
    /** Reply state change callback. */
    private Consumer<String> onReplyStateChange;
    /** Clear input callback. */
    private Runnable onClearInput;
    /** Message removed callback. */
    private Consumer<String> onMessageRemoved;
    /** Attachment set callback. */
    private Consumer<String> onAttachmentSet;
    /** Show error dialog callback. */
    private Consumer<String> onShowErrorDialog;
    /** Show success dialog callback. */
    private Consumer<String> onShowSuccessDialog;

    // --- Constructor (Dependency Injection) ---
    /**
     * Creates a new ChatViewModel.
     * @param rpcParam The RPC instance
     * @param userProfile The user profile
     */
    public ChatViewModel(final AbstractRPC rpcParam, final UserProfile userProfile) {
        this.rpc = rpcParam;
        this.userEmail = userProfile.getEmail();
        this.userDisplayName = userProfile.getDisplayName();
        // Subscribe to incoming messages broadcast from the Core
        this.rpc.subscribe("chat:new-message", this::handleBackendTextMessage);
        this.rpc.subscribe("chat:file-metadata-received", this::handleBackendFileMetadata);
        this.rpc.subscribe("chat:file-saved-success", this::handleFileSaveSuccess);
        this.rpc.subscribe("chat:file-saved-error", this::handleFileSaveError);
        this.rpc.subscribe("chat:message-deleted", this::handleBackendDelete);
    }


    // Add this helper method inside ChatViewModel
    /**
     * Formats UTC date time to local time.
     * @param utcDateTime The UTC date time
     * @return Formatted local time string
     */
    private String formatToLocalTime(final LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return "";
        }
        return utcDateTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * DEBUG: Prints the current state of message history to the console.
     */
    public void printMessageHistory() {
        System.out.println("\n====== DEBUG: MESSAGE HISTORY DUMP (" + messageHistory.size() + " items) ======");

        if (messageHistory.isEmpty()) {
            System.out.println("(History is empty)");
        } else {
            // Iterate over every message in the map
            messageHistory.values().forEach(vm -> {
                System.out.println("------------------------------------------------");
                System.out.println("ID        : " + vm.getMessageId());
                System.out.println("Time      : " + vm.getTimestamp());
                final String senderLabel;
                if (vm.isSentByMe()) {
                    senderLabel = " (Me)";
                } else {
                    senderLabel = "";
                }
                System.out.println("Sender    : " + vm.getUsername() + senderLabel);

                if (vm.isFileMessage()) {
                    System.out.println("Type      : FILE");
                    System.out.println("File Name : " + vm.getFileName());
                    System.out.println("Caption   : " + vm.getContent());
                } else {
                    System.out.println("Type      : TEXT");
                    System.out.println("Content   : " + vm.getContent());
                }

                System.out.println("ReplyToID : " + vm.getReplyToId());
            });
        }
        System.out.println("============================================================\n");
    }

    /**
     * HANDLER 1: Text Message from Backend.
     * @param data The message data
     * @return Empty byte array
     */
    private byte[] handleBackendTextMessage(final byte[] data) {
        final ChatMessage message = ChatMessageSerializer.deserialize(data);
        // Convert UTC LocalDateTime to User's System Default TimeZone
        final String localTime = formatToLocalTime(message.getTimestamp());

        handleIncomingMessage(
                message.getMessageId(),
                message.getUserId(),
                message.getSenderDisplayName(),
                localTime,
                message.getReplyToMessageId(),
                message.getContent(),  // Text content
                null,                  // No file
                0,                     // No file size
                null                   // No file bytes
        );
        return new byte[0];
    }

    /**
     * HANDLER 2: File Metadata from Backend (KEY!).
     *
     * <p>Backend sends ONLY metadata - NO file data</p>
     */
    /**
     * Handles file metadata from backend.
     * @param data The file metadata data
     * @return Empty byte array
     */
    private byte[] handleBackendFileMetadata(final byte[] data) {
        System.out.println("[FRONT] Received file metadata (no data attached)");

        try {
            final FileMessage message = FileMessageSerializer.deserialize(data);

            if (message == null || message.getFileName() == null) {
                System.err.println("[FRONT] Invalid file metadata");
                return new byte[0];
            }
            System.out.println("[FRONT][Chat] Metadata for file id=" + message.getMessageId()
                    + " name=" + message.getFileName()
                    + " caption=" + message.getCaption());

            // Extract compressed size for UI display
            final long compressedSize;
            if (message.getFileContent() != null) {
                compressedSize = message.getFileContent().length;
            } else {
                compressedSize = 0;
            }
            System.out.println("[FRONT][Chat]   reported compressed bytes=" + compressedSize
                    + " pathMode=" + (message.getFilePath() != null));

            // Convert UTC to Local Time
            final String localTime = formatToLocalTime(message.getTimestamp());

            handleIncomingMessage(
                    message.getMessageId(),
                    message.getUserId(),
                    message.getSenderDisplayName(),
                    localTime,
                    message.getReplyToMessageId(),
                    message.getCaption(),    // Caption
                    message.getFileName(),   // File name
                    compressedSize,          // Size
                    null                     // NO file content!
            );
        } catch (Exception e) {
            System.err.println("[FRONT] Failed to deserialize file metadata: " + e.getMessage());
            e.printStackTrace();
        }

        return new byte[0];
    }

    /**
     * HANDLER 3: File Save Success.
     * @param data The success message data
     * @return Empty byte array
     */
    private byte[] handleFileSaveSuccess(final byte[] data) {
        final String message = new String(data, StandardCharsets.UTF_8);
        if (onShowSuccessDialog != null) {
            onShowSuccessDialog.accept("File saved successfully!\n" + message);
        }
        return new byte[0];
    }

    /**
     * HANDLER 4: File Save Error.
     * @param data The error message data
     * @return Empty byte array
     */
    private byte[] handleFileSaveError(final byte[] data) {
        final String message = new String(data, StandardCharsets.UTF_8);
        if (onShowErrorDialog != null) {
            onShowErrorDialog.accept("Failed to save file: " + message);
        }
        return new byte[0];
    }

    /**
     * Handles backend delete message.
     * @param data The message ID data
     * @return Empty byte array
     */
    private byte[] handleBackendDelete(final byte[] data) {
        try {
            // 1. Decode the ID
            final String rawId = new String(data, StandardCharsets.UTF_8);

            // 2. SANITIZE: Important! Trim whitespace/newlines that might break the match
            final String messageId = rawId.trim();

            System.out.println("[FRONT] Received DELETE signal for ID: '" + messageId + "'");

            // 3. Look up the message
            final MessageVM oldMsg = messageHistory.get(messageId);

            if (oldMsg != null) {
                // 4. Create the "Deleted" replacement
                final MessageVM deletedMsg = new MessageVM(
                        oldMsg.getMessageId(),
                        oldMsg.getUsername(),
                        "<i>This message was deleted</i>", // The specific text your View looks for
                        null,           // Clear file attachment
                        oldMsg.getTimestamp(),
                        new MessageVM.ReplyContext(oldMsg.isSentByMe(), null, null)
                );

                // 5. Update History
                messageHistory.put(messageId, deletedMsg);

                // 6. Trigger the View Update
                if (onMessageAdded != null) {
                    // This forces the View to swap the bubble
                    onMessageAdded.accept(deletedMsg);
                }
                System.out.println("[FRONT] Message deleted successfully in View.");
            } else {
                System.err.println("[FRONT] DELETE FAILED: Message ID '" + messageId + "' not found in history.");
                // Optional: Print keys to see what IS in history
                // System.out.println("Available keys: " + messageHistory.keySet());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * Sends a message.
     * @param messageText The message text
     */
    public void send(final String messageText) {
        if (this.attachedFile != null) {
            sendFileMessage(this.attachedFile, messageText);
        } else if (messageText != null && !messageText.trim().isEmpty()) {
            sendTextMessage(messageText);
        }

        if (onClearInput != null) {
            onClearInput.run();
        }
        cancelReply();
        cancelAttachment();
    }

    /**
     * Sends a text message.
     * @param messageTextParam The message text
     */
    private void sendTextMessage(final String messageTextParam) {
        final String messageId = UUID.randomUUID().toString();
        final ChatMessage messageToSend = new ChatMessage(
                messageId,
                this.userEmail,
                this.userDisplayName,
                messageTextParam,
                this.currentReplyId
        );

        final byte[] messageBytes = ChatMessageSerializer.serialize(messageToSend);
        sendRpc("chat:send-text", messageBytes);

        final String localTimeFormatted = formatToLocalTime(messageToSend.getTimestamp());

        // Optimistic update
        handleIncomingMessage(
                messageToSend.getMessageId(),
                messageToSend.getUserId(),
                messageToSend.getSenderDisplayName(),
                localTimeFormatted,
                messageToSend.getReplyToMessageId(),
                messageToSend.getContent(),
                null, 0, null
        );
        printMessageHistory();
    }

    /**
     * SEND FILE - Sends ONLY the path to backend.
     * @param file The file to send
     * @param caption The file caption
     */
    private void sendFileMessage(final File file, final String caption) {
        String cleanPath = file.getAbsolutePath().trim();
        if (cleanPath.startsWith("*")) {
            cleanPath = cleanPath.substring(1).trim();
        }

        if (cleanPath == null || cleanPath.isEmpty()) {
            if (onShowErrorDialog != null) {
                onShowErrorDialog.accept("File path is empty or invalid!");
            }
            return;
        }

        final String messageId = UUID.randomUUID().toString();
        System.out.println("[FRONT][Chat] Sending file message id=" + messageId
                + " name=" + file.getName()
                + " size=" + file.length()
                + " caption=" + caption);

        // Create PATH-MODE FileMessage
        final FileMessage messageToSend = new FileMessage(
                messageId,
                this.userEmail,
                this.userDisplayName,
                caption,
                file.getName(),
                cleanPath,  //  PATH, not bytes
                this.currentReplyId
        );

        final byte[] messageBytes = FileMessageSerializer.serialize(messageToSend);
        System.out.println("[FRONT][Chat] Serialized file metadata payload bytes=" + messageBytes.length);
        sendRpc("chat:send-file", messageBytes);

        // Optimistic UI update
        handleIncomingMessage(
                messageToSend.getMessageId(),
                messageToSend.getUserId(),
                messageToSend.getSenderDisplayName(),
                messageToSend.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")),
                messageToSend.getReplyToMessageId(),
                caption,
                file.getName(),
                file.length(),  // Show original size
                null            // NO file content
        );
        System.out.println("[FRONT][Chat] Optimistic UI update added file bubble for id=" + messageId);
    }


    /**
     * Downloads a file message.
     * @param fileMessage The file message to download
     */
    public void downloadFile(final MessageVM fileMessage) {
        if (fileMessage == null || !fileMessage.isFileMessage()) {
            if (onShowErrorDialog != null) {
                onShowErrorDialog.accept("Invalid file message");
            }
            return;
        }

        System.out.println("[FRONT] User clicked 'Save'. Requesting backend to decompress and save.");

        final byte[] messageIdBytes = fileMessage.getMessageId().getBytes(StandardCharsets.UTF_8);

        CompletableFuture.runAsync(() -> {
            try {
                this.rpc.call("chat:save-file-to-disk", messageIdBytes)
                        .thenAccept(response -> {
                            System.out.println("[FRONT] Backend finished saving file");
                        })
                        .exceptionally(e -> {
                            System.err.println("[FRONT] Failed to request save: " + e.getMessage());
                            if (onShowErrorDialog != null) {
                                onShowErrorDialog.accept("Save failed: " + e.getMessage());
                            }
                            return null;
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Handles incoming message.
     * @param messageIdParam The message ID
     * @param userEmailParam The user email
     * @param senderDisplayNameParam The sender display name
     * @param formattedTimeParam The formatted time
     * @param replyToIdParam The reply to ID
     * @param contentParam The content
     * @param fileNameParam The file name
     * @param compressedFileSizeParam The compressed file size
     * @param fileContentParam The file content
     */
    private void handleIncomingMessage(
            final String messageIdParam, final String userEmailParam, final String senderDisplayNameParam,
            final String formattedTimeParam, final String replyToIdParam,
            final String contentParam,              // Text or Caption
            final String fileNameParam,             // File name
            final long compressedFileSizeParam,     // Compressed size (metadata)
            final byte[] fileContentParam           // NULL except when needed
    ) {
        if (messageHistory.containsKey(messageIdParam)) {
            System.out.println("[FRONT] Duplicate message ignored: " + messageIdParam);
            return;
        }

        final boolean isSentByMe = userEmailParam.equals(this.userEmail);
        final String username;
        if (isSentByMe) {
            username = "You";
        } else {
            username = senderDisplayNameParam;
        }

        String quotedContent = null;
        if (replyToIdParam != null) {
            final MessageVM repliedTo = messageHistory.get(replyToIdParam);
            if (repliedTo != null) {
                final String sender;
                if (repliedTo.isSentByMe()) {
                    sender = "You";
                } else {
                    sender = repliedTo.getUsername();
                }
                final String contentSnippet;
                if (repliedTo.isFileMessage()) {
                    contentSnippet = "File: " + repliedTo.getFileName();
                } else {
                    final int maxLength = 20;
                    final int contentLength = repliedTo.getContent().length();
                    final int endIndex = Math.min(contentLength, maxLength);
                    contentSnippet = repliedTo.getContent().substring(0, endIndex) + "...";
                }
                quotedContent = "Replying to " + sender + ": " + contentSnippet;
            } else {
                quotedContent = "Reply to unavailable message";
            }
        }

        // Create file attachment if needed
        MessageVM.FileAttachment attachment = null;
        if (fileNameParam != null) {
            attachment = new MessageVM.FileAttachment(fileNameParam, compressedFileSizeParam, fileContentParam);
        }

        final MessageVM vm = new MessageVM(
                messageIdParam,
                username,
                contentParam,
                attachment,
                formattedTimeParam,
                new MessageVM.ReplyContext(isSentByMe, quotedContent, replyToIdParam)
        );

        messageHistory.put(vm.getMessageId(), vm);
        System.out.println("[FRONT][Chat] Stored message id=" + vm.getMessageId()
                + " type=" + (vm.isFileMessage() ? "FILE" : "TEXT")
                + " sender=" + vm.getUsername());
        if (onMessageAdded != null) {
            onMessageAdded.accept(vm);
        }
        printMessageHistory();
    }

    /**
     * Handles user file selection for attachment.
     * @param selectedFile The selected file
     */
    public void userSelectedFileToAttach(final File selectedFile) {
        if (selectedFile == null) {
            return;
        }

        if (selectedFile.length() == 0) {
            if (onShowErrorDialog != null) {
                onShowErrorDialog.accept("Cannot send an empty file.");
            }
            return;
        }

//        if (selectedFile.length() > MAX_FILE_SIZE_BYTES) {
//            if (onShowErrorDialog != null) {
//                onShowErrorDialog.accept("File is too large (Max 50MB).");
//            }
//            return;
//        }

        this.attachedFile = selectedFile;
        if (onAttachmentSet != null) {
            onAttachmentSet.accept("Attached: " + selectedFile.getName());
        }
        System.out.println("[FRONT][Chat] Queued attachment: " + selectedFile.getAbsolutePath()
                + " (" + selectedFile.length() + " bytes)");
    }

    /**
     * Deletes a message.
     * @param messageToDelete The message to delete
     */
    public void deleteMessage(final MessageVM messageToDelete) {
        if (messageToDelete == null || !messageToDelete.isSentByMe()) {
            return;
        }

        // 1. Send RPC to backend (keep this)
        final byte[] messageIdBytes = messageToDelete.getMessageId().getBytes(StandardCharsets.UTF_8);
        sendRpc("chat:delete-message", messageIdBytes);

        // 2. CREATE THE "DELETED" VERSION OF THE MESSAGE
        // We convert the file or text message into a simple text message with italics
        final MessageVM deletedMsg = new MessageVM(
                messageToDelete.getMessageId(),
                messageToDelete.getUsername(),
                "<i>This message was deleted</i>", // HTML italics for styling
                null,           // No file attachment
                messageToDelete.getTimestamp(),
                new MessageVM.ReplyContext(messageToDelete.isSentByMe(), null, null)
        );

        // 3. UPDATE THE HISTORY (Do not remove!)
        messageHistory.put(messageToDelete.getMessageId(), deletedMsg);

        // 4. NOTIFY VIEW TO UPDATE (Use onMessageAdded, NOT onMessageRemoved)
        // Your View logic already handles updating if the ID exists
        if (onMessageAdded != null) {
            onMessageAdded.accept(deletedMsg);
        }

        // 5. Handle Reply cancellation logic
        if (this.currentReplyId != null && this.currentReplyId.equals(messageToDelete.getMessageId())) {
            cancelReply();
        }
    }





    /**
     * Cancels the attachment.
     */
    public void cancelAttachment() {
        this.attachedFile = null;
        if (onAttachmentSet != null) {
            onAttachmentSet.accept(null);
        }
    }

    /**
     * Sends an RPC call.
     * @param endpointParam The endpoint
     * @param dataParam The data
     */
    private void sendRpc(final String endpointParam, final byte[] dataParam) {
        System.out.println("[FRONT][Chat] RPC -> " + endpointParam
                + " payloadBytes=" + (dataParam != null ? dataParam.length : 0));
        CompletableFuture.runAsync(() -> {
            try {
                this.rpc.call(endpointParam, dataParam)
                        .thenAccept(responseBytes -> {
                            if (responseBytes != null && responseBytes.length > 0) {
                                System.out.println("[FRONT] Received response from backend");
                            }
                        })
                        .exceptionally(e -> {
                            System.err.println("[FRONT] RPC call failed: " + e.getMessage());
                            if (onShowErrorDialog != null) {
                                onShowErrorDialog.accept("RPC call failed: " + e.getMessage());
                            }
                            return null;
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    // --- User Actions ---

    /**
     * Starts a reply to a message.
     * @param messageToReplyParam The message to reply to
     */
    public void startReply(final MessageVM messageToReplyParam) {
        this.currentReplyId = messageToReplyParam.getMessageId();
        if (onReplyStateChange != null) {
            final String quoteText;
            final int maxLength = 20;
            if (messageToReplyParam.isFileMessage()) {
                quoteText = "Replying to file: " + messageToReplyParam.getFileName();
            } else {
                final int contentLength = messageToReplyParam.getContent().length();
                final int endIndex = Math.min(contentLength, maxLength);
                quoteText = "Replying to " + messageToReplyParam.getUsername() + ": "
                        + messageToReplyParam.getContent().substring(0, endIndex) + "...";
            }
            onReplyStateChange.accept(quoteText);
        }
    }

    /**
     * Cancels the reply.
     */
    public void cancelReply() {
        this.currentReplyId = null;
        if (onReplyStateChange != null) {
            onReplyStateChange.accept(null);
        }
    }

    // --- View Bindings ---
    /**
     * Sets the message added listener.
     * @param listener The listener
     */
    public void setOnMessageAdded(final Consumer<MessageVM> listener) {
        this.onMessageAdded = listener;
    }

    /**
     * Sets the reply state change listener.
     * @param listener The listener
     */
    public void setOnReplyStateChange(final Consumer<String> listener) {
        this.onReplyStateChange = listener;
    }

    /**
     * Sets the clear input listener.
     * @param listener The listener
     */
    public void setOnClearInput(final Runnable listener) {
        this.onClearInput = listener;
    }

    /**
     * Sets the message removed listener.
     * @param listener The listener
     */
    public void setOnMessageRemoved(final Consumer<String> listener) {
        this.onMessageRemoved = listener;
    }

    /**
     * Sets the attachment set listener.
     * @param listener The listener
     */
    public void setOnAttachmentSet(final Consumer<String> listener) {
        this.onAttachmentSet = listener;
    }

    /**
     * Sets the show error dialog listener.
     * @param listener The listener
     */
    public void setOnShowErrorDialog(final Consumer<String> listener) {
        this.onShowErrorDialog = listener;
    }

    /**
     * Sets the show success dialog listener.
     * @param listener The listener
     */
    public void setOnShowSuccessDialog(final Consumer<String> listener) {
        this.onShowSuccessDialog = listener;
    }
}
