import { debug } from "../Components/Helpers/Helpers";

const proto = require('./compiled.js');

function capitalize(s) {
    return s.charAt(0).toUpperCase() + s.slice(1);
}

function decapitalize(s) {
    return s.charAt(0).toLowerCase() + s.slice(1);
}

export class GameProtoSocket {
    constructor(url, msgHandler) {
        debug('[connecting]', url);
        this.socket = new WebSocket(url);
        this.socket.binaryType = 'arraybuffer';
        this.socket.onmessage = event => {
            const msg = proto.ServerGameMessage.decode(new Uint8Array(event.data));
            debug('[recvGameMsg]', msg);
            msgHandler(capitalize(msg.payload), msg[msg.payload]);
        };
    }

    waitForOpen = (bytes, waitCount = 0) => {
        if (this.socket.readyState !== 1) {
            setTimeout(() => {
                if (waitCount >= 50) {
                    return;
                } else {
                    this.waitForOpen(bytes, waitCount + 1);
                }
            }, 10);
        } else {
            this.socket.send(bytes);
        }
    };

    send = (msgType, params) => {
        // Sends client game messages
        if (this.socket.readyState !== 1) {
            debug('wtf not ready');
        }

        const msgParams = {};
        msgParams[decapitalize(msgType)] = params;
        const msg = proto.ClientGameMessage.create(msgParams);
        const bytes = proto.ClientGameMessage.encode(msg).finish();

        debug('[sendGameMsg]', msg);
        this.waitForOpen(bytes);
    };
}

export class ProtoSocket {
    constructor(url, msgHandler) {
        debug('[connecting]', url);
        this.socket = new WebSocket(url);
        this.socket.binaryType = 'arraybuffer';
        this.socket.onmessage = event => {
            const msg = proto.ServerMessage.decode(new Uint8Array(event.data));
            debug('[recvMsg]', msg);
            msgHandler(capitalize(msg.payload), msg[msg.payload]);
        };
    }

    sendMsg = bytes => {
        this.socket.send(bytes);
    };

    waitForOpen = (bytes, waitCount = 0) => {
        if (this.socket.readyState !== 1) {
            setTimeout(() => {
                if (waitCount >= 500) {
                    return;
                } else {
                    this.waitForOpen(bytes, waitCount + 1);
                }
            }, 10);
        } else {
            debug('[sentMsg]', bytes);
            this.socket.send(bytes);
        }
    };

    send = (msgType, params) => {
        // Sends client game messages
        if (this.socket.readyState !== 1) {
            debug('wtf not ready');
        }

        const msgParams = {};
        msgParams[decapitalize(msgType)] = params;
        const msg = proto.ClientMessage.create(msgParams);
        const bytes = proto.ClientMessage.encode(msg).finish();

        debug('[sendMsg]', msg);
        this.waitForOpen(bytes);
    };
}