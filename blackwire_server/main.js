
const showIP = true

const fs = require("fs")
const path = require('path');

const logsDir = "./logs"
let logFile;
class Logger {
    static log(message, level = "log"){
        let logMessage = `[${this.nowHours()}] [${level.toUpperCase()}]: ` + message
        if(level.toUpperCase() === "ERROR"){
            console.error(logMessage)
            return
        }
        console.log(logMessage)
        fs.appendFile(logFile, logMessage + "\n", () => {

        })
    }

    static nowFormatted(){
        return new Date().toISOString().
        replace(/T/, ' ').      // replace T with a space
            replace(/\..+/, '')
    }

    static nowForFile(){
        return this.nowFormatted().split(" ")[0]
    }

    static nowHours(){
        return this.nowFormatted().split(" ")[1]
    }

    static mostRecentFile(dir){
        const files = this.orderRecentFiles(dir);
        return files.length ? files[0] : undefined;
    }

    static orderRecentFiles(dir){
        return fs.readdirSync(dir)
            .filter(file => fs.lstatSync(path.join(dir, file)).isFile())
            .map(file => ({ file, mtime: fs.lstatSync(path.join(dir, file)).mtime }))
            .sort((a, b) => b.mtime.getTime() - a.mtime.getTime());
    }
}

const version = "1.0"
if(fs.existsSync(logsDir)){
    let mostRecent = Logger.mostRecentFile(logsDir)
    if(mostRecent === undefined){
        logFile = logsDir + "/" + Logger.nowForFile()
    } else {
        let parts = mostRecent.file.split("_");
        if(parts.length === 1 || parts.length === 0){
            logFile = logsDir + "/" + Logger.nowForFile() + "_1.txt"
        } else {
            logFile = logsDir + "/" + Logger.nowForFile() + "_" + (parseInt(parts[1]) + 1) + ".txt"
        }

    }
} else {
    fs.mkdirSync(logsDir)
    logFile = logsDir + "/" + Logger.nowForFile()
}
fs.writeFileSync(logFile, "BlackWire server " + version + "\n")

Logger.log("Starting BlackWire server...")

const connectedUsers = []
const WebSocket = require("ws")

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

try {
    Logger.log("Started BlackWire server")

    wss.on("connection", (ws) => {
        if(showIP){
            connectedUsers.push(new Client(ws, ws._socket.remoteAddress))
        } else {
            connectedUsers.push(new Client(ws))
        }
        Logger.log("Connected client" + (showIP ? " with ip: " + ws._socket.remoteAddress : ""))

        ws.on("message", (rawMessage) => {
            try {
                let message = JSON.parse(rawMessage);
                if("token" in message){
                    Logger.log("Authenticating user")
                }
            } catch {
                Logger.log(rawMessage)
            }
        })
        ws.onclose = () => {
            removeClientByWebsocket(ws)
        }
    })
} catch (ex){
    Logger.log(ex, "error")
}

function removeClientByWebsocket(ws){
    for(let i = 0; i < connectedUsers.length; i++){
        if(connectedUsers[i].websocket === ws){
            Logger.log("Disconnecting client")
            connectedUsers.splice(i, 1)
            break
        }
    }
}

function sendMessage(ws, array){
    ws.send(JSON.stringify(array))
}

class Client {
    websocket
    name = null
    ip
    constructor(websocket, ip = null) {
        this.websocket = websocket
    }
}