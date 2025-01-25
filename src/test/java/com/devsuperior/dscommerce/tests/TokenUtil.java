package com.devsuperior.dscommerce.tests;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static io.restassured.RestAssured.*;


public class TokenUtil {

    public static String obtainAccessToken(String username, String password) {
        Response response = authRequest(username, password);
        JsonPath jsonBody = response.jsonPath();

        return jsonBody.getString("access_token");
    }

    private static Response authRequest(String userName, String password) {
        return
                given()
                        .auth()
                        .preemptive()
                        .basic("myclientid", "myclientsecret")

                        .contentType("application/x-www-form-urlencoded")
                        .formParam("grant_type","password")
                        .formParam("username",userName)
                        .formParam("password",password)

                        .when()
                        .post("/oauth2/token");



    }
}
