package game;

//
// Engine principal do jogo.
// Executa o ciclo de atualização (tick) e aplica:
//  - processamento de inputs
//  - movimentação de jogadores
//  - resolução de colisões
//  - geração de frutas
//
import service.CollisionService;
import service.FruitService;
import service.PlayerService;

public class GameEngine {

    private final GameState state; // Estado atual do jogo, contendo informações sobre jogadores, frutas, etc.

    private final CollisionService collisionService = new CollisionService(); // Serviço responsável por resolver colisões entre jogadores e frutas

    private final FruitService fruitService = new FruitService(); // Serviço responsável por gerar frutas

    private final PlayerService playerService = new PlayerService(); // Serviço responsável por gerenciar jogadores

    public GameEngine(GameState state) {
        this.state = state;
    }

    //
    // Executa um ciclo de atualização do jogo (tick). Este método é chamado em intervalos regulares pelo TickLoop.
    // Ele processa os inputs dos jogadores, move os jogadores, resolve colisões e gera frutas periodicamente.
    public void tick() {
        state.nextTick();

        playerService.processInputs(state);
        playerService.movePlayers(state);
        collisionService.resolve(state);

        if (state.getTick() % 10 == 0) {
            fruitService.generateFruits(state);
        }
    }
}