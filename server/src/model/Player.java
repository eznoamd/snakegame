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

    public Player(String id, String name, SocketAddress address) {
        this(id, name, address, new Position(0, 0), Direction.RIGHT);
    }

    public Player(String id, String name, SocketAddress address, Position start, Direction direction) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.direction = direction;

        // Initialize body with the head at 'start' and the tail behind it
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

    public void addInput(Direction dir) {
        pendingInputs.add(dir);
    }

    public void processInput() {
        while (!pendingInputs.isEmpty()) {
            Direction next = pendingInputs.poll();
            if (direction == null || !direction.isOpposite(next)) {
                direction = next;
                break;
            }
        }
    }

    public void move() {
        if (body.isEmpty() || direction == null) return;

        Position head = this.getHead();
        Position next = Utils.getNextPos(head, direction);

        body.addFirst(next);
        body.removeLast();
    }

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

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Deque<Position> getBody() {
        return body;
    }

    public void setBody(Deque<Position> body) {
        this.body = body;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Queue<Direction> getPendingInputs() {
        return pendingInputs;
    }

    public void setPendingInputs(Queue<Direction> pendingInputs) {
        this.pendingInputs = pendingInputs;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void setAddress(SocketAddress address) {
        this.address = address;
    }

    public Position getHead(){ return body.peekFirst(); }
}