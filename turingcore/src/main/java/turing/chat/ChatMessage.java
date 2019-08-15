package turing.chat;

import turing.model.user.User;

public class ChatMessage {
    private User user;
    private String message;

    public ChatMessage(User user, String message) {
        this.user = user;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("<%s> %s", user.name, message);
    }
}
