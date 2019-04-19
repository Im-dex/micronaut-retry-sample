package retries;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Retryable;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
class BadController {
    public static AtomicInteger counter = new AtomicInteger(0);

    @Get("/crash")
    HttpResponse crash() {
        counter.incrementAndGet();
        return HttpResponse.serverError();
    }
}

@Retryable(attempts = "3")
@Client(id = "bad")
interface BadClient {
    @Get("/crash")
    CompletableFuture<String> crash();
}

@MicronautTest
class RetryTest {
    @Inject
    BadClient badClient;

    @Test
    void test() {
        try {
            badClient.crash().join();
        } catch (Throwable ignored) {
        }

        assert BadController.counter.get() == 3;
    }
}
