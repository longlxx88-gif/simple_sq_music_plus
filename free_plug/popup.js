


document.getElementById('clearbtn').addEventListener("click", function () {
     // 存储数据到 local 存储区域
     chrome.storage.local.set({ 'sqmusicexurl':"请重新输入" }, function() {
      console.log('url:'+url);
    });
    chrome.storage.local.set({ 'sqmusicextoken':"请重新输入" }, function() {
      console.log('token:'+token);
    });

})

document.getElementById('savebtn').addEventListener("click", function () {

   let url =  document.getElementById('url').value;
   let token =  document.getElementById('token').value;
   url = url.replace(/^\s*|\s*$/g,"");
   token = token.replace(/^\s*|\s*$/g,"");
   if(url==""||token==""){
       alert("请填写完整")
       return
   }

   //如果最后一位是/则删除掉
   if(url.endsWith("/")){
       url = url.substring(0,url.length-1)
   }

     // 存储数据到 local 存储区域
chrome.storage.local.set({ 'sqmusicexurl':url }, function() {
    console.log('url:'+url);
  });
  chrome.storage.local.set({ 'sqmusicextoken':token }, function() {
    console.log('token:'+token);
  });
  check(url,token)
})



    // 从 local 存储区域获取数据
    chrome.storage.local.get(['sqmusicextoken'], function(result) {
      document.getElementById('token').value = result.sqmusicextoken
    });
    chrome.storage.local.get(['sqmusicexurl'], function(result) {
      document.getElementById('url').value = result.sqmusicexurl
    });
    function check(url,token){
        var xhr = new XMLHttpRequest(); 
        xhr.open('POST', url+"/isLogin", true); 
        xhr.setRequestHeader('Content-Type', 'application/json'); 
        xhr.setRequestHeader("sqmusic",token);
        xhr.onload = function () {
            if(xhr.status==401){
                alert("token 错误或超时")
            }
            if(xhr.status==404){
                alert("URL错误未找到服务器")
            }
            if (xhr.status >= 200 && xhr.status < 300) {
        
                console.log(xhr.responseText);
                var obj = JSON.parse(xhr.responseText)
                if(obj["code"]==200){
                    alert("登录成功请刷新网页（必须）")
                }else{
                  alert("失败！请检查token是否正确")
                }

            } else {
                // 请求失败，处理错误

                console.error('Request fAIled:', xhr.statusText);
            }
        };
        xhr.send(JSON.stringify({})); 
    }



