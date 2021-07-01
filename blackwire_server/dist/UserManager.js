class User {
    constructor(websocket) {
        this.websocket = websocket;
        this.name = null;
    }
    loggedIn() {
        return this.name != null;
    }
}
class Token {
}
//# sourceMappingURL=UserManager.js.map