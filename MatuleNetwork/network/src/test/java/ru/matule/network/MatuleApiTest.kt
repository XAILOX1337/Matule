package ru.matule.network

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.matule.network.config.NetworkConfig
import ru.matule.network.config.TokenProvider
import ru.matule.network.model.CreateProjectRequestDto
import ru.matule.network.model.ImageUpload
import ru.matule.network.result.NetworkError
import ru.matule.network.result.NetworkResult

/**
 * Проверка сетевой библиотеки на подставном сервере: разбор ответов, заголовок авторизации,
 * фильтры, multipart и обработка ошибок.
 */
class MatuleApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: MatuleApi
    private var token: String? = null

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = MatuleNetwork.create(NetworkConfig(server.url("/").toString()), TokenProvider { token })
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueue(code: Int, body: String) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    @Test
    fun signIn_success_returnsTokenAndUser() = runBlocking {
        enqueue(200, """{"token":"abc","record":{"id":"u1","email":"demo@matule.ru","name":"Эдуард","extra":1}}""")

        val result = api.auth.signIn("demo@matule.ru", "Matule2026!")

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals("abc", data.token)
        assertEquals("Эдуард", data.record.name)
        val request = server.takeRequest()
        assertEquals("/api/collections/users/auth-with-password", request.path)
        assertTrue(request.body.readUtf8().contains("\"identity\":\"demo@matule.ru\""))
    }

    @Test
    fun signIn_wrongPassword_returnsValidationError() = runBlocking {
        enqueue(400, """{"status":400,"message":"Failed to authenticate.","data":{}}""")

        val result = api.auth.signIn("demo@matule.ru", "wrong")

        assertTrue(result is NetworkResult.Failure)
        val error = (result as NetworkResult.Failure).error
        assertTrue(error is NetworkError.Validation)
        assertEquals("Failed to authenticate.", (error as NetworkError.Validation).serverMessage)
    }

    @Test
    fun signUp_duplicateEmail_translatesFieldError() = runBlocking {
        enqueue(
            400,
            """{"status":400,"message":"Failed to create record.","data":{"email":{"code":"validation_not_unique","message":"Value must be unique."}}}""",
        )

        val result = api.auth.signUp(
            ru.matule.network.model.SignUpRequestDto(
                email = "demo@matule.ru",
                password = "Matule2026!",
                passwordConfirm = "Matule2026!",
                name = "Эдуард",
                patronymic = "Олегович",
                surname = "Тицкий",
                birthday = "28.02.1991",
                gender = "Мужской",
            ),
        )

        val error = (result as NetworkResult.Failure).error
        assertEquals("Пользователь с таким email уже зарегистрирован", error.message)
    }

    @Test
    fun isEmailAvailable_sendsEmailAndParsesAnswer() = runBlocking {
        enqueue(200, """{"available":false}""")

        val result = api.auth.isEmailAvailable("demo@matule.ru")

        assertEquals(false, (result as NetworkResult.Success).data)
        val request = server.takeRequest()
        assertEquals("/api/matule/check-email", request.path)
        assertTrue(request.body.readUtf8().contains("\"email\":\"demo@matule.ru\""))
    }

    @Test
    fun authorizedRequest_sendsTokenHeader() = runBlocking {
        token = "secret-token"
        enqueue(200, """{"id":"u1","email":"demo@matule.ru"}""")

        api.profile.getProfile("u1")

        assertEquals("secret-token", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun unauthorized_returnsUnauthorized() = runBlocking {
        enqueue(401, """{"status":401,"message":"The request requires valid record authorization token.","data":{}}""")

        val result = api.auth.refresh()

        assertTrue((result as NetworkResult.Failure).error is NetworkError.Unauthorized)
    }

    @Test
    fun products_parsesPocketBasePage() = runBlocking {
        enqueue(
            200,
            """{"page":1,"perPage":200,"totalItems":2,"totalPages":1,"items":[
                {"id":"p1","title":"Рубашка","price":690,"type":"Мужская одежда","category":"c1"},
                {"id":"p2","title":"Шорты","price":300.0,"category":"c1"}]}""",
        )

        val result = api.catalog.getProducts("c1")

        val items = (result as NetworkResult.Success).data
        assertEquals(2, items.size)
        assertEquals(690.0, items.first().price, 0.0)
        val filter = server.takeRequest().requestUrl!!.queryParameter("filter")
        assertEquals("category = \"c1\"", filter)
    }

    @Test
    fun search_buildsCaseVariantsFilter() = runBlocking {
        enqueue(200, """{"items":[]}""")

        api.catalog.searchProducts("рубашка")

        val filter = server.takeRequest().requestUrl!!.queryParameter("filter")!!
        assertTrue(filter.contains("title ~ \"Рубашка\""))
        assertTrue(filter.contains("title ~ \"рубашка\""))
    }

    @Test
    fun brokenJson_returnsParsingError() = runBlocking {
        enqueue(200, """{"page":1,"items":[{"title":"без id"}]}""")

        val result = api.catalog.getProducts()

        assertTrue((result as NetworkResult.Failure).error is NetworkError.Parsing)
    }

    @Test
    fun serverError_returnsServerError() = runBlocking {
        enqueue(500, "")

        val result = api.orders.createOrder(
            ru.matule.network.model.CreateOrderRequestDto(user = "u1", items = emptyList(), total = 0),
        )

        val error = (result as NetworkResult.Failure).error
        assertTrue(error is NetworkError.Server)
        assertEquals(500, (error as NetworkError.Server).code)
    }

    @Test
    fun deleteFromCart_emptyBody_isSuccess() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204))

        val result = api.cart.removeFromCart("item1")

        assertTrue(result is NetworkResult.Success)
        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/collections/cart/records/item1", request.path)
    }

    @Test
    fun createProject_sendsMultipartWithImage() = runBlocking {
        enqueue(200, """{"id":"pr1","title":"Мой первый проект","image":"photo_abc.jpg","created":"2026-09-29 10:00:00.000Z"}""")

        val result = api.projects.createProject(
            CreateProjectRequestDto(
                userId = "u1",
                title = "Мой первый проект",
                type = "Одежда",
                startDate = "01.10.2026",
                endDate = "15.10.2026",
                recipient = "Себе",
                source = "example.com",
                category = "Женщинам",
            ),
            ImageUpload(fileName = "photo.jpg", mimeType = "image/jpeg", bytes = byteArrayOf(1, 2, 3)),
        )

        assertEquals("pr1", (result as NetworkResult.Success).data.id)
        val body = server.takeRequest().body.readUtf8()
        assertTrue(body.contains("name=\"image\"; filename=\"photo.jpg\""))
        assertTrue(body.contains("name=\"start_date\""))
    }

    @Test
    fun serverDown_returnsNoConnection() = runBlocking {
        val offline = MatuleNetwork.create(NetworkConfig("http://127.0.0.1:1/", connectTimeoutSeconds = 2), TokenProvider { null })

        val result = offline.catalog.getPromotions()

        assertTrue((result as NetworkResult.Failure).error is NetworkError.NoConnection)
    }

    @Test
    fun fileUrl_buildsPocketBasePath() {
        assertEquals(
            "${server.url("/")}api/files/projects/pr1/photo.jpg",
            api.fileUrl("projects", "pr1", "photo.jpg"),
        )
    }
}
