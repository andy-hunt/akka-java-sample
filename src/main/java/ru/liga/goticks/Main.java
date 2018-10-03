package ru.liga.goticks;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.concurrent.CompletionStage;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class Main {

    public static void main(String[] args) {
        Config config = ConfigFactory.load();
        ActorSystem system = ActorSystem.create("goticks");
        final Http http = new Http(system.systemImpl());
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        //#server-bootstrapping
        Long timeout = Long.parseLong(config.getString("akka.http.server.request-timeout").replaceAll("[^\\d]", ""));
        RestApi apiRoutes = new RestApi(system, timeout);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = apiRoutes.routes().flow(system, materializer);
        String host = config.getString("http.host");
        int port = config.getInt("http.port");
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost(host, port), materializer);
        LoggingAdapter log = Logging.getLogger(system, Main.class);
        binding.thenAccept(serverBinding -> log.info("RestApi bound to {}", serverBinding.localAddress()))
                .toCompletableFuture().exceptionally(e -> {
            log.error("Failed to bind to {}:{}!", host, port);
            return null;
        });
    }
}
