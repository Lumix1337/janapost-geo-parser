package com.example.myapplication.data

import kotlinx.coroutines.delay

class MockAuthRepository : AuthRepository {
    override suspend fun requestOtp(dial: String, national: String) {
        delay(200)
    }

    override suspend fun verifyOtp(dial: String, national: String, code: String): Boolean {
        delay(250)
        return code.length == 6 && code.all(Char::isDigit)
    }

    override suspend fun login(dial: String, national: String, password: String): UserSession? {
        delay(250)
        if (password.isBlank()) return null
        return UserSession(dial, national)
    }
}

class MockCatalogRepository : CatalogRepository {
    override val warehouse = Warehouse(
        userId = "JP49028173",
        phone = "13762045891",
        addressEn = "JanaPost Demo Warehouse, 88 Chouzhou North Road, Yiwu, Zhejiang, China",
        addressRu = "Демо-склад JanaPost, Chouzhou North Road, 88, Иу, Чжэцзян, Китай",
        addressLocal = "浙江省义乌市稠州北路88号 JanaPost Demo",
    )

    private val parcels = listOf(
        Parcel(
            id = "1",
            tracking = "JP2026KZ0018",
            titleEn = "Sneakers",
            titleRu = "Кроссовки",
            direction = Direction.RECEIVE,
            statusId = "in_stock",
            updatedEn = "28 Sep",
            updatedRu = "28 сен",
            weightEn = "1.2 kg",
            weightRu = "1,2 кг",
            marketplace = "Temu",
        ),
        Parcel(
            id = "2",
            tracking = "JP2026KZ0044",
            titleEn = "Phone case",
            titleRu = "Чехол",
            direction = Direction.RECEIVE,
            statusId = "on_road",
            updatedEn = "27 Sep",
            updatedRu = "27 сен",
            weightEn = "0.2 kg",
            weightRu = "0,2 кг",
            marketplace = "Pinduoduo",
        ),
        Parcel(
            id = "3",
            tracking = "JP2026KZ0102",
            titleEn = "Headphones",
            titleRu = "Наушники",
            direction = Direction.RECEIVE,
            statusId = "pending",
            updatedEn = "29 Sep",
            updatedRu = "29 сен",
            weightEn = "0.4 kg",
            weightRu = "0,4 кг",
            marketplace = "Taobao",
        ),
        Parcel(
            id = "4",
            tracking = "JP2026KZ0090",
            titleEn = "Cable",
            titleRu = "Кабель",
            direction = Direction.RECEIVE,
            statusId = "received",
            updatedEn = "20 Sep",
            updatedRu = "20 сен",
            weightEn = "0.1 kg",
            weightRu = "0,1 кг",
            marketplace = "Temu",
        ),
        Parcel(
            id = "5",
            tracking = "JP2026KZ0077",
            titleEn = "Hoodie",
            titleRu = "Худи",
            direction = Direction.SEND,
            statusId = "sorting",
            updatedEn = "26 Sep",
            updatedRu = "26 сен",
            weightEn = "0.8 kg",
            weightRu = "0,8 кг",
            marketplace = "JD.com",
        ),
        Parcel(
            id = "6",
            tracking = "JP2026KZ0114",
            titleEn = "Books",
            titleRu = "Книги",
            direction = Direction.SEND,
            statusId = "accepted",
            updatedEn = "25 Sep",
            updatedRu = "25 сен",
            weightEn = "2.0 kg",
            weightRu = "2,0 кг",
            marketplace = "Pinduoduo",
        ),
    )

    override fun parcels(direction: Direction, statusId: String): List<Parcel> {
        return parcels.filter { it.direction == direction && it.statusId == statusId }
    }
}
