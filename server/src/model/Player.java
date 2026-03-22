package model;

import util.Utils;

import java.net.SocketAddress;
import java.util.*;

//
// Classe que estrutura um player do jogo
public class Player {

    private static final int INITIAL_LENGTH = 3;

    private String id;   // ID do player
    private String name; // Nome do player
    private Deque<Position> body = new LinkedList<>(); // Corpo do player (primeiro item é a cabeça do player)
    private Direction direction;                       // Direção de movimento do player com base em seu Input
    private Queue<Direction> pendingInputs = new LinkedList<>(); // Fila de inputs do player (não verificado o quão optimizado isso é)
    private boolean alive = true;  // Se este player esta vivo atualmente
    private SocketAddress address; // Endereço do socket do player
    private int port;

    public Player(String id, String name, SocketAddress address) {
        this(id, name, address, new Position(0, 0), Direction.RIGHT);
    }

    public Player(String id, String name, SocketAddress address, Position start, Direction direction) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.direction = direction;

        for (int i = 0; i < INITIAL_LENGTH; i++) {
            int x = start.x;
            int y = start.y;

            if (direction == Direction.RIGHT) x = start.x - i;
            if (direction == Direction.LEFT) x = start.x + i;
            if (direction == Direction.DOWN) y = start.y - i;
            if (direction == Direction.UP) y = start.y + i;

            body.addLast(new Position(x, y));
        }
    }

    //
    // Adiciona um input do jogador na fila de inputs pendentes.
    public void addInput(Direction dir) {
        pendingInputs.add(dir);
    }

    //
    // Processa os inputs pendentes do jogador
    public void processInput() {
        while (!pendingInputs.isEmpty()) {
            Direction next = pendingInputs.poll();
            if (direction == null || !direction.isOpposite(next)) {
                direction = next;
                break;
            }
        }
    }

    //
    // Move o player na direção atual
    public void move() {
        if (body.isEmpty() || direction == null) return;

        Position head = this.getHead();
        Position next = Utils.getNextPos(head, direction);

        body.addFirst(next);
        body.removeLast();
    }

    //
    // Faz o player crescer
    public void grow() {
        Position tail = body.peekLast();
        if (tail != null) {
            body.addLast(tail.copy());
        }
    }

    //
    // Getters and Setters
    public String getId() {
        return id;
    }

    public Deque<Position> getBody() {
        return body;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void setAddress(SocketAddress address) { this.address = address; }

    public SocketAddress getAddress() {
        return address;
    }

    public Position getHead(){ 
        return body.peekFirst(); 
    }

    public int getPort() { return port; }

    public void setPort(int port) { this.port = port; }
}