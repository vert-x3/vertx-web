package io.vertx.ext.web.it;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.webauthn.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.WebAuthnHandler;
import io.vertx.ext.web.impl.RoutingContextInternal;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(VertxUnitRunner.class)
public class FIDO2TCK {

  static {
    System.setProperty("io.vertx.ext.web.fido2.conformance.test", "true");
    System.setProperty("vertxweb.environment", "dev");
  }

  private static final String ORIGIN = System.getProperty("ORIGIN", "http://192.168.178.183:8080");

  private static final String[] MDS3 = {
    "https://mds3.certinfra.fidoalliance.org/execute/12eaf6d06bc2f6c1bf169e837ca01cd7bc6c0c257bf060d9d3f4f6683601b44b",
    "https://mds3.certinfra.fidoalliance.org/execute/588ca39f3f089fd07793fefae80ccebe9070ff00b9b77e9083b0ba0e12e17295",
    "https://mds3.certinfra.fidoalliance.org/execute/91b7c26cebf103c2e502a36bad0193ea52753439a1c12d75b16775f22c1cea68",
    "https://mds3.certinfra.fidoalliance.org/execute/ab47a97c090c7cbf515d6e95dcf39bcf021146cad3445bc0c764b9e32490ac59",
    "https://mds3.certinfra.fidoalliance.org/execute/b2bc6a0c7ee5c054c4aa7fd4fa04b5089b5295859b7b373c34fe375e6d540912"
  };

  private static final String[] MDS2 = {
    "https://mds.certinfra.fidoalliance.org/execute/333b32fad3d882a9d2163faa14cfc82ac42ddd4cbf4032052a526daae2334fa0",
    "https://mds.certinfra.fidoalliance.org/execute/61984215c66217158500e6ccb341bf18f98facb5ea46f01918d02eefaafcef0a",
    "https://mds.certinfra.fidoalliance.org/execute/c7a1295798026b93b9c0b3139e984f206dc6984d0d58bd452c5844cf4606d960",
    "https://mds.certinfra.fidoalliance.org/execute/d22387051125832d8365cbf1521b6c79df2390859a004524aef5ce42b1d58707",
    "https://mds.certinfra.fidoalliance.org/execute/f04a888fccf28a71f458db8e820523e24156333b2210e16aa49ad55c640ad491"
  };

  private static final String MDS3_ROOT_CERTIFICATE =
    "MIICaDCCAe6gAwIBAgIPBCqih0DiJLW7+UHXx/o1MAoGCCqGSM49BAMDMGcxCzAJ" +
      "BgNVBAYTAlVTMRYwFAYDVQQKDA1GSURPIEFsbGlhbmNlMScwJQYDVQQLDB5GQUtF" +
      "IE1ldGFkYXRhIDMgQkxPQiBST09UIEZBS0UxFzAVBgNVBAMMDkZBS0UgUm9vdCBG" +
      "QUtFMB4XDTE3MDIwMTAwMDAwMFoXDTQ1MDEzMTIzNTk1OVowZzELMAkGA1UEBhMC" +
      "VVMxFjAUBgNVBAoMDUZJRE8gQWxsaWFuY2UxJzAlBgNVBAsMHkZBS0UgTWV0YWRh" +
      "dGEgMyBCTE9CIFJPT1QgRkFLRTEXMBUGA1UEAwwORkFLRSBSb290IEZBS0UwdjAQ" +
      "BgcqhkjOPQIBBgUrgQQAIgNiAASKYiz3YltC6+lmxhPKwA1WFZlIqnX8yL5RybSL" +
      "TKFAPEQeTD9O6mOz+tg8wcSdnVxHzwnXiQKJwhrav70rKc2ierQi/4QUrdsPes8T" +
      "EirZOkCVJurpDFbXZOgs++pa4XmjYDBeMAsGA1UdDwQEAwIBBjAPBgNVHRMBAf8E" +
      "BTADAQH/MB0GA1UdDgQWBBQGcfeCs0Y8D+lh6U5B2xSrR74eHTAfBgNVHSMEGDAW" +
      "gBQGcfeCs0Y8D+lh6U5B2xSrR74eHTAKBggqhkjOPQQDAwNoADBlAjEA/xFsgri0" +
      "xubSa3y3v5ormpPqCwfqn9s0MLBAtzCIgxQ/zkzPKctkiwoPtDzI51KnAjAmeMyg" +
      "X2S5Ht8+e+EQnezLJBJXtnkRWY+Zt491wgt/AwSs5PHHMv5QgjELOuMxQBc=";

  private static final String MDS2_ROOT_CERTIFICATE =
    "MIICZzCCAe6gAwIBAgIPBF0rd3WL/GExWV/szYNVMAoGCCqGSM49BAMDMGcxCzAJ" +
      "BgNVBAYTAlVTMRYwFAYDVQQKDA1GSURPIEFsbGlhbmNlMScwJQYDVQQLDB5GQUtF" +
      "IE1ldGFkYXRhIFRPQyBTaWduaW5nIEZBS0UxFzAVBgNVBAMMDkZBS0UgUm9vdCBG" +
      "QUtFMB4XDTE3MDIwMTAwMDAwMFoXDTQ1MDEzMTIzNTk1OVowZzELMAkGA1UEBhMC" +
      "VVMxFjAUBgNVBAoMDUZJRE8gQWxsaWFuY2UxJzAlBgNVBAsMHkZBS0UgTWV0YWRh" +
      "dGEgVE9DIFNpZ25pbmcgRkFLRTEXMBUGA1UEAwwORkFLRSBSb290IEZBS0UwdjAQ" +
      "BgcqhkjOPQIBBgUrgQQAIgNiAARcVLd6r4fnNHzs5K2zfbg//4X9/oBqmsdRVtZ9" +
      "iXhlgM9vFYaKviYtqmwkq0D3Lihg3qefeZgXXYi4dFgvzU7ZLBapSNM3CT8RDBe/" +
      "MBJqsPwaRQbIsGmmItmt/ESNQD6jYDBeMAsGA1UdDwQEAwIBBjAPBgNVHRMBAf8E" +
      "BTADAQH/MB0GA1UdDgQWBBTd95rIHO/hX9Oh69szXzD0ahmZWTAfBgNVHSMEGDAW" +
      "gBTd95rIHO/hX9Oh69szXzD0ahmZWTAKBggqhkjOPQQDAwNnADBkAjBkP3L99KEX" +
      "QzviJVGytDMWBmITMBYv1LgNXXiSilWixTyQqHrYrFpLvNFyPZQvS6sCMFMAOUCw" +
      "Ach/515XH0XlDbMgdIe2N4zzdY77TVwiHmsxTFWRT0FtS7fUk85c/LzSPQ==";

  // https://mds3.certinfra.fidoalliance.org/crl/MDSCA-1.crl
  private static final String MDS3_MDSCA_1_CRL =
    "MIIB7DCCAZICAQEwCgYIKoZIzj0EAwIwbzELMAkGA1UEBhMCVVMxFjAUBgNVBAoM" +
      "DUZJRE8gQWxsaWFuY2UxLzAtBgNVBAsMJkZBS0UgTWV0YWRhdGEgMyBCTE9CIElO" +
      "VEVSTUVESUFURSBGQUtFMRcwFQYDVQQDDA5GQUtFIENBLTEgRkFLRRcNMTgwMjAx" +
      "MDAwMDAwWhcNMjIwMjAxMDAwMDAwWjCBwDAuAg8ELS9CzLtxNJTOFTHXiV8XDTE2" +
      "MDQxMzAwMDAwMFowDDAKBgNVHRUEAwoBADAuAg8ExejzukpclaXnFLGvxDEXDTE3" +
      "MDMyNTAwMDAwMFowDDAKBgNVHRUEAwoBADAuAg8Er13ouX8KNf3VOr4OzQEXDTE2" +
      "MDMwMTAwMDAwMFowDDAKBgNVHRUEAwoBADAuAg8EgGdJ3jB7vVF1om1z9fMXDTE4" +
      "MDMyNTAwMDAwMFowDDAKBgNVHRUEAwoBAKAvMC0wCgYDVR0UBAMCAQEwHwYDVR0j" +
      "BBgwFoAUo4SnpGSiiTwKvxeeog3wEhqm18swCgYIKoZIzj0EAwIDSAAwRQIgDgts" +
      "hLf5/82mHcOgl2TsUizHsjLCslmQVDdSPcolS8UCIQDa5MSjQbX1v8MkCPpzxbrB" +
      "b1I510aSTuZB0RUuwPnOYw==";

  // https://mds3.certinfra.fidoalliance.org/crl/MDSROOT.crl (expired)
  private static final String MDS3_MDSROOT_CRL =
    "MIIB1zCCAV0CAQEwCgYIKoZIzj0EAwMwZzELMAkGA1UEBhMCVVMxFjAUBgNVBAoM" +
      "DUZJRE8gQWxsaWFuY2UxJzAlBgNVBAsMHkZBS0UgTWV0YWRhdGEgMyBCTE9CIFJP" +
      "T1QgRkFLRTEXMBUGA1UEAwwORkFLRSBSb290IEZBS0UXDTE4MDIwMTAwMDAwMFoX" +
      "DTIwMDIwMTAwMDAwMFowgZMwLwIQBCZYfWbvAtCiCiDkzlVBNhcNMTQwMzAxMDAw" +
      "MDAwWjAMMAoGA1UdFQQDCgEAMC8CEDB1uuLr+thg94Dk2tjuTl0XDTE0MDQxMzAw" +
      "MDAwMFowDDAKBgNVHRUEAwoBADAvAhBZqgqFaRNC66FNnJfk9beCFw0xNTAzMjUw" +
      "MDAwMDBaMAwwCgYDVR0VBAMKAQCgLzAtMAoGA1UdFAQDAgEBMB8GA1UdIwQYMBaA" +
      "FAZx94KzRjwP6WHpTkHbFKtHvh4dMAoGCCqGSM49BAMDA2gAMGUCMHK+tW8lZLZ3" +
      "qMxwVcOEgZiX3oEaM7WteieaoKg+R54cCzsgS/GmgIfn/dzWDKYhmgIxAKr6/S8a" +
      "sTsF9yoon6hvzh+qzseltavkr7iu7BVs2dMG6WStr9P9gRvhk2UCxbk6Wg==";

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  AuthenticatorStore database;

  @Before
  public void init() {
    database = new AuthenticatorStore(rule.vertx().getOrCreateContext());
  }

  @Test(timeout = 300_000)
  @Ignore("This test needs to be executed manually against the Conformance Tools")
  public void testServerForFIDO2TCK(TestContext should) {
    final Async test = should.async();
    final Vertx vertx = rule.vertx();

    final FileSystem fs = vertx.fileSystem();

    // clear the DB at each run to avoid previous state
    database.clear();


    final WebAuthnOptions config = new WebAuthnOptions()
      .setRelyingParty(new RelyingParty().setName("Vert.x Conformance Test 4.2.0"))
      .putRootCertificate("mds", MDS3_ROOT_CERTIFICATE)
//      .addRootCrl(MDS3_MDSROOT_CRL)
      .addRootCrl(MDS3_MDSCA_1_CRL);

    // create the webauthn security object
    WebAuthn webAuthN = WebAuthn.create(
        vertx, config)
      // where to load/update authenticators data
      .authenticatorFetcher(database::fetcher)
      .authenticatorUpdater(database::updater);

    // do we want to load custom metadata statements?
    if (fs.existsBlocking("metadataStatements")) {
      for (String f : fs.readDirBlocking("metadataStatements")) {
        System.out.println("Loading metadata statement: " + f);
        webAuthN.metaDataService()
          .addStatement(new JsonObject(fs.readFileBlocking(f)));
      }
    } else {
      should.fail("test metadata statements are not in the filesystem!");
      return;
    }

    List<Future> futures = new ArrayList<>();

    // do we want to load custom MDS3 servers?
    for (String el : MDS3) {
      System.out.println("Loading toc: " + el);
      futures.add(webAuthN.metaDataService().fetchTOC(el));
    }

    CompositeFuture.all(futures)
      .onFailure(should::fail)
      .onSuccess(done -> {
        final Router app = Router.router(vertx);

        // parse the BODY
        app.post()
          .handler(BodyHandler.create());
        // add a session handler
        app.route()
          .handler(SessionHandler
            .create(LocalSessionStore.create(vertx)));

        app.post("/attestation/options")
          .handler(ctx -> {
            JsonObject json = ctx.body().asJsonObject();
            // vert.x doesn't work with "username" but "name"
            if (json.containsKey("username")) {
              String username = json.getString("username");
              json.remove("username");
              json.put("name", username);
              // patch the request
              ((RoutingContextInternal) ctx).setBody(json.toBuffer());
            }

            // register, we need to listen to the request and change the config
            JsonObject authenticatorSelection = json.getJsonObject("authenticatorSelection");
            if (authenticatorSelection != null) {
              if (authenticatorSelection.containsKey("requireResidentKey")) {
                config.setRequireResidentKey(authenticatorSelection.getBoolean("requireResidentKey"));
              }
              if (authenticatorSelection.containsKey("authenticatorAttachment")) {
                config.setAuthenticatorAttachment(AuthenticatorAttachment.of(authenticatorSelection.getString("authenticatorAttachment")));
              }
              if (authenticatorSelection.containsKey("userVerification")) {
                config.setUserVerification(UserVerification.of(authenticatorSelection.getString("userVerification")));
              }
            }
            config.setAttestation(Attestation.of(json.getString("attestation")));
            config.setExtensions(json.getJsonObject("extensions"));

            ctx.next();
          });

        app.post("/attestation/result")
          .handler(ctx -> {
            ctx.reroute("/callback");
          });

        app.post("/assertion/options")
          .handler(ctx -> {
            JsonObject json = ctx.body().asJsonObject();
            // vert.x doesn't work with "username" but "name"
            if (json.containsKey("username")) {
              String username = json.getString("username");
              json.remove("username");
              json.put("name", username);
              // patch the request
              ((RoutingContextInternal) ctx).setBody(json.toBuffer());
            }

            config.setUserVerification(UserVerification.of(json.getString("userVerification", "discouraged")));
            config.setExtensions(json.getJsonObject("extensions"));

            ctx.next();
          });

        app.post("/assertion/result")
          .handler(ctx -> {
            ctx.reroute("/callback");
          });


        // security handler
        WebAuthnHandler webAuthnHandler = WebAuthnHandler.create(webAuthN)
          .setOrigin(ORIGIN)
          .setupCallback(app.post("/callback"))
          .setupCredentialsCreateCallback(app.post("/attestation/options"))
          .setupCredentialsGetCallback(app.post("/assertion/options"));

        // secure the remaining routes
        app.route()
          .handler(webAuthnHandler);

        // failure handler to comply to conformance test requirements
        app.route()
          .failureHandler(ctx -> {
            ctx.failure().printStackTrace();

            ctx.response().setStatusCode(500);
            ctx
              .json(new JsonObject().put("status", "failed").put("errorMessage", ctx.failure().getMessage()));
          });

        vertx.createHttpServer()
          .requestHandler(app)
          .listen(8080, "0.0.0.0")
          .onFailure(should::fail)
          .onSuccess(v -> {
            System.out.printf("Server listening at: %s%n", ORIGIN);

            System.out.println("-----------------------------------");
            System.out.println("Run the FIDO2 Conformance tool now!");
            System.out.println("-----------------------------------");
          });
      });
  }
}

class AuthenticatorStore {

  final ContextInternal ctx;

  public AuthenticatorStore(Context ctx) {
    this.ctx = (ContextInternal) ctx;
  }

  /**
   * This is a dummy database, just for demo purposes.
   * In a real world scenario you should be using something like:
   *
   * <ul>
   *   <li>Postgres</li>
   *   <li>MySQL</li>
   *   <li>Mongo</li>
   *   <li>Redis</li>
   *   <li>...</li>
   * </ul>
   */
  private final List<Authenticator> database = new ArrayList<>();

  public Future<List<Authenticator>> fetcher(Authenticator query) {
    return ctx.succeededFuture(
      database.stream()
        .filter(entry -> {
          if (query.getUserName() != null) {
            return query.getUserName().equals(entry.getUserName());
          }
          if (query.getCredID() != null) {
            return query.getCredID().equals(entry.getCredID());
          }
          // This is a bad query! both username and credID are null
          return false;
        })
        .collect(Collectors.toList())
    );
  }

  public Future<Void> updater(Authenticator authenticator) {

    long updated = database.stream()
      .filter(entry -> authenticator.getCredID().equals(entry.getCredID()))
      .peek(entry -> {
        // update existing counter
        entry.setCounter(authenticator.getCounter());
      }).count();

    if (updated > 0) {
      return ctx.succeededFuture();
    } else {
      database.add(authenticator);
      return ctx.succeededFuture();
    }
  }

  public void clear() {
    database.clear();
  }
}
