$(document).ready(function(){
	$.extend($.fn,{
		blink     : function(options) {
			var defaults = {blink:true,interval:500,count:4};
			var data = $.extend(($(this).data('blinkdata')||defaults), (options||{}));
			$(this).data('blinkdata',data).fadeOut(data.interval, function(){
				var data = ($(this).data('blinkdata')||defaults);
				if(data.blink||false) {
					data.count--;
					if(data.count<1) {
						data.blink = false;
						if(data.animationEnd||false)data.animationEnd();
					}
					$(this).data('blinkdata',data).fadeIn(data.interval, function(){
						var data = ($(this).data('blinkdata')||defaults);
						if(data.blink||false) $(this).blink(data);
					});
				}
			});
			return $(this);
		},
		center    : function() {
			var t = $(this);
			var p = t.parent();
			if (p.length>0) {
				var mg = ((p.height()-t.height())/2);
				t.css({
					'margin-top'    : mg,
					'margin-bottom' : mg,
					'margin-left'   : 'auto',
					'margin-right'  : 'auto'
				});
			}
			return $(this);
		}
	});
	$.extend($,{
		stringify : function(obj) {
			 var t = typeof (obj);
			 if (t != "object" || obj === null) {
			   if (t == "string") obj = '"' + obj + '"';
			     return String(obj);
			   } else {
			     var n, v, json = [], arr = (obj && obj.constructor == Array);
			  	 for (n in obj) {
			  	   v = obj[n];
			       t = typeof(v);
			       if (obj.hasOwnProperty(n)) {
			         if (t == "string") 
			           v = '"' + v + '"'; 
			         else 
			           if (t == "object" && v !== null) 
			        	   v = $.stringify(v);
			           json.push((arr ? "" : '"' + n + '":') + String(v));
			   }
			 }
			 return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");
		   }
		},
		app       : {
			initialized     : false,
			usePolling      : true,
			polling_period  : 50,
			callbackId      : 0,	
			callbacks       : [],
			plugins         : [],
			callbackStatus  : {
				NO_RESULT                 : 0,
				OK                        : 1,
				CLASS_NOT_FOUND_EXCEPTION : 2,
				ILLEGAL_ACCESS_EXCEPTION  : 3,
				INSTANTIATION_EXCEPTION   : 4,
				MALFORMED_URL_EXCEPTION   : 5,
				IO_EXCEPTION              : 6,
				INVALID_ACTION            : 7,
				JSON_EXCEPTION            : 8,
				ERROR                     : 9
			},
			shuttingDown    : false,
			init            : function(apiurl, apitimeout, showConsole, showTitle, showStatus, tbardata, bbardata, loadingtext, loadingerrtext) {
				if(this.shuttingDown) return;
				
				if(this.initialized||false) return;
				else this.initialized = true;
				
				window.api_url         = (apiurl||'about:blank');
				window.timeout         = (apitimeout||60000);
				window.debug           = (showConsole||false);
				window.bottom_bar      = (showTitle||false);
				window.top_bar         = (showStatus||false);
				window.load_error_text = (loadingerrtext||'no connection to server,<br/>or server down.<br/>try again later...');
				
				this.showLoading((loadingtext||'loading...'));
				if(window.top_bar)    this.setTitleData(tbardata);
				if(window.bottom_bar) this.setStatusData(bbardata);
				$(window).triggerHandler('resize');
				if(/Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent)){
					this.getHttpData(window.api_url, function(data){
						console.log('done, starting app from server...');
						setTimeout("$.app.open_app(window.api_url, data);",1000);
					}, function(err){
						$.app.showError(err);
					});
				}else{
					setTimeout("$('#loading').hide();",30000);
					setTimeout("$.app.showError('test error');",30000);
				}
			},
			open_app        : function(url, data){
			    $('#loading').hide();
			    var w = $('body').width();
                var h = $('body').height();
			    //var doc = $('#remoteapp')[0].contentWindow.document;
                //var $ibody = $('body', doc);
			    if(window.debug||false) h-= $('#console').outerHeight(true);
			    //$ibody.html(data);
			    $('#remoteapp').attr('src',url);
			    $('#remoteapp').css({top:0,left:0,width:w,height:h}).show();
			}, 
			getHttpData     : function(url, success, fail) { this.exec(success, fail, "MJDev", "getHttpString", [url]); },
			setTitleData    : function(data){$('#topbar #text').html(data);},
			setStatusData   : function(data){$('#bottombar #text').html(data);},
			showLoading     : function(data){$(window).triggerHandler('loading', data);},
			hideLoading     : function(data){$('#loading').hide();},
			showError       : function(data){$(window).triggerHandler('app_error', data);},
			hideError       : function(data){$('#loading').hide();},
			loadUrl         : function(url, noHistory) {this.exec(null,null,"App","loadUrl",[url, {clearhistory:noHistory}]);},
			start           : function()    {
			  $('#splashscreen').hide().remove();
    		  if(/Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent)){
    			  if (this.usePolling) this.polling();
                  else {
                      var isPolling = prompt("usePolling", "gap_callbackServer:");
                      this.usePolling = isPolling;
                      if (isPolling == "true") {
                          this.usePolling = true;
                          polling();
                      } else {
                          this.usePolling = false;
                          this.callback();
                      }
                  }
    		  } else this.init('about:blank', '60000', 'test title', 'test status', 'test loading ...', true);
			},
			polling         : function(){
	          if (this.shuttingDown) return;
	          if (!this.usePolling) {
	        	  this.callback();
			      return;
			  }
	          var msg = prompt("", "gap_poll:");
			  if (msg||false) {
			    setTimeout(function() {
			      try { var t = eval(""+msg); }
			      catch (e) {
			        console.log("Message from Server: " + msg);
			        console.log("Error: "+e);
			    }}, 1);
			    setTimeout("$(window).triggerHandler('polling');", 1);
			  } else {
			    setTimeout("$(window).triggerHandler('polling');", $.app.polling_period);
			  }
			},
			callback        : function(){ $(window).triggerHandler('pooling'); },
			addPlugin       : function(plugin, obj, mountpoint) {
				this.plugins[plugin] = obj;
				if(mountpoint||false) {
					if (mountpoint[plugin] || false) {
						console.log("Error: Plugin "+ plugin +" already exists.");
					} else {
						mountpoint[plugin] = obj; 
						console.log(plugin + ' plugin added');
					}
				} else {
					console.log("Error: Plugin mount point does not exists.");
				}
			},
			log             : function(args){
				var logi = $('<div/>',{id:'log_item'});
				logi.html($.stringify(args));
				$('#console #data').prepend(logi);
			},
			exec            : function(success,fail,plugin,action,args){
				try {
					var callbackId = plugin + (this.callbackId++);
					if (success || fail) this.callbacks[callbackId] = {success:success, fail:fail};
					var r = prompt($.stringify(args||[]), "gap:"+$.stringify([plugin, action, callbackId, true]));
					if (r.length > 0) {
						var v;
						eval("v="+r+";");
						if (v.status === this.callbackStatus.OK) {
							if (success)try{success(v.message);}catch(e){console.log("Error in success callback: " + callbackId  + " = " + e);}
							return v.message;
						} else if (v.status === this.callbackStatus.NO_RESULT) {
                    } else {
                        if (fail)try{fail(v.message);}catch(e1){console.log("Error in error callback: "+callbackId+" = "+e1);}
                        return null;
                    }
                    if (!v.keepCallback) delete this.callbacks[callbackId];
                }
              } catch (e2) { console.log("Error: "+e2);}
			},
			callbackSuccess : function(callbackId, args) {
				if (this.callbacks[callbackId]) {
					if (args.status == this.callbackStatus.OK) {
						try {
							if (this.callbacks[callbackId].success) this.callbacks[callbackId].success(args.message);
						} catch (e) {
							console.log("Error in success callback: "+callbackId+" = "+e);
						}
					}
					if (!args.keepCallback) delete this.callbacks[callbackId];
				}
			},
			callbackError   : function(callbackId, args) {
				if (this.callbacks[callbackId]) {
					try {
						if (this.callbacks[callbackId].fail) this.callbacks[callbackId].fail(args.message);
					} catch (e) {
						console.log("Error in error callback: "+callbackId+" = "+e);
					}
					if (!args.keepCallback) delete this.callbacks[callbackId];
				}
			},
			exit            : function(args){
				this.shuttingDown = true;
				this.exec(null, null, 'App', 'exitApp', args||[]);
			}
		}
	});
	
	//events
	$(window).bind('loading',function(e, data){ 
	  e.stopPropagation(); 
      e.preventDefault();
      $('#loading').show().find('#text').html((data || 'unknown error'));;
      $(window).triggerHandler('resize');
      return false;
	});
	
	$(window).bind('resize',function(e, data){
	  var top = 0;
	  var bottom = 0;
	  
	  if(window.top_bar||false) {
	    $('#topbar').show();
	    top += $('#topbar').outerHeight(true);
	  } else $('#topbar').hide();
	  
	  if(window.bottom_bar||false) {
	    $('#bottombar').show();
	    bottom += $('#bottombar').outerHeight(true);
	  } else $('#bottombar').hide();
	  
	  if(window.debug||false) {
	    $('#console').css({ bottom : ((window.bottom_bar||false) ? $('#bottombar').outerHeight(true) : 0)}).show();
	    bottom += $('#console').outerHeight(true);
	  } else $('#console').hide();
	  
	  var w = $('body').width();
      var h = $('body').height();
      if(window.debug||false) h-= $('#console').outerHeight(true);
      $('#remoteapp').css({top:0,left:0,width:w,height:h});
	  
	  $('#loading').css({top:top,bottom:bottom,left:0,right:0});
	  $('#error').css({top:top,bottom:bottom,left:0,right:0});
      $('#loading #data').center();
      $('#error #data').center();
	  $('#splashscreen #data').center();
	});
	
	$(window).bind('app_error',function(e, data){
	  e.stopPropagation(); 
      e.preventDefault();
      var error   = $('#error');
      var loading = $('#loading');
	  loading.hide();
	  console.log(data);
      error.find('#text').html((data || 'no connection'));
      error.fadeIn(function(){$(this).find('#data').center();});
      error.find('#image').blink({
        count        : 8,
        animationEnd : function(){
        	setTimeout("$.app.exit();", 4000);
        }
      });
      return false;
  });
  
  $(window).bind('device_ready',function(e){
	e.stopPropagation(); 
    e.preventDefault();
    var msg = prompt("", "gap_init:");
    if(msg == 'OK') $('#splashscreen').fadeOut(2000,function(){ $.app.start(); });
    else         	$.app.showError('Error initialising device...<br/>' + msg);
  });
  
  $(window).bind('polling', function(){ $.app.polling(); });
  
  $(window).bind('app_resumed',function(e, data){
	  e.stopPropagation(); 
      e.preventDefault();
      $.app.usePolling = true;
      $.app.polling();
      // own method
  });
  
  $(window).bind('app_paused',function(e, data){
	  e.stopPropagation(); 
      e.preventDefault();
      $.app.usePolling = false;
      // own method
  });
  
  $(window).bind('app_kill',function(e, data){
	  e.stopPropagation(); 
      e.preventDefault();
      $.app.shuttingDown = true;
      // own method
  });
  
  $(window).bind('menu_button',function(e, data){
	  e.stopPropagation(); 
      e.preventDefault();
      // own method
  });
  
  $(window).bind('search_button',function(e, data){
	  e.stopPropagation(); 
      e.preventDefault();
      // own method
  });
  
  $(window).bind('back_button',function(e, data){
	  e.stopPropagation(); 
      e.preventDefault();
	  setTimeout("$.app.exit();", 500);
  });
  
  $(window).bind('showkeyboard',function(e, data){
	  e.stopPropagation(); 
      e.preventDefault();
     // own method
  });
  
  $(window).bind('hidekeyboard',function(e, data){
	  e.stopPropagation(); 
      e.preventDefault();
      // own method
  });
  
  $(window).bind('config_change',function(e, data){$(window).triggerHandler('resize',data||{});});
  
  // logging
  window.oldconsole = window.console; 
  
  // own console
  window.console = { 
    log : function() { 
	  if($.app||false)             $.app.log($.stringify(arguments).replace('\n','<br\>')); 
	  if(window.oldconsole||false) window.oldconsole.log($.stringify(arguments)); 
	} 
  };
  
  // rearange screen and raise all ready
  $(window).triggerHandler('resize');
  
  setTimeout("$(window).triggerHandler('device_ready');", 5000);
});