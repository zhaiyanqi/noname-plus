'use strict';
(function () {

    var lib = {
        configprefix: 'noname_0.9_'

    };

    var app = {
        initialize: function () {
            this.jsReady();
            // this.javaReady();
            this.openDB();
        },
        jsReady: function () {
            console.log("start: " + window.localStorage);
            console.log("jsBrige: " + window.jsBridge.getAssetPath());
        },
        javaReady: function () {
            var script = document.createElement('script');
            script.src = 'javabridge.js';
            document.head.appendChild(script);
            console.log("javaReady: " + script);
        },
        openDB: function () {
            var request = window.indexedDB.open(lib.configprefix + 'data', 4);
            request.onupgradeneeded = function (e) {
                var db = e.target.result;
                if (!db.objectStoreNames.contains('video')) {
                    db.createObjectStore('video', { keyPath: 'time' });
                }
                if (!db.objectStoreNames.contains('image')) {
                    db.createObjectStore('image');
                }
                if (!db.objectStoreNames.contains('audio')) {
                    db.createObjectStore('audio');
                }
                if (!db.objectStoreNames.contains('config')) {
                    db.createObjectStore('config');
                }
                if (!db.objectStoreNames.contains('data')) {
                    db.createObjectStore('data');
                }
            };
            request.onsuccess = function (e) {
                lib.db = e.target.result;
                console.log("openDB success.");
                app.onJsInited();
            }
        },
        onJsInited: function () {
            console.log('onJsInited');
            window.jsBridge.onPageStarted();
        },
        getDB: function (key, callback) {
            if (!lib.db) {
                console.log("getDB, lib.db: " + lib + ", key: " + key);
                return;
            }

            if (key) {
                var store = lib.db.transaction(['config'], 'readwrite').objectStore('config');
                store.get(key).onsuccess = function (e) {
                    callback(e.target.result)
                };
            }
        },
        putDB: function (key, value, onsucc) {
            if (!lib.db) {
                console.log("putDB, lib.db: " + lib + ", key: " + key + ", value: " + value);
                return;
            }
            var put = lib.db.transaction(['config'], 'readwrite').objectStore('config').put(value, key);
            put.onsuccess = function () {
                console.log("putDB, success, key: " + key + ", value: " + value);

                if (onsucc) {
                    onsucc();
                }
            };
        },
        getExtensions: function () {
            this.getDB("extensions", function (value) {
                if (value) {
                    window.jsBridge.onGetExtensions(value.toString());
                } else {
                    window.jsBridge.onGetExtensions(null);
                }
            });
        },
        getExtensionState: function(extname) {
            var key = "extension_" + extname + "_enable";
            this.getDB(key, function(value) {
                window.jsBridge.onExtensionStateGet(extname, value);
            })
        },
        enableExtension: function (extname, enable) {
            var key = "extension_" + extname + "_enable";
            this.putDB(key, enable);
        },
        setServerIp: function (ip, directStart) {
            if (directStart) {
                this.putDB('mode','connect', function() {
                    app.setServerIp(ip, false);
                });
            } else {
                this.putDB("last_ip", ip, function() {
                    window.jsBridge.onServeIpSet();
                });
            }
        }
    };

    app.initialize();
    window.app = app;
}());
