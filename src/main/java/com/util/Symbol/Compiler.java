package com.util.Symbol;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

import com.util.Utility;
import com.util.Symbol.Data.HData;
import com.util.Symbol.Data.HData0;
import com.util.Symbol.Data.HData1;
import com.util.Symbol.Data.HDatax;

public class Compiler {
	static interface FunctionPtr {
		// void invoke();
		HExpress invoke(HExpress exp) throws Exception;
	}

	<T> FunctionPtr construct_bfn(final Class<T> clazz) {
		return new FunctionPtr() {
			public HExpress invoke(HExpress exp) throws Exception {
				HExpress hCaret = new HExpressCaret();

				Constructor<T> ct = clazz.getConstructor(Compiler.class, HExpress.class, HExpress.class);
				HExpress ptr = (HExpress) ct.newInstance(Compiler.this, exp, hCaret);
				recaret(ptr);
				walker.push(hCaret);
				return hCaret;
			}
		};
	}

	<T> FunctionPtr construct_ufn(final Class<T> clazz) {
		return new FunctionPtr() {
			public HExpress invoke(HExpress exp) throws Exception {
				HExpress ptr = (HExpress) clazz.getConstructor(HExpress.class).newInstance(exp);
				recaret(ptr);
				return ptr;
			}
		};
	}

	abstract class HExpress {
		HExpress parent() {
			while (!(this == Compiler.this.recaret())) {
				Compiler.this.pop_cursor();
			}
			return Compiler.this.parent();
		}

		void construct_bfn(FunctionPtr fnptr, int input_precedence) throws Exception {
			if (parent().stack_precedence() < input_precedence)
				// push operator;
				fnptr.invoke(this);
			else
				// pop operator;
				parent().construct_bfn(fnptr, input_precedence);
		}

		void replace(HExpress old, HExpress replacement) throws Exception {
			throw new Exception("not a parent type " + this.getClass());
		}

		abstract HData evaluate();

		abstract void construct_arg(FunctionPtr fnptr) throws Exception;

		abstract HExpress construct_ufn(FunctionPtr fnptr);

		abstract int stack_precedence();

		void construct_space() throws Exception {
			recaret(new HExpressPostNull(this));
		}

		abstract HExpress append_comma();

		abstract HExpress append_dot();

		abstract HExpress append_semicolon();

		abstract HExpress append_brace_right(HExpress exp);

		abstract HExpress construct_pow();

		void construct_digit() throws Exception {
			construct_arg(new FunctionPtr() {
				public HExpress invoke(HExpress exp) {
					return new HExpressDigit(Compiler.this.value);
				}
			});
		}

		void construct_char() throws Exception {
			construct_arg(new FunctionPtr() {
				public HExpress invoke(HExpress exp) {
					return new HExpressChar((char) Compiler.this.value);
				}
			});
		}

		public abstract String toString();
	}

	class HExpressCaret extends HExpress {
		static final int stack_precedence = 14;
		static final int input_precedence = 14;

		@Override
		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_pow() {
			return null;
		}

		@Override
		void construct_arg(FunctionPtr fnptr) throws Exception {
			Compiler.this.recaret(fnptr.invoke(this));
		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class HExpressDigit extends HExpress {
		static final int stack_precedence = 14;
		static final int input_precedence = 15;

		long value;

		public HExpressDigit(long value) {
			this.value = value;
		}

		@Override
		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {
		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			return "" + value;
		}

		@Override
		HData evaluate() {
			if (value == 0)
				return new HData0();
			if (value == 1)
				return new HData1();

			return new HDatax(value);
		}

	}

	class HExpressChar extends HExpress {
		static final int stack_precedence = 14;
		static final int input_precedence = 14;
		char ch;

		HExpressChar(char ch) {
			this.ch = ch;
		}

		@Override
		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		HExpress append_subscript() {
			return null;
		}

		@Override
		HExpress construct_pow() {

			return null;
		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "" + this.ch;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class HExpressSubscript extends HExpressChar {
		HExpress index;

		HExpressSubscript(char ch, HExpress index) {
			super(ch);
			this.index = index;
		}

		static final int stack_precedence = 0;
		static final int input_precedence = 14;

		public String toString() {
			return super.toString() + "[" + index + "]";
		}
	};

	abstract class HExpressVector extends HExpress {
		ArrayList<HExpress> v;

		void replace(HExpress old, HExpress replacement) {
			v.set(v.indexOf(old), replacement);
		}

		public HExpressVector(ArrayList<HExpress> v) {
			this.v = v;
		}
	}

	class HExpressNull extends HExpressVector {
		public HExpressNull(ArrayList<HExpress> v) {
			super(v);
		}

		static final int stack_precedence = 13;
		static final int input_precedence = 14;

		@Override
		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			String str = "";
			for (HExpress e : v) {
				str += e;
			}
			return str;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class HExpressSentence extends HExpressVector {
		public HExpressSentence(ArrayList<HExpress> v) {
			super(v);
		}

		static final int stack_precedence = 0;
		static final int input_precedence = 0;

		@Override
		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			String str = "";
			for (HExpress e : v) {
				str += e;
			}
			return str;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	abstract class HExpressUnary extends HExpress {
		HExpress ptr;

		public HExpressUnary(HExpress ptr) {
			this.ptr = ptr;
		}

		void replace(HExpress old, HExpress replacement) throws Exception {
			if (ptr == old)
				ptr = replacement;
			else
				throw new Exception(old + " not found in " + this);
		}
	}

	class HExpressPostNull extends HExpressUnary {
		static final int stack_precedence = 14;
		static final int input_precedence = 2;

		public HExpressPostNull(HExpress ptr) {
			super(ptr);
		}

		@Override
		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return this.ptr + " ";
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return ptr.evaluate();
		}
	}

	class HExpressAbs extends HExpressUnary {
		static final int stack_precedence = 0;
		static final int input_precedence = 18;

		public HExpressAbs(HExpress ptr) {
			super(ptr);
		}

		@Override
		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "|" + this.ptr + "|";
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class HExpressConj extends HExpressUnary {
		static final int stack_precedence = 0;
		static final int input_precedence = 18;

		public HExpressConj(HExpress ptr) {
			super(ptr);
		}

		@Override
		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "~" + ptr;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class HExpressBrace extends HExpressUnary {
		static final int stack_precedence = 0;
		static final int input_precedence = 18;

		public HExpressBrace(HExpress ptr) {
			super(ptr);
		}

		@Override
		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "{" + ptr + "}";
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class HExpressParenthesis extends HExpressUnary {
		static final int stack_precedence = 0;
		static final int input_precedence = 16;

		public HExpressParenthesis(HExpress ptr) {
			super(ptr);
		}

		@Override
		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "(" + ptr + ")";
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class HExpressSignP extends HExpressUnary {
		static final int stack_precedence = 13;
		static final int input_precedence = 0;

		public HExpressSignP(HExpress ptr) {
			super(ptr);
		}

		@Override
		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "+" + ptr;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class HExpressSignN extends HExpressUnary {
		static final int stack_precedence = 13;
		static final int input_precedence = 1;

		public HExpressSignN(HExpress ptr) {
			super(ptr);
		}

		@Override
		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "-" + ptr;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	abstract class HExpressBinary extends HExpress {
		public HExpress x, y;

		public HExpressBinary(HExpress x, HExpress y) {
			this.x = x;
			this.y = y;
		}

		void replace(HExpress old, HExpress replacement) throws Exception {
			if (x == old)
				x = replacement;
			else if (y == old)
				y = replacement;
			else
				throw new Exception(old + " not found in " + this);
		}
	}

	class HExpressMul extends HExpressBinary {
		static final int stack_precedence = 13;
		static final int input_precedence = 12;

		public HExpressMul(HExpress x, HExpress y) {
			super(x, y);
		}

		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return x + "*" + y;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public class HExpressDiv extends HExpressBinary {
		static final int stack_precedence = 14;
		static final int input_precedence = 13;

		public HExpressDiv(HExpress x, HExpress y) {
			super(x, y);
		}

		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return x + "/" + y;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public class HExpressAdd extends HExpressBinary {
		static final int stack_precedence = 11;
		static final int input_precedence = 10;

		public HExpressAdd(HExpress x, HExpress y) {
			super(x, y);
		}

		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return x + "+" + y;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return x.evaluate().add(y.evaluate());
		}
	}

	public class HExpressSub extends HExpressBinary {
		static final int stack_precedence = 11;
		static final int input_precedence = 11;

		public HExpressSub(HExpress x, HExpress y) {
			super(x, y);
		}

		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return x + "-" + y;
		}

		@Override
		HData evaluate() {
			return x.evaluate().sub(y.evaluate());
		}
	}

	public class HExpressColon extends HExpressBinary {
		static final int stack_precedence = 0;
		static final int input_precedence = 2;

		public HExpressColon(HExpress x, HExpress y) {
			super(x, y);
		}

		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return x + ":" + y;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public class HExpressEquality extends HExpressBinary {
		static final int stack_precedence = 3;
		static final int input_precedence = 4;

		public HExpressEquality(HExpress x, HExpress y) {
			super(x, y);
		}

		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return x + "=" + y;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public class HExpressLess extends HExpressBinary {
		static final int stack_precedence = 3;
		static final int input_precedence = 4;

		public HExpressLess(HExpress x, HExpress y) {
			super(x, y);
		}

		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return x + "<" + y;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public class HExpressMore extends HExpressBinary {
		static final int stack_precedence = 3;
		static final int input_precedence = 4;

		public HExpressMore(HExpress x, HExpress y) {
			super(x, y);
		}

		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return x + ">" + y;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public class HExpressNoless extends HExpressBinary {
		static final int stack_precedence = 3;
		static final int input_precedence = 4;

		public HExpressNoless(HExpress x, HExpress y) {
			super(x, y);
		}

		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {

		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return null;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return x + ">=" + y;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public class HExpressNomore extends HExpressBinary {
		static final int stack_precedence = 3;
		static final int input_precedence = 4;

		public HExpressNomore(HExpress x, HExpress y) {
			super(x, y);
		}

		int stack_precedence() {
			return stack_precedence;
		}

		@Override
		void construct_space() {
		}

		@Override
		HExpress construct_ufn(FunctionPtr fnptr) {
			return null;

		}

		@Override
		HExpress append_comma() {
			return null;

		}

		@Override
		HExpress append_dot() {
			return null;

		}

		@Override
		void construct_arg(FunctionPtr fnptr) {

		}

		@Override
		HExpress append_semicolon() {
			return x;

		}

		@Override
		HExpress construct_pow() {
			return null;

		}

		@Override
		HExpress append_brace_right(HExpress exp) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return x + "<=" + y;
		}

		@Override
		HData evaluate() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	static final int TRAIT_RANK = (1 << 13);
	int cntabs;
	long value;
	int i;
	int j;
	Stack<HExpress> walker;
	ArrayList<HExpress> vSentences;

	HExpress recaret() {
		// return walker.get(walker.size() - 1);
		return walker.lastElement();
	}

	void recaret(HExpress exp) throws Exception {
		// walker.set(walker.size() - 1, exp);
		parent().replace(recaret(), exp);
		walker.pop();
		walker.push(exp);
	}

	HExpress parent() {
		return walker.get(walker.size() - 2);
	}

	HExpress get_expression() {
		if (i < vSentences.size())
			return ((HExpressSentence) vSentences.get(i)).v.get(j);
		return null;
	}

	Compiler() {
		cntabs = 0;
		value = 0;
		i = 0;
		j = 0;
		walker = new Stack<HExpress>();
		vSentences = new ArrayList<HExpress>();
	}

	HExpress newline() {
		HExpressSentence hSentence = new HExpressSentence(new ArrayList<HExpress>());
		HExpressCaret hCaret = new HExpressCaret();
		hSentence.v.add(hCaret);
		vSentences.add(hSentence);

		walker.add(hSentence);
		walker.add(hCaret);
		return hCaret;
	}

	void reset(int trait) {
		value &= ~trait;
	}

	boolean get(int trait) {
		return (value & trait) != 0;
	}

	void put(int trait) {
		value |= trait;
	}

	public void construct(String exp) throws Exception {
		exp += '\0';
		this.newline();
		for (int i = 0; i < exp.length(); ++i) {
			// lable_begin:
			char glyph = exp.charAt(i);
			switch (glyph) {
			case '\0':
				break;
			case '\t':
			case ' ':
			case '\n': // 0x0a;
				recaret().construct_bfn(new FunctionPtr() {
					public HExpress invoke(HExpress exp) throws Exception {
						exp.construct_space();
						return exp;
					}
				}, HExpressNull.input_precedence);
				continue;
			case '\r': // 0x0d;
				glyph = exp.charAt(i + 1);
				if (glyph != '\n') {
					throw new Exception("glyph != '\n'");
				}
				continue;

			case '!':
				continue;

			case '(':
				reset(TRAIT_RANK);
				recaret().construct_ufn(new FunctionPtr() {
					public HExpress invoke(HExpress exp) {
						return new HExpressParenthesis(exp);
					}
				});
				continue;
			case ')':
				for (;;) {
					if (parent() instanceof HExpressParenthesis) {
						break;
					}
				}
				continue;

			case '*':
				reset(TRAIT_RANK);
				recaret().construct_bfn(construct_bfn(HExpressMul.class), HExpressMul.input_precedence);
				continue;
			case '+':
				if (get(TRAIT_RANK)) {
					reset(TRAIT_RANK);
					recaret().construct_bfn(construct_bfn(HExpressAdd.class), HExpressAdd.input_precedence);
				} else {
					recaret().construct_ufn(construct_bfn(HExpressSignP.class));
				}
				continue;
			case ',':
				reset(TRAIT_RANK);
				parent().append_comma();
				continue;
			case '-':
				if (get(TRAIT_RANK)) {
					reset(TRAIT_RANK);
					recaret().construct_bfn(construct_bfn(HExpressSub.class), HExpressSub.input_precedence);
				} else {
					recaret().construct_ufn(construct_bfn(HExpressSignN.class));
				}
				continue;
			case '.':
				reset(TRAIT_RANK);
				recaret().append_dot();
				continue;
			case '/':
				reset(TRAIT_RANK);
				recaret().construct_bfn(construct_bfn(HExpressDiv.class), HExpressDiv.input_precedence);
				continue;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				put(TRAIT_RANK); {
				long n = glyph - '0';
				while (Character.isDigit(exp.charAt(++i))) {
					n *= 10;
					n += glyph - '0';
				}
				long _id = value;
				value = n;
				recaret().construct_bfn(new FunctionPtr() {
					public HExpress invoke(HExpress exp) throws Exception {
						exp.construct_digit();
						return exp;
					}
				}, HExpressDigit.input_precedence);
				value = _id;
				--i;
			}
				continue;
			case ':':
				reset(TRAIT_RANK);
				recaret().construct_bfn(construct_bfn(HExpressColon.class), HExpressColon.input_precedence);
				continue;
			case ';':
				reset(TRAIT_RANK);
				parent().append_semicolon();
				continue;
			case '<':
				reset(TRAIT_RANK);
				glyph = exp.charAt(++i);
				if (glyph == '=') {
					recaret().construct_bfn(construct_bfn(HExpressNomore.class), HExpressNomore.input_precedence);
				} else {
					recaret().construct_bfn(construct_bfn(HExpressLess.class), HExpressLess.input_precedence);
					--i;
				}
				continue;
			case '=':
				reset(TRAIT_RANK);
				recaret().construct_bfn(construct_bfn(HExpressEquality.class), HExpressEquality.input_precedence);
				continue;
			case '>':
				reset(TRAIT_RANK);
				glyph = exp.charAt(++i);
				if (glyph == '=')
					recaret().construct_bfn(construct_bfn(HExpressNoless.class), HExpressNoless.input_precedence);
				else {
					recaret().construct_bfn(construct_bfn(HExpressMore.class), HExpressMore.input_precedence);
					--i;
				}
				continue;
			case '[':
				if (get(TRAIT_RANK)) {
					reset(TRAIT_RANK);
					if (recaret() instanceof HExpressChar) {
						((HExpressChar) recaret()).append_subscript();
					}
				} else {
					throw new Exception("unexpected");
				}
				continue;
			case ']': {
				HExpress hCaret = recaret();
				for (;;) {
					hCaret = recaret();
					if (hCaret instanceof HExpressSubscript) {
						break;
					}
				}
			}
				continue;
			case '^':
				reset(TRAIT_RANK);
				recaret().construct_pow();
				continue;
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'J':
			case 'K':
			case 'L':
			case 'M':
			case 'N':
			case 'O':
			case 'P':
			case 'Q':
			case 'R':
			case 'S':
			case 'T':
			case 'U':
			case 'V':
			case 'W':
			case 'X':
			case 'Y':
			case 'Z':
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'h':
			case 'i':
			case 'j':
			case 'k':
			case 'l':
			case 'm':
			case 'n':
			case 'o':
			case 'p':
			case 'q':
			case 'r':
			case 's':
			case 't':
			case 'u':
			case 'v':
			case 'w':
			case 'x':
			case 'y':
			case 'z':
				put(TRAIT_RANK);
				recaret().construct_bfn(new FunctionPtr() {
					public HExpress invoke(HExpress exp) throws Exception {
						exp.construct_char();
						return exp;
					}
				}, HExpressChar.input_precedence);
				continue;
			case '{':
				reset(TRAIT_RANK);

				recaret().construct_ufn(construct_bfn(HExpressBrace.class));
				continue;
			case '|':
				if (cntabs > 0 && get(TRAIT_RANK)) {
					--cntabs;

					for (;;) {
						if (recaret() instanceof HExpressAbs)
							break;
					}
				} else {
					++cntabs;
					recaret().construct_ufn(construct_bfn(HExpressAbs.class));
				}
				continue;
			case '}':
				parent().append_brace_right(recaret());
				continue;
			case '~':
				recaret().construct_ufn(construct_bfn(HExpressConj.class));
				continue;
			}
			break;
		}
	}

	void pop_cursor() {
		walker.remove(walker.size() - 1);
		// walker.pop();
	}

	public static String evaluate(String str) throws Exception {
		Compiler compiler = new Compiler();
		compiler.construct(str);
		HExpress exp = compiler.get_expression();
		HData data = exp.evaluate();
		return data.toString();
	}

	public static String java_evaluate(String str) throws Exception {
		String classname = "com.industry.Calculator";
		String classDomain = classname.replace('.', '/');
		// String classpath = Compiler.class.getResource("/").getPath();

		String workingDirectory = new File(Utility.workingDirectory).getAbsolutePath();
		String classpath = workingDirectory + "/Robot/bin/";

		System.out.println("classpath = " + classpath);

		// File classpathDir = new File(classpath);
		// if (!classpathDir.exists()) {
		// classpathDir.mkdirs();
		// }

		String javaFile = workingDirectory + "/Robot/src/" + classDomain + ".java";
		System.out.println("javaFile = " + javaFile);

		String content = new Utility.Text(javaFile).fetchContent();
		// System.out.println("content = \n" + content);

		String[] res = Utility.regexSingleton(content, "([\\s\\S]+public static double value = )([\\s\\S]+?;)([\\s\\S]+)");
		// System.out.println("res[1] = \n" + res[1]);
		// System.out.println("res[2] = \n" + res[2].substring(0,
		// res[2].length() - 1));
		// System.out.println("res[3] = \n" + res[3]);

		content = res[1] + str + ";" + res[3];

		System.out.println("changed content = \n" + content);

		Utility.writeString(javaFile, content);
		java_compile(classname);
		return java_execute(classname);
	}

	public static String java_execute(String classname) throws Exception {
		String classDomain = classname.replace('.', '/');

		String workingDirectory = new File(Utility.workingDirectory).getAbsolutePath();
		String classpath = workingDirectory + "/Robot/bin/";

		System.out.println("classpath = " + classpath);

		File classFile = new File(classpath + classDomain + ".class");
		if (!classFile.exists()) {
			throw new Exception(classFile + " does not exist.");
		}
		String cmd = "java -classpath " + classpath + " " + classname;
		System.out.println("cmd = " + cmd);

		Runtime run = Runtime.getRuntime();
		Process process = null;
		if (SystemUtils.IS_OS_WINDOWS) {
			process = run.exec("cmd /c " + cmd);
		} else {
			process = run.exec(new String[] { "/bin/sh", "-c", cmd });
		}

		int exitCode = process.waitFor();
		// Thread.sleep(500);
		if (exitCode != 0) {
			System.out.println("exitCode = " + exitCode);
			for (String s : fetchResult(process)) {
				System.out.println(s);
			}

			throw new Exception("exitCode = " + exitCode);
		}

		String[] result = fetchResult(process);
		if (result.length == 0)
			throw new Exception("the resulting output is empty!");
		String res = result[0];

		process.destroy();

		System.out.println(classpath + classDomain + ".class" + " is deleted.");
		new File(classpath + classDomain + ".class").delete();

		// // Class clazz = Class.forName("Calculator");
		// Method method = clazz.getMethod("evaluate", new Class[] {});
		// double value = clazz.getField("value").getDouble(null);
		// Object object = method.invoke(null, new Object[] {});
		// str = object.toString();
		//

		return res;
	}

	/**
	 * compile a java source file to generate a .class file;
	 * 
	 * @param classname
	 * @throws Exception
	 */
	// = "com.industry.Calculator";
	public static void java_compile(String classname) throws Exception {
		String classDomain = classname.replace('.', '/');
		String workingDirectory = new File(Utility.workingDirectory).getAbsolutePath();
		String classpath = workingDirectory + "/Robot/bin/";

		System.out.println("classpath = " + classpath);

		File classpathDir = new File(classpath);
		if (!classpathDir.exists()) {
			new File(classpath).mkdirs();
		}

		String javaFile = workingDirectory + "/Robot/src/" + classDomain + ".java";
		System.out.println("javaFile = " + javaFile);

		String cmd = "javac -encoding UTF-8 -classpath " + classpath + " -d " + classpath + " " + javaFile;
		System.out.println("cmd = " + cmd);

		Runtime run = Runtime.getRuntime();
		File classFile = new File(classpath + classDomain + ".class");
		Process process = null;
		if (SystemUtils.IS_OS_WINDOWS) {
			System.out.println("SystemUtils.IS_OS_WINDOWS");
			process = run.exec("cmd /c " + cmd);
		} else {
			System.out.println("SystemUtils.IS_OS_LINUX");
			process = run.exec(new String[] { "/bin/sh", "-c", cmd });
		}
		int exitCode = process.waitFor();

		if (exitCode != 0) {
			System.out.println(cmd + " \nexitCode = " + exitCode);
			for (String s : fetchResult(process)) {
				System.out.println(s);
			}

			throw new Exception(cmd + " \nexitCode = " + exitCode);
		}

		process.destroy();
		if (!classFile.exists()) {
			throw new Exception(classFile + " does not exist.");
		}
	}

	static public String[] getFileNames(String path, String filter) throws Exception {
		ArrayList<String> files = new ArrayList<String>();
		File fpath = new File(path);

		if (!fpath.isDirectory()) {
			throw new Exception("path should be a directory.");
		}

		File[] flist = fpath.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isFile()) {
				if (filter == null || flist[i].getName().endsWith(filter))
					files.add(flist[i].getName());
			}
		}
		return files.toArray(new String[files.size()]);
	}

	static public String generate(String classpath, String filePath, String clazz, String... jars) {
		StringBuilder java = new StringBuilder();
		//		java.append("java -classpath ");
		java.append("java -classpath ");
		if (classpath == null) {
			java.append(filePath + jars[0]);
		} else {
			java.append(classpath);
			java.append(";" + filePath + jars[0]);
		}

		for (int i = 1; i < jars.length; ++i) {
			if (!jars[i].endsWith(".jar"))
				jars[i] += ".jar";
			java.append(";" + filePath + jars[i]);

		}

		java.append(" " + clazz);
		return java.toString();
	}

	static public String generateFromFolder(String filePath, String clazz, String jarsPath) throws Exception {
		return generate(null, filePath, clazz, getFileNames(jarsPath, "jar"));
	}

	public static void main(String[] args) throws Exception {
				String classpath = Utility.workingDirectory + "Stanford/bin";		
				String mainClass = "edu.stanford.nlp.wordseg.SegCoach";
		String jarFolder = Utility.workingDirectory + "QASystem/WebContent/WEB-INF/lib/";
//		String classpath = Utility.workingDirectory + "FudanNLP/bin";
//		String mainClass = "edu.fudan.nlp.cn.tag.CWSTagger";

		String jars[] = { "Utility.jar", "log4j-1.2.16", "poi-3.9", "poi-ooxml-3.9", "poi-ooxml-schemas-3.9", "trove" };
		System.out.println(generate(classpath, jarFolder, mainClass, jars));

		//		System.out.println(generate("/opt/ucc/apache-tomcat-7.0.47/webapps/QASystem/WEB-INF/lib/", Utility.workingDirectory + "QASystem/WebContent/WEB-INF/lib/", "com.robot.QASystemInvoker"));
		//		Utility.workingDirectory = "D:\\solution/";
		//		String str = "1-1";
		//		System.out.println("str = " + str);
		//		System.out.println(str + " = " + java_evaluate(str));
		//		str = "4 * 9";
		//		System.out.println("str = " + str);
		//		System.out.println(str + " = " + java_evaluate(str));
		//		str = "1 / 5.8";
		//		System.out.println("str = " + str);
		//		System.out.println(str + " = " + java_evaluate(str));
	}

	static String[] fetchResult(Process process) throws IOException {
		ArrayList<String> res = new ArrayList<String>();
		// 取得命令结果的输出流
		InputStream is = process.getInputStream();
		// 用一个读输出流类去读
		InputStreamReader isr = new InputStreamReader(is);
		// 用缓冲器读行
		BufferedReader br = new BufferedReader(isr);
		for (;;) {
			String str = br.readLine();
			if (str != null) {
				// System.out.println(str);
				res.add(str);
			} else
				break;
		}

		is.close();
		isr.close();
		br.close();
		return res.toArray(new String[res.size()]);
	}
	// private static Logger log = Logger.getLogger(Compiler.class);
}

//log4j.rootLogger=info, Console, RollingFile
//#RollingFile
//log4j.appender.RollingFile=org.apache.log4j.DailyRollingFileAppender
//log4j.appender.RollingFile.File=../logs/error.log 
//log4j.appender.RollingFile.layout=org.apache.log4j.PatternLayout
//log4j.appender.RollingFile.layout.ConversionPattern=%d{yyyy.mm.dd HH:mm:ss,SSS} [%t] %-5p [%c] - %m%n
