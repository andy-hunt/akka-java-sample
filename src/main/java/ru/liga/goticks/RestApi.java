package ru.liga.goticks;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.pattern.PatternsCS;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class RestApi extends AllDirectives {
    final private LoggingAdapter log;
    final private ActorRef boxOffice;
    final private Duration timeout;

    public RestApi(ActorSystem system, Long timout) {
        log = Logging.getLogger(system, this);
        timeout = Duration.ofSeconds(timout);
        boxOffice = system.actorOf(BoxOffice.props(timeout), BoxOffice.NAME);
    }

    public Route routes() {
        return
            route(
                pathPrefix("events", () ->
                    pathEndOrSingleSlash(() ->
                        get(() -> {
                            // GET /events
                            CompletionStage<List<BoxOffice.Events>> stage = getEvents();
                            return onSuccess(() -> stage,
                                performed -> complete(StatusCodes.OK, performed, Jackson.marshaller())
                            );
                        })
                    )
                ),
                pathPrefix("events", () -> path(PathMatchers.segment(), event -> route(
                    pathEndOrSingleSlash(() ->
                        post(() -> entity(
                            // POST /events/:event
                            Jackson.unmarshaller(EventDescription.class),
                            eventDescription -> {
                                CompletionStage<BoxOffice.EventResponse> stage = createEvent(event, eventDescription.getTickets());
                                return onSuccess(() -> stage,
                                    performed -> {
                                        if (performed instanceof BoxOffice.EventCreated)
                                            return complete(StatusCodes.OK, performed, Jackson.marshaller());
                                        else
                                            return complete(StatusCodes.BAD_REQUEST, "Event " + event + " already exists");
                                    }
                                );
                            })
                        )
                    )
                ))),
                pathPrefix("events", () -> path(PathMatchers.segment(), event -> route(
                        pathEndOrSingleSlash(() ->
                                get(() -> {
                                    // GET /events/:event
                                    CompletionStage<Optional<BoxOffice.Event>> stage = getEvent(event);
                                    return onSuccess(() -> stage,
                                        performed -> {
                                            if (performed.isPresent())
                                                return complete(StatusCodes.OK, performed.get(), Jackson.marshaller());
                                            else
                                                return complete(StatusCodes.NOT_FOUND);
                                        }
                                    );
                                })
                        )
                ))),
                pathPrefix("events", () -> path(PathMatchers.segment().slash("tickets"), event ->
                    pathEndOrSingleSlash(() ->
                        post(() -> entity(
                            // POST /events/:event
                            Jackson.unmarshaller(TicketRequest.class),
                                request -> {
                                CompletionStage<TicketSeller.Tickets> stage = requestTickets(event, request.getTickets());
                                return onSuccess(() -> stage,
                                        performed -> {
                                            if (!performed.getTickets().isEmpty())
                                                return complete(StatusCodes.OK, performed, Jackson.marshaller());
                                            else
                                                return complete(StatusCodes.NOT_FOUND);
                                        }
                                );
                            })
                        )
                    )
                ))
            );
    }

    private CompletionStage<BoxOffice.EventResponse> createEvent(String name, int nrOfTickets) {
        return PatternsCS
                .ask(boxOffice, new BoxOffice.CreateEvent(name, nrOfTickets), timeout)
                .thenApply(obj -> (BoxOffice.EventResponse) obj);
    }


    private CompletionStage<List<BoxOffice.Events>> getEvents() {
        return PatternsCS
                .ask(boxOffice, new BoxOffice.GetEvents(), timeout)
                .thenApply(obj -> (List<BoxOffice.Events>) obj);
    }

    private CompletionStage<Optional<BoxOffice.Event>> getEvent(String event) {
        return PatternsCS
                .ask(boxOffice, new BoxOffice.GetEvent(event), timeout)
                .thenApply(obj -> (Optional<BoxOffice.Event>) obj);
    }

    private CompletionStage<Optional<BoxOffice.Event>> cancelEvent(String event) {
        return PatternsCS
                .ask(boxOffice, new BoxOffice.CancelEvent(event), timeout)
                .thenApply(obj -> (Optional<BoxOffice.Event>) obj);
    }

    private CompletionStage<TicketSeller.Tickets> requestTickets(String event, int tickets) {
        return PatternsCS
                .ask(boxOffice, new BoxOffice.GetTickets(event, tickets), timeout)
                .thenApply(obj -> (TicketSeller.Tickets) obj);
    }

    private static class EventDescription {
        private int tickets;

        public int getTickets() {
            return tickets;
        }

        public void setTickets(int tickets) {
            this.tickets = tickets;
        }
    }

    private static class TicketRequest {
        private int tickets;

        public int getTickets() {
            return tickets;
        }

        public void setTickets(int tickets) {
            this.tickets = tickets;
        }
    }
}
