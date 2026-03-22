package server;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.*;

//
// Gateway que funciona como ponte entre clientes WebSocket e o servidor UDP.
// Permite que clientes web (navegadores) se conectem ao jogo que usa UDP como protocolo principal.
// Cada cliente WebSocket recebe um ID único que é usado para rotear mensagens corretamente.
public class UdpWebSocketGateway extends WebSocketServer {

    private int udpPort; // Porta do servidor UDP para onde as mensagens serão encaminhadas
    private int returnPort; // Porta UDP onde o gateway escuta as respostas do servidor
    private String udpHost; // Endereço do servidor UDP

    private DatagramSocket udpSocket; // Socket para enviar mensagens ao servidor UDP
    private InetAddress udpAddress; // Endereço do servidor UDP

    private final Map<String, WebSocket> clients = new ConcurrentHashMap<>(); // Mapeia ID do jogador -> conexão WebSocket
    private final Map<WebSocket, String> reverseClients = new ConcurrentHashMap<>(); // Mapeia conexão WebSocket -> ID do jogador

    private final Gson gson = new Gson(); // Conversor JSON para serialização de mensagens

    //
    // Construtor que configura o gateway com as portas e endereço do servidor UDP.
    // Inicia o listener UDP para receber respostas do servidor.
    public UdpWebSocketGateway(int wsPort, int udpPort, int returnPort, String udpHost) throws Exception {
        super(new InetSocketAddress(wsPort));

        this.udpPort = udpPort;
        this.udpHost = udpHost;
        this.returnPort = returnPort;

        udpSocket = new DatagramSocket();
        udpAddress = InetAddress.getByName(udpHost);

        startUdpListener();
    }

    //
    // Chamado quando um novo cliente WebSocket se conecta.
    // Gera um ID único para o jogador e armazena o mapeamento nos dois sentidos.
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String playerId = UUID.randomUUID().toString();

        clients.put(playerId, conn);
        reverseClients.put(conn, playerId);

        System.out.println("WS conectado: " + playerId);
    }

    //
    // Recebe mensagens do cliente WebSocket, adiciona o ID do jogador e encaminha para o servidor UDP.
    // Todas as mensagens dos clientes web passam por aqui antes de chegar ao servidor do jogo.
    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JsonObject data = gson.fromJson(message, JsonObject.class);

            String playerId = reverseClients.get(conn);
            data.addProperty("id", playerId);

            byte[] bytes = gson.toJson(data).getBytes(StandardCharsets.UTF_8);

            DatagramPacket packet = new DatagramPacket(
                    bytes,
                    bytes.length,
                    udpAddress,
                    udpPort
            );

            udpSocket.send(packet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String playerId = reverseClients.remove(conn);
        if (playerId != null) {
            clients.remove(playerId);

            try {
                JsonObject leaveMsg = new JsonObject();
                leaveMsg.addProperty("type", "leave");
                leaveMsg.addProperty("id", playerId);

                byte[] bytes = gson.toJson(leaveMsg).getBytes(StandardCharsets.UTF_8);

                DatagramPacket packet = new DatagramPacket(
                        bytes,
                        bytes.length,
                        udpAddress,
                        udpPort
                );

                udpSocket.send(packet);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("WS desconectado: " + playerId);
    }

    //
    // Trata erros de conexão WebSocket. Apenas imprime o stack trace para debug.
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("Erro de conexão WebSocket: " + ex.getMessage());
        ex.printStackTrace();
    }

    //
    // Chamado quando o servidor WebSocket é iniciado com sucesso.
    @Override
    public void onStart() {
        System.out.println("Gateway WS iniciado");
        System.out.println("Servidor WebSocket iniciado com sucesso!");
    }

    //
    // Inicia uma thread que escuta continuamente por respostas UDP do servidor do jogo.
    // Quando recebe uma resposta, extrai o ID do jogador (selfId) e envia a mensagem
    // apenas para o cliente WebSocket correspondente.
    private void startUdpListener() {
        new Thread(() -> {
            try {
                DatagramSocket listener = new DatagramSocket(this.returnPort);

                byte[] buffer = new byte[4096];

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    listener.receive(packet);

                    String msg = new String(packet.getData(), 0, packet.getLength());

                    JsonObject state = gson.fromJson(msg, JsonObject.class);

                    if (!state.has("selfId")) continue;

                    String playerId = state.get("selfId").getAsString();
                    WebSocket ws = clients.get(playerId);

                    if (ws != null && ws.isOpen()) {
                        ws.send(msg);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}