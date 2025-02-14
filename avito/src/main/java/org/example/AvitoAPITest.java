package org.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AvitoAPITest {

    private static String createdItemId;
    private static int sellerId;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://qa-internship.avito.com";
        sellerId = generateUniqueSellerId(); // Генерация уникального sellerId
    }
    private static int generateUniqueSellerId() {
        return 111111 + (int) (Math.random() * (999999 - 111111));
    }

    @Test
    @DisplayName("Создание объявления")
    @Order(1)
    public void testCreateItem() {
        String requestBody = String.format("{\n" +
                "  \"sellerID\": %d,\n" +
                "  \"name\": \"dsdsd\",\n" +
                "  \"price\": 100,\n" +
                "  \"statistics\":{\n" +
                "    \"contacts\":3,\n" +
                "    \"likes\":123,\n" +
                "    \"viewCount\":12\n" +
                "  }\n" +
                "}", sellerId);

        Response createResponse = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(requestBody)
                .when()
                .post("/api/1/item")
                .then()
                .statusCode(200)
                .extract()
                .response();

        createdItemId = createResponse.jsonPath().getString("status").replaceAll("^.* - ", "").trim();

//        System.out.println(createdItemId);
    }

    @Test
    @DisplayName("Получаем объявление по айди")
    @Order(2)
    public void testGetItemById() {
        given()
                .header("Accept", "application/json")
                .pathParam("id", createdItemId)
                .when()
                .get("/api/1/item/{id}")
                .then()
                .statusCode(200)
                .body("[0].id", equalTo(createdItemId))
                .body("[0].sellerId", equalTo(sellerId))
                .body("[0].name", equalTo("dsdsd"))
                .body("[0].price", equalTo(100));
    }

    @Test
    @DisplayName("Получение всех объявлений по идентификатору продавца")
    @Order(3) // Третий тест: получение всех объявлений по идентификатору продавца
    public void testGetItemsBySellerId() {
        given()
                .header("Accept", "application/json")
                .pathParam("sellerID", sellerId)
                .when()
                .get("/api/1/{sellerID}/item")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("findAll { it.sellerId == " + sellerId + " }.size()", greaterThan(0));
    }

    @Test
    @DisplayName("Получение статистики по объявлению")
    @Order(4)
    public void testGetItemStatistics() {
        given()
                .header("Accept", "application/json")
                .pathParam("id", createdItemId)
                .when()
                .get("/api/1/statistic/{id}")
                .then()
                .statusCode(200)
                .body("likes", notNullValue())
                .body("viewCount", notNullValue())
                .body("contacts", notNullValue());
    }
}