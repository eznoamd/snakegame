package config;

//
// Configurações do servidor
public class ServerConfig {
    // porta do servidor
    public static final int SERVER_PORT = 3000;
    // porta do gateway
    public static final int GATEWAY_PORT = 8080;
    // porta de retorno do gateway
    public static final int GATEWAY_RETURN_PORT = 3001;
    // configuração especifica do gateway para manter "conexão" com o server udp
    public static final String UDP_HOST = "127.0.0.1";
}
