package turing.server;

import java.io.Closeable;
import java.io.IOException;

public interface ServerInterface extends Closeable {
    public void start() throws IOException;
}
