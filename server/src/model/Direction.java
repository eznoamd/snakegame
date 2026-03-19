package model;

//
// Enum para facilitar a separação das direções de controle do jogo
public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    //
    // Esse metodo é usado para evitar que o player faça movimentos estranhos como
    // Indo direita -> esquerda (com body maior que 3) ele automaticamente morre
    public boolean isOpposite(Direction other) {
        return (this == UP && other == DOWN) ||
                (this == DOWN && other == UP) ||
                (this == LEFT && other == RIGHT) ||
                (this == RIGHT && other == LEFT);
    }
}