package io.vertx.ext.web.api.contract.openapi3;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is used by OpenAPI3ParametersUnitTest
 */
public class ApiClient {
    private WebClient client;
    private int port;
    private String host;



    private MultiMap cookieParams;

    ApiClient(Vertx vertx, String host, int port) {
        client = WebClient.create(vertx, new WebClientOptions().setDefaultHost(host).setDefaultPort(port));
        this.port = port;
        this.host = host;

        cookieParams = MultiMap.caseInsensitiveMultiMap();
    }

    ApiClient(WebClient client) {
        this.client = client;

        cookieParams = MultiMap.caseInsensitiveMultiMap();
    }

    /**
     * Call path_matrix_noexplode_string with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathMatrixNoexplodeString(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/matrix/noexplode/string/{;color}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathMatrix("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_matrix_noexplode_array with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathMatrixNoexplodeArray(
        List<Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/matrix/noexplode/array/{;color}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathArrayMatrix("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_matrix_noexplode_object with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathMatrixNoexplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/matrix/noexplode/object/{;color}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathObjectMatrix("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_matrix_explode_string with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathMatrixExplodeString(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/matrix/explode/string/{;color*}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathMatrix("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_matrix_explode_array with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathMatrixExplodeArray(
        List<Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/matrix/explode/array/{;color*}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathArrayMatrixExplode("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_matrix_explode_object with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathMatrixExplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/matrix/explode/object/{;color*}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathObjectMatrixExplode("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_label_noexplode_string with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathLabelNoexplodeString(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/label/noexplode/string/{.color}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathLabel("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_label_noexplode_array with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathLabelNoexplodeArray(
        List<Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/label/noexplode/array/{.color}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathArrayLabel("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_label_noexplode_object with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathLabelNoexplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/label/noexplode/object/{.color}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathObjectLabel("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_label_explode_string with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathLabelExplodeString(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/label/explode/string/{.color*}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathLabel("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_label_explode_array with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathLabelExplodeArray(
        List<Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/label/explode/array/{.color*}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathArrayLabelExplode("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_label_explode_object with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathLabelExplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/label/explode/object/{.color*}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathObjectLabelExplode("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_simple_noexplode_string with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathSimpleNoexplodeString(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/simple/noexplode/string/{color}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathParam("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_simple_noexplode_array with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathSimpleNoexplodeArray(
        List<Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/simple/noexplode/array/{color}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathArraySimple("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_simple_noexplode_object with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathSimpleNoexplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/simple/noexplode/object/{color}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathObjectSimple("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_simple_explode_string with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathSimpleExplodeString(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/simple/explode/string/{color*}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathParam("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_simple_explode_array with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathSimpleExplodeArray(
        List<Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/simple/explode/array/{color*}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathArraySimpleExplode("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_simple_explode_object with empty body.
     * @param color Parameter color inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathSimpleExplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color in path");


        // Generate the uri
        String uri = "/path/simple/explode/object/{color*}";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color}", this.renderPathObjectSimpleExplode("color", color));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_multi_simple_label with empty body.
     * @param colorSimple Parameter color_simple inside path
     * @param colorLabel Parameter color_label inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathMultiSimpleLabel(
        String colorSimple,
        List<Object> colorLabel,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (colorSimple == null) throw new RuntimeException("Missing parameter colorSimple in path");
        if (colorLabel == null) throw new RuntimeException("Missing parameter colorLabel in path");


        // Generate the uri
        String uri = "/path/multi/{color_simple}{.color_label}/test";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color_simple}", this.renderPathParam("color_simple", colorSimple));
        uri = uri.replace("{color_label}", this.renderPathArrayLabel("color_label", colorLabel));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_multi_simple_matrix with empty body.
     * @param colorSimple Parameter color_simple inside path
     * @param colorMatrix Parameter color_matrix inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathMultiSimpleMatrix(
        String colorSimple,
        List<Object> colorMatrix,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (colorSimple == null) throw new RuntimeException("Missing parameter colorSimple in path");
        if (colorMatrix == null) throw new RuntimeException("Missing parameter colorMatrix in path");


        // Generate the uri
        String uri = "/path/multi/{color_simple}{;color_matrix}/test";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color_simple}", this.renderPathParam("color_simple", colorSimple));
        uri = uri.replace("{color_matrix}", this.renderPathArrayMatrix("color_matrix", colorMatrix));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call path_multi_label_matrix with empty body.
     * @param colorLabel Parameter color_label inside path
     * @param colorMatrix Parameter color_matrix inside path
     * @param handler The handler for the asynchronous request
     */
    public void pathMultiLabelMatrix(
        List<Object> colorLabel,
        Map<String, Object> colorMatrix,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (colorLabel == null) throw new RuntimeException("Missing parameter colorLabel in path");
        if (colorMatrix == null) throw new RuntimeException("Missing parameter colorMatrix in path");


        // Generate the uri
        String uri = "/path/multi/{.color_label}{;color_matrix*}/test";
        uri = uri.replaceAll("\\{{1}([.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*)\\}{1}", "{$2}"); //Remove * . ; ? from url template
        uri = uri.replace("{color_label}", this.renderPathArrayLabel("color_label", colorLabel));
        uri = uri.replace("{color_matrix}", this.renderPathObjectMatrixExplode("color_matrix", colorMatrix));


        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call query_form_noexplode_empty with empty body.
     * @param color Parameter color inside query
     * @param handler The handler for the asynchronous request
     */
    public void queryFormNoexplodeEmpty(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/query/form/noexplode/empty";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addQueryParam("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call query_form_noexplode_string with empty body.
     * @param color Parameter color inside query
     * @param handler The handler for the asynchronous request
     */
    public void queryFormNoexplodeString(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/query/form/noexplode/string";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addQueryParam("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call query_form_noexplode_array with empty body.
     * @param color Parameter color inside query
     * @param handler The handler for the asynchronous request
     */
    public void queryFormNoexplodeArray(
        List<Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/query/form/noexplode/array";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addQueryArrayForm("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call query_form_noexplode_object with empty body.
     * @param color Parameter color inside query
     * @param handler The handler for the asynchronous request
     */
    public void queryFormNoexplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/query/form/noexplode/object";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addQueryObjectForm("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call query_form_explode_empty with empty body.
     * @param color Parameter color inside query
     * @param handler The handler for the asynchronous request
     */
    public void queryFormExplodeEmpty(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/query/form/explode/empty";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addQueryParam("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call query_form_explode_string with empty body.
     * @param color Parameter color inside query
     * @param handler The handler for the asynchronous request
     */
    public void queryFormExplodeString(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/query/form/explode/string";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addQueryParam("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call query_form_explode_array with empty body.
     * @param color Parameter color inside query
     * @param handler The handler for the asynchronous request
     */
    public void queryFormExplodeArray(
        List<Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/query/form/explode/array";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addQueryArrayFormExplode("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call query_form_explode_object with empty body.
     * @param color Parameter color inside query
     * @param handler The handler for the asynchronous request
     */
    public void queryFormExplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/query/form/explode/object";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addQueryObjectFormExplode("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call query_spaceDelimited_noexplode_array with empty body.
     * @param color Parameter color inside query
     * @param handler The handler for the asynchronous request
     */
    public void querySpaceDelimitedNoexplodeArray(
        List<Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/query/spaceDelimited/noexplode/array";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addQueryArraySpaceDelimited("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call query_spaceDelimited_noexplode_object with empty body.
     * @param color Parameter color inside query
     * @param handler The handler for the asynchronous request
     */
    public void querySpaceDelimitedNoexplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/query/spaceDelimited/noexplode/object";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addQueryObjectSpaceDelimited("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call query_pipeDelimited_noexplode_array with empty body.
     * @param color Parameter color inside query
     * @param handler The handler for the asynchronous request
     */
    public void queryPipeDelimitedNoexplodeArray(
        List<Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/query/pipeDelimited/noexplode/array";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addQueryArrayPipeDelimited("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call query_pipeDelimited_noexplode_object with empty body.
     * @param color Parameter color inside query
     * @param handler The handler for the asynchronous request
     */
    public void queryPipeDelimitedNoexplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/query/pipeDelimited/noexplode/object";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addQueryObjectPipeDelimited("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call query_deepObject_explode_object with empty body.
     * @param color Parameter color inside query
     * @param handler The handler for the asynchronous request
     */
    public void queryDeepObjectExplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/query/deepObject/explode/object";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addQueryObjectDeepObjectExplode("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call cookie_form_noexplode_empty with empty body.
     * @param color Parameter color inside cookie
     * @param handler The handler for the asynchronous request
     */
    public void cookieFormNoexplodeEmpty(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/cookie/form/noexplode/empty";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.renderCookieParam("color", color, requestCookies);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call cookie_form_noexplode_string with empty body.
     * @param color Parameter color inside cookie
     * @param handler The handler for the asynchronous request
     */
    public void cookieFormNoexplodeString(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/cookie/form/noexplode/string";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.renderCookieParam("color", color, requestCookies);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call cookie_form_noexplode_array with empty body.
     * @param color Parameter color inside cookie
     * @param handler The handler for the asynchronous request
     */
    public void cookieFormNoexplodeArray(
        List<Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/cookie/form/noexplode/array";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.renderCookieArrayForm("color", color, requestCookies);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call cookie_form_noexplode_object with empty body.
     * @param color Parameter color inside cookie
     * @param handler The handler for the asynchronous request
     */
    public void cookieFormNoexplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/cookie/form/noexplode/object";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.renderCookieObjectForm("color", color, requestCookies);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call cookie_form_explode_empty with empty body.
     * @param color Parameter color inside cookie
     * @param handler The handler for the asynchronous request
     */
    public void cookieFormExplodeEmpty(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/cookie/form/explode/empty";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.renderCookieParam("color", color, requestCookies);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call cookie_form_explode_string with empty body.
     * @param color Parameter color inside cookie
     * @param handler The handler for the asynchronous request
     */
    public void cookieFormExplodeString(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/cookie/form/explode/string";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.renderCookieParam("color", color, requestCookies);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call cookie_form_explode_array with empty body.
     * @param color Parameter color inside cookie
     * @param handler The handler for the asynchronous request
     */
    public void cookieFormExplodeArray(
        List<Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/cookie/form/explode/array";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.renderCookieArrayFormExplode("color", color, requestCookies);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call cookie_form_explode_object with empty body.
     * @param color Parameter color inside cookie
     * @param handler The handler for the asynchronous request
     */
    public void cookieFormExplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/cookie/form/explode/object";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.renderCookieObjectFormExplode("color", color, requestCookies);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call header_simple_noexplode_string with empty body.
     * @param color Parameter color inside header
     * @param handler The handler for the asynchronous request
     */
    public void headerSimpleNoexplodeString(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/header/simple/noexplode/string";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addHeaderParam("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call header_simple_noexplode_array with empty body.
     * @param color Parameter color inside header
     * @param handler The handler for the asynchronous request
     */
    public void headerSimpleNoexplodeArray(
        List<Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/header/simple/noexplode/array";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addHeaderArraySimple("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call header_simple_noexplode_object with empty body.
     * @param color Parameter color inside header
     * @param handler The handler for the asynchronous request
     */
    public void headerSimpleNoexplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/header/simple/noexplode/object";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addHeaderObjectSimple("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call header_simple_explode_string with empty body.
     * @param color Parameter color inside header
     * @param handler The handler for the asynchronous request
     */
    public void headerSimpleExplodeString(
        String color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/header/simple/explode/string";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addHeaderParam("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call header_simple_explode_array with empty body.
     * @param color Parameter color inside header
     * @param handler The handler for the asynchronous request
     */
    public void headerSimpleExplodeArray(
        List<Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/header/simple/explode/array";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addHeaderArraySimpleExplode("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }

    /**
     * Call header_simple_explode_object with empty body.
     * @param color Parameter color inside header
     * @param handler The handler for the asynchronous request
     */
    public void headerSimpleExplodeObject(
        Map<String, Object> color,
        Handler<AsyncResult<HttpResponse>> handler) {
        // Check required params
        if (color == null) throw new RuntimeException("Missing parameter color");


        // Generate the uri
        String uri = "/header/simple/explode/object";

        HttpRequest request = client.get(uri);

        MultiMap requestCookies = MultiMap.caseInsensitiveMultiMap();
        if (color != null) this.addHeaderObjectSimpleExplode("color", color, request);


        this.renderAndAttachCookieHeader(request, requestCookies);
        request.send(handler);
    }




    // Parameters functions

    /**
     * Remove a cookie parameter from the cookie cache
     *
     * @param paramName name of cookie parameter
     */
    public void removeCookie(String paramName) {
        cookieParams.remove(paramName);
    }

    private void addQueryParam(String paramName, Object value, HttpRequest request) {
        request.addQueryParam(paramName, String.valueOf(value));
    }

    /**
     * Add a cookie param in cookie cache
     *
     * @param paramName name of cookie parameter
     * @param value value of cookie parameter
     */
    public void addCookieParam(String paramName, Object value) {
        renderCookieParam(paramName, value, cookieParams);
    }

    private void addHeaderParam(String headerName, Object value, HttpRequest request) {
        request.putHeader(headerName, String.valueOf(value));
    }

    private String renderPathParam(String paramName, Object value) {
        return String.valueOf(value);
    }

    private void renderCookieParam(String paramName, Object value, MultiMap map) {
        map.remove(paramName);
        map.add(paramName, String.valueOf(value));
    }

    /**
     * Following this table to implement parameters serialization
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | style          | explode | in            | array                               | object                                 |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | matrix         | false   | path          | ;color=blue,black,brown             | ;color=R,100,G,200,B,150               |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | matrix         | true    | path          | ;color=blue;color=black;color=brown | ;R=100;G=200;B=150                     |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | label          | false   | path          | .blue.black.brown                   | .R.100.G.200.B.150                     |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | label          | true    | path          | .blue.black.brown                   | .R=100.G=200.B=150                     |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | form           | false   | query, cookie | color=blue,black,brown              | color=R,100,G,200,B,150                |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | form           | true    | query, cookie | color=blue&color=black&color=brown  | R=100&G=200&B=150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | simple         | false   | path, header  | blue,black,brown                    | R,100,G,200,B,150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | simple         | true    | path, header  | blue,black,brown                    | R=100,G=200,B=150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | spaceDelimited | false   | query         | blue%20black%20brown                | R%20100%20G%20200%20B%20150            |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | pipeDelimited  | false   | query         | blue|black|brown                    | R|100|G|200                            |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | deepObject     | true    | query         | n/a                                 | color[R]=100&color[G]=200&color[B]=150 |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     */

    /**
     * Render path value with matrix style exploded/not exploded
     *
     * @param paramName
     * @param value
     * @return
     */
    private String renderPathMatrix(String paramName, Object value) {
        return ";" + paramName + "=" + String.valueOf(value);
    }

    /**
     * Render path array with matrix style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | matrix         | false   | path          | ;color=blue,black,brown             | ;color=R,100,G,200,B,150               |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @return
     */
    private String renderPathArrayMatrix(String paramName, List<Object> values) {
        String serialized = String.join(",", values.stream().map(object -> encode(String.valueOf(object))).collect(Collectors.toList()));
        return ";" + paramName + "=" + serialized;
    }

    /**
     * Render path object with matrix style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | matrix         | false   | path          | ;color=blue,black,brown             | ;color=R,100,G,200,B,150               |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @return
     */
    private String renderPathObjectMatrix(String paramName, Map<String, Object> values) {
        List<String> listToSerialize = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            listToSerialize.add(entry.getKey());
            listToSerialize.add(encode(String.valueOf(entry.getValue())));
        }
        String serialized = String.join(",", listToSerialize);
        return ";" + paramName + "=" + serialized;
    }

    /**
     * Render path array with matrix style and exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | matrix         | true    | path          | ;color=blue;color=black;color=brown | ;R=100;G=200;B=150                     |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @return
     */
    private String renderPathArrayMatrixExplode(String paramName, List<Object> values) {
        return String.join("", values.stream().map(object -> ";" + paramName + "=" + encode(String.valueOf(object))).collect(Collectors.toList()));
    }

    /**
     * Render path object with matrix style and exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | matrix         | true    | path          | ;color=blue;color=black;color=brown | ;R=100;G=200;B=150                     |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @return
     */
    private String renderPathObjectMatrixExplode(String paramName, Map<String, Object> values) {
      return String.join("", values.entrySet().stream().map(
        entry -> ";" + entry.getKey() + "=" + encode(String.valueOf(entry.getValue()))
      ).collect(Collectors.toList()));
    }

    /**
     * Render path value with label style exploded/not exploded
     *
     * @param paramName
     * @param value
     * @return
     */
    private String renderPathLabel(String paramName, Object value) {
        return "." + String.valueOf(value);
    }

    /**
     * Render path array with label style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | label          | false   | path          | .blue.black.brown                   | .R.100.G.200.B.150                     |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @return
     */
    private String renderPathArrayLabel(String paramName, List<Object> values) {
        return "." + String.join(".", values.stream().map(object -> encode(String.valueOf(object))).collect(Collectors.toList()));
    }

    /**
     * Render path object with label style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | label          | false   | path          | .blue.black.brown                   | .R.100.G.200.B.150                     |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @return
     */
    private String renderPathObjectLabel(String paramName, Map<String, Object> values) {
        List<String> listToSerialize = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            listToSerialize.add(entry.getKey());
            listToSerialize.add(encode(String.valueOf(entry.getValue())));
        }
        return "." + String.join(".", listToSerialize);
    }

    /**
     * Render path array with label style and exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | label          | true    | path          | .blue.black.brown                   | .R=100.G=200.B=150                     |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @return
     */
    private String renderPathArrayLabelExplode(String paramName, List<Object> values) {
        return renderPathArrayLabel(paramName, values);
    }

    /**
     * Render path object with label style and exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | label          | true    | path          | .blue.black.brown                   | .R=100.G=200.B=150                     |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @return
     */
    private String renderPathObjectLabelExplode(String paramName, Map<String, Object> values) {
        String result = "";
        for (Map.Entry<String, Object> value : values.entrySet())
            result = result.concat("." + value.getKey() + "=" + encode(String.valueOf(value.getValue())));
        return result;
    }

    /**
     * Render path array with simple style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | simple         | false   | path, header  | blue,black,brown                    | R,100,G,200,B,150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @return
     */
    private String renderPathArraySimple(String paramName, List<Object> values) {
        return String.join(",", values.stream().map(object -> encode(String.valueOf(object))).collect(Collectors.toList()));
    }

    /**
     * Render path object with simple style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | simple         | false   | path, header  | blue,black,brown                    | R,100,G,200,B,150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @return
     */
    private String renderPathObjectSimple(String paramName, Map<String, Object> values) {
        List<String> listToSerialize = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            listToSerialize.add(entry.getKey());
            listToSerialize.add(encode(String.valueOf(entry.getValue())));
        }
        return String.join(",", listToSerialize);
    }

    /**
     * Render path array with simple style and exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | simple         | true    | path, header  | blue,black,brown                    | R=100,G=200,B=150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @return
     */
    private String renderPathArraySimpleExplode(String paramName, List<Object> values) {
        return renderPathArraySimple(paramName, values);
    }

    /**
     * Render path object with simple style and exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | simple         | true    | path, header  | blue,black,brown                    | R=100,G=200,B=150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @return
     */
    private String renderPathObjectSimpleExplode(String paramName, Map<String, Object> values) {
        return String.join(",",
          values.entrySet().stream().map((entry) -> entry.getKey() + "=" + encode(String.valueOf(entry.getValue()))).collect(Collectors.toList()));
    }

    /**
     * Add query array with form style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | form           | false   | query, cookie | color=blue,black,brown              | color=R,100,G,200,B,150                |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @param request
     */
    private void addQueryArrayForm(String paramName, List<Object> values, HttpRequest request) {
        String serialized = String.join(",", values.stream().map(String::valueOf).collect(Collectors.toList()));
        this.addQueryParam(paramName, serialized, request); // Encoding is done by WebClient
    }

    /**
     * Add query object with form style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | form           | false   | query, cookie | color=blue,black,brown              | color=R,100,G,200,B,150                |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @param request
     */
    private void addQueryObjectForm(String paramName, Map<String, Object> values, HttpRequest request) {
        List<String> listToSerialize = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            listToSerialize.add(entry.getKey());
            listToSerialize.add(String.valueOf(entry.getValue()));
        }
        String serialized = String.join(",", listToSerialize);
        this.addQueryParam(paramName, serialized, request); // Encoding is done by WebClient
    }

    /**
     * Add cookie array with form style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | form           | false   | query, cookie | color=blue,black,brown              | color=R,100,G,200,B,150                |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     */
    private void renderCookieArrayForm(String paramName, List<Object> values, MultiMap map) {
        String value = String.join(",", values.stream().map(String::valueOf).collect(Collectors.toList()));
        map.remove(paramName);
        map.add(paramName, value);
    }

    /**
     * Add a cookie array parameter in cookie cache
     *
     * @param paramName name of cookie parameter
     * @param values list of values of cookie parameter
     */
    public void addCookieArrayForm(String paramName, List<Object> values) {
        renderCookieArrayForm(paramName, values, cookieParams);
    }

    /**
     * Add cookie object with form style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | form           | false   | query, cookie | color=blue,black,brown              | color=R,100,G,200,B,150                |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     */
    private void renderCookieObjectForm(String paramName, Map<String, Object> values, MultiMap map) {
        List<String> listToSerialize = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            listToSerialize.add(entry.getKey());
            listToSerialize.add(String.valueOf(entry.getValue()));
        }
        String value = String.join(",", listToSerialize);
        map.remove(paramName);
        map.add(paramName, value);
    }

    /**
     * Add a cookie object parameter in cookie cache
     *
     * @param paramName name of cookie parameter
     * @param values map of values of cookie parameter
     */
    public void addCookieObjectForm(String paramName, Map<String, Object> values) {
        renderCookieObjectForm(paramName, values, cookieParams);
    }

    /**
     * Add query array with form style and exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | form           | true    | query, cookie | color=blue&color=black&color=brown  | R=100&G=200&B=150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @param request
     */
    private void addQueryArrayFormExplode(String paramName, List<Object> values, HttpRequest request) {
        for (Object value : values)
            this.addQueryParam(paramName, String.valueOf(value), request);
    }

    /**
     * Add query object with form style and exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | form           | true    | query, cookie | color=blue&color=black&color=brown  | R=100&G=200&B=150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @param request
     */
    private void addQueryObjectFormExplode(String paramName, Map<String, Object> values, HttpRequest request) {
        for (Map.Entry<String, Object> value : values.entrySet())
            this.addQueryParam(value.getKey(), String.valueOf(value.getValue()), request);
    }

    /**
     * Add cookie array with form style and exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | form           | true    | query, cookie | color=blue&color=black&color=brown  | R=100&G=200&B=150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     */
    private void renderCookieArrayFormExplode(String paramName, List<Object> values, MultiMap map) {
        map.remove(paramName);
        for (Object value : values)
            map.add(paramName, String.valueOf(value));
    }

    public void addCookieArrayFormExplode(String paramName, List<Object> values) {
        renderCookieArrayFormExplode(paramName, values, cookieParams);
    }

    /**
     * Add cookie object with form style and exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | form           | true    | query, cookie | color=blue&color=black&color=brown  | R=100&G=200&B=150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     */
    private void renderCookieObjectFormExplode(String paramName, Map<String, Object> values, MultiMap map) {
        for (Map.Entry<String, Object> value : values.entrySet()) {
            map.remove(value.getKey());
            map.add(value.getKey(), String.valueOf(value.getValue()));
        }
    }

    public void addCookieObjectFormExplode(String paramName, Map<String, Object> values) {
        renderCookieObjectFormExplode(paramName, values, cookieParams);
    }

    /**
     * Add header array with simple style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | simple         | false   | path, header  | blue,black,brown                    | R,100,G,200,B,150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param headerName
     * @param values
     * @param request
     */
    private void addHeaderArraySimple(String headerName, List<Object> values, HttpRequest request) {
        String serialized = String.join(",", values.stream().map(String::valueOf).collect(Collectors.toList()));
        this.addHeaderParam(headerName, serialized, request);
    }

    /**
     * Add header object with simple style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | simple         | false   | path, header  | blue,black,brown                    | R,100,G,200,B,150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param headerName
     * @param values
     * @param request
     */
    private void addHeaderObjectSimple(String headerName, Map<String, Object> values, HttpRequest request) {
        List<String> listToSerialize = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            listToSerialize.add(entry.getKey());
            listToSerialize.add(String.valueOf(entry.getValue()));
        }
        String serialized = String.join(",", listToSerialize);
        this.addHeaderParam(headerName, serialized, request);
    }

    /**
     * Add header array with simple style and exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | simple         | true    | path, header  | blue,black,brown                    | R=100,G=200,B=150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param headerName
     * @param values
     * @param request
     */
    private void addHeaderArraySimpleExplode(String headerName, List<Object> values, HttpRequest request) {
        this.addHeaderArraySimple(headerName, values, request);
    }

    /**
     * Add header object with simple style and exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | simple         | true    | path, header  | blue,black,brown                    | R=100,G=200,B=150                      |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param headerName
     * @param values
     * @param request
     */
    private void addHeaderObjectSimpleExplode(String headerName, Map<String, Object> values, HttpRequest request) {
        List<String> listToSerialize = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            listToSerialize.add(entry.getKey() + "=" + String.valueOf(entry.getValue()));
        }
        String serialized = String.join(",", listToSerialize);
        this.addHeaderParam(headerName, serialized, request);
    }

    /**
     * Add query array with spaceDelimited style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | spaceDelimited | false   | query         | blue%20black%20brown                | R%20100%20G%20200%20B%20150            |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @param request
     */
    private void addQueryArraySpaceDelimited(String paramName, List<Object> values, HttpRequest request) {
        String serialized = String.join(" ", values.stream().map(String::valueOf).collect(Collectors.toList()));
        this.addQueryParam(paramName, serialized, request); // Encoding is done by WebClient
    }

    /**
     * Add query object with spaceDelimited style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | spaceDelimited | false   | query         | blue%20black%20brown                | R%20100%20G%20200%20B%20150            |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @param request
     */
    private void addQueryObjectSpaceDelimited(String paramName, Map<String, Object> values, HttpRequest request) {
        List<String> listToSerialize = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            listToSerialize.add(entry.getKey());
            listToSerialize.add(String.valueOf(entry.getValue()));
        }
        String serialized = String.join(" ", listToSerialize);
        this.addQueryParam(paramName, serialized, request); // Encoding is done by WebClient
    }

    /**
     * Add query array with pipeDelimited style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | pipeDelimited  | false   | query         | blue|black|brown                    | R|100|G|200                            |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @param request
     */
    private void addQueryArrayPipeDelimited(String paramName, List<Object> values, HttpRequest request) {
        String serialized = String.join("|", values.stream().map(String::valueOf).collect(Collectors.toList()));
        this.addQueryParam(paramName, serialized, request); // Encoding is done by WebClient
    }

    /**
     * Add query object with pipeDelimited style and not exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | pipeDelimited  | false   | query         | blue|black|brown                    | R|100|G|200                            |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @param request
     */
    private void addQueryObjectPipeDelimited(String paramName, Map<String, Object> values, HttpRequest request) {
        List<String> listToSerialize = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            listToSerialize.add(entry.getKey());
            listToSerialize.add(String.valueOf(entry.getValue()));
        }
        String serialized = String.join("|", listToSerialize);
        this.addQueryParam(paramName, serialized, request); // Encoding is done by WebClient
    }

    /**
     * Add query object with deepObject style and exploded
     *
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     | deepObject     | true    | query         | n/a                                 | color[R]=100&color[G]=200&color[B]=150 |
     +----------------+---------+---------------+-------------------------------------+----------------------------------------+
     *
     * @param paramName
     * @param values
     * @param request
     */
    private void addQueryObjectDeepObjectExplode(String paramName, Map<String, Object> values, HttpRequest request) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            this.addQueryParam(paramName + "[" + entry.getKey() + "]", String.valueOf(entry.getValue()), request);
        }
    }


    private void renderAndAttachCookieHeader(HttpRequest request, MultiMap otherCookies) {
        if ((otherCookies == null || otherCookies.isEmpty()) && cookieParams.isEmpty())
            return;
        List<String> listToSerialize = new ArrayList<>();
        for (Map.Entry<String, String> e : cookieParams.entries()) {
            if (otherCookies!= null && !otherCookies.contains(e.getKey())) {
                try {
                    listToSerialize.add(URLEncoder.encode(e.getKey(), "UTF-8") + "=" + URLEncoder.encode(e.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e1) {
                }
            }
        }
        if (otherCookies != null) {
            for (Map.Entry<String, String> e : otherCookies.entries()) {
                try {
                    listToSerialize.add(URLEncoder.encode(e.getKey(), "UTF-8") + "=" + URLEncoder.encode(e.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e1) {
                }
            }
        }
        request.putHeader("Cookie", String.join("; ", listToSerialize));
    }

    // Other functions

    private String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Close the connection with server
     *
     */
    public void close() {
        client.close();
    }

}
