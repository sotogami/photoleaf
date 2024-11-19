package com.example.photoleaf.Message;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class MessageDTO {

    private Long senderId;
    private Long receiverId;
    private String content;

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    // Convert a Message entity to a DTO
    public static MessageDTO fromEntity(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setSenderId(message.getSender().getId());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setContent(message.getContent());
        return dto;
    }

    // Convert a list of Message entities to a list of DTOs
    public static List<MessageDTO> fromEntityList(List<Message> messages) {
        return messages.stream()
                .map(msg -> fromEntity(msg))
                .collect(toList());
    }
}