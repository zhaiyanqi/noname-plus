'use strict';
(function () {
    var helper = {
        tag: "js_version_fragment",
        url: String,
        initialize: function () {
            this.initJavaArgs();
            this.start();
        },
        initJavaArgs: function () {
            this.url = window.version_fragment.getUrl();
            console.log(this.tag, "initJavaArgs, url: " + this.url);
        },
        start: function () {
            var url = this.url;
            var loadFailed = function () {
                console.log(this.tag, "loadFailed.");
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
                    this.loaded = true;
                    console.log("init, window.version: " + window.noname_update.version);
                    window.version_fragment.onResourceLoad(JSON.stringify(window.noname_update));
                }, loadFailed);
            }, loadFailed);
        },
        getGameVersion: function() {
            if (this.loaded) {
                return window.noname_update.version;
            } else {
                return "资源未加载";
            }
        }
    };

    helper.initialize();
}());
