import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import akka.actor.*;
import static akka.pattern.Patterns.ask;

/**
 * These are not fully-blown solutions to check-ins example.
 * We use a small part of the example that exposes the concurrent state problem and shows potential solutions.
 */
public class ch10_CheckInsImperative {
    public static void main(String[] args) throws InterruptedException {
        noSynchronization();
        monitors();
        threadSafeDataStructures();
        atomicReferencesImperative();
        atomicReferencesFunctional();
        actors();
    }

    static void noSynchronization() throws InterruptedException {
        var cityCheckIns = new HashMap<String, Integer>();
        Runnable task = () -> {
            for(int i = 0; i < 1000; i++) {
                var cityName = i % 2 == 0 ? "Cairo" : "Auckland";
                cityCheckIns.compute(cityName,
                        (city, checkIns) -> checkIns != null ? checkIns + 1 : 1);
            }
        };
        new Thread(task).start();
        new Thread(task).start();

        // main thread is the ranking computation thread (a simulation that shows the problem)
        Thread.sleep(300);
        System.out.println("[no synchronization] Computing ranking based on: " + cityCheckIns);
    }

    static void monitors() throws InterruptedException {
        var cityCheckIns = new HashMap<String, Integer>();
        Runnable task = () -> {
            for(int i = 0; i < 1000; i++) {
                var cityName = i % 2 == 0 ? "Cairo" : "Auckland";
                synchronized (cityCheckIns) {
                    cityCheckIns.compute(cityName,
                            (city, checkIns) -> checkIns != null ? checkIns + 1 : 1);
                }
            }
        };
        new Thread(task).start();
        new Thread(task).start();

        // main thread is the ranking computation thread (a simulation that shows the problem)
        Thread.sleep(300);
        System.out.println("[monitors] Computing ranking based on: " + cityCheckIns);
    }

    static void threadSafeDataStructures() throws InterruptedException {
        var cityCheckIns = new ConcurrentHashMap<String, Integer>();
        Runnable task = () -> {
            for(int i = 0; i < 1000; i++) {
                var cityName = i % 2 == 0 ? "Cairo" : "Auckland";
                cityCheckIns.compute(cityName,
                        (city, checkIns) -> checkIns != null ? checkIns + 1 : 1);
            }
        };
        new Thread(task).start();
        new Thread(task).start();

        // main thread is the ranking computation thread (a simulation that shows the problem)
        Thread.sleep(300);
        System.out.println("[thread-safe data structures] Computing ranking based on: " + cityCheckIns);
    }

    static void atomicReferencesImperative() throws InterruptedException {
        var cityCheckIns = new AtomicReference<>(new HashMap<String, Integer>());
        Runnable task = () -> {
            for(int i = 0; i < 1000; i++) {
                var cityName = i % 2 == 0 ? "Cairo" : "Auckland";
                var updated = false;
                while(!updated) {
                    var currentCheckIns = cityCheckIns.get();
                    var newCheckIns = new HashMap<>(currentCheckIns); // this is critical, because AtomicReference in Java expects two different objects passed to CAS
                    newCheckIns.compute(cityName,
                            (city, checkIns) -> checkIns != null ? checkIns + 1 : 1);
                    updated = cityCheckIns.compareAndSet(currentCheckIns, newCheckIns);
                }
            }
        };
        new Thread(task).start();
        new Thread(task).start();

        Thread.sleep(300);
        System.out.println("[atomic reference imperative] Computing ranking based on: " + cityCheckIns.get());
    }

    static void atomicReferencesFunctional() throws InterruptedException {
        var cityCheckIns = new AtomicReference<>(new HashMap<String, Integer>());
        Runnable task = () -> {
            for(int i = 0; i < 1000; i++) {
                var cityName = i % 2 == 0 ? "Cairo" : "Auckland";
                cityCheckIns.updateAndGet(oldCheckIns -> {
                    var newCheckIns = new HashMap<>(oldCheckIns);
                    newCheckIns.compute(cityName,
                            (city, checkIns) -> checkIns != null ? checkIns + 1 : 1);
                    return newCheckIns;
                });
            }
        };
        new Thread(task).start();
        new Thread(task).start();

        Thread.sleep(300);
        System.out.println("[atomic reference functional] Computing ranking based on: " + cityCheckIns.get());
    }

    static void actors() throws InterruptedException {
        ActorSystem system = ActorSystem.create("test-system");
        ActorRef checkInsActor = system.actorOf(Props.create(CheckInsActor.class), "check-ins-actor");
        ActorRef rankingActor = system.actorOf(Props.create(RankingActor.class), "ranking-actor");
        Runnable task = () -> {
            for(int i = 0; i < 1000; i++) {
                var cityName = i % 2 == 0 ? "Cairo" : "Auckland";
                checkInsActor.tell(new StoreCheckIn(cityName), null);
            }
        };
        new Thread(task).start();
        new Thread(task).start();

        Thread.sleep(300);
        rankingActor.tell(new ComputeRanking(checkInsActor), null);
        Thread.sleep(100);
        system.terminate();
    }
}

class CheckInsActor extends AbstractActor {
    private Map<String, Integer> cityCheckIns = new HashMap<>();

    public Receive createReceive() {
        return receiveBuilder().match(StoreCheckIn.class, message -> {
              cityCheckIns.compute(message.cityName,
                      (city, checkIns) -> checkIns != null ? checkIns + 1 : 1);
        }).match(GetCurrentCheckIns.class, message -> {
            getSender().tell(new HashMap<>(cityCheckIns), null);
        }).build();
    }
}

class RankingActor extends AbstractActor {
    public Receive createReceive() {
        return receiveBuilder().match(ComputeRanking.class, message -> {
            ask(message.checkInsActor, new GetCurrentCheckIns(), 1000).foreach(
                    cityCheckIns -> {
                        System.out.println("[actors] Computing ranking based on: " + cityCheckIns);
                        return this;
                    },
                    getContext().dispatcher()
            );
        }).build();
    }
}

class StoreCheckIn {
    public final String cityName;

    public StoreCheckIn(String cityName) {
        this.cityName = cityName;
    }
}

class GetCurrentCheckIns {
}

class ComputeRanking {
    public final ActorRef checkInsActor;

    public ComputeRanking(ActorRef checkInsActor) {
        this.checkInsActor = checkInsActor;
    }
}

