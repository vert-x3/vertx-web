/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.templ;

import com.mitchellbosecke.pebble.PebbleEngine;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.extension.TestExtension;
import io.vertx.ext.web.templ.impl.PebbleVertxLoader;
import org.junit.Test;

/**
 * @author Dan Kristensen
 */
public class PebbleTemplateTest extends WebTestBase {

	@Test
	public void testTemplateHandlerOnClasspath() throws Exception {
		final TemplateEngine engine = PebbleTemplateEngine.create(vertx);
		testTemplateHandler(engine, "somedir", "test-pebble-template2.peb",
		        "Hello badger and foxRequest path is /test-pebble-template2.peb");
	}

	@Test
	public void testTemplateHandlerOnFileSystem() throws Exception {
		final TemplateEngine engine = PebbleTemplateEngine.create(vertx);
		testTemplateHandler(engine, "src/test/filesystemtemplates", "test-pebble-template3.peb",
		        "Hello badger and foxRequest path is /test-pebble-template3.peb");
	}

	@Test
	public void testTemplateHandlerNoExtension() throws Exception {
		final TemplateEngine engine = PebbleTemplateEngine.create(vertx);
		testTemplateHandler(engine, "somedir", "test-pebble-template2", "Hello badger and foxRequest path is /test-pebble-template2");
	}

	@Test
	public void testTemplateHandlerChangeExtension() throws Exception {
		final TemplateEngine engine = PebbleTemplateEngine.create(vertx).setExtension("beb");
		testTemplateHandler(engine, "somedir", "test-pebble-template2", "Cheerio badger and foxRequest path is /test-pebble-template2");
	}

	private void testTemplateHandler(TemplateEngine engine, String directoryName, String templateName, String expected) throws Exception {
		router.route().handler(context -> {
			context.put("foo", "badger");
			context.put("bar", "fox");
			context.next();
		});
		router.route().handler(TemplateHandler.create(engine, directoryName, "text/plain"));
		testRequest(HttpMethod.GET, "/" + templateName, 200, "OK", expected);
	}

	@Test
	public void testNoSuchTemplate() throws Exception {
		final TemplateEngine engine = PebbleTemplateEngine.create(vertx);
		router.route().handler(TemplateHandler.create(engine, "nosuchtemplate.peb", "text/plain"));
		testRequest(HttpMethod.GET, "/foo.peb", 500, "Internal Server Error");
	}

	@Test
	public void testTemplateComplex() throws Exception {

		String expected = "Hello.Hi fox.\nHi badger!Footer - badger";

		final TemplateEngine engine = PebbleTemplateEngine.create(vertx);
		testTemplateHandler(engine, "src/test/filesystemtemplates", "test-pebble-complex.peb", expected);
	}

	@Test
	public void testTemplateBigAndComplex() throws Exception{
		final TemplateEngine engine = PebbleTemplateEngine.create(vertx);
		testTemplateHandler(engine, "src/test/filesystemtemplates", "test-pebble-template4.peb", bigTemplateExpected);
	}

    private String bigTemplateExpected = "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"utf-8\">\n" +
            "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
            "    <title> Home </title>\n" +
            "    <meta content=\"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no\" name=\"viewport\">\n" +
            "    <link rel=\"stylesheet\"\n" +
            "          href=\"http://cdn.jsdelivr.net/webjars/org.webjars.bower/adminlte/2.3.8/bootstrap/css/bootstrap.min.css\">\n" +
            "    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css\">\n" +
            "    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/ionicons/2.0.1/css/ionicons.min.css\">\n" +
            "    <link rel=\"stylesheet\"\n" +
            "          href=\"http://cdn.jsdelivr.net/webjars/org.webjars.bower/adminlte/2.3.8/plugins/jvectormap/jquery-jvectormap-1.2.2.css\">\n" +
            "    <link rel=\"stylesheet\"\n" +
            "          href=\"http://cdn.jsdelivr.net/webjars/org.webjars.bower/adminlte/2.3.8/dist/css/AdminLTE.min.css\">\n" +
            "    <link rel=\"stylesheet\"\n" +
            "          href=\"http://cdn.jsdelivr.net/webjars/org.webjars.bower/adminlte/2.3.8/dist/css/skins/_all-skins.min.css\">\n" +
            "    <script src=\"https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js\"></script>\n" +
            "    <script src=\"https://oss.maxcdn.com/respond/1.4.2/respond.min.js\"></script>\n" +
            "</head>\n" +
            "<body class=\"hold-transition skin-blue sidebar-mini\">\n" +
            "<div id=\"content\">\n" +
            "    <div class=\"wrapper\">\n" +
            "            <header class=\"main-header\">\n" +
            "        <a href=\"\" class=\"logo\">\n" +
            "            <span class=\"logo-mini\"><b>A</b>LT</span>\n" +
            "            <span class=\"logo-lg\"><b>Admin</b>LTE</span>\n" +
            "        </a>\n" +
            "        <nav class=\"navbar navbar-static-top\">\n" +
            "            <a href=\"#\" class=\"sidebar-toggle\" data-toggle=\"offcanvas\" role=\"button\">\n" +
            "                <span class=\"sr-only\">Toggle navigation</span>\n" +
            "                <span class=\"icon-bar\"></span>\n" +
            "                <span class=\"icon-bar\"></span>\n" +
            "                <span class=\"icon-bar\"></span>\n" +
            "            </a>\n" +
            "\n" +
            "            <div class=\"navbar-custom-menu\">\n" +
            "                <ul class=\"nav navbar-nav\">\n" +
            "                    <li class=\"dropdown messages-menu\">\n" +
            "                        <a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\">\n" +
            "                            <i class=\"fa fa-envelope-o\"></i>\n" +
            "                            <span class=\"label label-success\">4</span>\n" +
            "                        </a>\n" +
            "                        <ul class=\"dropdown-menu\">\n" +
            "                            <li class=\"header\">You have 4 messages</li>\n" +
            "                            <li>\n" +
            "                                <ul class=\"menu\">\n" +
            "                                    <a href=\"#\">\n" +
            "                                        <div class=\"pull-left\">\n" +
            "                                            <img src=\"\" class=\"img-circle\" alt=\"User Image\">\n" +
            "                                        </div>\n" +
            "                                        <h4>\n" +
            "                                            Support Team\n" +
            "                                            <small><i class=\"fa fa-clock-o\"></i> 5 mins</small>\n" +
            "                                        </h4>\n" +
            "                                        <p>Why not buy a new awesome theme?</p>\n" +
            "                                    </a>\n" +
            "                                    </li>\n" +
            "                                    <li>\n" +
            "                                        <a href=\"#\">\n" +
            "                                            <div class=\"pull-left\">\n" +
            "                                                <img src=\"\" class=\"img-circle\" alt=\"User Image\">\n" +
            "                                            </div>\n" +
            "                                            <h4>\n" +
            "                                                AdminLTE Design Team\n" +
            "                                                <small><i class=\"fa fa-clock-o\"></i> 2 hours</small>\n" +
            "                                            </h4>\n" +
            "                                            <p>Why not buy a new awesome theme?</p>\n" +
            "                                        </a>\n" +
            "                                    </li>\n" +
            "                                    <li>\n" +
            "                                        <a href=\"#\">\n" +
            "                                            <div class=\"pull-left\">\n" +
            "                                                <img src=\"\" class=\"img-circle\" alt=\"User Image\">\n" +
            "                                            </div>\n" +
            "                                            <h4>\n" +
            "                                                Developers\n" +
            "                                                <small><i class=\"fa fa-clock-o\"></i> Today</small>\n" +
            "                                            </h4>\n" +
            "                                            <p>Why not buy a new awesome theme?</p>\n" +
            "                                        </a>\n" +
            "                                    </li>\n" +
            "                                    <li>\n" +
            "                                        <a href=\"#\">\n" +
            "                                            <div class=\"pull-left\">\n" +
            "                                                <img src=\"\" class=\"img-circle\" alt=\"User Image\">\n" +
            "                                            </div>\n" +
            "                                            <h4>\n" +
            "                                                Sales Department\n" +
            "                                                <small><i class=\"fa fa-clock-o\"></i> Yesterday</small>\n" +
            "                                            </h4>\n" +
            "                                            <p>Why not buy a new awesome theme?</p>\n" +
            "                                        </a>\n" +
            "                                    </li>\n" +
            "                                    <li>\n" +
            "                                        <a href=\"#\">\n" +
            "                                            <div class=\"pull-left\">\n" +
            "                                                <img src=\"\" class=\"img-circle\" alt=\"User Image\">\n" +
            "                                            </div>\n" +
            "                                            <h4>\n" +
            "                                                Reviewers\n" +
            "                                                <small><i class=\"fa fa-clock-o\"></i> 2 days</small>\n" +
            "                                            </h4>\n" +
            "                                            <p>Why not buy a new awesome theme?</p>\n" +
            "                                        </a>\n" +
            "                                    </li>\n" +
            "                                </ul>\n" +
            "                            </li>\n" +
            "                            <li class=\"footer\"><a href=\"#\">See All Messages</a></li>\n" +
            "                        </ul>\n" +
            "                    </li>\n" +
            "                    <li class=\"dropdown notifications-menu\">\n" +
            "                        <a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\">\n" +
            "                            <i class=\"fa fa-bell-o\"></i>\n" +
            "                            <span class=\"label label-warning\">10</span>\n" +
            "                        </a>\n" +
            "                        <ul class=\"dropdown-menu\">\n" +
            "                            <li class=\"header\">You have 10 notifications</li>\n" +
            "                            <li>\n" +
            "                                <ul class=\"menu\">\n" +
            "                                    <li>\n" +
            "                                        <a href=\"#\">\n" +
            "                                            <i class=\"fa fa-users text-aqua\"></i> 5 new members joined today\n" +
            "                                        </a>\n" +
            "                                    </li>\n" +
            "                                    <li>\n" +
            "                                        <a href=\"#\">\n" +
            "                                            <i class=\"fa fa-warning text-yellow\"></i> Very long description here that\n" +
            "                                            may not fit into the\n" +
            "                                            page and may cause design problems\n" +
            "                                        </a>\n" +
            "                                    </li>\n" +
            "                                    <li>\n" +
            "                                        <a href=\"#\">\n" +
            "                                            <i class=\"fa fa-users text-red\"></i> 5 new members joined\n" +
            "                                        </a>\n" +
            "                                    </li>\n" +
            "\n" +
            "                                    <li>\n" +
            "                                        <a href=\"#\">\n" +
            "                                            <i class=\"fa fa-shopping-cart text-green\"></i> 25 sales made\n" +
            "                                        </a>\n" +
            "                                    </li>\n" +
            "                                    <li>\n" +
            "                                        <a href=\"#\">\n" +
            "                                            <i class=\"fa fa-user text-red\"></i> You changed your username\n" +
            "                                        </a>\n" +
            "                                    </li>\n" +
            "                                </ul>\n" +
            "                            </li>\n" +
            "                            <li class=\"footer\"><a href=\"#\">View all</a></li>\n" +
            "                        </ul>\n" +
            "                    </li>\n" +
            "                    <li class=\"dropdown tasks-menu\">\n" +
            "                        <a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\">\n" +
            "                            <i class=\"fa fa-flag-o\"></i>\n" +
            "                            <span class=\"label label-danger\">9</span>\n" +
            "                        </a>\n" +
            "                        <ul class=\"dropdown-menu\">\n" +
            "                            <li class=\"header\">You have 9 tasks</li>\n" +
            "                            <li>\n" +
            "                                <ul class=\"menu\">\n" +
            "                                    <a href=\"#\">\n" +
            "                                        <h3>\n" +
            "                                            Design some buttons\n" +
            "                                            <small class=\"pull-right\">20%</small>\n" +
            "                                        </h3>\n" +
            "                                        <div class=\"progress xs\">\n" +
            "                                            <div class=\"progress-bar progress-bar-aqua\" style=\"width: 20%\"\n" +
            "                                                 role=\"progressbar\" aria-valuenow=\"20\" aria-valuemin=\"0\"\n" +
            "                                                 aria-valuemax=\"100\">\n" +
            "                                                <span class=\"sr-only\">20% Complete</span>\n" +
            "                                            </div>\n" +
            "                                        </div>\n" +
            "                                    </a>\n" +
            "                                    </li>\n" +
            "                                    <a href=\"#\">\n" +
            "                                        <h3>\n" +
            "                                            Create a nice theme\n" +
            "                                            <small class=\"pull-right\">40%</small>\n" +
            "                                        </h3>\n" +
            "                                        <div class=\"progress xs\">\n" +
            "                                            <div class=\"progress-bar progress-bar-green\" style=\"width: 40%\"\n" +
            "                                                 role=\"progressbar\" aria-valuenow=\"20\" aria-valuemin=\"0\"\n" +
            "                                                 aria-valuemax=\"100\">\n" +
            "                                                <span class=\"sr-only\">40% Complete</span>\n" +
            "                                            </div>\n" +
            "                                        </div>\n" +
            "                                    </a>\n" +
            "                                    </li>\n" +
            "                                    <a href=\"#\">\n" +
            "                                        <h3>\n" +
            "                                            Some task I need to do\n" +
            "                                            <small class=\"pull-right\">60%</small>\n" +
            "                                        </h3>\n" +
            "                                        <div class=\"progress xs\">\n" +
            "                                            <div class=\"progress-bar progress-bar-red\" style=\"width: 60%\"\n" +
            "                                                 role=\"progressbar\" aria-valuenow=\"20\" aria-valuemin=\"0\"\n" +
            "                                                 aria-valuemax=\"100\">\n" +
            "                                                <span class=\"sr-only\">60% Complete</span>\n" +
            "                                            </div>\n" +
            "                                        </div>\n" +
            "                                    </a>\n" +
            "                                    </li>\n" +
            "                                    <a href=\"#\">\n" +
            "                                        <h3>\n" +
            "                                            Make beautiful transitions\n" +
            "                                            <small class=\"pull-right\">80%</small>\n" +
            "                                        </h3>\n" +
            "                                        <div class=\"progress xs\">\n" +
            "                                            <div class=\"progress-bar progress-bar-yellow\" style=\"width: 80%\"\n" +
            "                                                 role=\"progressbar\" aria-valuenow=\"20\" aria-valuemin=\"0\"\n" +
            "                                                 aria-valuemax=\"100\">\n" +
            "                                                <span class=\"sr-only\">80% Complete</span>\n" +
            "                                            </div>\n" +
            "                                        </div>\n" +
            "                                    </a>\n" +
            "                                    </li>\n" +
            "                                </ul>\n" +
            "                            </li>\n" +
            "                            <li class=\"footer\">\n" +
            "                                <a href=\"#\">View all tasks</a>\n" +
            "                            </li>\n" +
            "                        </ul>\n" +
            "                    </li>\n" +
            "                </ul>\n" +
            "            </div>\n" +
            "        </nav>\n" +
            "    </header>\n" +
            "    </div>\n" +
            "</div>\n" +
            "<div id=\"footer\">\n" +
            "            Copyright 2014\n" +
            "    </div>\n" +
            "\n" +
            "<script src=\"http://cdn.jsdelivr.net/webjars/org.webjars.bower/adminlte/2.3.8/plugins/jQuery/jquery-2.2.3.min.js\"></script>\n" +
            "<script src=\"http://cdn.jsdelivr.net/webjars/org.webjars.bower/adminlte/2.3.8/bootstrap/js/bootstrap.min.js\"></script>\n" +
            "<script src=\"http://cdn.jsdelivr.net/webjars/org.webjars.bower/adminlte/2.3.8/plugins/fastclick/fastclick.js\"></script>\n" +
            "<script src=\"http://cdn.jsdelivr.net/webjars/org.webjars.bower/adminlte/2.3.8/dist/js/app.min.js\"></script>\n" +
            "<script src=\"http://cdn.jsdelivr.net/webjars/org.webjars.bower/adminlte/2.3.8/plugins/sparkline/jquery.sparkline.min.js\"></script>\n" +
            "<script src=\"http://cdn.jsdelivr.net/webjars/org.webjars.bower/adminlte/2.3.8/plugins/slimScroll/jquery.slimscroll.min.js\"></script>\n" +
            "\n" +
            "</body>\n" +
            "</html>";

  @Test
  public void customBuilderShouldRender() throws Exception {
    final TemplateEngine engine = PebbleTemplateEngine.create(new PebbleEngine.Builder().extension(new TestExtension()).loader(new PebbleVertxLoader(vertx)));
    testTemplateHandler(engine, "src/test/filesystemtemplates", "test-pebble-template5.peb","Hello badger and foxString is TESTRequest path is /test-pebble-template5.peb");
  }
}
