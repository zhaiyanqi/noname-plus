'use strict';
(function () {
    var preapp = {
        url: "",
        initialize: function () {
            this.initJavaArgs();
            this.start();
        },
        initJavaArgs: function () {
            var url =  window.jsBridge.getAssetPath();
            console.log("jsBridge.getAssetPath: " + url);

            if (localStorage.getItem("noname_inited") != url) {
                localStorage.setItem('noname_inited', url);
            }

            if (!location.protocol.startsWith('http')) {
                this.url = url;
            }
        },
        start: function () {
            var url = this.url;
            var loadFailed = function () {
                window.location.reload();
            }
            var load = function (src, onload, onerror) {
                console.log("load, src: " + src);
                var script = document.createElement('script');
                script.src = url + 'game/' + src + '.js';
                script.onload = onload;
                script.onerror = onerror;
                document.head.appendChild(script);
            }
            load('update', function () {
                load('config', function () {
                    load('package', function () {
                        load('game', null, loadFailed);
                    }, loadFailed);
                }, loadFailed);
            }, loadFailed);
        }
    };

    preapp.initialize();
}());
