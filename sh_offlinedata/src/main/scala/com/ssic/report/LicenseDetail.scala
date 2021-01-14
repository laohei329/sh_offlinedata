package com.ssic.report

import java.sql.{Date, DriverManager}

import com.ssic.utils.{JPools, Rule, Tools}
import com.typesafe.config.ConfigFactory
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.hive.HiveContext

import scala.collection.JavaConverters._

/**
 * 清洗学校和供应学校的供应商数据，将清洗后的数据从hive转移到mysql中
 */

object LicenseDetail {
  private val format3 = FastDateFormat.getInstance("MM")
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("清洗学校和供应学校的供应商数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val hiveContext: HiveContext = new HiveContext(sc)

    val rdGroupSupplierDetail = config.getString("rd.default.groupSupplierDetail") //redis的团餐公司表名
    val rdSupplierDetail = config.getString("rd.default.supplierDetail")//redis的供应商公司表名

    //redis的团餐公司
    val jedis = JPools.getJedis
    val groupSupplierDetail = jedis.hgetAll(rdGroupSupplierDetail)
    val groupSupplierDetailData: RDD[(String, String)] = sc.parallelize(groupSupplierDetail.asScala.toList)
    //redis的供应商公司
    val supplierDetail = jedis.hgetAll(rdSupplierDetail)
    val supplierDetailData: RDD[(String, String)] = sc.parallelize(supplierDetail.asScala.toList)



    //学校的自营，外包字段数据从hive中更新到mysql中
    hiveContext.sql("select id,license_main_type,license_main_child from app_saas_v1.t_edu_school_new where stat=1 and reviewed =1 and is_deleted =0 ")
      .rdd.map({
      row =>
        val id = row.getAs[String]("id")
        val license_main_type = Rule.emptyToNull(row.getAs[String]("license_main_type"))
        val license_main_child = row.getAs[Integer]("license_main_child")


        (id, license_main_type, license_main_child)
    }).foreachPartition({
      itr =>
        val conn = Tools.open
        itr.foreach({
          x =>
            val statement = conn.prepareStatement(
              s"""
                 |update saas_v1.t_edu_school
                 |set license_main_type =?,
                 |license_main_child =?
                 |where id = '${x._1}'
             """.stripMargin)
            statement.setString(1, x._2)
            statement.setObject(2, x._3)
            statement.execute()
        })
        conn.close()
    })

    //供应商数据按照统一信用代码去重写到redis中
    /**
     * 原来的数据sql 方法
     */

/*    hiveContext.sql(
      """
       |select temp.supplierId as supplierId,temp.supplier_name
        |as supplier_name,temp.supplier_classify as supplier_classify ,temp.address as address,temp.business_license,temp.shipin_jinyin_no,temp.shipin_liutong_no,temp.shipin_shengchang_no
        |from (
        |select a.id as supplierId,a.supplier_name as supplier_name,a.supplier_classify as supplier_classify,a.address as address,a.business_license as business_license,ROW_NUMBER() over(PARTITION BY a.business_license ORDER BY a.id) as rn ,c.lic_no as shipin_jinyin_no,d.lic_no as shipin_liutong_no,e.lic_no as shipin_shengchang_no
        |from
        |(select id ,supplier_name,business_license,supplier_classify,address from app_saas_v1.t_pro_supplier where supplier_type =2 and business_license is not null ) as a
        |left outer join
        |(select * from app_saas_v1.supplier_tab where tab[0] = 1) as b
        |on a.id = b.supplier_id
        |left outer join
        |(select supplier_id,lic_no from app_saas_v1.t_pro_license where lic_type = 1 and stat=1 and reviewed =1) as c
        |on a.id = c.supplier_id
        |left outer join
        |(select supplier_id,lic_no from app_saas_v1.t_pro_license where lic_type = 2 and stat=1 and reviewed =1) as d
        |on a.id = d.supplier_id
        |left outer join
        |(select supplier_id,lic_no from app_saas_v1.t_pro_license where lic_type = 3 and stat=1 and reviewed =1) as e
        |on a.id = e.supplier_id where b.tab is not null) as temp where temp.rn =1
        | union all
        | select a.id as supplierId,a.supplier_name as supplier_name,0 as supplier_classify ,a.address as address,d.qualification_code as business_license,e.qualification_code as shipin_jinyin_no,null as shipin_liutong_no,f.qualification_code as shipin_shengchang_no
        |from
        |(select id ,supplier_name,business_license,address from app_saas_v1.t_pro_supplier where supplier_type is null ) as a
        |left outer join
        |(select * from app_saas_v1.supplier_tab where tab[0] = 1) as b
        |on a.id = b.supplier_id
        |left outer join
        |(select * from ods_b2b_new.merchant_apply ) as c
        |on a.id = c.merchant_id
        |left outer join
        |(select * from
        |(select id,qualification_name,qualification_code,merchant_apply_id,start_effective_date,end_effective_date,ROW_NUMBER() over(PARTITION BY merchant_apply_id,qualification_name ORDER BY id) as rn from ods_b2b_new.merchant_apply_qualification where del=0 and  qualification_name="营业执照") as temp where temp.rn =1) as d
        |on c.id = d.merchant_apply_id
        |left outer join
        |(select * from
        |(select id,qualification_name,qualification_code,merchant_apply_id,start_effective_date,end_effective_date,ROW_NUMBER() over(PARTITION BY merchant_apply_id,qualification_name ORDER BY id) as rn from ods_b2b_new.merchant_apply_qualification where del=0 and  qualification_name="食品经营许可证") as temp where temp.rn =1) as e
        |on c.id = e.merchant_apply_id
        |left outer join
        |(select * from
        |(select id,qualification_name,qualification_code,merchant_apply_id,start_effective_date,end_effective_date,ROW_NUMBER() over(PARTITION BY merchant_apply_id,qualification_name ORDER BY id) as rn from ods_b2b_new.merchant_apply_qualification where del=0 and  qualification_name="食品生产许可证") as temp where temp.rn =1) as f
        |on c.id = f.merchant_apply_id where b.tab is not null
        |
      """.stripMargin).rdd
      .map({
        row =>
          val supplierId = Rule.emptyToNull(row.getAs[String]("supplierId"))
          val supplier_name = Rule.emptyToNull(row.getAs("supplier_name"))
          val supplier_classify = Rule.intToString(row.getAs[Integer]("supplier_classify"))
          val address = Rule.emptyToNull(row.getAs[String]("address"))
          val business_license = Rule.emptyToNull(row.getAs[String]("business_license"))
          val shipin_jinyin_no = Rule.emptyToNull(row.getAs[String]("shipin_jinyin_no"))
          val shipin_liutong_no = Rule.emptyToNull(row.getAs[String]("shipin_liutong_no"))
          val shipin_shengchang_no = Rule.emptyToNull(row.getAs[String]("shipin_shengchang_no"))
          (supplierId,(supplier_name,supplier_classify,address,business_license,shipin_jinyin_no,shipin_liutong_no,shipin_shengchang_no))
      }).cogroup(supplierDetailData)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hdel("supplier-detail", k)
              }else{
                jedis.hset("supplier-detail",k,
                  "id"+";"+k+";suppliername;"+v._1.head._1+";classify;"+v._1.head._2+";area;null;address;"
                    +v._1.head._3+";businesslicense;"+v._1.head._4+";regcapital;null;foodbusiness;"+v._1.head._5
                    +";foodcirculation;"+v._1.head._6+";foodproduce;"+v._1.head._7)
              }
          })
      })*/

    hiveContext.sql(
      """
        |select
        |temp.supplierId as supplierId,
        |temp.supplier_name as supplier_name,
        |temp.supplier_classify as supplier_classify ,
        |temp.address as address,
        |temp.business_license,
        |temp.shipin_jinyin_no,
        |temp.shipin_liutong_no,
        |temp.shipin_shengchang_no
        |from (
        |select
        |a.id as supplierId,
        |a.supplier_name as supplier_name,
        |a.supplier_classify as supplier_classify,
        |a.address as address,
        |a.business_license as business_license,
        |ROW_NUMBER() over(PARTITION BY a.business_license ORDER BY a.id) as rn ,
        |c.lic_no as shipin_jinyin_no,
        |d.lic_no as shipin_liutong_no,
        |e.lic_no as shipin_shengchang_no
        |from
        |(select id ,supplier_name,business_license,supplier_classify,address
        |	from app_saas_v1.t_pro_supplier
        |	where supplier_type =2 and business_license is not null and trim(business_license) <> "") as a
        |left outer join
        |(select * from app_saas_v1.supplier_tab where tab[0] = 1) as b
        |on a.id = b.supplier_id
        |left outer join
        |(select supplier_id,lic_no
        |	from ods.ods_saas_v1_db_t_pro_license
        |	where lic_type = 1 and stat=1 and reviewed =1) as c
        |on a.id = c.supplier_id
        |left outer join
        |(select supplier_id,lic_no
        |	from ods.ods_saas_v1_db_t_pro_license
        |	where lic_type = 2 and stat=1 and reviewed =1) as d
        |on a.id = d.supplier_id
        |left outer join
        |(select supplier_id,lic_no
        |	from ods.ods_saas_v1_db_t_pro_license
        |	where lic_type = 3 and stat=1 and reviewed =1) as e
        |on a.id = e.supplier_id where b.tab is not null
        |) as temp where temp.rn =1
        |
        | union all
        | select
        | a.id as supplierId,
        | a.supplier_name as supplier_name,
        | 0 as supplier_classify ,
        | a.address as address,
        | d.qualification_code as business_license,
        | e.qualification_code as shipin_jinyin_no,
        | null as shipin_liutong_no,
        | f.qualification_code as shipin_shengchang_no
        |from
        |(select id ,supplier_name,business_license,address
        |	from app_saas_v1.t_pro_supplier
        |	where supplier_type is null ) as a
        |left outer join
        |(select *
        |	from app_saas_v1.supplier_tab
        |	where tab[0] = 1) as b
        |on a.id = b.supplier_id
        |left outer join
        |(select * from ods_b2b_new.merchant_apply ) as c
        |on a.id = c.merchant_id
        |left outer join
        |(select * from
        |(select id,qualification_name,qualification_code,
        |	merchant_apply_id,start_effective_date,
        |	end_effective_date,ROW_NUMBER() over(PARTITION BY merchant_apply_id,qualification_name ORDER BY id) as rn
        |	from ods_b2b_new.merchant_apply_qualification
        |	where del=0 and  qualification_name="营业执照") as temp
        |where temp.rn =1) as d
        |on c.id = d.merchant_apply_id
        |left outer join
        |(select * from
        |(select id,qualification_name,qualification_code,
        |	merchant_apply_id,start_effective_date,end_effective_date,
        |	ROW_NUMBER() over(PARTITION BY merchant_apply_id,qualification_name ORDER BY id) as rn
        |	from ods_b2b_new.merchant_apply_qualification
        |	where del=0 and  qualification_name="食品经营许可证") as temp where temp.rn =1) as e
        |on c.id = e.merchant_apply_id
        |left outer join
        |(select * from
        |(select id,qualification_name,qualification_code,
        |	merchant_apply_id,start_effective_date,
        |	end_effective_date,ROW_NUMBER() over(PARTITION BY merchant_apply_id,qualification_name ORDER BY id) as rn
        |	from ods_b2b_new.merchant_apply_qualification
        |	where del=0 and  qualification_name="食品生产许可证") as temp
        |where temp.rn =1) as f
        |on c.id = f.merchant_apply_id where b.tab is not null
        |
      """.stripMargin).rdd
      .map({
        row =>
          val supplierId = Rule.emptyToNull(row.getAs[String]("supplierId"))
          val supplier_name = Rule.emptyToNull(row.getAs("supplier_name"))
          val supplier_classify = Rule.intToString(row.getAs[Integer]("supplier_classify"))
          val address = Rule.emptyToNull(row.getAs[String]("address"))
          val business_license = Rule.emptyToNull(row.getAs[String]("business_license"))
          val shipin_jinyin_no = Rule.emptyToNull(row.getAs[String]("shipin_jinyin_no"))
          val shipin_liutong_no = Rule.emptyToNull(row.getAs[String]("shipin_liutong_no"))
          val shipin_shengchang_no = Rule.emptyToNull(row.getAs[String]("shipin_shengchang_no"))
          (supplierId,(supplier_name,supplier_classify,address,business_license,shipin_jinyin_no,shipin_liutong_no,shipin_shengchang_no))
      }).cogroup(supplierDetailData)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hdel("supplier-detail", k)
              }else{
                jedis.hset("supplier-detail",k,
                  "id"+";"+k+";suppliername;"+v._1.head._1+";classify;"+v._1.head._2+";area;null;address;"
                    +v._1.head._3+";businesslicense;"+v._1.head._4+";regcapital;null;foodbusiness;"+v._1.head._5
                    +";foodcirculation;"+v._1.head._6+";foodproduce;"+v._1.head._7)
              }
          })
      })
    /**
     * 先尝试写入到test 的 redis中
     */
/*    hiveContext.sql(
      """
 select
        |temp.supplierId as supplierId,
        |temp.supplier_name as supplier_name,
        |temp.supplier_classify as supplier_classify ,
        |temp.address as address,
        |temp.business_license,
        |temp.shipin_jinyin_no,
        |temp.shipin_liutong_no,
        |temp.shipin_shengchang_no
        |from (
        |select
        |a.id as supplierId,
        |a.supplier_name as supplier_name,
        |a.supplier_classify as supplier_classify,
        |a.address as address,
        |a.business_license as business_license,
        |ROW_NUMBER() over(PARTITION BY a.business_license ORDER BY a.id) as rn ,
        |c.lic_no as shipin_jinyin_no,
        |d.lic_no as shipin_liutong_no,
        |e.lic_no as shipin_shengchang_no
        |from
        |(select id ,supplier_name,business_license,supplier_classify,address
        |	from app_saas_v1.t_pro_supplier
        |	where supplier_type =2 and business_license is not null ) as a
        |left outer join
        |(select * from app_saas_v1.supplier_tab where tab[0] = 1) as b
        |on a.id = b.supplier_id
        |left outer join
        |(select supplier_id,lic_no
        |	from ods.ods_saas_v1_db_t_pro_license
        |	where lic_type = 1 and stat=1 ) as c
        |on a.id = c.supplier_id
        |left outer join
        |(select supplier_id,lic_no
        |	from ods.ods_saas_v1_db_t_pro_license
        |	where lic_type = 2 and stat=1 ) as d
        |on a.id = d.supplier_id
        |left outer join
        |(select supplier_id,lic_no
        |	from ods.ods_saas_v1_db_t_pro_license
        |	where lic_type = 3 and stat=1 ) as e
        |on a.id = e.supplier_id where b.tab is not null
        |) as temp where temp.rn =1
        | union all
        | select
        | a.id as supplierId,
        | a.supplier_name as supplier_name,
        | 0 as supplier_classify ,
        | a.address as address,
        | d.qualification_code as business_license,
        | e.qualification_code as shipin_jinyin_no,
        | null as shipin_liutong_no,
        | f.qualification_code as shipin_shengchang_no
        |from
        |(select id ,supplier_name,business_license,address
        |	from app_saas_v1.t_pro_supplier
        |	where supplier_type is null ) as a
        |left outer join
        |(select *
        |	from app_saas_v1.supplier_tab
        |	where tab[0] = 1) as b
        |on a.id = b.supplier_id
        |left outer join
        |(select * from ods_b2b_new.merchant_apply ) as c
        |on a.id = c.merchant_id
        |left outer join
        |(select * from
        |(select id,qualification_name,qualification_code,
        |	merchant_apply_id,start_effective_date,
        |	end_effective_date,ROW_NUMBER() over(PARTITION BY merchant_apply_id,qualification_name ORDER BY id) as rn
        |	from ods_b2b_new.merchant_apply_qualification
        |	where del=0 and  qualification_name="营业执照") as temp
        |where temp.rn =1) as d
        |on c.id = d.merchant_apply_id
        |left outer join
        |(select * from
        |(select id,qualification_name,qualification_code,
        |	merchant_apply_id,start_effective_date,end_effective_date,
        |	ROW_NUMBER() over(PARTITION BY merchant_apply_id,qualification_name ORDER BY id) as rn
        |	from ods_b2b_new.merchant_apply_qualification
        |	where del=0 and  qualification_name="食品经营许可证") as temp where temp.rn =1) as e
        |on c.id = e.merchant_apply_id
        |left outer join
        |(select * from
        |(select id,qualification_name,qualification_code,
        |	merchant_apply_id,start_effective_date,
        |	end_effective_date,ROW_NUMBER() over(PARTITION BY merchant_apply_id,qualification_name ORDER BY id) as rn
        |	from ods_b2b_new.merchant_apply_qualification
        |	where del=0 and  qualification_name="食品生产许可证") as temp
        |where temp.rn =1) as f
        |on c.id = f.merchant_apply_id where b.tab is not null
        |
      """.stripMargin).rdd
      .map({
        row =>
          val supplierId = Rule.emptyToNull(row.getAs[String]("supplierId"))
          val supplier_name = Rule.emptyToNull(row.getAs("supplier_name"))
          val supplier_classify = Rule.intToString(row.getAs[Integer]("supplier_classify"))
          val address = Rule.emptyToNull(row.getAs[String]("address"))
          val business_license = Rule.emptyToNull(row.getAs[String]("business_license"))
          val shipin_jinyin_no = Rule.emptyToNull(row.getAs[String]("shipin_jinyin_no"))
          val shipin_liutong_no = Rule.emptyToNull(row.getAs[String]("shipin_liutong_no"))
          val shipin_shengchang_no = Rule.emptyToNull(row.getAs[String]("shipin_shengchang_no"))
          (supplierId,(supplier_name,supplier_classify,address,business_license,shipin_jinyin_no,shipin_liutong_no,shipin_shengchang_no))
      }).cogroup(supplierDetailData)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hdel("supplier-detail_test", k)
              }else{
                jedis.hset("supplier-detail_test",k,"id"+";"+k+";"+"suppliername"+";"+v._1.head._1+";"+"classify"+";"+v._1.head._2+";"+"area"+";"+"null"+";"+"address"+";"+v._1.head._3+";"+"businesslicense"+";"+v._1.head._4+";"+"regcapital"+";"+"null"+";"+"foodbusiness"+";"+v._1.head._5+";"+"foodcirculation"+";"+v._1.head._6+";"+"foodproduce"+";"+v._1.head._7)
              }

          })
      })*/

    sc.stop()
  }

}
