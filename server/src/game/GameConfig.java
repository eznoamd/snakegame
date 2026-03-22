package game;

//
// Configurações do jogo
public class GameConfig {

    // tamanho do grid
    public static final int GRID_SIZE = 50;
    // quantos ticks por segundo
    public static final int TICK_RATE = 10;
    // o quanto o player consegue "ver" e renderizar do mapa
    public static final int VIEW_RADIUS = 12;
    // qual a porcentagem de frutas no mapa
    public static final double FRUIT_RATIO = (0.6);
    // limite de caracteres no nome
    public static final int NAME_LIMIT = 20;

    // evita ser instanciado
    private GameConfig() {}
}

