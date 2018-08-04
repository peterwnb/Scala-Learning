package com.today.main

import java.util.concurrent.ForkJoinPool

import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.immutable.{ParSeq, ParSet}

object Test3 {

  def doSth(it: Int): String ={
    val result = Thread.currentThread().getName

    Thread.sleep(500)

    result
  }

  def main(args: Array[String]): Unit = {

    val list = List.range(1, 100)

    val pool = new ForkJoinPool(8)
    val ts = new ForkJoinTaskSupport(pool)
    val par = list.par

    par.tasksupport = ts

    val begin = System.currentTimeMillis()
    val results: ParSet[String] = par.map(doSth).toSet
    val end = System.currentTimeMillis()

    println("time:" + (begin-end) + "ms\t" + results.size + ":" + results)

  }

}
