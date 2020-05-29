package com.ssic.report

import com.ssic.utils.{JPools, Rule}
import com.ssic.utils.Tools.{conn, edu_bd_pro_license, url}
import org.apache.commons.lang3._
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import scala.collection.JavaConverters._

object LicenseDetail {
  private val format3 = FastDateFormat.getInstance("MM")
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession


    //    val pro_reserve_sample = session.read.jdbc(url, pro_reserve_sample, conn)
    //    pro_reserve_sample.createTempView("t_pro_reserve_sample")
    //    sqlContext.sql("select * from t_pro_reserve_sample where stat =1 and supply_date = '2020-01-16' ").rdd.map({
    //      row =>
    //        val supply_date = row.getAs[Timestamp]("supply_date")
    //
    //    })

    val pro_license = session.read.jdbc(url, edu_bd_pro_license, conn)

    pro_license.createTempView("t_pro_license")

//    val jedis = JPools.getJedis
//    val schoolDetail = jedis.hgetAll("supplier-detail")
//    val schoolDetailaData = sc.parallelize(schoolDetail.asScala.toList)

    session.sql("select * from t_pro_license where stat=1  ").rdd.map({
      row =>
        //id;0b43b2ab-e871-4baa-8fbc-49f2ed5f2fed;suppliername;上海萨莉亚餐饮有限公司槎溪路店;classify;2;area;null;address;上海市嘉定区槎溪路地下一层788弄3号26���地;businesslicense;null;regcapital;null;foodbusiness;null;foodcirculation;null;foodproduce;null
        val id = row.getAs[String]("id")
        val relation_id = row.getAs[String]("relation_id")
        val lic_type = row.getAs[Int]("lic_type")
        val lic_no =Rule.emptyToNull( row.getAs[String]("lic_no"))

        (relation_id,(lic_type, lic_no))

    }).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            val v = jedis.hget("supplier-detail",x._1 )
            if (StringUtils.isNoneEmpty(v) && !"null".equals(v)) {
              if(x._2._1 == 4){
                jedis.hset("supplier-detail", x._1, v.split("businesslicense")(0)+"businesslicense"+";"+x._2._2+";"+"regcapital"+v.split("regcapital")(1))
              }else if(x._2._1 == 1){
                jedis.hset("supplier-detail", x._1, v.split("foodbusiness")(0)+"foodbusiness"+";"+x._2._2+";"+"foodcirculation"+v.split("foodcirculation")(1))
              }

            }

        })
    })



    sc.stop()
  }

}
