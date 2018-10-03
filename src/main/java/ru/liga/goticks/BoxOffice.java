package ru.liga.goticks;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.PatternsCS;
import scala.Option;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class BoxOffice extends AbstractActor {
    public static final String NAME = "boxOffice" ;
    private Duration duration;

    public BoxOffice(Duration duration) {
        this.duration = duration;
    }

    public static Props props(Duration timeout) {
        return Props.create(BoxOffice.class, () -> new BoxOffice(timeout));
    }

    public ActorRef createTicketSeller(String name) {
        return getContext().actorOf(TicketSeller.props(name), name);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateEvent.class, createEvent -> {
                    ActorRef eventTickets = createTicketSeller(createEvent.getName());
                    List<TicketSeller.Ticket> newTickets = IntStream.range(1, createEvent.getTickets() + 1).boxed()
                            .map(TicketSeller.Ticket::new)
                            .collect(Collectors.toUnmodifiableList());
                    eventTickets.tell(new TicketSeller.Add(newTickets), ActorRef.noSender());
                    sender().tell(new EventCreated(new Event(createEvent.getName(), createEvent.getTickets())), ActorRef.noSender());
                })
                .match(GetTickets.class, getTickets -> {
                    Option<ActorRef> child = getContext().child(getTickets.getEvent());
                    if (child.isDefined()) {
                        child.get().forward(new TicketSeller.Buy(getTickets.getTickets()), getContext());
                    } else {
                        sender().tell(new TicketSeller.Tickets(getTickets.getEvent()), ActorRef.noSender());
                    }
                })
                .match(GetEvent.class, event -> {
                    Option<ActorRef> option = getContext().child(event.getName());
                    if (option.isDefined()) {
                        option.get().forward(new TicketSeller.GetEvent(), getContext());
                    } else {
                        sender().tell(Optional.empty(), ActorRef.noSender());
                    }
                })
                .match(GetEvents.class, getEvents -> {
                    List<CompletableFuture<Optional<Event>>> futureEvents = new ArrayList<>();
                    for (ActorRef child : getContext().getChildren()) {
                        CompletableFuture<Optional<Event>> eventCompletionStage =
                                PatternsCS.ask(child, new GetEvent(child.path().name()), duration)
                                        .thenApply(e -> (Optional<Event>) e).toCompletableFuture();
                        futureEvents.add(eventCompletionStage);
                    }
                    CompletableFuture<List<Event>> listCompletableFuture = CompletableFuture.allOf(futureEvents.toArray(new CompletableFuture[futureEvents.size()]))
                            .thenApply(v -> futureEvents.stream().map(CompletableFuture::join).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
                    PatternsCS.pipe(listCompletableFuture, getContext().dispatcher()).to(sender());
                })
                .match(CancelEvent.class, cancelEvent -> {
                    ActorRef child = getContext().getChild(cancelEvent.getName());
                    if (child == null) {
                        getSender().tell(Optional.empty(), ActorRef.noSender());
                    } else {
                        child.forward(new TicketSeller.Cancel(), getContext());
                    }
                })
                .build();
    }

    //Case classes
    public static class CreateEvent {
        private String name;
        private int tickets;

        public CreateEvent(String name, int tickets) {
            this.name = name;
            this.tickets = tickets;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getTickets() {
            return tickets;
        }

        public void setTickets(int tickets) {
            this.tickets = tickets;
        }
    }

    public static class GetEvent {
        private String name;

        public GetEvent(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class GetEvents {

    }

    public static class GetTickets {
        private String event;
        private int tickets;

        public GetTickets(String event, int tickets) {
            this.event = event;
            this.tickets = tickets;
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public int getTickets() {
            return tickets;
        }

        public void setTickets(int tickets) {
            this.tickets = tickets;
        }
    }

    public static class CancelEvent {
        private String name;

        public CancelEvent(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Event {

        public Event(String event, int tickets) {
            this.event = event;
            this.tickets = tickets;
        }

        private String event;
        private int tickets;

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public int getTickets() {
            return tickets;
        }

        public void setTickets(int tickets) {
            this.tickets = tickets;
        }
    }

    public static class Events {
        private List<Event> events;

        public List<Event> getEvents() {
            return events;
        }

        public void setEvents(List<Event> events) {
            this.events = events;
        }
    }

    public static class EventCreated implements EventResponse {
        private Event event;

        public EventCreated(Event event) {
            this.event = event;
        }

        public Event getEvent() {
            return event;
        }

        public void setEvent(Event event) {
            this.event = event;
        }
    }

    public static class EventExists implements EventResponse {
        private Event event;

        public Event getEvent() {
            return event;
        }

        public void setEvent(Event event) {
            this.event = event;
        }
    }

    public interface EventResponse {

    }
}
