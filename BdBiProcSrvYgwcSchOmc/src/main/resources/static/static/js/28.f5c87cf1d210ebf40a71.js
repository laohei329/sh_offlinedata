webpackJsonp([28],{M02Z:function(t,a,e){"use strict";Object.defineProperty(a,"__esModule",{value:!0});var i=e("Dd8w"),n=e.n(i),s=e("Cz8s"),o=e("+Ap0"),l=e("8YC6"),c=e("NYxO"),r={data:function(){return{isCollapse:this.$store.state.ui.navIsOpen,loadingData:!0,dateSelected:[],pageInfo:null,tableData:[],querys:{vidName:null}}},directives:{permission:l.a},watch:{"$store.state.ui.navIsOpen":function(t,a){this.isCollapse=t}},created:function(){this.getTableData()},methods:n()({},Object(c.b)(["geteEduVideoList"]),{getTableData:function(){var t=this;this.loadingData=!0;var a={vidName:this.querys.vidName};this.geteEduVideoList(a).then(function(a){t.tableData=a.etVidLib,t.pageInfo=a.pageInfo,t.loadingData=!1}).catch(function(t){console.log(t)})},handleSizeChange:function(t){this.pageSize=t,this.getTableData()},handleCurrentChange:function(t){this.page=t,this.getTableData()},playPause:function(t){var a=document.getElementById("video-"+t),e=document.getElementById("video-shadow-"+t);a.paused&&(a.play(),e.style.display="none"),a.removeEventListener("ended",function(){e.style.display="block"}),a.addEventListener("ended",function(){e.style.display="block"})}}),components:{Header:s.a,Breadcrumb:o.a}},d={render:function(){var t=this,a=t.$createElement,e=t._self._c||a;return e("div",{attrs:{id:"container"}},[e("Header"),t._v(" "),e("div",{staticClass:"main",class:{fullsize:1==t.isCollapse}},[e("Breadcrumb"),t._v(" "),e("div",{staticClass:"wrapperContent"},[e("div",{staticClass:"content-item"},[e("el-form",{staticClass:"search-item-content",attrs:{inline:!0}},[e("el-form-item",{staticStyle:{"margin-bottom":"0"},attrs:{label:"检索："}},[e("el-input",{attrs:{placeholder:"输入视频名称"},model:{value:t.querys.vidName,callback:function(a){t.$set(t.querys,"vidName",a)},expression:"querys.vidName"}})],1),t._v(" "),e("el-form-item",{staticStyle:{"margin-bottom":"0"}},[e("el-button",{attrs:{type:"primary"},on:{click:function(a){(t.page=1)&&t.getTableData()}}},[e("i",{staticClass:"iconfont icon-search icon-1x"}),e("span",[t._v("查询")])])],1)],1),t._v(" "),e("div",{staticClass:"search-item-content"},[e("span",[t._v("分类：")]),t._v(" "),e("a",{attrs:{href:"javascript:;"},on:{click:function(a){t.getTableData()}}},[t._v("全部")]),t._v(" "),e("a",{attrs:{href:"javascript:;"},on:{click:function(a){t.getTableData()}}},[t._v("系统操作")]),t._v(" "),e("a",{attrs:{href:"javascript:;"},on:{click:function(a){t.getTableData()}}},[t._v("食品安全")]),t._v(" "),e("a",{attrs:{href:"javascript:;"},on:{click:function(a){t.getTableData()}}},[t._v("政策法规")])]),t._v(" "),e("div",{staticClass:"search-item-content"},[e("span",[t._v("排序：")]),t._v(" "),e("a",{attrs:{href:"javascript:;"},on:{click:function(a){t.getTableData()}}},[t._v("按播放次数"),e("i",{staticClass:"iconfont icon-sort-down icon-distance"})]),t._v(" "),e("a",{attrs:{href:"javascript:;"},on:{click:function(a){t.getTableData()}}},[t._v("按上传时间"),e("i",{staticClass:"iconfont icon-sort-up icon-distance"})]),t._v(" "),e("a",{attrs:{href:"javascript:;"},on:{click:function(a){t.getTableData()}}},[t._v("按好评率"),e("i",{staticClass:"iconfont icon-sort-down icon-distance"})])])],1),t._v(" "),e("div",{staticClass:"content-item",staticStyle:{height:"calc(100% - 250px)",overflow:"hidden auto","margin-bottom":"0",padding:"30px 0 0"}},[e("div",{staticClass:"video-list-container"},[e("ul",{directives:[{name:"loading",rawName:"v-loading",value:t.loadingData,expression:"loadingData"}],staticClass:"video-list",attrs:{"element-loading-text":"加载中","element-loading-spinner":"el-icon-loading","element-loading-background":"rgba(255, 255, 255, 0.8)"}},t._l(t.tableData,function(a){return e("li",[e("div",{staticClass:"grid-content bg-purple video-item"},[e("span",{staticClass:"video-shadow",attrs:{id:"video-shadow-"+a.vidId}},[e("span",{on:{click:function(e){t.playPause(a.vidId)}}},[e("i",{staticClass:"iconfont icon-play"})])]),t._v(" "),e("video",{attrs:{id:"video-"+a.vidId,src:a.vidUrl,width:"100%",height:"119",controls:"controls",controlsList:"nodownload"}}),t._v(" "),e("p",{staticClass:"video-title",attrs:{title:a.vidName}},[t._v(t._s(a.vidName))]),t._v(" "),e("p",{staticStyle:{color:"#a3a3a3","margin-bottom":"18px"}},[t._v("轻松上手，为你解忧")]),t._v(" "),e("ul",[e("li",[t._v(t._s(a.playCount)+"次播放")]),t._v(" "),e("li",[t._v(t._s(a.likeCount)+"人赞")])])])])}))])]),t._v(" "),e("el-pagination",{attrs:{"current-page":t.pageInfo&&t.pageInfo.curPageNum,"page-sizes":[20,30,50,100],"page-size":20,layout:"total, sizes, prev, pager, next, jumper",total:t.pageInfo&&t.pageInfo.pageTotal},on:{"size-change":t.handleSizeChange,"current-change":t.handleCurrentChange}})],1)],1)],1)},staticRenderFns:[]};var v=e("VU/8")(r,d,!1,function(t){e("zBbe")},"data-v-59408d1e",null);a.default=v.exports},zBbe:function(t,a){}});