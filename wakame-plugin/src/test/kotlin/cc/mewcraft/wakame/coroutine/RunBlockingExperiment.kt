package cc.mewcraft.wakame.coroutine

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import kotlin.test.Test

class RunBlockingExperiment {

    @Test
    fun test1() {
        println("test1")
        runBlocking {
            println("test1 in runBlocking outer")
            runBlocking {
                println("test1 in runBlocking inner 1")
                runBlocking {
                    println("test1 in runBlocking inner 2")
                }
            }
        }
    }

    @Test
    fun test2() {
        println("test2")
    }

    val dispatcher = Executors.newSingleThreadExecutor(ThreadFactoryBuilder().setNameFormat("test-single-thread").build()).asCoroutineDispatcher()
}