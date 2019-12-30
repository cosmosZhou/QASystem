<%@ page language="java" contentType="text/html;charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>

<head>
<meta charset="utf-8">
<title>QASystem</title>
</head>

<style type="text/css">
body {
	background-color: rgb(199, 237, 204);
}

input[type="file"] {
	color: transparent;
}

.non-arrowed {
	-webkit-appearance: none;
	-moz-appearance: none;
	background-color: rgb(199, 237, 204);
	border: 0;
	font-size: 1em;
}

.monospace-p, .monospace {
	font-style: normal;
	font-family: 新宋体, 宋体;
	font-size: 1em;
	font-weight: normal;
	margin-left: 1em;
}

.monospace-p {
	white-space: nowrap;
}
</style>

<body>
	<button onclick="clear_cookie()">delete cookie</button>

	<%@ page import="com.util.Native"%>
	<%
		request.setCharacterEncoding("utf-8");
	%>

	<form name="paraphrase" method="post">
		paraphrase:<br> <input type="text" id=paraphrase_x
			name="paraphrase_x" onblur="set_cookie(this)"
			onfocus="set_focus(this)"> / <input type="text"
			id=paraphrase_y name="paraphrase_y" onblur="set_cookie(this)"
			onfocus="set_focus(this)"> =
		<nobr id=paraphrase_score>
			<%
				String x = request.getParameter("paraphrase_x");
				String y = request.getParameter("paraphrase_y");
				if (x != null && !x.isEmpty() && y != null && !y.isEmpty()) {
					out.print(Native.similarity(x, y));
				}
			%>
		</nobr>
	</form>

	<form name="phatic" method="post">
		phatic:<br> <input type="text" id=phatic_text name="phatic_text"
			onblur="set_cookie(this, 'paraphrase_score')" onfocus="set_focus(this)"> =
		<%
 	{
 		String text = request.getParameter("phatic_text");
 		if (text != null && !text.isEmpty()) {
 			out.print(Native.phatic(text));
 		}
 	}
 %>
	</form>

	<form name="qatype" method="post">
		qatype:<br> <input type="text" id=qatype_text name="qatype_text"
			onblur="set_cookie(this, 'paraphrase_score')" onfocus="set_focus(this)"> =
		<%
 	{
 		String text = request.getParameter("qatype_text");
 		if (text != null && !text.isEmpty()) {
 			out.print(Native.qatype(text));
 		}
 	}
 %>
	</form>

</body>

<script>
	function clear_cookie() {
		console.log(document.cookie);
		var cookie = document.cookie.split(";");
		var expires = new Date();
		expires.setTime(expires.getTime() - 10);

		for (var i = 0; i < cookie.length; i++) {
			var pair = cookie[i].trim().split("=");
			if (pair.length != 2)
				continue;

			console.log(pair);
			document.cookie = pair[0] + '=' + escape('null') + ';expires='
					+ expires.toGMTString();
		}

		console.log("after clearing: " + document.cookie);
		initialize();
	}

	function get_cookie(key) {
		console.log(document.cookie);
		var cookie = document.cookie.split(";");
		//		console.log(cookie);
		for (var i = 0; i < cookie.length; ++i) {
			var pair = cookie[i].trim().split("=");
			if (pair.length != 2)
				continue;
			console.log(pair);
			if (key == pair[0])
				return unescape(pair[1]);
		}
		return '';
	}

	function set_cookie(self) {
		document.cookie = self.id + "=" + escape(self.value);
		//		document.cookie = "paraphrase_score="
		//				+ document.getElementById("paraphrase_score").value;
		//		document.cookie = "phatic_score="
		//				+ document.getElementById("phatic_score").value;
		//		document.cookie = "qatype_score="
		//				+ document.getElementById("qatype_score").value;
		for (var i = 1; i < arguments.length; ++i){
			var key = arguments[i];
			console.log(key + "=" + document.getElementById(key).innerHTML);
			document.cookie = key + "=" + escape(document.getElementById(key).innerHTML);
		}
		self.parentElement.submit();
	}

	function set_focus(self) {
		document.cookie = "focus=" + self.id;
	}

	function del_cookie(key) {
		var tmp_cookie = '';
		var cookie = document.cookie.split(";");
		for (var i = 0; i < cookie.length; i++) {
			var pair = cookie[i].split("=");
			if (key != pair[0]) {
				tmp_cookie += cookie[i] + ';';
			}
		}
		document.cookie = tmp_cookie;
	}

	function initialize() {
		document.getElementById("paraphrase_x").value = get_cookie('paraphrase_x');
		document.getElementById("paraphrase_y").value = get_cookie('paraphrase_y');
		//		document.getElementById("paraphrase_score").value = get_cookie('paraphrase_score');
		document.getElementById("phatic_text").value = get_cookie('phatic_text');
		document.getElementById("qatype_text").value = get_cookie('qatype_text');
		document.getElementById(get_cookie('focus')).focus();
	}

	initialize();
	//https://www.cnblogs.com/sunny-roman/p/11393413.html
	//https://blog.csdn.net/brandyzhaowei/article/details/12750071
</script>
</html>