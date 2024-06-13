package com.distribuida;

import com.distribuida.db.Book;
import com.distribuida.service.IBookService;
import com.google.gson.Gson;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.enterprise.inject.spi.CDI;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContainerLifecycle;

import java.util.List;

public class Main {

    private static ContainerLifecycle lifecycle = null;

    static IBookService service;

    static Gson gson = new Gson();

    static void crear(ServerRequest rq, ServerResponse res) {
        String bstr = rq.content().as(String.class);
        Book book= gson.fromJson(bstr, Book.class);
        service.insertar(book);
        res.send(gson.toJson(book));
    }

    static void eliminar(ServerRequest rq, ServerResponse res) {
        String _id = rq.path().pathParameters().get("id");
        service.eliminar(Integer.valueOf(_id));
        res.send("Eliminado");
    }

    static void buscarId(ServerRequest rq, ServerResponse res) {
        String _id = rq.path().pathParameters().get("id");
        res.send(gson.toJson(service.buscarId(Integer.valueOf(_id))));
    }

    static void listarLibros(ServerRequest rq, ServerResponse res) {
        res.send(gson.toJson(service.buscarLibros()));
    }

    static void modificar(ServerRequest rq, ServerResponse res) {
        String _id = rq.path().pathParameters().get("id");
        String bstr = rq.content().as(String.class);
        Book book = gson.fromJson(bstr, Book.class);

        book.setId(Integer.valueOf(_id));

        service.modificar(book);

        res.send(gson.toJson(book) );

    }

    public static void main(String[] args) {
        lifecycle = WebBeansContext.currentInstance().getService(ContainerLifecycle.class);
        lifecycle.startApplication(null);

        service = CDI.current().select(IBookService.class).get();

        WebServer server = WebServer.builder()
                .port(8080)
                .routing(builder -> builder
                        .post("/books", Main::crear)
                        .delete("/books/{id}", Main::eliminar)
                        .get("/books/{id}", Main::buscarId)
                        .get("/books", Main::listarLibros)
                        .put("/books/{id}", Main::modificar)

                )
                .build();

        server.start();

        service.buscarLibros().stream().forEach(System.out::println);

        lifecycle.stopApplication(null);

    }

}
