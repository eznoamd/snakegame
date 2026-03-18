const dgram = require("dgram");
const crypto = require("crypto");
const { WebSocketServer } = require("ws");

const UDP_PORT = 3000;
const UDP_HOST = "127.0.0.1";

const WS_PORT = 8080;

const udp = dgram.createSocket("udp4");

const wss = new WebSocketServer({ port: WS_PORT });

const clients = new Map();

console.log("Gateway iniciado");

wss.on("connection", (ws) => {
    const playerId = crypto.randomUUID();
    clients.set(playerId, ws);


    ws.on("message", msg => {
        let data;
        try { data = JSON.parse(msg); } catch { return; }

        udp.send(
            Buffer.from(JSON.stringify({
                ...data,
                id: playerId
            })),
            UDP_PORT,
            UDP_HOST
        );
    });

    ws.on("close", () => {
        clients.delete(playerId);

        udp.send(
            Buffer.from(JSON.stringify({
                type: "leave",
                id: playerId
            })),
            UDP_PORT,
            UDP_HOST
        );
    });
});

udp.on("message", (msg) => {
    let state;
    try {
        state = JSON.parse(msg.toString());
    } catch { return; }

    const ws = clients.get(state.selfId);
    if (!ws || ws.readyState !== ws.OPEN) return;

    ws.send(JSON.stringify(state));
});

