import server.UdpServer;
import server.UdpWebSocketGateway;
import config.ServerConfig;

public class Main {

    public static void main(String[] args) {
        try {
            UdpServer server = new UdpServer(
                    ServerConfig.SERVER_PORT
            );
            server.start();

            UdpWebSocketGateway gateway = new UdpWebSocketGateway(
                    ServerConfig.GATEWAY_PORT,
                    ServerConfig.SERVER_PORT,
                    ServerConfig.GATEWAY_RETURN_PORT,
                    ServerConfig.UDP_HOST
            );
            gateway.start();

            System.out.println("Sistema rodando:");
            System.out.println("    [Server UDP] -> " + ServerConfig.UDP_HOST + ":" + ServerConfig.SERVER_PORT);
            System.out.println("    [Gateway WebSocket] -> " + ServerConfig.UDP_HOST + ":" + ServerConfig.GATEWAY_PORT);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}