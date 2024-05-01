import java.io.*;
public class Grader {
    private static int limit = 21;
    private static int global = 1;
    private static String problem = "indexing/";
    public static void main(String[] args) throws Exception {
        Interpreter runner = new Interpreter();
        BufferedReader buffer;
        int test = 1;
        try{
            String runresult = runner.initcode(limit, global, problem + "in" + test + ".txt");
            buffer = new BufferedReader(new FileReader(problem + "out" + test + ".txt"));
            String expected = buffer.readLine();
            while (runresult.equals(expected)){
                test ++;
                buffer = new BufferedReader(new FileReader(problem + "out" + test + ".txt"));
                expected = buffer.readLine();
                runresult = runner.initcode(limit, global, problem + "in" + test + ".txt");
            }
            System.out.println("failed test " + test);
            if (test == 1){
                System.out.println("expected: " + expected + "\nreceived: " + runresult);
            }
        }
        catch (Exception e){
            System.out.println("passed all tests (" + (test - 1) + ")");
        }
    }
}
