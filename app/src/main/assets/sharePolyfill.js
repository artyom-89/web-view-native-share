if (navigator.share == null) {
    var queue = [];
    var id = 0;
    var callbackName = "AndroidWebViewCallback";

    window[callbackName] = function (id, result) {
        if (queue[id] != null && typeof queue[id] === "function") {
            queue[id](result);

            delete queue[id];
        }
    };

    var isObject = function (v) {
        return typeof v === 'object' &&
            !Array.isArray(v) &&
            v !== null;
    };

    var isString = function (v) {
        return typeof v === 'string' || v instanceof String;
    };

    var getFileAsBase64 = function (file) {
        return new Promise(function (resolve, reject) {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = function () {
            var encoded = reader.result.toString();
                resolve(encoded);
            };
            reader.onerror = function (error) {
                reject(error)
            };
        });
    };

    if (Promise.all == null) {
        Promise.all = function (promises) {
            return new Promise(function (resolve, reject) {
                var result = [];
                var count = 0;
                for (var i = 0; i < promises.length; ++i) {
                    (function (index) {
                        Promise.resolve(promises[index]).then(function (res) {
                            result[index] = res;
                            ++count;
                            if (count === promises.length) {
                                resolve(result);
                            }
                        }, reject);
                    })(i);
                }
            });
        };
    }

    var getFilesAsBase64 = function (files) {
        var promises = [];
        for (var i = 0; i < files.length; ++i) {
            promises.push(getFileAsBase64(files[i]));
        }
        return Promise.all(promises);
    };

    navigator.canShare = function(config) {
        return true;
    }

    navigator.share = function(param) {
        return new Promise(function (resolve, reject) {
            if (!isObject(param)) {
                reject(new DOMException("Empty data", "DataError"));
                return;
            }

            var callback = function () {
                resolve();
            };
            ++id;
            queue[id] = callback;

            var url = null;
            if (isString(param.url)) {
                url = param.url;
            }
            var text = null;
            if (isString(param.text)) {
                text = param.text;
            }
            var title = null;
            if (isString(param.title)) {
                title = param.title;
            }

            var files = [];
            var shareIntent = function () {
                window.AndroidWebView.share(callbackName, id, url, text, title, files);
            };

            if (Array.isArray(param.files) && param.files.length > 0) {
                getFilesAsBase64(param.files).then(
                    function(_files) {
                    files = _files;
                        shareIntent();
                    },
                    function () {
                        reject(new DOMException("Empty data", "DataError"));
                    });
            } else {
                shareIntent();
            }
        });


    };
}