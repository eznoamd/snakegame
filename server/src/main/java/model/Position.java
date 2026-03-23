package model;

//
// Classe que generaliza um posição (x, y)
// É usada para guardar posição de body do player ou de frutas no mapa
public class Position {
    public int x;
    public int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position copy() {
        return new Position(x, y);
    }
}