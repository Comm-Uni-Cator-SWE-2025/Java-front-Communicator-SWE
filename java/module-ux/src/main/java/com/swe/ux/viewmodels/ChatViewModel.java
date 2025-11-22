package com.swe.ux.viewmodels;

import com.swe.chat.FileMessageSerializer;
import com.swe.chat.MessageVM;
import com.swe.app.RPCinterface.AbstractRPC;
import com.swe.ux.model.ChatMessage;
import com.swe.app.RPC;
import com.swe.chat.ChatMessageSerializer;
import com.swe.ux.model.FileMessage;

import java.io.File;
import java.nio.charset.StandardCharsets;
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
    private static ChatViewModel INSTANCE;
    private final AbstractRPC rpc;
    // TODO: In a real app, get this from your AuthService
    private final String currentUserId = "user-" + UUID.randomUUID().toString().substring(0, 8);
    private final String currentDisplayName = "A";

    private static final long MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024; // 50 MB

    private final Map<String, MessageVM> messageHistory = new ConcurrentHashMap<>();
    private String currentReplyId = null;
    private File attachedFile = null;

    // --- View Callbacks ---
    private Consumer<MessageVM> onMessageAdded;
    private Consumer<String> onReplyStateChange;
    private Runnable onClearInput;
    private Consumer<String> onMessageRemoved;
    private Consumer<String> onAttachmentSet;
    private Consumer<String> onShowErrorDialog;
    private Consumer<String> onShowSuccessDialog;

    // --- Constructor (Dependency Injection) ---
    public ChatViewModel(AbstractRPC RPC) {
        this.rpc = RPC;
        // Subscribe to incoming messages broadcast from the Core
        this.rpc.subscribe("chat:new-message", this::handleBackendTextMessage);
        this.rpc.subscribe("chat:file-metadata-received", this::handleBackendFileMetadata);
        this.rpc.subscribe("chat:file-saved-success", this::handleFileSaveSuccess);
        this.rpc.subscribe("chat:file-saved-error", this::handleFileSaveError);
        this.rpc.subscribe("chat:message-deleted", this::handleBackendDelete);
    }

    /**
     * HANDLER 1: Text Message from Backend
     */
    private byte[] handleBackendTextMessage(byte[] data) {
        ChatMessage message = ChatMessageSerializer.deserialize(data);
        handleIncomingMessage(
                message.getMessageId(),
                message.getUserId(),
                message.getSenderDisplayName(),
                message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")),
                message.getReplyToMessageId(),
                message.getContent(),  // Text content
                null,                  // No file
                0,                     // No file size
                null                   // No file bytes
        );
        return new byte[0];
    }

    /**
     * HANDLER 2: File Metadata from Backend (KEY!)
     *
     * Backend sends ONLY metadata - NO file data
     */
    private byte[] handleBackendFileMetadata(byte[] data) {
        System.out.println("[FRONT] Received file metadata (no data attached)");

        try {
            FileMessage message = FileMessageSerializer.deserialize(data);

            if (message == null || message.getFileName() == null) {
                System.err.println("[FRONT] Invalid file metadata");
                return new byte[0];
            }

            // Extract compressed size for UI display
            long compressedSize = (message.getFileContent() != null)
                    ? message.getFileContent().length
                    : 0;

            handleIncomingMessage(
                    message.getMessageId(),
                    message.getUserId(),
                    message.getSenderDisplayName(),
                    message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")),
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
     * HANDLER 3: File Save Success
     */
    private byte[] handleFileSaveSuccess(byte[] data) {
        String message = new String(data, StandardCharsets.UTF_8);
        if (onShowSuccessDialog != null) {
            onShowSuccessDialog.accept("File saved successfully!\n" + message);
        }
        return new byte[0];
    }

    /**
     * HANDLER 4: File Save Error
     */
    private byte[] handleFileSaveError(byte[] data) {
        String message = new String(data, StandardCharsets.UTF_8);
        if (onShowErrorDialog != null) {
            onShowErrorDialog.accept("Failed to save file: " + message);
        }
        return new byte[0];
    }

    private byte[] handleBackendDelete(byte[] messageIdBytes) {
        String messageId = new String(messageIdBytes, StandardCharsets.UTF_8);
        messageHistory.remove(messageId);
        if (onMessageRemoved != null) {
            onMessageRemoved.accept(messageId);
        }
        return new byte[0];
    }

    public void send(String messageText) {
        if (this.attachedFile != null) {
            sendFileMessage(this.attachedFile, messageText);
        } else if (messageText != null && !messageText.trim().isEmpty()) {
            sendTextMessage(messageText);
        }

        if (onClearInput != null) onClearInput.run();
        cancelReply();
        cancelAttachment();
    }

    private void sendTextMessage(String messageText) {
        final String messageId = UUID.randomUUID().toString();
        final ChatMessage messageToSend = new ChatMessage(
                messageId,
                this.currentUserId,
                this.currentDisplayName,
                messageText,
                this.currentReplyId
        );

        byte[] messageBytes = ChatMessageSerializer.serialize(messageToSend);
        sendRpc("chat:send-text", messageBytes);

        // Optimistic update
        handleIncomingMessage(
                messageToSend.getMessageId(),
                messageToSend.getUserId(),
                messageToSend.getSenderDisplayName(),
                messageToSend.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")),
                messageToSend.getReplyToMessageId(),
                messageToSend.getContent(),
                null, 0, null
        );
    }

    /**
     * SEND FILE - Sends ONLY the path to backend
     */
    private void sendFileMessage(File file, String caption) {
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

        // Create PATH-MODE FileMessage
        final FileMessage messageToSend = new FileMessage(
                messageId,
                this.currentUserId,
                this.currentDisplayName,
                caption,
                file.getName(),
                cleanPath,  //  PATH, not bytes
                this.currentReplyId
        );

        byte[] messageBytes = FileMessageSerializer.serialize(messageToSend);
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
    }


    public void downloadFile(MessageVM fileMessage) {
        if (fileMessage == null || !fileMessage.isFileMessage()) {
            if (onShowErrorDialog != null) {
                onShowErrorDialog.accept("Invalid file message");
            }
            return;
        }

        System.out.println("[FRONT] User clicked 'Save'. Requesting backend to decompress and save.");

        byte[] messageIdBytes = fileMessage.messageId.getBytes(StandardCharsets.UTF_8);

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

    private void handleIncomingMessage(
            String messageId, String userId, String senderDisplayName,
            String formattedTime, String replyToId,
            String content,              // Text or Caption
            String fileName,             // File name
            long compressedFileSize,     // Compressed size (metadata)
            byte[] fileContent           // NULL except when needed
    ) {
        if (messageHistory.containsKey(messageId)) {
            System.out.println("[FRONT] Duplicate message ignored: " + messageId);
            return;
        }

        final boolean isSentByMe = userId.equals(this.currentUserId);
        final String username = isSentByMe ? "You" : senderDisplayName;

        String quotedContent = null;
        if (replyToId != null) {
            final MessageVM repliedTo = messageHistory.get(replyToId);
            if (repliedTo != null) {
                String sender = repliedTo.isSentByMe ? "You" : repliedTo.username;
                String contentSnippet = repliedTo.isFileMessage() ?
                        "File: " + repliedTo.fileName :
                        repliedTo.content.substring(0, Math.min(repliedTo.content.length(), 20)) + "...";
                quotedContent = "Replying to " + sender + ": " + contentSnippet;
            } else {
                quotedContent = "Reply to unavailable message";
            }
        }

        MessageVM vm = new MessageVM(
                messageId,
                username,
                content,
                fileName,
                compressedFileSize,
                fileContent,  // Usually NULL!
                formattedTime,
                isSentByMe,
                quotedContent
        );

        messageHistory.put(vm.messageId, vm);
        if (onMessageAdded != null) {
            onMessageAdded.accept(vm);
        }
    }

    public void userSelectedFileToAttach(File selectedFile) {
        if (selectedFile == null) return;

        if (selectedFile.length() > MAX_FILE_SIZE_BYTES) {
            if (onShowErrorDialog != null) {
                onShowErrorDialog.accept("File is too large (Max 50MB).");
            }
            return;
        }

        this.attachedFile = selectedFile;
        if (onAttachmentSet != null) {
            onAttachmentSet.accept("Attached: " + selectedFile.getName());
        }
    }

    public void deleteMessage(MessageVM messageToDelete) {
        if (messageToDelete == null || !messageToDelete.isSentByMe) return;

        byte[] messageIdBytes = messageToDelete.messageId.getBytes(StandardCharsets.UTF_8);
        sendRpc("chat:delete-message", messageIdBytes);

        messageHistory.remove(messageToDelete.messageId);
        if (onMessageRemoved != null) {
            onMessageRemoved.accept(messageToDelete.messageId);
        }

        if (this.currentReplyId != null && this.currentReplyId.equals(messageToDelete.messageId)) {
            cancelReply();
        }
    }





    public void cancelAttachment() {
        this.attachedFile = null;
        if (onAttachmentSet != null) onAttachmentSet.accept(null);
    }

    private void sendRpc(String endpoint, byte[] data) {
        CompletableFuture.runAsync(() -> {
            try {
                this.rpc.call(endpoint, data)
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

    public void startReply(MessageVM messageToReply) {
        this.currentReplyId = messageToReply.messageId;
        if (onReplyStateChange != null) {
            String quoteText = messageToReply.isFileMessage() ?
                    "Replying to file: " + messageToReply.fileName :
                    "Replying to " + messageToReply.username + ": " +
                            messageToReply.content.substring(0, Math.min(messageToReply.content.length(), 20)) + "...";
            onReplyStateChange.accept(quoteText);
        }
    }

    public void cancelReply() {
        this.currentReplyId = null;
        if (onReplyStateChange != null) {
            onReplyStateChange.accept(null);
        }
    }

    // --- View Bindings ---
    public void setOnMessageAdded(Consumer<MessageVM> listener) { this.onMessageAdded = listener; }
    public void setOnReplyStateChange(Consumer<String> listener) { this.onReplyStateChange = listener; }
    public void setOnClearInput(Runnable listener) { this.onClearInput = listener; }
    public void setOnMessageRemoved(Consumer<String> listener) { this.onMessageRemoved = listener; }
    public void setOnAttachmentSet(Consumer<String> listener) { this.onAttachmentSet = listener; }
    public void setOnShowErrorDialog(Consumer<String> listener) { this.onShowErrorDialog = listener; }
    public void setOnShowSuccessDialog(Consumer<String> listener) { this.onShowSuccessDialog = listener; }
}