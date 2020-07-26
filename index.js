var DeviceEventEmitter = require('react-native').DeviceEventEmitter;
var SnowBoy = require('react-native').NativeModules.Snowboy;
var Snowboy = {
    addEventListener(eventName, handler) {
        DeviceEventEmitter.addListener(eventName, handler)
    },
    removeEventListener(eventName, handler) {
        DeviceEventEmitter.removeListener(eventName, handler)
    },
    removeAllListeners(eventType) {
        DeviceEventEmitter.removeAllListeners(eventType)
    },
    startRecording() {
        SnowBoy.start();
    },
    stopRecording() {
        SnowBoy.stop();
    },
    initHotword() {
        return SnowBoy.initHotword();
    },
    destroy() {
        SnowBoy.destroy();
    }
}
module.exports = Snowboy;