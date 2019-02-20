package io.fabric8.quickstarts.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

@Configuration
@SpringBootApplication
@ImportResource({"classpath:spring/camel-context.xml"})
public class Application extends SpringBootServletInitializer {

    @Autowired
    private YamlConfig yamlConfig;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean(
                new CamelHttpTransportServlet(), "/camel-rest-sql/*");
        servlet.setName("CamelServlet");
        return servlet;
    }

    @Component
    class RestApi extends RouteBuilder {

        @Override
        public void configure() {
            restConfiguration()
                    .contextPath("/camel-rest-sql").apiContextPath("/api-doc")
                    .apiProperty("api.title", "Camel REST API")
                    .apiProperty("api.version", "1.0")
                    .apiProperty("cors", "true")
                    .apiContextRouteId("doc-api")
                    .component("servlet")
                    .bindingMode(RestBindingMode.json);

            rest("/books").description("Books REST service")
                    .get("/").description("The list of all the books")
                    .route().routeId("books-api")
                    .to("sql:select distinct description from orders?" +
                            "dataSource=dataSource&" +
                            "outputClass=io.fabric8.quickstarts.camel.Book")
                    .endRest()
                    .get("order/{id}").description("Details of an order by id")
                    .route().routeId("order-api")
                    .to("sql:select * from orders where id = :#${header.id}?" +
                            "dataSource=dataSource&outputType=SelectOne&" +
                            "outputClass=io.fabric8.quickstarts.camel.Order");
        }
    }

    @Component
    class Backend extends RouteBuilder {

        @Override
        public void configure() {
            from("quartz2://quartztimer?cron=0/10+*+*+*+*+?&stateful=true").routeId("quartztimer")
                    .setHeader("ROUTING_KEY", simple("'trigger1'")).log("Quartz2");

            // A first route generates some orders and queue them in DB
            from("timer:new-order?delay=1s&period={{quickstart.generateOrderPeriod:10s}}")
                    .routeId("generate-order")
                    .bean("orderService", "generateOrder")
                    .to("sql:insert into orders (id, item, amount, description, processed) values " +
                            "(:#${body.id} , :#${body.item}, :#${body.amount}, :#${body.description}, false)?" +
                            "dataSource=dataSource")
                    .log("Inserted new order ${body.id}");

            // A second route polls the DB for new orders and processes them
            from("sql:select * from orders where processed = false?" +
                    "consumer.onConsume=update orders set processed = true where id = :#id&" +
                    "consumer.delay={{quickstart.processOrderPeriod:5s}}&" +
                    "dataSource=dataSource")
                    .routeId("process-order")
                    .bean("orderService", "rowToOrder")
                    .log("Processed order #id ${body.id} with ${body.amount} copies of the «${body.description}» book");
        }
    }
}