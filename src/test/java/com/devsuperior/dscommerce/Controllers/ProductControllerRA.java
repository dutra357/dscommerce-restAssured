package com.devsuperior.dscommerce.Controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dscommerce.tests.TokenUtil;

import io.restassured.http.ContentType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


public class ProductControllerRA {

    private Map<String, Object> postProductInstance;
    private String clientUsername, clientPassword,
            adminUsername, adminPassword, clientToken, adminToken;

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
                .body(newObject)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)

                .when()
                    .post("/products")

                .then()
                    .statusCode(201)
                    .body("name", equalTo("Meu Produto"))
                    .body("price", is(200.00F))
                    .body("categories.id", hasItems(2, 3));
    }

    @Test
    public void insertProductShouldReturnUnprocessableEntityWhenProductDataIsInvalid() {
        postProductInstance.put("name", "ab");
        JSONObject newObject = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newObject)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)

                .when()
                .post("/products")

                .then()
                .statusCode(422);
    }

    @Test
    public void insertProductShouldReturnUnauthorizedWhenNoToken() {

        JSONObject newObject = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application-json")
                .body(newObject)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)

                .when()
                .post("/products")

                .then()
                .statusCode(401);
    }

    @Test
    public void insertProductShouldReturnUnauthorizedWhenInvalidToken() {

        JSONObject newObject = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + adminToken + "1234")
                .body(newObject)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)

                .when()
                .post("/products")

                .then()
                .statusCode(401);
    }

    @Test
    public void insertProductShouldReturnForbiddenWhenLoggedAsClient() {

        JSONObject newObject = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + clientToken)
                .body(newObject)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)

                .when()
                .post("/products")

                .then()
                .statusCode(403);
    }

    @Test
    public void deleteShouldReturnNoContentWhenIdExistsAndAdminLogged() {
        Long existingProductId = 25L;

        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + adminToken)

                .when()
                .delete("/products/{id}", existingProductId)

                .then()
                .statusCode(204);
    }

    @Test
    public void deleteShouldReturnNotFoundWhenIdDoesNotExistsAndAdminLogged() {
        Long nonExistingProductId = 9999L;

        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + adminToken)

                .when()
                .delete("/products/{id}", nonExistingProductId)

                .then()
                .statusCode(404);
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteShouldReturnBadRequestWhenDependentIdAndAdminLogged() {
        Long dependentId = 1L;

        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + adminToken)

                .when()
                .delete("/products/{id}", dependentId)

                .then()
                .statusCode(400);
    }

    @Test
    public void deleteShouldReturnForbiddenWhenClientLogged() {
        Long dependentId = 1L;

        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + clientToken)

                .when()
                .delete("/products/{id}", dependentId)

                .then()
                .statusCode(403);
    }

    @Test
    public void deleteShouldReturnUnauthorizedWhenNobodyIsLogged() {
        Long dependentId = 1L;

        given()
                .header("Content-type", "application-json")

                .when()
                .delete("/products/{id}", dependentId)

                .then()
                .statusCode(401);
    }
}
