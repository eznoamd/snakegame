package game;

import java.util.concurrent.*;

//
// Controla o loop de atualização do jogo (ticks), executando uma tarefa em intervalos regulares.
public class TickLoop {

    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(); // Executor que garante execução sequencial das tarefas
    private final Runnable tickTask; // Tarefa a ser executada a cada tick (atualização do jogo)
    private final int tickRate; // Número de ticks por segundo (frequência de atualização do jogo)

    public TickLoop(Runnable tickTask, int tickRate) {
        this.tickTask = tickTask;
        this.tickRate = tickRate;
    }

    // 
    // Inicia o loop de ticks, agendando tarefa para ser executada em intervalos regulares com base no tickRate.
    public void start() {
        long period = 1000L / tickRate;

        executor.scheduleAtFixedRate(
                tickTask,
                0,
                period,
                TimeUnit.MILLISECONDS
        );
    }

    //
    // Para o loop de ticks
    public void stop() {
        executor.shutdown();
    }
}