webpackJsonp([17],{FGVP:function(e,t){},heu2:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var n=a("fZjL"),s=a.n(n),l=a("mvHQ"),i=a.n(l),r=a("Dd8w"),o=a.n(r),c=a("Cz8s"),p=a("+Ap0"),u=a("CjlA"),d=a("aV0S"),m=a("8YC6"),h=a("GEFk"),f=a("4giq"),g=a("t5DY"),y=a("NYxO"),b=void 0,v={data:function(){return{isCollapse:this.$store.state.ui.navIsOpen,distNameOptions:JSON.parse(localStorage.getItem("distList")),ppNameOptions:JSON.parse(localStorage.getItem("ppname")),schTypeOptions:JSON.parse(localStorage.getItem("schType")),dialogFormVisible:!1,loadingData:!0,exportLoading:!1,dateSelected:[],pageInfo:null,tableData:[],querys:{ppName:null,distNames:[],rsFlag:null,schTypes:[]},isCodes:[{name:"否",code:"0"},{name:"是",code:"1"}],page:1,pageSize:20,tableSelect:[],showSelect:!1,selectInit:[]}},directives:{permission:m.a},watch:{"$store.state.ui.navIsOpen":function(e,t){this.isCollapse=e},$router:"getParams"},beforeRouteEnter:function(e,t,a){b="/mealRetentionDetailList"===t.path,a()},created:function(){this.getParams(),this.selectBase(),this.userInterface()},methods:o()({},Object(y.b)(["exportTableData"]),{userInterface:function(){var e=this;g.a.getUserInterfaceList({interfaceName:"ppRetSamples"}).then(function(t){if(t.userColums){e.tableSelect=JSON.parse(t.userColums.columns);var a=[];e.tableSelect.map(function(e){a.push(e.key)}),e.selectInit.map(function(e){e.checked=!1;var t=function(t){a.map(function(a){e[t]===a&&(e.checked=!0)})};for(var n in e)t(n)}),e.$refs.selectMenu.selectArray=e.selectInit}}).catch(function(e){console.log(e)})},selectBase:function(){this.tableSelect=[{label:"就餐日期",key:"repastDate",width:120,checked:!0},{label:"所在地",key:"distName",checked:!0},{label:"学校学制",key:"schType",checked:!0},{label:"项目点名称",key:"ppName",width:250,checked:!0},{label:"地址",key:"detailAddr",width:250,checked:!0},{label:"项目联系人",key:"projContact",width:160,checked:!0},{label:"手机",key:"pcMobilePhone",width:120,checked:!0},{label:"是否留样",key:"rsFlag",width:120,checked:!0},{label:"菜品数量",key:"dishNum",checked:!0},{label:"已留样菜品",key:"rsDishNum",width:100,checked:!0},{label:"未留样菜品",key:"noRsDishNum",width:100,checked:!0}],this.selectInit=JSON.parse(i()(this.tableSelect))},search:function(){this.page=1,this.getTableData()},goBack:function(){b?this.$router.go(-2):this.$router.back()},getParams:function(){var e=this,t=this.$route.params;if(t.date&&(this.dateSelected=t.date),t.distName){var a=[];a.push(t.distName),this.querys.distNames=a}t.rsFlag&&(this.querys.rsFlag=t.rsFlag),this.$nextTick(function(t){e.getTableData()})},setParams:function(){var e=this,t={startSubDate:this.dateSelected&&this.dateSelected[0]&&this.dateSelected[0].replace(/\//g,"-"),endSubDate:this.dateSelected&&this.dateSelected[1]&&this.dateSelected[1].replace(/\//g,"-"),page:this.page,pageSize:this.pageSize};return s()(this.querys).forEach(function(a){""!==e.querys[a]&&(t[a]=e.querys[a])}),t},sentNotices:function(){this.dialogFormVisible=!0},todoMultiple:function(e){return s()(e).map(function(t){if(e[t]instanceof Array){var a=e[t];a.length>0&&a.map(function(e){e=String(e)}),e[t]=i()(e[t])}}),e},getTableData:function(){var e=this;this.loadingData=!0;var t=this.setParams();this.todoMultiple(t),f.a.getppRetSamples(t).then(function(t){e.tableData=t.ppRetSamples,e.pageInfo=t.pageInfo,e.loadingData=!1}).catch(function(e){console.log(e)})},handleSizeChange:function(e){this.pageSize=e,this.getTableData()},handleCurrentChange:function(e){this.page=e,this.getTableData()},exportData:function(){var e=this;this.exportLoading=!0;var t={url:"/biOptAnl/v1/expPpRetSamples",querys:this.setParams()};this.todoMultiple(t.querys),this.exportTableData(t).then(function(t){e.exportLoading=!1,e.$utils.downLoad(t.expPpRetSamples.expFileUrl)}).catch(function(e){console.log(e)})},showMenu:function(){this.showSelect?this.showSelect=!1:this.showSelect=!0},hideMenu:function(){this.showSelect=!1},selectData:function(e){var t=this,a={interfaceName:"ppRetSamples",columns:i()(e)};g.a.addUserInterface(a).then(function(a){t.tableSelect=e,t.hideMenu()}).catch(function(e){console.log(e)})}}),components:{Header:c.a,Breadcrumb:p.a,searchBox:u.a,SelectMenu:d.a,NoticeForm:h.a}},N={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{attrs:{id:"container"}},[a("Header"),e._v(" "),a("div",{staticClass:"main",class:{fullsize:1==e.isCollapse}},[a("Breadcrumb"),e._v(" "),a("div",{staticClass:"wrapperContent"},[a("searchBox",{ref:"searchBox",attrs:{model:e.querys},on:{search:e.search,exportData:e.exportData,showMenu:e.showMenu,hideMenu:e.hideMenu,goBack:e.goBack}},[[a("el-form",{staticClass:"search-form-inline",attrs:{inline:!0}},[a("el-form-item",[a("el-date-picker",{attrs:{type:"daterange","start-placeholder":"开始日期","end-placeholder":"结束日期","value-format":"yyyy-MM-dd"},model:{value:e.dateSelected,callback:function(t){e.dateSelected=t},expression:"dateSelected"}})],1),e._v(" "),a("el-form-item",[a("el-select",{attrs:{placeholder:"项目点名称",clearable:"",filterable:"","allow-create":""},model:{value:e.querys.ppName,callback:function(t){e.$set(e.querys,"ppName",t)},expression:"querys.ppName"}},e._l(e.ppNameOptions,function(e,t){return a("el-option",{key:e.code+t,attrs:{label:e.name,value:e.name}})}))],1),e._v(" "),a("el-form-item",[a("el-select",{attrs:{clearable:"",placeholder:"所在地",multiple:"","collapse-tags":""},model:{value:e.querys.distNames,callback:function(t){e.$set(e.querys,"distNames",t)},expression:"querys.distNames"}},e._l(e.distNameOptions,function(e){return a("el-option",{key:e.code,attrs:{label:e.name,value:e.code}})}))],1),e._v(" "),a("el-form-item",[a("el-select",{attrs:{clearable:"",filterable:"",placeholder:"是否留样"},model:{value:e.querys.rsFlag,callback:function(t){e.$set(e.querys,"rsFlag",t)},expression:"querys.rsFlag"}},e._l(e.isCodes,function(e){return a("el-option",{key:e.code,attrs:{label:e.name,value:e.code}})}))],1),e._v(" "),a("el-form-item",[a("el-select",{attrs:{placeholder:"学校学制",multiple:"","collapse-tags":"",clearable:"",filterable:""},model:{value:e.querys.schTypes,callback:function(t){e.$set(e.querys,"schTypes",t)},expression:"querys.schTypes"}},e._l(e.schTypeOptions,function(e){return a("el-option",{key:e.code,attrs:{label:e.name,value:e.code}})}))],1)],1),e._v(" "),a("el-button",{attrs:{slot:"other",type:"primary",plain:""},on:{click:e.sentNotices},slot:"other"},[a("i",{staticClass:"iconfont icon-send icon-1x"}),a("span",[e._v("发送")])])]],2),e._v(" "),a("select-menu",{directives:[{name:"show",rawName:"v-show",value:e.showSelect,expression:"showSelect"}],ref:"selectMenu",attrs:{generateData:e.selectInit},on:{selectData:e.selectData,onCancel:e.hideMenu}}),e._v(" "),a("el-table",{directives:[{name:"loading",rawName:"v-loading",value:e.loadingData,expression:"loadingData"}],attrs:{height:"calc(100% - 150px)",data:e.tableData,"element-loading-text":"加载中","element-loading-spinner":"el-icon-loading","element-loading-background":"rgba(255, 255, 255, 1)",stripe:""}},e._l(e.tableSelect,function(t,n){return a("el-table-column",{key:n,attrs:{"min-width":t.width,align:"center",label:t.label},scopedSlots:e._u([{key:"default",fn:function(n){return["是否留样"!==t.label&&"菜品数量"!==t.label&&"已留样菜品"!==t.label&&"未留样菜品"!==t.label?a("el-popover",{attrs:{trigger:"hover",placement:"top"}},[a("p",[e._v(e._s(n.row[t.key]))]),e._v(" "),a("div",{attrs:{slot:"reference"},slot:"reference"},[a("span",[e._v(e._s(n.row[t.key]))])])]):e._e(),e._v(" "),"是否留样"===t.label?a("span",{style:0===n.row.rsFlag?"color:red;":""},[e._v(e._s(0===n.row.rsFlag?"否":"是"))]):e._e(),e._v(" "),"菜品数量"===t.label?a("span",[a("router-link",{attrs:{to:{name:"mealRetentionDetailList",params:{distName:e.distNameOptions.filter(function(e){return e.name===n.row.distName})[0].code?e.distNameOptions.filter(function(e){return e.name===n.row.distName})[0].code:null,date:Array(2).fill(n.row.repastDate),schType:e.schTypeOptions.filter(function(e){return e.name===n.row.schType})[0]?e.schTypeOptions.filter(function(e){return e.name===n.row.schType})[0].code:null,ppName:e.ppNameOptions.filter(function(e){return e.name===n.row.ppName})[0].code?e.ppNameOptions.filter(function(e){return e.name===n.row.ppName})[0].code:null}}}},[e._v("\n                "+e._s(n.row.dishNum))])],1):e._e(),e._v(" "),"已留样菜品"===t.label?a("span",[a("router-link",{attrs:{to:{name:"mealRetentionDetailList",params:{distName:e.distNameOptions.filter(function(e){return e.name===n.row.distName})[0].code?e.distNameOptions.filter(function(e){return e.name===n.row.distName})[0].code:null,date:Array(2).fill(n.row.repastDate),schType:e.schTypeOptions.filter(function(e){return e.name===n.row.schType})[0]?e.schTypeOptions.filter(function(e){return e.name===n.row.schType})[0].code:null,ppName:e.ppNameOptions.filter(function(e){return e.name===n.row.ppName})[0].code?e.ppNameOptions.filter(function(e){return e.name===n.row.ppName})[0].code:null,rsFlag:"1"}}}},[e._v("\n                "+e._s(n.row.rsDishNum))])],1):e._e(),e._v(" "),"未留样菜品"===t.label?a("span",[a("router-link",{attrs:{to:{name:"mealRetentionDetailList",params:{distName:e.distNameOptions.filter(function(e){return e.name===n.row.distName})[0].code?e.distNameOptions.filter(function(e){return e.name===n.row.distName})[0].code:null,date:Array(2).fill(n.row.repastDate),schType:e.schTypeOptions.filter(function(e){return e.name===n.row.schType})[0]?e.schTypeOptions.filter(function(e){return e.name===n.row.schType})[0].code:null,ppName:e.ppNameOptions.filter(function(e){return e.name===n.row.ppName})[0].code?e.ppNameOptions.filter(function(e){return e.name===n.row.ppName})[0].code:null,rsFlag:"0"}}}},[e._v("\n                "+e._s(n.row.noRsDishNum))])],1):e._e()]}}])})})),e._v(" "),a("el-pagination",{attrs:{"current-page":e.pageInfo&&e.pageInfo.curPageNum,"page-sizes":[20,30,50,100],"page-size":20,layout:"total, sizes, prev, pager, next, jumper",total:e.pageInfo&&e.pageInfo.pageTotal},on:{"size-change":e.handleSizeChange,"current-change":e.handleCurrentChange}})],1)],1),e._v(" "),a("el-dialog",{attrs:{title:"发送未留样数据",visible:e.dialogFormVisible},on:{"update:visible":function(t){e.dialogFormVisible=t}}},[a("NoticeForm",{attrs:{dialog:!0},on:{closeDialog:function(t){e.dialogFormVisible=!1}}})],1)],1)},staticRenderFns:[]};var w=a("VU/8")(v,N,!1,function(e){a("FGVP")},"data-v-d6672068",null);t.default=w.exports}});