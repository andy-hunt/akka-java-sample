package ru.liga.goticks;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class TicketSeller extends AbstractActor {
    static public Props props(String event) {
        return Props.create(TicketSeller.class, () -> new TicketSeller(event));
    }

    private String event;
    private List<Ticket> tickets = Collections.emptyList();

    public TicketSeller(String event) {
        this.event = event;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Add.class, addTicket -> {
                    tickets = Stream.concat(addTicket.getTickets().stream(), tickets.stream()).collect(Collectors.toUnmodifiableList());
                })
                .match(Buy.class, buy -> {
                    if (tickets.size() >= buy.getTickets()) {
                        final List<Ticket> buyTickets;
                        buyTickets = tickets.subList(0, buy.getTickets());
                        sender().tell(new Tickets(event, buyTickets), ActorRef.noSender());
                        tickets = tickets.subList(buy.getTickets(), tickets.size());
                        sender().tell(new Tickets(event, tickets), ActorRef.noSender());
                    } else {
                        sender().tell(new Tickets(event), ActorRef.noSender());
                    }
                })
                .match(GetEvent.class, getEvent -> {
                    sender().tell(new BoxOffice.Event(event, tickets.size()), ActorRef.noSender());
                })
                .match(Cancel.class, cancel -> {
                    sender().tell(new BoxOffice.Event(event, tickets.size()), ActorRef.noSender());
                    self().tell(PoisonPill.getInstance(), ActorRef.noSender());
                })
                .build();
    }

    public static class Ticket {
        private int id;

        public Ticket(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class Tickets {
        private String event;
        private List<Ticket> tickets = Collections.emptyList();

        public Tickets(String event) {
            this.event = event;
        }

        public Tickets(String event, List<Ticket> tickets) {
            this.event = event;
            this.tickets = tickets;
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public List<Ticket> getTickets() {
            return tickets;
        }

        public void setTickets(List<Ticket> tickets) {
            this.tickets = tickets;
        }
    }

    //Case классы
    public static class Add {
        private List<Ticket> tickets;

        public Add(List<Ticket> tickets) {
            this.tickets = tickets;
        }

        public List<Ticket> getTickets() {
            return tickets;
        }
    }

    public static class Buy {
        private int tickets;

        public Buy(int tickets) {
            this.tickets = tickets;
        }

        public int getTickets() {
            return tickets;
        }
    }

    public static class GetEvent {

    }

    public static class Cancel {

    }
}
