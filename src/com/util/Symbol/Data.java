package com.util.Symbol;

public class Data {
	public static abstract class HData {
		public abstract String toString();
		abstract public HData neg();
		abstract public HData reciprocal();
		
		abstract public HData add(HData y);
		abstract public HData sub(HData y);
		abstract public HData mul(HData y);
		abstract public HData div(HData y);
		
		abstract public HData add(HData1 y);
		abstract public HData sub(HData1 y);
		public HData mul(HData1 y){
			return this;
		}
		
		public HData div(HData1 y){
			return this;
		}
		
		abstract public HData add(HData1N y);
		abstract public HData sub(HData1N y);
		
		public HData mul(HData1N y){
			return neg();
		}
		public HData div(HData1N y){
			return neg();
		}
	}

	public static abstract class HDataINTEGER extends HData {

	}

	public static class HData0 extends HDataINTEGER {

		@Override
		public String toString() {
			return "0";
		}

		@Override
		public HData add(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData mul(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData div(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData add(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData mul(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		

		@Override
		public HData add(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData neg() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData reciprocal() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static class HData1 extends HDataINTEGER {

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "1";
		}

		@Override
		public HData add(HData y) {
			return y.add(this);
		}

		@Override
		public HData sub(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData mul(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData div(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData add(HData1 y) {
			return new HDatax(2);
		}

		@Override
		public HData sub(HData1 y) {
			// TODO Auto-generated method stub
			return new HData0();
		}

		@Override
		public HData add(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData neg() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData reciprocal() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static class HData1N extends HDataINTEGER {

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "-1";
		}

		@Override
		public HData add(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData mul(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData div(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData add(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData add(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData neg() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData reciprocal() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static class HDatax extends HDataINTEGER {
		long x;
		HDatax(long x){
			this.x = x;
		}
		@Override
		public String toString() {
			// TODO Auto-generated method stub

			return ((Long) x).toString();
		}

		@Override
		public HData add(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData mul(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData div(HData y) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public HData add(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public HData sub(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		public HData add(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public HData sub(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public HData neg() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public HData reciprocal() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static class HDataxV extends HDataINTEGER {
		long x[];
		HDataxV(long x[]){
			this.x = x;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData add(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData mul(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData div(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData add(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}
		

		@Override
		public HData add(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData neg() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData reciprocal() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static class HDataxN extends HDataINTEGER {
		long x;
		HDataxN(long x){
			this.x = x;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "-" + (Long) x;
		}
		@Override
		public HData add(HData y) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public HData sub(HData y) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public HData mul(HData y) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public HData div(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData add(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData add(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData neg() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData reciprocal() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static class HDataxVN extends HDataINTEGER {
		long x[];
		HDataxVN(long x[]){
			this.x = x;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData add(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData mul(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData div(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData add(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData mul(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		

		@Override
		public HData add(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData neg() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData reciprocal() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	// HData *pow2(dword m);

	public static class HDataRATIONAL extends HData {
		HDataINTEGER x, y;

		@Override
		public String toString() {
			return x.toString() + "/" + y;
		}

		@Override
		public HData add(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData mul(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData div(HData y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData add(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData mul(HData1 y) {
			// TODO Auto-generated method stub
			return null;
		}

		

		@Override
		public HData add(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData sub(HData1N y) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData neg() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HData reciprocal() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static void main(String[] args) {
		System.out.println("public static void main(String[] args)");
	}

}
