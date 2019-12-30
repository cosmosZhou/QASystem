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
	<input type="button" name="clear_cookie" value='clear cookie'
		onclick="clear_cookie()">

	<form name="paraphrase" method="post">
		<input type="text" name="x" onblur="set_cookie(this)"> / <input
			type="text" name="y" onblur="set_cookie(this)">
	</form>

	<%@ page import="com.util.Native"%>
	<%
		request.setCharacterEncoding("utf-8");

		String x = request.getParameter("x");
		String y = request.getParameter("y");
		if (x != null && !x.isEmpty() && y != null && !y.isEmpty()) {
			out.print("x = " + x);
			out.print("<br>");
			out.print("y = " + y);
			out.print("<br>");
			out.print("Native.similarity(x, y) = " + Native.similarity(x, y));
			out.print("<br>");
		}
		//https://www.cnblogs.com/sunny-roman/p/11393413.html
		//https://blog.csdn.net/brandyzhaowei/article/details/12750071
	%>
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
		document.cookie = self.name + "=" + escape(self.value);
		self.parentElement.submit();
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

	document.paraphrase.x.value = get_cookie('x');
	document.paraphrase.y.value = get_cookie('y');
</script>


</html>