var server = require('webserver').create();
var page = new WebPage();
page.viewportSize = {"width":"${WIDTH}","height":"${HEIGHT}"};
page.paperSize = {"format":"${FORMAT}","orientation":"${ORIENTATION}","border":"${BORDER}"};

var pageEvaluate = function(expression) {
    return eval(expression);
}

page.onLoadStarted = function () {
}

page.onResourceRequested = function(req) {
}

page.onResourceReceived = function(res) {
}

page.onUrlChanged = function(url) {
}

var currentResponse = null;

page.onError = function (msg, trace) {
    if (currentResponse != null) {
        var errResponse = msg;
        trace.forEach(function(item) {
            errResponse += "\n" + item.file + ':' + item.line;
        });
        currentResponse.statusCode = -101;
        currentResponse.write(errResponse);
        currentResponse.close();
        currentResponse = null;

    }
}

var service = server.listen(${PORT}, function (request, response) {
    currentResponse = response;

    var params = request.post;
    var action = params["action"];

    try {
        if (action == "load") {
            var urlToLoad = params["url"];
            var pageContent = params["content"];
            if (urlToLoad) {
                if (pageContent) {
                    page.setContent(pageContent, urlToLoad);
                    response.statusCode = 200;
                    response.write(pageContent);
                    response.close();
                } else {
                    page.onLoadFinished = function (status) {
                        response.statusCode = 200;
                        response.write(page.content);
                        response.close();
                        page.onLoadFinished = null;
                    }
                    page.open(urlToLoad);
                }
            } else {
                response.statusCode = 201;
                response.write("URL is not specified!");
                response.close();
            }
        } else if (action == "eval") {
            var jsToEvaluate = params["exp"];
            var urlChange = params["urlchange"] == "true";
            if (!urlChange) {
                var evalResult = page.evaluate(pageEvaluate, jsToEvaluate);
                response.statusCode = 200;
                response.write(evalResult);
                response.close();
            } else {
                page.onLoadFinished = function (status) {
                    response.statusCode = 200;
                    response.write(page.content);
                    response.close();
                    page.onLoadFinished = null;
                }
                page.evaluate(pageEvaluate, jsToEvaluate);
            }
        } else if (action == "includejs") {
            var jsFileToInclude = params["path"];
            if (jsFileToInclude != null) {
                page.injectJs(jsFileToInclude);
                response.statusCode = 200;
                response.write("");
                response.close();
            }
        } else if (action == "rendertoimage") {
            var type = params["type"];
            if (!type) {
                type = "JPEG";
            }
            var encodedImage = page.renderBase64(type);
            if (!encodedImage) {
                encodedImage = "";
            }
            response.statusCode = 200;
            response.write(encodedImage);
            response.close();
        } else if (action == "rendertopdf") {
            var path = params["path"];
            if (path) {
                page.render(path);
                response.statusCode = 200;
                response.write("");
                response.close();
            }
        } else {
            response.write('Error: Invalid action required!');
            response.close();
        }
    } catch (err) {
        response.statusCode = 201;
        response.write('Error:' + err);
        response.close();
    }
});
