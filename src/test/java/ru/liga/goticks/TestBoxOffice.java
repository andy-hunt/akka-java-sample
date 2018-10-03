package ru.liga.goticks;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class TestTicketSeller {
    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testAddAndBuyTicket() {
        final TestKit testProbe = new TestKit(system);
        List<TicketSeller.Ticket> tickets = IntStream.rangeClosed(1, 10).boxed().map(i -> new TicketSeller.Ticket(i))
                .collect(Collectors.toList());
        String event = "RCHP";
        ActorRef ticketingActor = system.actorOf(TicketSeller.props(event));
        ticketingActor.tell(new TicketSeller.Add(tickets), testProbe.getRef());
        ticketingActor.tell(new TicketSeller.Buy(1), testProbe.getRef());
        TicketSeller.Tickets buyerTickets = testProbe.expectMsgClass(TicketSeller.Tickets.class);
        Assert.assertEquals(buyerTickets.getTickets().size(), 1);
    }
}
