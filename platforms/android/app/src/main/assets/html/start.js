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

            var request = window.indexedDB.open('noname_0.9_A_CRa_data', 4);

            request.onsuccess = function (e) {
                var db = e.target.result;
                console.log("success: " + db);
            }
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
        enableExtension: function (extname, enable) {
            var key = "extension_" + extname + "_enable";
            this.putDB(key, enable);
        },
        test: function () {
            console.log("hello, ");
            console.log("path: " + window.localStorage.getItem('noname_inited'));
            console.log(window.config);

            console.log(window.localStorage.getItem(lib.configprefix + 'nodb'));

            this.putDB('extension_键杀_enable', false);

            this.getDB("extensions", function (value) {
                console.log("extensions: " + value);
            });
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
        putDB: function (key, value) {
            if (!lib.db) {
                console.log("putDB, lib.db: " + lib + ", key: " + key + ", value: " + value);
                return;
            }
            var put = lib.db.transaction(['config'], 'readwrite').objectStore('config').put(value, key);
            put.onsuccess = function () {
                console.log("putDB, success, key: " + key + ", value: " + value);
            };
        },
        getExtensions: function () {
            this.getDB("extensions", function (value) {
                console.log("extensions: " + (typeof value));
                window.jsBridge.onGetExtensions(value.toString());
            });
        }
        // game.saveConfig('extensions',lib.config.extensions);
        // game.saveConfig('extension_'+extname+'_enable',true);
    };

    app.initialize();
    window.app = app;
}());
