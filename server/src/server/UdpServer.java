package server;

import com.google.gson.*;
import game.*;
import model.*;
import service.*;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.*;

//
// Servidor UDP que recebe comandos JSON dos clientes e replica o estado visível do jogo.
// Utiliza um loop de eventos para evitar condições de corrida entre recepção de pacotes
// e atualização do estado do jogo.
public class UdpServer {

    private final DatagramSocket socket;

    private final GameState gameState;
    private final GameEngine engine;

    private final PlayerService playerService = new PlayerService();
    private final VisibilityService visibilityService =
            new VisibilityService(GameConfig.VIEW_RADIUS);

    private final Gson gson = new Gson();

    private final Queue<Runnable> eventQueue = new ConcurrentLinkedQueue<>();

    private TickLoop tickLoop;

    public UdpServer(int port) throws Exception {
        this.socket = new DatagramSocket(port);
        this.gameState = new GameState(GameConfig.GRID_SIZE);
        this.engine = new GameEngine(gameState);
    }

    public void start() {
        startReceiver();

        tickLoop = new TickLoop(() -> {
            processEvents();
            engine.tick();
            sendState();
        }, GameConfig.TICK_RATE);

        tickLoop.start();

        System.out.println("UDP server running on port " + GameConfig.PORT);
    }

    private void startReceiver() {
        new Thread(() -> {
            while (true) {
                try {
                    byte[] buffer = new byte[1024];

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String msg = new String(
                            packet.getData(),
                            0,
                            packet.getLength(),
                            StandardCharsets.UTF_8
                    );

                    handleMessage(msg, packet.getSocketAddress());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handleMessage(String msg, SocketAddress addr) {
        try {
            JsonObject json = JsonParser.parseString(msg).getAsJsonObject();

            String type = json.get("type").getAsString();
            String id = addr.toString();

            switch (type) {

                case "join":
                    String name = json.get("name").getAsString();

                    eventQueue.add(() ->
                            playerService.addPlayer(gameState, id, name, addr)
                    );
                    break;

                case "input":
                    String dirStr = json.get("dir").getAsString();
                    Direction dir = Direction.valueOf(dirStr);

                    eventQueue.add(() -> {
                        Player p = gameState.getPlayers().get(id);
                        if (p != null) {
                            p.addInput(dir);
                        }
                    });
                    break;

                case "leave":
                    eventQueue.add(() ->
                            playerService.removePlayer(gameState, id)
                    );
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processEvents() {
        while (!eventQueue.isEmpty()) {
            eventQueue.poll().run();
        }
    }

    private void sendState() {
        for (Player p : gameState.getPlayers().values()) {
            try {
                Object state = visibilityService.buildState(gameState, p.getId());
                if (state == null) continue;

                byte[] data = gson.toJson(state)
                        .getBytes(StandardCharsets.UTF_8);

                DatagramPacket packet = new DatagramPacket(
                        data,
                        data.length,
                        (InetSocketAddress) p.getAddress()
                );

                socket.send(packet);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}