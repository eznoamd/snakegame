const utils = require("./utils.cjs");
const dgram = require("dgram");
const server = dgram.createSocket("udp4");

const GRID_SIZE = 50;
const TICK_RATE = 10;
const VIEW_RADIUS = 12;
const MAX_FRUITS = Math.trunc(0.6 * GRID_SIZE);
const PORT = 3000;
const LIMITE_NAME = 20;

let tickCounter = 0;

let map = Array.from({ length: GRID_SIZE }, () =>
    Array(GRID_SIZE).fill(0)
);

let fruitCount = 0;

// players[id] = { body, dir, pendingInputs, alive, rinfo }
let players = {};


// game
function generateFood() {
    let attempts = 0;
    while (fruitCount < MAX_FRUITS && attempts++ < 500) {
        const x = utils.randomInt(GRID_SIZE);
        const y = utils.randomInt(GRID_SIZE);
        if (map[x][y] === 0) {
            map[x][y] = 1;
            fruitCount++;
        }
    }
}

function addPlayer(id, rinfo, rawName) {
    if (players[id]) return;

    const name = utils.sanitizeName(rawName, LIMITE_NAME);

    const x = utils.randomInt(GRID_SIZE - 3) + 2;
    const y = utils.randomInt(GRID_SIZE);

    const dir = utils.chooseDirection(x, y);

    console.log(`[CREATION] - player position: [x: ${x}, y: ${y}]`);
    console.log(`[CREATION] - player direction: ${dir}`);

    players[id] = {
        name,
        body: [
            { x, y },
            { x: x - 1, y },
            { x: x - 2, y }
        ],
        dir: dir,
        pendingInputs: [],
        alive: true,
        rinfo
    };

    console.log(`Player joined: \n  ${name}\n  ${id}`);
}

function removePlayer(id) {
    delete players[id];
    console.log(`Player left: ${id}`);
}

function onClientInput(playerId, input) {
    const p = players[playerId];
    if (!p || !p.alive) return;

    if (!input || !input.dir) return;

    p.pendingInputs.push(input);
}

const OPPOSITE = {
    UP: "DOWN",
    DOWN: "UP",
    LEFT: "RIGHT",
    RIGHT: "LEFT"
};


function processInputs() {
    for (const id in players) {
        const p = players[id];
        if (!p.alive) continue;

        while (p.pendingInputs.length > 0) {
            const next = p.pendingInputs.shift();
            if (!next || typeof next.dir !== "string") continue;
            if (OPPOSITE[p.dir] === next.dir) continue;

            p.dir = next.dir;
            break;
        }
    }
}

function movePlayers() {
    for (const id in players) {
        const p = players[id];
        if (!p.alive) continue;
        const head = p.body[0];
        const next = utils.getNextPos(head, p.dir);
        p.body.unshift(next);
        p.body.pop();
    }
}

function resolveCollisions() {
    for (const id in players) {
        const p = players[id];
        if (!p.alive) continue;

        const head = p.body[0];

        // borda do mundo
        if (
            head.x < 0 || head.y < 0 ||
            head.x >= GRID_SIZE || head.y >= GRID_SIZE
        ) {
            p.alive = false;
        } else {
            // fruta
            if (map[head.x][head.y] === 1) {
                map[head.x][head.y] = 0;
                fruitCount--;
                const tail = p.body[p.body.length - 1];
                p.body.push({ ...tail });
            }

            // colisão players
            for (const oid in players) {
                const other = players[oid];
                for (let i = 0; i < other.body.length; i++) {
                    if (oid === id && i === 0) continue;
                    if (
                        head.x === other.body[i].x &&
                        head.y === other.body[i].y
                    ) {
                        p.alive = false;
                    }
                }
            }
        }

        if (!p.alive) {
            const buffer = Buffer.from(JSON.stringify({
                type: "dead",
                selfId: id
            }));
            server.send(buffer, p.rinfo.port, p.rinfo.address);
            removePlayer(id);
        }

    }

}

function getVisibleState(playerId) {
    const p = players[playerId];
    if (!p || !p.alive) return null;

    const head = p.body[0];

    const state = {
        tick: tickCounter,
        selfId: playerId,
        world: { width: GRID_SIZE, height: GRID_SIZE },
        players: {},
        fruits: []
    };

    for (let x = head.x - VIEW_RADIUS; x <= head.x + VIEW_RADIUS; x++) {
        for (let y = head.y - VIEW_RADIUS; y <= head.y + VIEW_RADIUS; y++) {
            if (
                x >= 0 && y >= 0 &&
                x < GRID_SIZE && y < GRID_SIZE &&
                map[x][y] === 1
            ) {
                state.fruits.push({ x, y });
            }
        }
    }

    for (const id in players) {
        const other = players[id];
        for (const seg of other.body) {
            if (
                Math.abs(seg.x - head.x) <= VIEW_RADIUS &&
                Math.abs(seg.y - head.y) <= VIEW_RADIUS
            ) {
                if (!state.players[id]) {
                    state.players[id] = { body: [], alive: other.alive };
                }
                state.players[id].body.push({
                    x: seg.x,
                    y: seg.y,
                    isHead: seg === other.body[0]
                });
            }
        }
    }

    return state;
}

server.on("message", (msg, rinfo) => {
    let data;
    try {
        data = JSON.parse(msg.toString());
    } catch {
        return;
    }

    const id = data.id || `${rinfo.address}:${rinfo.port}`;

    if (data.type === "join") {
        addPlayer(id, rinfo, data.name);
    }

    if (data.type === "input") {
        onClientInput(id, data.input);
    }

    if (data.type === "leave") {
        removePlayer(id);
    }
});

function sendState() {
    for (const id in players) {
        const p = players[id];
        const state = getVisibleState(id);
        if (!state) continue;

        const buffer = Buffer.from(JSON.stringify(state));
        server.send(buffer, p.rinfo.port, p.rinfo.address);
    }
}

function serverTick() {
    tickCounter++;
    processInputs();
    movePlayers();
    resolveCollisions();

    if (tickCounter % 10 === 0) generateFood();

    sendState();
}

setInterval(serverTick, 1000 / TICK_RATE);

server.bind(PORT, () => {
    console.log(`UDP server running on port ${PORT}`);
});
