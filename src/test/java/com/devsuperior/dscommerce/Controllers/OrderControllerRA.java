package com.devsuperior.dscommerce.Controllers;

import com.devsuperior.dscommerce.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;


public class OrderControllerRA {

    private Map<String, Object> postProductInstance;
    private String clientUsername, clientPassword,
            adminUsername, adminPassword, clientToken, adminToken;

    private Long existingOrderId, nonExistingOrderId;
    @BeforeEach
    public void setUp() {
        baseURI = "http://localhost:8080";

        postProductInstance = new HashMap<>();
        postProductInstance.put("name", "Meu Produto");
        postProductInstance.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
        postProductInstance.put("imgUrl", "http://imgurldomeuproduto.com.br/1234567890.jpg");
        postProductInstance.put("price", 200.00);

        List<Map<String, Object>> categories = new ArrayList<>();

        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", 2);

        Map<String, Object> category2 = new HashMap<>();
        category2.put("id", 3);

        categories.add(category1);
        categories.add(category2);

        postProductInstance.put("categories", categories);

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";

        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
        adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);

        existingOrderId = 1L;
        nonExistingOrderId = 999L;
    }

    @Test
    public void findByIdShouldReturnOrderWhenIdExistsAndAdminLogged() {

        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)

                .when()
                    .get("/orders/{id}", existingOrderId)

                .then()
                    .statusCode(200)
                    .body("id", is(1))
                    .body("moment", equalTo("2022-07-25T13:00:00Z"))
                    .body("status", equalTo("PAID"))
                    .body("client.name", equalTo("Maria Brown"))
                    .body("total", is(1431.0F));
    }

    @Test
    public void findByIdShouldReturnYourOwnOrderWhenIdExistsAndClientLogged() {

        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)

                .when()
                .get("/orders/{id}", existingOrderId)

                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("moment", equalTo("2022-07-25T13:00:00Z"))
                .body("status", equalTo("PAID"))
                .body("client.name", equalTo("Maria Brown"))
                .body("total", is(1431.0F));
    }

    @Test
    public void findByIdShouldReturnForbiddenWhenIdExistsAndClientLoggedAndOrderDoesNotBelongUser() {

        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)

                .when()
                .get("/orders/{id}", existingOrderId + 1)

                .then()
                .statusCode(403);
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExistsAndAdminLogged() {

        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)

                .when()
                .get("/orders/{id}", nonExistingOrderId)

                .then()
                .statusCode(404);
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExistsAndClientLogged() {

        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)

                .when()
                .get("/orders/{id}", nonExistingOrderId)

                .then()
                .statusCode(404);
    }
    @Test
    public void findByIdShouldReturnUnauthorizedWhenIdDoesNotExistsAndNotLogged() {

        given()
                .header("Content-type", "application-json")
                .accept(ContentType.JSON)

                .when()
                .get("/orders/{id}", nonExistingOrderId)

                .then()
                .statusCode(401);
    }

}
