package io.veicot;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RestRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .component("servlet");

        rest()
                .get("/user/hello")
                    .to("direct:helloUser")
                .get("/admin/hello")
                    .to("direct:helloAdmin");

        from("direct:helloUser")
                .transform().simple("Hello User!");

        from("direct:helloAdmin")
                .transform().simple("Hello Admin!");
    }
}
