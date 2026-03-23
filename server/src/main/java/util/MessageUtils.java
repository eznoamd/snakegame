package util;

import com.google.gson.*;
import config.ServerConfig;

import java.net.SocketAddress;
import java.util.Map;

//
// Utilitário para criar e processar mensagens JSON do jogo
// Centraliza a criação de mensagens para evitar duplicação de código
public class MessageUtils {
    
    private static final Gson gson = new Gson();
    
    //
    // Cria uma mensagem de join para um jogador
    public static String createJoinMessage(String playerName) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "join");
        msg.addProperty("name", playerName);
        return gson.toJson(msg);
    }
    
    //
    // Cria uma mensagem de input/movimento
    public static String createInputMessage(String direction) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "input");
        msg.addProperty("dir", direction);
        return gson.toJson(msg);
    }
    
    //
    // Cria uma mensagem de leave
    public static String createLeaveMessage(String playerId) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "leave");
        msg.addProperty("id", playerId);
        return gson.toJson(msg);
    }
    
    //
    // Adiciona ID de jogador a uma mensagem existente
    public static String addPlayerId(String message, String playerId) {
        JsonObject json = JsonParser.parseString(message).getAsJsonObject();
        json.addProperty("id", playerId);
        return gson.toJson(json);
    }
    
    //
    // Cria mensagem de estado do jogo para enviar ao cliente
    public static String createStateMessage(Map<String, Object> state, String selfId) {
        JsonObject msg = new JsonObject();
        msg.addProperty("selfId", selfId);
        
        // Adiciona todas as propriedades do estado
        for (Map.Entry<String, Object> entry : state.entrySet()) {
            if (entry.getValue() instanceof String) {
                msg.addProperty(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof Number) {
                msg.addProperty(entry.getKey(), (Number) entry.getValue());
            } else if (entry.getValue() instanceof Boolean) {
                msg.addProperty(entry.getKey(), (Boolean) entry.getValue());
            }
        }
        
        return gson.toJson(msg);
    }
    
    //
    // Verifica se a mensagem veio do gateway (tem ID) e retorna a porta adequada
    public static int getPortForMessage(JsonObject json, SocketAddress addr) {
        if (json.has("id")) {
            // veio do gateway (web)
            return ServerConfig.GATEWAY_RETURN_PORT;
        } else {
            // cliente UDP direto (desktop)
            return ((java.net.InetSocketAddress) addr).getPort();
        }
    }
    
    //
    // Parse JSON para objeto
    public static JsonObject fromJson(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }
    
    //
    // Converte objeto para JSON
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
}
