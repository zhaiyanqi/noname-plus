'use strict';
(function () {
    var preapp = {
        url: String,
        initialize: function () {
            this.initJavaArgs();
            this.start();
        },
        initJavaArgs: function () {
            this.url = window.jsBridge.getAssetPath();
            console.log("jsBridge.getAssetPath: " + this.url);

            if (this.url) {
                localStorage.setItem('noname_inited', this.url);
            }
        },
        start: function () {
            var url = this.url;
            var loadFailed = function () {
                localStorage.removeItem('noname_inited');
                window.location.reload();
            }

            var load = function (src, onload, onerror) {
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
            window.cordovaLoadTimeout = setTimeout(loadFailed, 5000);
        }
    };

    preapp.initialize();
}());
