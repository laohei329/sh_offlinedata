package com.ssic.report

import java.io.{File, FileOutputStream}
import java.net.URI
import java.sql.Timestamp
import java.util.{Calendar, Date}

import com.ssic.service.ShanghaiWeekReport
import com.ssic.utils.Tools.{conn, edu_bd_department, url}
import com.ssic.utils.{Rule, Tools}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext
import org.apache.commons.lang3._
import org.apache.commons.lang3.time._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.spark.broadcast.Broadcast



object WeekToExcel {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val format1 = FastDateFormat.getInstance("yyyy")
  private val format2 = FastDateFormat.getInstance("yyyyMMddHHmmss")
  private val format3 = FastDateFormat.getInstance("M")
  private val format4 = FastDateFormat.getInstance("yyyyMMdd")

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")


    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val hiveContext: HiveContext = new HiveContext(sc)

    val sparkSession = sqlContext.sparkSession

    val bd_department = sparkSession.read.jdbc(url, edu_bd_department, conn)
    bd_department.createTempView("t_edu_bd_department")

    val departmentidToName: Broadcast[Map[String, String]] = sc.broadcast(Tools.departmentidToName(sparkSession))

    val calendar = Calendar.getInstance()
    calendar.setTime(new Date())
    calendar.add(Calendar.DAY_OF_MONTH, -7)
    val time = calendar.getTime
    val date = format.format(time)
    val year = format1.format(time)
    val month = format3.format(time)
    val datetime = format4.format(time)

    val calendar1 = Calendar.getInstance()
    calendar1.setTime(new Date())
    calendar1.add(Calendar.DAY_OF_MONTH, -1)
    val time1 = calendar1.getTime
    val datetime1 = format4.format(time1)
    val datetime2 = format.format(time1)

    val calendar2 = Calendar.getInstance()
    calendar2.setTime(new Date())
    calendar2.add(Calendar.DAY_OF_MONTH, 0)
    val time2 = calendar2.getTime
    val date2 = format.format(time2)
    val date3 = format4.format(time2)

    val edu_platoon_detail = hiveContext.sql(s"select * from app_saas_v1.app_t_edu_platoon_total_w where year ='${year}' and month ='${month}' and start_use_date ='${date}' ").rdd
      .map({
        row =>
          val department_id = row.getAs[String]("department_id")
          val department_name = departmentidToName.value.getOrElse(department_id, "null")
          val school_name = row.getAs[String]("school_name")
          val level_name = Rule.levelToName(row.getAs[String]("level_name"))
          val level_type = Rule.nullToEmpty(level_name.split("_")(0))
          val level_type_name = Rule.nullToEmpty(level_name.split("_")(1))
          val address = Rule.nullToEmpty(row.getAs[String]("address"))
          val food_safety_persion = Rule.nullToEmpty(row.getAs[String]("food_safety_persion"))
          val food_safety_mobilephone = Rule.nullToEmpty(row.getAs[String]("food_safety_mobilephone"))
          val have_class_total = row.getAs[Int]("have_class_total")
          val have_platoon_total = row.getAs[Int]("have_platoon_total")
          val have_no_platoon_total = row.getAs[Int]("have_no_platoon_total")

          (department_name, school_name, address, food_safety_persion, food_safety_mobilephone, level_type, level_type_name, have_class_total, have_platoon_total, have_no_platoon_total)
      })

    val ledgerData = hiveContext.sql(s"select * from app_saas_v1.app_t_edu_ledger_master_total_w where year ='${year}' and month ='${month}' and start_action_date ='${date}' ").rdd.map({
      row =>
        val department_id = row.getAs[String]("department_id")
        val department_name = departmentidToName.value.getOrElse(department_id, "null")
        val school_name = row.getAs[String]("school_name")
        val level_name = Rule.levelToName(row.getAs[String]("level_name"))
        val level_type = Rule.nullToEmpty(level_name.split("_")(0))
        val level_type_name = Rule.nullToEmpty(level_name.split("_")(1))
        val address = Rule.nullToEmpty(row.getAs[String]("address"))
        val food_safety_persion = Rule.nullToEmpty(row.getAs[String]("food_safety_persion"))
        val food_safety_mobilephone = Rule.nullToEmpty(row.getAs[String]("food_safety_mobilephone"))
        val ledger_day_total = row.getAs[Int]("ledger_day_total")
        val have_ledger_day_total = row.getAs[Int]("have_ledger_day_total")
        val have_no_ledger_day_total = row.getAs[Int]("have_no_ledger_day_total")

        (department_name, school_name, address, food_safety_persion, food_safety_mobilephone, level_type, level_type_name, ledger_day_total, have_ledger_day_total, have_no_ledger_day_total)
    })

    val licenseWarnData = hiveContext.sql(s"select * from app_saas_v1.app_t_edu_warn_detail where warn_type = 1 and target = 3 and warn_date >= '${date}' and warn_date <= '${datetime2}' ").rdd.map({
      row =>
        val department_id = row.getAs[String]("department_id")
        val department_name = departmentidToName.value.getOrElse(department_id, "null")
        val address = Rule.nullToEmpty(row.getAs[String]("address"))
        val food_safety_persion = Rule.nullToEmpty(row.getAs[String]("food_safety_persion"))
        val food_safety_mobilephone = Rule.nullToEmpty(row.getAs[String]("food_safety_mobilephone"))
        val school_name = row.getAs[String]("school_name")
        val level_name = Rule.levelToName(row.getAs[String]("level_name"))
        val level_type = Rule.nullToEmpty(level_name.split("_")(0))
        val level_type_name = Rule.nullToEmpty(level_name.split("_")(1))
        val warn_type_child = row.getAs[Integer]("warn_type_child")
        val lic_no = row.getAs[String]("lic_no")
        val lose_time = format.format(format.parse(row.getAs[Timestamp]("lose_time").toString))
        val warn_stat = row.getAs[Int]("warn_stat")
        val warn_date = row.getAs[String]("warn_date")
        val company_type = row.getAs[Integer]("company_type")
        val supplier_name = row.getAs[String]("supplier_name")
        val written_name = row.getAs[String]("written_name")
        var status = ""
        if (StringUtils.isEmpty(lose_time) && "null".equals(lose_time)) {
          status
        } else {
          if (((format.parse(lose_time).getTime - format.parse(date2).getTime) / (1000 * 3600 * 24)).toInt < 0) {
            status = "逾期"
          } else {
            status = "剩余" + ((format.parse(lose_time).getTime - format.parse(date2).getTime) / (1000 * 3600 * 24)).toInt + "天"
          }
        }


        (department_name, school_name, address, food_safety_persion, food_safety_mobilephone, level_type, level_type_name, warn_type_child, lic_no, lose_time, warn_stat, warn_date, company_type, supplier_name, written_name, status)
    })


    //排菜

    val qu_tuples = edu_platoon_detail.filter(x => "黄浦区教育局".equals(x._1) || "静安区教育局".equals(x._1) || "徐汇区教育局".equals(x._1) || "长宁区教育局".equals(x._1) || "普陀区教育局".equals(x._1) || "虹口区教育局".equals(x._1) || "杨浦区教育局".equals(x._1) || "闵行区教育局".equals(x._1) || "嘉定区教育局".equals(x._1) || "宝山区教育局".equals(x._1) || "浦东新区教育局".equals(x._1) || "松江区教育局".equals(x._1) || "金山区教育局".equals(x._1) || "青浦区教育局".equals(x._1) || "奉贤区教育局".equals(x._1) || "崇明区教育局".equals(x._1)).sortBy(x => (x._1, x._6)).collect()
    val you_platoon_detail = edu_platoon_detail.filter(x => "幼托".equals(x._6)).collect()
    val xiao_platoon_detail = edu_platoon_detail.filter(x => "中小学".equals(x._6)).collect()
    val qita_platoon_detail = edu_platoon_detail.filter(x => "其他".equals(x._6)).collect()
    val waiji_tuples = edu_platoon_detail.filter(x => "外籍人员子女学校".equals(x._1)).sortBy(x => (x._1, x._6)).collect()
    val zhongzhi_tuples = edu_platoon_detail.filter(x => "市属中职校".equals(x._1)).sortBy(x => (x._1, x._6)).collect()


    //"hdfs://172.16.10.17:9000/edu_week_report/shanghai/"+year+"年食安管理平台使用、验收及证照逾期处理情况_上海市_"+date1+"-"+date+"_"+date2+".xls"
    val filename = year + "年食安管理平台使用、验收及证照逾期处理情况_上海市_" + datetime + "__" + datetime1 + "__" + date3 + "000000" + ".xls"

    val file = new File("/data/" + filename)
    val workbook = new HSSFWorkbook()

    val sheet = workbook.createSheet("全市汇总情况（根据使用率、验收率、逾期处理率依次排序）")
    new ShanghaiWeekReport().shangtotal(sheet, edu_platoon_detail, workbook, ledgerData, date, datetime2, licenseWarnData, date2)


    //各区排菜统计（按区排序，再按学校分类排序）
    val sheet1 = workbook.createSheet("各区排菜统计（按区排序，再按学校分类排序）")
    new ShanghaiWeekReport().areaplatoonweek(sheet1, qu_tuples)

    //托幼排菜统计
    val sheet2 = workbook.createSheet("托幼排菜统计")
    new ShanghaiWeekReport().areaplatoonweek(sheet2, you_platoon_detail)

    //中小学排菜统计
    val sheet3 = workbook.createSheet("中小学排菜统计")
    new ShanghaiWeekReport().areaplatoonweek(sheet3, xiao_platoon_detail)

    //其他学校排菜统计
    val sheet4 = workbook.createSheet("其他学校排菜统计")
    new ShanghaiWeekReport().areaplatoonweek(sheet4, qita_platoon_detail)

    //外籍人员子女学校排菜统计
    val sheet5 = workbook.createSheet("外籍人员子女学校排菜统计")
    new ShanghaiWeekReport().areaplatoonweek(sheet5, waiji_tuples)

    //市属与行业中职校排菜统计（增加）
    val sheet6 = workbook.createSheet("市属与行业中职校排菜统计（增加）")
    new ShanghaiWeekReport().areaplatoonweek(sheet6, zhongzhi_tuples)


    //验收
    val qu_ledger_tuples = ledgerData.filter(x => "黄浦区教育局".equals(x._1) || "静安区教育局".equals(x._1) || "徐汇区教育局".equals(x._1) || "长宁区教育局".equals(x._1) || "普陀区教育局".equals(x._1) || "虹口区教育局".equals(x._1) || "杨浦区教育局".equals(x._1) || "闵行区教育局".equals(x._1) || "嘉定区教育局".equals(x._1) || "宝山区教育局".equals(x._1) || "浦东新区教育局".equals(x._1) || "松江区教育局".equals(x._1) || "金山区教育局".equals(x._1) || "青浦区教育局".equals(x._1) || "奉贤区教育局".equals(x._1) || "崇明区教育局".equals(x._1)).sortBy(x => (x._1, x._6)).collect()
    val you_ledger_detail = ledgerData.filter(x => "幼托".equals(x._6)).collect()
    val xiao_ledger_detail = ledgerData.filter(x => "中小学".equals(x._6)).collect()
    val qita_ledger_detail = ledgerData.filter(x => "其他".equals(x._6)).collect()
    val waiji_ledger_tuples = ledgerData.filter(x => "外籍人员子女学校".equals(x._1)).sortBy(x => (x._1, x._6)).collect()
    val zhongzhi_ledger_tuples = ledgerData.filter(x => "市属中职校".equals(x._1)).sortBy(x => (x._1, x._6)).collect()

    //各区验收统计（按区排序，再按学校分类排序）
    val sheet7 = workbook.createSheet("各区验收统计（按区排序，再按学校分类排序）")
    new ShanghaiWeekReport().arealedgerweek(sheet7, qu_ledger_tuples)

    //托幼验收统计
    val sheet8 = workbook.createSheet("托幼验收统计")
    new ShanghaiWeekReport().arealedgerweek(sheet8, you_ledger_detail)

    //中小学验收统计
    val sheet9 = workbook.createSheet("中小学验收统计")
    new ShanghaiWeekReport().arealedgerweek(sheet9, xiao_ledger_detail)

    //其他学校验收统计
    val sheet10 = workbook.createSheet("其他学校验收统计")
    new ShanghaiWeekReport().arealedgerweek(sheet10, qita_ledger_detail)

    //外籍人员子女学校验收统计
    val sheet11 = workbook.createSheet("外籍人员子女学校验收统计")
    new ShanghaiWeekReport().arealedgerweek(sheet11, waiji_ledger_tuples)

    //市属与行业中职校验收统计(增加)
    val sheet12 = workbook.createSheet("市属与行业中职校验收统计(增加)")
    new ShanghaiWeekReport().arealedgerweek(sheet12, zhongzhi_ledger_tuples)

    //证照预警
    val sheet13 = workbook.createSheet("未处理证照逾期学校名单")
    val licenseWarnDetail = licenseWarnData.filter(x => !"其他".equals(x._1)).filter(x => "逾期".equals(x._16)).filter(x => x._11 != 4).sortBy(x => (x._1, x._6)).collect()
    new ShanghaiWeekReport().arealicensewarnweek(sheet13, licenseWarnDetail)


    val stream = new FileOutputStream(file)

    workbook.write(stream)
    stream.close()

    val fileSystem = FileSystem.get(new URI("hdfs://172.18.14.30:8020/"), new Configuration())
    fileSystem.moveFromLocalFile(new Path("/data/"+filename),new Path("/edu_week_report/shanghai/"+filename))

    sc.stop()
  }

}
