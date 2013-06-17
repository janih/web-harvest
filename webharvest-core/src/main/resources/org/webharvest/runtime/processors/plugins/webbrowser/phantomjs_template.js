var pageEvaluate = function(expression) {
    return eval.apply(window, [expression]);
}

var currentResponse = null;

var initPage = function(page, pageParams) {
    page.viewportSize = {width: pageParams.width, height: pageParams.height};
    page.paperSize = {format: pageParams.paperformat, orientation: pageParams.paperorientation, border: pageParams.paperborder};
    page.settings.javascriptEnabled = pageParams.javascriptenabled == null || pageParams.javascriptenabled === "true" || pageParams.javascriptenabled === "yes";
    page.settings.loadImages = pageParams.loadimages == null || pageParams.loadimages === "true" || pageParams.loadimages === "yes";
    if (pageParams.useragent) {
        page.settings.userAgent = pageParams.useragent;
    }
    if (pageParams.username) {
        page.settings.username = pageParams.username;
    }
    if (pageParams.password) {
        page.settings.password = pageParams.password;
    }
    if (pageParams.zoomfactor) {
        page.zoomFactor = parseFloat(pageParams.zoomfactor);
    }
    page.onError = function (msg, trace) {
        if (currentResponse != null) {
            currentResponse.statusCode = -101;
            currentResponse.write(msg ? msg : "There is an error executing JavaScript!");
            currentResponse.close();
            currentResponse = null;
        }
    }
    return page;
}

var server = require('webserver').create();


var dfltPage = null;
var namedPages = {};

var getPage = function(pageName, createIfNotExist, pageParams) {
    if (!pageName) {
        if (dfltPage == null && createIfNotExist) {
            dfltPage = initPage(new WebPage(), pageParams);
        }
        return dfltPage;
    } else if (namedPages[pageName]) {
        return namedPages[pageName];
    } else if (createIfNotExist) {
        var newPage = initPage(new WebPage(), pageParams);
        namedPages[pageName] = newPage;
        return newPage;
    }
    return null;
}

var service = server.listen(${PORT}, function (request, response) {
    currentResponse = response;

    var params = request.post;
    var action = params["action"];
    var pageName = params["page"];
    var pageParams = {
        width: params["width"] ? params["width"] : "1280",
        height: params["width"] ? params["width"] : "720",
        paperformat: params["paperformat"] ? params["paperformat"] : "A4",
        paperorientation: params["paperorientation"] ? params["paperorientation"] : "portrait",
        paperborder: params["paperborder"] ? params["paperborder"] : "0",
        javascriptenabled: params["javascriptenabled"],
        loadimages: params["loadimages"],
        useragent: params["useragent"],
        username: params["username"],
        password: params["password"],
        zoomfactor: params["zoomfactor"]
    };

    var page = getPage(pageName, action == "load", pageParams);

    if (page == null) {
        response.statusCode = -101;
        response.write('Error: ' + (pageName ? 'Page \"' + pageName + '\" does not exist!': 'Default page doesn\'t exist!'));
        response.close();
        return;
    }

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
        response.statusCode = -101;
        response.write('Error:' + err);
        response.close();
    }
});
