package com.widget.noname.cola.net;

import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketServerFactory;
import org.java_websocket.drafts.Draft;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

public class NonameWebSocketServerFactory implements WebSocketServerFactory {
    @Override
    public WebSocketClient createWebSocket(WebSocketAdapter a, Draft d) {
        return new WebSocketClient(a, d);
    }

    @Override
    public WebSocketClient createWebSocket(WebSocketAdapter a, List<Draft> drafts) {
        return new WebSocketClient(a, drafts);
    }

    @Override
    public ByteChannel wrapChannel(SocketChannel channel, SelectionKey key) throws IOException {
        return channel;
    }

    @Override
    public void close() {

    }
}
