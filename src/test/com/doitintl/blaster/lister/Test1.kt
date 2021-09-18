package com.doitintl.blaster.lister

import org.junit.jupiter.api.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class Test1 {

    @BeforeTest
    fun before() {

    }

    @AfterTest
    fun after() {

    }

    @Test
    fun sampleTest() {
        throw  IllegalArgumentException("xxxxxxxxxxxxxxxx")
    }
}