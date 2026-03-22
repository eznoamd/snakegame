package server;

import java.net.SocketAddress;
import config.ServerConfig;

//
// Representa uma conexão de cliente, seja direta (UDP) ou via gateway (WebSocket)
// Centraliza informações de endereço e porta de forma mais organizada
public class ClientConnection {
    
    private final String playerId;
    private final SocketAddress address;
    private final int port;
    private final ClientType type;
    
    public enum ClientType {
        DIRECT_UDP,    // Cliente UDP direto (desktop)
        WEBSOCKET      // Cliente via WebSocket (web)
    }
    
    public ClientConnection(String playerId, SocketAddress address, int port, ClientType type) {
        this.playerId = playerId;
        this.address = address;
        this.port = port;
        this.type = type;
    }
    
    //
    // Cria uma conexão para cliente UDP direto
    public static ClientConnection createDirect(String playerId, SocketAddress address) {
        int port = ((java.net.InetSocketAddress) address).getPort();
        return new ClientConnection(playerId, address, port, ClientType.DIRECT_UDP);
    }
    
    //
    // Cria uma conexão para cliente WebSocket
    public static ClientConnection createWebSocket(String playerId, SocketAddress address) {
        return new ClientConnection(playerId, address, ServerConfig.GATEWAY_RETURN_PORT, ClientType.WEBSOCKET);
    }
    
    // Getters
    public String getPlayerId() { return playerId; }
    public SocketAddress getAddress() { return address; }
    public int getPort() { return port; }
    public ClientType getType() { return type; }
    public boolean isWebSocket() { return type == ClientType.WEBSOCKET; }
    public boolean isDirectUdp() { return type == ClientType.DIRECT_UDP; }
}
