package ru.sanmedexpress.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.sanmedexpress.config.TelegramProperties;
import ru.sanmedexpress.domain.Client;
import ru.sanmedexpress.domain.OrderRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class TelegramNotificationServiceTest {
    @Test
    void disabledDoesNotSendRequest() {
        var builder = RestClient.builder().baseUrl("https://api.telegram.org");
        var server = MockRestServiceServer.bindTo(builder).build();
        var service = new TelegramNotificationService(builder.build(), new TelegramProperties(false, "token", "chat"));

        service.sendNewOrder(order("Комментарий"));

        server.verify();
    }

    @Test
    void enabledSendsMessage() {
        var builder = RestClient.builder().baseUrl("https://api.telegram.org");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.telegram.org/bottest-token/sendMessage"))
                .andExpect(method(POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Новая заявка")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("№")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("+79057690303")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        var service = new TelegramNotificationService(builder.build(), new TelegramProperties(true, "test-token", "12345"));

        service.sendNewOrder(order("Нужна перевозка"));

        server.verify();
    }

    @Test
    void blankCommentIsRenderedAsNotProvided() {
        var service = new TelegramNotificationService(RestClient.builder().build(), new TelegramProperties(false, "", ""));

        var message = service.buildMessage(order(""));

        assertThat(message).contains("Комментарий: Не указан");
    }

    @Test
    void apiErrorDoesNotPropagate() {
        var builder = RestClient.builder().baseUrl("https://api.telegram.org");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.telegram.org/bottest-token/sendMessage"))
                .andExpect(method(POST))
                .andRespond(withServerError());
        var service = new TelegramNotificationService(builder.build(), new TelegramProperties(true, "test-token", "12345"));

        assertThatNoException().isThrownBy(() -> service.sendNewOrder(order("Комментарий")));
        server.verify();
    }

    @Test
    void emptyTokenOrChatSkipsSending() {
        var builder = RestClient.builder().baseUrl("https://api.telegram.org");
        var server = MockRestServiceServer.bindTo(builder).build();
        var service = new TelegramNotificationService(builder.build(), new TelegramProperties(true, "", ""));

        assertThatNoException().isThrownBy(() -> service.sendNewOrder(order("Комментарий")));
        server.verify();
    }

    private OrderRequest order(String comment) {
        return new OrderRequest(new Client("Maxim", "+79057690303"), comment, "127.0.0.1", "test-agent");
    }
}
