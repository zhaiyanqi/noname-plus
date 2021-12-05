'use strict';
(function(){

    var app = {
        initialize: function() {
            this.jsReady();
            this.javaReady();
        },
        jsReady: function() {
            console.log("start: " + window.localStorage);
            console.log("jsBrige: " + window.jsBridge.getAssetPath());

            var request = window.indexedDB.open('noname_0.9_A_CRa_data',4);

            request.onsuccess=function(e){
                var db = e.target.result;
                console.log("success: " + db);
            }
        },
        javaReady: function() {
            var script=document.createElement('script');
            script.src='javabridge.js';
            document.head.appendChild(script);
            console.log("javaReady: " + script);
        }
    };

    app.initialize();
}());
