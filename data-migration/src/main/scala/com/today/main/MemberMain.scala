

package com.today.main

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger
import java.util.{Optional, Scanner}

import com.github.dapeng.core.InvocationContextImpl
import com.today.common.{RedisDataSource, _}
import com.today.enums._
import com.today.mysql.member.dto._
import com.today.mysql.member.dto.freemud._
import com.today.mysql.member.sql.MemberSql
import com.today.redis.RedisUtil
import org.slf4j.LoggerFactory
import org.springframework.context.support.GenericXmlApplicationContext
import wangzx.scala_commons.sql._

import scala.collection.parallel.ForkJoinTaskSupport

/**
  * 会员信息全量同步
  */
object MemberMain {

  val MEMBER_CACHE_PREFIX = "MEMBER_CACHE"
  val CARD_CACHE_PREFIX = "CARD_CACHE"
  val ORDER_CACHE_PREFIX = "ORDER_CACHE"
  val defaultSourceEnum = LogSourceEnum.ALL

  val rows = 1000;
  var start = 1;

  var THREADS = 2
  lazy val executor = {
    new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue[Runnable](100),
      new ThreadPoolExecutor.CallerRunsPolicy()
    )
    // println(s"THREADS = $THREADS")
    // Executors.newFixedThreadPool(THREADS)
  }
  private val log = LoggerFactory.getLogger(MemberMain.getClass)

  def main(args: Array[String]): Unit = {
    println("程序执行命令注意携带参数： java -cp migration.jar com.today.main.MemberMain 2 5")
    if(args.length <2){
      println("缺少必要的参数，  args(0) 表示线程数量 , args(1) 表示需要执行的迁移方法：" +
        "1:迁移会员主档   2：迁移会员卡   3：迁移订单   4：迁移优惠券  、迁移积分 和 礼品卡消费流水  5、加载新老会员ID关系到redis缓存")
      System.exit(0)
      return
    }

    if (args.length >= 1)
      THREADS = args(0).toInt
    val context = new GenericXmlApplicationContext
    context.setValidating(false);
    context.load("./META-INF/spring/services.xml")
    context.refresh()
    System.setProperty("soa.zookeeper.host", "192.168.10.128") //取号服务器zookeeper配置
    //System.setProperty("soa.zookeeper.host", "127.0.0.1") //取号服务器zookeeper配置

    //同步会员主档表信息(member)、会员余额表信息(member_card_account)
    //clearData()
    //executeMemberNew()

    val cmd = args(1).toInt
    cmd match {
      case 1 => {
        clearData()
        executeMemberNew()
      }
      case 2 => executeMemberCard()
      case 3 => executeMemberOrder()
      case 4=> {
        //同步优惠券
        new Thread(() =>{executeMemberCoupon()}).start()
        //同步积分
        new Thread(()=>{executeScoreJournal()}).start()
        //同步礼品卡消费流水
        new Thread(()=>{executeCardConsume()}).start()
      }
      case 5 => {
        loadMemberCache()
      }
      case 6 => {
        //单独跑优惠券
        executeMemberCoupon()
      }
      case 7 => {
        //单独跑礼品卡流水
        executeCardConsume()
      }
      case _ => println(s"""命令错误，请确认需要 1:迁移会员主档   2：迁移会员卡   3：迁移订单   4：迁移优惠券  5：迁移积分  6：迁移礼品卡消费流水 """)
    }
  }

  def executeByCmd():Unit = {
    val sc = new Scanner(System.in)
    var flag = true
    while (flag){
      val cmd = sc.nextInt()
      cmd match {
        case 1 =>{
          //1、同步会员主档表信息(member)、会员余额表信息(member_card_account)
          clearData()
          executeMemberNew()
        }
        case 2 => executeMemberCard()    //2、同步会员卡
        case 3 => executeMemberOrder()   //3、同步订单
        case 4 => executeMemberCoupon()  //4、同步优惠券
        case 5 => executeScoreJournal()  //5、同步积分
        case 6 => executeCardConsume()   //6、同步礼品卡消费流水
        case -1 =>{
          println("程序结束")
          flag = flag
          System.exit(0)
        }
        case _ => println(s"""命令错误，请确认需要 1:迁移会员主档   2：迁移会员卡   3：迁移订单   4：迁移优惠券  5：迁移积分  6：迁移礼品卡消费流水 """)
      }
    }
  }

  def time[T](label: String)(p: => T): T = {
    val begin = System.currentTimeMillis();
    val v = p
    val end = System.currentTimeMillis()
    log.info(s"$label time: ${end - begin}ms")
    v
  }

  /**
    * 同步会员主档信息
    * @param member
    */
  def processMember(member: MemberBase): Unit = {

    val invocationCtx = InvocationContextImpl.Factory.currentInstance()
    invocationCtx.timeout(5000)
    val code = GenIdUtil.getId(GenIdUtil.MEMBER_CODE)
    //val code=999999
    try {
      //插入会员主档信息，并返回新的会员ID
      val newMemberId = MemberSql.createUser(member, code)
      //println("创建用户成功"+member.MemberId.toString+"----"+memberId.toString)

      //RedisDataSource.redisData.addSet(member.MemberId.toString, newMemberId.toString)
      RedisDataSource.redisData.setHSet(MEMBER_CACHE_PREFIX,member.MemberId.toString ,newMemberId.toString)

      //创建用户账户
      MemberSql.createMemberAccount(newMemberId, member.MemberName, member.LeftValue)

    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        log.info("创建用户失败，用户id为" + member.MemberId)
    }
    //log.info( Thread.currentThread().getName + " 成功创建一个用户用户id为 ")
  }

  val pool = new ForkJoinTaskSupport(new ForkJoinPool(15))

  def byPar2(list: List[MemberBase]): Unit = {
    val par = list.par
    par.tasksupport = pool
    val ids = par.map { member =>
      //      println(Thread.currentThread().getName)
      processMember(member)
      Thread.currentThread().getName
    }
  }

  def bySeq(list: List[MemberBase]): Unit = {
    list.foreach(processMember)
  }

  def byExecutor(list: List[MemberBase]): Unit = {
    val done = new AtomicInteger(0)
    list.foreach { member =>
      executor.execute(() => {
        processMember(member)
        done.incrementAndGet()
      })
    }
    while (done.get() != list.size) {
      Thread.sleep(100)
    }
    log.info("start为" + start)
  }

  def executeMemberNew():Unit = {
    log.info("开始迁移会员主档数据...")
    println("开始迁移会员主档数据...")

    val exec = {
      new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable](100),
        new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }

    val stmt = MemberDataSource.mysqlData.getConnection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
    stmt.setFetchSize(Integer.MIN_VALUE)
    val rs = stmt.executeQuery("SELECT * FROM member_member_base ")

    while (rs.next()){
      val row = ResultSetMapper.material[MemberBase].from(rs)
      exec.execute(() => {
        log.info(row.MemberId + "\t同步一条会员数据\t" + row.Mobile)
        try {
          val code = GenIdUtil.getId(GenIdUtil.MEMBER_CODE)
          //插入会员主档信息，并返回新的会员ID
          val newMemberId = MemberSql.createUser(row, code)
          //老的oldMemberId对应新的memberId ，对应关系放入redis缓存  hash结构
          RedisDataSource.redisData.setHSet(MEMBER_CACHE_PREFIX,row.MemberId.toString,newMemberId.toString)

          //创建用户账户
          MemberSql.createMemberAccount(newMemberId, row.MemberName, row.LeftValue)

          //插入会员同步日志表
          //MemberIncSyncLogSql.insert(newMemberId, row.MemberId, LogTypeEnum.MEMBER_MEMBER_BASE, defaultSourceEnum, SyncLogStatusEnum.SUCCESS, None)
        } catch{
          case ex: Exception =>
            ex.printStackTrace()
            log.info(row.MemberId + " \t同步会员失败\t" + row.Mobile)
        }
      })
    }

    //关闭线程池，但是不会立刻关闭，等待所有的任务执行完毕之后，才会执行
    exec.shutdown()
    while (true){
      if (exec.isTerminated()) {
        log.info("会员主档数据全部迁移完成")
        println("会员主档数据全部迁移完成")
        //执行完member然后执行 member_card
        System.exit(0)
        return
      }
      Thread.sleep(200);
    }
  }

  //@tailrec
  def executeMember():Unit = {
    val page = (start - 1) * rows
    log.info("开始迁移member数据,start为" + page)
    println("开始迁移会员主档数据,start为"+page)
    val list = MemberSql.findMemberList(page, rows)
    if (list.isEmpty) {
      log.info("会员主档数据全部迁移完成")
      println("会员主档数据全部迁移完成")
      start = 1
      //executeMemberAccountStream()
      //executeMemberConsumeLog()
      //执行完member然后执行 member_card
      //executeMemberCard()
    } else {
      //val memberCode = GenIdUtil.getIdByCount(GenIdUtil.MEMBER,rows)
      time(s"process batch $start") {
        // byExecutor(list)
        // byPar(list)
        byPar2(list)
        // bySeq(list)
      }
      start += 1
      executeMember()
    }
  }

  //创建优惠卷
  def executeMemberCoupon(): Unit = {
    log.info("开始迁移优惠券数据...")
    println("开始迁移优惠券数据...")
    MemberDataSource.mysqlData.withConnection(conn => {
      val stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
      stmt.setFetchSize(Integer.MIN_VALUE)
      val rs = stmt.executeQuery("select * from member_member_coupon where MemberId is not null")
      while (rs.next()) {
        val row = ResultSetMapper.material[MemberMemberCoupon].from(rs)

        val runnable: Runnable = () => {
          //log.info("创建一个优惠劵" + row.CouponId)
          try {
            //从redis缓存取出oldMemberId对应的newMemberId
            val newMemberStr = RedisDataSource.redisData.getHSet(MEMBER_CACHE_PREFIX,row.MemberId)
            val newMemberId = if(newMemberStr == null){
              log.warn(s"无会员ID关联的优惠券，ID=${row.Id} , CouponName=${row.CouponName}")
              0
            } else {
              newMemberStr.toLong
            }
            MemberSql.createMemberCoupon(row,newMemberId)

          } catch {
            case ex: Exception =>
              ex.printStackTrace()
              log.error(s"""创建优惠券失败, Id = ${row.Id} , CouponName = ${row.CouponName},exception ==> ${ex.getCause}""")
          }
        }
        executor.execute(runnable)
      }
    })
    log.info("优惠券数据迁移结束")
    println("优惠券数据迁移结束")
  }

  //同步用户积分流水
  def executeScoreJournal(): Unit = {

    log.info("开始迁移积分流水数据")
    println("开始迁移积分流水数据")

    val exec = {
      new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable](100),
        new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }

    val stmt = MemberDataSource.mysqlData.getConnection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
    stmt.setFetchSize(Integer.MIN_VALUE)
    val rs = stmt.executeQuery("select * from member_score_history ")
    while (rs.next()) {
      val row = ResultSetMapper.material[MemberScoreHistory].from(rs)
      exec.execute(() => {
        log.info(row.Score + "同步一条积分流水" + row.MemberId)
        try {
          //从redis缓存取出oldMemberId对应的newMemberId
          val newMemberStr = RedisDataSource.redisData.getHSet(MEMBER_CACHE_PREFIX,row.MemberId)

          val newMemberId = if(newMemberStr == null){
            log.warn(s"未关联会员ID的积分流水，ID=${row.ScoreId} , CouponName=${row.Score}")
            0
          } else {
            newMemberStr.toLong
          }

          //加强判断，orderId为空的情况
          val newOrderId =if(row.OrderId == null) 0 else {
            //从redis取出oldOrderId对应的newOrderId
            val orderIdString = RedisDataSource.redisData.getHSet(ORDER_CACHE_PREFIX,row.OrderId)
            if(orderIdString == null) 0 else orderIdString.toLong
          }
          MemberSql.createScoreJournal(row,newMemberId.toLong,newOrderId)
        } catch {
          case ex: Exception =>
            ex.printStackTrace()
            log.error(row.MemberId + "同步积分失败。。。。。。。。。" + row.Score)
        }
      })
    }

    //关闭线程池，但是不会立刻关闭，等待所有的任务执行完毕之后，才会执行
    exec.shutdown()
    while (true){
      if (exec.isTerminated()) {
        log.info("积分流水迁移结束")
        println("积分流水迁移结束")
        return
      }
      Thread.sleep(200);
    }
  }

  /**
    * 全量同步order数据
    */
  def executeMemberOrder():Unit = {
    log.info("开始迁移order数据")
    println("开始迁移order数据")

    val exec = {
      new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable](100),
        new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }

    val stmt = MemberDataSource.mysqlData.getConnection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
    stmt.setFetchSize(Integer.MIN_VALUE)
    //order_order_base => member_order
    val rs = stmt.executeQuery("select * from order_order_base ")
    while (rs.next()){
      val row = ResultSetMapper.material[OrderOrderBase].from(rs)
      exec.execute(() => {
        log.info(row.MemberId + "\t同步一条订单流水\t" + row.OrderNo)
        try {
          //从redis缓存中取出oldMemberId对应的newMemberId
          val newMemberStr = RedisDataSource.redisData.getHSet(MEMBER_CACHE_PREFIX,row.MemberId)

          val newMemberId = if(newMemberStr == null){
            log.warn(s"无会员ID的订单，ID=${row.OrderId} , OrderNo=${row.OrderNo}")
            "0"
          } else {
            newMemberStr
          }
          val newOrderId = MemberSql.createOrder(row,newMemberId)

          //老的orderId对应新的orderId ，对应关系放入redis缓存
          RedisDataSource.redisData.setHSet(ORDER_CACHE_PREFIX,row.OrderId,newOrderId.toString)
          //插入 member_inc_sync_log 表
        } catch{
          case ex: Exception =>
            ex.printStackTrace()
            log.info(row.MemberId + "\t创建订单流水失败\t" + row.OrderNo)
        }
      })
    }

    //关闭线程池，但是不会立刻关闭，等待所有的任务执行完毕之后，才会执行
    exec.shutdown()
    while (true){
      if (exec.isTerminated()) {
        log.info("order数据迁移结束")
        println("order数据迁移结束")
        System.exit(0)
        return
      }
      Thread.sleep(200);
    }
  }

  /**
    * 全量同步card数据
    */
  def executeMemberCard():Unit = {
    log.info("开始迁移memberCard数据")
    println("开始迁移memberCard数据")

    val exec = {
      new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable](100),
        new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }

    val stmt = MemberDataSource.mysqlData.getConnection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
    stmt.setFetchSize(Integer.MIN_VALUE)
    //card_card_base => card_base
    val rs = stmt.executeQuery("select * from card_card_base where MemberId is not null ")
    while (rs.next()){
      val row = ResultSetMapper.material[CardCardBase].from(rs)
      exec.execute(() => {
        log.info(row.MemberId + "\t同步一条card数据\t" + row.CardNo)
        try {
          //从redis缓存取出oldMemId对应的newMemberId
          val newMemberStr = RedisDataSource.redisData.getHSet(MEMBER_CACHE_PREFIX,row.MemberId)
          val newMemberId = if(newMemberStr == null){
            log.warn(s"会员卡未绑定用户，CardNo=${row.CardNo} , CardId=${row.CardId}")
            0
          } else {
            newMemberStr.toLong
          }

          val newCardId = MemberSql.createCardBaseNew(row, newMemberId, CardTypeEnum.NORMAL)

          //老的cardId对应新的cardId ，对应关系放入redis缓存  hash结构
          RedisDataSource.redisData.setHSet(CARD_CACHE_PREFIX,row.CardId,newCardId.toString)

          //只需要插入最后一条的记录即可  待优化
        } catch{
          case ex: Exception =>
            ex.printStackTrace()
            log.info(row.MemberId + " \t创建礼品卡失败\t" + row.CardNo)
        }
      })
    }
    //关闭线程池，但是不会立刻关闭，等待所有的任务执行完毕之后，才会执行
   exec.shutdown()
    while (true){
      if (exec.isTerminated()) {
        log.info("memberCard数据迁移结束")
        println("memberCard数据迁移结束")
        System.exit(0)
        return
      }
      Thread.sleep(200);
    }

  }

  /**
    * 全量同步会员流水记录
    */
  def executeMemberConsumeLog():Unit = {
    val stmt = MemberDataSource.mysqlData.getConnection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
    stmt.setFetchSize(Integer.MIN_VALUE)
    //member_consume_log => member_account_journal
    val rs = stmt.executeQuery("select * from member_consume_log ")
    while(rs.next()){
      val row = ResultSetMapper.material[MemberConsumeLog].from(rs)
      executor.execute(() => {
        log.info(row.MemberId + "\t同步一条Log数据\t" + row.ConsumeCode)
        try{
          //从redis缓出oldMemberId对应的newMemberId
          val newMemberStr = RedisDataSource.redisData.getHSet(MEMBER_CACHE_PREFIX,row.MemberId)
          val newMemberId = if(newMemberStr == null){
            log.warn(s"无会员ID关联的消费流水，ID=${row.ConsumeId} , ConsumeCode=${row.ConsumeCode}")
            0
          } else {
            newMemberStr.toLong
          }
          //从redis取出oldOrderId对应的newOrderId
          val orderIdString = RedisDataSource.redisData.getHSet(ORDER_CACHE_PREFIX,row.OrderId)
          val newOrderId = if(orderIdString == null) 0 else orderIdString.toLong
          MemberSql.createAccountJournal(row,newMemberId.toLong,newOrderId.toLong)

        } catch{
          case ex: Exception =>
            ex.printStackTrace()
            log.info(row.MemberId + " \t 同步会员消费流水失败 \t" + row.ConsumeCode)
        }
      })
    }
  }


  /**
    *同步礼品卡消费流水表
    */
  def executeCardConsume(): Unit ={
    log.info("开始迁移礼品卡消费流水数据...")
    println("开始迁移礼品卡消费流水数据...")

    val exec = {
      new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable](100),
        new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }

    val stmt = MemberDataSource.mysqlData.getConnection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
    stmt.setFetchSize(Integer.MIN_VALUE)
    //card_card_consume => card_consume
    val rs = stmt.executeQuery("select * from card_card_consume ")
    while (rs.next()){
      val row = ResultSetMapper.material[CardCardConsume].from(rs)
      exec.execute(() =>{
        log.info(row.MemberId + "\t同步一条礼品卡消费流水\t" + row.ConsumeCode)
        try{
          //从redis缓存取出oldMemId对应的newMemberId
          val newMemberStr = RedisDataSource.redisData.getHSet(MEMBER_CACHE_PREFIX,row.MemberId)
          val newMemberId = if(newMemberStr == null){
            log.warn(s"无会员ID关联的礼品卡消费流水，ID=${row.ConsumeId} , CardId=${row.CardId}")
            0
          } else {
            newMemberStr.toLong
          }
          //从redis缓存取出oldCardId对应的newCardId
          val newCardIdString = RedisDataSource.redisData.getHSet(CARD_CACHE_PREFIX,row.CardId)

          val newCardId = if(newCardIdString == null){
            log.warn(s"""mewCardId不存在，流水异常：对应流水的ConsumeId=${row.OrderId} , 老卡ID=${row.CardId}""")
            0
          }else {
            newCardIdString.toLong
          }
          //从redis取出oldOrderId对应的newOrderId
          val orderIdString = RedisDataSource.redisData.getHSet(ORDER_CACHE_PREFIX,row.OrderId)
          val newOrderId = if(orderIdString == null) 0 else orderIdString.toLong

          //插入表
          MemberSql.createCardConsume(
            newCardId,
            newOrderId,
            newMemberId,
            row
          )

        } catch {
          case ex: Exception =>
            ex.printStackTrace()
            log.info(row.MemberId + " \t 同步礼品卡消费流水 \t" + row.ConsumeCode)
        }
      })
    }

    //关闭线程池，但是不会立刻关闭，等待所有的任务执行完毕之后，才会执行
    exec.shutdown()
    while (true){
      if (exec.isTerminated()) {
        log.info("礼品卡消费流水数据迁移结束")
        println("礼品卡消费流水数据迁移结束")
        println("批量同步程序执行结束")
        return
      }
      Thread.sleep(200);
    }
  }

  /**
    * 危险操作慎用
    */
  def clearData():Unit = {
    //清除已有的数据，然后flush all redis缓存
    //MemberSql.cleanData()
    RedisUtil.flushData()
  }

  /**
    * 从数据库加载member到redis缓存
    */
  def loadMemberCache():Unit = {
    log.info("member缓存预加载")
    println("member缓存预加载...")

    val exec = {
      new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable](100),
        new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }

    val stmt = MemberDataSource.mysqlData.getConnection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
    stmt.setFetchSize(Integer.MIN_VALUE)
    val rs = stmt.executeQuery("SELECT * FROM member ")

    while (rs.next()){
      val row = ResultSetMapper.material[Member].from(rs)
      exec.execute(() => {
        log.info(row.id + "\t加载一条会员数据\t" + row.old_memberId)
        try {
          //老的oldMemberId对应新的newMemberId ，对应关系放入redis缓存  hash结构
          RedisDataSource.redisData.setHSet(MEMBER_CACHE_PREFIX,row.old_memberId.toString ,row.id.toString)

        } catch{
          case ex: Exception =>
            ex.printStackTrace()
            log.info(row.id + " \t记载会员失败\t" + row.old_memberId)
        }
      })
    }

    //关闭线程池，但是不会立刻关闭，等待所有的任务执行完毕之后，才会执行
    exec.shutdown()
    while (true){
      if (exec.isTerminated()) {
        log.info("member缓存预加载完成")
        println("member缓存预加载完成！")
        return
      }
      Thread.sleep(200);
    }
  }
}
