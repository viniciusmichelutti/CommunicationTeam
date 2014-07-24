var chat = chat || {};
chat.Room = {
	
	chatSocket: null,

	init: function(socketUrl) {
		 var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
		 this.chatSocket = new WS(socketUrl);
		 this.chatSocket.onmessage = this.receiveEvent;
		 $("body").on("submit", "#sendMessage", this.sendMessage);
	},
	
	sendMessage: function(e) {
		e.preventDefault();
		var form = $("#sendMessage");
		
		chat.Room.chatSocket.send(JSON.stringify({
			msg: form.find("input").val()
		}));
		
		form.find("input").val("");
	},
	
	receiveEvent: function(event) {
		var data = JSON.parse(event.data);

		if (data.error) {
			chat.Room.chatSocket.close();
			alert("An error occurred, please, try again.");
			return;
		}

		var user = $(".message").last().data("user");
		var dType = $(".message").last().data("type");
		if (user === data.email && dType === "message") {
			var html = "<p>" + data.message + "</p>";
			$("#messages .message").last().append(html);
		} else {
			var el = $('<div class="message"><img src="" /><span></span><p></p></div>');
			$("img", el).attr("src", chat.Room.getImageLink(data.email));
			$("span", el).text(data.username);
			$("p", el).text(data.message);
			$(el).addClass(data.type);
			$(el).data("user", data.email);
			$(el).data("type", data.type);
			$("#messages").append(el);
		}

        $("#messages").append("<div class='clear'></div>");
        $("#messages").animate({ scrollTop: $("#messages").height() }, "fast");
	},
	
	getImageLink: function(email) {
		var link = "http://www.gravatar.com/avatar/";
		
		$.ajax({
			url: "/hash?email=" + email,
			success: function(result) {
				link += result;
			},
			async: false
		});
		
		return link;
	}

}

$(function() {
	var socketUrl = $("#socketsUrl").val();
	chat.Room.init(socketUrl);
});