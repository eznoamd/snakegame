const canvas = document.getElementById("game");
const ctx = canvas.getContext("2d");

// size constants
const TILE_SIZE = 16;
const VIEW_RADIUS = 12;
const VIEW_SIZE = VIEW_RADIUS * 2 + 1;
canvas.width  = TILE_SIZE * VIEW_SIZE;
canvas.height = TILE_SIZE * VIEW_SIZE;

// settings
_SNAKE_HEAD_COLOR = "#00ff88";
_SNAKE_BODY_COLOR = "#2ecc71";
_FRUIT_COLOR = "#ffcc00";
_BARRER_COLOR = "aa0000";

_GRID_ACTIVE = false;

function worldToScreen(wx, wy, cx, cy) {
    return {
        x: (wx - cx + VIEW_RADIUS) * TILE_SIZE,
        y: (wy - cy + VIEW_RADIUS) * TILE_SIZE
    };
}

function drawGrid(ctx, blockSize, viewSize) {
    ctx.strokeStyle = "#2a2a2a";
    ctx.lineWidth = 1;

    for (let i = 0; i <= viewSize; i++) {
        ctx.beginPath();
        ctx.moveTo(i * blockSize, 0);
        ctx.lineTo(i * blockSize, viewSize * blockSize);
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(0, i * blockSize);
        ctx.lineTo(viewSize * blockSize, i * blockSize);
        ctx.stroke();
    }
}


function drawWorldBorders(head, world) {
    ctx.fillStyle = _BARRER_COLOR;

    for (let dx = -VIEW_RADIUS; dx <= VIEW_RADIUS; dx++) {
        for (let dy = -VIEW_RADIUS; dy <= VIEW_RADIUS; dy++) {

            const wx = head.x + dx;
            const wy = head.y + dy;

            if (
                wx < 0 || wy < 0 ||
                wx >= world.width ||
                wy >= world.height
            ) {
                const sx = (dx + VIEW_RADIUS) * TILE_SIZE;
                const sy = (dy + VIEW_RADIUS) * TILE_SIZE;

                ctx.fillRect(sx, sy, TILE_SIZE, TILE_SIZE);
            }
        }
    }
}

function render() {
    ctx.fillStyle = "#111";
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    if (!gameState) {
        requestAnimationFrame(render);
        return;
    }

    const me = gameState.players[gameState.selfId];
    if (!me) {
        requestAnimationFrame(render);
        return;
    }

    const head = me.body[0];

    ctx.fillStyle = _FRUIT_COLOR;
    for (const f of gameState.fruits) {
        const p = worldToScreen(f.x, f.y, head.x, head.y);
        ctx.fillRect(p.x, p.y, TILE_SIZE, TILE_SIZE);
    }

    for (const id in gameState.players) {
        const player = gameState.players[id];

        for (const seg of player.body) {
            ctx.fillStyle = seg.isHead ? _SNAKE_HEAD_COLOR : _SNAKE_BODY_COLOR;
            const p = worldToScreen(seg.x, seg.y, head.x, head.y);
            ctx.fillRect(p.x, p.y, TILE_SIZE, TILE_SIZE);
        }
    }



    drawWorldBorders(head, gameState.world);

    if (_GRID_ACTIVE){
        drawGrid(ctx, TILE_SIZE, VIEW_SIZE);
    }
    requestAnimationFrame(render);
}

function getCurrentDirection(body) {
    if (!body || body.length < 2) return null;

    const head = body[0];
    const neck = body[1];

    if (head.x > neck.x) return "RIGHT";
    if (head.x < neck.x) return "LEFT";
    if (head.y > neck.y) return "DOWN";
    if (head.y < neck.y) return "UP";
}

const OPPOSITE = {
    UP: "DOWN",
    DOWN: "UP",
    LEFT: "RIGHT",
    RIGHT: "LEFT"
};

const KEYS = {
    ArrowUp: "UP",
    ArrowDown: "DOWN",
    ArrowLeft: "LEFT",
    ArrowRight: "RIGHT"
};

function enableInput() {
    window.addEventListener("keydown", e => {
        if (!gameState) return;

        const newDir = KEYS[e.key];
        if (!newDir) return;

        const me = gameState.players[gameState.selfId];
        if (!me) return;

        const currentDir = getCurrentDirection(me.body);
        if (OPPOSITE[currentDir] === newDir) return;

        socket.send(JSON.stringify({
            type: "input",
            input: { dir: newDir }
        }));
    });
}


function changeView() {
    document.getElementById("menuContainer").classList.add("hidden");
    document.getElementById("gameContainer").classList.remove("hidden");
}

document.getElementById("joinBtn").addEventListener("click", () => {
    const nameInput = document.getElementById("nameInput");
    const name = nameInput.value.trim();

    if (name.length === 0) {
        alert("Por favor, insira um nome.");
        return;
    }

    socket.send(JSON.stringify({
        type: "join",
        name: name
    }));

    render();
    changeView();
    enableInput();
});


//
// Settings Menu
//
document.getElementById("settingsBtn").addEventListener("click", () => {
    const settingsMenu = document.getElementById("settingsMenu");
    settingsMenu.classList.toggle("hidden");
}); 

document.getElementById("gridToggle").addEventListener("change", (e) => {
    _GRID_ACTIVE = e.target.checked;
});

document.getElementById("closeSettingsBtn").addEventListener("click", () => {
    const settingsMenu = document.getElementById("settingsMenu");
    settingsMenu.classList.add("hidden");
});

const fruitColorPicker = document.getElementById("fruitColor");
fruitColorPicker.addEventListener("change", watchFruitColorPicker);

function watchFruitColorPicker(event) {
  _FRUIT_COLOR = event.target.value;
}

const barerColorPicker = document.getElementById("barerColor");
barerColorPicker.addEventListener("change", watchBarerColorPicker);

function watchBarerColorPicker(event) {
  _BARRER_COLOR = event.target.value;
}

function saveSettings() {
    localStorage.setItem("gridToggle", _GRID_ACTIVE);
    localStorage.setItem("fruitColor", _FRUIT_COLOR);
    localStorage.setItem("barerColor", _BARRER_COLOR);
}

document.getElementById("saveSettingsBtn").addEventListener("click", () => {
    saveSettings();
    const settingsMenu = document.getElementById("settingsMenu");
    settingsMenu.classList.add("hidden");
});

function loadSettings() {
    const gridToggle = localStorage.getItem("gridToggle");

    if (gridToggle) {
        _GRID_ACTIVE = (gridToggle === "true");
        document.getElementById("gridToggle").checked = _GRID_ACTIVE;
    }

    const fruitColor = localStorage.getItem("fruitColor");
    if (fruitColor) {
        _FRUIT_COLOR = fruitColor;
        fruitColorPicker.value = fruitColor;
    } else {
        fruitColorPicker.value = _FRUIT_COLOR;
    }

    const barerColor = localStorage.getItem("barerColor");
    if (barerColor) {
        _BARRER_COLOR = barerColor;
        barerColorPicker.value = barerColor;
    }else {
        barerColorPicker.value = _BARRER_COLOR;
    }

}

loadSettings();

// socket handling
const socket = new WebSocket("ws://localhost:8080");
let gameState = null;

socket.onopen = () => {
    console.log("Conectado ao servidor");
};

socket.onmessage = (event) => {
    const data = JSON.parse(event.data);

    if (data.type === "dead") {
        alert("Você morreu!");
        
        gameState = null;
        document.getElementById("gameContainer").classList.add("hidden");
        document.getElementById("menuContainer").classList.remove("hidden");
        return;
    }

    gameState = data;
};

socket.onerror = (err) => {
    console.error("WebSocket erro:", err);
};
