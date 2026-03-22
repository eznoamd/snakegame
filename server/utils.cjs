module.exports = {

    //
    // Escolhe a direção com base na prioridade de escolha
    // se |x| > |y| a prioridade de escolha será em x, assim
    // escolhendo se ira para esquerda (menor que grid_size/2)
    // ou se ira para direita (maior que grid_size/2)
    chooseDirection: (x, y, GRID_SIZE = 50) => {
        mod_x = Math.abs(x);
        mod_y = Math.abs(y);

        x_dir = "RIGHT"
        x = x/GRID_SIZE;
        if (x > 0.5) x_dir = "LEFT";
        
        y_dir = "DOWN"
        y = y/GRID_SIZE;
        if (y > 0.5) y_dir = "UP";

        dir = x_dir; 
        if (mod_x <= mod_y){
            dir = y_dir; 
        }
        return dir;
    },

    //
    // Retorna um int aleatório levando em conta o max possivel
    randomInt: (max) => {
        return Math.floor(Math.random() * max);
    },

    //
    // retorna a string de nome limpa
    sanitizeName: (name, LIMITE_NAME = 20) => {
        if (typeof name !== "string") return "Player";
        return name
            .trim()
            .slice(0, LIMITE_NAME)
            .replace(/[^\w ]/g, "");
    },

    //
    // retorna para qual posição o player deve ir 
    // com base na direção que esta ativa
    getNextPos: (pos, dir) => {
        switch (dir) {
            case "UP": return { x: pos.x, y: pos.y - 1 };
            case "DOWN": return { x: pos.x, y: pos.y + 1 };
            case "LEFT": return { x: pos.x - 1, y: pos.y };
            case "RIGHT": return { x: pos.x + 1, y: pos.y };
        }
    }
}