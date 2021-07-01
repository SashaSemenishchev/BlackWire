const showIP = true;
console.log(`[${new Date()}] Starting websocket server`);
// @ts-ignore
// const WebSocket = require('ws'); // webstorm bug
// @ts-ignore
const UserManager = require("./UserManager.js");
// @ts-ignore
const wss = new WebSocket.Server({
    port: 8080,
    perMessageDeflate: {
        zlibDeflateOptions: {
            // See zlib defaults.
            chunkSize: 1024,
            memLevel: 7,
            level: 3
        },
        zlibInflateOptions: {
            chunkSize: 10 * 1024
        },
        // Other options settable:
        clientNoContextTakeover: true,
        serverNoContextTakeover: true,
        serverMaxWindowBits: 10,
        // Below options specified as default values.
        concurrencyLimit: 10,
        threshold: 8192 // Size (in bytes) below which messages
        // should not be compressed.
    }
});
try {
    console.log(`[${new Date()}] Started websocket server`);
    wss.on("connection", (ws) => {
        Logger.log("Connected client" + (showIP ? " with ip: " + ws._socket.remoteAddress : ""));
        ws.on("message", (rawMessage) => {
            try {
                let message = JSON.parse(rawMessage);
                if ("token" in message) {
                    Logger.log("Authenticating user");
                }
            }
            catch (_a) {
                Logger.log(rawMessage);
            }
        });
        UserManager.connectedUsers.add(new UserManager.User(ws));
    });
}
catch (ex) {
    console.error(ex);
}
function sendMessage(ws, array) {
    ws.send(JSON.stringify(array));
}
class Logger {
    static log(message) {
        console.log(`[${new Date()}] ` + message);
    }
}
//# sourceMappingURL=main.js.map