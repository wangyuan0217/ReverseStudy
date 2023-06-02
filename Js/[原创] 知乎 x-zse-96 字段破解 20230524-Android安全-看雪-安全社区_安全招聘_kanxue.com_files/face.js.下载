// QQ表情插件
(function($){  
	$.fn.qqFace = function(options){
		var defaults = {
			id : 'facebox',
			path : 'face/',
			assign : 'content',
			tip : 'em_'
		};
		var option = $.extend(defaults, options);
		var assign = $('#'+option.assign);
		var id = option.id;
		var path = option.path;
		var tip = option.tip;
		
		if(assign.length<=0){
			// alert('缺少表情赋值对象。');
			return false;
		}
		
		$(this).on('click', function(e){
			var strFace, labFace;
			if($('#'+id).length<=0){
				// strFace = '<div id="'+id+'" style="display:none; margin: 0;" class="qqFace">' +
				// 			  '<table border="0" cellspacing="0" cellpadding="0"><tr>';
				// for(var i=1; i<=75; i++){
				// 	labFace = '['+tip+i+']';
				// 	strFace += '<td><img src="'+path+i+'.gif" onclick="$(\'#'+option.assign+'\').setCaret();$(\'#'+option.assign+'\').insertAtCaret(\'' + labFace + '\');" /></td>';
				// 	if( i % 15 == 0 ) strFace += '</tr><tr>';
				// }
				// strFace += '</tr></table></div>';

				strFace = '<div id="'+id+'" style="display:none; margin: 0;max-height: 150px; overflow: auto;" class="qqFace">' +
							  '<ul style="list-style-type: none; padding: 0; margin: 0px;">';
				
				for(var i=1; i<= 97; i++){
					labFace = '['+tip+i+']';
					strFace += '<li style="display: inline-block; padding: 3px;"><img style="border-width: 1px; border-style: solid; border-color: #fff;" src="'+path+i+'.gif" onclick="$(\'#'+option.assign+'\').setCaret();$(\'#'+option.assign+'\').insertAtCaret(\'' + labFace + '\');" /></li>';
				}
				strFace += '</ul></div>';
			}
			$(this).parents(".face_open").prepend(strFace);
			// var offset = $(this).position();
			// var top = offset.top + $(this).outerHeight();
			// $('#'+id).css('top',top);
			// $('#'+id).css('left',offset.left);
			// $('#'+id).parents('.face_open').css('margin-bottom', '123px');
			$('#'+id).slideDown("fast");
			e.stopPropagation();
		});

		$(document).on('click', function(){
			$('#'+id).parents('.face_open').removeAttr("style");
			$('#'+id).slideUp('fast',function(){
				// $('#'+id).remove();
			});
			
		});
	};

})(jQuery);

jQuery.extend({ 
unselectContents: function(){ 
	if(window.getSelection) {
		window.getSelection().removeAllRanges(); 
	}else if(document.selection) {
		document.selection.empty(); 
	
	}
	} 
}); 
jQuery.fn.extend({ 
	selectContents: function(){ 
		$(this).each(function(i){ 
			var node = this; 
			var selection, range, doc, win; 
			if ((doc = node.ownerDocument) && (win = doc.defaultView) && typeof win.getSelection != 'undefined' && typeof doc.createRange != 'undefined' && (selection = window.getSelection()) && typeof selection.removeAllRanges != 'undefined'){ 
				range = doc.createRange(); 
				range.selectNode(node); 
				if(i == 0){ 
					selection.removeAllRanges(); 
				} 
				selection.addRange(range); 
			} else if (document.body && typeof document.body.createTextRange != 'undefined' && (range = document.body.createTextRange())){ 
				range.moveToElementText(node); 
				range.select(); 
			} 
		}); 
	}, 

	setCaret: function(){ 
		// if(!$.browser.msie) return; 
		var initSetCaret = function(){ 
			var textObj = $(this).get(0); 
			// textObj.caretPos = document.selection.createRange().duplicate(); 
		}; 
		$(this).click(initSetCaret).select(initSetCaret).keyup(initSetCaret); 
	}, 

	insertAtCaret: function(textFeildValue){ 
		var textObj = $(this).get(0); 
		if(document.all && textObj.createTextRange && textObj.caretPos){ 
			var caretPos=textObj.caretPos; 
			caretPos.text = caretPos.text.charAt(caretPos.text.length-1) == '' ? 
			textFeildValue+'' : textFeildValue; 
		} else if(textObj.setSelectionRange){ 
			var rangeStart=textObj.selectionStart; 
			var rangeEnd=textObj.selectionEnd; 
			var tempStr1=textObj.value.substring(0,rangeStart); 
			var tempStr2=textObj.value.substring(rangeEnd); 
			textObj.value=tempStr1+textFeildValue+tempStr2; 
			textObj.focus(); 
			var len=textFeildValue.length; 
			textObj.setSelectionRange(rangeStart+len,rangeStart+len); 
			textObj.blur(); 
		}else{ 
			textObj.value+=textFeildValue; 
		} 
	} 
});