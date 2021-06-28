console.log("Starting websocket server")
// @ts-ignore
const WebSocket = require('ws'); // webstorm bug
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
        clientNoContextTakeover: true, // Defaults to negotiated value.
        serverNoContextTakeover: true, // Defaults to negotiated value.
        serverMaxWindowBits: 10, // Defaults to negotiated value.
        // Below options specified as default values.
        concurrencyLimit: 10, // Limits zlib concurrency for perf.
        threshold: 8192 // Size (in bytes) below which messages
        // should not be compressed.
    }
});

console.log("Started websocket server")

wss.on("connection", (ws) => {
    console.log("Connected new client")
    sendMessage(ws, {"user": "kappega"})
    ws.on("message", (message) => {
        console.log(message)
    })
})

function sendMessage(ws, array){
    ws.send(JSON.stringify(array))
}