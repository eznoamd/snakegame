package game;

import java.util.concurrent.*;

//
// Controla o loop de atualização do jogo (ticks), executando uma tarefa em intervalos regulares.
public class TickLoop {

    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();

    private final Runnable tickTask;
    private final int tickRate;

    public TickLoop(Runnable tickTask, int tickRate) {
        this.tickTask = tickTask;
        this.tickRate = tickRate;
    }

    public void start() {
        long period = 1000L / tickRate;

        executor.scheduleAtFixedRate(
                tickTask,
                0,
                period,
                TimeUnit.MILLISECONDS
        );
    }

    public void stop() {
        executor.shutdown();
    }
}