import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenRingMutex {

    private final List<Boolean> tokens;
    private final Queue<Long> queue;
    private final int n;
    private final Object lock = new Object();

    public TokenRingMutex(int n) {
        this.n = n;
        this.tokens = new ArrayList<>(Collections.nCopies(n, false));
        this.tokens.set(0, true); // Initial token holder
        this.queue = new LinkedList<>();
    }

    public void requestCriticalSection() {
        long threadId = Thread.currentThread().getId();
        synchronized (lock) {
            if (!queue.contains(threadId)) {
                queue.add(threadId);
            }
        }

        while (true) {
            synchronized (lock) {
                long currentThreadId = Thread.currentThread().getId();
                int index = getIndexInQueue(currentThreadId);
                if (tokens.get(index % n) && index == 0) {
                    return;
                }
            }

            try {
                Thread.sleep(10); // To avoid busy-waiting
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void releaseCriticalSection() {
        synchronized (lock) {
            long threadId = Thread.currentThread().getId();
            int index = getIndexInQueue(threadId);
            int nextIndex = (index + 1) % n;
            tokens.set(index % n, false);
            tokens.set(nextIndex, true);
            queue.remove(threadId);
        }
    }

    private int getIndexInQueue(long threadId) {
        int i = 0;
        for (Long id : queue) {
            if (id == threadId) return i;
            i++;
        }
        return -1;
    }

    public static void main(String[] args) {
        TokenRingMutex mutex = new TokenRingMutex(3);

        for (int i = 0; i < 3; i++) {
            final int id = i;
            new Thread(() -> {
                while (true) {
                    System.out.println("Worker " + id + " is outside the critical section");
                    mutex.requestCriticalSection();
                    System.out.println("Worker " + id + " is inside the critical section");
                    try {
                        Thread.sleep(1000); // Simulate work inside critical section
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    mutex.releaseCriticalSection();
                }
            }).start();
        }
    }
}
