import server.UdpServer;
import game.GameConfig;

public class Main {

    public static void main(String[] args) {
        try {
            UdpServer server = new UdpServer(GameConfig.PORT);
            server.start();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}