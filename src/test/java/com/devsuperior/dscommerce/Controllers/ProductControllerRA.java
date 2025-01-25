package com.devsuperior.dscommerce.Controllers;

import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;


public class ProductControllerRA {

    private Map<String, Object> postProductInstance;
    @BeforeEach
    public void setUp() {
        baseURI = "http://localhost:8080";

        postProductInstance = new HashMap<>();
        postProductInstance.put("name", "Meu Produto");
        postProductInstance.put("description", "Um produto muito bom para o meu uso.");
        postProductInstance.put("imgUrl", "http://imgurldomeuproduto.com.br/1234567890");
        postProductInstance.put("price", 200.00);

        List<Map<String, Object>> categories = new ArrayList<>();

        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", 2);

        Map<String, Object> category2 = new HashMap<>();
        category1.put("id", 3);

        categories.add(category1);
        categories.add(category2);

        postProductInstance.put("categories", categories);
    }

    @Test
    public void findByIdShouldReturnProductWhenIdExists() {
        Long existingId = 1L;

        given()
                .when()
                    .get("/products/{id}", existingId)
                .then()
                    .statusCode(200)
                    .body("id", is(1))
                    .body("name", equalTo("The Lord of the Rings"))
                    .body("categories.name", hasItems("Livros"));
    }

    @Test
    public void findAllShouldReturnPageOfProducts() {

        given()
                .when()
                .get("/products?name=")
                .then()
                .statusCode(200)
                .body("content[0].name", equalTo("The Lord of the Rings"));
    }

    @Test
    public void findAllShouldReturnPageOfProductsWithNameFilter() {

        given()
                .when()
                .get("/products?name=Macbook")
                .then()
                .statusCode(200)
                .body("content.id[0]", is(3))
                .body("totalElements", is(1));
    }

    @Test
    public void findAllShouldReturnPageOfProductsWithPriceGreaterThen2000() {

        given()
                .when()
                .get("/products")
                .then()
                .statusCode(200)
                .body("content.findAll{ it.price > 2000.00 }.name", hasItems("Smart TV", "PC Gamer Hera"));
    }

    @Test
    public void insertProductShouldReturnCreatedWhenUserLoggedAsAdmin() {

        JSONObject newObject = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + adminToken)

                .when()
                    .post("/products")
                    .body(newObject)
                    .contentType(ContentType.APPLICATION_JSON)
                    .accept(ContentType.APPLICATION_JSON)

                .then()
                    .statusCode(200)
                    .body("name", equalTo("Meu produto"))
                    .body("price", is(200.00))
                    .body("categories", hasItems(2, 3));
    }
}
