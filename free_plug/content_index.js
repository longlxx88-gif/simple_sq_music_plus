

var pushtoken;
var pushurl;

var musicname = "";
var musicartistName = "";
var musicalbumName = "";


//   // 手动添加mate标签
//   const addMeta = (name, content) => {
//     const meta = document.createElement('meta');
//     meta.content = content;
//     meta.httpEquiv = name;
//     document.getElementsByTagName('head')[0].appendChild(meta);
//   };

//   addMeta("Content-Security-Policy", "upgrade-insecure-requests")




chrome.storage.local.get(['sqmusicextoken'], function(result) {
    pushtoken = result.sqmusicextoken
  });
  chrome.storage.local.get(['sqmusicexurl'], function(result) {
    pushurl = result.sqmusicexurl
  });

var t2 = window.setInterval(core,1000)

function core(){
    var mb = document.querySelector("body > div.arco-overlay.arco-overlay-modal > div > div.arco-modal-wrapper.arco-modal-wrapper-align-center > div")
    if (mb!=null){
        var adddiv = document.querySelector("body > div.arco-overlay.arco-overlay-modal > div > div.arco-modal-wrapper.arco-modal-wrapper-align-center > div > div.arco-modal-body > div");

    var musicname = document.querySelector("body > div.arco-overlay.arco-overlay-modal > div > div.arco-modal-wrapper.arco-modal-wrapper-align-center > div > div.arco-modal-body > div > input").value
    var musicimage; 
    var musiclyrc; 
    if(document.getElementById(musicname)!=null){

                return;
        }
        var musictag =   adddiv.getElementsByTagName("div")
        for(var v = 0; v <  musictag.length; v++){
          let dsadsa =   musictag[v].getElementsByTagName("span")[0];
          if(dsadsa!=null){
            if(dsadsa.innerText.includes("封面")){
                musicimage = musictag[v].getElementsByTagName("a")[0].href
            }
            if(dsadsa.innerText.includes("歌词")){
                musiclyrc = musictag[v].getElementsByTagName("a")[0].href
            }
          }
        }
 
    var downloadurls = adddiv.getElementsByClassName("arco-space arco-space-horizontal arco-space-align-center");
   var fornood = downloadurls[0].children
   var pushdiv = document.createElement("div");
   for(var i = 0; i <  fornood.length; i++){
   let url =  fornood[i].getElementsByTagName('a')[0].href
    let type = fornood[i].getElementsByTagName('a')[0].innerText
    console.log('type:'+type)
    console.log('url:'+url)
    var adda = document.createElement('a');

    adda.addEventListener("click", function() {
        pushdiv.innerText="正在推送。。。。。";

        push(musicname,musicartistName,musicalbumName,url,musicimage,musiclyrc,pushdiv)
      });
  
    adda.innerText = "推送："+type
    adda.style.color = "red"
    adda.style.margin = "18px"
    adda.className ="no-underline";
    adda.href = "javascript:void(0)";
    pushdiv.appendChild(adda)
   }
   pushdiv.className="arco-space-item";
   pushdiv.id = musicname
   adddiv.appendChild(pushdiv)
    }

}
var targetNode = document.querySelector("body")
// // 观察者的选项(要观察哪些突变)

var config = { attributes: true,
    characterData: true,
    childList: true,
    subtree: true,
    attributeOldValue: true,
    characterDataOldValue: true };


// 当观察到突变时执行的回调函数
var callback = function(mutationsList) {
    mutationsList.forEach(function(item,index){
        if (item.type == 'childList') {
            var ssdsd = item.target.getElementsByClassName("arco-btn arco-btn-text arco-btn-shape-circle arco-btn-size-medium arco-btn-status-normal text-btn")
            for (var i = 0; i < ssdsd.length; i++ ){
                ssdsd[i].addEventListener("click", function () {
                    var infonode = this.parentNode.parentNode.children;
                    // console.log("infonode:");
                    // console.log(infonode);
                    let mtemp = 0;
                    for (let index = 0; index < infonode.length; index++) {
                        const element = infonode[index];
                        // console.log("element");

                        var rightdiv = element.querySelector("div > div.arco-col.flex-col.truncate > div")
                        
                        if(rightdiv==null){
                               //说明是左边类型 
                               let span =  element.querySelectorAll("span");
                               if(span!=null){
                                // console.log(index+"||"+mtemp+"||"+"span:");
                                // console.log(span[0]);
                                if(mtemp==1&&index ==1){
                                    if(span[0]!=null){
                                        musicname = span[0].innerText;
                                    }
                                   
                                }else if(
                                    mtemp==2&&index ==2
                                ){
                                    if(span[0]!=null){
                                        musicartistName =   span[0].innerText;
                                    }
                                  
                                }else if(mtemp=2&&index ==3){
                                    if(span[0]!=null){
                                        musicalbumName =   span[0].innerText;
                                    }
                                   
                                }
                                mtemp++;
                               }

                        }else{
                            let span =  element.querySelectorAll("span");
                            if(index ==1){
                                if(span[0]!=null){
                                    musicname = span[0].innerText;
                                }
                               
                            }
                            //右边类型
                           let tempspans =  rightdiv.children
                           musicartistName = tempspans[0].innerText
                           musicalbumName = tempspans[2].innerText
                    
                        }


                        
                    }
                    // console.log("mname:"+musicname);
                    // console.log("mar:"+musicartistName);
                    // console.log("malb:"+musicalbumName);
                    

                 })
            }

            // console.log(item.target.innerHTML);
        }
    });
};

// 创建一个链接到回调函数的观察者实例
var observer = new MutationObserver(callback);

// 开始观察已配置突变的目标节点
observer.observe(targetNode, config);


function push(musicname,musicartistName,musicalbumName,downloadurl,musicimage,musiclyrc,pushdiv){

    if(pushtoken==null||pushurl==null){
        alert("请先配置推送地址")
    }

    // 请求的URL
    const url = pushurl+'/download/freemp3';

    console.log("下载地址："+"downloadurl请求的URL：" + downloadurl);
    
    console.log("下载地址："+"musicimage请求的URL：" + musicimage);
        
    console.log("下载地址："+"musiclyrc请求的URL：" + musiclyrc);
// 需要发送的数据
const data = {
    "musicname": musicname,
    "musicartistName": musicartistName,
    "musicalbumName": musicalbumName,
    "downloadurl": downloadurl,
    "musicimage": musicimage,
    "musiclyrc": musiclyrc,
    "sqmusictoken": pushtoken
};
var jsonStr = JSON.stringify(data);

 // 前端压缩
 let clientData = pako.deflate(jsonStr);
 
 // 变成 串
 clientData = clientData.toString()
console.log("ys:"+clientData);
pushdiv.innerText="推送成功！";
// let pushdata = "?musicname="+musicname+"&musicartistName="+musicartistName+"&musicalbumName="+musicalbumName+"&downloadurl="+downloadurl+"&musicimage="+musicimage+"&musiclyrc="+musiclyrc+"&sqmusictoken="+pushtoken;
let pushdata = "?data="+clientData;

// window.open(url+pushdata, '_blank');
window.open(url+pushdata, '_blank','width=1,height=1');


    // var xhr = new XMLHttpRequest(); 
    // xhr.open('POST', url, true); 
    // xhr.setRequestHeader('Content-Type', 'application/json'); 
    // xhr.setRequestHeader("sqmusic",pushtoken);
    // xhr.onload = function () {
    //     if(xhr.status==401){
    //         alert("token过期，请重新配置")
    //         pushdiv.innerText="推送失败！（token过期，请重新配置）";
    //     }
    //     if(xhr.status==404){
    //         alert("URL错误未找到服务器")
    //         pushdiv.innerText="推送失败！（URL错误未找到服务器）";
    //     }
    //     if (xhr.status >= 200 && xhr.status < 300) {
    //         // 请求成功，处理响应数据
    //         console.log("返回数据：");
    //         console.log(xhr.responseText);
    //         pushdiv.innerText="推送成功！";
    //     } else {
    //         // 请求失败，处理错误
    //         pushdiv.innerText="推送失败！（未知异常）";
    //         console.error('Request fAIled:', xhr.statusText);
    //     }
    // };
    // xhr.send(JSON.stringify(data)); 

    
}


