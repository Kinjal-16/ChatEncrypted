public class Test {
    public static void main(String args[])
    {
        String str="Client 0:6.0";
        String strln[]=str.split(":");
        System.out.println(strln[0]);
        System.out.println(Double.parseDouble(strln[1]));
    }
}
