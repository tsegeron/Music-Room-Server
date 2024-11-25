package com.laru

import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class DbTest {
    companion object {

        @BeforeClass
        @JvmStatic
        fun setup() {
            // init db
//            val dataSource = HikariDataSource()
//            dataSource.jdbcUrl = "jdbc:postgresql://localhost:5432/testdb?ssl=false"
//            dataSource.username = "test"
//            dataSource.password = "testpassword"
//            Database.connect(dataSource)
        }
    }

    @Before
    fun prepareTest() {
//        transact {
//            SchemaUtils.create(Animals)
//        }
    }

    @After
    fun cleanupTest() {
//        transact {
//            SchemaUtils.drop(Animals)
//        }
    }

    @Test
    fun testThatDefaultValuesSavedAreOk() {
//        val timeB4Transaction = System.currentTimeMillis()
//
//        val id = transaction {
//            val animal = Animal.new {
//                name = "Tom"
//            }
//            return@transaction animal.id
//        }
//
//        val animal = transact {
//            Animal[id]
//        }
//
//        println("timeB4Transaction: $timeB4Transaction, createdAt: ${animal.createdAt}")
//        Assert.assertTrue(animal.createdAt > timeB4Transaction)
    }
}
