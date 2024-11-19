package io.vertx.ext.web.client.tests;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.net.JdkSSLEngineOptions;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.TrustOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.test.tls.Cert;
import io.vertx.test.tls.Trust;

import java.util.*;
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static io.vertx.core.Future.all;
import static io.vertx.core.Future.await;
import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.concurrent.TimeUnit.SECONDS;

public class VerticleUndeployTest {

  private static final Vertx vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true));

  final static Buffer b_12000 = Buffer.buffer();
  final static Buffer b_1024 = Buffer.buffer();

  static {
    for (int i = 0;i < 12_000;i++) {
      b_12000.appendByte((byte) 65);
    }
    for (int i = 0;i < 1_024;i++) {
      b_1024.appendByte((byte) 65);
    }
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    vertx.exceptionHandler(Throwable::printStackTrace);
    startServer();

    //setup scanner, 1 deploys verticles, 2 undeploys
    Timer timer = new Timer();
    TimerTask timerTask = null;
    Scanner scanner = new Scanner(System.in);
    while (!Thread.currentThread().isInterrupted()) {
      try {
        switch (scanner.nextInt()) {
          case 1:
            startAll();
            break;
          case 2:
            stopAll();
            break;
          case 3: {
            if(timerTask == null) {
              timerTask = new TimerTask() {
                @Override
                public void run() {
                  System.out.println("Test starting");
                  startAll();
                  try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1250, 5000));
                    stopAll();
                    Thread.sleep(5000);
                  } catch (Exception e) {
                    System.out.println("Test failed: " + e.getMessage());
                  }
                }
              };
              timer.schedule(timerTask, 0, 10_000);
            } else {
              System.out.println("Stopping test");
              timerTask.cancel();
              timerTask = null;
            }
          }
        }
      } catch (Exception e) {
        //ignore
      }
    }
  }

  public static void startAll() {
    for (int i = 0; i < 1000; i++) {
      vertx.deployVerticle(new TestHttpVerticle(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)).await();
    }
  }

  public static void stopAll() throws ExecutionException, InterruptedException {
    List<Future<Void>> futs = vertx.deploymentIDs().stream().map(vertx::undeploy).collect(Collectors.toList());
    all(futs).await();
  }

  private static void startServer() throws ExecutionException, InterruptedException {
    vertx.createHttpServer(createHttp2ServerOptions(8080, "0.0.0.0"))
      .requestHandler(req -> {
        req.body().onComplete(ar -> {
//          System.out.println("Request received "+req.connection() + " "+ar.result().length());
          req.response().end(b_1024);
        });
      })
      .listen()
      .toCompletionStage()
      .toCompletableFuture()
      .get();
  }

  private static class TestHttpVerticle extends AbstractVerticle {
    private HttpClient client;
    private boolean isStopped;

    @Override
    public void start() throws Exception {
      this.client = this.vertx.createHttpClient(createHttp2ClientOptions());
      this.context.runOnContext(v -> loopAwait());
    }

    private void loopAwait() {
      while (!this.isStopped) {
        try {
          HttpClientRequest req = await(this.client.request(HttpMethod.POST, 8080, "localhost", "/"));
          req.idleTimeout(10_000);
          await(req.end(b_12000));
          HttpClientResponse resp = await(req.response());
          Buffer body = await(resp.body());
          sleep();
        } catch (Throwable e) {
          String msg = e.getMessage() == null ? e.getClass().getName() : e.getMessage();
//          System.out.println("Failed to get response " + msg);
        }
      }
    }

    private void sleep() {
      Promise<Void> p = Promise.promise();
      this.vertx.setTimer(current().nextInt(400, 6000), x -> p.tryComplete());
      await(p.future());
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
      this.isStopped = true;
      this.client.close().onComplete(stopPromise);
    }
  }

  private static class TestWebClientVerticle extends AbstractVerticle {
    private WebClient client;
    private boolean isStopped;

    @Override
    public void start() throws Exception {
      this.client = WebClient.create(this.vertx, new WebClientOptions(createHttp2ClientOptions()).setShared(false));
      this.context.runOnContext(v -> loopAwait());
    }

    private void loop() {
      try {
        this.client.getAbs("https://localhost:8080").timeout(10_000).sendBuffer(b_12000).onComplete(ar -> {
          if (!ar.succeeded()) {
            System.out.println("Failed to get response " + ar.cause().getMessage());
          }
          this.vertx.setTimer(current().nextInt(400, 6000), x -> loop());
        });
      } catch (Exception e) {
        System.out.println("Failed to loop request: "+ e.getMessage());
      }
    }

    private void loopAwait() {
      while (!this.isStopped) {
        try {
          await(this.client.postAbs("https://localhost:8080").timeout(10_000).sendBuffer(b_12000));
          sleep();
        } catch (Exception e) {
          String msg = e.getMessage() == null ? e.getClass().getName() : e.getMessage();
//          System.out.println("Failed to get response " + msg);
        }
      }
    }

    private void sleep() {
      Promise<Void> p = Promise.promise();
      this.vertx.setTimer(current().nextInt(400, 6000), x -> p.tryComplete());
      await(p.future());
    }

    @Override
    public void stop() throws Exception {
      this.isStopped = true;
      Optional.of(this.client).ifPresent(WebClient::close);
//      System.out.println("Verticle stopped");
    }
  }

  public static HttpServerOptions createHttp2ServerOptions(int port, String host) {
    return (new HttpServerOptions()).setPort(port).setHost(host).setSslEngineOptions(new JdkSSLEngineOptions()).setUseAlpn(true).setSsl(true).addEnabledCipherSuite("TLS_RSA_WITH_AES_128_CBC_SHA").setKeyCertOptions((KeyCertOptions) Cert.SERVER_JKS.get());
  }

  public static HttpClientOptions createHttp2ClientOptions() {
    return (new HttpClientOptions()).setSslEngineOptions(new JdkSSLEngineOptions()).setUseAlpn(true).setSsl(true).setTrustOptions((TrustOptions) Trust.SERVER_JKS.get()).setProtocolVersion(HttpVersion.HTTP_2);
  }
}
