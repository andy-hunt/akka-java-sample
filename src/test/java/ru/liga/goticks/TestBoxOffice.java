package ru.liga.goticks;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class TestBoxOffice {
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
        ActorRef boxOffice = system.actorOf(BoxOffice.props(Duration.ofMillis(10000)));
        String eventName = "RHCP";
        boxOffice.tell(new BoxOffice.CreateEvent(eventName, 10), testProbe.getRef());
        BoxOffice.EventCreated eventCreated = testProbe.expectMsgClass(BoxOffice.EventCreated.class);
        Assert.assertEquals(eventCreated.getEvent().getTickets(), 10);
    }
}
