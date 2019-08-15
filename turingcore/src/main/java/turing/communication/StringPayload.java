package turing.communication;

public class StringPayload implements Payload {
    private String content;

    public StringPayload(String content) {
        this.content = content;
    }

    public StringPayload(byte[] bytes) {
        content = new String(bytes);
    }

    @Override
    public String formatted() {
        return content;
    }
}
