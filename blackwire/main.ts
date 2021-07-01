const { app, BrowserWindow, ipc } = require('electron')
const path = require('path')
const cryptico = require("crypto")
const fs = require("fs")
const homedir = require('os').homedir();

// @ts-ignore
const WebSocket = require('ws'); // webstorm bug


function createWindow () {
    const mainWindow = new BrowserWindow({
        width: 1350,
        height: 780,
        webPreferences: {
            preload: path.join(__dirname, 'preload.ts')
        },
        darkTheme: true
    })

    mainWindow.removeMenu()
    mainWindow.loadFile('templates/index.html')
    app.setBadgeCount(1)
}

app.whenReady().then(() => {
    createWindow()
    app.on('activate', function () {
        if (BrowserWindow.getAllWindows().length === 0) createWindow()
    })
})

app.on('window-all-closed', function () {
    if (process.platform !== 'darwin') app.quit()
})

const ws = new WebSocket('ws://localhost:8080');
ws.onmessage = data => {
    let received;
    try {
        received = JSON.parse(data.data)
    } catch (exception) {
        received = null;
        console.log(data.data)
    }

    if(received == null){
        return
    }
    if("status" in received){
        if(received.status === "show_ip"){
            console.warn("This server is collects your ip address")
        }
    }
}

