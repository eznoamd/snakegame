package server;

import game.*;
import model.*;
import service.*;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.*;
import com.google.gson.*;

//
// Servidor UDP que recebe comandos JSON dos clientes e replica o estado visível do jogo.
// Utiliza um loop de eventos para evitar condições de corrida entre recepção de pacotes
// e atualização do estado do jogo.
public class UdpServer {

    private final DatagramSocket socket; // Socket que envia e recebe pacotes
    
    private final GameState gameState;   // Estado global do jogo
    
    private final GameEngine engine;     // Engine principal do jogo, responsável por atualizar o estado do jogo a cada tick
    
    private final PlayerService playerService = new PlayerService(); // Add e remove de usuarios

    private final VisibilityService visibilityService = new VisibilityService(GameConfig.VIEW_RADIUS); // Padrão de visão dos jogadores

    private final Gson gson = new Gson(); // Instancia (objetos <-> json)
    
    private final Queue<Runnable> eventQueue = new ConcurrentLinkedQueue<>(); // Armazena ações (Runnables)

    private TickLoop tickLoop; // Loop que executa o tick do jogo em intervalos regulares

    public UdpServer(int port) throws Exception {
        this.socket = new DatagramSocket(port);
        this.gameState = new GameState(GameConfig.GRID_SIZE);
        this.engine = new GameEngine(gameState);
    }

    //
    // Inicia o servidor, começando a escutar por pacotes e executando o loop de ticks do jogo.
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

    //
    // Inicia uma thread que fica escutando por pacotes UDP. Quando um pacote é recebido, ele é processado
    // e a ação correspondente é adicionada à fila de eventos para ser executada no próximo tick do jogo.
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

    //
    // Processa uma mensagem JSON recebida de um cliente. Dependendo do tipo da mensagem ("join", "input", "leave"),
    // a ação correspondente é adicionada à fila de eventos para ser executada no próximo tick do jogo.
    private void handleMessage(String msg, SocketAddress addr) {
        try {
            JsonObject json = JsonParser.parseString(msg).getAsJsonObject();

            String type = json.get("type").getAsString();
            String id = addr.toString();

            switch (type) {

                // "join": um novo jogador quer entrar no jogo. O servidor adiciona o jogador ao estado do jogo.
                case "join":
                    String name = json.get("name").getAsString();

                    eventQueue.add(() ->
                            playerService.addPlayer(gameState, id, name, addr)
                    );
                    break;

                // "input": um jogador envia um comando de direção. O servidor adiciona esse comando à fila de inputs do jogador.
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

                // "leave": um jogador quer sair do jogo. O servidor remove o jogador do estado do jogo.
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

    //
    // Processa todas as ações na fila de eventos. Isso garante que as ações sejam executadas de forma ordenada e sem condições de corrida.
    private void processEvents() {
        while (!eventQueue.isEmpty()) {
            eventQueue.poll().run();
        }
    }

    //
    // Envia o estado visível do jogo para cada jogador. O servidor constrói um objeto de estado específico para cada jogador, 
    // contendo apenas as informações que ele pode ver, e envia esse objeto como JSON em um pacote UDP para o endereço do jogador.
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